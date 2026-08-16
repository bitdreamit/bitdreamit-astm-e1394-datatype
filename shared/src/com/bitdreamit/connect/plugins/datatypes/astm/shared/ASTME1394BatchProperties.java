package com.bitdreamit.connect.plugins.datatypes.astm.shared;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;

/**
 * ASTM E1394 batch-processing properties.
 *
 * <p>Controls how Mirth Connect splits an inbound ASTM batch (multiple
 * sessions concatenated together) into individual messages for the
 * transformer pipeline. The default strategy is to split on
 * {@code H…L} boundaries, which preserves order and ensures each emitted
 * message is a complete, valid ASTM session.</p>
 */
public class ASTME1394BatchProperties extends BatchProperties {

    /** Default batch split type: split on H..L boundary (one ASTM session per message). */
    public static final String SPLIT_TYPE_H_L_BOUNDARY = "H_L_BOUNDARY";
    /** Alternative split type: split on each individual record (H, P, O, R, …). */
    public static final String SPLIT_TYPE_RECORD        = "RECORD";
    /** Alternative split type: do not split — pass through the entire batch as one message. */
    public static final String SPLIT_TYPE_NONE         = "NONE";

    private boolean splitByRecord     = true;
    private int     batchTimeout      = 5000;   // ms — max idle time before flushing a partial batch
    private int     maxBatchSize       = 1000;  // max messages per batch
    private String  splitBatchBy      = SPLIT_TYPE_H_L_BOUNDARY;
    private boolean preserveOrder     = true;
    private boolean includeTerminator = true;

    @Override
    @SuppressWarnings("unchecked")
    public Map getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> props = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        props.put("splitByRecord",     new DataTypePropertyDescriptor(splitByRecord,    "Split by Record",      "Treat each ASTM record as a separate message in batch.",                      PropertyEditorType.BOOLEAN));
        props.put("batchTimeout",      new DataTypePropertyDescriptor(String.valueOf(batchTimeout),  "Batch Timeout (ms)",  "Max idle time before flushing partial batch.",                              PropertyEditorType.STRING));
        props.put("maxBatchSize",      new DataTypePropertyDescriptor(String.valueOf(maxBatchSize),    "Max Batch Size",     "Maximum number of messages per batch.",                                     PropertyEditorType.STRING));
        props.put("splitBatchBy",      new DataTypePropertyDescriptor(splitBatchBy,    "Split Batch By",       "Batch splitting strategy.",                                                  PropertyEditorType.STRING, new Object[]{SPLIT_TYPE_H_L_BOUNDARY, SPLIT_TYPE_RECORD, SPLIT_TYPE_NONE}));
        props.put("preserveOrder",     new DataTypePropertyDescriptor(preserveOrder,   "Preserve Order",       "Maintain original record order in batch processing.",                        PropertyEditorType.BOOLEAN));
        props.put("includeTerminator", new DataTypePropertyDescriptor(includeTerminator,"Include Terminator",   "Include L (terminator) records in batch output.",                            PropertyEditorType.BOOLEAN));

        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(Map properties) {
        if (properties == null) return;
        Object sbr = properties.get("splitByRecord");
        if (sbr != null) this.splitByRecord = (Boolean) sbr;

        Object bt = properties.get("batchTimeout");
        if (bt != null) {
            try { this.batchTimeout = Integer.parseInt(bt.toString()); }
            catch (NumberFormatException e) { this.batchTimeout = 5000; }
        }

        Object mbs = properties.get("maxBatchSize");
        if (mbs != null) {
            try { this.maxBatchSize = Integer.parseInt(mbs.toString()); }
            catch (NumberFormatException e) { this.maxBatchSize = 1000; }
        }

        Object sbb = properties.get("splitBatchBy");
        if (sbb != null) this.splitBatchBy = sbb.toString();

        Object po = properties.get("preserveOrder");
        if (po != null) this.preserveOrder = (Boolean) po;

        Object it = properties.get("includeTerminator");
        if (it != null) this.includeTerminator = (Boolean) it;
    }

    public boolean isSplitByRecord()        { return splitByRecord; }
    public void    setSplitByRecord(boolean v) { this.splitByRecord = v; }
    public int     getBatchTimeout()         { return batchTimeout; }
    public void    setBatchTimeout(int v)     { this.batchTimeout = v; }
    public int     getMaxBatchSize()          { return maxBatchSize; }
    public void    setMaxBatchSize(int v)     { this.maxBatchSize = v; }
    public String  getSplitBatchBy()         { return splitBatchBy; }
    public void    setSplitBatchType(String v) { this.splitBatchBy = v; }
    public void    setSplitBatchBy(String v) { this.splitBatchBy = v; }
    public boolean isPreserveOrder()        { return preserveOrder; }
    public void    setPreserveOrder(boolean v) { this.preserveOrder = v; }
    public boolean isIncludeTerminator()    { return includeTerminator; }
    public void    setIncludeTerminator(boolean v) { this.includeTerminator = v; }

    /**
     * Return the batch splitting script (JavaScript).
     *
     * <p>The ASTM E1394 plugin performs batch splitting natively in
     * {@link ASTME1394BatchAdaptor} (using the H..L boundary / record /
     * no-split strategy configured via {@link #setSplitBatchBy(String)}),
     * so no JavaScript snippet is required — return {@code null} to signal
     * the framework that the native batch adaptor handles splitting.</p>
     */
    @Override
    public String getBatchScript() {
        return null;
    }

    /**
     * Setter for the batch script.
     *
     * <p>Not declared as {@code @Override} because the parent
     * {@code BatchProperties} class does not declare a {@code setBatchScript}
     * method in Mirth 4.5.x — only the getter is abstract. HL7v2's
     * {@code HL7v2BatchProperties.setBatchScript(String)} is also a regular
     * method, not an override. This method is a no-op because the ASTM
     * plugin doesn't use JavaScript batch scripts.</p>
     */
    public void setBatchScript(String batchScript) {
        // No-op — native batch adaptor handles splitting.
    }

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
        purged.put("splitByRecord",     splitByRecord);
        purged.put("batchTimeout",      batchTimeout);
        purged.put("maxBatchSize",      maxBatchSize);
        purged.put("splitBatchBy",      splitBatchBy);
        purged.put("preserveOrder",     preserveOrder);
        purged.put("includeTerminator", includeTerminator);
        return purged;
    }
}
