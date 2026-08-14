package com.bitdreamit.mirth.astm.e1394.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

/**
 * ASTM E1394 Response Validation Properties
 * Premium: Per-data-type timeout, checksum validation, retry logic
 */
public class ASTME1394ResponseValidationProperties extends ResponseValidationProperties {

    private boolean validateResponseChecksum = true;
    private int responseTimeout              = 15000; // ms
    private int maxRetryAttempts             = 3;     // Premium
    private boolean validateResponseStructure = true; // Premium
    private boolean requirePositiveAck       = true;  // Premium

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<>();

        props.put("validateResponseChecksum", new DataTypePropertyDescriptor(validateResponseChecksum, "Validate Response Checksum", "Validate LRC on inbound ACK/NAK responses.", PropertyEditorType.BOOLEAN));
        props.put("responseTimeout",          new DataTypePropertyDescriptor(responseTimeout, "Response Timeout (ms)", "Milliseconds to wait for remote system response.", PropertyEditorType.STRING));
        props.put("maxRetryAttempts",         new DataTypePropertyDescriptor(maxRetryAttempts, "Max Retry Attempts", "Maximum retry attempts for failed responses.", PropertyEditorType.STRING));
        props.put("validateResponseStructure", new DataTypePropertyDescriptor(validateResponseStructure, "Validate Response Structure", "Enforce ASTM frame structure on responses.", PropertyEditorType.BOOLEAN));
        props.put("requirePositiveAck",        new DataTypePropertyDescriptor(requirePositiveAck, "Require Positive ACK", "Treat non-ACK responses as failures.", PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        if (properties.get("validateResponseChecksum") != null) this.validateResponseChecksum = (Boolean) properties.get("validateResponseChecksum");
        if (properties.get("responseTimeout") != null) {
            try { this.responseTimeout = Integer.parseInt(properties.get("responseTimeout").toString()); }
            catch (Exception e) { this.responseTimeout = 15000; }
        }
        if (properties.get("maxRetryAttempts") != null) {
            try { this.maxRetryAttempts = Integer.parseInt(properties.get("maxRetryAttempts").toString()); }
            catch (Exception e) { this.maxRetryAttempts = 3; }
        }
        if (properties.get("validateResponseStructure") != null) this.validateResponseStructure = (Boolean) properties.get("validateResponseStructure");
        if (properties.get("requirePositiveAck") != null) this.requirePositiveAck = (Boolean) properties.get("requirePositiveAck");
    }

    public boolean isValidateResponseChecksum() { return validateResponseChecksum; }
    public void setValidateResponseChecksum(boolean validateResponseChecksum) { this.validateResponseChecksum = validateResponseChecksum; }
    public int getResponseTimeout() { return responseTimeout; }
    public void setResponseTimeout(int responseTimeout) { this.responseTimeout = responseTimeout; }
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    public boolean isValidateResponseStructure() { return validateResponseStructure; }
    public void setValidateResponseStructure(boolean validateResponseStructure) { this.validateResponseStructure = validateResponseStructure; }
    public boolean isRequirePositiveAck() { return requirePositiveAck; }
    public void setRequirePositiveAck(boolean requirePositiveAck) { this.requirePositiveAck = requirePositiveAck; }

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
        purged.put("validateResponseChecksum", validateResponseChecksum);
        purged.put("responseTimeout", responseTimeout);
        purged.put("maxRetryAttempts", maxRetryAttempts);
        purged.put("validateResponseStructure", validateResponseStructure);
        purged.put("requirePositiveAck", requirePositiveAck);
        return purged;
    }
}
