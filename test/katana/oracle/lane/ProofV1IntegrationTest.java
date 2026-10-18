package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import katana.oracle.compare.Compare;
import katana.oracle.compare.Layout;
import katana.oracle.fixture.Fixture;
import katana.oracle.proof.ProofReport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 4.6's check (research §27): the proof report v1 for each of the four jobs, the java lane against
 * the cobc lane, every dataset the job leaves compared under its fixture's copybook, each program's coverage
 * from the cobc trace or, where the cobc lane cannot run it (R-027), the java lane's, which is held to the
 * cobc trace on every program both run; and CardDemo's coverage by the four, every paragraph none entered
 * listed. Held by goldens in oracle/proof/ (`-Dkatana.record=true` rewrites them).
 */
@Tag("integration")
class ProofV1IntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT, CARDDEMO = ROOT.resolve("corpus/carddemo");
    /** The four jobs of 4.5, and TRANREPT-STUDY, the run of TRANREPT that reaches CBTRN03C (TRANREPT as its JCL is written stops at its first step, J-035). */
    private static final List<String> JOBS = List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "CREASTMT");

    @Test
    void theFourJobsProvenAndCardDemosCoverage() throws Exception {
        LaneRuns runA = new LaneRuns("proof1-a"), runB = new LaneRuns("proof1-b");
        Map<String, Map<String, Report.Coverage>> byJob = new LinkedHashMap<>();
        Map<String, List<String>> differ = new TreeMap<>();
        for (String job : JOBS) prove(job, runA, runB, byJob, differ);
        katana.intent.PerformTraces.hold(performs(), JOBS);
        assertEquals(Map.of(), differ, "datasets that differ, and programs whose java trace is not the cobc trace");
        hold("CARDDEMO.coverage.md", ProofReport.coverage(byJob));
    }

    /** One job: the gate between two cobc runs, the cobc trace unchanged by tracing, the java lane's trace and datasets compared with the cobc lane's, its v1 report held. */
    private static void prove(String job, LaneRuns runA, LaneRuns runB, Map<String, Map<String, Report.Coverage>> byJob, Map<String, List<String>> differ) throws Exception {
        Fixture fixture = CardDemoJavaLaneIntegrationTest.fixture(job);
        Report a = runA.run(job), b = runB.run(job);
        Gate.Result gate = Gate.compare(fixture, a, runA.work(job), b, runB.work(job));
        Path cobcTrace = ROOT.resolve("build/proof1-trace/" + job), javaDir = ROOT.resolve("build/proof1-java/" + job), classes = Files.createDirectories(ROOT.resolve("build/proof1-classes/" + job));
        Report traced = CobcLane.run(fixture, runA.setup(job).traced(cobcTrace));
        // Under -ftrace libcob says where a signal stopped the program, on stderr: the trace's own lines, not the run's.
        Set<String> signalled = signalled(fixture, a);
        List<String> changed = Gate.compare(fixture, a, runA.work(job), traced, cobcTrace).differences().stream()
                .filter(d -> signalled.stream().noneMatch(step -> d.startsWith(step + ".stderr:"))).toList();
        assertEquals(List.of(), changed, job + ": tracing changed the run");
        Report java = CobcLane.run(fixture, CardDemoJavaLaneIntegrationTest.setup(javaDir, CardDemoJavaLaneIntegrationTest.commands(fixture, classes)).traced(javaDir));
        Set<String> javaOnly = javaOnly(fixture, a);
        Map<String, Report.Coverage> coverage = new TreeMap<>();
        Map<String, String> from = new TreeMap<>();
        for (var c : java.coverage().entrySet()) {
            boolean cobc = !javaOnly.contains(c.getKey());
            if (cobc && !c.getValue().equals(traced.coverage().get(c.getKey()))) differ.put(job + " " + c.getKey(), List.of("java " + c.getValue(), "cobc " + traced.coverage().get(c.getKey())));
            coverage.put(c.getKey(), c.getValue());
            from.put(c.getKey(), cobc ? "the cobc trace; the java lane's the same" : "the java lane's trace (the cobc lane cannot run it, R-027)");
        }
        byJob.put(job, coverage);
        List<ProofReport.Compared> compared = new ArrayList<>();
        List<String> notes = new ArrayList<>(laneHypotheses(fixture, javaOnly));
        List<String> onlyJava = new ArrayList<>();
        compareDatasets(job, fixture, a, gate, runA.work(job), javaDir, java, compared, onlyJava, notes, differ);
        List<katana.findings.Finding> findings = new ArrayList<>();
        for (String program : coverage.keySet()) findings.addAll(ProofIntegrationTest.findings(source(program), CARDDEMO.resolve("app/cpy"), Tabs.widths(fixture)));
        hold(job + ".v1.report.md", ProofReport.v1(fixture, "oracle/fixture/carddemo/" + job + ".fixture", a, gate, compared, onlyJava, coverage, from,
                Files.readString(ROOT.resolve("docs/PROOF.md")), findings, notes));
    }

    /** Each dataset both lanes wrote compared record by record, with the copybook's layout; one only the java lane wrote listed apart. */
    private static void compareDatasets(String job, Fixture fixture, Report a, Gate.Result gate, Path cobcWork, Path javaDir, Report java, List<ProofReport.Compared> compared, List<String> onlyJava,
            List<String> notes, Map<String, List<String>> differ) throws Exception {
        for (String dsn : new TreeSet<>(java.datasets().keySet())) {
            if (!a.datasets().containsKey(dsn)) {
                onlyJava.add(dsn);
                continue;
            }
            int lrecl = a.lrecls().get(dsn);
            Compare.Result r = Compare.compare(gate, Compare.Side.fixed(dsn, Files.readAllBytes(cobcWork.resolve("unload/" + dsn)), lrecl, "ascii"),
                    Compare.Side.fixed(dsn, Files.readAllBytes(javaDir.resolve("unload/" + dsn)), lrecl, "ascii"), layout(fixture, dsn, lrecl, notes), List.of(), Compare.Numproc.NOPFD);
            compared.add(new ProofReport.Compared(dsn, List.of(), r));
            if (!r.same()) differ.put(job + " " + dsn, r.findings().stream().map(String::valueOf).toList());
        }
    }

    /** The steps the cobc lane ended on a signal (R-027). */
    private static Set<String> signalled(Fixture fixture, Report cobc) {
        Set<String> out = new TreeSet<>();
        for (Fixture.Step step : fixture.steps()) if (cobc.steps().getOrDefault(step.name(), "").contains(" abend SIG")) out.add(step.name());
        return out;
    }

    /** The programs the cobc lane cannot run (R-027): each signalled step's PGM, and, in such a job, its modules. */
    private static Set<String> javaOnly(Fixture fixture, Report cobc) {
        Set<String> out = new TreeSet<>(), signalled = signalled(fixture, cobc);
        for (Fixture.Step step : fixture.steps()) if (signalled.contains(step.name())) out.add(step.line().attributes().get("pgm"));
        if (!out.isEmpty()) for (var line : fixture.header()) if (line.keyword().equals("module")) out.add(line.values().get(0));
        return out;
    }

    /** What a java-only step stands on, for the report. */
    private static List<String> laneHypotheses(Fixture fixture, Set<String> javaOnly) {
        if (javaOnly.isEmpty()) return List.of();
        return List.of(String.join(" and ", javaOnly) + " run in the java lane only (R-027), their step reading z/OS control blocks through the lane's storage facade, laid out as R-063's hypotheses say;"
                + " what they write is held to oracle/lane/carddemo-java.golden and to its input (Statements), and proven by nothing");
    }

    /** A dataset's layout: the copybook a DD of the fixture names for it, else its bytes, said in a note when the copybook is not one record. */
    private static Layout layout(Fixture fixture, String dsn, int lrecl, List<String> notes) throws Exception {
        String base = dsn.replaceAll("\\.G\\d{4}V\\d{2}$", "");
        for (Fixture.Step step : fixture.steps()) {
            for (Fixture.Dd dd : step.dds()) {
                String named = dd.attributes().getOrDefault("dsn", "").replaceAll("\\(.*\\)$", ""), copybook = dd.attributes().get("copybook");
                if (!named.equals(base) || copybook == null || copybook.equals("-")) continue;
                Path cpy = CARDDEMO.resolve("app/cpy/" + copybook + ".cpy");
                try {
                    return Layout.read(Files.readString(Files.exists(cpy) ? cpy : CARDDEMO.resolve("app/cpy/" + copybook + ".CPY")));
                } catch (Layout.Unsupported e) {
                    notes.add(dsn + " compared byte for byte: " + copybook + " is no one record (" + e.getMessage() + ")");
                    return Layout.raw(lrecl);
                }
            }
        }
        return Layout.raw(lrecl);
    }

    private static Path source(String program) {
        Path lower = CARDDEMO.resolve("app/cbl/" + program + ".cbl");
        return Files.exists(lower) ? lower : CARDDEMO.resolve("app/cbl/" + program + ".CBL");
    }

    /** Each java-lane step's PERFORM trace (rt/PerformTrace), the jobs in order, each job's steps by name (T-F3, research §34). */
    private static List<List<String>> performs() throws Exception {
        List<List<String>> out = new ArrayList<>();
        for (String job : JOBS) {
            Path dir = ROOT.resolve("build/proof1-java/" + job + "/perform");
            if (!Files.exists(dir)) continue;
            try (var files = Files.list(dir)) {
                for (Path f : files.sorted().toList()) out.add(Files.readAllLines(f, java.nio.charset.StandardCharsets.ISO_8859_1));
            }
        }
        return out;
    }

    private static void hold(String name, String report) throws Exception {
        Path golden = ROOT.resolve("oracle/proof/" + name);
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, report);
        assertEquals(Files.readString(golden), report);
    }
}
