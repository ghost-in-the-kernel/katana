package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import org.junit.jupiter.api.Test;

/** R-090 step 4: ACCEPT FROM and DISPLAY UPON take a SPECIAL-NAMES mnemonic or an IBM environment-name; any other name is a finding, never output. */
class EnvironmentNamesTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";

    private static Lower.Result lower(String... statements) {
        List<String> lines = new java.util.ArrayList<>(List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. ENV.", "ENVIRONMENT DIVISION.", "CONFIGURATION SECTION.", "SPECIAL-NAMES.",
                "    SYSOUT IS MY-OUT", "    SYSIN IS MY-IN.", "DATA DIVISION.", "WORKING-STORAGE SECTION.", "01 W PIC X(10).", "PROCEDURE DIVISION.", "P1."));
        for (String s : statements) lines.add("    " + s);
        lines.add("    STOP RUN.");
        String text = lines.stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Lower.java(Build.program(Parse.text(text, new Copybooks(List.of(), n -> 8)), HASH).program(), "Env");
    }

    @Test
    void aMnemonicAndIbmsEnvironmentNamesLowerWithoutAFinding() {
        for (String upon : List.of("MY-OUT", "SYSOUT", "SYSLIST", "SYSLST", "SYSPUNCH", "SYSPCH", "CONSOLE", "SYSERR")) {
            Lower.Result r = lower("DISPLAY \"A\" UPON " + upon + ".");
            assertTrue(r.findings().isEmpty(), upon + ": " + r.findings());
        }
        for (String from : List.of("MY-IN", "SYSIN", "SYSIPT", "CONSOLE")) {
            Lower.Result r = lower("ACCEPT W FROM " + from + ".");
            assertTrue(r.findings().isEmpty(), from + ": " + r.findings());
            assertTrue(r.java().contains("in.accept("), r.java());
        }
    }

    @Test
    void aNameIbmLacksIsAFindingWithNoOutput() {
        for (String upon : List.of("ENVIRONMENT-NAME", "ENVIRONMENT-VALUE", "STDOUT", "PRINTER", "ARGUMENT-NUMBER")) {
            Lower.Result r = lower("DISPLAY \"A\" UPON " + upon + ".");
            assertEquals(1, r.findings().size(), upon + ": " + r.findings());
            String f = r.findings().get(0);
            assertTrue(f.contains("UPON " + upon) && f.contains("SYSOUT, SYSLIST") && f.contains("(recollection, R-082)"), f);
            assertTrue(r.java().contains("Unsupported.fail(") && !r.java().contains("out.display("), r.java());
        }
        for (String from : List.of("ENVIRONMENT-VALUE", "ARGUMENT-VALUE", "ARGUMENT-NUMBER", "STDIN", "SYSOUT")) {
            Lower.Result r = lower("ACCEPT W FROM " + from + ".");
            assertEquals(1, r.findings().size(), from + ": " + r.findings());
            String f = r.findings().get(0);
            assertTrue(f.contains("FROM " + from) && f.contains("SYSIN, SYSIPT, CONSOLE") && f.contains("(recollection, R-082)"), f);
            assertTrue(r.java().contains("Unsupported.fail(") && !r.java().contains("in.accept("), r.java());
        }
    }
}
