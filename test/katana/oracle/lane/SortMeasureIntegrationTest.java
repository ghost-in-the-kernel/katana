package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * R-010's measurement (docs/ROADMAP.md 1.5): TRANREPT-STUDY through the Java sort and through the
 * generated COBOL sort, each reproducing the frozen sort and report (F-02), with the time each run
 * took and which CardDemo SORT steps each sort can run. The numbers are printed for the journal;
 * the choice between the two is Ghost's.
 */
@Tag("integration")
class SortMeasureIntegrationTest {
    private static final String DALY = "sha256:a7d14a96d9c6c4c90985d95185804bcb5fb09fabfe88bac74bb6f882da82dd96";
    private static final String REPORT = "sha256:19b7f04fad9501a2ce8e1d143857a4ba928d0bbdbf78129ee98d23c2de5fa0e7";

    /** Each sort in its own folders: POSTTRAN first (its KSDS is TRANREPT-STUDY's input), TRANREPT-STUDY timed, then the other SORT jobs tried. */
    @Test
    void bothSortsReproduceTranreptStudyAndSayWhatElseTheyRun() {
        List<String> lines = new ArrayList<>();
        for (CobcLane.Sort sort : CobcLane.Sort.values()) {
            LaneRuns lane = new LaneRuns(sort, "measure-" + sort.name().toLowerCase());
            lane.run("POSTTRAN");
            long start = System.nanoTime();
            Report study = lane.run("TRANREPT-STUDY");
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertEquals(List.of(), study.findings(), sort + "\n" + study.text());
            assertEquals(DALY, study.datasets().get("AWS.M2.CARDDEMO.TRANSACT.DALY.G0001V00"), sort.name());
            assertEquals(REPORT, study.datasets().get("AWS.M2.CARDDEMO.TRANREPT.G0001V00"), sort.name());
            List<String> runs = new ArrayList<>();
            for (String job : List.of("COMBTRAN", "SORTTEST", "CREASTMT", "PRTCATBL-INLINE")) {
                Report r = lane.run(job);
                runs.add(job + (r.findings().isEmpty() ? " runs" : " stops: " + r.findings().get(0)));
            }
            lines.add("measure " + sort + " TRANREPT-STUDY " + ms + " ms; " + String.join("; ", runs));
        }
        lines.forEach(System.out::println);
    }
}
