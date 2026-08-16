package com.bitdreamit.connect.plugins.datatypes.astm.shared;

import org.apache.log4j.Logger;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.server.message.DefaultAutoResponder;

/**
 * ASTM E1394 auto-responder.
 *
 * <p>Constructs an ASTM E1381-framed ACK response for every inbound message
 * received by the source connector. The response codes, sequence numbering,
 * timestamp inclusion, and frame wrapping are all configurable through
 * {@link ASTME1394ResponseGenerationProperties}.</p>
 *
 * <p>The LRC (Longitudinal Redundancy Check) byte is computed per ASTM E1381
 * §6.3.2 by XOR-ing every byte of the payload (including ETX, excluding STX
 * and the LRC byte itself).</p>
 *
 * <p><b>API note (Mirth 4.5.x):</b> {@link DefaultAutoResponder} in Mirth
 * 4.5.x exposes only a no-arg constructor — the previous one-arg
 * {@code DefaultAutoResponder(ResponseGenerationProperties)} constructor
 * was removed. The response-generation properties are stored locally and
 * used by {@link #getResponse(String, String, String)}.</p>
 *
 * <p>The {@code getResponse(String, String, String)} method signature may
 * or may not match a parent method depending on the Mirth micro version.
 * The {@code @Override} annotation is deliberately omitted so the code
 * compiles on every version; if the signature matches a parent method,
 * the override is implicit.</p>
 */
public class ASTME1394AutoResponder extends DefaultAutoResponder {

    private static final Logger logger = Logger.getLogger(ASTME1394AutoResponder.class);

    private final ASTME1394SerializationProperties   serProps;
    private final ASTME1394ResponseGenerationProperties genProps;

    public ASTME1394AutoResponder(SerializationProperties serializationProperties,
                                   ResponseGenerationProperties responseGenerationProperties) {
        super();
        this.serProps  = (serializationProperties instanceof ASTME1394SerializationProperties)
                ? (ASTME1394SerializationProperties) serializationProperties
                : new ASTME1394SerializationProperties();
        this.genProps = (responseGenerationProperties instanceof ASTME1394ResponseGenerationProperties)
                ? (ASTME1394ResponseGenerationProperties) responseGenerationProperties
                : new ASTME1394ResponseGenerationProperties();
    }

    /**
     * Build an ASTM E1381-framed ACK response.
     *
     * @param message     the original inbound message (unused in the simple
     *                    ACK case, but available for correlation)
     * @param status      "SUCCESS", "ERROR", or "REJECT"
     * @param destination the destination connector name (unused)
     * @return the framed ACK string
     */
    public String getResponse(String message, String status, String destination) {
        if (genProps == null) {
            return ASTME1394Constants.RESPONSE_ACCEPT;
        }

        StringBuilder sb = new StringBuilder(64);

        if (genProps.isWrapInASTMFrame()) {
            String responseCode;
            if ("ERROR".equalsIgnoreCase(status)) {
                responseCode = genProps.getErrorResponseCode();
            } else if ("REJECT".equalsIgnoreCase(status)) {
                responseCode = genProps.getRejectResponseCode();
            } else {
                responseCode = genProps.getSuccessResponseCode();
            }

            // Payload layout: [seq]<code>[|<timestamp>]<ETX>
            String seq = genProps.isIncludeSequenceNumber() ? "1" : "";
            StringBuilder payload = new StringBuilder();
            payload.append(seq).append(responseCode);
            if (genProps.isIncludeTimestamp()) {
                payload.append('|').append(System.currentTimeMillis());
            }
            payload.append(ASTME1394Constants.FRAME_ETX);

            byte lrc = calculateLRC(payload.toString(), serProps.getEncoding());

            sb.append(ASTME1394Constants.FRAME_STX);
            sb.append(payload);
            sb.append((char) (lrc & 0xFF));
            sb.append('\r').append('\n');
        } else {
            // Unwrapped: just emit the response code.
            sb.append(genProps.getSuccessResponseCode());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("ASTM AutoResponse: " + toDebugString(sb.toString()));
        }
        return sb.toString();
    }

    /**
     * Compute the ASTM E1381 LRC over the payload string (which already
     * includes the trailing ETX byte). Per the spec, STX is excluded, the
     * LRC byte itself is excluded, and every byte between (including ETX)
     * is XOR-ed together.
     */
    private byte calculateLRC(String payload, String encoding) {
        byte[] bytes;
        try {
            bytes = payload.getBytes(encoding);
        } catch (java.io.UnsupportedEncodingException e) {
            bytes = payload.getBytes();
        }
        byte lrc = 0;
        for (byte b : bytes) {
            lrc ^= b;
        }
        return lrc;
    }

    /** Replace control characters with printable debug tokens for log output. */
    private String toDebugString(String s) {
        StringBuilder out = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case ASTME1394Constants.FRAME_STX: out.append("<STX>"); break;
                case ASTME1394Constants.FRAME_ETX: out.append("<ETX>"); break;
                case ASTME1394Constants.FRAME_ETB: out.append("<ETB>"); break;
                case ASTME1394Constants.FRAME_EOT: out.append("<EOT>"); break;
                case ASTME1394Constants.FRAME_ENQ: out.append("<ENQ>"); break;
                case ASTME1394Constants.FRAME_ACK: out.append("<ACK>"); break;
                case ASTME1394Constants.FRAME_NAK: out.append("<NAK>"); break;
                case '\r': out.append("<CR>"); break;
                case '\n': out.append("<LF>"); break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        out.append(String.format("<0x%02X>", (int) c & 0xFF));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }
}
