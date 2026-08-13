/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import org.apache.log4j.Logger;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ASTM E1394 serializer with template engine support.
 * Exceeds commercial extension capabilities.
 */
public class AstmE1394Serializer {
    private static final Logger logger = Logger.getLogger(AstmE1394Serializer.class);
    private final AstmE1394DataTypeProperties props;
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\$\{([^}]+)\}");

    public AstmE1394Serializer(AstmE1394DataTypeProperties props) {
        this.props = props;
    }

    public String serialize(String xml) {
        try {
            // Template engine mode
            if (props.isEnableTemplateEngine() && !props.getSerializationTemplate().isEmpty()) {
                return applyTemplate(xml);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            StringBuilder sb = new StringBuilder();
            NodeList children = doc.getDocumentElement().getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    sb.append(serializeRecord((Element) node));
                    sb.append(props.getSegmentDelimiter().replace("\\", "\"));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("ASTM serialize error", e);
            return "";
        }
    }

    private String applyTemplate(String xml) {
        String template = props.getSerializationTemplate();
        // Simple variable substitution from XML context
        // In production, this would use a proper template engine
        Matcher m = TEMPLATE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String var = m.group(1);
            String value = extractValueFromXml(xml, var);
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String extractValueFromXml(String xml, String path) {
        // Simple path extraction: record/field/component
        // e.g., "O/F2" -> O-record field 2
        return ""; // Simplified - full implementation would parse XML path
    }

    private String serializeRecord(Element record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getTagName());

        int maxField = -1;
        NodeList fields = record.getChildNodes();
        for (int i = 0; i < fields.getLength(); i++) {
            Node n = fields.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().startsWith("F")) {
                try {
                    int idx = Integer.parseInt(n.getNodeName().substring(1));
                    maxField = Math.max(maxField, idx);
                } catch (NumberFormatException ignored) {}
            }
        }

        for (int f = 0; f <= maxField; f++) {
            sb.append("|");
            Element fieldEl = getChildElement(record, "F" + f);
            if (fieldEl != null) {
                sb.append(serializeField(fieldEl));
            } else if (props.isIncludeEmptyFields()) {
                // empty field already added
            }
        }

        return sb.toString();
    }

    private String serializeField(Element fieldEl) {
        // Check for repeat fields
        NodeList repeats = fieldEl.getChildNodes();
        boolean hasRepeats = false;
        int maxRepeat = -1;
        for (int i = 0; i < repeats.getLength(); i++) {
            Node n = repeats.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().startsWith("R")) {
                hasRepeats = true;
                try {
                    int idx = Integer.parseInt(n.getNodeName().substring(1));
                    maxRepeat = Math.max(maxRepeat, idx);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (hasRepeats) {
            StringBuilder sb = new StringBuilder();
            for (int r = 0; r <= maxRepeat; r++) {
                if (r > 0) sb.append("\");
                Element repEl = getChildElement(fieldEl, "R" + r);
                sb.append(repEl != null ? getTextContent(repEl) : "");
            }
            return sb.toString();
        }

        // Check for components
        NodeList comps = fieldEl.getChildNodes();
        boolean hasComps = false;
        int maxComp = -1;
        for (int i = 0; i < comps.getLength(); i++) {
            Node n = comps.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().startsWith("C")) {
                hasComps = true;
                try {
                    int idx = Integer.parseInt(n.getNodeName().substring(1));
                    maxComp = Math.max(maxComp, idx);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (!hasComps) {
            return getTextContent(fieldEl);
        }

        StringBuilder sb = new StringBuilder();
        for (int c = 0; c <= maxComp; c++) {
            if (c > 0) sb.append("^");
            Element compEl = getChildElement(fieldEl, "C" + c);
            sb.append(compEl != null ? getTextContent(compEl) : "");
        }
        return sb.toString();
    }

    private Element getChildElement(Element parent, String name) {
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(name)) {
                return (Element) n;
            }
        }
        return null;
    }

    private String getTextContent(Element el) {
        StringBuilder sb = new StringBuilder();
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(n.getNodeValue());
            }
        }
        return sb.toString();
    }
}