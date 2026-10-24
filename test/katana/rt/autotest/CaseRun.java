package katana.rt.autotest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One autotest case run the way its expected check runs it (TODO 3.1, J-278): a process of its own (so a case that loops is killed, not abandoned), in
 * a copy of the case's folder (so what the program writes is not in the repository), with the check's environment variables, arguments, input file
 * and pipe as `CaseCommand` read them. The program is Katana's lowering compiled against `rt/`, its `main` as the generated class writes it; the
 * check's text is never run, and no cobc stands in. Two names of the testsuite's command are put in Katana's own terms and nothing else is
 * translated: `COB_CURRENT_DATE`, from cobc's `YYYY/MM/DD HH:MM:SS` to the lane's form (`Now`, LANE-03), and a true `COB_SET_DEBUG` to `-Dkatana.debug=Y` (LANE-13).
 */
final class CaseRun {
    private CaseRun() {}

    /** The longest a case may run. */
    static final int SECONDS = 20;
    private static final Pattern COBC_DATE = Pattern.compile("(\\d{4})/(\\d\\d)/(\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)");

    /** What a run wrote to stdout, and why it did not end normally (null when it did): a timeout, an uncaught exception or an abend (a return code is not a failure). */
    record Result(byte[] stdout, String failure) {}

    /**
     * The class `main` run in `work`, a fresh copy of `source` (null for an empty folder), on `classpath`, with `command` (an unread command gives the run nothing).
     * Stops the process after `seconds`; keeps the first `command.head()` lines of stdout, stopping the process then, as `head` does when it closes the pipe.
     */
    static Result run(Path source, Path work, String classpath, String main, CaseCommand command, int seconds) throws IOException, InterruptedException {
        delete(work);
        Files.createDirectories(work);
        if (source != null) copy(source, work);
        List<String> line = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "-Xshare:auto"));
        Map<String, String> env = environment(command);
        if (debug(env.get("COB_SET_DEBUG"))) line.add("-Dkatana.debug=Y");
        line.addAll(List.of("-cp", classpath, main));
        line.addAll(command.args());
        Path err = work.resolveSibling(work.getFileName() + ".err");
        ProcessBuilder pb = new ProcessBuilder(line).directory(work.toFile()).redirectError(err.toFile());
        pb.redirectInput(command.stdin() == null ? new File("/dev/null") : work.resolve(command.stdin()).toFile());
        pb.environment().clear();
        if (System.getenv("PATH") != null) pb.environment().put("PATH", System.getenv("PATH"));
        pb.environment().putAll(env);
        Process process = pb.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> read(process, out, command.head()), "case-stdout");
        reader.setDaemon(true);
        reader.start();
        boolean ended = process.waitFor(seconds, TimeUnit.SECONDS);
        if (!ended) process.destroyForcibly().waitFor();
        reader.join(5000);
        String failure = !ended ? "no end in " + seconds + " s, the process killed" : stderrFailure(err);
        synchronized (out) {
            return new Result(out.toByteArray(), failure);
        }
    }

    /** The stdout of the process, up to the nth line when a pipe keeps only those; the process is killed when they are written. */
    private static void read(Process process, ByteArrayOutputStream out, int head) {
        byte[] buffer = new byte[8192];
        int lines = 0;
        try (InputStream in = process.getInputStream()) {
            for (int n; (n = in.read(buffer)) > 0; ) {
                int keep = n;
                if (head >= 0) {
                    for (int k = 0; k < n && keep == n; k++) if (buffer[k] == '\n' && ++lines == head) keep = k + 1;
                }
                synchronized (out) {
                    out.write(buffer, 0, keep);
                }
                if (head >= 0 && lines >= head) {
                    process.destroyForcibly();
                    return;
                }
            }
        } catch (IOException closed) {
            // the process was killed under the reader
        }
    }

    /** An uncaught exception or an abend, as the process's stderr says it; null for any other end. */
    private static String stderrFailure(Path err) throws IOException {
        for (String l : Files.readAllLines(err, java.nio.charset.StandardCharsets.ISO_8859_1)) {
            if (l.startsWith("Exception in thread") || l.startsWith("CEE3ABD abend")) return l;
        }
        return null;
    }

    /** The check's variables, `COB_CURRENT_DATE` in the lane's form. */
    static Map<String, String> environment(CaseCommand command) {
        Map<String, String> env = new LinkedHashMap<>(command.env());
        Matcher m = COBC_DATE.matcher(env.getOrDefault("COB_CURRENT_DATE", ""));
        if (m.matches()) env.put("COB_CURRENT_DATE", m.group(1) + "-" + m.group(2) + "-" + m.group(3) + " " + m.group(4) + ":" + m.group(5) + ":" + m.group(6) + ".00 +00:00");
        return env;
    }

    /** Whether `COB_SET_DEBUG` is on: `1` or `Y`. */
    static boolean debug(String value) {
        return value != null && (value.equals("1") || value.equalsIgnoreCase("Y"));
    }

    /** The case's folder into `work`, but for the testsuite's own check files and the extracted notes (R-045: the case's program and data, nothing of the checks). */
    private static void copy(Path from, Path to) throws IOException {
        try (var files = Files.walk(from)) {
            for (Path p : (Iterable<Path>) files::iterator) {
                Path rel = from.relativize(p);
                String name = rel.toString();
                if (name.isEmpty() || name.matches("check-\\d+\\.(sh|stdout|stderr)") || name.equals("case.txt") || name.equals("flags.txt")) continue;
                if (Files.isDirectory(p)) Files.createDirectories(to.resolve(rel));
                else Files.copy(p, to.resolve(rel), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void delete(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var files = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) files.sorted(java.util.Comparator.reverseOrder())::iterator) Files.delete(p);
        }
    }
}
