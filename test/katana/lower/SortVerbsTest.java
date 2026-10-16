package katana.lower;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** SORT, RELEASE and RETURN lowered (research §23, J-184), in a program that names an item CHAR, which IBM does not reserve (D-032). */
class SortVerbsTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";

    @Test
    void aSortWithProceduresLowersAndCharIsAName() {
        List<String> lines = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. SRT.", "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
                "    SELECT SFILE ASSIGN TO SRTWK.", "DATA DIVISION.", "FILE SECTION.", "SD SFILE.", "01 SREC.", "   02 K1 PIC X(4).", "   02 CHAR PIC X(6).",
                "WORKING-STORAGE SECTION.", "01 W PIC X(10).", "PROCEDURE DIVISION.", "P1.",
                "    SORT SFILE ON DESCENDING KEY K1", "      INPUT PROCEDURE IS P2 OUTPUT PROCEDURE IS P3.", "    STOP RUN.",
                "P2.", "    MOVE \"X\" TO CHAR.", "    RELEASE SREC FROM W.", "P3.", "    RETURN SFILE INTO W AT END DISPLAY \"E\".");
        Program p = Build.program(Parse.text(lines.stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8)), HASH).program();
        Lower.Result r = Lower.java(p, "Srt");
        assertTrue(r.findings().isEmpty(), r.findings().toString());
        assertTrue(r.java().contains("file_SFILE.begin(List.of(new SortFile.Key("), r.java());
        assertTrue(r.java().contains(", true)), co);"), "DESCENDING, and the program's collating sequence");
        assertTrue(r.java().contains("file_SFILE.sort();") && r.java().contains("file_SFILE.release(") && r.java().contains("file_SFILE.returnNext("), r.java());
        assertFalse(r.java().contains("not lowered"), r.java());
    }
}
