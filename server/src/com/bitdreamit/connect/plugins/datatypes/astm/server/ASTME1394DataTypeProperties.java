package com.bitdreamit.mirth.astm.e1394.server;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class ASTME1394DataTypeProperties extends DataTypeProperties {
    public ASTME1394DataTypeProperties() {
        serializationProperties        = new ASTME1394SerializationProperties();
        deserializationProperties      = new ASTME1394DeserializationProperties();
        batchProperties                = new ASTME1394BatchProperties();
        responseGenerationProperties   = new ASTME1394ResponseGenerationProperties();
        responseValidationProperties   = new ASTME1394ResponseValidationProperties();
    }
}
