package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import katana.oracle.compare.Compare;
import katana.oracle.compare.Layout;
import katana.oracle.fixture.Fixture;
import katana.oracle.proof.ProofReport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 1.9's check: the v0 proof reports for the trivial fixture (the cobc lane against the java lane)
 * and POSTTRAN (the cobc lane against the study's frozen datasets, F-02), each with the gate, every
 * dataset compared, coverage from a traced run that is itself held to the untraced one, and PROOF.md's
 * limits quoted. Both reports are held by goldens in oracle/proof/ (`-Dkatana.record=true` rewrites).
 */
@Tag("integration")
class ProofIntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT, TRIVIAL = ROOT.resolve("oracle/fixture/trivial");

    private static CobcLane.Setup trivial(String folder, Map<String, List<String>> java) {
        return new CobcLane.Setup(ROOT, Map.of("repo", ROOT), TRIVIAL.resolve("cbl"), List.of(TRIVIAL.resolve("cpy")),
                ROOT.resolve("build/" + folder), ROOT.resolve("build/lane-cache"), Duration.ofSeconds(60), (j, d) -> null, null, java);
    }

    private static void hold(String name, String report) throws Exception {
        Path golden = ROOT.resolve("oracle/proof/" + name + ".report.md");
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, report);
        System.out.print(report);
        assertEquals(Files.readString(golden), report);
    }

    @Test
    void theTrivialFixtureAgainstTheJavaLane() throws Exception {
        Fixture fixture = Fixture.read(Files.readString(TRIVIAL.resolve("COPY.fixture")));
        Report a = CobcLane.run(fixture, trivial("proof-copy-a", null)), b = CobcLane.run(fixture, trivial("proof-copy-b", null));
        Report traced = CobcLane.run(fixture, trivial("proof-copy-a", null).traced(ROOT.resolve("build/proof-copy-trace")));
        Report java = CobcLane.run(fixture, trivial("proof-copy-java", Map.of("COPYREC", List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), CopyRecords.class.getName()))));
        Gate.Result gate = Gate.compare(fixture, a, ROOT.resolve("build/proof-copy-a"), b, ROOT.resolve("build/proof-copy-b"));
        assertTrue(Gate.compare(fixture, a, ROOT.resolve("build/proof-copy-a"), traced, ROOT.resolve("build/proof-copy-trace")).identical(), "tracing changed the run");
        String out = "KATANA.TRIVIAL.OUT";
        Compare.Result result = Compare.compare(gate, Compare.Side.fixed(out, Files.readAllBytes(ROOT.resolve("build/proof-copy-a/unload/" + out)), 20, "ascii"),
                Compare.Side.fixed(out, Files.readAllBytes(ROOT.resolve("build/proof-copy-java/unload/" + out)), 20, "ascii"),
                Layout.read(Files.readString(TRIVIAL.resolve("cpy/COPYREC.cpy"))), List.of(), Compare.Numproc.NOPFD);
        assertEquals(List.of(), java.findings());
        hold("COPY", ProofReport.write(fixture, "oracle/fixture/trivial/COPY.fixture", a, gate, "the java lane (a hand-written Java copy)",
                List.of(new ProofReport.Compared(out, List.of(), result)), traced, Files.readString(ROOT.resolve("docs/PROOF.md")),
                findings(TRIVIAL.resolve("cbl/COPYREC.cbl"), TRIVIAL.resolve("cpy"))));
    }

    /** A step program's findings under the ibm profile: what NonExact finds in its IR. */
    static List<katana.findings.Finding> findings(Path source, Path copybooks) throws Exception {
        katana.profile.Profile ibm = katana.profile.Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        katana.front.Parse.Result parse = katana.front.Parse.file(source, new katana.front.Copybooks(List.of(copybooks), n -> 8), 8);
        List<katana.findings.Finding> out = new java.util.ArrayList<>();
        for (var r : katana.sir.build.Build.programs(parse, ibm.hash())) out.addAll(katana.sir.check.NonExact.findings(r.program(), ibm));
        return out;
    }

    @Test
    void posttranAgainstTheStudysFrozenDatasets() throws Exception {
        Fixture fixture = Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/POSTTRAN.fixture")));
        LaneRuns runA = new LaneRuns("proof-posttran-a"), runB = new LaneRuns("proof-posttran-b");
        Report a = runA.run("POSTTRAN"), b = runB.run("POSTTRAN");
        Path carddemo = ROOT.resolve("corpus/carddemo");
        CobcLane.Setup setup = new CobcLane.Setup(ROOT, Map.of("carddemo", carddemo, "repo", ROOT), carddemo.resolve("app/cbl"),
                List.of(carddemo.resolve("app/cpy")), runA.work("POSTTRAN"), ROOT.resolve("build/lane-cache"), Duration.ofSeconds(60), (j, d) -> null, null);
        Report traced = CobcLane.run(fixture, setup.traced(ROOT.resolve("build/proof-posttran-trace")));
        Gate.Result gate = Gate.compare(fixture, a, runA.work("POSTTRAN"), b, runB.work("POSTTRAN"));
        assertTrue(Gate.compare(fixture, a, runA.work("POSTTRAN"), traced, ROOT.resolve("build/proof-posttran-trace")).identical(), "tracing changed the run");
        Path frozen = ROOT.resolve("oracle/prototypes/carddemo/post1");
        String tranfile = "AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS", rejects = "AWS.M2.CARDDEMO.DALYREJS.G0001V00";
        Compare.Result t = Compare.compare(gate, Compare.Side.fixed(tranfile, Files.readAllBytes(runA.work("POSTTRAN").resolve("unload/" + tranfile)), 350, "ascii"),
                Compare.Side.fixed(tranfile, Files.readAllBytes(frozen.resolve("tranfile.unl")), 350, "ascii"),
                Layout.read(Files.readString(carddemo.resolve("app/cpy/CVTRA05Y.cpy"))), List.of(), Compare.Numproc.NOPFD);
        Compare.Result r = Compare.compare(gate, Compare.Side.fixed(rejects, Files.readAllBytes(runA.work("POSTTRAN").resolve("unload/" + rejects)), 430, "ascii"),
                Compare.Side.fixed(rejects, Files.readAllBytes(frozen.resolve("dalyrejs.out")), 430, "ascii"), Layout.raw(430), List.of(), Compare.Numproc.NOPFD);
        assertTrue(t.same() && r.same(), t.findings() + " " + r.findings());
        assertTrue(traced.coverage().get("CBTRN02C").total() > 0, traced.text());
        hold("POSTTRAN", ProofReport.write(fixture, "oracle/fixture/carddemo/POSTTRAN.fixture", a, gate, "the study's frozen datasets (F-02, oracle/prototypes/carddemo/post1)",
                List.of(new ProofReport.Compared(tranfile, List.of(), t), new ProofReport.Compared(rejects, List.of(), r)), traced, Files.readString(ROOT.resolve("docs/PROOF.md")),
                findings(carddemo.resolve("app/cbl/CBTRN02C.cbl"), carddemo.resolve("app/cpy"))));
    }
}
