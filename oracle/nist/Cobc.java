package katana.oracle.nist;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * GnuCOBOL's `cobc` and the programs it builds, run as processes (R-003): the oracle uses the binary's
 * behaviour and output, never its source (R-045). Every run has a time limit, so a program that loops
 * is a crash, not a hung build.
 */
// Unsure: this is the start of the cobc lane (ROADMAP 1.4) and may move there when the lane is built.
public final class Cobc {
    /** How a process ended: its exit code (-1 when it was stopped at the time limit) and what it printed. */
    public record Result(int exit, String output) {
        public boolean ok() {
            return exit == 0;
        }
    }

    private final Duration limit;

    public Cobc(Duration limit) {
        this.limit = limit;
    }

    /** Compiles a source in a folder into an executable (`-x`) named after it, under COBOL 85. */
    public Result compile(Path dir, String source, List<String> flags) {
        return cobc(dir, source, "-x", flags);
    }

    /** Compiles a source in a folder into a module another program CALLs (`-m`), under COBOL 85. */
    public Result compileModule(Path dir, String source, List<String> flags) {
        return cobc(dir, source, "-m", flags);
    }

    private Result cobc(Path dir, String source, String kind, List<String> flags) {
        List<String> command = new ArrayList<>(List.of("cobc", "-std=cobol85", kind));
        command.addAll(flags);
        command.add(source);
        return run(dir, command, Map.of());
    }

    /** Runs an executable built in a folder, with these environment variables added. */
    public Result execute(Path dir, String program, Map<String, String> env) {
        return run(dir, List.of("./" + program), env);
    }

    private Result run(Path dir, List<String> command, Map<String, String> env) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command).directory(dir.toFile()).redirectErrorStream(true);
            builder.environment().putAll(env);
            Process process = builder.start();
            process.getOutputStream().close();
            byte[][] output = new byte[1][];
            Thread reader = Thread.ofVirtual().start(() -> {
                try {
                    output[0] = process.getInputStream().readAllBytes();
                } catch (IOException e) {
                    output[0] = new byte[0];
                }
            });
            boolean finished = process.waitFor(limit.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) process.destroyForcibly().waitFor();
            reader.join();
            return new Result(finished ? process.exitValue() : -1, new String(output[0], StandardCharsets.ISO_8859_1));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
