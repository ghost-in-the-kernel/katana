package katana.oracle.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 3.1's first step: GnuCOBOL's autotests (fetched by `corpus/fetch.sh gnucobol-autotests`, test
 * input only, R-045) read by `Autotests` into `corpus/gnucobol-autotests/cases/`, and their counts held
 * in oracle/autotest/autotests.golden: per `.at` file, the cases, those that compile a program and run
 * it against an expected stdout, and those an `AT_SKIP_IF` or `AT_XFAIL_IF` marks. Counts only: no
 * case's text enters the repository. `-Dkatana.record=true` rewrites the golden.
 */
@Tag("integration")
class AutotestCorpusIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path SOURCES = ROOT.resolve("corpus/gnucobol-autotests/tests/testsuite.src");
    private static final Path GOLDEN = ROOT.resolve("oracle/autotest/autotests.golden");
    private static final String HEADER = """
            # Phase 3.1: GnuCOBOL's autotests as Katana reads them (oracle/autotest/Autotests.java), counts only (R-045):
            # `FILE cases C runnable R conditional K`, R the cases that compile a program and run it against an
            # expected stdout; then the total. Written by `gradle integration -Dkatana.record=true`.
            """;

    /** Whether a case compiles a program with `$COMPILE` and later runs something against an expected stdout. */
    static boolean runnable(Autotests.Case c) {
        boolean compiled = false;
        for (Autotests.Check k : c.checks()) {
            if (k.command().contains("$COMPILE ") || k.command().contains("$COMPILE\t")) compiled = true;
            else if (compiled && !k.stdout().isEmpty() && (k.command().contains("COBCRUN_DIRECT") || k.command().contains("./"))) return true;
        }
        return false;
    }

    @Test
    void everyAutotestReadsAndItsCountsAreTheGolden() throws Exception {
        List<Path> files;
        try (Stream<Path> list = Files.list(SOURCES)) {
            files = list.filter(p -> p.toString().endsWith(".at")).sorted().toList();
        }
        StringBuilder out = new StringBuilder(HEADER);
        int cases = 0, runnable = 0, conditional = 0;
        List<Autotests.Case> all = new ArrayList<>();
        for (Path f : files) {
            List<Autotests.Case> read = Autotests.read(f.getFileName().toString(), Files.readString(f, StandardCharsets.ISO_8859_1));
            all.addAll(read);
            int r = (int) read.stream().filter(AutotestCorpusIntegrationTest::runnable).count(), k = (int) read.stream().filter(Autotests.Case::conditional).count();
            out.append(f.getFileName()).append(" cases ").append(read.size()).append(" runnable ").append(r).append(" conditional ").append(k).append('\n');
            cases += read.size();
            runnable += r;
            conditional += k;
        }
        out.append("total files ").append(files.size()).append(" cases ").append(cases).append(" runnable ").append(runnable).append(" conditional ").append(conditional).append('\n');
        Autotests.write(all, Files.createDirectories(ROOT.resolve("corpus/gnucobol-autotests/cases")));
        Files.write(ROOT.resolve("corpus/gnucobol-autotests/runnable.txt"),
                all.stream().filter(AutotestCorpusIntegrationTest::runnable).map(c -> c.file().replaceAll("\\.at$", "") + "-" + c.line()).toList());
        List<String> bracketed = all.stream().filter(AutotestCorpusIntegrationTest::runnable)
                .filter(c -> c.checks().stream().anyMatch(k -> k.stdout().contains("[") || k.stdout().contains("]"))).map(c -> c.file() + ":" + c.line()).toList();
        assertEquals(List.of("run_misc.at:1762", "run_ml.at:856"), bracketed,
                "the runnable cases whose expected stdout holds a bracket, both real: run_misc.at:1762's is a quadrigraph, run_ml.at:856's the CDATA its program holds (J-269: the `[[x]]` second level is gone)");
        System.out.print(out);
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, out.toString());
        assertEquals(Files.exists(GOLDEN) ? Files.readString(GOLDEN) : "(no golden)", out.toString());
    }
}
