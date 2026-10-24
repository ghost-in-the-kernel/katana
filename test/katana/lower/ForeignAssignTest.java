package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import org.junit.jupiter.api.Test;

/** R-090 for a file's ASSIGN: a ddname is IBM's, EXTERNAL and DYNAMIC are not; they are a finding, never a file named "EXTERNAL X" (J-282). */
class ForeignAssignTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";

    private static Lower.Result lower(String assign) {
        List<String> lines = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. ASG.", "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
                "    SELECT F ASSIGN " + assign + " ORGANIZATION IS SEQUENTIAL.", "DATA DIVISION.", "FILE SECTION.", "FD F.", "01 R PIC X(10).",
                "PROCEDURE DIVISION.", "P1.", "    OPEN INPUT F.", "    CLOSE F.", "    STOP RUN.");
        String text = lines.stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Lower.java(Build.program(Parse.text(text, new Copybooks(List.of(), n -> 8)), HASH).program(), "Asg");
    }

    @Test
    void aDdnameIsTheFilesNameAsBefore() {
        for (String assign : List.of("TO INFILE", "INFILE", "TO \"MY FILE\"", "TO EXTERNAL-X", "TO S-INFILE")) {
            Lower.Result r = lower(assign);
            assertTrue(r.findings().isEmpty(), assign + ": " + r.findings());
            assertTrue(r.java().contains("new SeqFile("), assign + ": " + r.java());
        }
    }

    @Test
    void anAssignIbmLacksIsAFindingWithNoFile() {
        for (String word : List.of("EXTERNAL", "DYNAMIC")) {
            Lower.Result r = lower("TO " + word + " OUTFILE");
            assertEquals(1, r.findings().size(), word + ": " + r.findings());
            String f = r.findings().get(0);
            assertTrue(f.contains("ASSIGN " + word + " OUTFILE") && f.contains("ddname") && f.contains("recollection, R-082"), f);
            assertTrue(r.java().contains("Unsupported.fail(") && !r.java().contains("new SeqFile("), r.java());
            assertFalse(r.java().contains("\"" + word + " OUTFILE\""), r.java());
        }
    }
}
