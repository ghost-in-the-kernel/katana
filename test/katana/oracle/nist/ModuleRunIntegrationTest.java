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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Every NIST module run under cobc, each run held to its golden (R-047): under `-std=cobol85`
 * oracle/nist/programs.golden (1.1), under `-std=ibm` oracle/nist/programs-ibm.golden (1.2).
 * `-Dkatana.std=ibm` runs one dialect only (CI runs each in its own step); `-Dkatana.record=true`
 * rewrites the goldens from the run, for review in their diff. EXEC85 itself is tooling and is always
 * compiled under COBOL 85.
 */
@Tag("integration")
class ModuleRunIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final List<String> MODULES = List.of("NC", "SM", "IC", "SQ", "RL", "IX", "ST", "SG", "OB", "IF", "RW", "DB");
    private static final String HEADER = """
            # Katana's NIST golden (R-047): each run of each program, as its own footer reports it under Katana's
            # profile (cobc 3.1.2, -std=cobol85, oracle/nist/exec85.cards and run-notes). A line is: module, program,
            # run, how it ended (ok: ran to an end; compile: cobc refused it; timeout: stopped at the limit), then
            # tests in all, passed, failed, deleted, needing inspection. Written by `gradle integration -Dkatana.record=true`.
            """;

    @Test
    void everyRunIsTheGoldens() throws Exception {
        for (String dialect : System.getProperty("katana.std", "cobol85,ibm").split(",")) everyRunIsTheGolden(dialect);
    }

    /** One dialect: EXEC85 extracts, every module runs in parallel, each run's line against the golden. */
    private void everyRunIsTheGolden(String dialect) throws Exception {
        Path work = ROOT.resolve("build/nist/" + dialect);
        Files.createDirectories(work);
        Cobc cobc = new Cobc(Duration.ofSeconds(30), dialect);
        Path overlay = ROOT.resolve("oracle/nist/exec85-" + dialect + ".cards");
        String base = Files.readString(ROOT.resolve("oracle/nist/exec85.cards"));
        List<String> cards = Files.exists(overlay) ? Exec85.cards(base, Files.readString(overlay)) : Exec85.cards(base);
        List<Population.Member> extracted = Exec85.run(ROOT.resolve("corpus/nist-cobol85-test-suite/newcob.val"),
                cards, Files.createTempDirectory(work, "exec85"), new Cobc(Duration.ofSeconds(30)));
        Map<String, String> switches = Exec85.switches(cards);
        RunNotes notes = RunNotes.read(Files.readString(ROOT.resolve("oracle/nist/run-notes")));
        Map<String, Future<List<ModuleRun.Outcome>>> runs = new LinkedHashMap<>();
        List<String> lines = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (String module : MODULES) {
                Path dir = Files.createTempDirectory(work, module);
                runs.put(module, pool.submit(() -> ModuleRun.run(module, extracted, notes, switches, dir, cobc)));
            }
            for (var run : runs.entrySet()) {
                Map<String, Integer> seen = new LinkedHashMap<>();
                for (ModuleRun.Outcome o : run.getValue().get()) lines.add(line(run.getKey(), o, seen.merge(o.program(), 1, Integer::sum)));
            }
        }
        String got = HEADER.replace("-std=cobol85", "-std=" + dialect) + String.join("\n", lines) + "\n";
        Files.writeString(work.resolve("programs.golden"), got);
        String summary = summary(Reconciliation.golden(got)).replace("NIST under cobc", "NIST under cobc -std=" + dialect);
        Files.writeString(work.resolve("summary.txt"), summary);
        System.out.print(summary);
        Path golden = ROOT.resolve(dialect.equals("cobol85") ? "oracle/nist/programs.golden" : "oracle/nist/programs-" + dialect + ".golden");
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, got);
        assertEquals(Files.exists(golden) ? Files.readString(golden) : "(no golden for -std=" + dialect + ")", got);
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
        return String.join(" ", module, o.program(), String.valueOf(run), ended, counts);
    }
}
