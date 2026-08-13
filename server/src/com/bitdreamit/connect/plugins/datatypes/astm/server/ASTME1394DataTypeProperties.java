package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.donkey.util.DonkeyElement;

public class ASTME1394DataTypeProperties extends DataTypeProperties {

    public ASTME1394DataTypeProperties() {
        setSerializationProperties(new ASTME1394SerializationProperties());
        setDeserializationProperties(new ASTME1394DeserializationProperties());
        setBatchProperties(new ASTME1394BatchProperties());
    }

    @Override
    public DonkeyElement toDonkeyElement() {
        DonkeyElement element = new DonkeyElement("dataTypeProperties");
        element.addChildElement(getSerializationProperties().toDonkeyElement());
        element.addChildElement(getDeserializationProperties().toDonkeyElement());
        element.addChildElement(getBatchProperties().toDonkeyElement());
        return element;
    }
}
