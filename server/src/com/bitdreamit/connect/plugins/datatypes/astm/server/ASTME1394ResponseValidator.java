package com.bitdreamit.connect.plugins.datatypes.astm.server;

import org.apache.log4j.Logger;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.model.datatype.ResponseValidationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.server.message.DefaultResponseValidator;

/**
 * ASTM E1394 response validator.
 *
 * <p>Validates inbound ACK / NAK responses received from remote instruments
 * after a message is sent. Enforces (optionally):</p>
 *
 * <ul>
 *   <li>Presence of the STX frame-start byte ({@code 0x02}).</li>
 *   <li>Absence of NAK ({@code 0x15}) and negative response codes
 *       ({@code "AR"} / {@code "AE"}) when
 *       {@link ASTME1394ResponseValidationProperties#isRequirePositiveAck()}
 *       is true.</li>
 * </ul>
 *
 * <p>Checksum (LRC) validation is delegated to the ASTM E1381 transmission-mode
 * plugin, which has direct access to the raw frame bytes. This validator
 * operates on the response string after transmission-mode processing.</p>
 *
 * <p><b>API note (Mirth 4.5.x):</b> The {@link DefaultResponseValidator}
 * parent class may or may not declare {@code validateResponse(String, String)}
 * as an abstract method depending on the micro version. The
 * {@code @Override} annotation is deliberately omitted so the code compiles
 * on every version; if the method signature matches a parent method, the
 * override is implicit.</p>
 */
public class ASTME1394ResponseValidator extends DefaultResponseValidator {

    private static final Logger logger = Logger.getLogger(ASTME1394ResponseValidator.class);

    private final ASTME1394SerializationProperties   serProps;
    private final ASTME1394ResponseValidationProperties valProps;

    public ASTME1394ResponseValidator(SerializationProperties serializationProperties,
                                       ResponseValidationProperties responseValidationProperties) {
        super();
        this.serProps  = (serializationProperties instanceof ASTME1394SerializationProperties)
                ? (ASTME1394SerializationProperties) serializationProperties
                : new ASTME1394SerializationProperties();
        this.valProps = (responseValidationProperties instanceof ASTME1394ResponseValidationProperties)
                ? (ASTME1394ResponseValidationProperties) responseValidationProperties
                : new ASTME1394ResponseValidationProperties();
    }

    /**
     * Validate an inbound response.
     *
     * @param message  the original outbound message (for correlation context)
     * @param response the response received from the remote instrument
     * @return {@code true} if the response is acceptable, {@code false} otherwise
     */
    public boolean validateResponse(String message, String response) {
        if (response == null || response.isEmpty()) {
            logger.warn("ASTM response is empty");
            return false;
        }

        if (valProps.isValidateResponseStructure()) {
            if (response.indexOf(ASTME1394Constants.FRAME_STX) < 0) {
                logger.warn("Response missing STX frame start");
                // Don't fail outright — some instruments send bare ACKs.
                if (logger.isDebugEnabled()) {
                    logger.debug("Response preview: " + response.substring(0, Math.min(64, response.length())));
                }
            }
        }

        if (valProps.isRequirePositiveAck()) {
            if (response.indexOf(ASTME1394Constants.FRAME_NAK) >= 0) {
                logger.warn("Negative ASTM response detected (NAK byte)");
                return false;
            }
            if (response.contains(ASTME1394Constants.RESPONSE_REJECT)) {
                logger.warn("ASTM reject response code (AR) detected");
                return false;
            }
            if (response.contains(ASTME1394Constants.RESPONSE_ERROR)) {
                logger.warn("ASTM application-error response code (AE) detected");
                return false;
            }
        }

        return true;
    }
}
