package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** T-F1 on programs small enough to read: one entry, one exit, no GO TO out, each kept and broken (research §33). */
class FunctionRangeStructureTest {
    private static Program program(String procedure) {
        String source = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. P.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n01 I PIC 9.\nPROCEDURE DIVISION.\n" + procedure)
                .lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
    }

    private static List<String> refused(Program p, String subject) {
        return FunctionRangeStructure.refusals(p, FunctionRangeStructure.range(p, subject), List.of());
    }

    @Test
    void aRangeOnlyAPerformEntersAndThatLeavesAtItsEndHolds() {
        Program p = program("MAIN.\n    PERFORM A.\n    PERFORM B THRU B-EXIT.\n    STOP RUN.\nA.\n    DISPLAY 'A'.\nB.\n    IF I = 1 GO TO B-EXIT.\n    DISPLAY 'B'.\nB-EXIT.\n    EXIT.\n");
        assertEquals(List.of(), refused(p, "A"));
        assertEquals(List.of(), refused(p, "B THRU B-EXIT"));
    }

    @Test
    void aMainLineThatFallsInAGoToOutAndAGoToInAreEachRefused() {
        Program falls = program("MAIN.\n    PERFORM A.\nA.\n    DISPLAY 'A'.\nZ.\n    STOP RUN.\n");
        assertTrue(refused(falls, "A").stream().anyMatch(w -> w.contains("main line")), refused(falls, "A").toString());
        Program out = program("MAIN.\n    PERFORM A.\n    STOP RUN.\nA.\n    GO TO Z.\nZ.\n    STOP RUN.\n");
        assertTrue(refused(out, "A").stream().anyMatch(w -> w.contains("leaves it")), refused(out, "A").toString());
        Program in = program("MAIN.\n    PERFORM A THRU A2.\n    GO TO A2.\nA.\n    DISPLAY 'A'.\nA2.\n    STOP RUN.\n");
        assertTrue(refused(in, "A THRU A2").stream().anyMatch(w -> w.contains("enters it")), refused(in, "A THRU A2").toString());
    }

    @Test
    void anExitProgramFallsOnlyWhereTheProgramIsNoSubprogram() {
        Program main = program("MAIN.\n    PERFORM A.\n    EXIT PROGRAM.\nA.\n    DISPLAY 'A'.\n");
        assertTrue(refused(main, "A").stream().anyMatch(w -> w.contains("main line")), refused(main, "A").toString());
    }
}
