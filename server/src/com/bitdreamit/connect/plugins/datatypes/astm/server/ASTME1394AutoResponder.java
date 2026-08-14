package com.bitdreamit.mirth.astm.e1394.server;

import org.apache.log4j.Logger;

import com.bitdreamit.mirth.astm.e1394.shared.ASTME1394Constants;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.server.message.DefaultAutoResponder;

public class ASTME1394AutoResponder extends DefaultAutoResponder {
    private Logger logger = Logger.getLogger(this.getClass());
    private ASTME1394SerializationProperties serProps;
    private ASTME1394ResponseGenerationProperties genProps;

    public ASTME1394AutoResponder(SerializationProperties sp, ResponseGenerationProperties gp) {
        this.serProps = (ASTME1394SerializationProperties) sp;
        this.genProps = (ASTME1394ResponseGenerationProperties) gp;
    }

    @Override
    public String getResponse(String message, String status, String destination) {
        if (genProps == null) return ASTME1394Constants.RESPONSE_ACCEPT;

        StringBuilder sb = new StringBuilder();

        if (genProps.isWrapInASTMFrame()) {
            char stx = 0x02, etx = 0x03, cr = 0x0D, lf = 0x0A;
            String responseText;
            if ("ERROR".equalsIgnoreCase(status)) {
                responseText = genProps.getErrorResponseCode();
            } else if ("REJECT".equalsIgnoreCase(status)) {
                responseText = genProps.getRejectResponseCode();
            } else {
                responseText = genProps.getSuccessResponseCode();
            }

            String seq = genProps.isIncludeSequenceNumber() ? "1" : "";
            String payload = seq + responseText;
            if (genProps.isIncludeTimestamp()) {
                payload += "|" + System.currentTimeMillis();
            }
            payload += etx;

            char lrc = calculateLRC(payload);
            sb.append(stx).append(payload).append(lrc).append(cr).append(lf);
        } else {
            sb.append(ASTME1394Constants.RESPONSE_ACCEPT);
        }

        logger.debug("ASTM AutoResponse: " + sb.toString().replace("\r", "<CR>").replace("\n", "<LF>"));
        return sb.toString();
    }

    private char calculateLRC(String data) {
        char lrc = 0;
        for (int i = 0; i < data.length(); i++) {
            lrc ^= data.charAt(i);
        }
        return lrc;
    }
}
