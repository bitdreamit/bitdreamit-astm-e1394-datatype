package com.bitdreamit.connect.plugins.datatypes.astm.shared;

/**
 * ASTM E1394-91/97 Record-Level Data Type Constants.
 *
 * <p>Centralizes record-type identifiers, default delimiter characters, validation
 * thresholds, response codes, and plugin metadata used across the shared, server,
 * and client modules. Keeping these in one place lets downstream Mirth channels
 * reference the same values for inbound / outbound transformers, auto-responder
 * templates, and the Administrator UI settings panel.</p>
 *
 * <p>All delimiter characters are exposed as {@code String} literals so they can be
 * used both as single-character comparisons and as map keys inside the property
 * descriptors. The {@code char} accessor equivalents live on the property classes
 * themselves.</p>
 */
public final class ASTME1394Constants {

    private ASTME1394Constants() {
        // Utility class — no instances.
    }

    // -----------------------------------------------------------------
    // ASTM E1394 Record Types
    // -----------------------------------------------------------------
    /** Header record — first record of every ASTM session. */
    public static final String RECORD_HEADER       = "H";
    /** Patient record — patient demographics. */
    public static final String RECORD_PATIENT      = "P";
    /** Order record — test order / specimen. */
    public static final String RECORD_ORDER        = "O";
    /** Result record — observation / measurement. */
    public static final String RECORD_RESULT       = "R";
    /** Query record — request for information. */
    public static final String RECORD_QUERY        = "Q";
    /** Comment record — free-text annotations. */
    public static final String RECORD_COMMENT      = "C";
    /** Manufacturer record — instrument-specific information. */
    public static final String RECORD_MANUFACTURER = "M";
    /** Terminator record — final record of every ASTM session. */
    public static final String RECORD_TERMINATOR    = "L";

    // -----------------------------------------------------------------
    // Default Delimiters (ASTM E1394 standard)
    // -----------------------------------------------------------------
    public static final String DEFAULT_FIELD_DELIMITER       = "|";
    public static final String DEFAULT_REPEAT_DELIMITER      = "\\";
    public static final String DEFAULT_COMPONENT_DELIMITER  = "^";
    public static final String DEFAULT_ESCAPE_CHARACTER      = "&";
    public static final String DEFAULT_RECORD_DELIMITER      = "\r";

    /** Single-character delimiters expressed as {@code char} for fast comparison. */
    public static final char   DEFAULT_FIELD_DELIMITER_CHAR     = '|';
    public static final char   DEFAULT_REPEAT_DELIMITER_CHAR    = '\\';
    public static final char   DEFAULT_COMPONENT_DELIMITER_CHAR = '^';
    public static final char   DEFAULT_ESCAPE_CHARACTER_CHAR    = '&';
    public static final char   DEFAULT_RECORD_DELIMITER_CHAR    = '\r';

    // -----------------------------------------------------------------
    // Encoding
    // -----------------------------------------------------------------
    public static final String DEFAULT_ENCODING = "UTF-8";

    // -----------------------------------------------------------------
    // Validation Thresholds
    // -----------------------------------------------------------------
    /** 256 KB default inbound message size limit; prevents OOM DoS attacks. */
    public static final int     DEFAULT_MAX_MESSAGE_SIZE  = 262144;
    public static final boolean DEFAULT_STRICT_VALIDATION = false;

    // -----------------------------------------------------------------
    // ASTM E1381 Framing / Response Codes
    // -----------------------------------------------------------------
    /** STX — start of frame. */
    public static final char FRAME_STX = 0x02;
    /** ETX — end of frame (final). */
    public static final char FRAME_ETX = 0x03;
    /** ETB — end of frame (intermediate, more frames follow). */
    public static final char FRAME_ETB = 0x17;
    /** EOT — end of transmission. */
    public static final char FRAME_EOT = 0x04;
    /** ENQ — enquiry (request to send). */
    public static final char FRAME_ENQ = 0x05;
    /** ACK — positive acknowledgment. */
    public static final char FRAME_ACK = 0x06;
    /** NAK — negative acknowledgment. */
    public static final char FRAME_NAK = 0x15;

    /** ASTM E1381 acceptance response code. */
    public static final String RESPONSE_ACCEPT  = "AA";
    /** ASTM E1381 application-error response code. */
    public static final String RESPONSE_ERROR   = "AE";
    /** ASTM E1381 reject response code. */
    public static final String RESPONSE_REJECT  = "AR";

    // -----------------------------------------------------------------
    // Plugin Metadata
    // -----------------------------------------------------------------
    public static final String PLUGIN_NAME    = "ASTM E1394";
    public static final String PLUGIN_VERSION = "1.2.0";

    /** Root XML element produced by {@code ASTME1394Deserializer#toXML(String)}. */
    public static final String XML_ROOT_ELEMENT = "ASTM";
}
