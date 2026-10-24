package katana.rt.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * `CaseRun` (TODO 3.1, J-278): a case's command given to a process of its own, in a copy of the case's folder. The programs are small Java classes that print
 * what they were given, so the expectations here are the shell's meaning of the command forms `CaseCommand` reads (the environment, the arguments, the input
 * file, `head -n N`, a program that never ends), written out below and not taken from the census.
 */
@Tag("integration")
class CaseRunIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path BUILD = ROOT.resolve("build/case-run-test").toAbsolutePath();
    private static final String SOURCE = """
            import java.nio.file.*;
            import java.util.*;
            public class Probe {
                public static void main(String[] a) throws Exception {
                    String mode = System.getenv().getOrDefault("MODE", "show");
                    switch (mode) {
                        case "loop" -> { while (true) Thread.onSpinWait(); }
                        case "many" -> { for (int i = 1; i <= 1000; i++) System.out.println("line " + i); }
                        case "throw" -> throw new IllegalStateException("boom");
                        case "abend" -> { System.err.println("CEE3ABD abend U0001"); System.exit(1); }
                        case "rc" -> { System.out.println("done"); System.exit(7); }
                        default -> {
                            System.out.println("env=" + System.getenv("NAME") + " debug=" + System.getProperty("katana.debug") + " date=" + System.getenv("COB_CURRENT_DATE"));
                            System.out.println("args=" + Arrays.toString(a));
                            List<String> files = new ArrayList<>();
                            try (var s = Files.list(Path.of("."))) { s.forEach(p -> files.add(p.getFileName().toString())); }
                            Collections.sort(files);
                            System.out.println("files=" + files);
                            System.out.println("stdin=" + new String(System.in.readAllBytes()).trim());
                            Files.writeString(Path.of("left.dat"), "x");
                        }
                    }
                }
            }
            """;
    private static final String CLASSPATH = BUILD.resolve("classes").toString();

    @BeforeAll
    static void compile() throws Exception {
        Files.createDirectories(BUILD.resolve("classes"));
        Path source = BUILD.resolve("Probe.java");
        Files.writeString(source, SOURCE);
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", BUILD.resolve("classes").toString(), source.toString()));
        Path folder = Files.createDirectories(BUILD.resolve("case"));
        Files.writeString(folder.resolve("prog.cob"), "program\n");
        Files.writeString(folder.resolve("input.txt"), "from the file\n");
        Files.writeString(folder.resolve("check-0.sh"), "$COMPILE prog.cob\n");
        Files.writeString(folder.resolve("case.txt"), "title t\n");
        Files.writeString(folder.resolve("flags.txt"), "");
    }

    private static CaseRun.Result run(String shell, int seconds, String name) throws Exception {
        return CaseRun.run(BUILD.resolve("case"), BUILD.resolve("work-" + name), CLASSPATH, "Probe", CaseCommand.read(shell), seconds);
    }

    private static String text(CaseRun.Result r) {
        return new String(r.stdout(), StandardCharsets.ISO_8859_1);
    }

    @Test
    void theCommandsEnvironmentArgumentsInputAndFolderReachTheProgram() throws Exception {
        CaseRun.Result r = run("NAME=\"two words\" COB_CURRENT_DATE=\"2015/02/06 16:40:52\" COB_SET_DEBUG=1 $COBCRUN_DIRECT ./prog 'a b' c < input.txt", 20, "show");
        assertNull(r.failure());
        assertEquals("env=two words debug=Y date=2015-02-06 16:40:52.00 +00:00\nargs=[a b, c]\nfiles=[input.txt, prog.cob]\nstdin=from the file\n", text(r));
        assertTrue(Files.exists(BUILD.resolve("work-show/left.dat")), "what the program writes is in its copy of the folder");
        assertTrue(!Files.exists(ROOT.resolve("left.dat")), "and not in the repository root");
    }

    /** R-056's mutation: a command the reader cannot read gives the run nothing, so the test above would see none of it. */
    @Test
    void aRunWithNoEnvironmentDoesNotSeeTheChecksVariable() throws Exception {
        CaseRun.Result r = run("cat input.txt | NAME=x $COBCRUN_DIRECT ./prog", 20, "unread");
        assertNull(r.failure());
        assertEquals("env=null debug=null date=null\nargs=[]\nfiles=[input.txt, prog.cob]\nstdin=\n", text(r));
    }

    @Test
    void aProgramThatNeverEndsIsKilledAtItsLimit() throws Exception {
        long start = System.nanoTime();
        CaseRun.Result r = run("MODE=loop $COBCRUN_DIRECT ./prog", 2, "loop");
        assertNotNull(r.failure());
        assertTrue(r.failure().startsWith("no end in 2 s"), r.failure());
        assertTrue((System.nanoTime() - start) / 1_000_000_000 < 15, "the limit, then the kill");
    }

    @Test
    void aHeadPipeKeepsTheFirstLinesAndStopsTheProgram() throws Exception {
        CaseRun.Result r = run("MODE=many $COBCRUN_DIRECT ./prog | head -n 2", 20, "head");
        assertNull(r.failure());
        assertEquals("line 1\nline 2\n", text(r));
        assertEquals(1000, text(run("MODE=many $COBCRUN_DIRECT ./prog", 20, "nohead")).lines().count());
    }

    @Test
    void anUncaughtExceptionAndAnAbendAreFailuresAndAReturnCodeIsNot() throws Exception {
        assertTrue(run("MODE=throw $COBCRUN_DIRECT ./prog", 20, "throw").failure().startsWith("Exception in thread"));
        assertTrue(run("MODE=abend $COBCRUN_DIRECT ./prog", 20, "abend").failure().startsWith("CEE3ABD abend"));
        CaseRun.Result rc = run("MODE=rc $COBCRUN_DIRECT ./prog", 20, "rc");
        assertNull(rc.failure());
        assertEquals("done\n", text(rc));
    }
}
