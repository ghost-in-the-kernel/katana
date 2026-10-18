package katana.oracle.lane;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The java lane's program step (docs/ROADMAP.md 1.8, docs/LANES.md): the Java program the lane's setup
 * names for a PGM, run as a process under the same DD contract as the cobc lane: each DD a `DD_<name>`
 * environment variable holding a path, the PARM as its one argument, SYSIN as stdin, stdout as the
 * step's output, the exit code as the return code, and an abend reported as `CEE3ABD abend Unnnn` on
 * stderr.
 */
final class JavaPrograms implements ProgramRunner {
    private static final Pattern ABEND = Pattern.compile("CEE3ABD abend (U\\d{4}|with no code)");
    private final Map<String, List<String>> commands;
    private final Path work;
    private final Duration limit;
    private final boolean trace;
    private final java.util.Set<String> ran = new java.util.TreeSet<>();

    /** The setup's Java programs by PGM; with `trace`, each step's paragraphs traced into `work/trace` as cobc's are (research §27). */
    JavaPrograms(Map<String, List<String>> commands, Path work, Duration limit, boolean trace) {
        this.commands = commands;
        this.work = work;
        this.limit = limit;
        this.trace = trace;
    }

    /** The programs run so far, by PGM. */
    java.util.Set<String> ran() {
        return ran;
    }

    /** The step's environment with KATANA_TRACE_FILE naming its trace, under the work directory's trace/ (LANE-21), and KATANA_PERFORM_TRACE_FILE its PERFORM trace, under perform/ (T-F3, research §34). */
    private Map<String, String> traced(String step, Map<String, String> env) {
        Map<String, String> with = new java.util.HashMap<>(env);
        with.put("KATANA_TRACE_FILE", work.resolve("trace").resolve(step + ".trace").toString());
        with.put("KATANA_PERFORM_TRACE_FILE", work.resolve("perform").resolve(step + ".perform").toString());
        try {
            java.nio.file.Files.createDirectories(work.resolve("trace"));
            java.nio.file.Files.createDirectories(work.resolve("perform"));
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return with;
    }

    @Override
    public Programs.Ended run(String step, String name, String parm, Map<String, String> env, Path stdin) {
        List<String> command = commands.get(name);
        if (command == null) return new Programs.Ended(0, null, "no Java program for " + name + " in the java lane's setup");
        List<String> full = new ArrayList<>(command);
        if (parm != null) full.add(parm);
        ran.add(name);
        if (trace) env = traced(step, env);
        int exit = Exec.run(full, work, env, stdin, work.resolve("sysout/" + step + ".stdout"), work.resolve("sysout/" + step + ".stderr"), limit);
        if (exit == -1) return new Programs.Ended(exit, null, name + " ran past the time limit");
        Matcher abend = ABEND.matcher(Programs.text(work.resolve("sysout/" + step + ".stderr")));
        return new Programs.Ended(exit, abend.find() ? abend.group(1) : null, null);
    }
}
