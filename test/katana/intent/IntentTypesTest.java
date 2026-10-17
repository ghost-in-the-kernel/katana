package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.Invalid;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** intent/types: each INT invariant refuses a violation, a program's facts are checked against its IR (INT-07), and a refuted fact is never actionable (R-029). */
class IntentTypesTest {
    private static final String HASH = "sha256:" + "0".repeat(64);
    private static final Evidence PERSON = new Evidence("person", 1, List.of(new Node.ItemNode(1)));

    /** A program with an enum, a REDEFINES and two paragraphs: item 1 STATUS-CODE (88 OK, 88 DONE), 2 AMOUNT, 3 VIEW over AMOUNT; paragraph 0 the procedure's unnamed start, 1 MAIN-PARA, 2 ADD-PARA. */
    private static Program program() {
        String source = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. P.", "DATA DIVISION.", "WORKING-STORAGE SECTION.",
                "01 STATUS-CODE PIC 9.", "   88 OK VALUE 0.", "   88 DONE VALUE 1.", "01 AMOUNT PIC S9(7)V99 COMP-3.", "01 VIEW REDEFINES AMOUNT PIC X(5).",
                "PROCEDURE DIVISION.", "MAIN-PARA.", "    PERFORM ADD-PARA.", "    STOP RUN.", "ADD-PARA.", "    ADD 1 TO AMOUNT.")
                .stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), HASH).program();
    }

    private static Fact fact(Kind kind, Subject subject) {
        return new Fact(kind, subject, PERSON, 1, State.INFERRED);
    }

    private static List<Fact> facts() {
        return List.of(fact(Kind.ENUM, new Subject.Choice(1, List.of("OK", "DONE"))), fact(Kind.MONEY, new Subject.Datum(2)),
                fact(Kind.TAGGED_UNION, new Subject.Union(2, List.of(3), 0)), fact(Kind.FUNCTION, new Subject.Range(2, 2)),
                fact(Kind.REDUCIBLE, new Subject.Paragraphs(List.of(0, 1, 2))), fact(Kind.STATELESS, new Subject.Whole("P")),
                new Fact(Kind.COUNTER, new Subject.Datum(1), new Evidence("person", 1, List.of(new Node.StatementNode(2, 0), new Node.EdgeNode(0), new Node.ParagraphNode(2))), 0.5, State.INFERRED));
    }

    private static void refused(String invariant, Supplier<Object> build) {
        Invalid e = assertThrows(Invalid.class, build::get);
        assertTrue(e.getMessage().startsWith(invariant + ": "), e.getMessage());
    }

    @Test
    void aProgramsFactsAreAcceptedAgainstItsIr() {
        Program p = program();
        assertEquals(List.of("STATUS-CODE", "OK", "DONE"), List.of(p.items().get(0).name(), p.items().get(0).conditions().get(0).name(), p.items().get(0).conditions().get(1).name()));
        new Intent("P", HASH, facts()).against(p);
        assertEquals("enum item 1 OK,DONE", facts().get(0).id());
    }

    @Test
    void eachInvariantRefusesAViolation() {
        refused("INT-01", () -> fact(Kind.ENUM, new Subject.Datum(1)));
        refused("INT-01", () -> fact(Kind.FUNCTION, new Subject.Paragraphs(List.of(1))));
        refused("INT-02", () -> new Subject.Datum(0));
        refused("INT-02", () -> new Subject.Choice(1, List.of("OK")));
        refused("INT-02", () -> new Subject.Choice(1, List.of("OK", "OK")));
        refused("INT-02", () -> new Subject.Union(2, List.of(2), 0));
        refused("INT-02", () -> new Subject.Union(2, List.of(3), 3));
        refused("INT-02", () -> new Subject.Range(2, 1));
        refused("INT-02", () -> new Subject.Paragraphs(List.of(1, 0)));
        refused("INT-02", () -> new Subject.Whole(" "));
        refused("INT-03", () -> new Evidence("", 1, List.of(new Node.ItemNode(1))));
        refused("INT-03", () -> new Evidence("r", 0, List.of(new Node.ItemNode(1))));
        refused("INT-03", () -> new Evidence("r", 1, List.of()));
        refused("INT-03", () -> new Node.StatementNode(0, -1));
        refused("INT-04", () -> new Fact(Kind.MONEY, new Subject.Datum(2), PERSON, 0, State.INFERRED));
        refused("INT-04", () -> new Fact(Kind.MONEY, new Subject.Datum(2), PERSON, 1.5, State.INFERRED));
        refused("INT-05", () -> new State(State.Status.CONFIRMED, "Ghost", "yesterday"));
        refused("INT-05", () -> new State(State.Status.REFUTED, "", "2026-10-17"));
        refused("INT-05", () -> new State(State.Status.INFERRED, "Ghost", ""));
        refused("INT-06", () -> new Intent("P", "sha256:nope", List.of()));
        refused("INT-06", () -> new Intent("P", HASH, List.of(fact(Kind.MONEY, new Subject.Datum(2)), fact(Kind.MONEY, new Subject.Datum(2)))));
    }

    @Test
    void factsNamingWhatTheProgramDoesNotHoldAreRefused() {
        Program p = program();
        refused("INT-07", () -> { new Intent("Q", HASH, List.of()).against(p); return null; });
        refused("INT-07", () -> { new Intent("P", "sha256:" + "1".repeat(64), List.of()).against(p); return null; });
        for (Fact wrong : List.of(fact(Kind.MONEY, new Subject.Datum(9)), fact(Kind.ENUM, new Subject.Choice(1, List.of("OK", "LATER"))),
                fact(Kind.TAGGED_UNION, new Subject.Union(1, List.of(3), 0)), fact(Kind.FUNCTION, new Subject.Range(2, 3)),
                fact(Kind.REDUCIBLE, new Subject.Paragraphs(List.of(5))), fact(Kind.STATELESS, new Subject.Whole("Q")),
                new Fact(Kind.MONEY, new Subject.Datum(2), new Evidence("r", 1, List.of(new Node.StatementNode(2, 1))), 1, State.INFERRED),
                new Fact(Kind.MONEY, new Subject.Datum(2), new Evidence("r", 1, List.of(new Node.EdgeNode(1))), 1, State.INFERRED))) {
            refused("INT-07", () -> { new Intent("P", HASH, List.of(wrong)).against(p); return null; });
        }
    }

    @Test
    void aRefutedFactIsNeverActionableAndAnInferredOneIsUnconfirmed() {
        Fact confirmed = new Fact(Kind.MONEY, new Subject.Datum(2), PERSON, 1, new State(State.Status.CONFIRMED, "Ghost", "2026-10-17"));
        Fact refuted = new Fact(Kind.FLAG, new Subject.Datum(1), PERSON, 1, new State(State.Status.REFUTED, "Ghost", "2026-10-17"));
        Fact inferred = fact(Kind.COUNTER, new Subject.Datum(1));
        assertEquals(List.of(confirmed, inferred), new Intent("P", HASH, List.of(confirmed, refuted, inferred)).actionable());
        assertEquals(List.of(false, true), List.of(confirmed.unconfirmed(), inferred.unconfirmed()));
    }
}
