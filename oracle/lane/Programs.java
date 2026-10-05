package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A program step in the cobc lane: the program built from its source (a module and the PARM driver
 * when its PROCEDURE DIVISION takes USING), run with each DD bound by `DD_<name>` (GnuCOBOL's file
 * mapping), the fixture's clock and zone, the run's bin folder as the library path (STEPLIB), and an
 * abend read from the CEE3ABD stub's line, or the signal libcob reports, rather than the 8-bit exit status.
 */
final class Programs implements ProgramRunner {
    /** How a program step ended: its return code, the abend code (Unnnn) or null, and a finding that stopped it, or null. */
    record Ended(int rc, String abend, String finding) {}

    private static final Pattern USING = Pattern.compile("(?m)^.{6}[ D].*PROCEDURE\\s+DIVISION\\s+USING");
    private static final Pattern COPY = Pattern.compile("(?m)^.{6}[ D].*?\\bCOPY\\s+['\"]?([A-Z0-9#@$-]+)");
    private static final Pattern ABEND = Pattern.compile("CEE3ABD abend (U\\d{4}|with no code)|\\(signal (SIG[A-Z]+)\\)");
    private final Path sources;
    private final Compiler compiler;
    private final Path work;
    private final Duration limit;
    private final Map<String, String> sourceHashes = new HashMap<>();
    private final java.util.Set<String> tabbed = new java.util.TreeSet<>();
    private final java.util.Set<String> copied = new java.util.TreeSet<>();

    private final java.util.function.ToIntFunction<String> width;

    private final boolean trace;

    Programs(Path sources, Compiler compiler, Path work, Duration limit, java.util.function.ToIntFunction<String> width, boolean trace) {
        this.width = width;
        this.trace = trace;
        this.sources = sources;
        this.compiler = compiler;
        this.work = work;
        this.limit = limit;
    }

    /** The source of a program, `NAME.cbl` or `NAME.CBL`, or null. */
    Path source(String name) {
        for (String ext : List.of(".cbl", ".CBL")) if (Files.exists(sources.resolve(name + ext))) return sources.resolve(name + ext);
        return null;
    }

    /** Each program built so far, by name, with its source's hash: what the run is a run of. */
    Map<String, String> sourceHashes() {
        return sourceHashes;
    }

    /** The program sources that held tabs, expanded before cobc read them (R-016). */
    java.util.Set<String> tabbed() {
        return tabbed;
    }

    /** The copybooks the programs built so far name in COPY statements. */
    java.util.Set<String> copied() {
        return copied;
    }

    /** Builds a called module (`module` lines) into bin; cobc's refusal, or "". */
    String module(String name) {
        Path source = source(name);
        if (source == null) return "no source for module " + name;
        return compiler.build(name, read(source, name), true, work.resolve("bin")).refusal();
    }

    /** Builds a stub from the lane's own source into bin. */
    void stub(String name, String source) {
        compiler.build(name, source.getBytes(StandardCharsets.US_ASCII), true, work.resolve("bin"));
    }

    /** Runs a program step: env holds the clock, the zone and the DD bindings; stdin is the step's SYSIN cards or nothing. */
    @Override
    public Ended run(String step, String name, String parm, Map<String, String> env, Path stdin) {
        Path source = source(name);
        if (source == null) return new Ended(0, null, "no source for " + name);
        byte[] text = read(source, name);
        boolean using = USING.matcher(new String(text, StandardCharsets.ISO_8859_1)).find();
        Path bin = work.resolve("bin");
        Compiler.Built built = compiler.build(name, text, using, bin);
        if (!built.refusal().isEmpty()) return new Ended(0, null, "cobc refused " + name + ": " + built.refusal());
        String executable = built.file().toString();
        if (using) {
            Compiler.Built driver = compiler.build("parm-" + name, Generated.parmDriver(name, parm == null ? "" : parm).getBytes(StandardCharsets.US_ASCII), false, bin);
            if (!driver.refusal().isEmpty()) throw new IllegalStateException("the lane's PARM driver did not compile: " + driver.refusal());
            executable = driver.file().toString();
        }
        Map<String, String> all = new HashMap<>(env);
        all.put("COB_LIBRARY_PATH", bin.toString());
        if (trace) {
            all.put("COB_SET_TRACE", "Y");
            all.put("COB_TRACE_FILE", work.resolve("trace").resolve(step + ".trace").toString());
            try {
                Files.createDirectories(work.resolve("trace"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        int exit = Exec.run(List.of(executable), work, all, stdin, work.resolve("sysout/" + step + ".stdout"), work.resolve("sysout/" + step + ".stderr"), limit);
        if (exit == -1) return new Ended(exit, null, name + " ran past the time limit");
        Matcher abend = ABEND.matcher(text(work.resolve("sysout/" + step + ".stderr")));
        // A program libcob stops on a signal ended abnormally, as z/OS would end it with a system abend.
        return new Ended(exit, !abend.find() ? null : abend.group(1) != null ? abend.group(1) : abend.group(2), null);
    }

    /** A source's bytes as cobc gets them (tabs expanded, R-016), its original hash kept for the report. */
    private byte[] read(Path source, String name) {
        try {
            byte[] bytes = Files.readAllBytes(source);
            sourceHashes.put(name, katana.oracle.fixture.Content.sha256(bytes));
            if (Tabs.has(bytes)) tabbed.add(source.getFileName().toString());
            Matcher copy = COPY.matcher(new String(bytes, StandardCharsets.ISO_8859_1));
            while (copy.find()) copied.add(copy.group(1));
            return Tabs.expand(bytes, width.applyAsInt(source.getFileName().toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String text(Path file) {
        try {
            return Files.exists(file) ? Files.readString(file, StandardCharsets.ISO_8859_1) : "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
