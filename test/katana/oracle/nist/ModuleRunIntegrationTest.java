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
 * Every NIST module run under cobc (1.1e), each run held to oracle/nist/programs.golden (R-047).
 * `gradle integration -Dkatana.record=true` rewrites the golden from the run, for review in its diff.
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
        Path work = ROOT.resolve("build/nist");
        Files.createDirectories(work);
        Cobc cobc = new Cobc(Duration.ofSeconds(30));
        List<Population.Member> extracted = Exec85.run(ROOT.resolve("corpus/nist-cobol85-test-suite/newcob.val"),
                Exec85.cards(Files.readString(ROOT.resolve("oracle/nist/exec85.cards"))), Files.createTempDirectory(work, "exec85"), cobc);
        RunNotes notes = RunNotes.read(Files.readString(ROOT.resolve("oracle/nist/run-notes")));
        Map<String, Future<List<ModuleRun.Outcome>>> runs = new LinkedHashMap<>();
        List<String> lines = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (String module : MODULES) {
                Path dir = Files.createTempDirectory(work, module);
                runs.put(module, pool.submit(() -> ModuleRun.run(module, extracted, notes, dir, cobc)));
            }
            for (var run : runs.entrySet()) {
                Map<String, Integer> seen = new LinkedHashMap<>();
                for (ModuleRun.Outcome o : run.getValue().get()) lines.add(line(run.getKey(), o, seen.merge(o.program(), 1, Integer::sum)));
            }
        }
        String got = HEADER + String.join("\n", lines) + "\n";
        Files.writeString(work.resolve("programs.golden"), got);
        Path golden = ROOT.resolve("oracle/nist/programs.golden");
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, got);
        assertEquals(Files.readString(golden), got);
    }

    /** A golden line for one run of one program. */
    static String line(String module, ModuleRun.Outcome o, int run) {
        String ended = !o.compiled() ? "compile" : o.error() != null ? "timeout" : "ok";
        String counts = o.counts().map(c -> c.total() + " " + c.pass() + " " + c.fail() + " " + c.deleted() + " " + c.inspect()).orElse("0 0 0 0 0");
        return String.join(" ", module, o.program(), String.valueOf(run), ended, counts);
    }
}
