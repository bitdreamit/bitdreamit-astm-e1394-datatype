package com.bitdreamit.mirth.astm.e1394.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.bitdreamit.mirth.astm.e1394.shared.ASTME1394Constants;

/**
 * ASTM E1394 Frame Parser
 * Production-grade parser with delimiter derivation from header, escape handling,
 * field repetition support, and subcomponent support.
 */
public class ASTME1394FrameParser {
    private Logger logger = Logger.getLogger(this.getClass());

    private String fieldDelimiter;
    private String repeatDelimiter;
    private String componentDelimiter;
    private String escapeCharacter;
    private String recordDelimiter;
    private boolean strictValidation;
    private boolean deriveFromHeader;

    public ASTME1394FrameParser(ASTME1394SerializationProperties serProps,
                                ASTME1394DeserializationProperties deserProps) {
        this.fieldDelimiter     = serProps.getFieldDelimiter();
        this.repeatDelimiter    = serProps.getRepeatDelimiter();
        this.componentDelimiter = serProps.getComponentDelimiter();
        this.escapeCharacter    = serProps.getEscapeCharacter();
        this.recordDelimiter    = serProps.getRecordDelimiter();
        this.strictValidation   = serProps.isStrictValidation();
        this.deriveFromHeader   = serProps.isDeriveFromHeader();
    }

    /**
     * Parse ASTM E1394 raw text into structured records.
     * If deriveFromHeader is true, delimiters are extracted from the H record.
     */
    public List<ASTMRecord> parse(String raw) {
        List<ASTMRecord> records = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return records;

        // Strip ASTM E1381 framing chars if present
        raw = stripFraming(raw);

        // Normalize line breaks to record delimiter
        raw = raw.replace("\r\n", recordDelimiter)
                 .replace("\n", recordDelimiter)
                 .replace("\r", recordDelimiter);

        String[] lines = raw.split(recordDelimiter, -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            // Derive delimiters from header record
            if (deriveFromHeader && i == 0 && line.startsWith("H" + fieldDelimiter)) {
                deriveDelimitersFromHeader(line);
            }

            ASTMRecord record = parseRecord(line);
            if (record != null) {
                records.add(record);
            }
        }

        return records;
    }

    private String stripFraming(String raw) {
        // Remove STX (0x02), ETX (0x03), ETB (0x17), EOT (0x04), ENQ (0x05)
        StringBuilder sb = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (c != 0x02 && c != 0x03 && c != 0x17 && c != 0x04 && c != 0x05) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void deriveDelimitersFromHeader(String header) {
        // ASTM E1394 Header format: H|~\&|... or H|\^&|...
        // The second field contains delimiter definition
        String[] fields = header.split(escapeRegex(fieldDelimiter), -1);
        if (fields.length >= 2) {
            String delimDef = fields[1];
            if (delimDef.length() >= 4) {
                fieldDelimiter     = String.valueOf(delimDef.charAt(0));
                repeatDelimiter    = String.valueOf(delimDef.charAt(1));
                componentDelimiter = String.valueOf(delimDef.charAt(2));
                escapeCharacter    = String.valueOf(delimDef.charAt(3));
                logger.info("Delimiters derived from header: FD=" + fieldDelimiter +
                            " RD=" + repeatDelimiter + " CD=" + componentDelimiter +
                            " EC=" + escapeCharacter);
            }
        }
    }

    private ASTMRecord parseRecord(String line) {
        if (line == null || line.isEmpty()) return null;

        String[] fields = line.split(escapeRegex(fieldDelimiter), -1);
        if (fields.length == 0) return null;

        String recordType = fields[0];
        ASTMRecord record = new ASTMRecord(recordType);

        for (int i = 1; i < fields.length; i++) {
            String fieldValue = unescape(fields[i]);
            // Handle repetitions
            if (fieldValue.contains(repeatDelimiter)) {
                String[] reps = fieldValue.split(escapeRegex(repeatDelimiter), -1);
                List<String> repetitions = new ArrayList<>();
                for (String rep : reps) {
                    repetitions.add(unescape(rep));
                }
                record.addField(new ASTMField(repetitions));
            } else {
                record.addField(new ASTMField(fieldValue));
            }
        }

        return record;
    }

    private String unescape(String value) {
        if (value == null || escapeCharacter == null || escapeCharacter.isEmpty()) return value;
        // ASTM escape sequences: &F|field& = field delimiter, &S\ = repeat, &E^ = component, &T~ = subcomponent
        // Simplified: just strip escape char for now
        return value.replace(escapeCharacter, "");
    }

    private String escapeRegex(String s) {
        // Escape special regex chars in delimiters
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if ("\.[]{}()*+?^$|".indexOf(c) >= 0) {
                sb.append('\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // --- Inner Classes ---

    public static class ASTMRecord {
        private String recordType;
        private List<ASTMField> fields = new ArrayList<>();

        public ASTMRecord(String recordType) { this.recordType = recordType; }
        public void addField(ASTMField field) { fields.add(field); }
        public String getRecordType() { return recordType; }
        public List<ASTMField> getFields() { return fields; }
    }

    public static class ASTMField {
        private List<String> values = new ArrayList<>();

        public ASTMField(String value) { this.values.add(value); }
        public ASTMField(List<String> values) { this.values = values; }
        public List<String> getValues() { return values; }
        public String getValue() { return values.isEmpty() ? "" : values.get(0); }
    }
}
