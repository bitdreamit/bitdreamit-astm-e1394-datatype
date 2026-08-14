package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.datatype.SerializerProperties;

/**
 * ASTM E1394 batch adaptor.
 *
 * <p>Reads raw bytes from a {@link BatchMessageSource} and emits individual
 * ASTM E1394 messages split on {@code H…L} boundaries. The split strategy is
 * configurable via {@link ASTME1394BatchProperties#getSplitBatchBy()}:</p>
 *
 * <ul>
 *   <li>{@code H_L_BOUNDARY} (default) — each {@code H…L} session becomes one
 *       message.</li>
 *   <li>{@code RECORD} — every record becomes its own message.</li>
 *   <li>{@code NONE} — the entire batch is passed through as a single message.</li>
 * </ul>
 *
 * <p>The static {@link #getMessages(String, ASTME1394BatchProperties)} helper
 * is exposed for unit testing and for use by transformer steps that already
 * hold the batch as a string.</p>
 */
public class ASTME1394BatchAdaptor extends BatchAdaptor {

    private static final Logger logger = Logger.getLogger(ASTME1394BatchAdaptor.class);

    private final SerializerProperties properties;
    private final ASTME1394BatchProperties batchProps;
    private final Charset charset;

    public ASTME1394BatchAdaptor(SourceConnector sourceConnector,
                                  BatchMessageSource batchMessageSource,
                                  SerializerProperties properties) {
        super(sourceConnector, batchMessageSource);
        this.properties = properties;
        ASTME1394BatchProperties bp = null;
        if (properties != null && properties.getBatchProperties() instanceof ASTME1394BatchProperties) {
            bp = (ASTME1394BatchProperties) properties.getBatchProperties();
        }
        this.batchProps = (bp != null) ? bp : new ASTME1394BatchProperties();

        Charset cs;
        try {
            String enc = (properties != null && properties.getSerializationProperties() != null)
                    ? properties.getSerializationProperties().getPropertyDescriptors().get("encoding").toString()
                    : ASTME1394Constants.DEFAULT_ENCODING;
            cs = Charset.forName(enc);
        } catch (Exception e) {
            cs = Charset.forName(ASTME1394Constants.DEFAULT_ENCODING);
        }
        this.charset = cs;
    }

    /** Convenience constructor for unit tests that pass batch props directly. */
    public ASTME1394BatchAdaptor(ASTME1394BatchProperties batchProps) {
        super(null, null);
        this.properties  = null;
        this.batchProps = (batchProps != null) ? batchProps : new ASTME1394BatchProperties();
        this.charset    = Charset.forName(ASTME1394Constants.DEFAULT_ENCODING);
    }

    @Override
    public String getMessage() throws BatchMessageException {
        try {
            byte[] bytes = batchMessageSource.getNextMessage();
            if (bytes == null) {
                return null;
            }
            return new String(bytes, charset);
        } catch (Exception e) {
            throw new BatchMessageException("Failed to read ASTM batch message", e);
        }
    }

    /**
     * Split a batch of one or more ASTM E1394 messages into individual
     * messages, using the configured split strategy.
     *
     * @param batch raw text containing one or more ASTM sessions
     * @return list of individual messages (each ending with a terminator)
     */
    public List<String> getMessages(String batch) {
        return getMessages(batch, batchProps);
    }

    /**
     * Static helper for splitting batches without an instantiated adaptor.
     *
     * @param batch raw text containing one or more ASTM sessions
     * @param props batch properties (controls split strategy)
     * @return list of individual messages
     */
    public static List<String> getMessages(String batch, ASTME1394BatchProperties props) {
        List<String> messages = new ArrayList<String>();
        if (batch == null || batch.isEmpty()) {
            return messages;
        }
        ASTME1394BatchProperties bp = (props != null) ? props : new ASTME1394BatchProperties();
        String splitType = bp.getSplitBatchBy();

        // Normalize line endings to CR.
        String normalized = batch.replace("\r\n", "\r").replace("\n", "\r");

        if (ASTME1394BatchProperties.SPLIT_TYPE_NONE.equals(splitType)) {
            messages.add(normalized);
            return messages;
        }

        if (ASTME1394BatchProperties.SPLIT_TYPE_RECORD.equals(splitType)) {
            // Each record becomes its own message.
            String[] lines = normalized.split("\r", -1);
            for (String line : lines) {
                if (!line.isEmpty()) {
                    messages.add(line + "\r");
                }
            }
            return messages;
        }

        // Default: H_L_BOUNDARY — split on H..L sessions.
        // Walk line-by-line, accumulating records until we hit an L record,
        // then emit the accumulated message.
        String[] lines = normalized.split("\r", -1);
        StringBuilder current = new StringBuilder();
        boolean inSession = false;
        Pattern recordStartPattern = Pattern.compile("^[HPORQCM].*");

        for (String line : lines) {
            if (line.isEmpty()) continue;
            if (!recordStartPattern.matcher(line).matches()) {
                // Not a record line — append to current session for context.
                if (inSession) {
                    current.append(line).append('\r');
                }
                continue;
            }

            char type = line.charAt(0);
            if (type == 'H') {
                // Starting a new session.
                if (inSession && current.length() > 0) {
                    // Previous session didn't terminate properly; flush it.
                    if (bp.isIncludeTerminator() && !current.toString().endsWith("L|1|N\r") && !current.toString().matches("(?s).*L\\|\\d+\\|[^|]*\\r$")) {
                        current.append("L|1|I\r"); // Mark incomplete
                    }
                    messages.add(current.toString());
                    current.setLength(0);
                }
                inSession = true;
                current.append(line).append('\r');
            } else if (type == 'L') {
                if (inSession) {
                    if (bp.isIncludeTerminator()) {
                        current.append(line).append('\r');
                    }
                    messages.add(current.toString());
                    current.setLength(0);
                    inSession = false;
                } else if (messages.isEmpty()) {
                    // Stray terminator with no preceding H — emit as-is.
                    if (bp.isIncludeTerminator()) {
                        messages.add(line + "\r");
                    }
                }
            } else {
                // H, P, O, R, Q, C, M record types accumulate.
                if (inSession) {
                    current.append(line).append('\r');
                } else {
                    // Records outside a session — treat as standalone.
                    if (bp.isSplitByRecord()) {
                        messages.add(line + "\r");
                    } else {
                        current.append(line).append('\r');
                    }
                }
            }
        }

        // Flush any trailing incomplete session.
        if (inSession && current.length() > 0) {
            if (bp.isIncludeTerminator() && !current.toString().matches("(?s).*L\\|\\d+\\|[^|]*\\r$")) {
                current.append("L|1|I\r");
            }
            messages.add(current.toString());
        }

        return messages;
    }

    @Override
    public void cleanup() {
        // No resources to release.
    }
}
