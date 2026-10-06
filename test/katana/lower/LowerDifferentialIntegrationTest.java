package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The lowering verb by verb (phase 3.3): small programs written for it (test/katana/lower/programs/), each
 * run by cobc -std=ibm and through the Java lane (`JavaRunner`), their output equal line for line. A
 * program's lowering findings are in the failure, with the IR of what was not lowered. Its mutation: the
 * outputs of two programs swapped differ.
 */
@Tag("integration")
class LowerDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path PROGRAMS = ROOT.resolve("test/katana/lower/programs");

    record Ran(String name, String cobc, String java) {}

    static List<Ran> run() throws Exception {
        List<Path> sources;
        try (Stream<Path> list = Files.list(PROGRAMS)) {
            sources = list.filter(p -> p.toString().endsWith(".cbl")).sorted().toList();
        }
        List<Ran> out = new ArrayList<>();
        Cobc cobc = new Cobc(Duration.ofSeconds(60), "ibm");
        JavaRunner lane = new JavaRunner(60);
        for (Path source : sources) {
            String name = source.getFileName().toString().replace(".cbl", "");
            Path dir = Files.createDirectories(ROOT.resolve("build/lower-differential/" + name));
            Files.copy(source, dir.resolve(name + ".cbl"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            katana.oracle.nist.Runner.Result c = cobc.compile(dir, name + ".cbl", List.of());
            String cobcOut = c.ok() ? cobc.execute(dir, name, Map.of()).output() : "cobc: " + c.output();
            katana.oracle.nist.Runner.Result j = lane.program(dir, name + ".cbl");
            String javaOut = j.ok() ? lane.run(dir, name, Map.of(), new byte[0]).output() : "lower or javac: " + j.output();
            String findings = Files.exists(dir.resolve(name + ".class-name")) ? Files.readString(dir.resolve(name + ".class-name"), StandardCharsets.UTF_8) : "";
            out.add(new Ran(name, cobcOut, javaOut + (findings.lines().count() > 1 ? "\nfindings:\n" + findings : "")));
        }
        return out;
    }

    @Test
    void eachProgramPrintsWhatCobcPrints() throws Exception {
        List<String> differ = new ArrayList<>();
        for (Ran r : run()) if (!r.cobc().equals(r.java())) differ.add(r.name() + "\ncobc:\n" + r.cobc() + "\njava:\n" + r.java());
        assertEquals(List.of(), differ);
    }

    /** R-056's mutation: each program's output held to the next program's cobc output differs. */
    @Test
    void anotherProgramsOutputDiffers() throws Exception {
        List<Ran> ran = run();
        for (int k = 0; k < ran.size(); k++) org.junit.jupiter.api.Assertions.assertNotEquals(ran.get((k + 1) % ran.size()).cobc(), ran.get(k).java(), ran.get(k).name());
    }
}
