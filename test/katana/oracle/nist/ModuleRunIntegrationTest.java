package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import katana.oracle.Runs;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Every NIST module run under cobc, each run held to its golden (R-047): under `-std=cobol85`
 * oracle/nist/programs.golden (1.1), under `-std=ibm` oracle/nist/programs-ibm.golden (1.2).
 * `-Dkatana.std=ibm` runs one dialect only (CI runs each in its own step); `-Dkatana.nist.modules=NC,IF`
 * runs those modules only, held to their lines of the goldens (the pre-push check's quick NIST, tithe
 * pass 2, C2); `-Dkatana.record=true` rewrites the goldens from a full run, for review in their diff.
 * EXEC85 itself is tooling and is always compiled under COBOL 85.
 */
@Tag("integration")
class ModuleRunIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final List<String> ALL = List.of("NC", "SM", "IC", "SQ", "RL", "IX", "ST", "SG", "OB", "IF", "RW", "DB");
    private static final String ONLY = System.getProperty("katana.nist.modules", "");
    /** Where NC is cut in two: its main programs before this one run in one folder, the rest in another. */
    private static final int NC_HALF = 50;
    private static final List<String> MODULES = ONLY.isBlank() ? ALL : List.of(ONLY.split(","));
    private static final String HEADER = """
            # Katana's NIST golden (R-047): each run of each program, as its own footer reports it under Katana's
            # profile (cobc 3.1.2, -std=cobol85, oracle/nist/exec85.cards and run-notes). A line is: module, program,
            # run, how it ended (ok: ran to an end; compile: cobc refused it; timeout: stopped at the limit), then
            # tests in all, passed, failed, deleted, needing inspection; a run that ended abnormally adds its exit status
            # and, when libcob names one, the reason (LANE-08). Written by `gradle integration -Dkatana.record=true`.
            """;

    /**
     * Each dialect at once, in folders of its own: run one after the other, each ended on NC alone, and the
     * class, last of the long ones to start, held the pre-push check past its ceiling (R-054, J-206).
     */
    @Test
    void everyRunIsTheGoldens() throws Exception {
        List<Future<?>> dialects = new ArrayList<>();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String dialect : System.getProperty("katana.std", "cobol85,ibm").split(",")) dialects.add(pool.submit(() -> {
                everyRunIsTheGolden(dialect);
                return null;
            }));
            for (Future<?> d : dialects) {
                try {
                    d.get();
                } catch (java.util.concurrent.ExecutionException e) {
                    if (e.getCause() instanceof Error error) throw error;
                    throw (Exception) e.getCause();
                }
            }
        }
    }

    /** One dialect: EXEC85 extracts, every module runs in parallel, each run's line against the golden. */
    private void everyRunIsTheGolden(String dialect) throws Exception {
        Path work = ROOT.resolve("build/nist/" + dialect);
        Files.createDirectories(work);
        Cobc cobc = new Cobc(Duration.ofSeconds(30), dialect);
        Path overlay = ROOT.resolve("oracle/nist/exec85-" + dialect + ".cards");
        String base = Files.readString(ROOT.resolve("oracle/nist/exec85.cards"));
        List<String> cards = Files.exists(overlay) ? Exec85.cards(base, Files.readString(overlay)) : Exec85.cards(base);
        Path exec85 = Files.createTempDirectory(work, "exec85");
        List<Population.Member> extracted = Exec85.run(ROOT.resolve("corpus/nist-cobol85-test-suite/newcob.val"),
                cards, exec85, new Cobc(Duration.ofSeconds(30)));
        Runs.passed(exec85);
        Map<String, String> switches = Exec85.switches(cards);
        RunNotes notes = RunNotes.read(Files.readString(ROOT.resolve("oracle/nist/run-notes")));
        Map<String, Future<List<ModuleRun.Outcome>>> runs = new LinkedHashMap<>();
        List<Path> dirs = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (String module : MODULES) {
                // NC, the longest, in two parts of its own folders: its programs share no file (J-206).
                int[][] parts = module.equals("NC") ? new int[][] {{0, NC_HALF}, {NC_HALF, Integer.MAX_VALUE}} : new int[][] {{0, Integer.MAX_VALUE}};
                for (int[] part : parts) {
                    Path dir = Files.createTempDirectory(work, module);
                    dirs.add(dir);
                    runs.put(module + " " + part[0], pool.submit(() -> ModuleRun.run(module, extracted, notes, switches, dir, cobc, part[0], part[1])));
                }
            }
            Map<String, Integer> seen = new LinkedHashMap<>();
            for (var run : runs.entrySet()) {
                String module = run.getKey().split(" ")[0];
                for (ModuleRun.Outcome o : run.getValue().get()) lines.add(line(module, o, seen.merge(module + " " + o.program(), 1, Integer::sum)));
            }
        }
        String got = HEADER.replace("-std=cobol85", "-std=" + dialect) + String.join("\n", lines) + "\n";
        Files.writeString(work.resolve("programs.golden"), got);
        String summary = summary(Reconciliation.golden(got)).replace("NIST under cobc", "NIST under cobc -std=" + dialect);
        Files.writeString(work.resolve("summary.txt"), summary);
        System.out.print(summary);
        Path golden = ROOT.resolve(dialect.equals("cobol85") ? "oracle/nist/programs.golden" : "oracle/nist/programs-" + dialect + ".golden");
        if (Boolean.getBoolean("katana.record") && !MODULES.equals(ALL)) throw new IllegalStateException("record from a full run, not from -Dkatana.nist.modules=" + ONLY);
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, got);
        assertEquals(Files.exists(golden) ? only(Files.readString(golden)) : "(no golden for -std=" + dialect + ")", got);
        Runs.passed(dirs.toArray(Path[]::new));
    }

    /** A golden's header and the lines of the modules this run runs (all of them, unless -Dkatana.nist.modules says). */
    private static String only(String golden) {
        StringBuilder out = new StringBuilder();
        for (String line : golden.split("\n", -1)) if (line.startsWith("#") || line.isEmpty() || MODULES.contains(line.split(" ")[0])) out.append(line).append('\n');
        return out.substring(0, out.length() - 1);
    }

    /**
     * The run's summary, as Katana counts (R-047): each module's row (programs run, executed, compile
     * errors, timeouts, then tests passed, failed, deleted, needing inspection, in all) and the total.
     * The published total is reached from it only through RECONCILIATION.md.
     */
    static String summary(List<Reconciliation.Run> runs) {
        StringBuilder out = new StringBuilder("NIST under cobc (Katana's golden, R-047)\nModule Programs Executed Error Timeout Pass Fail Deleted Inspect Total\n");
        int[] total = new int[9];
        for (String row : Reconciliation.rows(runs).values()) {
            out.append(row).append('\n');
            String[] f = row.split(" ");
            for (int i = 0; i < 9; i++) total[i] += Integer.parseInt(f[i + 1]);
        }
        out.append("Total");
        for (int n : total) out.append(' ').append(n);
        return out.append('\n').toString();
    }

    /** A golden line for one run of one program. */
    static String line(String module, ModuleRun.Outcome o, int run) {
        String ended = !o.compiled() ? "compile" : o.error() != null ? "timeout" : "ok";
        String counts = o.counts().map(c -> c.total() + " " + c.pass() + " " + c.fail() + " " + c.deleted() + " " + c.inspect()).orElse("0 0 0 0 0");
        String line = String.join(" ", module, o.program(), String.valueOf(run), ended, counts);
        return o.abnormal() ? line + " exit=" + o.exit() + (o.reason() == null ? "" : " reason=" + o.reason().replace(' ', '_')) : line;
    }
}
