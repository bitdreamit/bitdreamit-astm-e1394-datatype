package com.bitdreamit.mirth.astm.e1394.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bitdreamit.mirth.astm.e1394.shared.ASTME1394Constants;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

/**
 * ASTM E1394 Serialization Properties (Outbound: XML → ASTM)
 */
public class ASTME1394SerializationProperties extends SerializationProperties {

    private String fieldDelimiter     = ASTME1394Constants.DEFAULT_FIELD_DELIMITER;
    private String repeatDelimiter    = ASTME1394Constants.DEFAULT_REPEAT_DELIMITER;
    private String componentDelimiter = ASTME1394Constants.DEFAULT_COMPONENT_DELIMITER;
    private String escapeCharacter    = ASTME1394Constants.DEFAULT_ESCAPE_CHARACTER;
    private String recordDelimiter    = ASTME1394Constants.DEFAULT_RECORD_DELIMITER;
    private String encoding           = ASTME1394Constants.DEFAULT_ENCODING;
    private boolean strictValidation  = ASTME1394Constants.DEFAULT_STRICT_VALIDATION;
    private boolean deriveFromHeader  = false;
    private boolean stripASTM1381Chars = true; // Premium: strip framing chars
    private boolean convertLineBreaks  = true;  // Premium: normalize line endings
    private boolean useFieldRepetitions = true;
    private boolean useSubcomponents   = true;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<>();

