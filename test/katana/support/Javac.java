package katana.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.ToolProvider;

/**
 * Lowered Java compiled in a test, against the test's own classpath, into `dir`; a failure shows javac's
 * diagnostics. JavaRunner and LoweredAutotestsIntegrationTest compile against `rt/` alone, and keep their own.
 */
public final class Javac {
    private Javac() {}

    /** Compiles `sources` into `dir`, failing with `what` and the diagnostics. */
    public static void compile(String what, Path dir, Path... sources) {
        List<String> args = new ArrayList<>(List.of("-classpath", System.getProperty("java.class.path"), "-d", dir.toString()));
        for (Path s : sources) args.add(s.toString());
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        int status = ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, args.toArray(String[]::new));
        assertEquals(0, status, what + " compiles: " + diagnostics.toString(StandardCharsets.UTF_8));
    }
}
