/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Automatic ASTM response message generator.
 * Extra feature beyond commercial extension.
 * Generates MSA (Message Status Acknowledgment) records.
 */
public class AstmResponseGenerator {
    private static final Logger logger = Logger.getLogger(AstmResponseGenerator.class);
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");

    public enum Status { AA, AE, AR }

    /**
     * Generate ASTM response message.
     * @param originalMessage The incoming ASTM message
     * @param status AA=Accepted, AE=Application Error, AR=Rejected
     * @param errorMessage Optional error description
     * @return Complete ASTM response with H, M, L records
     */
    public String generateResponse(String originalMessage, Status status, String errorMessage) {
        try {
            String[] lines = originalMessage.split("\r\n|\r|\n");
            String controlId = "1";
            String sender = "LIS";
            String receiver = "SNIBE";

            for (String line : lines) {
                if (line.startsWith("H|")) {
                    String[] parts = line.split("\|", -1);
                    if (parts.length > 4) sender = parts[4].isEmpty() ? "LIS" : parts[4];
                    if (parts.length > 5) receiver = parts[5].isEmpty() ? "SNIBE" : parts[5];
                    if (parts.length > 13) controlId = parts[13].isEmpty() ? "1" : parts[13];
                    break;
                }
            }

            String now = fmt.format(new Date());
            StringBuilder sb = new StringBuilder();
            sb.append("H|\^&|||").append(receiver).append("|||||").append(sender)
              .append("||||").append(now).append("\r");
            sb.append("M|1|").append(controlId).append("|").append(status.name()).append("|");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                sb.append(errorMessage);
            }
            sb.append("\r");
            sb.append("L|1|N\r");

            logger.info("Generated ASTM response: status=" + status + ", controlId=" + controlId);
            return sb.toString();
        } catch (Exception e) {
            logger.error("Response generation error", e);
            return "H|\^&|||LIS|||||SNIBE||||" + fmt.format(new Date()) + "\rM|1|1|AE|Internal error\rL|1|N\r";
        }
    }

    public String generateAck(String originalMessage) {
        return generateResponse(originalMessage, Status.AA, null);
    }

    public String generateNak(String originalMessage, String error) {
        return generateResponse(originalMessage, Status.AR, error);
    }
}