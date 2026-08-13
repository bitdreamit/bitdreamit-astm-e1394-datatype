package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ASTME1394Deserializer {

    private final ASTME1394DeserializationProperties props;

    public ASTME1394Deserializer(ASTME1394DeserializationProperties props) {
        this.props = props;
    }

    public String toXML(String astmMessage) throws Exception {
        char fieldDelim = props.getFieldDelimiter();
        char repeatDelim = props.getRepeatDelimiter();
        char componentDelim = props.getComponentDelimiter();
        char escapeChar = props.getEscapeCharacter();

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("ASTM");
        doc.appendChild(root);

        String[] lines = astmMessage.split("\r\n|\r|\n");
        boolean headerProcessed = false;

        for (String line : lines) {
            if (line.isEmpty()) continue;
            String recordType = line.substring(0, 1);

            // Derive delimiters from Header record field 2 if enabled
            if (!headerProcessed && "H".equals(recordType) && props.isDeriveDelimitersFromHeader()) {
                Delimiters derived = deriveDelimitersFromHeader(line);
                if (derived != null) {
                    fieldDelim = derived.field;
                    repeatDelim = derived.repeat;
                    componentDelim = derived.component;
                    escapeChar = derived.escape;
                }
                headerProcessed = true;
            }

            Element recordEl = doc.createElement(sanitizeTag(recordType));
            root.appendChild(recordEl);

            String[] fields = splitRespectingHeaderException(line, recordType, fieldDelim);
            for (int i = 0; i < fields.length; i++) {
                Element fieldEl = doc.createElement(String.valueOf(i + 1));
                recordEl.appendChild(fieldEl);
                appendFieldContent(doc, fieldEl, fields[i], repeatDelim, componentDelim, escapeChar);
            }
        }
        return com.mirth.connect.util.XmlUtil.serialize(doc, false, true, true);
    }

    private static class Delimiters {
        char field, repeat, component, escape;
    }

    private Delimiters deriveDelimitersFromHeader(String headerLine) {
        int firstDelim = headerLine.indexOf(ASTME1394Constants.DEFAULT_FIELD_DELIMITER);
        if (firstDelim < 0 || headerLine.length() <= firstDelim + 1) return null;
        int secondDelim = headerLine.indexOf(ASTME1394Constants.DEFAULT_FIELD_DELIMITER, firstDelim + 1);
        if (secondDelim < 0) return null;
        String delimDef = headerLine.substring(firstDelim + 1, secondDelim);
        if (delimDef.length() < 4) return null;
        Delimiters d = new Delimiters();
        d.escape = delimDef.charAt(0);
        d.field = delimDef.charAt(1);
        d.repeat = delimDef.charAt(2);
        d.component = delimDef.charAt(3);
        return d;
    }

    private String[] splitRespectingHeaderException(String line, String recordType, char fieldDelim) {
        if (!"H".equals(recordType)) {
            return line.split(java.util.regex.Pattern.quote(String.valueOf(fieldDelim)), -1);
        }
        int firstDelim = line.indexOf(fieldDelim);
        int secondDelim = line.indexOf(fieldDelim, firstDelim + 1);
        if (firstDelim < 0 || secondDelim < 0) {
            return line.split(java.util.regex.Pattern.quote(String.valueOf(fieldDelim)), -1);
        }
        String field1 = line.substring(0, firstDelim);
        String field2 = line.substring(firstDelim + 1, secondDelim);
        String rest = line.substring(secondDelim + 1);
        String[] remaining = rest.isEmpty() ? new String[0]
                : rest.split(java.util.regex.Pattern.quote(String.valueOf(fieldDelim)), -1);
        String[] out = new String[2 + remaining.length];
        out[0] = field1;
        out[1] = field2;
        System.arraycopy(remaining, 0, out, 2, remaining.length);
        return out;
    }

    private void appendFieldContent(Document doc, Element fieldEl, String fieldValue,
                                     char repeatDelim, char componentDelim, char escapeChar) {
        ASTME1394EscapeUtil esc = new ASTME1394EscapeUtil(escapeChar, '|', repeatDelim, componentDelim);
        String[] repeats = fieldValue.split(java.util.regex.Pattern.quote(String.valueOf(repeatDelim)), -1);
        boolean hasRepeats = repeats.length > 1;
        for (String repeatVal : repeats) {
            Element container = hasRepeats ? doc.createElement("Repeat") : fieldEl;
            if (hasRepeats) fieldEl.appendChild(container);

            String[] components = repeatVal.split(java.util.regex.Pattern.quote(String.valueOf(componentDelim)), -1);
            if (components.length > 1) {
                for (int c = 0; c < components.length; c++) {
                    Element compEl = doc.createElement(String.valueOf(c + 1));
                    compEl.setTextContent(esc.unescape(components[c]));
                    container.appendChild(compEl);
                }
            } else {
                container.setTextContent(esc.unescape(repeatVal));
            }
        }
    }

    private String sanitizeTag(String recordType) {
        return recordType.matches("[A-Za-z][A-Za-z0-9]*") ? recordType : "REC_" + recordType.hashCode();
    }
}
