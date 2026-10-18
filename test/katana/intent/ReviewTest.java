package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.review.Review;
import katana.intent.rules.Rules;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.Kind;
import katana.intent.types.State;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** The review file (research §30, R-064): a person's word kept across a rebuild, by the program's names. */
class ReviewTest {
    private static final String HASH = "sha256:" + "0".repeat(64);
    private static final String DATA = """
            01 STATUS-CODE PIC X.
               88 S-A VALUE 'A'.
               88 S-B VALUE 'B'.
            01 FLAG-X PIC X.
               88 F-Y VALUE 'Y'.
               88 F-N VALUE 'N'.
            01 GRP.
               05 TOTAL PIC S9(7)V99 COMP-3.
            01 OTHER-GRP.
               05 TOTAL PIC S9(7)V99 COMP-3.
            01 COUNT-X PIC 9(5).
            """;
    private static final String PROCEDURE = """
            MAIN.
                ADD TOTAL OF OTHER-GRP TO TOTAL OF GRP.
                ADD TOTAL OF GRP TO TOTAL OF OTHER-GRP.
                PERFORM WORK.
                STOP RUN.
            WORK.
                MOVE 'A' TO STATUS-CODE.
            """;

    private static Program program(String data, String procedure) {
        String source = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. P.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + procedure)
                .lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), HASH).program();
    }

    private static Review.Result rebuild(Program p, String review) {
        return Review.merge(p, Rules.infer(p), review);
    }

    /** A person's edits to a first review: money confirmed, a flag refuted, a function confirmed, a counter of their own. */
    private static String edited(String first) {
        return first.replace("inferred | money | TOTAL OF GRP | money.used v2 | 0.9", "confirmed | money | TOTAL OF GRP | money.used v2 | 0.9 | Ghost 2026-10-17")
                .replace("inferred | flag | FLAG-X | flag.yes-no v1 | 0.9", "refuted | flag | FLAG-X | flag.yes-no v1 | 0.9 | Ghost 2026-10-17 | the trial's refutation")
                .replace("inferred | function | WORK | function.perform-range v1 | 1", "confirmed | function | WORK | function.perform-range v1 | 1 | Ghost 2026-10-17")
                + "confirmed | counter | COUNT-X | person v1 | 1 | Ghost 2026-10-17\n";
    }

    private static State state(Review.Result r, Kind kind, String id) {
        return r.intent().facts().stream().filter(f -> f.kind() == kind && f.id().equals(id)).findFirst().orElseThrow().state();
    }

    @Test
    void aFirstReviewNamesEveryFactByTheProgramsNames() {
        String text = rebuild(program(DATA, PROCEDURE), "").text();
        for (String line : List.of("inferred | enum | STATUS-CODE: S-A, S-B | enum.disjoint-88s v2 | 0.9", "inferred | flag | FLAG-X | flag.yes-no v1 | 0.9", "inferred | money | TOTAL OF GRP | money.used v2 | 0.9",
                "inferred | money | TOTAL OF OTHER-GRP | money.used v2 | 0.9", "inferred | function | WORK | function.perform-range v1 | 1")) {
            assertTrue(text.contains(line + "\n"), line + " in\n" + text);
        }
    }

    @Test
    void aPersonsWordSurvivesARebuildAndARenumbering() {
        Program p = program(DATA, PROCEDURE);
        String review = edited(rebuild(p, "").text());
        Review.Result again = rebuild(p, review);
        assertEquals(List.of(), again.reported());
        assertEquals(State.Status.CONFIRMED, state(again, Kind.MONEY, "money item 4").status());
        assertEquals(State.Status.REFUTED, state(again, Kind.FLAG, "flag item 2").status());
        assertEquals(State.Status.CONFIRMED, state(again, Kind.COUNTER, "counter item 7").status());
        assertTrue(again.text().contains("refuted | flag | FLAG-X | flag.yes-no v1 | 0.9 | Ghost 2026-10-17 | the trial's refutation\n"), again.text());
        Program renumbered = program("01 ADDED-ABOVE PIC X.\n" + DATA, PROCEDURE);
        Review.Result moved = rebuild(renumbered, again.text());
        assertEquals(again.text(), moved.text());
        assertEquals(State.Status.CONFIRMED, state(moved, Kind.MONEY, "money item 5").status());
        assertEquals(rebuild(p, again.text()).text(), again.text());
    }

    @Test
    void aRenamedSubjectsLineIsOrphanedNeverRebound() {
        Program p = program(DATA, PROCEDURE);
        String review = rebuild(p, edited(rebuild(p, "").text())).text();
        Review.Result renamed = rebuild(program(DATA, PROCEDURE.replace("WORK", "WORK-2")), review);
        assertTrue(renamed.text().contains("orphaned | confirmed | function | WORK | function.perform-range v1 | 1 | Ghost 2026-10-17\n"), renamed.text());
        assertTrue(renamed.text().contains("inferred | function | WORK-2 | function.perform-range v1 | 1\n"), renamed.text());
        assertEquals(1, renamed.reported().size(), renamed.reported().toString());
        Review.Result later = rebuild(program(DATA, PROCEDURE.replace("WORK", "WORK-2")), renamed.text());
        assertTrue(later.text().contains("orphaned | confirmed | function | WORK |"), later.text());
    }

    @Test
    void aRefutedFactComesBackOnlyUnderANewerRuleVersion() {
        Program p = program(DATA, PROCEDURE);
        String review = rebuild(p, edited(rebuild(p, "").text())).text();
        List<Fact> v2 = new ArrayList<>();
        for (Fact f : Rules.infer(p).facts()) {
            v2.add(f.kind() != Kind.FLAG ? f : new Fact(f.kind(), f.subject(), new Evidence(f.evidence().rule(), 2, f.evidence().nodes()), f.confidence(), f.state()));
        }
        Review.Result newer = Review.merge(p, new Intent("P", HASH, v2), review);
        assertEquals(State.Status.INFERRED, state(newer, Kind.FLAG, "flag item 2").status());
        assertTrue(newer.text().contains("inferred | flag | FLAG-X | flag.yes-no v2 | 0.9 |  | was refuted by Ghost 2026-10-17 under flag.yes-no v1: the trial's refutation\n"), newer.text());
        assertEquals(List.of(), new Intent("P", HASH, newer.intent().actionable()).facts().stream().filter(f -> f.state().status() == State.Status.REFUTED).toList());
    }

    @Test
    void aLineThatNamesNothingIsKeptAndSaid() {
        Program p = program(DATA, PROCEDURE);
        Review.Result r = rebuild(p, rebuild(p, "").text() + "confirmed | money | NO-SUCH-ITEM | person v1 | 1 | Ghost 2026-10-17\nconfirmed | gold | X\n");
        assertEquals(2, r.reported().size(), r.reported().toString());
        assertTrue(r.text().contains("orphaned | confirmed | money | NO-SUCH-ITEM | person v1 | 1 | Ghost 2026-10-17\n"), r.text());
        assertTrue(r.text().contains("orphaned | confirmed | gold | X\n"), r.text());
    }

    @Test
    void aFactTheProgramHasNoNameForIsSaidNotWritten() {
        Program p = program("01 FILLER PIC X.\n   88 A-1 VALUE 'A'.\n   88 A-2 VALUE 'B'.\n", "MAIN.\n    STOP RUN.\n");
        Review.Result r = rebuild(p, "");
        assertEquals(1, r.reported().size(), r.reported().toString());
        assertTrue(!r.text().contains("| enum |"), r.text());
    }
}
