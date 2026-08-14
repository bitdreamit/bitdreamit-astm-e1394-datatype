package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;

/**
 * ASTM E1394 response-generation properties.
 *
 * <p>Controls how the {@link ASTME1394AutoResponder} constructs outbound ACK
 * frames in response to inbound messages. Supports the standard ASTM E1381
 * framing (STX … ETX LRC CR LF) plus the configurable response codes, optional
 * sequence numbering, and timestamp inclusion expected by laboratory
 * instruments.</p>
 */
public class ASTME1394ResponseGenerationProperties extends ResponseGenerationProperties {

    private boolean wrapInASTMFrame      = true;
    private boolean includeSequenceNumber = true;
    private String  successResponseCode  = ASTME1394Constants.RESPONSE_ACCEPT;
    private String  errorResponseCode    = ASTME1394Constants.RESPONSE_ERROR;
    private String  rejectResponseCode   = ASTME1394Constants.RESPONSE_REJECT;
    private boolean includeTimestamp     = true;
    private boolean includeOriginalFrame = false;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        props.put("wrapInASTMFrame",       new DataTypePropertyDescriptor(wrapInASTMFrame,        "Wrap in ASTM Frame",      "Enclose response in STX … ETX LRC CR LF framing.",                PropertyEditorType.BOOLEAN));
        props.put("includeSequenceNumber", new DataTypePropertyDescriptor(includeSequenceNumber, "Include Sequence Number", "Include frame sequence number (0–7) in response.",                PropertyEditorType.BOOLEAN));
        props.put("successResponseCode",   new DataTypePropertyDescriptor(successResponseCode,   "Success Code",            "Response code for accepted message (default AA).",                PropertyEditorType.STRING));
        props.put("errorResponseCode",     new DataTypePropertyDescriptor(errorResponseCode,     "Error Code",              "Response code for application error (default AE).",                PropertyEditorType.STRING));
        props.put("rejectResponseCode",    new DataTypePropertyDescriptor(rejectResponseCode,    "Reject Code",              "Response code for rejected message (default AR).",                PropertyEditorType.STRING));
        props.put("includeTimestamp",       new DataTypePropertyDescriptor(includeTimestamp,      "Include Timestamp",        "Append timestamp to response message.",                            PropertyEditorType.BOOLEAN));
        props.put("includeOriginalFrame",   new DataTypePropertyDescriptor(includeOriginalFrame,  "Include Original Frame",   "Echo original frame data in response for correlation.",            PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        Object w = properties.get("wrapInASTMFrame");
        if (w != null) this.wrapInASTMFrame = (Boolean) w;

        Object sn = properties.get("includeSequenceNumber");
        if (sn != null) this.includeSequenceNumber = (Boolean) sn;

        Object sc = properties.get("successResponseCode");
        if (sc != null && !sc.toString().isEmpty()) this.successResponseCode = sc.toString();

        Object ec = properties.get("errorResponseCode");
        if (ec != null && !ec.toString().isEmpty()) this.errorResponseCode = ec.toString();

        Object rc = properties.get("rejectResponseCode");
        if (rc != null && !rc.toString().isEmpty()) this.rejectResponseCode = rc.toString();

        Object ts = properties.get("includeTimestamp");
        if (ts != null) this.includeTimestamp = (Boolean) ts;

        Object iof = properties.get("includeOriginalFrame");
        if (iof != null) this.includeOriginalFrame = (Boolean) iof;
    }

    public boolean isWrapInASTMFrame()         { return wrapInASTMFrame; }
    public void    setWrapInASTMFrame(boolean v) { this.wrapInASTMFrame = v; }
    public boolean isIncludeSequenceNumber()  { return includeSequenceNumber; }
    public void    setIncludeSequenceNumber(boolean v) { this.includeSequenceNumber = v; }
    public String  getSuccessResponseCode()   { return successResponseCode; }
    public void    setSuccessResponseCode(String v) { this.successResponseCode = v; }
    public String  getErrorResponseCode()     { return errorResponseCode; }
    public void    setErrorResponseCode(String v) { this.errorResponseCode = v; }
    public String  getRejectResponseCode()    { return rejectResponseCode; }
    public void    setRejectResponseCode(String v) { this.rejectResponseCode = v; }
    public boolean isIncludeTimestamp()        { return includeTimestamp; }
    public void    setIncludeTimestamp(boolean v) { this.includeTimestamp = v; }
    public boolean isIncludeOriginalFrame()    { return includeOriginalFrame; }
    public void    setIncludeOriginalFrame(boolean v) { this.includeOriginalFrame = v; }

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
        purged.put("wrapInASTMFrame",      wrapInASTMFrame);
        purged.put("includeSequenceNumber", includeSequenceNumber);
        purged.put("includeTimestamp",      includeTimestamp);
        purged.put("includeOriginalFrame",  includeOriginalFrame);
        return purged;
    }
}
