package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/**
 * A comma is a numeric literal's decimal point only under DECIMAL-POINT IS COMMA (J-119). Elsewhere
 * `DISPLAY 1,1,1` does not parse: IBM's separator comma is followed by a space (LR 6.4, separators), so
 * the program is not conforming, and it is refused rather than read as 1.1.
 */
class DecimalCommaTest {
    private static String source(String specialNames) {
        return String.join("\n", "       IDENTIFICATION DIVISION.", "       PROGRAM-ID. P.", "       ENVIRONMENT DIVISION.", "       CONFIGURATION SECTION.",
                "       SPECIAL-NAMES." + specialNames, "       PROCEDURE DIVISION.", "           DISPLAY 1,1,1.", "           STOP RUN.", "");
    }

    private static List<String> displayed(String specialNames) {
        String text = String.join("\n", "       IDENTIFICATION DIVISION.", "       PROGRAM-ID. P.", "       ENVIRONMENT DIVISION.", "       CONFIGURATION SECTION.",
                "       SPECIAL-NAMES." + specialNames, "       PROCEDURE DIVISION.", "           DISPLAY 1,1,1.", "           STOP RUN.", "");
        Parse.Result parse = Parse.text(text, new Copybooks(List.of(), n -> 8));
        Statement display = Build.program(parse, "sha256:" + "0".repeat(64)).program().procedure().paragraphs().get(0).statements().get(0);
        return display.operands().stream().map(o -> (Value.Literal) o.value()).map(l -> l.payload() + "e-" + l.scale()).toList();
    }

    @Test
    void withoutTheClauseTheProgramIsRefused() {
        org.junit.jupiter.api.Assertions.assertFalse(Parse.text(source(""), new Copybooks(List.of(), n -> 8)).clean());
    }

    @Test
    void underTheClauseACommaIsTheDecimalPoint() {
        assertEquals(List.of("11e-1", "1e-1"), displayed(" DECIMAL-POINT IS COMMA."));
    }
}
