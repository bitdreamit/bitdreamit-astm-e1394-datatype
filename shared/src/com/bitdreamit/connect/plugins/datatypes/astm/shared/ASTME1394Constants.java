package com.bitdreamit.mirth.astm.e1394.shared;

/**
 * ASTM E1394-91 Data Type Constants
 * Production-grade constants for message parsing and serialization
 */
public final class ASTME1394Constants {
    private ASTME1394Constants() {}

    // ASTM E1394 Record Types
    public static final String RECORD_HEADER      = "H"; // Header
    public static final String RECORD_PATIENT     = "P"; // Patient
    public static final String RECORD_ORDER       = "O"; // Order
    public static final String RECORD_RESULT      = "R"; // Result
    public static final String RECORD_QUERY       = "Q"; // Query
    public static final String RECORD_COMMENT     = "C"; // Comment
    public static final String RECORD_MANUFACTURER= "M"; // Manufacturer
    public static final String RECORD_TERMINATOR   = "L"; // Terminator (end of session)

    // Default Delimiters (ASTM E1394 standard)
    public static final String DEFAULT_FIELD_DELIMITER      = "|";
    public static final String DEFAULT_REPEAT_DELIMITER   = "\\";
    public static final String DEFAULT_COMPONENT_DELIMITER  = "^";
    public static final String DEFAULT_ESCAPE_CHARACTER     = "&";
    public static final String DEFAULT_RECORD_DELIMITER     = "\r";

    // Encoding
    public static final String DEFAULT_ENCODING = "UTF-8";

    // Validation
    public static final int    DEFAULT_MAX_MESSAGE_SIZE = 10240; // 10 KB
    public static final boolean DEFAULT_STRICT_VALIDATION = false;

    // Response codes
    public static final String RESPONSE_ACCEPT  = "AA";
    public static final String RESPONSE_ERROR   = "AE";
    public static final String RESPONSE_REJECT  = "AR";

    // Plugin metadata
    public static final String PLUGIN_NAME    = "ASTM E1394";
    public static final String PLUGIN_VERSION = "1.0.0";
}
