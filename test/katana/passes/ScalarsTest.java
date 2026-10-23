package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.intent.rules.Rules;
import katana.lower.Lower;
import katana.sir.types.Item;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.3 on programs small enough to read (research §39, R-068): an item each obligation keeps, an item each one refuses, and the Java a promoted one becomes. */
class ScalarsTest {
    private static Program program(String data, String procedure) {
        return katana.support.Small.program(data, "MAIN.\n" + procedure + "    STOP RUN.\n");
    }

    private static int id(Program p, String name) {
        return p.items().stream().filter(i -> i.name().equals(name)).mapToInt(Item::id).findFirst().orElseThrow();
    }

    @Test
    void aCounterAndATotalBecomeVariablesEveryStoreExact() {
        Program p = program("01 WS-COUNT PIC 9(3) VALUE 5.\n01 WS-TOTAL PIC S9(5)V99 COMP-3.\n",
                "    ADD 1 TO WS-COUNT.\n    COMPUTE WS-TOTAL = WS-COUNT / 3.\n    MOVE 7 TO WS-COUNT.\n    MOVE 1000 TO WS-COUNT.\n    IF WS-COUNT > 2 DISPLAY WS-COUNT WS-TOTAL.\n");
        Structuring.Result r = Structuring.plan(p, Rules.infer(p));
        int count = id(p, "WS-COUNT"), total = id(p, "WS-TOTAL");
        assertEquals(Set.of(count, total), r.plan().scalars(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("private long wsCount;") && java.contains("private java.math.BigDecimal wsTotal"), java);
        assertFalse(java.contains("private Field f" + count + ";") || java.contains("private Field f" + total + ";"), "the promoted items' fields are gone");
        assertTrue(java.contains("wsCount = 5;") && java.contains("wsCount = 7;"), "a literal the item holds as written is assigned");
        assertTrue(java.contains("wsCount = Scalars.longOf(Scalars.received(A" + count + ", cp, scratch -> Move.move(Literal.number(\"1000\""), "a literal it would truncate goes through MOVE's own truncation");
        assertTrue(java.contains("sz |= Scalars.store(") && java.contains("stored -> wsCount = Scalars.longOf(stored)"), "an arithmetic store keeps Decimal.store's truncation and size error (R-068)");
        assertTrue(java.contains("Scalars.field(A" + count + ", cp, Scalars.of(wsCount, 0))"), "a DISPLAY reads a field the value is encoded into");
    }

    /** Research §40: the verbs that write it through a field of its own, and CALL BY CONTENT, which only reads it. */
    @Test
    void initializeSetStringUnstringAcceptFromDateAndCallByContentPromote() {
        Program p = program("01 A PIC 9(3) VALUE 5.\n01 B PIC 9(3).\n   88 B-DONE VALUE 9.\n01 C PIC 99 VALUE 1.\n01 D PIC 9(6).\n01 E PIC 9(4).\n01 S PIC X(20).\n",
                "    INITIALIZE A.\n    SET B-DONE TO TRUE.\n    SET A UP BY 2.\n    STRING 'AB' DELIMITED BY SIZE INTO S WITH POINTER C.\n"
                        + "    UNSTRING S DELIMITED BY ' ' INTO E.\n    ACCEPT D FROM DATE.\n    CALL 'SUB' USING BY CONTENT A.\n    DISPLAY A B C D E.\n");
        Structuring.Result r = Structuring.plan(p, Rules.infer(p));
        assertEquals(Set.of(id(p, "A"), id(p, "B"), id(p, "C"), id(p, "D"), id(p, "E")), r.plan().scalars(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        int a = id(p, "A");
        assertTrue(java.contains("Field p" + a + " = Scalars.field(A" + a + ", cp, Scalars.of(a, 0));") && java.contains("a = Scalars.longOf(p" + a + ".get());"), "INITIALIZE runs on a field of A's own, its value taken back: " + java);
        assertTrue(java.contains("RunUnit.content(Scalars.field(A" + a + ", cp, Scalars.of(a, 0)))"), "CALL BY CONTENT copies a field the value is encoded into");
    }

    /** Each obligation broken on purpose: the item keeps its bytes, and the pass says why. */
    @Test
    void eachObligationBrokenKeepsTheItemsBytes() {
        assertRefused("01 G.\n   05 N PIC 9(3).\n01 R REDEFINES G PIC X(3).\n", "    ADD 1 TO N.\n", "redefines");
        assertRefused("01 G.\n   05 N PIC 9(3) OCCURS 3.\n", "    ADD 1 TO N (1).\n", "occurs");
        assertRefused("01 G.\n   05 N PIC 9(3).\n01 X PIC X(3).\n", "    ADD 1 TO N.\n    MOVE G TO X.\n", "a group holding it");
        assertRefused("01 G VALUE '123'.\n   05 N PIC 9(3).\n", "    ADD 1 TO N.\n", "has a VALUE");
        assertRefused("01 N PIC 9(3).\n", "    PERFORM VARYING N FROM 1 BY 1 UNTIL N > 3\n        DISPLAY N\n    END-PERFORM.\n", "varied by a PERFORM");
        assertRefused("01 N PIC 9(3).\n", "    ADD 1 TO N.\n    DISPLAY N (1:1).\n", "reference-modified");
        assertRefused("01 N PIC 9(3)PP.\n", "    ADD 1 TO N.\n", "P");
        assertRefused("01 N PIC 9(19).\n", "    ADD 1 TO N.\n", "18");
        assertRefused("01 N PIC 9(3) COMP-5.\n", "    ADD 1 TO N.\n", "usage");
        assertRefused("01 N PIC 9(3).\n", "    DISPLAY 'X'.\n", "never referenced");
        assertRefused("01 N PIC 9(3).\n", "    ADD 1 TO N.\n    CALL 'SUB' USING N.\n", "named by a file, a table or a CALL");
        assertRefused("01 N PIC 9(3).\n", "    ACCEPT N.\n", "input device");
        assertRefused("01 N PIC 99.\n01 T PIC X(10) VALUE 'AB'.\n01 S PIC X(20).\n", "    STRING T DELIMITED BY SIZE INTO S WITH POINTER N\n        ON OVERFLOW DISPLAY 'O'\n    END-STRING.\n", "ON OVERFLOW");
        assertRefused("01 G EXTERNAL.\n   05 N PIC 9(3).\n", "    ADD 1 TO N.\n", "EXTERNAL");
        assertRefused("01 G GLOBAL.\n   05 N PIC 9(3).\n", "    ADD 1 TO N.\n", "GLOBAL");
    }

    private static void assertRefused(String data, String procedure, String why) {
        Program p = program(data, procedure);
        Map<String, List<String>> refused = new LinkedHashMap<>();
        Set<Integer> promoted = Scalars.promote(p, refused);
        assertFalse(promoted.contains(id(p, "N")), data + procedure);
        assertTrue(refused.getOrDefault("item N", List.of()).stream().anyMatch(w -> w.contains(why)), why + ": " + refused);
    }

    /**
     * A statement through a promoted subscript, with a phrase whose statement goes through another promoted item
     * (J-231): the phrase is lowered after the statement's block closes, so the two blocks follow each other, each
     * item its own field once. The review's nesting (a phrase inside the block) does not arise.
     */
    @Test
    void aPhrasesStatementGoesThroughItsOwnItemsAfterTheStatementsBlock() {
        Program p = program("01 I PIC 9 VALUE 1.\n01 K PIC 9(3) VALUE 0.\n01 A PIC X(5) VALUE 'HELLO'.\n01 G.\n   05 T PIC X(3) OCCURS 2.\n",
                "    STRING A DELIMITED BY SIZE INTO T (I) ON OVERFLOW SET K UP BY 1 END-STRING.\n    DISPLAY K.\n");
        Structuring.Result r = Structuring.plan(p, Rules.infer(p));
        int i = p.items().stream().filter(x -> x.name().equals("I")).findFirst().orElseThrow().id(), k = p.items().stream().filter(x -> x.name().equals("K")).findFirst().orElseThrow().id();
        assertTrue(r.plan().scalars().containsAll(java.util.Set.of(i, k)), "I and K promoted: " + r.refused());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("Field p" + i + " = ") && java.contains("Field p" + k + " = "), java);
    }
}
