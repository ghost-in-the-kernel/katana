package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.oracle.Runs;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phases 4.1 and 4.5's CardDemo check (research §22, §26): each CardDemo fixture through the java lane, the
 * programs it runs lowered from it, its indexed datasets `rt/IdxFile`'s, loaded and unloaded by `rt/Ksds`
 * (LANE-16), reports what the cobc lane's golden (oracle/lane/carddemo.golden) holds: every step's return
 * code, every stdout's lines and hash, every dataset's records and hash, every finding. The java lane's
 * report says it is the java lane, and has no program sources to hash; nothing else may differ but D-005's
 * DISPLAY lines. CREASTMT's STEP040, which the cobc lane abends in at the PSA (R-027), runs through the z/OS
 * storage facade (LANE-20) and is held to the java lane's own golden and to its input (`Statements`).
 */
@Tag("integration")
class CardDemoJavaLaneIntegrationTest {
    private static final Path ROOT = CardDemoLane.ROOT;
    private static final List<String> JOBS = katana.oracle.fixture.CardDemoJobs.held(ROOT, List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "PRTCATBL", "PRTCATBL-INLINE", "COMBTRAN", "SORTTEST", "CREASTMT"), java.util.Map.of("POSTTRAN-STUDY", "the study's reading of POSTTRAN (J-183), held by its own frozen checks, not a run of its own here"));
    /** The java lane's own golden: CREASTMT, whose STEP040 the cobc lane cannot run (R-027), as this lane ran it (research §26). */
    private static final Path JAVA_GOLDEN = ROOT.resolve("oracle/lane/carddemo-java.golden");

    /**
     * Phase 4.5 (research §26): each job's programs lowered from its own fixture, its steps' PGMs and its
     * modules at its tab widths, no program named here; every job reports what the cobc lane's golden holds,
     * D-005's stdout lines aside, but CREASTMT from STEP040, which holds the java lane's golden and its input.
     */
    @Test
    void everyCardDemoJobTheJavaLaneRunsReportsWhatTheCobcLanesGoldenHolds() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/carddemo-java"));
        Map<String, String> golden = sections(Files.readString(ROOT.resolve("oracle/lane/carddemo.golden")));
        List<Path> runs = new ArrayList<>();
        Map<String, List<String>> differ = new LinkedHashMap<>();
        Map<String, String> javaGolden = new LinkedHashMap<>();
        for (String job : JOBS) {
            Path dir = Files.createTempDirectory(work, job), classes = Files.createTempDirectory(work, job + "-classes");
            runs.add(dir);
            runs.add(classes);
            String text = CobcLane.run(CardDemoLane.fixture(job), CardDemoLane.setup(dir, CardDemoLane.commands(CardDemoLane.fixture(job), classes))).text();
            List<String> cobc = CardDemoLane.comparable(golden.get(job)), java = CardDemoLane.comparable(text);
            int at = job.equals("CREASTMT") ? java.indexOf(java.stream().filter(l -> l.startsWith("stdout STEP040")).findFirst().orElseThrow()) : java.size();
            List<String> unexplained = java.subList(0, at).stream().filter(l -> !cobc.contains(l)).filter(l -> !d005(job, l, dir)).toList();
            if (!unexplained.isEmpty() || (at == java.size() && cobc.size() != java.size())) differ.put(job, unexplained);
            if (job.equals("CREASTMT")) {
                javaGolden.put(job, text + String.join("\n", Files.readAllLines(dir.resolve("sysout/STEP040.stdout"), StandardCharsets.ISO_8859_1)) + "\n");
                assertEquals("50 statements, 262 transactions, each once, every total its lines' sum", Statements.check(CardDemoLane.dataset(dir, "STATEMNT.PS"), CardDemoLane.dataset(dir, "TRXFL.SEQ")));
            }
        }
        assertEquals(Map.of(), differ, "the java lane's lines the cobc lane's golden does not hold, by job");
        String recorded = javaGolden.entrySet().stream().map(e -> "# oracle/fixture/carddemo/" + e.getKey() + ".fixture\n" + e.getValue()).collect(java.util.stream.Collectors.joining());
        if (Boolean.getBoolean("katana.record")) Files.writeString(JAVA_GOLDEN, recorded);
        assertEquals(Files.readString(JAVA_GOLDEN), recorded, "CREASTMT as the java lane ran it (`-Dkatana.record=true` rewrites it; its diff is the review)");
        Runs.passed(runs.toArray(Path[]::new));
    }

    private static final LaneRuns COBC = new LaneRuns("carddemo-java-cobc");

    /**
     * Whether a java lane's stdout line differs from the cobc lane's only as D-005 says: cobc DISPLAYs an
     * overpunched zoned item with a separate trailing sign, where IBM's DISPSIGN(COMPAT) shows its bytes.
     * Each line of the two stdouts must be the same, or the same once each run of digits cobc ends with `+`
     * or `-` is written as COMPAT shows it (the last digit plain, or `p`-`y`, the cobc lane's negative zone).
     */
    private static boolean d005(String job, String line, Path dir) {
        if (!line.startsWith("stdout ")) return false;
        String step = line.split(" ")[1];
        try {
            if (!Files.exists(COBC.work(job).resolve("sysout/" + step + ".stdout"))) COBC.run(job);
            List<String> cobc = Files.readAllLines(COBC.work(job).resolve("sysout/" + step + ".stdout"), StandardCharsets.ISO_8859_1);
            List<String> java = Files.readAllLines(dir.resolve("sysout/" + step + ".stdout"), StandardCharsets.ISO_8859_1);
            if (cobc.size() != java.size()) return false;
            for (int k = 0; k < cobc.size(); k++) if (!cobc.get(k).equals(java.get(k)) && !CardDemoLane.compat(cobc.get(k)).equals(java.get(k))) return false;
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    /** The golden's text for each fixture, by job. */
    private static Map<String, String> sections(String golden) {
        Map<String, String> out = new LinkedHashMap<>();
        String job = null;
        StringBuilder text = new StringBuilder();
        for (String line : golden.split("\n", -1)) {
            if (line.startsWith("# oracle/fixture/carddemo/")) {
                if (job != null) out.put(job, text.toString());
                job = line.replace("# oracle/fixture/carddemo/", "").replace(".fixture", "");
                text.setLength(0);
            } else text.append(line).append('\n');
        }
        if (job != null) out.put(job, text.toString());
        return out;
    }
}
