package com.bitdreamit.mirth.astm.e1394.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bitdreamit.mirth.astm.e1394.shared.ASTME1394Constants;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.PropertyEditorType;

/**
 * ASTM E1394 Deserialization Properties (Inbound: ASTM → XML)
 * Premium features: DoS protection, frame validation, control char stripping
 */
public class ASTME1394DeserializationProperties extends DeserializationProperties {

    private boolean validateChecksum        = true;
    private boolean validateFrameStructure  = true;
    private boolean allowIntermediateRecords= true;
    private int maxMessageSize              = ASTME1394Constants.DEFAULT_MAX_MESSAGE_SIZE;
    private boolean stripControlChars         = true;
    private boolean parseFieldRepetitions   = true;
    private boolean parseSubcomponents        = true;
    private boolean useStrictParser         = false;
    private boolean stripNamespaces           = true;
    private String segmentDelimiter         = ASTME1394Constants.DEFAULT_RECORD_DELIMITER;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<>();

        props.put("validateChecksum",         new DataTypePropertyDescriptor(validateChecksum, "Validate Checksum (LRC)", "Validate ASTM E1381 frame checksum on inbound messages.", PropertyEditorType.BOOLEAN));
        props.put("validateFrameStructure",   new DataTypePropertyDescriptor(validateFrameStructure, "Validate Frame Structure", "Enforce STX/ETX/ETB/CR/LF frame boundaries.", PropertyEditorType.BOOLEAN));
        props.put("allowIntermediateRecords", new DataTypePropertyDescriptor(allowIntermediateRecords, "Allow Intermediate Records", "Allow ETB (intermediate) frames before final ETX frame.", PropertyEditorType.BOOLEAN));
        props.put("maxMessageSize",           new DataTypePropertyDescriptor(maxMessageSize, "Max Message Size (bytes)", "Hard limit for inbound message size to prevent OOM attacks.", PropertyEditorType.STRING));
        props.put("stripControlChars",        new DataTypePropertyDescriptor(stripControlChars, "Strip Control Chars", "Remove non-printable control characters from payload before parsing.", PropertyEditorType.BOOLEAN));
        props.put("parseFieldRepetitions",    new DataTypePropertyDescriptor(parseFieldRepetitions, "Parse Field Repetitions", "Parse repeated field values.", PropertyEditorType.BOOLEAN));
        props.put("parseSubcomponents",       new DataTypePropertyDescriptor(parseSubcomponents, "Parse Subcomponents", "Parse subcomponent structures.", PropertyEditorType.BOOLEAN));
        props.put("useStrictParser",          new DataTypePropertyDescriptor(useStrictParser, "Use Strict Parser", "Enforce strict E1394 parsing rules.", PropertyEditorType.BOOLEAN));
        props.put("stripNamespaces",          new DataTypePropertyDescriptor(stripNamespaces, "Strip Namespaces", "Strip XML namespace definitions from transformed message.", PropertyEditorType.BOOLEAN));
        props.put("segmentDelimiter",         new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "Input delimiter expected after each record.", PropertyEditorType.STRING));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        if (properties.get("validateChecksum") != null)         this.validateChecksum         = (Boolean) properties.get("validateChecksum");
        if (properties.get("validateFrameStructure") != null)     this.validateFrameStructure   = (Boolean) properties.get("validateFrameStructure");
        if (properties.get("allowIntermediateRecords") != null)   this.allowIntermediateRecords = (Boolean) properties.get("allowIntermediateRecords");
        if (properties.get("maxMessageSize") != null) {
            try { this.maxMessageSize = Integer.parseInt(properties.get("maxMessageSize").toString()); }
            catch (Exception e) { this.maxMessageSize = ASTME1394Constants.DEFAULT_MAX_MESSAGE_SIZE; }
        }
        if (properties.get("stripControlChars") != null)          this.stripControlChars        = (Boolean) properties.get("stripControlChars");
        if (properties.get("parseFieldRepetitions") != null)      this.parseFieldRepetitions    = (Boolean) properties.get("parseFieldRepetitions");
        if (properties.get("parseSubcomponents") != null)         this.parseSubcomponents       = (Boolean) properties.get("parseSubcomponents");
        if (properties.get("useStrictParser") != null)            this.useStrictParser          = (Boolean) properties.get("useStrictParser");
        if (properties.get("stripNamespaces") != null)            this.stripNamespaces           = (Boolean) properties.get("stripNamespaces");
        if (properties.get("segmentDelimiter") != null)           this.segmentDelimiter          = (String) properties.get("segmentDelimiter");
    }

    public boolean isValidateChecksum() { return validateChecksum; }
    public void setValidateChecksum(boolean validateChecksum) { this.validateChecksum = validateChecksum; }
    public boolean isValidateFrameStructure() { return validateFrameStructure; }
    public void setValidateFrameStructure(boolean validateFrameStructure) { this.validateFrameStructure = validateFrameStructure; }
    public boolean isAllowIntermediateRecords() { return allowIntermediateRecords; }
    public void setAllowIntermediateRecords(boolean allowIntermediateRecords) { this.allowIntermediateRecords = allowIntermediateRecords; }
    public int getMaxMessageSize() { return maxMessageSize; }
    public void setMaxMessageSize(int maxMessageSize) { this.maxMessageSize = maxMessageSize; }
    public boolean isStripControlChars() { return stripControlChars; }
    public void setStripControlChars(boolean stripControlChars) { this.stripControlChars = stripControlChars; }
    public boolean isParseFieldRepetitions() { return parseFieldRepetitions; }
    public void setParseFieldRepetitions(boolean parseFieldRepetitions) { this.parseFieldRepetitions = parseFieldRepetitions; }
    public boolean isParseSubcomponents() { return parseSubcomponents; }
    public void setParseSubcomponents(boolean parseSubcomponents) { this.parseSubcomponents = parseSubcomponents; }
    public boolean isUseStrictParser() { return useStrictParser; }
    public void setUseStrictParser(boolean useStrictParser) { this.useStrictParser = useStrictParser; }
    public boolean isStripNamespaces() { return stripNamespaces; }
    public void setStripNamespaces(boolean stripNamespaces) { this.stripNamespaces = stripNamespaces; }
    public String getSegmentDelimiter() { return segmentDelimiter; }
    public void setSegmentDelimiter(String segmentDelimiter) { this.segmentDelimiter = segmentDelimiter; }

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
        purged.put("validateChecksum", validateChecksum);
        purged.put("validateFrameStructure", validateFrameStructure);
        purged.put("allowIntermediateRecords", allowIntermediateRecords);
        purged.put("maxMessageSize", maxMessageSize);
        purged.put("stripControlChars", stripControlChars);
        purged.put("parseFieldRepetitions", parseFieldRepetitions);
        purged.put("parseSubcomponents", parseSubcomponents);
        purged.put("useStrictParser", useStrictParser);
        purged.put("stripNamespaces", stripNamespaces);
        return purged;
    }
}
