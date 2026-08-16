package com.bitdreamit.connect.plugins.datatypes.astm.shared;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;

/**
 * XML → ASTM E1394 converter.
 *
 * <p>Inverts {@link ASTME1394Deserializer#toXML(String)}. Reads the canonical
 * {@code <ASTM><H>…</H>…</ASTM>} document and emits an ASTM E1394 message
 * using the delimiters resolved from the supplied
 * {@link ASTME1394SerializationProperties}.</p>
 *
 * <p>Round-trip contract: for any input that contains valid delimiters and
 * valid escape sequences, {@code fromXML(toXML(message)) == message} modulo
 * trailing-record-delimiter differences.</p>
 *
 * <p>Element-name conventions (must mirror {@link ASTME1394Deserializer}):</p>
 * <ul>
 *   <li>{@code <H>}, {@code <P>}, {@code <O>}, … — record elements (one letter)</li>
 *   <li>{@code <F1>}, {@code <F2>}, … — field elements inside a record</li>
 *   <li>{@code <R>} — repeat wrapper element inside a field</li>
 *   <li>{@code <C1>}, {@code <C2>}, … — component elements inside a field or repeat</li>
 * </ul>
 */
public class ASTME1394FromXmlConverter {

    private static final Logger logger = Logger.getLogger(ASTME1394FromXmlConverter.class);

    private static final Pattern FIELD_TAG_PATTERN     = Pattern.compile("F(\\d+)");
    private static final Pattern COMPONENT_TAG_PATTERN = Pattern.compile("C(\\d+)");

    private final ASTME1394SerializationProperties props;

    public ASTME1394FromXmlConverter(ASTME1394SerializationProperties props) {
        this.props = (props != null) ? props : new ASTME1394SerializationProperties();
    }

