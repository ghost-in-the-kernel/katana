package katana.oracle.prove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.tools.ToolProvider;
import katana.findings.Divergence;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.lower.Lower;
import katana.oracle.fixture.Fixture;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 8A.5's check (research §52): the mutation harness, each kind's generated case run as Katana's literal Java with
 * that kind's rule broken and attributed to that kind first; the same with its probe withheld, unattributed; and
 * 8A.1's hand-written HALF_UP port of ACCRUE attributed to scale truncation.
 */
@Tag("integration")
class AttributionIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    /** Each kind's mutant through every probe, then without its own; ACCRUE's naive port; each result printed. */
    @Test
    void eachMutantIsAttributedToItsKindAndUnattributedWithoutItsProbe() throws Exception {
        String profile = Files.readAllLines(ROOT.resolve("oracle/fixture/trivial/COPY.fixture")).stream().filter(l -> l.startsWith("profile ")).findFirst().orElseThrow();
        List<String> wrong = new ArrayList<>();
        for (Divergence.Kind kind : Divergence.Kind.values()) {
            Generators.Case c = Generators.generate(kind, 49);
            Path dir = ROOT.resolve("build/attribution/" + kind.code);
            Fixture fixture = Fixture.read(Files.readString(Generators.write(c, dir, profile)));
            String className = lowered(dir.resolve("cbl/" + c.program() + ".cbl"), dir.resolve("cpy"), dir.resolve("classes"));
            Map<Divergence.Kind, Map<String, List<String>>> probes = probes(c.program(), className, dir.resolve("classes"));
            Prove.Setup s = setup(dir, dir, dir.resolve("run"));
            Attribute.Result r = Attribute.attribute(fixture, s, probes.get(kind), probes);
            System.out.println(kind.code + "'s mutant: " + r.text());
            if (r.first() != kind || r.ranks().get(0).reproduced() != r.cells().size() || r.ranks().size() != 1) wrong.add(kind.code + " attributed as " + r.ranks());
            Map<Divergence.Kind, Map<String, List<String>>> without = new LinkedHashMap<>(probes);
            without.remove(kind);
            Attribute.Result w = Attribute.attribute(fixture, setup(dir, dir, dir.resolve("without")), probes.get(kind), without);
            System.out.println(kind.code + "'s mutant, its probe withheld: " + w.text());
            if (w.first() == kind || w.unattributed().size() != w.cells().size()) wrong.add(kind.code + " withheld still attributed: " + w.ranks());
        }
        Path foreign = ROOT.resolve("oracle/fixture/foreign"), dir = ROOT.resolve("build/attribution/ACCRUE");
        String className = lowered(foreign.resolve("cbl/ACCRUE.cbl"), foreign.resolve("cpy"), dir.resolve("classes"));
        List<String> naive = List.of(java(), "-cp", System.getProperty("java.class.path"), AccruePorts.class.getName() + "$Naive");
        Attribute.Result accrue = Attribute.attribute(Fixture.read(Files.readString(foreign.resolve("ACCRUE.fixture"))), setup(ROOT, foreign, dir.resolve("run")), Map.of("ACCRUE", naive),
                probes("ACCRUE", className, dir.resolve("classes")));
        System.out.println("ACCRUE's naive port: " + accrue.text());
        if (accrue.first() != Divergence.Kind.DV_02 || accrue.ranks().get(0).reproduced() != accrue.cells().size()) wrong.add("ACCRUE's naive port attributed as " + accrue.ranks());
        assertEquals(List.of(), wrong);
    }

    /** Each kind's probe: the lowered program with that kind's rule broken (LANE-23). */
    static Map<Divergence.Kind, Map<String, List<String>>> probes(String program, String className, Path classes) {
        Map<Divergence.Kind, Map<String, List<String>>> out = new LinkedHashMap<>();
        for (Divergence.Kind k : Divergence.Kind.values())
            out.put(k, Map.of(program, List.of(java(), "-Dkatana.mutate=" + k.code, "-cp", classes + java.io.File.pathSeparator + System.getProperty("java.class.path"), className)));
        return out;
    }

    /** The lanes' setup: `repo` the fixture's sources, `dir` its programs and copybooks, `work` under build/. */
    static Prove.Setup setup(Path repo, Path dir, Path work) {
        return new Prove.Setup(ROOT, Map.of("repo", repo), dir.resolve("cbl"), List.of(dir.resolve("cpy")), work, Duration.ofSeconds(60));
    }

    static String java() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }

    /** A COBOL program lowered literal and compiled into `classes`; its class name. */
    static String lowered(Path cbl, Path cpy, Path classes) throws Exception {
        Files.createDirectories(classes);
        Program program = Build.programs(Parse.file(cbl, new Copybooks(List.of(cpy), n -> 8), 8), "sha256:" + "0".repeat(64)).get(0).program();
        String className = Lower.className(program);
        Files.writeString(classes.resolve(className + ".java"), Lower.java(program, className).java());
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        int status = ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, "-classpath", System.getProperty("java.class.path"), "-d", classes.toString(), classes.resolve(className + ".java").toString());
        assertTrue(status == 0, className + " compiles: " + diagnostics.toString(StandardCharsets.UTF_8));
        return className;
    }
}