        props.put("fieldDelimiter",      new DataTypePropertyDescriptor(fieldDelimiter, "Field Delimiter", "Delimiter separating fields (default pipe |).", PropertyEditorType.STRING));
        props.put("repeatDelimiter",     new DataTypePropertyDescriptor(repeatDelimiter, "Repeat Delimiter", "Delimiter for repeated fields (default backslash).", PropertyEditorType.STRING));
        props.put("componentDelimiter",  new DataTypePropertyDescriptor(componentDelimiter, "Component Delimiter", "Delimiter separating components (default caret ^).", PropertyEditorType.STRING));
        props.put("escapeCharacter",     new DataTypePropertyDescriptor(escapeCharacter, "Escape Character", "Character used to escape sequences (default ampersand &).", PropertyEditorType.STRING));
        props.put("recordDelimiter",     new DataTypePropertyDescriptor(recordDelimiter, "Record Delimiter", "End-of-record delimiter (default CR).", PropertyEditorType.STRING));
        props.put("encoding",            new DataTypePropertyDescriptor(encoding, "Character Encoding", "Message encoding (UTF-8, ISO-8859-1, US-ASCII).", PropertyEditorType.STRING));
        props.put("strictValidation",    new DataTypePropertyDescriptor(strictValidation, "Strict ASTM Validation", "Enforce strict E1394 field-level validation rules.", PropertyEditorType.BOOLEAN));
        props.put("deriveFromHeader",    new DataTypePropertyDescriptor(deriveFromHeader, "Derive from Header", "Derive delimiter configuration from ASTM header record (H|...).", PropertyEditorType.BOOLEAN));
        props.put("stripASTM1381Chars",  new DataTypePropertyDescriptor(stripASTM1381Chars, "Strip ASTM E1381 Characters", "Remove STX/ETX/ETB framing characters before parsing.", PropertyEditorType.BOOLEAN));
        props.put("convertLineBreaks",   new DataTypePropertyDescriptor(convertLineBreaks, "Convert Line Breaks", "Normalize all line break styles (CRLF, CR, LF) to record delimiter.", PropertyEditorType.BOOLEAN));
        props.put("useFieldRepetitions", new DataTypePropertyDescriptor(useFieldRepetitions, "Use Field Repetitions", "Support repeated field values using repeat delimiter.", PropertyEditorType.BOOLEAN));
        props.put("useSubcomponents",    new DataTypePropertyDescriptor(useSubcomponents, "Use Subcomponents", "Support subcomponent parsing using component delimiter.", PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        if (properties.get("fieldDelimiter") != null)      this.fieldDelimiter     = (String) properties.get("fieldDelimiter");
        if (properties.get("repeatDelimiter") != null)     this.repeatDelimiter    = (String) properties.get("repeatDelimiter");
        if (properties.get("componentDelimiter") != null)  this.componentDelimiter = (String) properties.get("componentDelimiter");
        if (properties.get("escapeCharacter") != null)     this.escapeCharacter    = (String) properties.get("escapeCharacter");
        if (properties.get("recordDelimiter") != null)   this.recordDelimiter    = (String) properties.get("recordDelimiter");
        if (properties.get("encoding") != null)            this.encoding           = (String) properties.get("encoding");
        if (properties.get("strictValidation") != null)  this.strictValidation   = (Boolean) properties.get("strictValidation");
        if (properties.get("deriveFromHeader") != null)  this.deriveFromHeader   = (Boolean) properties.get("deriveFromHeader");
        if (properties.get("stripASTM1381Chars") != null) this.stripASTM1381Chars = (Boolean) properties.get("stripASTM1381Chars");
        if (properties.get("convertLineBreaks") != null)  this.convertLineBreaks  = (Boolean) properties.get("convertLineBreaks");
        if (properties.get("useFieldRepetitions") != null) this.useFieldRepetitions = (Boolean) properties.get("useFieldRepetitions");
        if (properties.get("useSubcomponents") != null)   this.useSubcomponents   = (Boolean) properties.get("useSubcomponents");
    }

    // Getters & Setters
    public String getFieldDelimiter() { return fieldDelimiter; }
    public void setFieldDelimiter(String fieldDelimiter) { this.fieldDelimiter = fieldDelimiter; }
    public String getRepeatDelimiter() { return repeatDelimiter; }
    public void setRepeatDelimiter(String repeatDelimiter) { this.repeatDelimiter = repeatDelimiter; }
    public String getComponentDelimiter() { return componentDelimiter; }
    public void setComponentDelimiter(String componentDelimiter) { this.componentDelimiter = componentDelimiter; }
    public String getEscapeCharacter() { return escapeCharacter; }
    public void setEscapeCharacter(String escapeCharacter) { this.escapeCharacter = escapeCharacter; }
    public String getRecordDelimiter() { return recordDelimiter; }
    public void setRecordDelimiter(String recordDelimiter) { this.recordDelimiter = recordDelimiter; }
    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
    public boolean isStrictValidation() { return strictValidation; }
    public void setStrictValidation(boolean strictValidation) { this.strictValidation = strictValidation; }
    public boolean isDeriveFromHeader() { return deriveFromHeader; }
    public void setDeriveFromHeader(boolean deriveFromHeader) { this.deriveFromHeader = deriveFromHeader; }
    public boolean isStripASTM1381Chars() { return stripASTM1381Chars; }
    public void setStripASTM1381Chars(boolean stripASTM1381Chars) { this.stripASTM1381Chars = stripASTM1381Chars; }
    public boolean isConvertLineBreaks() { return convertLineBreaks; }
    public void setConvertLineBreaks(boolean convertLineBreaks) { this.convertLineBreaks = convertLineBreaks; }
    public boolean isUseFieldRepetitions() { return useFieldRepetitions; }
    public void setUseFieldRepetitions(boolean useFieldRepetitions) { this.useFieldRepetitions = useFieldRepetitions; }
    public boolean isUseSubcomponents() { return useSubcomponents; }
    public void setUseSubcomponents(boolean useSubcomponents) { this.useSubcomponents = useSubcomponents; }

    @Override public void migrate3_0_1(DonkeyElement element) {}
    @Override public void migrate3_0_2(DonkeyElement element) {}
    @Override public void migrate3_1_0(DonkeyElement element) {}
    @Override public void migrate3_2_0(DonkeyElement element) {}
    @Override public void migrate3_3_0(DonkeyElement element) {}
    @Override public void migrate3_4_0(DonkeyElement element) {}
    @Override public void migrate3_5_0(DonkeyElement element) {}

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purged = new HashMap<>();
        purged.put("strictValidation", strictValidation);
        purged.put("deriveFromHeader", deriveFromHeader);
        purged.put("encoding", encoding);
        purged.put("stripASTM1381Chars", stripASTM1381Chars);
        purged.put("convertLineBreaks", convertLineBreaks);
        purged.put("useFieldRepetitions", useFieldRepetitions);
        purged.put("useSubcomponents", useSubcomponents);
        return purged;
    }
}
