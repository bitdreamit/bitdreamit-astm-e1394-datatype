package com.bitdreamit.mirth.astm.e1394.server;

import java.util.List;

import org.apache.log4j.Logger;

import com.bitdreamit.mirth.astm.e1394.server.ASTME1394FrameParser.ASTMField;
import com.bitdreamit.mirth.astm.e1394.server.ASTME1394FrameParser.ASTMRecord;

/**
 * ASTM E1394 → XML Converter
 * Converts parsed ASTM records to Mirth-compatible XML.
 */
public class ASTME1394ToXmlConverter {
    private Logger logger = Logger.getLogger(this.getClass());
    private ASTME1394SerializationProperties serProps;
    private ASTME1394DeserializationProperties deserProps;
    private ASTME1394FrameParser parser;

    public ASTME1394ToXmlConverter(ASTME1394SerializationProperties serProps,
                                    ASTME1394DeserializationProperties deserProps) {
        this.serProps = serProps;
        this.deserProps = deserProps;
        this.parser = new ASTME1394FrameParser(serProps, deserProps);
    }

    public String convert(String raw) throws Exception {
        List<ASTMRecord> records = parser.parse(raw);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"").append(serProps.getEncoding()).append("\"?>\n");
        xml.append("<ASTMMessage>\n");

        for (ASTMRecord record : records) {
            String tagName = sanitizeTagName(record.getRecordType());
            xml.append("  <").append(tagName).append(">\n");

            List<ASTMField> fields = record.getFields();
            for (int i = 0; i < fields.size(); i++) {
                ASTMField field = fields.get(i);
                String fieldTag = "Field" + (i + 1);
                xml.append("    <").append(fieldTag).append(">\n");

                List<String> values = field.getValues();
                if (values.size() == 1) {
                    xml.append("      <Value>").append(escapeXml(values.get(0))).append("</Value>\n");
                } else {
                    for (int r = 0; r < values.size(); r++) {
                        xml.append("      <Repeat index=\"").append(r).append("\">")
                           .append(escapeXml(values.get(r))).append("</Repeat>\n");
                    }
                }

                xml.append("    </").append(fieldTag).append(">\n");
            }

            xml.append("  </").append(tagName).append(">\n");
        }

        xml.append("</ASTMMessage>");
        return xml.toString();
    }

    private String sanitizeTagName(String name) {
        if (name == null || name.isEmpty()) return "Record";
        // ASTM record types are single letters: H, P, O, R, Q, C, M, L
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
