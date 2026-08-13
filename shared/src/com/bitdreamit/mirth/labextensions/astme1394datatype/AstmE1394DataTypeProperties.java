/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import com.mirth.connect.plugins.datatypes.DataTypeProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ASTM E1394 data type properties with all commercial features + extras.
 */
@XStreamAlias("astmE1394DataTypeProperties")
public class AstmE1394DataTypeProperties extends DataTypeProperties {
    private static final long serialVersionUID = 1L;

    // Validation
    private boolean useStrictValidation = true;
    private boolean fieldLevelValidation = true;
    private boolean validateRecordTypes = true;
    private boolean validateFieldCounts = true;

    // Response generation (extra feature)
    private boolean generateResponses = true;
    private String responseStatus = "AA"; // AA, AE, AR

    // Batch processing
    private boolean enableBatchScripting = false;
    private String batchScript = "";
    private String batchType = "Record_Based";

    // Template engine (extra feature)
    private boolean enableTemplateEngine = false;
    private String serializationTemplate = "";

    // Tracking & deduplication (extra feature)
    private boolean trackControlIds = true;
    private int maxControlIdHistory = 10000;

    // Delimiters
    private String segmentDelimiter = "\r";
    private String fieldDelimiter = "\|";
    private String componentDelimiter = "\^";
    private String repeatDelimiter = "\\";
    private String escapeDelimiter = "\&";

    // Advanced parsing
    private boolean handleRepeatFields = true;
    private boolean handleSubcomponents = true;
    private boolean handleManufacturerExtensions = false;
    private boolean stripEmptyFields = false;

    // Serialization
    private boolean prettyPrintXml = false;
    private boolean includeEmptyFields = true;

    public AstmE1394DataTypeProperties() {
        super("ASTM E1394");
    }

    // Getters & Setters
    public boolean isUseStrictValidation() { return useStrictValidation; }
    public void setUseStrictValidation(boolean v) { this.useStrictValidation = v; }
    public boolean isFieldLevelValidation() { return fieldLevelValidation; }
    public void setFieldLevelValidation(boolean v) { this.fieldLevelValidation = v; }
    public boolean isValidateRecordTypes() { return validateRecordTypes; }
    public void setValidateRecordTypes(boolean v) { this.validateRecordTypes = v; }
    public boolean isValidateFieldCounts() { return validateFieldCounts; }
    public void setValidateFieldCounts(boolean v) { this.validateFieldCounts = v; }
    public boolean isGenerateResponses() { return generateResponses; }
    public void setGenerateResponses(boolean v) { this.generateResponses = v; }
    public String getResponseStatus() { return responseStatus; }
    public void setResponseStatus(String v) { this.responseStatus = v; }
    public boolean isEnableBatchScripting() { return enableBatchScripting; }
    public void setEnableBatchScripting(boolean v) { this.enableBatchScripting = v; }
    public String getBatchScript() { return batchScript; }
    public void setBatchScript(String v) { this.batchScript = v; }
    public String getBatchType() { return batchType; }
    public void setBatchType(String v) { this.batchType = v; }
    public boolean isEnableTemplateEngine() { return enableTemplateEngine; }
    public void setEnableTemplateEngine(boolean v) { this.enableTemplateEngine = v; }
    public String getSerializationTemplate() { return serializationTemplate; }
    public void setSerializationTemplate(String v) { this.serializationTemplate = v; }
    public boolean isTrackControlIds() { return trackControlIds; }
    public void setTrackControlIds(boolean v) { this.trackControlIds = v; }
    public int getMaxControlIdHistory() { return maxControlIdHistory; }
    public void setMaxControlIdHistory(int v) { this.maxControlIdHistory = v; }
    public String getSegmentDelimiter() { return segmentDelimiter; }
    public void setSegmentDelimiter(String v) { this.segmentDelimiter = v; }
    public String getFieldDelimiter() { return fieldDelimiter; }
    public void setFieldDelimiter(String v) { this.fieldDelimiter = v; }
    public String getComponentDelimiter() { return componentDelimiter; }
    public void setComponentDelimiter(String v) { this.componentDelimiter = v; }
    public String getRepeatDelimiter() { return repeatDelimiter; }
    public void setRepeatDelimiter(String v) { this.repeatDelimiter = v; }
    public String getEscapeDelimiter() { return escapeDelimiter; }
    public void setEscapeDelimiter(String v) { this.escapeDelimiter = v; }
    public boolean isHandleRepeatFields() { return handleRepeatFields; }
    public void setHandleRepeatFields(boolean v) { this.handleRepeatFields = v; }
    public boolean isHandleSubcomponents() { return handleSubcomponents; }
    public void setHandleSubcomponents(boolean v) { this.handleSubcomponents = v; }
    public boolean isHandleManufacturerExtensions() { return handleManufacturerExtensions; }
    public void setHandleManufacturerExtensions(boolean v) { this.handleManufacturerExtensions = v; }
    public boolean isStripEmptyFields() { return stripEmptyFields; }
    public void setStripEmptyFields(boolean v) { this.stripEmptyFields = v; }
    public boolean isPrettyPrintXml() { return prettyPrintXml; }
    public void setPrettyPrintXml(boolean v) { this.prettyPrintXml = v; }
    public boolean isIncludeEmptyFields() { return includeEmptyFields; }
    public void setIncludeEmptyFields(boolean v) { this.includeEmptyFields = v; }

    @Override public String getPluginPointName() { return "ASTM E1394"; }
    @Override public DataTypeProperties create() { return new AstmE1394DataTypeProperties(); }
}