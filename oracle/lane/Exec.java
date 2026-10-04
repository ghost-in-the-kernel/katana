package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A process as the cobc lane runs one: an environment holding only what it is given and PATH (so
 * nothing of the machine leaks into a run), stdin from a file, stdout and stderr each to its own
 * file, and a time limit. The exit code, or -1 when the limit stopped it.
 */
final class Exec {
    private Exec() {}

    /** Runs a command to completion or the limit; its exit code, or -1 when the limit stopped it. */
    static int run(List<String> command, Path dir, Map<String, String> env, Path stdin, Path stdout, Path stderr, Duration limit) {
        ProcessBuilder builder = new ProcessBuilder(command).directory(dir.toFile());
        String path = builder.environment().get("PATH");
        builder.environment().clear();
        builder.environment().put("PATH", path);
        builder.environment().putAll(env);
        builder.redirectInput(stdin.toFile()).redirectOutput(stdout.toFile()).redirectError(stderr.toFile());
        try {
            Process process = builder.start();
            if (process.waitFor(limit.toMillis(), TimeUnit.MILLISECONDS)) return process.exitValue();
            process.destroyForcibly().waitFor();
            return -1;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