    public String convert(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            return "";
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(props.getEncoding())));
        return convert(doc);
    }

    /**
     * Convert a pre-parsed {@link Document} into ASTM E1394 text. Useful for
     * transformer pipelines that already hold the DOM.
     */
    public String convert(Document doc) throws Exception {
        if (doc == null) {
            return "";
        }
        StringBuilder astm = new StringBuilder(256);
        Element root = doc.getDocumentElement();
        if (root == null) {
            return "";
        }

        char fieldDelim     = props.getFieldDelimiter();
        char repeatDelim    = props.getRepeatDelimiter();
        char componentDelim = props.getComponentDelimiter();
        char escapeChar     = props.getEscapeCharacter();
        char recordDelim    = props.getRecordDelimiter();
        ASTME1394EscapeUtil esc = new ASTME1394EscapeUtil(escapeChar, fieldDelim, repeatDelim, componentDelim);

        NodeList records = root.getChildNodes();
        for (int i = 0; i < records.getLength(); i++) {
            Node recordNode = records.item(i);
            if (recordNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element recordEl = (Element) recordNode;
            String recordType = mapTagToRecordType(recordEl.getTagName());

            // First field of every ASTM record is the record-type letter itself.
            astm.append(recordType);

            // Collect direct child elements that represent fieldN (tag matches F\d+).
            List<Element> fieldElements = new ArrayList<Element>();
            NodeList fieldNodes = recordEl.getChildNodes();
            for (int j = 0; j < fieldNodes.getLength(); j++) {
                Node n = fieldNodes.item(j);
                if (n.getNodeType() == Node.ELEMENT_NODE && isFieldTag(n.getNodeName())) {
                    fieldElements.add((Element) n);
                }
            }

            // Reconstruct each field in positional order; missing positions
            // produce empty fields so the field index is preserved.
            int maxIndex = 0;
            for (Element f : fieldElements) {
                int idx = parseFieldIndex(f.getTagName());
                if (idx > maxIndex) maxIndex = idx;
            }

            // Field 1 (F1) holds the record-type letter — already emitted
            // above as `recordType`. Start reconstruction at F2.
            int startIdx = Math.max(2, 1);
            for (int idx = startIdx; idx <= maxIndex; idx++) {
                astm.append(fieldDelim);
                Element f = findFieldByIndex(fieldElements, idx);
                if (f != null) {
                    // Field 2 of the header record contains the delimiter
                    // definition itself — preserve verbatim (don't escape).
                    boolean isHeaderDelimDef = "H".equals(recordType) && idx == 2;
                    if (isHeaderDelimDef) {
                        astm.append(f.getTextContent());
                    } else {
                        astm.append(serializeField(f, esc, repeatDelim, componentDelim));
                    }
                }
            }

            // If there's only F1 (no other fields), still emit a trailing
            // delimiter so the message round-trips correctly.
            if (maxIndex < 2) {
                astm.append(fieldDelim);
            }

            astm.append(recordDelim);
        }
        return astm.toString();
    }

    private String serializeField(Element fieldEl, ASTME1394EscapeUtil esc,
                                  char repeatDelim, char componentDelim) {
        StringBuilder sb = new StringBuilder();
        NodeList children = fieldEl.getChildNodes();

        // Determine whether the field contains <R> children (multi-valued).
        List<Element> repeatEls = new ArrayList<Element>();
        List<Element> componentEls = new ArrayList<Element>();
        boolean hasTextContent = false;
        StringBuilder textContent = new StringBuilder();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if ("R".equals(n.getNodeName())) {
                    repeatEls.add((Element) n);
                } else if (isComponentTag(n.getNodeName())) {
                    componentEls.add((Element) n);
                }
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                textContent.append(n.getNodeValue());
                hasTextContent = true;
            }
        }

        if (!repeatEls.isEmpty()) {
            // Multi-valued field — join repeats with repeatDelim.
            for (int r = 0; r < repeatEls.size(); r++) {
                if (r > 0) sb.append(repeatDelim);
                sb.append(serializeRepeat(repeatEls.get(r), esc, componentDelim));
            }
        } else if (!componentEls.isEmpty()) {
            // Single repeat with multiple components.
            sb.append(serializeComponents(componentEls, esc, componentDelim));
        } else if (hasTextContent) {
            sb.append(esc.escape(textContent.toString()));
        }
        return sb.toString();
    }

    private String serializeRepeat(Element repeatEl, ASTME1394EscapeUtil esc, char componentDelim) {
        List<Element> componentEls = new ArrayList<Element>();
        StringBuilder textContent = new StringBuilder();
        boolean hasTextContent = false;
        NodeList children = repeatEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && isComponentTag(n.getNodeName())) {
                componentEls.add((Element) n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                textContent.append(n.getNodeValue());
                hasTextContent = true;
            }
        }
        if (!componentEls.isEmpty()) {
            return serializeComponents(componentEls, esc, componentDelim);
        }
        if (hasTextContent) {
            return esc.escape(textContent.toString());
        }
        return "";
    }

    private String serializeComponents(List<Element> componentEls, ASTME1394EscapeUtil esc, char componentDelim) {
        // Order components by their index, then join with componentDelim.
        int maxIdx = 0;
        for (Element c : componentEls) {
            int idx = parseComponentIndex(c.getTagName());
            if (idx > maxIdx) maxIdx = idx;
        }
        StringBuilder sb = new StringBuilder();
        for (int idx = 1; idx <= maxIdx; idx++) {
            if (idx > 1) sb.append(componentDelim);
            Element c = findComponentByIndex(componentEls, idx);
            if (c != null) {
                sb.append(esc.escape(c.getTextContent()));
            }
        }
        return sb.toString();
    }

    private boolean isFieldTag(String name) {
        return name != null && FIELD_TAG_PATTERN.matcher(name).matches();
    }

    private boolean isComponentTag(String name) {
        return name != null && COMPONENT_TAG_PATTERN.matcher(name).matches();
    }

    private int parseFieldIndex(String tag) {
        java.util.regex.Matcher m = FIELD_TAG_PATTERN.matcher(tag);
        if (m.matches()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private int parseComponentIndex(String tag) {
        java.util.regex.Matcher m = COMPONENT_TAG_PATTERN.matcher(tag);
        if (m.matches()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private Element findFieldByIndex(List<Element> elements, int idx) {
        for (Element e : elements) {
            if (parseFieldIndex(e.getTagName()) == idx) return e;
        }
        return null;
    }

    private Element findComponentByIndex(List<Element> elements, int idx) {
        for (Element e : elements) {
            if (parseComponentIndex(e.getTagName()) == idx) return e;
        }
        return null;
    }

    /**
     * Map an XML tag name back to the single-letter ASTM record type. Accepts
     * both the literal single-letter form ({@code "H"}, {@code "P"}, …) and the
     * long-form aliases that may be produced by upstream transformers.
     */
    private String mapTagToRecordType(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return ASTME1394Constants.RECORD_HEADER;
        }
        if ("Header".equalsIgnoreCase(tagName))       return ASTME1394Constants.RECORD_HEADER;
        if ("Patient".equalsIgnoreCase(tagName))      return ASTME1394Constants.RECORD_PATIENT;
        if ("Order".equalsIgnoreCase(tagName))        return ASTME1394Constants.RECORD_ORDER;
        if ("Result".equalsIgnoreCase(tagName))       return ASTME1394Constants.RECORD_RESULT;
        if ("Query".equalsIgnoreCase(tagName))        return ASTME1394Constants.RECORD_QUERY;
        if ("Comment".equalsIgnoreCase(tagName))      return ASTME1394Constants.RECORD_COMMENT;
        if ("Manufacturer".equalsIgnoreCase(tagName)) return ASTME1394Constants.RECORD_MANUFACTURER;
        if ("Terminator".equalsIgnoreCase(tagName))  return ASTME1394Constants.RECORD_TERMINATOR;
        // Single-letter form — return as-is.
        return tagName.substring(0, 1);
    }
}
