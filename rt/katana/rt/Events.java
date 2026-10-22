package katana.rt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * The divergence report's execution counters (8A.3, research §50): with `-Dkatana.events=<file>`, each event a store
 * or a division meets is a line, `Event: KIND PROGRAM PARAGRAPH`, the paragraph the one control last entered
 * (`Trace`); without it, nothing. The kinds: a size error met (`SIZE_ERROR`), one stored without ON SIZE ERROR so
 * high-order digits are lost (`TRUNCATED`), a negative value stored into an unsigned receiver (`SIGN_DROPPED`), and
 * a quotient cut at the places Appendix A carries (`DMAX_CUT`).
 */
public final class Events {
    static final String FILE = System.getProperty("katana.events");
    static final boolean ON = FILE != null;
    private static PrintStream out;

    private Events() {}

    /** An event at the paragraph control last entered. */
    static synchronized void note(String kind) {
        if (!ON) return;
        if (out == null) {
            try {
                out = new PrintStream(new FileOutputStream(FILE, true), true, StandardCharsets.ISO_8859_1);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        out.println("Event: " + kind + " " + Trace.at);
    }

    /** A store's events: what `Decimal.store` met, and whether the receiver took the value (`stored`). */
    static void stored(Decimal.Stored s, boolean onSizeError, boolean divisionByZero) {
        if (!ON) return;
        if (s.sizeError()) note("SIZE_ERROR");
        if (s.sizeError() && !onSizeError && !divisionByZero) note("TRUNCATED");
        if (s.signDropped() && !(s.sizeError() && onSizeError) && !divisionByZero) note("SIGN_DROPPED");
    }
}
