package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

/**
 * ASTM E1394 serialization properties (outbound: XML → ASTM E1394 text).
 *
 * <p>Holds the effective delimiter set used when serializing XML back to ASTM
 * E1394 raw text. Delimiter characters are stored as {@code char} primitives
 * for fast comparison inside the serializer hot path; the
 * {@link DataTypePropertyDescriptor} map exposes them as single-character
 * strings for the Mirth Administrator UI.</p>
 */
public class ASTME1394SerializationProperties extends SerializationProperties {

    private char    fieldDelimiter     = ASTME1394Constants.DEFAULT_FIELD_DELIMITER_CHAR;
    private char    repeatDelimiter    = ASTME1394Constants.DEFAULT_REPEAT_DELIMITER_CHAR;
    private char    componentDelimiter = ASTME1394Constants.DEFAULT_COMPONENT_DELIMITER_CHAR;
    private char    escapeCharacter    = ASTME1394Constants.DEFAULT_ESCAPE_CHARACTER_CHAR;
    private char    recordDelimiter    = ASTME1394Constants.DEFAULT_RECORD_DELIMITER_CHAR;
    private String  encoding           = ASTME1394Constants.DEFAULT_ENCODING;
    private boolean strictValidation  = ASTME1394Constants.DEFAULT_STRICT_VALIDATION;
    private boolean deriveFromHeader  = false;
    private boolean stripASTM1381Chars = true;
    private boolean convertLineBreaks  = true;
    private boolean useFieldRepetitions = true;
    private boolean useSubcomponents   = true;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        props.put("fieldDelimiter",     new DataTypePropertyDescriptor(String.valueOf(fieldDelimiter),     "Field Delimiter",      "Delimiter separating fields (default pipe |).",                       PropertyEditorType.STRING));
        props.put("repeatDelimiter",    new DataTypePropertyDescriptor(String.valueOf(repeatDelimiter),    "Repeat Delimiter",     "Delimiter for repeated fields (default backslash \\).",                PropertyEditorType.STRING));
        props.put("componentDelimiter", new DataTypePropertyDescriptor(String.valueOf(componentDelimiter), "Component Delimiter",  "Delimiter separating components (default caret ^).",                  PropertyEditorType.STRING));
        props.put("escapeCharacter",    new DataTypePropertyDescriptor(String.valueOf(escapeCharacter),    "Escape Character",     "Character used to escape sequences (default ampersand &).",          PropertyEditorType.STRING));
        props.put("recordDelimiter",    new DataTypePropertyDescriptor(String.valueOf(recordDelimiter),    "Record Delimiter",     "End-of-record delimiter (default CR).",                              PropertyEditorType.STRING));
        props.put("encoding",           new DataTypePropertyDescriptor(encoding,                            "Character Encoding",   "Message encoding (UTF-8, ISO-8859-1, US-ASCII).",                    PropertyEditorType.STRING));
        props.put("strictValidation",   new DataTypePropertyDescriptor(strictValidation,                    "Strict ASTM Validation","Enforce strict E1394 field-level validation rules.",                 PropertyEditorType.BOOLEAN));
        props.put("deriveFromHeader",   new DataTypePropertyDescriptor(deriveFromHeader,                   "Derive from Header",   "Derive delimiter configuration from ASTM header record (H|...).",     PropertyEditorType.BOOLEAN));
        props.put("stripASTM1381Chars",  new DataTypePropertyDescriptor(stripASTM1381Chars,                  "Strip ASTM E1381 Chars","Remove STX/ETX/ETB framing characters before parsing.",            PropertyEditorType.BOOLEAN));
        props.put("convertLineBreaks",   new DataTypePropertyDescriptor(convertLineBreaks,                  "Convert Line Breaks",  "Normalize CRLF / CR / LF line endings to the record delimiter.",      PropertyEditorType.BOOLEAN));
        props.put("useFieldRepetitions", new DataTypePropertyDescriptor(useFieldRepetitions,                "Use Field Repetitions","Support repeated field values using the repeat delimiter.",         PropertyEditorType.BOOLEAN));
        props.put("useSubcomponents",    new DataTypePropertyDescriptor(useSubcomponents,                   "Use Subcomponents",    "Support subcomponent parsing using the component delimiter.",        PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        Object fd = properties.get("fieldDelimiter");
        if (fd != null && !fd.toString().isEmpty()) this.fieldDelimiter = fd.toString().charAt(0);

        Object rd = properties.get("repeatDelimiter");
        if (rd != null && !rd.toString().isEmpty()) this.repeatDelimiter = rd.toString().charAt(0);

        Object cd = properties.get("componentDelimiter");
        if (cd != null && !cd.toString().isEmpty()) this.componentDelimiter = cd.toString().charAt(0);

        Object ec = properties.get("escapeCharacter");
        if (ec != null && !ec.toString().isEmpty()) this.escapeCharacter = ec.toString().charAt(0);

        Object rcd = properties.get("recordDelimiter");
        if (rcd != null && !rcd.toString().isEmpty()) {
            String s = rcd.toString();
            // Allow "\r", "\n" escape sequences in the property string.
            if ("\\r".equals(s)) this.recordDelimiter = '\r';
            else if ("\\n".equals(s)) this.recordDelimiter = '\n';
            else this.recordDelimiter = s.charAt(0);
        }

        Object enc = properties.get("encoding");
        if (enc != null && !enc.toString().isEmpty()) this.encoding = enc.toString();

        Object sv = properties.get("strictValidation");
        if (sv != null) this.strictValidation = (Boolean) sv;

        Object dh = properties.get("deriveFromHeader");
        if (dh != null) this.deriveFromHeader = (Boolean) dh;

        Object sa = properties.get("stripASTM1381Chars");
        if (sa != null) this.stripASTM1381Chars = (Boolean) sa;

        Object cl = properties.get("convertLineBreaks");
        if (cl != null) this.convertLineBreaks = (Boolean) cl;

        Object ufr = properties.get("useFieldRepetitions");
        if (ufr != null) this.useFieldRepetitions = (Boolean) ufr;

        Object usc = properties.get("useSubcomponents");
        if (usc != null) this.useSubcomponents = (Boolean) usc;
    }

    // --- Getters / Setters (char-based for the serializer hot path) ---

    public char   getFieldDelimiter()        { return fieldDelimiter; }
    public void    setFieldDelimiter(char v)  { this.fieldDelimiter = v; }
    public char   getRepeatDelimiter()       { return repeatDelimiter; }
    public void    setRepeatDelimiter(char v) { this.repeatDelimiter = v; }
    public char   getComponentDelimiter()     { return componentDelimiter; }
    public void    setComponentDelimiter(char v) { this.componentDelimiter = v; }
    public char   getEscapeCharacter()       { return escapeCharacter; }
    public void    setEscapeCharacter(char v) { this.escapeCharacter = v; }
    public char   getRecordDelimiter()       { return recordDelimiter; }
    public void    setRecordDelimiter(char v) { this.recordDelimiter = v; }
    public String getEncoding()              { return encoding; }
    public void    setEncoding(String v)      { this.encoding = v; }
    public boolean isStrictValidation()      { return strictValidation; }
    public void    setStrictValidation(boolean v) { this.strictValidation = v; }
    public boolean isDeriveFromHeader()       { return deriveFromHeader; }
    public void    setDeriveFromHeader(boolean v) { this.deriveFromHeader = v; }
    public boolean isStripASTM1381Chars()     { return stripASTM1381Chars; }
    public void    setStripASTM1381Chars(boolean v) { this.stripASTM1381Chars = v; }
    public boolean isConvertLineBreaks()      { return convertLineBreaks; }
    public void    setConvertLineBreaks(boolean v) { this.convertLineBreaks = v; }
    public boolean isUseFieldRepetitions()    { return useFieldRepetitions; }
    public void    setUseFieldRepetitions(boolean v) { this.useFieldRepetitions = v; }
    public boolean isUseSubcomponents()       { return useSubcomponents; }
    public void    setUseSubcomponents(boolean v) { this.useSubcomponents = v; }

    @Override public void migrate3_0_1(DonkeyElement e) {}
    @Override public void migrate3_0_2(DonkeyElement e) {}
    @Override public void migrate3_1_0(DonkeyElement e) {}
    @Override public void migrate3_2_0(DonkeyElement e) {}
    @Override public void migrate3_3_0(DonkeyElement e) {}
    @Override public void migrate3_4_0(DonkeyElement e) {}
    @Override public void migrate3_5_0(DonkeyElement e) {}

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purged = new HashMap<String, Object>();
        purged.put("strictValidation",     strictValidation);
        purged.put("deriveFromHeader",     deriveFromHeader);
        purged.put("encoding",              encoding);
        purged.put("stripASTM1381Chars",    stripASTM1381Chars);
        purged.put("convertLineBreaks",     convertLineBreaks);
        purged.put("useFieldRepetitions",   useFieldRepetitions);
        purged.put("useSubcomponents",      useSubcomponents);
        return purged;
    }
}
