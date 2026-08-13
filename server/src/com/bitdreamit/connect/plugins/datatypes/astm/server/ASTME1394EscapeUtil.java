package com.bitdreamit.connect.plugins.datatypes.astm.server;

public class ASTME1394EscapeUtil {

    private final char escapeChar;
    private final char fieldDelimiter;
    private final char repeatDelimiter;
    private final char componentDelimiter;

    public ASTME1394EscapeUtil(char escapeChar, char fieldDelimiter, char repeatDelimiter, char componentDelimiter) {
        this.escapeChar = escapeChar;
        this.fieldDelimiter = fieldDelimiter;
        this.repeatDelimiter = repeatDelimiter;
        this.componentDelimiter = componentDelimiter;
    }

    public String unescape(String value) {
        if (value == null || value.indexOf(escapeChar) < 0) return value;
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == escapeChar && i + 1 < value.length()) {
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
                        if (i + 3 < value.length()) {
                            String hex = value.substring(i + 2, i + 4);
                            try {
                                out.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                                continue;
                            } catch (NumberFormatException e) {
                                // fall through
                            }
                        }
                        break;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    public String escape(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder();
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
                out.append(escapeChar).append('H').append(String.format("%02X", (int) c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
