package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.lower.Lower;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.5 on a program small enough to read (research §42, R-070): an Enum fact's names, a Money fact's type, each refusal, and a refuted fact never acted on. */
class TypesTest {
    private static final String SOURCE = "IDENTIFICATION DIVISION.\nPROGRAM-ID. P.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n"
            + "01 APPL-RESULT PIC S9(9) COMP.\n   88 APPL-AOK VALUE 0.\n   88 APPL-EOF VALUE 16.\n01 WS-STATUS PIC XX.\n   88 WS-OK VALUE '00'.\n   88 WS-EOF VALUE '10'.\n"
            + "01 WS-TOTAL PIC S9(7)V99.\n01 R.\n   05 R-AMT PIC S9(7)V99.\nPROCEDURE DIVISION.\nMAIN.\n    MOVE 8 TO APPL-RESULT.\n    SET APPL-AOK TO TRUE.\n"
            + "    MOVE 16 TO APPL-RESULT.\n    MOVE '00' TO WS-STATUS.\n    IF APPL-AOK AND WS-OK DISPLAY 'Y'.\n    ADD 1.25 TO WS-TOTAL.\n    MOVE SPACES TO R.\n    DISPLAY WS-TOTAL R-AMT.\n    STOP RUN.\n";

    private static Program program() {
        String source = SOURCE.lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
    }

    private static int id(Program p, String name) {
        return p.items().stream().filter(i -> i.name().equals(name)).mapToInt(Item::id).findFirst().orElseThrow();
    }

    private static Fact fact(Kind kind, Subject s, int item, State state) {
        return new Fact(kind, s, new Evidence("test", 1, List.of(new Node.ItemNode(item))), 1, state);
    }

    private static Intent intent(Program p, State state) {
        int result = id(p, "APPL-RESULT"), status = id(p, "WS-STATUS"), total = id(p, "WS-TOTAL"), amt = id(p, "R-AMT");
        return new Intent("P", p.header().profileHash(), List.of(fact(Kind.ENUM, new Subject.Choice(result, List.of("APPL-AOK", "APPL-EOF")), result, state),
                fact(Kind.ENUM, new Subject.Choice(status, List.of("WS-OK", "WS-EOF")), status, state), fact(Kind.MONEY, new Subject.Datum(total), total, state),
                fact(Kind.MONEY, new Subject.Datum(amt), amt, state)));
    }

    @Test
    void anEnumFactNamesItsValuesAndAMoneyFactTypesAPromotedItem() {
        Program p = program();
        Passes.Result r = Passes.plan(p, intent(p, State.INFERRED));
        int result = id(p, "APPL-RESULT"), status = id(p, "WS-STATUS"), total = id(p, "WS-TOTAL");
        assertEquals(java.util.Set.of(result, status), r.plan().enums(), r.refused().toString());
        assertEquals(java.util.Set.of(total), r.plan().money());
        assertTrue(r.refused().get("item R-AMT").stream().anyMatch(w -> w.contains("waits for 6.7")), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("private static final long APPL_AOK = 0; // 88 APPL-AOK") && java.contains("private static final long APPL_EOF = 16;"), java);
        assertTrue(java.contains("applResult = 8;") && java.contains("applResult = APPL_AOK;") && java.contains("applResult = APPL_EOF;"), "8 is no 88 value and stays a number; SET and a MOVE of 16 use the names");
        assertTrue(java.contains("private boolean applAok() {") && java.contains("private boolean wsOk() {") && java.contains("if ((applAok() && wsOk()))"), "the condition test calls the predicates");
        assertTrue(java.contains("private Money wsTotal = Money.zero(2);") && java.contains("wsTotal = Money.of(stored)"), "a promoted money item is Money, its store COBOL's");
    }

    @Test
    void aRefutedFactIsNeverActedOn() {
        Program p = program();
        Passes.Result r = Passes.plan(p, intent(p, new State(State.Status.REFUTED, "Ghost", "2026-10-22")));
        assertTrue(r.plan().enums().isEmpty() && r.plan().money().isEmpty(), "R-029: " + r.plan());
        assertEquals(Map.of(), r.typed());
    }
}
