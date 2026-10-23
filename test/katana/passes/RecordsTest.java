package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.rules.Rules;
import katana.lower.Lower;
import katana.lower.ManifestTest;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.7's record classes on programs small enough to read (research §46): the class, its getters and a table's accessor, and each obligation broken. */
class RecordsTest {
    private static Program program(String data, String procedure) {
        return katana.support.Small.program(data, "MAIN.\n" + procedure + "    STOP RUN.\n");
    }

    private static final String DATA = "01 TRAN-RECORD.\n   05 TRAN-ID PIC X(4).\n   05 TRAN-AMT PIC S9(5)V99.\n   05 TRAN-QTY PIC 9(3).\n   05 TRAN-LINE OCCURS 3.\n      10 LINE-QTY PIC 9(2).\n01 OUT-RECORD PIC X(20).\n01 TOTAL PIC 9(5).\n";

    @Test
    void aRecordIsAClassOverItsBytes() {
        Program p = program(DATA, "    MOVE 'A' TO TRAN-ID.\n    MOVE 2 TO LINE-QTY (3).\n    ADD TRAN-QTY LINE-QTY (1) TO TOTAL.\n    COMPUTE TOTAL = TRAN-QTY + 1.\n    MOVE TRAN-RECORD TO OUT-RECORD.\n    DISPLAY OUT-RECORD TOTAL.\n");
        Passes.Result r = Passes.plan(p, Rules.infer(p));
        assertEquals(1, r.plan().records().size(), r.refused().toString());
        Lower.Result lowered = Lower.java(p, "P", r.plan());
        String java = lowered.java();
        assertTrue(java.contains("private TranRecord tranRecord; // TRAN-RECORD") && java.contains("private static final class TranRecord {"), java);
        assertTrue(java.contains("Field tranAmt; // TRAN-AMT") && java.contains("java.math.BigDecimal tranAmt() {") && java.contains("long tranQty() {"), "a numeric member's getter");
        assertTrue(java.contains("Field lineQty(long s0) {") && java.contains("tranRecord.lineQty(3L)"), "a table member's accessor");
        assertTrue(java.contains("tranRecord.whole") && java.contains("tranRecord = new TranRecord(r1, 0);"), "the 01 itself is `whole`");
        assertTrue(java.contains("tranRecord.tranQty()"), "exact arithmetic reads through the getter");
        assertTrue(!java.contains("private Field f2;"), "no program field for a member");
        assertEquals(ManifestTest.declared(java), ManifestTest.members(lowered.symbols()));
        assertTrue(lowered.symbols().stream().anyMatch(s -> s.name().equals("P.tranRecord") && s.node().equals("record 1")), lowered.symbols().toString());
    }

    /** 6.7b (research §47): a LINKAGE record's class is built where its CALL binds it, at the argument's start. */
    @Test
    void aLinkageRecordIsBoundWhole() {
        String source = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. P.", "DATA DIVISION.", "LINKAGE SECTION.", "01 PARM.", "   05 PARM-CODE PIC 9(2).", "   05 PARM-TEXT PIC X(8).",
                "PROCEDURE DIVISION USING PARM.", "MAIN.", "    MOVE 7 TO PARM-CODE.", "    GOBACK.").stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        Program p = Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
        Passes.Result r = Passes.plan(p, Rules.infer(p));
        assertEquals(1, r.plan().records().size(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("parm = new Parm(a.record(), a.offset());") && java.contains("parmCode = new Field(r, base + 0, ") && java.contains("parm.parmCode"), java);
    }

    @Test
    void eachObligationBrokenKeepsTheFields() {
        assertKept("01 R.\n   05 A PIC X(4).\n   05 B REDEFINES A PIC 9(4).\n", "    DISPLAY R.\n", "redefines");
        assertKept("01 R.\n   05 A PIC X(4).\n", "    DISPLAY A (2:1).\n", "reference-modified");
        assertKept("01 N PIC 9.\n01 R.\n   05 A PIC X OCCURS 1 TO 5 DEPENDING ON N.\n", "    DISPLAY R.\n", "occurs depending on");
        assertKept("01 R EXTERNAL.\n   05 A PIC X(4).\n", "    DISPLAY R.\n", "EXTERNAL");
        assertKept("01 R.\n   05 A COMP-1.\n", "    DISPLAY R.\n", "FLOAT_SHORT");
    }

    private static void assertKept(String data, String procedure, String why) {
        Program p = program(data, procedure);
        Passes.Result r = Passes.plan(p, Rules.infer(p));
        assertTrue(r.plan().records().isEmpty() && r.refused().entrySet().stream().anyMatch(e -> e.getKey().startsWith("record ") && e.getValue().stream().anyMatch(w -> w.contains(why))), why + ": " + r.refused());
    }
}
