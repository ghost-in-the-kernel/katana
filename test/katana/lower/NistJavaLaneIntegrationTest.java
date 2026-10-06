package katana.lower;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import katana.oracle.nist.Cobc;
import katana.oracle.nist.Exec85;
import katana.oracle.nist.ModuleRun;
import katana.oracle.nist.Population;
import katana.oracle.nist.RunNotes;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 3.4, the Java lane's first module: NIST NC extracted as the cobc lane extracts it, each program
 * lowered and run by `JavaRunner`, its report's counts set beside the cobc lane's golden under -std=ibm
 * (oracle/nist/programs-ibm.golden). How many runs equal cobc's is written to build/nist-java/NC.txt with
 * each program's line and, for those that do not, what was not lowered. Its floor only rises.
 */
@Tag("integration")
class NistJavaLaneIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String MODULE = "NC";
    /** The NC runs whose counts equal cobc's; raised as the lowering grows, never lowered (the bench discipline, P-01). */
    static final int FLOOR = 83;

    /**
     * The runs where cobc -std=ibm fails tests the Java lane passes, each with why: a discovery against cobc
     * (D-019 Appendix A's intermediate places, D-020 ACCEPT's records, D-021 a reserved word, D-024 a relation's
     * dmax taken from the whole condition) or the lane's
     * known TRUNC setting (the golden recorded without binary truncation, PROFILE-04). Each must pass every
     * test through the Java lane while cobc's golden differs, so a listed run that fails one is not hidden.
     */
    static final Map<String, String> COBC_FINDINGS = Map.ofEntries(
            Map.entry("NC105A", "PROFILE-04"), Map.entry("NC106A", "D-019, PROFILE-04"), Map.entry("NC118A", "D-019"),
            Map.entry("NC119A", "D-019"), Map.entry("NC175A", "D-019"), Map.entry("NC176A", "PROFILE-04"),
            Map.entry("NC177A", "D-019"), Map.entry("NC204M", "D-020"), Map.entry("NC205A", "D-024"),
            Map.entry("NC211A", "D-021"), Map.entry("NC250A", "D-019"));

    @Test
    void ncThroughTheJavaLane() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/nist-java"));
        String base = Files.readString(ROOT.resolve("oracle/nist/exec85.cards")), overlay = Files.readString(ROOT.resolve("oracle/nist/exec85-ibm.cards"));
        List<String> cards = Exec85.cards(base, overlay);
        List<Population.Member> extracted = Exec85.run(ROOT.resolve("corpus/nist-cobol85-test-suite/newcob.val"), cards, Files.createTempDirectory(work, "exec85"), new Cobc(Duration.ofSeconds(30)));
        RunNotes notes = RunNotes.read(Files.readString(ROOT.resolve("oracle/nist/run-notes")));
        Path dir = Files.createTempDirectory(work, MODULE);
        List<ModuleRun.Outcome> outcomes = ModuleRun.run(MODULE, extracted, notes, Exec85.switches(cards), dir, new JavaRunner(60));
        Map<String, String> cobc = new LinkedHashMap<>();
        for (String line : Files.readAllLines(ROOT.resolve("oracle/nist/programs-ibm.golden"))) if (line.startsWith(MODULE + " ")) cobc.putIfAbsent(line.split(" ")[1], line.substring(line.indexOf(' ', line.indexOf(' ') + 1) + 1));
        int same = 0, explained = 0;
        List<String> unexplained = new ArrayList<>();
        List<String> report = new ArrayList<>();
        Map<String, Integer> missing = new TreeMap<>();
        for (ModuleRun.Outcome o : outcomes) {
            // A run with no report is `0 0 0 0 0`, as the cobc lane's golden writes it (ModuleRunIntegrationTest); one that ended in an exception is never the same.
            String java = "1 " + (o.error() == null ? "ok" : "error") + " " + o.counts().map(c -> c.total() + " " + c.pass() + " " + c.fail() + " " + c.deleted() + " " + c.inspect()).orElse("0 0 0 0 0");
            boolean equal = o.error() == null && !o.note().startsWith("exit") && java.equals(cobc.get(o.program()));
            if (equal) same++;
            boolean clean = o.error() == null && o.counts().map(c -> c.fail() == 0 && c.total() > 0).orElse(false);
            if (COBC_FINDINGS.containsKey(o.program()) && (equal || !clean)) unexplained.add(o.program() + ": listed as cobc's but " + (equal ? "the same as cobc" : "failing through the Java lane"));
            if (COBC_FINDINGS.containsKey(o.program()) && !equal && clean) explained++;
            report.add(o.program() + (equal ? " SAME " : " DIFFER ") + java + " | cobc " + cobc.get(o.program()) + (o.error() == null ? "" : " | " + o.error()) + " " + o.note());
            notLowered(dir, o.program(), missing);
        }
        report.add(0, "NC through the Java lane: " + same + " of " + outcomes.size() + " runs report what cobc -std=ibm reports, " + explained + " more pass every test where cobc departs (COBC_FINDINGS); not lowered, by kind: " + missing);
        Files.write(work.resolve(MODULE + ".txt"), report);
        System.out.println(report.get(0));
        assertTrue(same >= FLOOR, same + " < floor " + FLOOR);
        org.junit.jupiter.api.Assertions.assertEquals(List.of(), unexplained);
    }

    /** The lowering's findings for a program, counted by what was not lowered (its first words). */
    private static void notLowered(Path dir, String program, Map<String, Integer> missing) throws Exception {
        Path names = dir.resolve(program + ".class-name");
        if (!Files.exists(names)) return;
        for (String line : Files.readAllLines(names).subList(1, Files.readAllLines(names).size())) {
            String kind = line.replaceAll(" at .*", "").replaceAll("not lowered: ", "").replaceAll("\\(.*", "").trim();
            missing.merge(kind.length() > 40 ? kind.substring(0, 40) : kind, 1, Integer::sum);
        }
    }
}
