package com.bitdreamit.connect.plugins.datatypes.astm.shared;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.message.DebuggableBatchAdaptor;

/**
 * ASTM E1394 batch adaptor.
 *
 * <p>Reads raw bytes from the batch framework and emits individual ASTM E1394
 * messages split on {@code H…L} boundaries. The split strategy is configurable
 * via {@link ASTME1394BatchProperties#getSplitBatchBy()}:</p>
 *
 * <ul>
 *   <li>{@code H_L_BOUNDARY} (default) — each {@code H…L} session becomes one
 *       message.</li>
 *   <li>{@code RECORD} — every record becomes its own message.</li>
 *   <li>{@code NONE} — the entire batch is passed through as a single message.</li>
 * </ul>
 *
 * <p><b>API note (Mirth 4.5.x):</b> The {@link DebuggableBatchAdaptor} contract
 * requires implementing {@code protected String getNextMessage(int i)} — the
 * {@code int} parameter is a 0-based counter incremented by the framework on
 * each call. The {@code batchMessageSource} field on the parent class is
 * accessed via reflection to remain source-compatible across Mirth 4.x
 * micro versions.</p>
 */
public class ASTME1394BatchAdaptor extends DebuggableBatchAdaptor {

    private static final Logger logger = Logger.getLogger(ASTME1394BatchAdaptor.class);

    private final SerializerProperties properties;
    private final ASTME1394BatchProperties batchProps;
    private final Charset charset;

    /** The BatchRawMessage supplied to the constructor (may be null). */
    private final BatchRawMessage batchRawMessage;

    /**
     * Cached list of split messages. Populated lazily on the first call to
     * {@link #getNextMessage(int)} and then served by index.
     */
    private List<String> splitMessages;

    /**
     * Production constructor — invoked by
     * {@link ASTME1394BatchAdaptorFactory#createBatchAdaptor(BatchRawMessage)}.
     *
     * @param factory          the owning factory
     * @param sourceConnector  the channel's source connector
     * @param batchRawMessage  the batch raw message (wraps the raw message text)
     * @param properties       the serializer properties for the channel
     */
    public ASTME1394BatchAdaptor(BatchAdaptorFactory factory,
                                  SourceConnector sourceConnector,
                                  BatchRawMessage batchRawMessage,
                                  SerializerProperties properties) {
        super(factory, sourceConnector, batchRawMessage);
        this.batchRawMessage = batchRawMessage;
        this.properties = properties;
        ASTME1394BatchProperties bp = null;
        if (properties != null && properties.getBatchProperties() instanceof ASTME1394BatchProperties) {
            bp = (ASTME1394BatchProperties) properties.getBatchProperties();
        }
        this.batchProps = (bp != null) ? bp : new ASTME1394BatchProperties();

        Charset cs;
        try {
            String enc = ASTME1394Constants.DEFAULT_ENCODING;
            if (properties != null
                    && properties.getSerializationProperties() instanceof ASTME1394SerializationProperties) {
                enc = ((ASTME1394SerializationProperties) properties.getSerializationProperties()).getEncoding();
            }
            cs = Charset.forName(enc);
        } catch (Exception e) {
            cs = Charset.forName(ASTME1394Constants.DEFAULT_ENCODING);
        }
        this.charset = cs;
    }

    /** Convenience constructor for unit tests that pass batch props directly. */
    public ASTME1394BatchAdaptor(ASTME1394BatchProperties batchProps) {
        super(null, null, null);
        this.batchRawMessage = null;
        this.properties  = null;
        this.batchProps = (batchProps != null) ? batchProps : new ASTME1394BatchProperties();
        this.charset    = Charset.forName(ASTME1394Constants.DEFAULT_ENCODING);
    }

    // ------------------------------------------------------------------
    // BatchAdaptor abstract-method implementation (Mirth 4.5.x contract)
    // ------------------------------------------------------------------

    /**
     * Return the i-th individual message from the batch, or {@code null} when
     * the batch is exhausted. The framework calls this with incrementing
     * {@code i} (0, 1, 2, …) until {@code null} is returned.
     *
     * <p>On the first call the entire batch is split using the configured
     * strategy and cached; subsequent calls serve from the cache.</p>
     *
     * @param i 0-based message index
     * @return the message string, or {@code null} if no more messages
     */
    @Override
    protected String getNextMessage(int i) throws Exception {
        // Lazy-initialize the split-messages cache.
        if (splitMessages == null) {
            splitMessages = splitBatch();
        }

        if (i >= 0 && i < splitMessages.size()) {
            return splitMessages.get(i);
        }
        return null;
    }

