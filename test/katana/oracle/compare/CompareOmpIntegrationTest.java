package katana.oracle.compare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import katana.oracle.lane.Gate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 1.7's check on the OMP course's EBCDIC data (45 records of 170 bytes; needs `corpus/fetch.sh
 * omp-cobol-course`): an injected one-byte change in a COMP-3 field is reported with the field name and
 * both decoded values, and a byte-blind `iconv` transcode is detected as corruption of the packed
 * fields while every text field compares the same. The data is no lane's output, so the gate is
 * passed by construction.
 */
@Tag("integration")
class CompareOmpIntegrationTest {
    private static final Path LABS = Path.of(System.getProperty("katana.root", "."), "corpus/omp-cobol-course/COBOL Programming Course #2 - Learning COBOL/Labs");
    private static final Gate.Result NOT_A_LANE = new Gate.Result(true, List.of());

    /** CBL0001's record layout: its FD's 01 level up to the next FD. */
    private static Layout layout() throws Exception {
        List<String> lines = Files.readAllLines(LABS.resolve("cbl/CBL0001.cobol"));
        List<String> record = new ArrayList<>();
        boolean in = false;
        for (String line : lines) {
            if (line.contains("01  ACCT-FIELDS.")) in = true;
            else if (in && line.matches("\\s{7}(FD|WORKING-STORAGE|PROCEDURE).*")) break;
            if (in) record.add(line);
        }
        return Layout.read(String.join("\n", record));
    }

    @Test
    void aOneByteChangeInACompThreeFieldIsNamedWithBothValues() throws Exception {
        Layout layout = layout();
        assertEquals(170, layout.length());
        byte[] data = Files.readAllBytes(LABS.resolve("data/data"));
        byte[] changed = data.clone();
        changed[4 * 170 + 15] ^= 0x10;
        Compare.Result result = Compare.compare(NOT_A_LANE, Compare.Side.fixed("OMP.DATA", data, 170, "cp037"),
                Compare.Side.fixed("OMP.DATA", changed, 170, "cp037"), layout, List.of(), Compare.Numproc.NOPFD);
        System.out.println("compare one byte: " + result.findings());
        assertEquals(1, result.findings().size());
        assertTrue(result.findings().get(0).matches("OMP\\.DATA ACCT-FIELDS\\.ACCT-BALANCE: 1 of 45 records differ; first at record 5, byte offset 693: X'\\w+' \\([-\\d.]+\\) then X'\\w+' \\([-\\d.]+\\), compared as bytes"),
                result.findings().get(0));
    }

    @Test
    void aByteBlindIconvIsCaughtAsCorruptPackedFields() throws Exception {
        Path in = LABS.resolve("data/data"), out = Files.createTempFile("omp-iconv", ".dat");
        Process iconv = new ProcessBuilder("iconv", "-f", "IBM037", "-t", "ISO-8859-1", in.toString()).redirectOutput(out.toFile()).start();
        assertEquals(0, iconv.waitFor());
        Compare.Result result = Compare.compare(NOT_A_LANE, Compare.Side.fixed("OMP.DATA", Files.readAllBytes(in), 170, "cp037"),
                Compare.Side.fixed("OMP.DATA", Files.readAllBytes(out), 170, "ascii"), layout(), List.of(), Compare.Numproc.NOPFD);
        System.out.println("compare iconv: " + result.findings());
        assertTrue(result.findings().stream().allMatch(f -> f.startsWith("OMP.DATA ACCT-FIELDS.ACCT-LIMIT:") || f.startsWith("OMP.DATA ACCT-FIELDS.ACCT-BALANCE:")), result.findings().toString());
        assertEquals(2, result.findings().size(), result.findings().toString());
    }
}
