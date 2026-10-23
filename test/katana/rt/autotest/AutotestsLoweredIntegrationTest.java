package katana.rt.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import katana.oracle.autotest.CompileFlags;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 3.1's check at scale, as a census (research §54): every runnable GnuCOBOL autotest through parse,
 * the Semantic IR, `lower/`, javac against `rt/` alone and a run, each case ending at the first stage it
 * cannot pass. The golden holds the stage per case and the counts; a floor that only rises: the count of
 * cases that pass the testsuite's expected stdout never falls below `floors.txt`'s, for PASS and for PASS and FLAGPASS together (checked before the golden is written). The expected side is
 * compared and never shown (R-045); it is GnuCOBOL's default dialect, not IBM's (D-010), so a FAIL is not
 * yet a finding against Katana.
 */
@Tag("integration")
class AutotestsLoweredIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CASES = ROOT.resolve("corpus/gnucobol-autotests/cases");
    private static final Path GOLDEN = ROOT.resolve("test/katana/rt/autotest/lowered.golden");
    private static final Path FLOORS = ROOT.resolve("test/katana/rt/autotest/floors.txt");
    private static final int SECONDS = 20;
    /** The golden's header: every line starts with `# `, so a reader that skips `#` lines skips all of it. */
    private static final List<String> HEADER = List.of(
            "# Phase 3.1's check at scale (AutotestsLoweredIntegrationTest, research 54): each runnable autotest through",
            "# parse, the IR, the lowering, javac against rt/ and a run; the first stage it cannot pass, or PASS or FAIL",
            "# against the testsuite's expected stdout (never shown, R-045; GnuCOBOL's dialect, D-010), each under its own compile",
            "# flags (J-256): FLAG and FLAGPASS name the flags the compile step does not map (the case is run without them; FLAGPASS passes),",
            "# each as `flag [reason key]`, joined by ` | `; a key's text is in the `# reason` block at the end.",
            "# Run under profile/ibm.profile (TRUNC(STD), R-087). Written by `gradle integration -Dkatana.record=true`.");
    /** The profile the census runs under (R-087, R-081 amended): TRUNC(STD); the TRUNC(BIN) flags are held back for it, not mapped. */
    private static final Path PROFILE = ROOT.resolve("profile/ibm.profile");
    /** A FLAGPASS case's flags that ask for a form IBM does not have: grammar leaks to tighten (R-089), never passes. */
    private static final Map<String, List<String>> LEAKS = new TreeMap<>();

    /**
     * A case's end: the first stage it cannot pass (NOSOURCE: no prog.cob, a case of several programs or a C helper; FLAG: its compile command
     * has a flag the compile step does not understand, named in the golden; the case is still run without the flag, and FLAGPASS says it
     * passes anyway, which is not a pass under the reading it was written for), or its verdict against the testsuite. The floors in `floors.txt` (PASS; PASS and FLAGPASS together), which a re-record never writes, are on those two (J-259).
     */
    enum Stage { NOSOURCE, FLAG, FLAGPASS, NOPARSE, NOLOWER, NOJAVAC, NORUN, FAIL, PASS }

    /** Why a NOPARSE case does not parse (the front end's first error, without where it was: the checkout's path, line, column), or which flags a FLAG case has. */
    private static final Map<String, String> REASONS = new TreeMap<>();
    /** The reason keys the flag columns used, whose texts the golden's reasons block writes. */
    private static final Set<String> USED_KEYS = new java.util.HashSet<>();

    private static String firstError(String id) throws Exception {
        var parse = katana.front.Parse.file(CASES.resolve(id).resolve("prog.cob"), new katana.front.Copybooks(List.of(CASES.resolve(id)), n -> 8), 8, LoweredAutotestsIntegrationTest.compileStep(id).settings());
        String e = parse.errors().isEmpty() ? "(no message)" : parse.errors().get(0);
        // Where it was and what was expected vary with the case; the kind and the offending text are the cause.
        e = e.replaceAll("\\s+", " ").replaceAll(" expecting .*$", "").replaceAll("(?<=[a-z] )\\d+:\\d+:? ?", "").replaceAll("^copybook .* not found.*$", "copybook not found").trim();
        return e.length() > 90 ? e.substring(0, 90) : e;
    }

    /**
     * The runnable cases, in name order: `corpus/gnucobol-autotests/runnable.txt`, which `AutotestCorpusIntegrationTest`
     * writes (J-093), so the count is the one `oracle/autotest/autotests.golden` holds, not a second reading of "runnable".
     */
    static List<String> runnable() throws Exception {
        Path list = ROOT.resolve("corpus/gnucobol-autotests/runnable.txt");
        assertTrue(Files.exists(list), "run AutotestCorpusIntegrationTest first: it writes " + list);
        return Files.readAllLines(list).stream().filter(l -> !l.isBlank()).sorted().toList();
    }

    /** One case to its end. A run that does not stop in SECONDS is NORUN; its thread is left to the JVM's exit. */
    static Stage stage(String id) throws Exception {
        // A case's files open relative to the working directory, which is the repository root: take away what the run leaves.
        Set<String> before = ls();
        try {
            return stageOf(id);
        } finally {
            for (String name : ls()) if (!before.contains(name)) Files.deleteIfExists(ROOT.resolve(name));
        }
    }

    private static Set<String> ls() throws Exception {
        try (var list = Files.list(ROOT)) {
            return list.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).collect(java.util.stream.Collectors.toSet());
        }
    }

    private static Stage stageOf(String id) throws Exception {
        Stage raw = rawStageOf(id);
        List<String> flags = LoweredAutotestsIntegrationTest.compileStep(id).unmapped();
        if (flags.isEmpty() || raw == Stage.NOSOURCE) return raw;
        if (raw == Stage.PASS) LEAKS.put(id, flags.stream().filter(CompileFlags::grammarLeak).toList());
        REASONS.put(id, flagColumn(flags));
        flags.stream().map(CompileFlags::reasonKey).filter(java.util.Objects::nonNull).forEach(USED_KEYS::add);
        return raw == Stage.PASS ? Stage.FLAGPASS : Stage.FLAG;
    }

    /** Between two flags in the golden: no flag in any `flags.txt` contains it (checked by `theGoldensFlagsReadBackAsTheCasesOwn`), so the column reads back whole. */
    static final String FLAG_SEPARATOR = " | ";
    private static final java.util.regex.Pattern KEYED = java.util.regex.Pattern.compile("(.*) \\[([a-z0-9-]+)\\]");

    /** A FLAG or FLAGPASS line's flags: each as `flag [reason key]` (the key alone when `CompileFlags` gives a reason), joined by the separator. */
    static String flagColumn(List<String> flags) {
        return flags.stream().map(f -> CompileFlags.reasonKey(f) == null ? f : f + " [" + CompileFlags.reasonKey(f) + "]").collect(java.util.stream.Collectors.joining(FLAG_SEPARATOR));
    }

    /** The golden's flag column back to the flags it was made from. */
    static List<String> readFlags(String column) {
        return Arrays.stream(column.split(java.util.regex.Pattern.quote(FLAG_SEPARATOR), -1)).map(f -> { var m = KEYED.matcher(f); return m.matches() ? m.group(1) : f; }).toList();
    }

    /** The case run with the flags the compile step maps and none of the others. */
    private static Stage rawStageOf(String id) throws Exception {
        // Several programs, or a C helper: the case has no prog.cob, and the lowering makes one program (a CALL between them is 3.4's).
        if (!Files.exists(CASES.resolve(id).resolve("prog.cob"))) return Stage.NOSOURCE;
        LoweredAutotestsIntegrationTest.Built built;
        try {
            built = LoweredAutotestsIntegrationTest.build(id);
        } catch (AssertionError javac) {
            System.out.println("NOJAVAC " + id + ": " + String.valueOf(javac.getMessage()).lines().skip(1).findFirst().orElse(""));
            return Stage.NOJAVAC;
        } catch (Throwable lowering) {
            System.out.println("NOLOWER " + id + ": " + lowering);
            return Stage.NOLOWER;
        }
        if (built == null) {
            REASONS.put(id, firstError(id));
            return Stage.NOPARSE;
        }
        FutureTask<byte[]> run = new FutureTask<>(() -> LoweredAutotestsIntegrationTest.run(built));
        // Unsure: a case that loops past SECONDS is abandoned, not stopped (a Thread cannot be killed); its daemon thread keeps a core until the JVM exits. Stays: until the census runs a process per case, which would stop it (TODO 3.1, open).
        Thread t = new Thread(run, "autotest-" + id);
        t.setDaemon(true);
        t.start();
        byte[] got;
        try {
            got = run.get(SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException failed) {
            System.out.println("NORUN " + id + ": " + root(failed));
            return Stage.NORUN;
        }
        return Arrays.equals(got, AutotestRtIntegrationTest.expected(id)) ? Stage.PASS : Stage.FAIL;
    }

    private static Throwable root(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t;
    }

    /** The census (TODO 3.1): every runnable case to its stage under its own flags, the floors checked before the golden is written, then the golden held fact for fact. */
    @Test
    void everyRunnableAutotestEndsAtAStage() throws Exception {
        katana.profile.Profile profile = katana.profile.Profile.read(Files.readString(PROFILE));
        assertEquals("STD", profile.ibm().get("ibm.trunc"), "R-087: the census runs ibm.profile, TRUNC(STD)");
        List<String> ids = runnable();
        Map<Stage, Integer> counts = new TreeMap<>();
        StringBuilder results = new StringBuilder(String.join("\n", HEADER) + "\n");
        Map<String, Stage> byCase = new TreeMap<>();
        for (String id : ids) {
            Stage s = stage(id);
            byCase.put(id, s);
            counts.merge(s, 1, Integer::sum);
            results.append(id).append(' ').append(s).append(s == Stage.NOPARSE || s == Stage.FLAG || s == Stage.FLAGPASS ? " " + REASONS.get(id) : "").append('\n');
        }
        results.append("runnable ").append(ids.size());
        for (Stage s : Stage.values()) results.append(' ').append(s).append(' ').append(counts.getOrDefault(s, 0));
        results.append('\n');
        // A FLAGPASS under a form IBM does not have is the front end too lenient: listed, to tighten (R-089).
        LEAKS.forEach((id, flags) -> { if (!flags.isEmpty() && byCase.get(id) == Stage.FLAGPASS) results.append("# grammar leak ").append(id).append(' ').append(String.join(" ", flags)).append('\n'); });
        // The reasons, once each, for the keys the flag columns use; `CompileFlags` holds the text.
        new java.util.TreeSet<>(USED_KEYS).forEach(k -> results.append("# reason ").append(k).append(": ").append(CompileFlags.reasonOfKey(k)).append('\n'));
        System.out.print(results.substring(results.indexOf("\nrunnable ") + 1));
        Map<String, Integer> causes = new TreeMap<>();
        byCase.forEach((id, s) -> { if (s == Stage.NOPARSE) causes.merge(REASONS.get(id), 1, Integer::sum); });
        causes.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).forEach(c -> System.out.println("CAUSE " + c.getValue() + " " + c.getKey()));
        // The floors first: a refused re-record leaves the golden as it was (J-259).
        String breach = floorBreach(counts.getOrDefault(Stage.PASS, 0), counts.getOrDefault(Stage.FLAGPASS, 0), floor("PASS"), floor(BOTH));
        assertNull(breach, breach);
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, results.toString());
        assertEquals(378, ids.size(), "the runnable count autotests.golden holds");
        // The hand-lowered cases that parse must not fall behind the hand-lowering.
        for (Lowered l : AutotestRtIntegrationTest.LOWERED) {
            Stage s = byCase.get(l.id());
            if (s == Stage.NOPARSE || s == Stage.FLAG || s == Stage.FLAGPASS) continue; // 21 of the 24 parse (J-114); the front end's gap, counted above
            assertTrue(s == Stage.PASS || s == Stage.FAIL, l.id() + " is hand-lowered and ends at " + s);
            boolean handPasses = Arrays.equals(AutotestRtIntegrationTest.katana(l), AutotestRtIntegrationTest.expected(l.id()));
            assertEquals(handPasses ? Stage.PASS : Stage.FAIL, s, l.id() + " through the lowering against by hand");
        }
        assertEquals(Files.exists(GOLDEN) ? Files.readString(GOLDEN) : "(no golden)", results.toString());
    }

    /**
     * The golden's flag columns read back as each case's own `flags.txt` (the flags the compile step leaves unmapped, in order, whole), so the separator can never
     * be ambiguous: no flag in any case's `flags.txt` contains it or looks like a reason key, and every key used has its text, written once, from `CompileFlags`.
     */
    @Test
    void theGoldensFlagsReadBackAsTheCasesOwn() throws Exception {
        List<String> lines = Files.readAllLines(GOLDEN);
        assertTrue(lines.stream().takeWhile(l -> !l.isEmpty() && l.startsWith("#")).count() == HEADER.size(), "the header is all `# ` lines, then the cases");
        Map<String, String> reasons = new TreeMap<>();
        for (String l : lines) if (l.startsWith("# reason ")) reasons.put(l.substring(9, l.indexOf(':')), l.substring(l.indexOf(':') + 2));
        Set<String> keysUsed = new java.util.HashSet<>();
        int checked = 0;
        for (String l : lines) {
            if (l.startsWith("#") || l.startsWith("runnable ")) continue;
            String[] part = l.split(" ", 3);
            if (!part[1].equals("FLAG") && !part[1].equals("FLAGPASS")) continue;
            List<String> own = Files.readAllLines(CASES.resolve(part[0]).resolve("flags.txt")).stream().filter(f -> !f.isBlank())
                    .filter(f -> !CompileFlags.map(List.of(f), true).unmapped().isEmpty()).toList();
            assertEquals(own, readFlags(part[2]), part[0] + ": the golden's flags against its flags.txt");
            for (String f : own) if (CompileFlags.reasonKey(f) != null) keysUsed.add(CompileFlags.reasonKey(f));
            checked++;
        }
        assertTrue(checked > 1, "FLAG and FLAGPASS lines");
        assertEquals(keysUsed, reasons.keySet(), "a reasons block line for each key used, and no other");
        reasons.forEach((k, text) -> assertEquals(CompileFlags.reasonOfKey(k), text, "# reason " + k));
        // The separator and the key's brackets are in no flag of any case, so a column splits one way only.
        try (var dirs = Files.list(CASES)) {
            for (Path d : dirs.toList()) {
                Path f = d.resolve("flags.txt");
                if (Files.exists(f)) for (String flag : Files.readAllLines(f)) assertTrue(!flag.contains(FLAG_SEPARATOR) && !KEYED.matcher(flag).matches(), d.getFileName() + ": " + flag);
            }
        }
    }

    /** R-056's mutation for the flag columns: a flag with spaces and commas, as `run_extensions-2904` has, comes back whole, and joining with a bare space would not. */
    @Test
    void aFlagWithSpacesSurvivesTheColumn() {
        List<String> flags = List.of("-fsystem-name=sw1, SwItCh\\ b, SWITCH\\ 25", "-fsystem-name=SWITCH-32", "-fsomething-new");
        assertEquals(flags, readFlags(flagColumn(flags)));
        assertTrue(flagColumn(flags).contains(" [cobc-runtime] | -fsystem-name=SWITCH-32 [cobc-runtime] | -fsomething-new"));
        assertTrue(!Arrays.equals(flags.toArray(), String.join(" ", flags).split(" ")), "joined by a space the first flag is lost in three");
    }

    /** R-056's mutation, in both passing stages: a passing case's lowered output held to another case's expected stdout is not a pass. */
    @Test
    void aPassingCaseHeldToAnotherCasesExpectedOutputFails() throws Exception {
        for (Stage stage : List.of(Stage.PASS, Stage.FLAGPASS)) {
            List<String> passing = Files.readAllLines(GOLDEN).stream().filter(l -> !l.startsWith("#") && l.split(" ")[1].equals(stage.name())).map(l -> l.substring(0, l.indexOf(' '))).toList();
            assertTrue(passing.size() > 1, stage + "");
            byte[] got = LoweredAutotestsIntegrationTest.run(LoweredAutotestsIntegrationTest.build(passing.get(0)));
            assertTrue(Arrays.equals(got, AutotestRtIntegrationTest.expected(passing.get(0))), stage + "");
            boolean seen = false;
            for (String other : passing.subList(1, passing.size()))
                if (!Arrays.equals(AutotestRtIntegrationTest.expected(other), got)) seen = true;
            assertTrue(seen, stage + ": every passing case expects the same stdout: the check could not fail");
        }
    }

    /** The second floor's name in `floors.txt`: the cases that pass, PASS and FLAGPASS together. */
    private static final String BOTH = "PASS+FLAGPASS";

    /**
     * Which floor the counts fall below, or null. Two floors, both only rising (R-086): PASS, and PASS and FLAGPASS together, so a case
     * that moves from FLAGPASS up to PASS lowers neither; its stage in the golden shows a case that falls back.
     */
    // Decision: R-086
    static String floorBreach(int pass, int flagpass, int passFloor, int bothFloor) {
        if (pass < passFloor) return "PASS " + pass + " falls below its floor " + passFloor + " in floors.txt (a floor is raised by hand, never lowered by a re-record)";
        if (pass + flagpass < bothFloor) return BOTH + " " + (pass + flagpass) + " falls below its floor " + bothFloor + " in floors.txt (a floor is raised by hand, never lowered by a re-record)";
        return null;
    }

    @Test
    void aCaseMovingUpFromFlagpassLowersNeitherFloorAndAFallLowersOne() {
        assertNull(floorBreach(45, 14, 44, 59), "FLAGPASS to PASS: nothing lost");
        assertNull(floorBreach(44, 15, 44, 59));
        assertNotNull(floorBreach(43, 16, 44, 59), "a PASS case fell back to FLAGPASS");
        assertNotNull(floorBreach(45, 13, 44, 59), "a case lost altogether");
    }

    /** A floor, from `floors.txt` (`<NAME> <count>` a line): raised by hand when the census rises, never written by a re-record. */
    private static int floor(String name) throws Exception {
        return Files.readAllLines(FLOORS).stream().filter(l -> l.startsWith(name + " ")).mapToInt(l -> Integer.parseInt(l.substring(l.indexOf(' ') + 1).trim())).findFirst().orElseThrow();
    }
}
