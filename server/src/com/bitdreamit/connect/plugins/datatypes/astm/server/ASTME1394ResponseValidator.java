package com.bitdreamit.mirth.astm.e1394.server;

import org.apache.log4j.Logger;

import com.mirth.connect.model.datatype.ResponseValidationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.server.message.DefaultResponseValidator;

public class ASTME1394ResponseValidator extends DefaultResponseValidator {
    private Logger logger = Logger.getLogger(this.getClass());
    private ASTME1394SerializationProperties serProps;
    private ASTME1394ResponseValidationProperties valProps;

    public ASTME1394ResponseValidator(SerializationProperties sp, ResponseValidationProperties vp) {
        this.serProps = (ASTME1394SerializationProperties) sp;
        this.valProps = (ASTME1394ResponseValidationProperties) vp;
    }

    @Override
    public boolean isValidResponse(String message, String response) {
        if (response == null || response.isEmpty()) {
            logger.warn("ASTM response is empty");
            return false;
        }

        if (valProps != null && valProps.isValidateResponseStructure()) {
            if (!response.startsWith(String.valueOf((char)0x02))) {
                logger.warn("Response missing STX frame start");
                return false;
            }
        }

        if (valProps != null && valProps.isRequirePositiveAck()) {
            if (response.indexOf(0x15) >= 0 || response.contains("AR") || response.contains("AE")) {
                logger.warn("Negative ASTM response detected");
                return false;
            }
        }

        return true;
    }
}
