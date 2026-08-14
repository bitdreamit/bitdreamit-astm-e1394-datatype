package com.bitdreamit.mirth.astm.e1394.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class ASTME1394FromXmlConverter {
    private ASTME1394SerializationProperties props;

    public ASTME1394FromXmlConverter(ASTME1394SerializationProperties props) { this.props = props; }

    public String convert(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) return "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(props.getEncoding())));

        StringBuilder astm = new StringBuilder();
        Element root = doc.getDocumentElement();
        NodeList records = root.getChildNodes();

        for (int i = 0; i < records.getLength(); i++) {
            Node recordNode = records.item(i);
            if (recordNode.getNodeType() != Node.ELEMENT_NODE) continue;
            String recordType = mapTagToRecordType(recordNode.getNodeName());
            astm.append(recordType);

            NodeList fields = recordNode.getChildNodes();
            for (int j = 0; j < fields.getLength(); j++) {
                Node fieldNode = fields.item(j);
                if (fieldNode.getNodeType() != Node.ELEMENT_NODE) continue;
                astm.append(props.getFieldDelimiter());

                NodeList values = fieldNode.getChildNodes();
                List<String> repetitions = new ArrayList<>();
                for (int k = 0; k < values.getLength(); k++) {
                    Node valNode = values.item(k);
                    if (valNode.getNodeType() == Node.ELEMENT_NODE) repetitions.add(valNode.getTextContent());
                }

                if (repetitions.size() == 1) {
                    astm.append(escapeDelimiters(repetitions.get(0)));
                } else {
                    for (int r = 0; r < repetitions.size(); r++) {
                        if (r > 0) astm.append(props.getRepeatDelimiter());
                        astm.append(escapeDelimiters(repetitions.get(r)));
                    }
                }
            }
            astm.append(props.getRecordDelimiter());
        }
        return astm.toString();
    }

    private String mapTagToRecordType(String tagName) {
        if ("Header".equalsIgnoreCase(tagName)) return "H";
        if ("Patient".equalsIgnoreCase(tagName)) return "P";
        if ("Order".equalsIgnoreCase(tagName)) return "O";
        if ("Result".equalsIgnoreCase(tagName)) return "R";
        if ("Query".equalsIgnoreCase(tagName)) return "Q";
        if ("Comment".equalsIgnoreCase(tagName)) return "C";
        if ("Manufacturer".equalsIgnoreCase(tagName)) return "M";
        if ("Terminator".equalsIgnoreCase(tagName)) return "L";
        return tagName.substring(0, 1);
    }

    private String escapeDelimiters(String text) {
        if (text == null) return "";
        String esc = props.getEscapeCharacter();
        text = text.replace(props.getFieldDelimiter(), esc + "F" + esc);
        text = text.replace(props.getRepeatDelimiter(), esc + "R" + esc);
        text = text.replace(props.getComponentDelimiter(), esc + "C" + esc);
        return text;
    }
}
