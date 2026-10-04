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

/** Every NIST module run under cobc (1.1e), each module's row held to oracle/nist/summary.golden. */
@Tag("integration")
class ModuleRunIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    @Test
    void eachModulesRowIsTheGoldens() throws Exception {
        Map<String, String> golden = new LinkedHashMap<>();
        for (String line : Files.readAllLines(ROOT.resolve("oracle/nist/summary.golden"))) {
            if (!line.startsWith("#") && !line.isBlank()) golden.put(line.split(" ")[0], line);
        }
        Path work = ROOT.resolve("build/nist");
        Files.createDirectories(work);
        Cobc cobc = new Cobc(Duration.ofSeconds(30));
        List<Population.Member> extracted = Exec85.run(ROOT.resolve("corpus/nist-cobol85-test-suite/newcob.val"),
                Exec85.cards(Files.readString(ROOT.resolve("oracle/nist/exec85.cards"))), Files.createTempDirectory(work, "exec85"), cobc);
        Map<String, Future<List<ModuleRun.Outcome>>> runs = new LinkedHashMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (String module : golden.keySet()) {
                Path dir = Files.createTempDirectory(work, module);
                runs.put(module, pool.submit(() -> ModuleRun.run(module, extracted, dir, cobc)));
            }
            Map<String, String> rows = new LinkedHashMap<>();
            List<String> detail = new ArrayList<>();
            for (var run : runs.entrySet()) {
                List<ModuleRun.Outcome> outcomes = run.getValue().get();
                rows.put(run.getKey(), row(run.getKey(), outcomes));
                for (ModuleRun.Outcome o : outcomes) {
                    detail.add(run.getKey() + " " + o.program() + " " + (o.error() != null ? "ERROR " + o.error() + " " : "") + (o.note().isEmpty() ? "" : "(" + o.note() + ") ")
                            + o.counts().map(c -> c.total() + " " + c.pass() + " " + c.fail() + " " + c.deleted() + " " + c.inspect()).orElse(""));
                }
            }
            Files.write(work.resolve("programs.txt"), detail);
            Files.write(work.resolve("summary.txt"), rows.values());
            assertEquals(String.join("\n", golden.values()), String.join("\n", rows.values()));
        }
    }

    /** A module's row in the golden's form, from its runs' outcomes. */
    static String row(String module, List<ModuleRun.Outcome> outcomes) {
        int executed = 0, errors = 0, crashes = 0, pass = 0, fail = 0, deleted = 0, inspect = 0, total = 0;
        for (ModuleRun.Outcome o : outcomes) {
            if (!o.compiled()) errors++;
            else if (o.error() != null) crashes++;
            else executed++;
            if (o.counts().isPresent()) {
                Report.Counts c = o.counts().get();
                pass += c.pass(); fail += c.fail(); deleted += c.deleted(); inspect += c.inspect(); total += c.total();
            }
        }
        return String.join(" ", module, "" + outcomes.size(), "" + executed, "" + errors, "" + crashes,
                "" + pass, "" + fail, "" + deleted, "" + inspect, "" + total);
    }
}
