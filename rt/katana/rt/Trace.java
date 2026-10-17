package katana.rt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * The java lane's paragraph trace (research §27): with `KATANA_TRACE_FILE` set, each section and paragraph a
 * program enters is a line in cobc's `-ftrace` form, `Program-Id: NAME Paragraph: NAME`, so the lane reads
 * coverage from either trace alike (`oracle/lane/Paragraphs`); without it, nothing.
 */
public final class Trace {
    private static final String FILE = System.getenv("KATANA_TRACE_FILE");
    private static PrintStream out;

    private Trace() {}

    /** Control enters a section: its first paragraph begins it. */
    public static void section(String program, String section) {
        line(program, "Section", section);
    }

    /** Control enters a paragraph. */
    public static void paragraph(String program, String paragraph) {
        line(program, "Paragraph", paragraph);
    }

    private static synchronized void line(String program, String kind, String name) {
        if (FILE == null) return;
        if (out == null) {
            try {
                out = new PrintStream(new FileOutputStream(FILE, true), true, StandardCharsets.ISO_8859_1);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        out.println("Program-Id: " + program + "     " + kind + ": " + name);
    }
}
