package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import katana.oracle.Runs;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 6.1's proof on CardDemo (research §35, ROADMAP 6): every job the java lane runs, its programs lowered under
 * the structuring pass, reports what it reports lowered literally (which CardDemoJavaLaneIntegrationTest holds
 * to the cobc lane's golden), and each step's paragraph-entry trace is the literal Java's, line for line.
 */
@Tag("integration")
class StructuringCardDemoIntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT;
    private static final List<String> JOBS = katana.oracle.fixture.CardDemoJobs.held(ROOT, List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "PRTCATBL", "PRTCATBL-INLINE", "COMBTRAN", "SORTTEST", "CREASTMT"), java.util.Map.of("POSTTRAN-STUDY", "the study's reading of POSTTRAN (J-183), held by its own frozen checks, not a run of its own here"));

    @Test
    void everyJobStructuredReportsAndTracesWhatItDoesLiteral() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/structuring-carddemo"));
        Map<String, String> differ = new LinkedHashMap<>();
        int traced = 0, methods = 0;
        for (String job : JOBS) {
            Fixture fixture = CardDemoJavaLaneIntegrationTest.fixture(job);
            Path literal = run(work, job, fixture, false), structured = run(work, job, fixture, true);
            if (!Files.readString(literal.resolve("report")).equals(Files.readString(structured.resolve("report")))) differ.put(job, "the report");
            Map<String, String> a = traces(literal), b = traces(structured);
            if (!a.equals(b)) differ.put(job + " traces", a.keySet() + " " + b.keySet());
            traced += a.size();
            methods += methods(structured.resolveSibling(structured.getFileName() + "-classes"));
            Runs.passed(literal, structured, literal.resolveSibling(literal.getFileName() + "-classes"), structured.resolveSibling(structured.getFileName() + "-classes"));
        }
        assertEquals(Map.of(), differ, "what a structured job did otherwise than the literal one");
        System.out.println("structured: " + methods + " range methods over the jobs' programs; " + traced + " steps traced alike");
        assertTrue(traced > 0 && methods > 0, "the structured runs ran structured Java, and were traced: " + methods + " methods, " + traced + " traces");
    }

    /** One job in the java lane, traced, its report written beside its folders; the folder. */
    private static Path run(Path work, String job, Fixture fixture, boolean structured) throws Exception {
        Path dir = Files.createDirectories(work.resolve(job + (structured ? "-structured" : "-literal")));
        Path classes = Files.createDirectories(work.resolve(job + (structured ? "-structured" : "-literal") + "-classes"));
        String text = CobcLane.run(fixture, CardDemoJavaLaneIntegrationTest.setup(dir, CardDemoJavaLaneIntegrationTest.commands(fixture, classes, structured)).traced(dir)).text();
        Files.writeString(work.resolve(job + (structured ? "-structured" : "-literal") + ".report"), text);
        Files.writeString(dir.resolve("report"), text);
        return dir;
    }

    /** The range methods (`private void perform<first>()`) the structured Java declares. */
    private static int methods(Path classes) throws Exception {
        int n = 0;
        try (var files = Files.list(classes)) {
            for (Path f : files.filter(f -> f.toString().endsWith(".java")).toList()) n += (int) Files.readAllLines(f).stream().filter(l -> l.trim().matches("private void perform\\d+\\(\\) \\{")).count();
        }
        return n;
    }

    /** Each step's paragraph trace, by step. */
    private static Map<String, String> traces(Path dir) throws Exception {
        Map<String, String> out = new TreeMap<>();
        Path trace = dir.resolve("trace");
        if (!Files.exists(trace)) return out;
        try (var files = Files.list(trace)) {
            for (Path f : files.sorted().toList()) out.put(f.getFileName().toString(), Files.readString(f, java.nio.charset.StandardCharsets.ISO_8859_1));
        }
        return out;
    }
}
