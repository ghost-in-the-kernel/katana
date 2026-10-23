package katana.rt.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 3.1's check at scale, as a census (research §54): every runnable GnuCOBOL autotest through parse,
 * the Semantic IR, `lower/`, javac against `rt/` alone and a run, each case ending at the first stage it
 * cannot pass. The golden holds the stage per case and the counts; a floor that only rises: the count of
 * cases that pass the testsuite's expected stdout never falls below the golden's. The expected side is
 * compared and never shown (R-045); it is GnuCOBOL's default dialect, not IBM's (D-010), so a FAIL is not
 * yet a finding against Katana.
 */
@Tag("integration")
class AutotestsLoweredIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CASES = ROOT.resolve("corpus/gnucobol-autotests/cases");
    private static final Path GOLDEN = ROOT.resolve("test/katana/rt/autotest/lowered.golden");
    private static final int SECONDS = 20;

    /** A case's end: the first stage it cannot pass (NOSOURCE: no prog.cob, a case of several programs or a C helper), or its verdict against the testsuite. */
    enum Stage { NOSOURCE, NOPARSE, NOLOWER, NOJAVAC, NORUN, FAIL, PASS }

    /** Why a NOPARSE case does not parse: the front end's first error, without where it was (the checkout's path, line, column). */
    private static final Map<String, String> REASONS = new TreeMap<>();

    private static String firstError(String id) throws Exception {
        var parse = katana.front.Parse.file(CASES.resolve(id).resolve("prog.cob"), new katana.front.Copybooks(List.of(CASES.resolve(id)), n -> 8), 8, LoweredAutotestsIntegrationTest.CENSUS);
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

    @Test
    void everyRunnableAutotestEndsAtAStage() throws Exception {
        List<String> ids = runnable();
        Map<Stage, Integer> counts = new TreeMap<>();
        StringBuilder results = new StringBuilder("""
                # Phase 3.1's check at scale (AutotestsLoweredIntegrationTest, research 54): each runnable autotest through
                # parse, the IR, the lowering, javac against rt/ and a run; the first stage it cannot pass, or PASS or FAIL
                # against the testsuite's expected stdout (never shown, R-045; GnuCOBOL's dialect, D-010). Written by
                # `gradle integration -Dkatana.record=true`.
                """);
        Map<String, Stage> byCase = new TreeMap<>();
        for (String id : ids) {
            Stage s = stage(id);
            byCase.put(id, s);
            counts.merge(s, 1, Integer::sum);
            results.append(id).append(' ').append(s).append(s == Stage.NOPARSE ? " " + REASONS.get(id) : "").append('\n');
        }
        results.append("runnable ").append(ids.size());
        for (Stage s : Stage.values()) results.append(' ').append(s).append(' ').append(counts.getOrDefault(s, 0));
        results.append('\n');
        System.out.print(results.substring(results.indexOf("runnable")));
        Map<String, Integer> causes = new TreeMap<>();
        REASONS.values().forEach(r -> causes.merge(r, 1, Integer::sum));
        causes.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).forEach(c -> System.out.println("CAUSE " + c.getValue() + " " + c.getKey()));
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, results.toString());
        assertEquals(378, ids.size(), "the runnable count autotests.golden holds");
        // The hand-lowered cases that parse must not fall behind the hand-lowering.
        for (Lowered l : AutotestRtIntegrationTest.LOWERED) {
            Stage s = byCase.get(l.id());
            if (s == Stage.NOPARSE) continue; // 21 of the 24 parse (J-114); the front end's gap, counted above
            assertTrue(s == Stage.PASS || s == Stage.FAIL, l.id() + " is hand-lowered and ends at " + s);
            boolean handPasses = Arrays.equals(AutotestRtIntegrationTest.katana(l), AutotestRtIntegrationTest.expected(l.id()));
            assertEquals(handPasses ? Stage.PASS : Stage.FAIL, s, l.id() + " through the lowering against by hand");
        }
        assertEquals(Files.exists(GOLDEN) ? Files.readString(GOLDEN) : "(no golden)", results.toString());
        assertTrue(counts.getOrDefault(Stage.PASS, 0) >= floor(), "PASS falls below the golden's");
    }

    /** R-056's mutation: a passing case's lowered output held to another case's expected stdout is not a pass. */
    @Test
    void aPassingCaseHeldToAnotherCasesExpectedOutputFails() throws Exception {
        List<String> passing = Files.readAllLines(GOLDEN).stream().filter(l -> l.endsWith(" PASS") && !l.startsWith("#")).map(l -> l.substring(0, l.indexOf(' '))).toList();
        assertTrue(passing.size() > 1);
        byte[] got = LoweredAutotestsIntegrationTest.run(LoweredAutotestsIntegrationTest.build(passing.get(0)));
        assertTrue(Arrays.equals(got, AutotestRtIntegrationTest.expected(passing.get(0))));
        for (String other : passing.subList(1, passing.size()))
            if (!Arrays.equals(AutotestRtIntegrationTest.expected(other), got)) return;
        throw new AssertionError("every passing case expects the same stdout: the check could not fail");
    }

    /** The PASS count the golden holds: the floor, which only rises. */
    private static int floor() throws Exception {
        return (int) Files.readAllLines(GOLDEN).stream().filter(l -> l.endsWith(" PASS") && !l.startsWith("#")).count();
    }
}
