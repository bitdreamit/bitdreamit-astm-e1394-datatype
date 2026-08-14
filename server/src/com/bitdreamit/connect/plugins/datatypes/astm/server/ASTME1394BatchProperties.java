package com.bitdreamit.mirth.astm.e1394.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;

/**
 * ASTM E1394 Batch Properties
 * Premium: Split by record type, batch timeout, max batch size
 */
public class ASTME1394BatchProperties extends BatchProperties {

    private boolean splitByRecord      = true;
    private int batchTimeout           = 5000;  // ms
    private int maxBatchSize           = 1000;  // max messages per batch
    private String splitBatchBy        = "Record"; // Record / Session / None
    private boolean preserveOrder      = true;   // Premium: maintain record order
    private boolean includeTerminator  = true;   // Premium: include L record in batch

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<>();

        props.put("splitByRecord",     new DataTypePropertyDescriptor(splitByRecord, "Split by Record", "Treat each ASTM record as a separate message in batch.", PropertyEditorType.BOOLEAN));
        props.put("batchTimeout",      new DataTypePropertyDescriptor(batchTimeout, "Batch Timeout (ms)", "Max idle time before flushing partial batch.", PropertyEditorType.STRING));
        props.put("maxBatchSize",      new DataTypePropertyDescriptor(maxBatchSize, "Max Batch Size", "Maximum number of messages per batch.", PropertyEditorType.STRING));
        props.put("splitBatchBy",      new DataTypePropertyDescriptor(splitBatchBy, "Split Batch By", "Batch splitting strategy.", PropertyEditorType.STRING, new Object[]{"Record", "Session", "None"}));
        props.put("preserveOrder",     new DataTypePropertyDescriptor(preserveOrder, "Preserve Order", "Maintain original record order in batch processing.", PropertyEditorType.BOOLEAN));
        props.put("includeTerminator", new DataTypePropertyDescriptor(includeTerminator, "Include Terminator", "Include L (terminator) records in batch output.", PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        if (properties.get("splitByRecord") != null)     this.splitByRecord     = (Boolean) properties.get("splitByRecord");
        if (properties.get("batchTimeout") != null) {
            try { this.batchTimeout = Integer.parseInt(properties.get("batchTimeout").toString()); }
            catch (Exception e) { this.batchTimeout = 5000; }
        }
        if (properties.get("maxBatchSize") != null) {
            try { this.maxBatchSize = Integer.parseInt(properties.get("maxBatchSize").toString()); }
            catch (Exception e) { this.maxBatchSize = 1000; }
        }
        if (properties.get("splitBatchBy") != null)      this.splitBatchBy      = (String) properties.get("splitBatchBy");
        if (properties.get("preserveOrder") != null)   this.preserveOrder     = (Boolean) properties.get("preserveOrder");
        if (properties.get("includeTerminator") != null) this.includeTerminator = (Boolean) properties.get("includeTerminator");
    }

    public boolean isSplitByRecord() { return splitByRecord; }
    public void setSplitByRecord(boolean splitByRecord) { this.splitByRecord = splitByRecord; }
    public int getBatchTimeout() { return batchTimeout; }
    public void setBatchTimeout(int batchTimeout) { this.batchTimeout = batchTimeout; }
    public int getMaxBatchSize() { return maxBatchSize; }
    public void setMaxBatchSize(int maxBatchSize) { this.maxBatchSize = maxBatchSize; }
    public String getSplitBatchBy() { return splitBatchBy; }
    public void setSplitBatchBy(String splitBatchBy) { this.splitBatchBy = splitBatchBy; }
    public boolean isPreserveOrder() { return preserveOrder; }
    public void setPreserveOrder(boolean preserveOrder) { this.preserveOrder = preserveOrder; }
    public boolean isIncludeTerminator() { return includeTerminator; }
    public void setIncludeTerminator(boolean includeTerminator) { this.includeTerminator = includeTerminator; }

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
        purged.put("splitByRecord", splitByRecord);
        purged.put("batchTimeout", batchTimeout);
        purged.put("maxBatchSize", maxBatchSize);
        purged.put("splitBatchBy", splitBatchBy);
        purged.put("preserveOrder", preserveOrder);
        purged.put("includeTerminator", includeTerminator);
        return purged;
    }
}
