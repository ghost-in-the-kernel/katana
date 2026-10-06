package katana.lower;

import katana.sir.types.Origin;

/**
 * The one place Java text is written (R-024): lines at an indent, and each statement's provenance
 * comment (`// PROG.cbl:1240 2000-COMPUTE-INTEREST COMPUTE`), so no pass can lose where a line came from.
 */
final class Emitter {
    private final StringBuilder out = new StringBuilder();
    private int indent;
    private int statements;

    void line(String text) {
        out.append("    ".repeat(indent)).append(text).append('\n');
    }

    void open(String text) {
        line(text + " {");
        indent++;
    }

    void close() {
        indent--;
        line("}");
    }

    /** The comment a statement's Java follows. */
    // Decision: R-024
    void provenance(Origin origin, String paragraph, String verb) {
        statements++;
        String file = origin.file().substring(origin.file().lastIndexOf('/') + 1);
        line("// " + file + ":" + origin.line() + " " + (paragraph.isEmpty() ? "(unnamed)" : paragraph) + " " + verb);
    }

    /** How many provenance comments were written: one a statement. */
    int statements() {
        return statements;
    }

    String text() {
        return out.toString();
    }

    /** A Java string literal of `s`, every character outside printable ASCII escaped. */
    static String quote(String s) {
        StringBuilder q = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') q.append('\\').append(c);
            else if (c < 0x20 || c > 0x7E) q.append(String.format("\\u%04x", (int) c));
            else q.append(c);
        }
        return q.append('"').toString();
    }
}
