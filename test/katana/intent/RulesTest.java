package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.rules.Rule;
import katana.intent.rules.Rules;
import katana.intent.types.Fact;
import katana.intent.types.Kind;
import katana.intent.types.Subject;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** intent/rules on programs small enough to read: each condition a rule names, kept and broken (research §29). */
class RulesTest {
    private static final String HASH = "sha256:" + "0".repeat(64);

    private static Program program(String data, String procedure) {
        String source = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. P.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + procedure)
                .lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), HASH).program();
    }

    /** The names of what a program's facts of one kind are about: items by name, ranges as `FIRST` or `FIRST THRU LAST`. */
    private static List<String> about(Program p, Kind kind) {
        return Rules.infer(p).facts().stream().filter(f -> f.kind() == kind).map(f -> name(p, f)).toList();
    }

    private static String name(Program p, Fact f) {
        return switch (f.subject()) {
            case Subject.Datum d -> p.items().get(d.item() - 1).name();
            case Subject.Choice c -> p.items().get(c.item() - 1).name();
            case Subject.Range r -> p.procedure().paragraphs().get(r.first()).name() + (r.last() == r.first() ? "" : " THRU " + p.procedure().paragraphs().get(r.last()).name());
            default -> f.subject().text();
        };
    }

    @Test
    void anItemWhoseConditionNamesShareNoValueIsAnEnum() {
        Program p = program("""
                01 KIND PIC X.
                   88 KIND-A VALUE 'A'.
                   88 KIND-B VALUE 'B' 'C'.
                01 SHARED PIC X.
                   88 SHARED-A VALUE 'A' 'B'.
                   88 SHARED-B VALUE 'B'.
                01 SPACED PIC X.
                   88 SPACED-BLANK VALUE SPACE.
                   88 SPACED-SPACE VALUE ' '.
                01 RANGED PIC 99.
                   88 LOW-DAYS VALUE 1 THRU 9.
                   88 HIGH-DAYS VALUE 20 THRU 31.
                01 ONE PIC X.
                   88 ONE-Y VALUE 'Y'.
                """, "MAIN.\n    STOP RUN.\n");
        assertEquals(List.of("KIND"), about(p, Kind.ENUM));
    }

    @Test
    void aPackedV99ItemAnAddStoresIntoAndNoneSquaresIsMoney() {
        Program p = program("""
                01 TOTAL PIC S9(7)V99 COMP-3.
                01 MOVED PIC S9(7)V99 COMP-3.
                01 ZONED PIC S9(7)V99.
                01 SQUARED PIC S9(7)V99 COMP-3.
                01 TAKEN PIC S9(7)V99 COMP-3.
                01 RATE PIC S9(3)V9999 COMP-3.
                """, """
                MAIN.
                    ADD 1 TO TOTAL ZONED SQUARED RATE.
                    SUBTRACT 1 FROM TAKEN.
                    MOVE 1 TO MOVED.
                    COMPUTE SQUARED = SQUARED * SQUARED.
                    STOP RUN.
                """);
        assertEquals(List.of("TOTAL", "TAKEN"), about(p, Kind.MONEY));
    }

    @Test
    void aRangeOnlyAPerformEntersAndThatLeavesAtItsEndIsAFunction() {
        Program p = program("01 I PIC 9.\n", """
                MAIN.
                    PERFORM CLEAN.
                    PERFORM RANGE-A THRU RANGE-A-EXIT.
                    PERFORM LEAVES.
                    PERFORM JUMPED-INTO.
                    PERFORM ALTERED.
                    PERFORM SELF.
                    PERFORM OVER-A THRU OVER-B.
                    PERFORM OVER-B.
                    GO TO DONE.
                    EXIT.
                CLEAN.
                    MOVE 1 TO I.
                RANGE-A.
                    IF I = 1 GO TO RANGE-A-EXIT.
                    MOVE 2 TO I.
                RANGE-A-EXIT.
                    EXIT.
                LEAVES.
                    GO TO DONE.
                JUMPED-INTO.
                    MOVE 3 TO I.
                ALTERED.
                    GO TO DONE.
                SELF.
                    PERFORM SELF.
                OVER-A.
                    MOVE 4 TO I.
                OVER-B.
                    MOVE 5 TO I.
                ALTERER.
                    ALTER ALTERED TO PROCEED TO LEAVES.
                    GO TO JUMPED-INTO.
                DONE.
                    STOP RUN.
                """);
        assertEquals(List.of("CLEAN", "RANGE-A THRU RANGE-A-EXIT"), about(p, Kind.FUNCTION));
    }

    @Test
    void aParagraphTheMainLineFallsIntoIsNoFunction() {
        Program main = program("01 I PIC 9.\n", "MAIN.\n    PERFORM NEXT-ONE.\nNEXT-ONE.\n    MOVE 1 TO I.\nLAST-ONE.\n    STOP RUN.\n");
        assertEquals(List.of(), about(main, Kind.FUNCTION));
    }

    @Test
    void everyRuleIsNamedForItsKindAndClaimsAConfidence() {
        for (Rule r : Rules.ALL) {
            String kind = r.name().substring(0, r.name().indexOf('.'));
            assertEquals(true, java.util.Arrays.stream(Kind.values()).anyMatch(k -> k.name().toLowerCase(java.util.Locale.ROOT).equals(kind)), r.name());
            assertEquals(true, r.version() >= 1 && r.confidence() > 0 && r.confidence() <= 1, r.name());
        }
    }
}
