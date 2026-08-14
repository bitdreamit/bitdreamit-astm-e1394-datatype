package com.bitdreamit.mirth.astm.e1394.server;

import com.bitdreamit.mirth.astm.e1394.shared.ASTME1394Constants;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;

public class ASTME1394DataTypeDelegate implements DataTypeDelegate {
    @Override public String getName() { return ASTME1394Constants.PLUGIN_NAME; }
    @Override public IMessageSerializer getSerializer(SerializerProperties properties) { return new ASTME1394Serializer(properties); }
    @Override public boolean isBinary() { return false; }
    @Override public SerializationType getSerializationType() { return SerializationType.XML; }
    @Override public DataTypeProperties getDefaultProperties() { return new ASTME1394DataTypeProperties(); }
}
