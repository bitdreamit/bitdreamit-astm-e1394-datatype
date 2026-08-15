package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;

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
 *
 * <p><b>Exception handling:</b> In Mirth Connect 4.x the
 * {@link IMessageSerializer} interface methods do not declare any checked
 * exceptions (the legacy {@code MessageSerializerException} type was
 * removed). All parser / serializer failures are therefore wrapped into a
 * {@link RuntimeException} so they propagate through Mirth's transformer
 * pipeline as unchecked errors, matching the behaviour of the built-in
 * HL7v2 / XML / JSON serializers.</p>
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
    public String toXML(String source) {
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
                    throw new RuntimeException(
                        "ASTM E1394 message exceeds max-size limit (" + maxBytes + " bytes, got " + len + ")");
                }
            }
            return new ASTME1394Deserializer(deserProps).toXML(source);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("ASTM → XML conversion failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String fromXML(String source) {
        if (source == null) {
            return null;
        }
        try {
            return new ASTME1394FromXmlConverter(serProps).convert(source);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("XML → ASTM conversion failed", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Serialize a pre-parsed DOM {@link Document} into ASTM text. Useful when
     * transformer steps manipulate the DOM directly.
     */
    public String fromXML(Document doc) {
        if (doc == null) {
            return null;
        }
        try {
            return new ASTME1394FromXmlConverter(serProps).convert(doc);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("DOM → ASTM conversion failed", e);
            throw new RuntimeException(e);
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

    /**
     * Extract metadata from an inbound ASTM E1394 message. Returns the
     * record-type letters observed in the message so Mirth's router /
     * filter steps can key off them. Always returns a non-null map (empty
     * if the message is null or empty).
     */
    @Override
    public Map<String, String> getMetaDataFromMessage(String message) {
        Map<String, String> meta = new LinkedHashMap<String, String>();
        if (message == null || message.isEmpty()) {
            return meta;
        }
        // Record the leading letter of every non-empty line.
        StringBuilder types = new StringBuilder();
        String normalized = message.replace("\r\n", "\r").replace("\n", "\r");
        for (String line : normalized.split("\r", -1)) {
            if (line == null || line.isEmpty()) continue;
            char c = line.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                if (types.length() > 0) types.append(',');
                types.append(c);
            }
        }
        if (types.length() > 0) {
            meta.put("recordTypes", types.toString());
            // The first record type is conventionally the message type.
            meta.put("type", String.valueOf(types.charAt(0)));
        }
        meta.put("encoding", serProps.getEncoding());
        return meta;
    }
}
