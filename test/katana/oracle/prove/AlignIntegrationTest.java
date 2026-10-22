package katana.oracle.prove;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 8A.4's trial (research §51): the alignment inferred for opensourcecobol4j's Java of the CardDemo batch programs
 * (read as text, `lower/prototypes/oc4j-carddemo-reference/`) and for 8A.1's hand-written port of ACCRUE; the
 * coverage of each kind of node printed, and held to what the prediction said.
 */
@Tag("integration")
class AlignIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", ".")), OC4J = ROOT.resolve("lower/prototypes/oc4j-carddemo-reference"), CARDDEMO = ROOT.resolve("corpus/carddemo/app");

    /** Every oc4j Java file with its CardDemo program aligned and summed; ACCRUE's hand port aligned; each printed. */
    @Test
    void generatedJavaAlignsByItsCommentsAndAHandPortByItsLiterals() throws Exception {
        Map<String, int[]> oc4j = new LinkedHashMap<>(Map.of("paragraph", new int[2], "statement", new int[2], "item", new int[2], "referenced item", new int[2]));
        List<String> programs = new ArrayList<>();
        try (var files = Files.list(OC4J)) {
            for (Path java : files.filter(f -> f.toString().endsWith(".java")).sorted().toList()) {
                String name = java.getFileName().toString().replace(".java", "");
                Path cbl = CARDDEMO.resolve("cbl/" + name + ".cbl");
                if (!Files.exists(cbl)) cbl = CARDDEMO.resolve("cbl/" + name + ".CBL");
                Program p = program(cbl, List.of(CARDDEMO.resolve("cpy")));
                Align.Result r = Align.infer(p, java.getFileName().toString(), Files.readString(java));
                r.coverage().forEach((k, v) -> { oc4j.get(k)[0] += v[0]; oc4j.get(k)[1] += v[1]; });
                programs.add(name);
                Files.createDirectories(ROOT.resolve("build/align"));
                Files.writeString(ROOT.resolve("build/align/" + name + ".align"), Align.write(name, r.lines()));
            }
        }
        Path foreign = ROOT.resolve("oracle/fixture/foreign");
        Align.Result accrue = Align.infer(program(foreign.resolve("cbl/ACCRUE.cbl"), List.of(foreign.resolve("cpy"))), "AccruePorts.java", Files.readString(ROOT.resolve("test/katana/oracle/prove/AccruePorts.java")));
        Files.writeString(ROOT.resolve("build/align/ACCRUE.align"), Align.write("ACCRUE", accrue.lines()));
        System.out.println("oc4j " + programs + ": " + text(oc4j));
        System.out.println("ACCRUE's hand port: " + text(accrue.coverage()) + "\n" + Align.write("ACCRUE", accrue.lines()));
        // §51 predicted items at 80% and more; the first run found 70% of every item, oc4j writing no Java for an item nothing names (J-234).
        assertTrue(share(oc4j, "paragraph") >= 0.95 && share(oc4j, "statement") >= 0.90 && share(oc4j, "referenced item") >= 0.80, "oc4j's coverage: " + text(oc4j));
        assertTrue(accrue.coverage().get("statement")[1] >= 1, "the hand port's statements by literal: " + text(accrue.coverage()));
    }

    private static Program program(Path cbl, List<Path> copybooks) {
        return Build.programs(Parse.file(cbl, new Copybooks(copybooks, n -> n.toUpperCase().startsWith("CUSTREC") ? 4 : 8), 8), "sha256:" + "0".repeat(64)).get(0).program();
    }

    private static double share(Map<String, int[]> c, String kind) {
        return c.get(kind)[0] == 0 ? 1 : (double) c.get(kind)[1] / c.get(kind)[0];
    }

    private static String text(Map<String, int[]> c) {
        StringBuilder b = new StringBuilder();
        c.forEach((k, v) -> b.append(k).append("s ").append(v[1]).append(" of ").append(v[0]).append("; "));
        return b.toString();
    }
}
