package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phases 1.4 and 1.5 (R-048): the CardDemo fixtures through the cobc lane. POSTTRAN reproduces the
 * study's frozen TRANFILE unload (F-02); INTCALC runs through the PARM driver; TRANREPT-STUDY
 * reproduces the frozen sort and report; COMBTRAN and SORTTEST run every step. TRANREPT and PRTCATBL
 * as the JCL writes them stop at REPROCT's comment in column 1 (J-035); CREASTMT and PRTCATBL-INLINE
 * stop where an OUTREC builds records SORTOUT's length does not hold (J-043). Every report is held by
 * oracle/lane/carddemo.golden (`-Dkatana.record=true` rewrites it). Needs cobc and `corpus/fetch.sh carddemo`.
 */
@Tag("integration")
class CardDemoLaneIntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT;
    private static final List<String> JOBS = List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "CREASTMT", "PRTCATBL", "PRTCATBL-INLINE", "COMBTRAN", "SORTTEST");
    private final LaneRuns lane = new LaneRuns(CobcLane.Sort.JAVA, "lane");

    private Report run(String job) {
        return lane.run(job);
    }

    @Test
    void theCardDemoJobsRunAsTheGoldenSays() throws Exception {
        StringBuilder all = new StringBuilder();
        for (String job : JOBS) all.append("# oracle/fixture/carddemo/").append(job).append(".fixture\n").append(run(job).text()).append('\n');
        System.out.print(all);
        Path golden = ROOT.resolve("oracle/lane/carddemo.golden");
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, all.toString());
        assertEquals(Files.readString(golden), all.toString());
    }

    @Test
    void posttranReproducesTheFrozenTranfileUnload() {
        Report posttran = run("POSTTRAN");
        assertEquals(List.of(), posttran.findings());
        assertEquals("CBTRN02C rc=4", posttran.steps().get("STEP15"));
        assertEquals("sha256:aed617444207a7ebd2684e589c8569f766fb5d1d8323a25a658f8c4c03020b48",
                posttran.datasets().get("AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS"));
    }

    @Test
    void intcalcRunsThroughTheParmDriver() {
        Report intcalc = run("INTCALC");
        assertEquals(List.of(), intcalc.findings());
        assertTrue(intcalc.steps().get("STEP15").matches("CBACT04C rc=\\d+"), intcalc.text());
    }

    @Test
    void tranreptStudyReproducesTheFrozenSortAndReport() {
        Report study = run("TRANREPT-STUDY");
        assertEquals(List.of(), study.findings(), study.text());
        assertEquals("sha256:a7d14a96d9c6c4c90985d95185804bcb5fb09fabfe88bac74bb6f882da82dd96", study.datasets().get("AWS.M2.CARDDEMO.TRANSACT.DALY.G0001V00"));
        assertEquals("sha256:19b7f04fad9501a2ce8e1d143857a4ba928d0bbdbf78129ee98d23c2de5fa0e7", study.datasets().get("AWS.M2.CARDDEMO.TRANREPT.G0001V00"));
    }

    @Test
    void combtranAndSorttestRunEveryStep() {
        for (String job : List.of("COMBTRAN", "SORTTEST")) {
            Report report = run(job);
            assertEquals(List.of(), report.findings(), report.text());
            report.steps().values().forEach(outcome -> assertTrue(outcome.endsWith("rc=0"), job + " " + outcome));
        }
        assertTrue(run("COMBTRAN").text().contains("dataset AWS.M2.CARDDEMO.TRANSACT.COMBINED.G0001V00 records=307 "), "POSTTRAN's 257 and INTCALC's 50");
    }

    @Test
    void anOutrecLengthSortoutDoesNotHoldStopsTheJob() {
        assertEquals(List.of("step STEP010: SORT: records of 328 bytes for SORTOUT's lrecl 350"), run("CREASTMT").findings());
        assertEquals(List.of("step STEP10R: SORT: records of 41 bytes for SORTOUT's lrecl 40"), run("PRTCATBL-INLINE").findings());
    }

    @Test
    void tranreptAndPrtcatblAsWrittenStopAtReproctsColumnOneComment() {
        for (String job : List.of("TRANREPT", "PRTCATBL")) {
            Report report = run(job);
            assertEquals(1, report.findings().size(), report.text());
            assertTrue(report.findings().get(0).startsWith("step STEP05R.PRC001: IDCAMS: card 1 opens a comment in column 1"), report.text());
        }
    }
}
