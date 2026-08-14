package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.PropertyEditorType;

/**
 * ASTM E1394 deserialization properties (inbound: ASTM E1394 text → XML).
 *
 * <p>Owns its own copy of the delimiter set so that inbound parsing does not
 * depend on the outbound serializer's configuration. This lets a Mirth channel
 * use different delimiters for inbound and outbound — useful when bridging
 * two instruments that use non-standard delimiter conventions.</p>
 *
 * <p>Includes the following production-grade hardening options:</p>
 * <ul>
 *   <li>{@code maxMessageSize} — bytes-level DoS guard.</li>
 *   <li>{@code stripControlChars} — strips non-printable bytes before parsing.</li>
 *   <li>{@code deriveDelimitersFromHeader} — extracts delimiters from the
 *       {@code H} record's field-2 delimiter definition.</li>
 *   <li>{@code validateFrameStructure} — enforces STX/ETX/ETB/CR/LF framing
 *       (typically disabled when used with the dedicated ASTM E1381
 *       transmission-mode plugin, which handles framing separately).</li>
 *   <li>{@code useStrictParser} — enables strict E1394 record validation.</li>
 * </ul>
 */
public class ASTME1394DeserializationProperties extends DeserializationProperties {

    private char    fieldDelimiter            = ASTME1394Constants.DEFAULT_FIELD_DELIMITER_CHAR;
    private char    repeatDelimiter           = ASTME1394Constants.DEFAULT_REPEAT_DELIMITER_CHAR;
    private char    componentDelimiter        = ASTME1394Constants.DEFAULT_COMPONENT_DELIMITER_CHAR;
    private char    escapeCharacter           = ASTME1394Constants.DEFAULT_ESCAPE_CHARACTER_CHAR;
    private char    recordDelimiter           = ASTME1394Constants.DEFAULT_RECORD_DELIMITER_CHAR;
    private String  encoding                  = ASTME1394Constants.DEFAULT_ENCODING;

