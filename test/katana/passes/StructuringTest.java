package katana.passes;

import katana.lower.Plan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.rules.Rules;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.State;
import katana.lower.Lower;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.1 on programs small enough to read (research §35): each obligation kept and broken, the plan lower/ writes, and a refuted fact never acted on (R-029). */
class StructuringTest {
    private static Program program(String procedure) {
        return katana.support.Small.program("01 I PIC 9.\n", procedure);
    }

    private static final String CLEAN = "MAIN.\n    PERFORM A.\n    PERFORM B THRU B-EXIT UNTIL I = 3.\n    STOP RUN.\nA.\n    ADD 1 TO I.\nB.\n    IF I = 1 GO TO B-EXIT.\n    ADD 1 TO I.\nB-EXIT.\n    EXIT.\n";

    @Test
    void aRangeOnlyPerformedBecomesAMethodItsCallerCalls() {
        Program p = program(CLEAN);
        Structuring.Result r = Structuring.plan(p, Rules.infer(p));
        assertEquals(2, r.applied().size(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        int a = index(p, "A"), b = index(p, "B");
        assertTrue(java.contains("private void perform" + a + "()") && java.contains("    perform" + a + "();"), java);
        assertTrue(java.contains("while (!(") && java.contains("perform" + b + "();"), "PERFORM ... UNTIL of a method is a loop in the caller's stretch");
        assertFalse(java.contains("return d.perform("), "no caller's stretch ends at a PERFORM: each calls the method");
        assertEquals(Set.of(a, b), r.plan().inline(), "both ranges are their methods' own code (6.2): " + r.refused());
        int exit = index(p, "B-EXIT");
        assertTrue(java.contains("B" + exit + ": {") && java.contains("if (true) break B" + exit + ";"), "B's GO TO B-EXIT is a break out of the block that ends before B-EXIT");
        assertFalse(java.contains("private int s" + a + "()") || java.contains("case " + a + " ->"), "A's stretch has left the switch");
        assertTrue(r.applied().stream().allMatch(x -> x.fact().unconfirmed()), "the facts are no person's word: unconfirmed (R-029)");
        assertEquals(Lower.java(p, "P").java().replace("Literal", "x"), Lower.java(p, "P", Plan.LITERAL).java().replace("Literal", "x"), "the empty plan is the literal lowering");
    }

    /**
     * The pass is held to its obligations whatever the rule says: each program is given a Function fact on a
     * range that breaks one, as a later rule or a person's own fact might, and the pass refuses it.
     */
    @Test
    void eachObligationBrokenKeepsTheRangeLiteral() {
        assertRefused("MAIN.\n    PERFORM A THRU A2.\n    GO TO A2.\nA.\n    DISPLAY 'A'.\nA2.\n    STOP RUN.\n", "A", "A2", "enters");
        assertRefused("MAIN.\n    PERFORM A.\n    STOP RUN.\nA.\n    GO TO Z.\nZ.\n    STOP RUN.\n", "A", "A", "leaves");
        assertRefused("MAIN.\n    PERFORM A.\nA.\n    DISPLAY 'A'.\nZ.\n    STOP RUN.\n", "A", "A", "main line");
        assertRefused("MAIN.\n    PERFORM A.\n    STOP RUN.\nA.\n    PERFORM A.\n", "A", "A", "PERFORMs itself");
        assertRefused("MAIN.\n    PERFORM A THRU B.\n    PERFORM B.\n    STOP RUN.\nA.\n    DISPLAY 'A'.\nB.\n    DISPLAY 'B'.\n", "A", "B", "enters it otherwise");
    }

    private static void assertRefused(String procedure, String first, String last, String why) {
        Program p = program(procedure);
        katana.intent.types.Subject.Range range = new katana.intent.types.Subject.Range(index(p, first), index(p, last));
        Fact given = new Fact(katana.intent.types.Kind.FUNCTION, range, new katana.intent.types.Evidence("person", 1, List.of(new katana.intent.types.Node.ParagraphNode(range.first()))), 1, State.INFERRED);
        Structuring.Result r = Structuring.plan(p, new Intent("P", p.header().profileHash(), List.of(given)));
        assertEquals(List.of(), r.applied(), procedure);
        assertTrue(r.refused().getOrDefault(Structuring.name(p, range), List.of()).stream().anyMatch(w -> w.contains(why)), procedure + ": " + r.refused());
    }

    private static int index(Program p, String name) {
        return p.procedure().paragraphs().stream().filter(g -> g.name().equals(name)).findFirst().orElseThrow().index();
    }

    @Test
    void aRefutedFactIsNeverActedOn() {
        Program p = program(CLEAN);
        Intent inferred = Rules.infer(p);
        List<Fact> refuted = inferred.facts().stream().map(f -> new Fact(f.kind(), f.subject(), f.evidence(), f.confidence(), new State(State.Status.REFUTED, "Ghost", "2026-10-18"))).toList();
        Structuring.Result r = Structuring.plan(p, new Intent(inferred.program(), inferred.profileHash(), refuted));
        assertEquals(List.of(), r.applied());
        assertTrue(r.plan().methods().isEmpty());
    }

    @Test
    void aGoToTheNextParagraphFallsUnlessARangeEndsThere() {
        Program falls = program("MAIN.\n    DISPLAY 'M'.\n    GO TO Z.\nZ.\n    STOP RUN.\n");
        assertEquals(java.util.Set.of(falls.procedure().paragraphs().stream().filter(g -> g.name().equals("MAIN")).findFirst().orElseThrow().index()), Structuring.falls(falls));
        assertTrue(Lower.java(falls, "P", Structuring.plan(falls, Rules.infer(falls)).plan()).java().contains("left to fall"));
        Program ends = program("MAIN.\n    PERFORM A.\n    STOP RUN.\nA.\n    DISPLAY 'A'.\n    GO TO Z.\nZ.\n    EXIT.\n");
        int a = ends.procedure().paragraphs().stream().filter(g -> g.name().equals("A")).findFirst().orElseThrow().index();
        assertFalse(Structuring.falls(ends).contains(a), "a PERFORM range ends at A: its GO TO passes the range's end, a fall would not");
    }

    private static final String VARY = "01 J PIC 99.\n01 T PIC 9(4).\n";

    private static Program varying(String procedure) {
        String source = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. P.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n01 I PIC 99.\n" + VARY + "PROCEDURE DIVISION.\n" + procedure)
                .lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
    }

    @Test
    void aVaryingOfOneMonotoneCounterBecomesAFor() {
        Program p = varying("MAIN.\n    PERFORM VARYING I FROM 1 BY 1 UNTIL I > 5\n        ADD I TO T\n        PERFORM A\n    END-PERFORM.\n    PERFORM A VARYING J FROM 1 BY 2 UNTIL J > 9.\n    STOP RUN.\nA.\n    ADD 1 TO T.\n");
        Structuring.Result r = Structuring.plan(p, Rules.infer(p));
        assertEquals(2, r.plan().loops().size(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("for (; !("), java);
        assertFalse(java.contains("return d.perform("), "the loops' bodies call the method: no stretch ends at a PERFORM");
    }

    @Test
    void eachLoopObligationBrokenKeepsItStretches() {
        assertLoopRefused("MAIN.\n    PERFORM VARYING I FROM 1 BY 1 UNTIL I > 5\n        ADD 1 TO I\n    END-PERFORM.\n    STOP RUN.\n", "store into its counter");
        assertLoopRefused("MAIN.\n    PERFORM VARYING I FROM 1 BY 1 UNTIL I > 5 AFTER J FROM 1 BY 1 UNTIL J > 2\n        ADD 1 TO T\n    END-PERFORM.\n    STOP RUN.\n", "AFTER");
        assertLoopRefused("MAIN.\n    PERFORM VARYING I FROM 1 BY T UNTIL I > 5\n        ADD 1 TO T\n    END-PERFORM.\n    STOP RUN.\n", "BY");
        assertLoopRefused("MAIN.\n    PERFORM A VARYING I FROM 1 BY 1 UNTIL I > 5.\n    STOP RUN.\nA.\n    MOVE 9 TO I.\n", "store into its counter");
    }

    private static void assertLoopRefused(String procedure, String why) {
        Program p = varying(procedure);
        Structuring.Result r = Structuring.plan(p, Rules.infer(p));
        assertEquals(java.util.Set.of(), r.plan().loops(), procedure);
        assertTrue(r.refused().entrySet().stream().anyMatch(e -> e.getKey().startsWith("PERFORM VARYING") && e.getValue().stream().anyMatch(w -> w.contains(why))), procedure + ": " + r.refused());
    }

    @Test
    void aRangeThatLoopsOrIsDispatchedStaysOverTheSwitch() {
        Program loops = program("MAIN.\n    PERFORM A THRU A2.\n    STOP RUN.\nA.\n    ADD 1 TO I.\nA2.\n    IF I < 5 GO TO A.\n");
        Structuring.Result r = Structuring.plan(loops, Rules.infer(loops));
        int a = index(loops, "A");
        assertEquals(Set.of(a), r.plan().inline(), "a GO TO back is a loop the method's own code says (6.2b): " + r.refused());
        String java = Lower.java(loops, "P", r.plan()).java();
        assertTrue(java.contains("W" + a + ": while (true) {") && java.contains("if (true) continue W" + a + ";"), java);
        assertFalse(java.contains("private int step("), "main line and method both left the switch, so there is none: " + java);
        Program enclosed = program("MAIN.\n    PERFORM B.\n    PERFORM A THRU C.\n    STOP RUN.\nA.\n    DISPLAY 'A'.\nB.\n    DISPLAY 'B'.\nC.\n    DISPLAY 'C'.\n");
        katana.intent.types.Subject.Range b = new katana.intent.types.Subject.Range(index(enclosed, "B"), index(enclosed, "B"));
        Fact given = new Fact(katana.intent.types.Kind.FUNCTION, b, new katana.intent.types.Evidence("person", 1, List.of(new katana.intent.types.Node.ParagraphNode(b.first()))), 1, State.INFERRED);
        Structuring.Result e = Structuring.plan(enclosed, new Intent("P", enclosed.header().profileHash(), List.of(given)));
        assertTrue(e.plan().inline().isEmpty() && e.refused().values().stream().flatMap(List::stream).anyMatch(w -> w.contains("runs through it")), "a PERFORM A THRU C runs through B, so B's stretch must stay: " + e.refused() + " " + e.plan());
        Program varying = program("MAIN.\n    PERFORM A.\n    PERFORM A VARYING I FROM 1 BY 1 UNTIL I > 3 AFTER I FROM 1 BY 1 UNTIL I > 2.\n    STOP RUN.\nA.\n    DISPLAY 'A'.\n");
        Structuring.Result v = Structuring.plan(varying, Rules.infer(varying));
        assertTrue(v.plan().inline().isEmpty() && v.refused().values().stream().flatMap(List::stream).anyMatch(w -> w.contains("through the dispatcher")), v.refused() + " " + v.plan());
    }

    /**
     * 6.2b (research §38): a main line with a loop entered past its header, or reached by an ALTER, stays in the
     * switch, the program's `main line` finding saying why (R-034); a reducible one is `run`'s own code.
     */
    @Test
    void anIrreducibleOrAlteredMainLineStaysInTheSwitchWithAFinding() {
        Program twoEntries = program("MAIN.\n    IF I = 1 GO TO B.\nA.\n    ADD 1 TO I.\nB.\n    ADD 1 TO I.\n    IF I < 5 GO TO A.\n    STOP RUN.\n");
        Structuring.Result r = Structuring.plan(twoEntries, Rules.infer(twoEntries));
        assertTrue(r.plan().main().isEmpty() && r.refused().getOrDefault("main line", List.of()).stream().anyMatch(w -> w.contains("enters the loop")), r.refused() + " " + r.plan());
        assertTrue(Lower.java(twoEntries, "P", r.plan()).java().contains("private int step("), "the residual main line keeps its switch");
        Program altered = program("MAIN.\n    ALTER G TO PROCEED TO B.\nG.\n    GO TO A.\nA.\n    STOP RUN.\nB.\n    STOP RUN.\n");
        Structuring.Result a = Structuring.plan(altered, Rules.infer(altered));
        assertTrue(a.plan().main().isEmpty() && a.refused().getOrDefault("main line", List.of()).stream().anyMatch(w -> w.contains("ALTER")), a.refused() + " " + a.plan());
        Program reducible = program("MAIN.\n    ADD 1 TO I.\n    IF I < 5 GO TO MAIN.\n    STOP RUN.\n");
        Structuring.Result m = Structuring.plan(reducible, Rules.infer(reducible));
        int main = index(reducible, "MAIN");
        assertTrue(m.plan().main().contains(main), m.refused().toString());
        assertTrue(Lower.java(reducible, "P", m.plan()).java().contains("W" + main + ": while (true) {"), "a GO TO back on the main line is a labeled while");
    }
}
