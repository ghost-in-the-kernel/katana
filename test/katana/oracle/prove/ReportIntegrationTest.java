package katana.oracle.prove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.oracle.fixture.Content;
import katana.oracle.fixture.Fixture;
import katana.oracle.lane.CobcLane;
import katana.oracle.proof.ProofReport;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.check.NonExact;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** 8A.6's check (research §53): the divergence report for 8A.1's hand-written HALF_UP port of ACCRUE, twice alike, its proof refused when its fixture is not the one cited. */
@Tag("integration")
class ReportIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", ".")), FOREIGN = ROOT.resolve("oracle/fixture/foreign");

    /** The report built twice from fresh runs; its entries as predicted; its proof held to the fixture. */
    @Test
    void theNaivePortsReportSaysWhatDivergesWhyWhereAndTheFix() throws Exception {
        String first = report(ROOT.resolve("build/divergence-report/a")), second = report(ROOT.resolve("build/divergence-report/b"));
        Files.writeString(ROOT.resolve("build/divergence-report/ACCRUE.md"), first);
        System.out.println(first);
        assertEquals(first, second, "two runs write the same report");
        assertTrue(first.contains("## 1. proven: ACCT-INT in KATANA.FOREIGN.OUT, records [1, 3]") && first.contains("- Statement: COMPUTE #1 IN PROCEDURE DIVISION (ACCRUE.cbl:28)"), first);
        assertTrue(first.contains("- Cause: DV-02 scale truncation, reproducing 2 of 2 cells") && first.contains("- Aligned Java: AccruePorts.java:17 (unconfirmed, literal 375)"), first);
        assertTrue(first.contains("proven: ACCT-BAL") && first.contains("- Follows from: the divergence of ACCT-INT, which the statement reads"), first);
        assertTrue(first.contains("Arithmetic.store(") && first.contains("## What it never covers (docs/PROOF.md, verbatim)"), first);
        assertEquals(List.of(), Report.refusals(first, ROOT));
        // Each proven entry is refused on its own: two here.
        assertEquals(2, Report.refusals(first.replace("oracle/fixture/foreign/ACCRUE.fixture", "oracle/fixture/foreign/NONE.fixture"), ROOT).size());
        String address = first.replaceAll("(?s).*?Fixture: \\S+ sha256:([0-9a-f]{64}).*", "$1");
        assertEquals(2, Report.refusals(first.replace(address, "0".repeat(64)), ROOT).size());
    }

    private static String report(Path work) throws Exception {
        Path classes = work.resolve("classes");
        String className = AttributionIntegrationTest.lowered(FOREIGN.resolve("cbl/ACCRUE.cbl"), FOREIGN.resolve("cpy"), classes);
        Fixture fixture = Fixture.read(Files.readString(FOREIGN.resolve("ACCRUE.fixture")));
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        Program program = Build.programs(Parse.file(FOREIGN.resolve("cbl/ACCRUE.cbl"), new Copybooks(List.of(FOREIGN.resolve("cpy")), n -> 8), 8), ibm.hash()).get(0).program();
        List<String> naive = List.of(AttributionIntegrationTest.java(), "-cp", System.getProperty("java.class.path"), AccruePorts.class.getName() + "$Naive");
        Prove.Setup s = AttributionIntegrationTest.setup(ROOT, FOREIGN, work.resolve("runs"));
        Attribute.Result a = Attribute.attribute(fixture, s, Map.of("ACCRUE", naive), AttributionIntegrationTest.probes("ACCRUE", className, classes));
        Path katana = work.resolve("katana"), events = work.resolve("events.txt");
        Files.deleteIfExists(events);
        CobcLane.run(fixture, Prove.lane(s, katana, Map.of("ACCRUE", List.of(AttributionIntegrationTest.java(), "-Dkatana.events=" + events, "-cp", classes + java.io.File.pathSeparator + System.getProperty("java.class.path"), className))).traced(katana));
        Map<String, Integer> counted = new TreeMap<>();
        if (Files.exists(events)) for (String l : Files.readAllLines(events)) counted.merge(l.substring("Event: ".length()), 1, Integer::sum);
        StringBuilder trace = new StringBuilder();
        if (Files.exists(katana.resolve("trace"))) try (var t = Files.list(katana.resolve("trace"))) {
            for (Path f : t.sorted().toList()) trace.append(Files.readString(f, StandardCharsets.ISO_8859_1));
        }
        byte[] input = Content.records(Files.readAllBytes(FOREIGN.resolve("IN.txt")), "lines", 20, null);
        List<Align.Line> align = Align.infer(program, "AccruePorts.java", Files.readString(ROOT.resolve("test/katana/oracle/prove/AccruePorts.java"))).lines();
        Divergences.Runs runs = new Divergences.Runs(Files.readString(classes.resolve(className + ".java")), trace.toString(), counted, "KATANA.FOREIGN.IN", input, 20);
        List<Report.Entry> entries = Divergences.entries(program, "oracle/fixture/foreign/ACCRUE.fixture", fixture, a, align, runs);
        String port = "java " + AccruePorts.class.getName() + "$Naive";
        return Report.write(new Report.Header("ACCRUE", "oracle/fixture/foreign/ACCRUE.fixture", fixture.hash().replace("sha256:", ""), port, a.cells().size()), entries,
                NonExact.findings(program, ibm), ProofReport.neverCovers(Files.readString(ROOT.resolve("docs/PROOF.md"))));
    }
}
