package com.bitdreamit.mirth.astm.e1394.server;

import org.apache.log4j.Logger;

import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.MessageSerializerException;

public class ASTME1394Serializer implements IMessageSerializer {
    private Logger logger = Logger.getLogger(this.getClass());
    private ASTME1394SerializationProperties serProps;
    private ASTME1394DeserializationProperties deserProps;

    public ASTME1394Serializer(SerializerProperties properties) {
        this.serProps = (ASTME1394SerializationProperties) properties.getSerializationProperties();
        this.deserProps = (ASTME1394DeserializationProperties) properties.getDeserializationProperties();
    }

    @Override
    public String toXML(String source) throws MessageSerializerException {
        try {
            if (source == null) return null;
            // DoS protection
            if (deserProps != null && source.getBytes(serProps.getEncoding()).length > deserProps.getMaxMessageSize()) {
                throw new MessageSerializerException("Message exceeds max size limit (" + deserProps.getMaxMessageSize() + " bytes)");
            }
            return new ASTME1394ToXmlConverter(serProps, deserProps).convert(source);
        } catch (Exception e) {
            logger.error("ASTM to XML conversion failed", e);
            throw new MessageSerializerException(e);
        }
    }

    @Override
    public String fromXML(String source) throws MessageSerializerException {
        try {
            if (source == null) return null;
            return new ASTME1394FromXmlConverter(serProps).convert(source);
        } catch (Exception e) {
            logger.error("XML to ASTM conversion failed", e);
            throw new MessageSerializerException(e);
        }
    }

    @Override public String toJSON(String source) { return null; }
    @Override public String fromJSON(String source) { return null; }
}
