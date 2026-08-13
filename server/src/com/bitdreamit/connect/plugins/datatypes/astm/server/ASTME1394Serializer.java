package com.bitdreamit.connect.plugins.datatypes.astm.server;

import org.w3c.dom.*;

public class ASTME1394Serializer {

    private final ASTME1394SerializationProperties props;

    public ASTME1394Serializer(ASTME1394SerializationProperties props) {
        this.props = props;
    }

    public String fromXML(Document doc) {
        char fieldDelim = props.getFieldDelimiter();
        char repeatDelim = props.getRepeatDelimiter();
        char componentDelim = props.getComponentDelimiter();
        char escapeChar = props.getEscapeCharacter();
        ASTME1394EscapeUtil esc = new ASTME1394EscapeUtil(escapeChar, fieldDelim, repeatDelim, componentDelim);

        StringBuilder out = new StringBuilder();
        Element root = doc.getDocumentElement();
        NodeList records = root.getChildNodes();

        for (int r = 0; r < records.getLength(); r++) {
            Node recordNode = records.item(r);
            if (recordNode.getNodeType() != Node.ELEMENT_NODE) continue;
            Element recordEl = (Element) recordNode;

            StringBuilder line = new StringBuilder(recordEl.getTagName());
            NodeList fields = recordEl.getChildNodes();
            for (int f = 0; f < fields.getLength(); f++) {
                Node fieldNode = fields.item(f);
                if (fieldNode.getNodeType() != Node.ELEMENT_NODE) continue;
                line.append(fieldDelim);
                line.append(serializeField((Element) fieldNode, repeatDelim, componentDelim, esc));
            }
            out.append(line).append("\r");
        }
        return out.toString();
    }

    private String serializeField(Element fieldEl, char repeatDelim, char componentDelim, ASTME1394EscapeUtil esc) {
        NodeList repeatNodes = fieldEl.getElementsByTagName("Repeat");
        if (repeatNodes.getLength() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < repeatNodes.getLength(); i++) {
                if (i > 0) sb.append(repeatDelim);
                sb.append(serializeComponents((Element) repeatNodes.item(i), componentDelim, esc));
            }
            return sb.toString();
        }
        return serializeComponents(fieldEl, componentDelim, esc);
    }

    private String serializeComponents(Element container, char componentDelim, ASTME1394EscapeUtil esc) {
        NodeList children = container.getChildNodes();
        boolean hasElementChildren = false;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) { hasElementChildren = true; break; }
        }
        if (!hasElementChildren) {
            String text = container.getTextContent();
            return text == null ? "" : esc.escape(text);
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!first) sb.append(componentDelim);
            sb.append(esc.escape(n.getTextContent()));
            first = false;
        }
        return sb.toString();
    }
}