    /**
     * Split the entire batch into individual messages. Tries multiple sources:
     * <ol>
     *   <li>The {@code batchRawMessage} supplied to the constructor.</li>
     *   <li>The parent's {@code batchMessageSource} field (via reflection).</li>
     * </ol>
     */
    private List<String> splitBatch() {
        List<String> messages = new ArrayList<String>();

        // 1) Try the constructor's batchRawMessage.
        String raw = extractRawMessageString(batchRawMessage);
        if (raw != null && !raw.isEmpty()) {
            messages = getMessages(raw, batchProps);
            if (!messages.isEmpty()) {
                return messages;
            }
        }

        // 2) Try the parent's batchMessageSource field.
        Object source = readParentField("batchMessageSource");
        if (source != null) {
            String chunk = readAllFromSource(source);
            if (chunk != null && !chunk.isEmpty()) {
                messages = getMessages(chunk, batchProps);
                if (!messages.isEmpty()) {
                    return messages;
                }
            }
        }

        // 3) Try the parent's batchRawMessage field (alternate naming).
        Object brmField = readParentField("batchRawMessage");
        if (brmField instanceof BatchRawMessage) {
            raw = extractRawMessageString((BatchRawMessage) brmField);
            if (raw != null && !raw.isEmpty()) {
                messages = getMessages(raw, batchProps);
            }
        } else if (brmField instanceof String) {
            messages = getMessages((String) brmField, batchProps);
        }

        return messages;
    }

    /**
     * Extract the raw message string from a {@link BatchRawMessage} using
     * reflection (the method name varies across Mirth versions:
     * {@code getRawMessage()}, {@code getMessage()}, etc.).
     */
    private String extractRawMessageString(BatchRawMessage brm) {
        if (brm == null) return null;
        for (String methodName : new String[] { "getRawMessage", "getMessage", "getText" }) {
            try {
                java.lang.reflect.Method m = BatchRawMessage.class.getMethod(methodName);
                Object result = m.invoke(brm);
                if (result instanceof String) {
                    return (String) result;
                }
                if (result instanceof byte[]) {
                    return new String((byte[]) result, charset);
                }
            } catch (NoSuchMethodException e) {
                // try next method name
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error calling " + methodName + " on BatchRawMessage: " + e.getMessage());
                }
            }
        }
        return brm.toString();
    }

    /**
     * Read all available data from a {@link BatchMessageSource} (or similar)
     * object using reflection. The method name to fetch the next chunk varies
     * across Mirth versions, so we try a list of candidates.
     */
    private String readAllFromSource(Object source) {
        StringBuilder sb = new StringBuilder();
        for (String methodName : new String[] { "getNextMessage", "read", "next", "poll", "take", "getMessage" }) {
            try {
                java.lang.reflect.Method m = source.getClass().getMethod(methodName);
                // Read up to a reasonable limit to avoid infinite loops.
                int safety = 10000;
                while (safety-- > 0) {
                    Object result;
                    try {
                        result = m.invoke(source);
                    } catch (Exception e) {
                        break;
                    }
                    if (result == null) break;
                    if (result instanceof byte[]) {
                        sb.append(new String((byte[]) result, charset));
                    } else if (result instanceof String) {
                        sb.append((String) result);
                    } else {
                        sb.append(result.toString());
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            } catch (NoSuchMethodException e) {
                // try next method name
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error calling " + methodName + " on source: " + e.getMessage());
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Read a protected/private field from the parent class using reflection.
     * Returns {@code null} if the field doesn't exist or is inaccessible.
     */
    private Object readParentField(String fieldName) {
        try {
            java.lang.reflect.Field f = DebuggableBatchAdaptor.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(this);
        } catch (NoSuchFieldException e) {
            // field not present in this Mirth version
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error reading field " + fieldName + ": " + e.getMessage());
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Batch-splitting logic (test-friendly static methods)
    // ------------------------------------------------------------------

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
        String[] lines = normalized.split("\r", -1);
        StringBuilder current = new StringBuilder();
        boolean inSession = false;
        Pattern recordStartPattern = Pattern.compile("^[HPORQCM].*");

        for (String line : lines) {
            if (line.isEmpty()) continue;
            if (!recordStartPattern.matcher(line).matches()) {
                if (inSession) {
                    current.append(line).append('\r');
                }
                continue;
            }

            char type = line.charAt(0);
            if (type == 'H') {
                if (inSession && current.length() > 0) {
                    if (bp.isIncludeTerminator() && !current.toString().endsWith("L|1|N\r") && !current.toString().matches("(?s).*L\\|\\d+\\|[^|]*\\r$")) {
                        current.append("L|1|I\r");
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
                    if (bp.isIncludeTerminator()) {
                        messages.add(line + "\r");
                    }
                }
            } else {
                if (inSession) {
                    current.append(line).append('\r');
                } else {
                    if (bp.isSplitByRecord()) {
                        messages.add(line + "\r");
                    } else {
                        current.append(line).append('\r');
                    }
                }
            }
        }

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
