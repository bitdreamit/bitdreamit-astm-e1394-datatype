package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.model.datatype.DeserializationProperties;
import org.w3c.dom.Document;

public class ASTME1394MessageSerializer implements IMessageSerializer {

    private ASTME1394SerializationProperties serializationProperties;
    private ASTME1394DeserializationProperties deserializationProperties;

    public ASTME1394MessageSerializer(SerializationProperties serializationProperties,
                                       DeserializationProperties deserializationProperties) {
        this.serializationProperties = (ASTME1394SerializationProperties) serializationProperties;
        this.deserializationProperties = (ASTME1394DeserializationProperties) deserializationProperties;
    }

    @Override
    public String toXML(String source) throws Exception {
        return new ASTME1394Deserializer(deserializationProperties).toXML(source);
    }

    @Override
    public String fromXML(String source) throws Exception {
        Document doc = com.mirth.connect.util.XmlUtil.parse(source);
        return new ASTME1394Serializer(serializationProperties).fromXML(doc);
    }

    @Override
    public String toJSON(String source) throws Exception {
        throw new UnsupportedOperationException("JSON not supported for ASTM E1394");
    }

    @Override
    public String fromJSON(String source) throws Exception {
        throw new UnsupportedOperationException("JSON not supported for ASTM E1394");
    }

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        return true;
    }
}
