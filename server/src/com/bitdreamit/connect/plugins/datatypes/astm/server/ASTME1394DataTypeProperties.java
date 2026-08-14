package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.datatype.DataTypeProperties;

/**
 * Container that wires together the five ASTM E1394 property groups used by
 * Mirth Connect's data-type framework.
 *
 * <p>Mirth instantiates this class via the default constructor whenever a new
 * channel / connector is created. The five nested property objects then
 * expose their individual {@code PropertyDescriptor} maps to the Administrator
 * UI for editing.</p>
 */
public class ASTME1394DataTypeProperties extends DataTypeProperties {

    public ASTME1394DataTypeProperties() {
        serializationProperties       = new ASTME1394SerializationProperties();
        deserializationProperties     = new ASTME1394DeserializationProperties();
        batchProperties               = new ASTME1394BatchProperties();
        responseGenerationProperties  = new ASTME1394ResponseGenerationProperties();
        responseValidationProperties  = new ASTME1394ResponseValidationProperties();
    }
}
