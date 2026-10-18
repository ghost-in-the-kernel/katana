package katana.rt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The java lane's PERFORM trace (T-F3, research §34): with `KATANA_PERFORM_TRACE_FILE` set, each paragraph
 * entered, each PERFORM, each return from one, each PERFORM dropped by an outer return, each fall from a
 * paragraph into the next and each EXIT PROGRAM or GOBACK is a line, paragraphs by the IR's index, the
 * program by its PROGRAM-ID; without it, nothing. A file of its own: the paragraph trace is held to cobc's.
 */
public final class PerformTrace {
    private static final String FILE = System.getenv("KATANA_PERFORM_TRACE_FILE");
    static final boolean ON = FILE != null;
    private static final Map<Class<?>, String> PROGRAMS = new ConcurrentHashMap<>();
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static PrintStream out;

    private PerformTrace() {}

    /** Control enters a named paragraph of `program`: the line, and the program's class known by its name. */
    static void entered(String program, String paragraph) {
        if (!ON) return;
        PROGRAMS.putIfAbsent(caller(), program);
        line("Enter: " + program + " " + paragraph);
    }

    static void perform(int first, int last) {
        if (ON) line("Perform: " + program() + " " + first + " " + last);
    }

    static void returned(int paragraph, int dropped) {
        if (!ON) return;
        String program = program();
        if (dropped > 0) line("Dropped: " + program + " " + dropped);
        line("Return: " + program + " " + paragraph);
    }

    static void fell(int paragraph, int next) {
        if (ON) line("Fall: " + program() + " " + paragraph + " " + next);
    }

    static void exited(int dropped) {
        if (ON) line("Exit: " + program() + " " + dropped);
    }

    /** The lowered program's class: the first frame not in the runtime. */
    private static Class<?> caller() {
        return WALKER.walk(s -> s.map(StackWalker.StackFrame::getDeclaringClass).filter(c -> !c.getName().startsWith("katana.rt.")).findFirst().orElse(PerformTrace.class));
    }

    private static String program() {
        Class<?> c = caller();
        return PROGRAMS.getOrDefault(c, c.getSimpleName());
    }

    private static synchronized void line(String text) {
        if (out == null) {
            try {
                out = new PrintStream(new FileOutputStream(FILE, true), true, StandardCharsets.ISO_8859_1);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        out.println(text);
    }
}