    private boolean deriveDelimitersFromHeader = false;
    private int     maxMessageSize             = ASTME1394Constants.DEFAULT_MAX_MESSAGE_SIZE;
    private boolean validateFrameStructure     = false;
    private boolean allowIntermediateRecords   = true;
    private boolean stripControlChars          = true;
    private boolean useStrictParser           = false;
    private boolean stripNamespaces           = true;
    private boolean parseFieldRepetitions      = true;
    private boolean parseSubcomponents          = true;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        props.put("fieldDelimiter",            new DataTypePropertyDescriptor(String.valueOf(fieldDelimiter),           "Field Delimiter",            "Inbound field delimiter (default |).",                                          PropertyEditorType.STRING));
        props.put("repeatDelimiter",           new DataTypePropertyDescriptor(String.valueOf(repeatDelimiter),         "Repeat Delimiter",           "Inbound repeat delimiter (default \\).",                                       PropertyEditorType.STRING));
        props.put("componentDelimiter",        new DataTypePropertyDescriptor(String.valueOf(componentDelimiter),      "Component Delimiter",        "Inbound component delimiter (default ^).",                                     PropertyEditorType.STRING));
        props.put("escapeCharacter",           new DataTypePropertyDescriptor(String.valueOf(escapeCharacter),        "Escape Character",           "Inbound escape character (default &).",                                        PropertyEditorType.STRING));
        props.put("recordDelimiter",            new DataTypePropertyDescriptor(String.valueOf(recordDelimiter),        "Record Delimiter",           "Inbound record-end delimiter (default CR).",                                   PropertyEditorType.STRING));
        props.put("encoding",                   new DataTypePropertyDescriptor(encoding,                                "Character Encoding",         "Inbound message encoding (UTF-8 / ISO-8859-1 / US-ASCII).",                   PropertyEditorType.STRING));
        props.put("deriveDelimitersFromHeader", new DataTypePropertyDescriptor(deriveDelimitersFromHeader,             "Derive Delimiters from Header","Extract delimiter set from the H record's field-2 delimiter definition.",       PropertyEditorType.BOOLEAN));
        props.put("maxMessageSize",             new DataTypePropertyDescriptor(String.valueOf(maxMessageSize),         "Max Message Size (bytes)",   "Hard limit for inbound message size to prevent OOM attacks.",                  PropertyEditorType.STRING));
        props.put("validateFrameStructure",    new DataTypePropertyDescriptor(validateFrameStructure,                  "Validate Frame Structure",   "Enforce STX/ETX/ETB/CR/LF frame boundaries (disable when ASTM E1381 transmission mode is used).", PropertyEditorType.BOOLEAN));
        props.put("allowIntermediateRecords",    new DataTypePropertyDescriptor(allowIntermediateRecords,                "Allow Intermediate Records",  "Allow ETB (intermediate) frames before final ETX frame.",                      PropertyEditorType.BOOLEAN));
        props.put("stripControlChars",          new DataTypePropertyDescriptor(stripControlChars,                       "Strip Control Chars",         "Remove non-printable control characters from payload before parsing.",         PropertyEditorType.BOOLEAN));
        props.put("useStrictParser",            new DataTypePropertyDescriptor(useStrictParser,                          "Use Strict Parser",           "Enforce strict E1394 parsing rules.",                                            PropertyEditorType.BOOLEAN));
        props.put("stripNamespaces",            new DataTypePropertyDescriptor(stripNamespaces,                          "Strip Namespaces",            "Strip XML namespace definitions from transformed message.",                   PropertyEditorType.BOOLEAN));
        props.put("parseFieldRepetitions",      new DataTypePropertyDescriptor(parseFieldRepetitions,                   "Parse Field Repetitions",    "Parse repeated field values.",                                                  PropertyEditorType.BOOLEAN));
        props.put("parseSubcomponents",          new DataTypePropertyDescriptor(parseSubcomponents,                      "Parse Subcomponents",         "Parse subcomponent structures.",                                                PropertyEditorType.BOOLEAN));

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
            if ("\\r".equals(s)) this.recordDelimiter = '\r';
            else if ("\\n".equals(s)) this.recordDelimiter = '\n';
            else this.recordDelimiter = s.charAt(0);
        }

        Object enc = properties.get("encoding");
        if (enc != null && !enc.toString().isEmpty()) this.encoding = enc.toString();

        Object dh = properties.get("deriveDelimitersFromHeader");
        if (dh != null) this.deriveDelimitersFromHeader = (Boolean) dh;

        Object ms = properties.get("maxMessageSize");
        if (ms != null) {
            try { this.maxMessageSize = Integer.parseInt(ms.toString()); }
            catch (NumberFormatException e) { this.maxMessageSize = ASTME1394Constants.DEFAULT_MAX_MESSAGE_SIZE; }
        }

        Object vfs = properties.get("validateFrameStructure");
        if (vfs != null) this.validateFrameStructure = (Boolean) vfs;

        Object air = properties.get("allowIntermediateRecords");
        if (air != null) this.allowIntermediateRecords = (Boolean) air;

        Object scc = properties.get("stripControlChars");
        if (scc != null) this.stripControlChars = (Boolean) scc;

        Object usp = properties.get("useStrictParser");
        if (usp != null) this.useStrictParser = (Boolean) usp;

        Object sn = properties.get("stripNamespaces");
        if (sn != null) this.stripNamespaces = (Boolean) sn;

        Object pfr = properties.get("parseFieldRepetitions");
        if (pfr != null) this.parseFieldRepetitions = (Boolean) pfr;

        Object psc = properties.get("parseSubcomponents");
        if (psc != null) this.parseSubcomponents = (Boolean) psc;
    }

    // --- Getters / Setters ---

    public char   getFieldDelimiter()             { return fieldDelimiter; }
    public void    setFieldDelimiter(char v)       { this.fieldDelimiter = v; }
    public char   getRepeatDelimiter()            { return repeatDelimiter; }
    public void    setRepeatDelimiter(char v)      { this.repeatDelimiter = v; }
    public char   getComponentDelimiter()         { return componentDelimiter; }
    public void    setComponentDelimiter(char v)   { this.componentDelimiter = v; }
    public char   getEscapeCharacter()            { return escapeCharacter; }
    public void    setEscapeCharacter(char v)      { this.escapeCharacter = v; }
    public char   getRecordDelimiter()            { return recordDelimiter; }
    public void    setRecordDelimiter(char v)      { this.recordDelimiter = v; }
    public String getEncoding()                   { return encoding; }
    public void    setEncoding(String v)           { this.encoding = v; }
    public boolean isDeriveDelimitersFromHeader() { return deriveDelimitersFromHeader; }
    public void    setDeriveDelimitersFromHeader(boolean v) { this.deriveDelimitersFromHeader = v; }
    public int     getMaxMessageSize()             { return maxMessageSize; }
    public void    setMaxMessageSize(int v)         { this.maxMessageSize = v; }
    public boolean isValidateFrameStructure()     { return validateFrameStructure; }
    public void    setValidateFrameStructure(boolean v) { this.validateFrameStructure = v; }
    public boolean isAllowIntermediateRecords()  { return allowIntermediateRecords; }
    public void    setAllowIntermediateRecords(boolean v) { this.allowIntermediateRecords = v; }
    public boolean isStripControlChars()          { return stripControlChars; }
    public void    setStripControlChars(boolean v) { this.stripControlChars = v; }
    public boolean isUseStrictParser()            { return useStrictParser; }
    public void    setUseStrictParser(boolean v)   { this.useStrictParser = v; }
    public boolean isStripNamespaces()             { return stripNamespaces; }
    public void    setStripNamespaces(boolean v)    { this.stripNamespaces = v; }
    public boolean isParseFieldRepetitions()       { return parseFieldRepetitions; }
    public void    setParseFieldRepetitions(boolean v) { this.parseFieldRepetitions = v; }
    public boolean isParseSubcomponents()          { return parseSubcomponents; }
    public void    setParseSubcomponents(boolean v)  { this.parseSubcomponents = v; }

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
        purged.put("deriveDelimitersFromHeader", deriveDelimitersFromHeader);
        purged.put("maxMessageSize",            maxMessageSize);
        purged.put("validateFrameStructure",     validateFrameStructure);
        purged.put("allowIntermediateRecords",   allowIntermediateRecords);
        purged.put("stripControlChars",          stripControlChars);
        purged.put("useStrictParser",            useStrictParser);
        purged.put("stripNamespaces",            stripNamespaces);
        purged.put("parseFieldRepetitions",      parseFieldRepetitions);
        purged.put("parseSubcomponents",          parseSubcomponents);
        return purged;
    }
}
