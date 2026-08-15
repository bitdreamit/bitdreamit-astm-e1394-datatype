package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
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
 *
 * <p><b>API note:</b> In Mirth Connect 4.x the {@link BatchAdaptor} contract
 * changed:</p>
 * <ul>
 *   <li>The constructor now takes
 *       {@code (BatchAdaptorFactory, SourceConnector, BatchRawMessage)}.</li>
 *   <li>The framework calls {@link #getNextMessage(Integer)} (returning a
 *       {@link RawMessage}) instead of the legacy {@code getMessage()}
 *       method.</li>
 *   <li>The {@code batchMessageSource} field is exposed by the parent as a
 *       protected member, initialized from
 *       {@link BatchRawMessage#getMessageSource()}.</li>
 * </ul>
 */
public class ASTME1394BatchAdaptor extends BatchAdaptor {

    private static final Logger logger = Logger.getLogger(ASTME1394BatchAdaptor.class);

    private final SerializerProperties properties;
    private final ASTME1394BatchProperties batchProps;
    private final Charset charset;

    /**
     * Buffer of pending messages extracted from the current batch read. The
     * Mirth batch framework calls {@link #getNextMessage(Integer)} one
     * message at a time, but the batch source may return a chunk containing
     * multiple complete ASTM sessions; we cache them here between calls.
     */
    private final List<String> pendingMessages = new ArrayList<String>();

    /**
     * Production constructor — invoked by
     * {@link ASTME1394BatchAdaptorFactory#createBatchAdaptor(BatchRawMessage)}.
     *
     * @param factory          the owning factory
     * @param sourceConnector  the channel's source connector
     * @param batchRawMessage  the batch raw message (wraps the message source)
     * @param properties       the serializer properties for the channel
     */
    public ASTME1394BatchAdaptor(BatchAdaptorFactory factory,
                                  SourceConnector sourceConnector,
                                  BatchRawMessage batchRawMessage,
                                  SerializerProperties properties) {
        super(factory, sourceConnector, batchRawMessage);
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
        super(null, null, (BatchRawMessage) null);
        this.properties  = null;
        this.batchProps = (batchProps != null) ? batchProps : new ASTME1394BatchProperties();
        this.charset    = Charset.forName(ASTME1394Constants.DEFAULT_ENCODING);
    }

    /**
     * Mirth Connect 4.x batch framework entry point. Returns the next
     * individual message (split per the configured strategy) wrapped in a
     * {@link RawMessage}, or {@code null} when the batch is exhausted.
     *
     * @param partitionId the channel partition id (ignored — ASTM E1394
     *                    messages are not partitioned)
     * @return the next {@link RawMessage}, or {@code null} if no more
     *         messages are available
     * @throws BatchMessageException if the underlying batch source fails
     */
    @Override
    public RawMessage getNextMessage(Integer partitionId) throws BatchMessageException {
        String message = getMessage();
        if (message == null) {
            return null;
        }
        try {
            return newRawMessage(message);
        } catch (Exception e) {
            throw new BatchMessageException("Failed to wrap ASTM message as RawMessage", e);
        }
    }

    /**
     * Construct a {@link RawMessage} from a plain message string. The
     * {@code RawMessage(String)} constructor has been part of the Mirth
     * Connect API since 3.x and remains in 4.x; the small reflective
     * fallback handles any future constructor signature change without
     * breaking the build.
     */
    private RawMessage newRawMessage(String message) throws Exception {
        try {
            return new RawMessage(message);
        } catch (Throwable t) {
            // Fallback: try the (String, Map, Long) constructor.
            try {
                java.lang.reflect.Constructor<RawMessage> c =
                        RawMessage.class.getConstructor(String.class, java.util.Map.class, Long.class);
                return c.newInstance(message, null, null);
            } catch (NoSuchMethodException nsme) {
                // Re-throw the original error.
                if (t instanceof Exception) throw (Exception) t;
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Read the next individual message from the batch source. Splits
     * multi-session chunks using the configured strategy and caches any
     * surplus messages for subsequent calls.
     *
     * @return the next message string, or {@code null} when the batch is
     *         exhausted
     * @throws BatchMessageException on read failure
     */
    @Override
    public String getMessage() throws BatchMessageException {
        // Serve any cached message first.
        if (!pendingMessages.isEmpty()) {
            return pendingMessages.remove(0);
        }
        try {
            BatchMessageSource source = resolveBatchMessageSource();
            if (source == null) {
                return null;
            }
            byte[] bytes = source.getNextMessage();
            if (bytes == null) {
                return null;
            }
            String chunk = new String(bytes, charset);

            // Split the chunk into individual messages per the configured
            // strategy; cache all but the first.
            List<String> messages = getMessages(chunk, batchProps);
            if (messages.isEmpty()) {
                return null;
            }
            for (int i = 1; i < messages.size(); i++) {
                pendingMessages.add(messages.get(i));
            }
            return messages.get(0);
        } catch (BatchMessageException e) {
            throw e;
        } catch (Exception e) {
            throw new BatchMessageException("Failed to read ASTM batch message", e);
        }
    }

    @Override
    protected String getNextMessage(int i) throws Exception {
        return "";
    }

    /**
     * Resolve the parent's {@link BatchMessageSource} without depending on a
     * specific Mirth 4.x micro-version's API:
     * <ol>
     *   <li>Try the public {@code getBatchMessageSource()} getter if it
     *       exists at runtime.</li>
     *   <li>Otherwise fall back to reflective field access on either
     *       {@code batchMessageSource} or {@code batchRawMessage}.</li>
     * </ol>
     * This keeps the plugin source-compatible across Mirth 4.0 — 4.5+
     * without forcing the user to upgrade their mirth-server.jar.
     */
    private BatchMessageSource resolveBatchMessageSource() {
        // 1) Try the public getter (Mirth 4.2+).
        try {
            java.lang.reflect.Method m = BatchAdaptor.class.getMethod("getBatchMessageSource");
            Object value = m.invoke(this);
            if (value instanceof BatchMessageSource) {
                return (BatchMessageSource) value;
            }
        } catch (NoSuchMethodException e) {
            // getter not present — fall through to field access
        } catch (Exception e) {
            // unexpected — fall through to field access
        }

        // 2) Reflective field access.
        for (String fieldName : new String[] { "batchMessageSource", "batchRawMessage" }) {
            try {
                java.lang.reflect.Field f = BatchAdaptor.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                Object value = f.get(this);
                if (value instanceof BatchMessageSource) {
                    return (BatchMessageSource) value;
                }
                if (value instanceof BatchRawMessage) {
                    return ((BatchRawMessage) value).getMessageSource();
                }
            } catch (NoSuchFieldException e) {
                // try next field name
            } catch (IllegalAccessException e) {
                // should not happen — we just setAccessible(true)
            }
        }
        return null;
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
