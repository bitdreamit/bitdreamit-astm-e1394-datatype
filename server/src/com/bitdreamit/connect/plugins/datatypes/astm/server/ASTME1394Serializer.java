package com.bitdreamit.connect.plugins.datatypes.astm.server;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.MessageSerializerException;

/**
 * ASTM E1394 message serializer — the single {@link IMessageSerializer} entry
 * point used by Mirth Connect's transformer pipeline.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>{@code toXML(raw)} — inbound path. Delegates to
 *       {@link ASTME1394Deserializer} to produce the canonical
 *       {@code <ASTM><H>…</H>…</ASTM>} document.</li>
 *   <li>{@code fromXML(xml)} — outbound path. Delegates to
 *       {@link ASTME1394FromXmlConverter} to produce ASTM E1394 raw text.</li>
 * </ul>
 *
 * <p>JSON conversion is not supported by the ASTM E1394 standard; the
 * {@code toJSON} / {@code fromJSON} methods return {@code null} (per the
 * {@link IMessageSerializer} contract for unsupported formats).</p>
 */
public class ASTME1394Serializer implements IMessageSerializer {

    private static final Logger logger = Logger.getLogger(ASTME1394Serializer.class);

    private final ASTME1394SerializationProperties   serProps;
    private final ASTME1394DeserializationProperties deserProps;

    public ASTME1394Serializer(SerializerProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("SerializerProperties must not be null");
        }
        this.serProps   = (ASTME1394SerializationProperties)   properties.getSerializationProperties();
        this.deserProps = (ASTME1394DeserializationProperties) properties.getDeserializationProperties();
    }

    /** Convenience constructor for unit tests that supply props directly. */
    public ASTME1394Serializer(ASTME1394SerializationProperties serProps,
                               ASTME1394DeserializationProperties deserProps) {
        this.serProps   = serProps   != null ? serProps   : new ASTME1394SerializationProperties();
        this.deserProps = deserProps != null ? deserProps : new ASTME1394DeserializationProperties();
    }

    @Override
    public String toXML(String source) throws MessageSerializerException {
        if (source == null) {
            return null;
        }
        try {
            // DoS protection — refuse oversized payloads early.
            int maxBytes = deserProps.getMaxMessageSize();
            if (maxBytes > 0) {
                int len;
                try {
                    len = source.getBytes(serProps.getEncoding()).length;
                } catch (java.io.UnsupportedEncodingException e) {
                    len = source.length();
                }
                if (len > maxBytes) {
                    throw new MessageSerializerException(
                        "ASTM E1394 message exceeds max-size limit (" + maxBytes + " bytes, got " + len + ")");
                }
            }
            return new ASTME1394Deserializer(deserProps).toXML(source);
        } catch (MessageSerializerException e) {
            throw e;
        } catch (Exception e) {
            logger.error("ASTM → XML conversion failed", e);
            throw new MessageSerializerException(e);
        }
    }

    @Override
    public String fromXML(String source) throws MessageSerializerException {
        if (source == null) {
            return null;
        }
        try {
            return new ASTME1394FromXmlConverter(serProps).convert(source);
        } catch (Exception e) {
            logger.error("XML → ASTM conversion failed", e);
            throw new MessageSerializerException(e);
        }
    }

    /**
     * Serialize a pre-parsed DOM {@link Document} into ASTM text. Useful when
     * transformer steps manipulate the DOM directly.
     */
    public String fromXML(Document doc) throws MessageSerializerException {
        if (doc == null) {
            return null;
        }
        try {
            return new ASTME1394FromXmlConverter(serProps).convert(doc);
        } catch (Exception e) {
            logger.error("DOM → ASTM conversion failed", e);
            throw new MessageSerializerException(e);
        }
    }

    @Override
    public String toJSON(String source) {
        // ASTM E1394 has no canonical JSON representation.
        return null;
    }

    @Override
    public String fromJSON(String source) {
        return null;
    }

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        // Always serialize — we need DOM access for transformer steps.
        return true;
    }
}
