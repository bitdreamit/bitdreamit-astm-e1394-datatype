package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.io.StringWriter;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;

/**
 * ASTM E1394 → XML converter.
 *
 * <p>Serializes an inbound ASTM E1394 message into Mirth's canonical XML form:
 * </p>
 *
 * <pre>{@code
 * <ASTM>
 *   <H><F1>H</F1><F2>\^&amp;</F2>...<F5><C1>BitDreamLIS</C1><C2>1.0</C2></F5>...</H>
 *   <P><F1>P</F1><F2>1</F2>...</P>
 *   ...
 *   <L><F1>L</F1><F2>1</F2><F3>N</F3></L>
 * </ASTM>
 * }</pre>
 *
 * <p>The first element of every record (the record-type letter) is preserved as
 * field {@code <F1>} so the XML round-trips byte-for-byte through
 * {@link ASTME1394Serializer#fromXML(String)}. Delimiter definition in the
 * header record (field {@code <F2>}) is preserved verbatim and is NOT split
 * into components, because doing so would corrupt the delimiter-definition
 * field.</p>
 *
 * <p>Element names use letter prefixes ({@code F} for field, {@code C} for
 * component, {@code R} for repeat) because XML element names cannot start
 * with a digit.</p>
 *
 * <p>Header-delimiter derivation is supported: when
 * {@link ASTME1394DeserializationProperties#isDeriveDelimitersFromHeader()}
 * is {@code true}, the parser extracts the delimiter characters from the
 * second field of the {@code H} record.</p>
 */
public class ASTME1394Deserializer {

    private static final Logger logger = Logger.getLogger(ASTME1394Deserializer.class);

    private final ASTME1394DeserializationProperties props;

    public ASTME1394Deserializer(ASTME1394DeserializationProperties props) {
        this.props = (props != null) ? props : new ASTME1394DeserializationProperties();
    }

    /**
     * Convert an ASTM E1394 raw-text message into the canonical XML representation.
     *
     * @param astmMessage raw ASTM text; may be {@code null} (returns {@code null})
     * @return XML string rooted at {@code <ASTM>}
     * @throws Exception on parser / DOM failures
     */
    public String toXML(String astmMessage) throws Exception {
        if (astmMessage == null) {
            return null;
        }
        if (astmMessage.isEmpty()) {
            return wrapEmptyDocument();
        }

        // Resolve effective delimiters (default vs. header-derived).
        char fieldDelim     = props.getFieldDelimiter();
        char repeatDelim    = props.getRepeatDelimiter();
        char componentDelim = props.getComponentDelimiter();
        char escapeChar     = props.getEscapeCharacter();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        Element root = doc.createElement(ASTME1394Constants.XML_ROOT_ELEMENT);
        doc.appendChild(root);

        // Normalize line endings: ASTM uses CR; tolerate CRLF / LF on inbound.
        String normalized = astmMessage.replace("\r\n", "\r").replace("\n", "\r");
        String[] lines = normalized.split("\r", -1);

        boolean headerProcessed = false;

        for (String line : lines) {
            if (line == null || line.isEmpty()) {
                continue;
            }

            // The leading character of an ASTM record identifies its type.
            // If a control / framing character leaked through, skip the line.
            char firstChar = line.charAt(0);
            if (firstChar < 0x20) {
                continue;
            }
            String recordType = line.substring(0, 1);

            // Derive delimiters from header field 2 when enabled.
            if (!headerProcessed && "H".equals(recordType) && props.isDeriveDelimitersFromHeader()) {
                Delimiters derived = deriveDelimitersFromHeader(line);
                if (derived != null) {
                    fieldDelim     = derived.field;
                    repeatDelim    = derived.repeat;
                    componentDelim = derived.component;
                    escapeChar     = derived.escape;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Derived delimiters from header: escape='" + escapeChar
                                + "' field='" + fieldDelim + "' repeat='" + repeatDelim
                                + "' component='" + componentDelim + "'");
                    }
                }
                headerProcessed = true;
            }

            Element recordEl = doc.createElement(sanitizeTag(recordType));
            root.appendChild(recordEl);

            String[] fields = splitRespectingHeaderException(line, recordType, fieldDelim);
            ASTME1394EscapeUtil esc = new ASTME1394EscapeUtil(escapeChar, fieldDelim, repeatDelim, componentDelim);

