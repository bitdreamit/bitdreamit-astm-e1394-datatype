package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;

/**
 * Mirth Connect {@link DataTypeDelegate} for ASTM E1394 record-level messages.
 *
 * <p>Registered in {@code server/resources/plugin.xml} and instantiated by the
 * Mirth server plugin loader. The delegate is the entry point that wires the
 * data type into Mirth's serializer / deserializer pipeline.</p>
 */
public class ASTME1394DataTypeDelegate implements DataTypeDelegate {

    @Override
    public String getName() {
        return ASTME1394Constants.PLUGIN_NAME;
    }

    @Override
    public IMessageSerializer getSerializer(SerializerProperties properties) {
        return new ASTME1394Serializer(properties);
    }

    @Override
    public boolean isBinary() {
        // ASTM E1394 is a text-based record protocol; no binary framing at this layer.
        return false;
    }

    @Override
    public SerializationType getSerializationType() {
        return SerializationType.XML;
    }

    @Override
    public DataTypeProperties getDefaultProperties() {
        return new ASTME1394DataTypeProperties();
    }
}
