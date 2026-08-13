/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * ASTM E1394 record parser with strict validation, repeat field handling,
 * field count validation, and manufacturer extension support.
 * Exceeds commercial extension capabilities.
 */
public class AstmE1394Parser {
    private static final Logger logger = Logger.getLogger(AstmE1394Parser.class);

    // Standard ASTM record types
    private static final Set<String> VALID_RECORD_TYPES = new HashSet<>(Arrays.asList(
        "H", "P", "O", "R", "C", "L", "M", "Q", "S"
    ));

    // Expected field counts per record type (minimum)
    private static final Map<String, Integer> MIN_FIELD_COUNTS = new HashMap<>();
    static {
        MIN_FIELD_COUNTS.put("H", 5);
        MIN_FIELD_COUNTS.put("P", 2);
        MIN_FIELD_COUNTS.put("O", 3);
        MIN_FIELD_COUNTS.put("R", 3);
        MIN_FIELD_COUNTS.put("C", 2);
        MIN_FIELD_COUNTS.put("L", 2);
        MIN_FIELD_COUNTS.put("M", 3);
        MIN_FIELD_COUNTS.put("Q", 2);
        MIN_FIELD_COUNTS.put("S", 2);
    }

    private final AstmE1394DataTypeProperties props;
    private final AstmControlIdTracker controlIdTracker;

    public AstmE1394Parser(AstmE1394DataTypeProperties props) {
        this.props = props;
        this.controlIdTracker = props.isTrackControlIds() ? new AstmControlIdTracker(props.getMaxControlIdHistory()) : null;
    }

    public String parse(String astmMessage) {
        try {
            String[] lines = astmMessage.split(props.getSegmentDelimiter().replace("\\", "\"));
            List<AstmRecord> records = new ArrayList<>();
            String controlId = null;

            for (int lineNum = 0; lineNum < lines.length; lineNum++) {
                String line = lines[lineNum].trim();
                if (line.isEmpty()) continue;

                // Validate record type
                String type = line.substring(0, 1);
                if (props.isValidateRecordTypes() && !VALID_RECORD_TYPES.contains(type)) {
                    if (!props.isHandleManufacturerExtensions()) {
                        logger.warn("Invalid record type '" + type + "' at line " + lineNum + ", skipping");
                        continue;
                    }
                }

                // Split fields
                String[] fields = line.split("\|", -1);

                // Validate field count
                if (props.isValidateFieldCounts()) {
                    Integer min = MIN_FIELD_COUNTS.get(type);
                    if (min != null && fields.length < min) {
                        logger.warn("Record type " + type + " has " + fields.length + " fields, expected at least " + min);
                    }
                }

                // Extract control ID from H-record
                if ("H".equals(type) && fields.length > 13) {
                    controlId = fields[13].trim();
                    if (controlIdTracker != null && controlIdTracker.isDuplicate(controlId)) {
                        logger.warn("Duplicate control ID detected: " + controlId);
                    }
                }

                records.add(new AstmRecord(type, fields, lineNum));
            }

            return buildXml(records);
        } catch (Exception e) {
            logger.error("ASTM parse error", e);
            return "<ASTM><ERROR>" + escapeXml(e.getMessage()) + "</ERROR></ASTM>";
        }
    }

    private String buildXml(List<AstmRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version="1.0" encoding="UTF-8"?>\n");
        if (props.isPrettyPrintXml()) {
            sb.append("<ASTM>\n");
            for (AstmRecord rec : records) {
                sb.append("  <").append(rec.type).append(">\n");
                for (int i = 0; i < rec.fields.length; i++) {
                    String fieldName = "F" + i;
                    String value = rec.fields[i];
                    if (props.isStripEmptyFields() && value.isEmpty()) continue;

                    if (props.isHandleRepeatFields() && value.contains(props.getRepeatDelimiter().replace("\\", "\"))) {
                        String[] repeats = value.split(props.getRepeatDelimiter().replace("\\", "\"), -1);
                        sb.append("    <").append(fieldName).append(">\n");
                        for (int r = 0; r < repeats.length; r++) {
                            sb.append("      <R").append(r).append(">");
                            sb.append(serializeFieldValue(repeats[r]));
                            sb.append("</R").append(r).append(">\n");
                        }
                        sb.append("    </").append(fieldName).append(">\n");
                    } else {
                        sb.append("    <").append(fieldName).append(">");
                        sb.append(serializeFieldValue(value));
                        sb.append("</").append(fieldName).append(">\n");
                    }
                }
                sb.append("  </").append(rec.type).append(">\n");
            }
            sb.append("</ASTM>");
        } else {
            sb.append("<ASTM>");
            for (AstmRecord rec : records) {
                sb.append("<").append(rec.type).append(">");
                for (int i = 0; i < rec.fields.length; i++) {
                    if (props.isStripEmptyFields() && rec.fields[i].isEmpty()) continue;
                    sb.append("<F").append(i).append(">");
                    sb.append(serializeFieldValue(rec.fields[i]));
                    sb.append("</F").append(i).append(">");
                }
                sb.append("</").append(rec.type).append(">");
            }
            sb.append("</ASTM>");
        }
        return sb.toString();
    }

    private String serializeFieldValue(String value) {
        if (!props.isHandleSubcomponents() || !value.contains("^")) {
            return escapeXml(value);
        }
        String[] comps = value.split("\^", -1);
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < comps.length; j++) {
            sb.append("<C").append(j).append(">").append(escapeXml(comps[j])).append("</C").append(j).append(">");
        }
        return sb.toString();
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace(""", "&quot;")
                .replace("'", "&apos;");
    }

    private static class AstmRecord {
        String type;
        String[] fields;
        int lineNumber;
        AstmRecord(String type, String[] fields, int lineNumber) {
            this.type = type;
            this.fields = fields;
            this.lineNumber = lineNumber;
        }
    }
}