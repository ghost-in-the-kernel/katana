package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import katana.findings.Finding;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.front.Settings;
import katana.sir.check.CompiledOut;
import org.junit.jupiter.api.Test;

/** R-081: each range a false `>>` condition excludes is a CO-01 finding, with the directive that excluded it. */
class CompiledOutTest {
    @Test
    void eachExcludedRangeIsAFindingWithItsDirective() {
        String text = "       IDENTIFICATION DIVISION.\n       PROGRAM-ID. P.\n       PROCEDURE DIVISION.\n       >>IF A DEFINED\n           DISPLAY 'A'.\n           DISPLAY 'B'.\n       >>ELSE\n           DISPLAY 'C'.\n       >>END-IF\n           STOP RUN.\n";
        Parse.Result parse = Parse.text(text, "P.cbl", new Copybooks(List.of(), n -> 8), Settings.declared(Map.of(), false));
        assertEquals(true, parse.clean());
        assertEquals("CO-01 compiled-out code: lines 5 to 6 excluded by >>IF A DEFINED (P.cbl:5:1)\n", Finding.report(CompiledOut.findings(parse)));
    }
}
