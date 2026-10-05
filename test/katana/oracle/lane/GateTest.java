package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import java.nio.charset.StandardCharsets;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** How the gate names a difference between two runs' unloads (docs/ROADMAP.md 1.6). */
class GateTest {
    private static final Fixture FIXTURE = Fixture.read("step S pgm=P\ndd OUT dsn=A.B disp=SHR recfm=F lrecl=4 copybook=- encoding=ascii\n");

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    @Test
    void identicalUnloadsAreNoDifference() {
        assertNull(Gate.dataset(FIXTURE, "A.B", 4, bytes("AAA0BBB0"), bytes("AAA0BBB0")));
    }

    @Test
    void aDifferenceNamesTheDdsTheRecordsAndTheColumns() {
        assertEquals("dataset A.B (DD OUT in S): 2 of 3 records differ, in column 4",
                Gate.dataset(FIXTURE, "A.B", 4, bytes("AAA0BBB0CCC0"), bytes("AAA1BBB0CCC2")));
        assertEquals("dataset A.B (DD OUT in S): 1 of 2 records differ, in columns 1-2, 4",
                Gate.dataset(FIXTURE, "A.B", 4, bytes("AAA0BBB0"), bytes("XXA1BBB0")));
        assertEquals("dataset A.B (DD OUT in S): 2 records, then 1", Gate.dataset(FIXTURE, "A.B", 4, bytes("AAA0BBB0"), bytes("AAA0")));
        assertEquals("dataset A.B is in one run only", Gate.dataset(FIXTURE, "A.B", 4, bytes("AAA0"), null));
    }

    @Test
    void sysoutDdsAndStderrAreHeldWithTheRunFolderAsAToken(@TempDir Path dir) throws Exception {
        Path a = dir.resolve("a"), b = dir.resolve("b");
        for (Path work : List.of(a, b)) {
            Files.createDirectories(work.resolve("sysout"));
            Files.writeString(work.resolve("sysout/S.stderr"), "libcob: error: " + work.resolve("x.dat") + " (status = 35)\n");
        }
        Files.writeString(a.resolve("sysout/S.SYSPRINT"), "TOTAL 1\n");
        Files.writeString(b.resolve("sysout/S.SYSPRINT"), "TOTAL 2\n");
        Report report = new Report();
        Gate.Result result = Gate.compare(FIXTURE, report, a, report, b);
        assertEquals(List.of("S.SYSPRINT: line 1 differs"), result.differences());
        Files.writeString(b.resolve("sysout/S.stderr"), "libcob: warning\n");
        assertTrue(Gate.compare(FIXTURE, report, a, report, b).differences().contains("S.stderr: line 1 differs"));
    }
}
