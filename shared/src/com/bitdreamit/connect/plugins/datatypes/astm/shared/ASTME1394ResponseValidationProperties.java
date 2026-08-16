package com.bitdreamit.connect.plugins.datatypes.astm.shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

/**
 * ASTM E1394 response-validation properties.
 *
 * <p>Controls how the {@link ASTME1394ResponseValidator} inspects inbound
 * ACK / NAK responses received from remote instruments. Supports LRC checksum
 * validation, frame-structure enforcement, timeout enforcement, retry, and
 * positive-ACK requirements.</p>
 */
public class ASTME1394ResponseValidationProperties extends ResponseValidationProperties {

    private boolean validateResponseChecksum = true;
    private int     responseTimeout           = 15000; // ms
    private int     maxRetryAttempts          = 3;
    private boolean validateResponseStructure  = true;
    private boolean requirePositiveAck        = true;

    @Override
    @SuppressWarnings("unchecked")
    public Map getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        props.put("validateResponseChecksum", new DataTypePropertyDescriptor(validateResponseChecksum, "Validate Response Checksum", "Validate LRC on inbound ACK/NAK responses.",           PropertyEditorType.BOOLEAN));
        props.put("responseTimeout",          new DataTypePropertyDescriptor(String.valueOf(responseTimeout), "Response Timeout (ms)", "Milliseconds to wait for remote system response.", PropertyEditorType.STRING));
        props.put("maxRetryAttempts",        new DataTypePropertyDescriptor(String.valueOf(maxRetryAttempts), "Max Retry Attempts",   "Maximum retry attempts for failed responses.",       PropertyEditorType.STRING));
        props.put("validateResponseStructure", new DataTypePropertyDescriptor(validateResponseStructure,      "Validate Response Structure", "Enforce ASTM frame structure on responses.",   PropertyEditorType.BOOLEAN));
        props.put("requirePositiveAck",        new DataTypePropertyDescriptor(requirePositiveAck,            "Require Positive ACK",        "Treat non-ACK responses as failures.",            PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;

        Object vrc = properties.get("validateResponseChecksum");
        if (vrc != null) this.validateResponseChecksum = (Boolean) vrc;

        Object rt = properties.get("responseTimeout");
        if (rt != null) {
            try { this.responseTimeout = Integer.parseInt(rt.toString()); }
            catch (NumberFormatException e) { this.responseTimeout = 15000; }
        }

        Object mra = properties.get("maxRetryAttempts");
        if (mra != null) {
            try { this.maxRetryAttempts = Integer.parseInt(mra.toString()); }
            catch (NumberFormatException e) { this.maxRetryAttempts = 3; }
        }

        Object vrs = properties.get("validateResponseStructure");
        if (vrs != null) this.validateResponseStructure = (Boolean) vrs;

        Object rpa = properties.get("requirePositiveAck");
        if (rpa != null) this.requirePositiveAck = (Boolean) rpa;
    }

    public boolean isValidateResponseChecksum() { return validateResponseChecksum; }
    public void    setValidateResponseChecksum(boolean v) { this.validateResponseChecksum = v; }
    public int     getResponseTimeout()         { return responseTimeout; }
    public void    setResponseTimeout(int v)     { this.responseTimeout = v; }
    public int     getMaxRetryAttempts()         { return maxRetryAttempts; }
    public void    setMaxRetryAttempts(int v)     { this.maxRetryAttempts = v; }
    public boolean isValidateResponseStructure() { return validateResponseStructure; }
    public void    setValidateResponseStructure(boolean v) { this.validateResponseStructure = v; }
    public boolean isRequirePositiveAck()       { return requirePositiveAck; }
    public void    setRequirePositiveAck(boolean v) { this.requirePositiveAck = v; }

    @Override public void migrate3_0_1(DonkeyElement e) {}
    @Override public void migrate3_0_2(DonkeyElement e) {}
    @Override public void migrate3_1_0(DonkeyElement e) {}
    @Override public void migrate3_2_0(DonkeyElement e) {}
    @Override public void migrate3_3_0(DonkeyElement e) {}
    @Override public void migrate3_4_0(DonkeyElement e) {}
    @Override public void migrate3_5_0(DonkeyElement e) {}
    @Override public void migrate3_6_0(DonkeyElement e) {}
    @Override public void migrate3_7_0(DonkeyElement e) {}
    @Override public void migrate3_9_0(DonkeyElement e) {}
    @Override public void migrate3_11_0(DonkeyElement e) {}
    @Override public void migrate3_11_1(DonkeyElement e) {}
    @Override public void migrate3_12_0(DonkeyElement e) {}

    @Override
    @SuppressWarnings("unchecked")
    public Map getPurgedProperties() {
        Map<String, Object> purged = new HashMap<String, Object>();
        purged.put("validateResponseChecksum", validateResponseChecksum);
        purged.put("responseTimeout",          responseTimeout);
        purged.put("maxRetryAttempts",         maxRetryAttempts);
        purged.put("validateResponseStructure", validateResponseStructure);
        purged.put("requirePositiveAck",        requirePositiveAck);
        return purged;
    }
}
