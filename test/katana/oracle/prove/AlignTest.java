package katana.oracle.prove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 8A.4's alignment on a program small enough to read (research §51): each kind of evidence, and a line naming nothing refused. */
class AlignTest {
    private static Program program() {
        String source = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. P.", "DATA DIVISION.", "WORKING-STORAGE SECTION.", "01 WS-REC.", "   05 ACCT-BAL PIC 9(5)V99.",
                "   05 ACCT-INT PIC 9(5)V99.", "PROCEDURE DIVISION.", "MAIN-LINE.", "    PERFORM CALC-INT.", "    DISPLAY 'DONE NOW'.", "    STOP RUN.", "CALC-INT.",
                "    COMPUTE ACCT-INT = ACCT-BAL * 0.0375.").stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, "P.cbl", new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
    }

    private static final String JAVA = String.join("\n", "class Port {", "  long acctBal, acctInt;", "  void calcInt() {", "    acctInt = acctBal * new java.math.BigDecimal(\"0.0375\");", "  }",
            "  void mainLine() {", "    calcInt();", "    System.out.println(\"DONE NOW\");", "  }", "}");

    @Test
    void eachEvidenceAligns() {
        Align.Result r = Align.infer(program(), "Port.java", JAVA);
        String text = Align.write("P", r.lines());
        assertTrue(text.contains("paragraph CALC-INT | Port.java:3-5 | unconfirmed | name CALC-INT"), text);
        assertTrue(text.contains("item ACCT-BAL | Port.java:2 | unconfirmed | name ACCT-BAL"), text);
        assertTrue(text.contains("statement COMPUTE #1 IN CALC-INT | Port.java:4 | unconfirmed | literal 375"), text);
        assertTrue(text.contains("statement DISPLAY #1 IN MAIN-LINE | Port.java:8 | unconfirmed | literal DONE NOW"), text);
        assertEquals(2, r.coverage().get("paragraph")[1]);
    }

    @Test
    void aSourceLineCommentAlignsItsStatement() {
        String generated = "class P {\n  /* P.cbl:14: COMPUTE */\n  d0.mul(d2);\n}\n";
        assertTrue(Align.write("P", Align.infer(program(), "P.java", generated).lines()).contains("statement COMPUTE #1 IN CALC-INT | P.java:3 | unconfirmed | source P.cbl:14"));
    }

    @Test
    void aLineNamingWhatTheProgramDoesNotHoldIsRefused() {
        List<String> refused = new ArrayList<>();
        List<Align.Line> read = Align.read("# P.align\nparagraph CALC-INT | Port.java:3-5 | confirmed | name CALC-INT\nparagraph CALC-FEES | Port.java:9 | unconfirmed | name\n"
                + "item ACCT-BAL | Port.java:2 | maybe | name\n", program(), refused);
        assertEquals(1, read.size());
        assertEquals(List.of("names paragraph CALC-FEES, which the program does not hold", "state maybe of item ACCT-BAL is not unconfirmed, confirmed or refuted"), refused);
    }
}
