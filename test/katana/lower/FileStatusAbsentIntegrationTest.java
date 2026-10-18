package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.oracle.nist.Cobc;
import katana.oracle.nist.Runner;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Value;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * T-E3 (research §33, §34): a file-status item whose condition-names name only '00' and '10', CBEXPORT's
 * and CBIMPORT's pattern, holds '35' after an OPEN INPUT of an absent file, in both lanes, a value none of
 * them names, so the program reads a value its `enum` fact says it never holds; with the file present the
 * status is '00' and the check is red.
 */
@Tag("integration")
class FileStatusAbsentIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    @Test
    void anAbsentFileLeavesAStatusNoConditionNameNamesInBothLanes() throws Exception {
        for (Runner lane : List.of(new Cobc(Duration.ofSeconds(30), "ibm"), new JavaRunner(60))) {
            String absent = ran(lane, false);
            assertTrue(absent.contains("STATUS 35") && absent.contains("UNNAMED"), lane + ": " + absent);
            assertTrue(unnamed("35"), "no condition-name of WS-IN-STATUS names 35");
            String present = ran(lane, true);
            assertTrue(present.contains("STATUS 00") && present.contains("NAMED") && !present.contains("UNNAMED"), lane + ": " + present);
            assertFalse(unnamed("00"), "the mutation: with the file present the check is red");
        }
    }

    /** Whether no condition-name of STATUS35's WS-IN-STATUS names the value, read on its IR. */
    static boolean unnamed(String status) throws Exception {
        Program p = Build.program(Parse.file(ROOT.resolve("test/katana/lower/status/STATUS35.cbl"), new Copybooks(List.of(), n -> 8), 8), "sha256:" + "0".repeat(64)).program();
        Item item = p.items().stream().filter(i -> i.name().equals("WS-IN-STATUS")).findFirst().orElseThrow();
        assertEquals(item.id(), p.files().get(0).status(), "WS-IN-STATUS is IN-FILE's status");
        return item.conditions().stream().flatMap(c -> c.values().stream()).noneMatch(v -> v instanceof Value.Literal l && l.payload().equals(status));
    }

    /** STATUS35's output, compiled and run in a folder of its own, with INFILE present or not. */
    private static String ran(Runner runner, boolean present) throws Exception {
        Path dir = Files.createTempDirectory(Files.createDirectories(ROOT.resolve("build/status35")), "run");
        Files.copy(ROOT.resolve("test/katana/lower/status/STATUS35.cbl"), dir.resolve("STATUS35.cbl"));
        if (present) Files.writeString(dir.resolve("INFILE"), "RECORD ONE");
        assertEquals(0, runner.program(dir, "STATUS35.cbl").exit(), "STATUS35 compiles");
        Runner.Result result = runner.run(dir, "STATUS35", Map.of(), new byte[0]);
        assertEquals(0, result.exit(), result.output());
        return result.output();
    }
}
