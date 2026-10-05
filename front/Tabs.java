package katana.front;

import java.nio.charset.StandardCharsets;

/**
 * R-016: a tab in a source or copy line is expanded to spaces up to the next tab stop, every 8 columns
 * or the width declared for the file, before the line's columns are read. The front end and the cobc
 * lane expand the same way, so both read the same text.
 */
public final class Tabs {
    private Tabs() {}

    /** The text with every tab expanded to the next stop of this width, line by line. */
    public static byte[] expand(byte[] source, int width) {
        String text = new String(source, StandardCharsets.ISO_8859_1);
        if (text.indexOf('\t') < 0) return source;
        StringBuilder out = new StringBuilder();
        int column = 0;
        for (char c : text.toCharArray()) {
            if (c == '\t') {
                do out.append(' '); while (++column % width != 0);
            } else {
                out.append(c);
                column = c == '\n' ? 0 : column + 1;
            }
        }
        return out.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    /** Whether the bytes hold a tab. */
    public static boolean has(byte[] source) {
        for (byte b : source) if (b == '\t') return true;
        return false;
    }
}
