package katana.rt.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 3.1's check, as far as it reaches by hand (HANDOFF, J-110): each hand-lowered autotest program
 * run against `rt/` and its output held two ways. Against the testsuite's expected stdout, pass or fail
 * only (R-045: the expected side is never shown; it is cobc's default dialect, not IBM's). Against cobc
 * -std=ibm running the same `prog.cob`, shown in full, where a difference must be a listed finding with
 * its reason. The results are a golden, so the check says how many it reaches and how many pass.
 */
@Tag("integration")
class AutotestRtIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CASES = ROOT.resolve("corpus/gnucobol-autotests/cases");
    private static final Path GOLDEN = ROOT.resolve("test/katana/rt/autotest/results.golden");

    static final List<Lowered> LOWERED = List.of(new RunFundamental146(), new RunFundamental247(), new RunFundamental296(), new RunFundamental2585(),
            new RunFundamental2628(), new RunFundamental4521(), new RunMisc2916(), new RunMisc2947(), new RunMisc7348(),
            new RunFundamental193(), new RunFundamental8846(), new RunFundamental8897(), new RunFundamental2433(), new RunFundamental2513(),
            new RunMisc156(), new RunMisc190(), new RunMisc2799(), new RunMisc12873());

    private static final String ZONED_SIGN = "cobc's ibm dialect DISPLAYs an overpunched zoned item's sign separate in the overpunch's place; IBM's DISPSIGN(SEP) leads with it (J-109)";

    /** Each case where Katana and cobc -std=ibm differ, and why: IBM's text against cobc's dialect. */
    static final Map<String, String> FINDINGS = Map.of(
            "run_fundamental-146", ZONED_SIGN,
            "run_misc-2947", ZONED_SIGN,
            "run_fundamental-193", ZONED_SIGN,
            "run_fundamental-2433", "cobc's ibm dialect judges suppression on a value before its digits are cut (J-104); Katana's output is the testsuite's",
            "run_fundamental-2513", "cobc's ibm dialect does not take B 0 / , inside a floating string as part of it, as IBM does (LR p. 223); Katana's output is the testsuite's");


    /** The testsuite's expected stdout for a case: the first check after the program is compiled that runs it and expects output. */
    static byte[] expected(String id) throws Exception {
        Path dir = CASES.resolve(id);
        Matcher m = Pattern.compile("(?m)^check (\\d+)").matcher(Files.readString(dir.resolve("case.txt"), StandardCharsets.ISO_8859_1));
        boolean compiled = false;
        while (m.find()) {
            String n = m.group(1), command = Files.readString(dir.resolve("check-" + n + ".sh"), StandardCharsets.ISO_8859_1);
            byte[] stdout = Files.readAllBytes(dir.resolve("check-" + n + ".stdout"));
            if (command.contains("$COMPILE")) compiled = true;
            else if (compiled && stdout.length > 0 && (command.contains("COBCRUN_DIRECT") || command.contains("./"))) return stdout;
        }
        throw new IllegalStateException(id + ": no check runs the program against an expected stdout");
    }

    /** cobc -std=ibm's output for a case's program; null when its ibm dialect rejects the program. */
    static byte[] cobc(String id) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-autotest/" + id));
        Files.copy(CASES.resolve(id).resolve("prog.cob"), dir.resolve("prog.cob"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "prog.cob", List.of());
        if (compiled.exit() != 0) return null;
        return cobc.execute(dir, "prog", Map.of()).output().getBytes(StandardCharsets.ISO_8859_1);
    }

    static byte[] katana(Lowered l) {
        Out out = new Out();
        l.run(out);
        return out.bytes();
    }

    @Test
    void handLoweredAutotestsRunOnRt() throws Exception {
        StringBuilder results = new StringBuilder("""
                # Phase 3.1's check by hand (AutotestRtIntegrationTest): each hand-lowered autotest, whether Katana's
                # output equals the testsuite's expected stdout (PASS or FAIL, never shown, R-045) and cobc -std=ibm's
                # (SAME or DIFFER, a DIFFER a listed finding; REJECTED when its ibm dialect does not compile the program). Written by `gradle integration -Dkatana.record=true`.
                """);
        List<String> unexplained = new ArrayList<>();
        int pass = 0;
        for (Lowered l : LOWERED) {
            byte[] katana = katana(l), cobc = cobc(l.id());
            boolean passes = Arrays.equals(katana, expected(l.id())), same = cobc == null || Arrays.equals(katana, cobc);
            if (passes) pass++;
            results.append(l.id()).append(passes ? " PASS" : " FAIL").append(cobc == null ? " REJECTED" : same ? " SAME" : " DIFFER").append('\n');
            if (same == FINDINGS.containsKey(l.id())) unexplained.add(l.id() + (same ? ": listed as a finding but the same as cobc" : ": Katana\n" + text(katana) + "cobc -std=ibm\n" + text(cobc)));
        }
        results.append("hand-lowered ").append(LOWERED.size()).append(" of 378, passing ").append(pass).append('\n');
        System.out.print(results);
        assertEquals(List.of(), unexplained);
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, results.toString());
        assertEquals(Files.exists(GOLDEN) ? Files.readString(GOLDEN) : "(no golden)", results.toString());
    }

    /** R-056's mutation: a case's output held to another case's expected stdout fails. */
    @Test
    void anotherCasesExpectedOutputFails() throws Exception {
        for (Lowered l : LOWERED) assertFalse(Arrays.equals(katana(l), expected(LOWERED.size() > 1 ? LOWERED.get((LOWERED.indexOf(l) + 1) % LOWERED.size()).id() : "run_misc-2916")), l.id());
    }

    private static String text(byte[] b) {
        return new String(b, StandardCharsets.ISO_8859_1);
    }
}
