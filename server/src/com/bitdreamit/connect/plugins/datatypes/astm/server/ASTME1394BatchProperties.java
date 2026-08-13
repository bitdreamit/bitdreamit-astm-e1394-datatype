package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.donkey.util.DonkeyElement;

public class ASTME1394BatchProperties extends BatchProperties {
    private String batchSplitType = "H_L_BOUNDARY";

    public String getBatchSplitType() { return batchSplitType; }
    public void setBatchSplitType(String v) { this.batchSplitType = v; }

    @Override
    public DonkeyElement toDonkeyElement() {
        DonkeyElement element = new DonkeyElement("batchProperties");
        element.addChildElement("batchSplitType", batchSplitType);
        return element;
    }
}