            for (int i = 0; i < fields.length; i++) {
                Element fieldEl = doc.createElement("F" + (i + 1));
                recordEl.appendChild(fieldEl);

                // Field 2 of the header record contains the delimiter definition
                // itself (e.g. "\^&") — its component chars ARE the delimiters,
                // so splitting on them would corrupt the field. Preserve verbatim.
                boolean isHeaderDelimDef = "H".equals(recordType) && i == 1;
                if (isHeaderDelimDef) {
                    fieldEl.setTextContent(fields[i]);
                } else {
                    appendFieldContent(doc, fieldEl, fields[i], repeatDelim, componentDelim, esc);
                }
            }
        }

        return serializeDocument(doc);
    }

    /**
     * Convert an ASTM E1394 raw-text message into an XML {@link Document}.
     *
     * <p>Useful when callers (e.g. transformer steps) want to inspect the DOM
     * directly rather than re-parsing the XML string.</p>
     */
    public Document toDocument(String astmMessage) throws Exception {
        String xml = toXML(astmMessage);
        if (xml == null) {
            return null;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        return dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes(props.getEncoding())));
    }

    // -----------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------

    private static final class Delimiters {
        char field, repeat, component, escape;
    }

    /**
     * Extract delimiters from the header's second field.
     *
     * <p>ASTM E1394 instruments encode the delimiter set in field 2 of the
     * header record. Two conventions are in widespread use:</p>
     *
     * <ul>
     *   <li><b>3-char form</b> (most common): {@code <repeat><component><escape>}.
     *       The field separator is always {@code |} and is not encoded.</li>
     *   <li><b>4-char form</b> (uncommon, non-standard): {@code <escape><field><repeat><component>}.
     *       The field separator in the delimDef is informational only — the
     *       parser continues to use {@code |} since that is what was used to
     *       locate the delimDef in the first place.</li>
     * </ul>
     *
     * <p>For example, the default delimiters produce the delimDef {@code \^&}
     * (3-char form: repeat={@code \}, component={@code ^}, escape={@code &}).</p>
     */
    private Delimiters deriveDelimitersFromHeader(String headerLine) {
        char defaultField = ASTME1394Constants.DEFAULT_FIELD_DELIMITER_CHAR;
        int firstDelim = headerLine.indexOf(defaultField);
        if (firstDelim < 0 || headerLine.length() <= firstDelim + 1) {
            return null;
        }
        int secondDelim = headerLine.indexOf(defaultField, firstDelim + 1);
        if (secondDelim < 0) {
            return null;
        }
        String delimDef = headerLine.substring(firstDelim + 1, secondDelim);
        Delimiters d = new Delimiters();
        d.field = defaultField;  // Always '|' — we located delimDef using it.

        if (delimDef.length() == 3) {
            // Standard 3-char form: <repeat><component><escape>
            d.repeat    = delimDef.charAt(0);
            d.component = delimDef.charAt(1);
            d.escape    = delimDef.charAt(2);
        } else if (delimDef.length() >= 4) {
            // Non-standard 4-char form: <escape><field><repeat><component>
            // (We honour the escape/repeat/component; field stays as '|'.)
            d.escape    = delimDef.charAt(0);
            // delimDef.charAt(1) is the claimed field separator — ignored.
            d.repeat    = delimDef.charAt(2);
            d.component = delimDef.charAt(3);
        } else {
            return null;
        }
        return d;
    }

    /**
     * Split a record into fields, treating the header's field-2 delimiter
     * definition as a literal value (so {@code \^&} is not itself split on
     * {@code |}).
     */
    private String[] splitRespectingHeaderException(String line, String recordType, char fieldDelim) {
        if (!"H".equals(recordType)) {
            return line.split(Pattern.quote(String.valueOf(fieldDelim)), -1);
        }
        int firstDelim  = line.indexOf(fieldDelim);
        int secondDelim = (firstDelim >= 0) ? line.indexOf(fieldDelim, firstDelim + 1) : -1;
        if (firstDelim < 0 || secondDelim < 0) {
            return line.split(Pattern.quote(String.valueOf(fieldDelim)), -1);
        }
        String field1 = line.substring(0, firstDelim);
        String field2 = line.substring(firstDelim + 1, secondDelim);
        String rest   = line.substring(secondDelim + 1);
        String[] remaining = rest.isEmpty()
                ? new String[0]
                : rest.split(Pattern.quote(String.valueOf(fieldDelim)), -1);
        String[] out = new String[2 + remaining.length];
        out[0] = field1;
        out[1] = field2;
        System.arraycopy(remaining, 0, out, 2, remaining.length);
        return out;
    }

    /**
     * Append a single field's content to the parent {@code <FN>} element.
     * If the field contains repeats (separated by {@code repeatDelim}), each
     * repeat is wrapped in a {@code <R>} child element; otherwise the
     * value is set as the element's text content. Components within each
     * repeat are wrapped in numbered child elements ({@code <C1>}, {@code <C2>},
     * …) — or, if there is only one component, set as text content directly.
     */
    private void appendFieldContent(Document doc, Element fieldEl, String fieldValue,
                                    char repeatDelim, char componentDelim, ASTME1394EscapeUtil esc) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return;
        }
        String[] repeats = fieldValue.split(Pattern.quote(String.valueOf(repeatDelim)), -1);
        boolean hasRepeats = repeats.length > 1;
        for (String repeatVal : repeats) {
            Element container = hasRepeats ? doc.createElement("R") : fieldEl;
            if (hasRepeats) {
                fieldEl.appendChild(container);
            }
            String[] components = repeatVal.split(Pattern.quote(String.valueOf(componentDelim)), -1);
            if (components.length > 1) {
                for (int c = 0; c < components.length; c++) {
                    Element compEl = doc.createElement("C" + (c + 1));
                    compEl.setTextContent(esc.unescape(components[c]));
                    container.appendChild(compEl);
                }
            } else {
                container.setTextContent(esc.unescape(repeatVal));
            }
        }
    }

    /**
     * XML element names must start with a letter and contain only letters /
     * digits. The 8 ASTM record types all satisfy this, but we sanitize anyway
     * to be defensive against malformed input.
     */
    private String sanitizeTag(String recordType) {
        if (recordType != null && recordType.matches("[A-Za-z][A-Za-z0-9]*")) {
            return recordType;
        }
        return "REC_" + Integer.toHexString(recordType == null ? 0 : recordType.hashCode());
    }

    private String wrapEmptyDocument() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().newDocument();
        Element root = doc.createElement(ASTME1394Constants.XML_ROOT_ELEMENT);
        doc.appendChild(root);
        return serializeDocument(doc);
    }

    private String serializeDocument(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        // Compact output — matches Mirth's HL7v2.x serializer conventions and
        // produces byte-stable XML for round-trip testing.
        t.setOutputProperty(OutputKeys.INDENT, "no");
        t.setOutputProperty(OutputKeys.ENCODING, props.getEncoding());
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
