package com.bitdreamit.connect.plugins.datatypes.astm.server;

/**
 * ASTM E1394 escape-sequence handling utility.
 *
 * <p>Per ASTM E1394-91 §6 (and reaffirmed in E1394-97), an escape character
 * (default {@code &}) introduces two-character escape sequences that let
 * literal delimiter characters appear inside field values:</p>
 *
 * <table border="1">
 *   <tr><th>Sequence</th><th>Literal</th></tr>
 *   <tr><td>{@code &F}</td><td>Field delimiter (default {@code |})</td></tr>
 *   <tr><td>{@code &S}</td><td>Component delimiter (default {@code ^})</td></tr>
 *   <tr><td>{@code &R}</td><td>Repeat delimiter (default {@code \})</td></tr>
 *   <tr><td>{@code &E}</td><td>Escape character itself (default {@code &})</td></tr>
 *   <tr><td>{@code &Hxx}</td><td>Hex byte value (2 hex digits, e.g. {@code &H0A} = LF)</td></tr>
 * </table>
 *
 * <p>This utility is stateless and thread-safe provided each caller constructs
 * a new instance with the delimiters derived for the current message. Instances
 * are cheap to allocate; consider them transient.</p>
 */
public class ASTME1394EscapeUtil {

    private final char escapeChar;
    private final char fieldDelimiter;
    private final char repeatDelimiter;
    private final char componentDelimiter;

    public ASTME1394EscapeUtil(char escapeChar, char fieldDelimiter, char repeatDelimiter, char componentDelimiter) {
        this.escapeChar         = escapeChar;
        this.fieldDelimiter     = fieldDelimiter;
        this.repeatDelimiter    = repeatDelimiter;
        this.componentDelimiter = componentDelimiter;
    }

    /**
     * Replace literal escape sequences in {@code value} with their corresponding
     * characters. Returns the input unchanged if no escape character is present.
     */
    public String unescape(String value) {
        if (value == null || value.isEmpty() || value.indexOf(escapeChar) < 0) {
            return value;
        }
        StringBuilder out = new StringBuilder(value.length());
        int i = 0;
        int n = value.length();
        while (i < n) {
            char c = value.charAt(i);
            if (c == escapeChar && i + 1 < n) {
                char next = value.charAt(i + 1);
                switch (next) {
                    case 'E':
                        out.append(escapeChar);
                        i += 2;
                        continue;
                    case 'F':
                        out.append(fieldDelimiter);
                        i += 2;
                        continue;
                    case 'S':
                        out.append(componentDelimiter);
                        i += 2;
                        continue;
                    case 'R':
                        out.append(repeatDelimiter);
                        i += 2;
                        continue;
                    case 'H':
                        // Hex byte escape: &Hxx — exactly 2 hex digits.
                        if (i + 3 < n) {
                            String hex = value.substring(i + 2, i + 4);
                            try {
                                int code = Integer.parseInt(hex, 16);
                                if (code >= 0x00 && code <= 0xFF) {
                                    out.append((char) code);
                                    i += 4;
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                // fall through — treat as literal '&H'
                            }
                        }
                        break;
                    default:
                        // Unknown escape — preserve both characters literally.
                        break;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    /**
     * Escape literal delimiter / escape characters in {@code value} using the
     * inverse of {@link #unescape(String)}. Non-printable control characters
     * (outside ASCII 0x20–0x7E) are escaped as {@code &Hxx}.
     */
    public String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() * 2);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == escapeChar) {
                out.append(escapeChar).append('E');
            } else if (c == fieldDelimiter) {
                out.append(escapeChar).append('F');
            } else if (c == componentDelimiter) {
                out.append(escapeChar).append('S');
            } else if (c == repeatDelimiter) {
                out.append(escapeChar).append('R');
            } else if (c < 0x20 || c > 0x7E) {
                out.append(escapeChar).append('H').append(String.format("%02X", (int) c & 0xFF));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
