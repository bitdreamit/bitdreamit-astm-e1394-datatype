package com.bitdreamit.mirth.astm.e1394.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bitdreamit.mirth.astm.e1394.shared.ASTME1394Constants;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;

/**
 * ASTM E1394 Response Generation Properties
 * Premium: Configurable response codes, ASTM framing, sequence numbers
 */
public class ASTME1394ResponseGenerationProperties extends ResponseGenerationProperties {

    private boolean wrapInASTMFrame     = true;
    private boolean includeSequenceNumber = true;
    private String successResponseCode  = ASTME1394Constants.RESPONSE_ACCEPT;
    private String errorResponseCode    = ASTME1394Constants.RESPONSE_ERROR;
    private String rejectResponseCode   = ASTME1394Constants.RESPONSE_REJECT;
    private boolean includeTimestamp    = true; // Premium
    private boolean includeOriginalFrame = false; // Premium: echo original in response

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<>();

        props.put("wrapInASTMFrame",      new DataTypePropertyDescriptor(wrapInASTMFrame, "Wrap in ASTM Frame", "Enclose response in STX ... ETX LRC CR LF framing.", PropertyEditorType.BOOLEAN));
        props.put("includeSequenceNumber", new DataTypePropertyDescriptor(includeSequenceNumber, "Include Sequence Number", "Include frame sequence number (0-7) in response.", PropertyEditorType.BOOLEAN));
        props.put("successResponseCode",  new DataTypePropertyDescriptor(successResponseCode, "Success Code", "Response code for accepted message (default AA).", PropertyEditorType.STRING));
        props.put("errorResponseCode",    new DataTypePropertyDescriptor(errorResponseCode, "Error Code", "Response code for application error (default AE).", PropertyEditorType.STRING));
        props.put("rejectResponseCode",   new DataTypePropertyDescriptor(rejectResponseCode, "Reject Code", "Response code for rejected message (default AR).", PropertyEditorType.STRING));
        props.put("includeTimestamp",     new DataTypePropertyDescriptor(includeTimestamp, "Include Timestamp", "Append timestamp to response message.", PropertyEditorType.BOOLEAN));
        props.put("includeOriginalFrame", new DataTypePropertyDescriptor(includeOriginalFrame, "Include Original Frame", "Echo original frame data in response for correlation.", PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        if (properties.get("wrapInASTMFrame") != null)      this.wrapInASTMFrame      = (Boolean) properties.get("wrapInASTMFrame");
        if (properties.get("includeSequenceNumber") != null) this.includeSequenceNumber = (Boolean) properties.get("includeSequenceNumber");
        if (properties.get("successResponseCode") != null)   this.successResponseCode  = (String) properties.get("successResponseCode");
        if (properties.get("errorResponseCode") != null)     this.errorResponseCode    = (String) properties.get("errorResponseCode");
        if (properties.get("rejectResponseCode") != null)    this.rejectResponseCode   = (String) properties.get("rejectResponseCode");
        if (properties.get("includeTimestamp") != null)      this.includeTimestamp     = (Boolean) properties.get("includeTimestamp");
        if (properties.get("includeOriginalFrame") != null)  this.includeOriginalFrame = (Boolean) properties.get("includeOriginalFrame");
    }

    public boolean isWrapInASTMFrame() { return wrapInASTMFrame; }
    public void setWrapInASTMFrame(boolean wrapInASTMFrame) { this.wrapInASTMFrame = wrapInASTMFrame; }
    public boolean isIncludeSequenceNumber() { return includeSequenceNumber; }
    public void setIncludeSequenceNumber(boolean includeSequenceNumber) { this.includeSequenceNumber = includeSequenceNumber; }
    public String getSuccessResponseCode() { return successResponseCode; }
    public void setSuccessResponseCode(String successResponseCode) { this.successResponseCode = successResponseCode; }
    public String getErrorResponseCode() { return errorResponseCode; }
    public void setErrorResponseCode(String errorResponseCode) { this.errorResponseCode = errorResponseCode; }
    public String getRejectResponseCode() { return rejectResponseCode; }
    public void setRejectResponseCode(String rejectResponseCode) { this.rejectResponseCode = rejectResponseCode; }
    public boolean isIncludeTimestamp() { return includeTimestamp; }
    public void setIncludeTimestamp(boolean includeTimestamp) { this.includeTimestamp = includeTimestamp; }
    public boolean isIncludeOriginalFrame() { return includeOriginalFrame; }
    public void setIncludeOriginalFrame(boolean includeOriginalFrame) { this.includeOriginalFrame = includeOriginalFrame; }

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
        purged.put("wrapInASTMFrame", wrapInASTMFrame);
        purged.put("includeSequenceNumber", includeSequenceNumber);
        purged.put("includeTimestamp", includeTimestamp);
        purged.put("includeOriginalFrame", includeOriginalFrame);
        return purged;
    }
}
