package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.build.Roles;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import org.junit.jupiter.api.Test;

/**
 * The role vocabulary pinned (tithe pass 2, A6): sir/roles.golden is `Roles.vocabulary()`, read from the
 * grammar, so a grammar or walker change that adds a verb, role or branch label lands as a diff phase 3's
 * lowering must answer. Its oracle is the programs: every verb, role and label the IR of the goldens in
 * sir/golden/ carries is in the vocabulary. `-Dkatana.record=true` rewrites it.
 */
class RolesGoldenTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("sir/roles.golden");
    private static final String HEADER = """
            # The statement vocabulary (tithe pass 2, A6), read from the grammar by sir/build/Roles.java: each
            # verb, then `roles` its operands can carry and `branches` the labels of its nested statements.
            # Phase 3's lowering covers every line. Written by `gradle test -Dkatana.record=true`.
            """;

    static String text(Map<String, Roles.Verb> vocabulary) {
        StringBuilder out = new StringBuilder(HEADER);
        vocabulary.forEach((verb, v) -> {
            out.append(verb).append('\n');
            out.append("  roles ").append(String.join(" ", v.roles())).append('\n');
            if (!v.branches().isEmpty()) out.append("  branches ").append(String.join(" ", v.branches())).append('\n');
        });
        return out.toString();
    }

    @Test
    void theVocabularyIsItsGolden() throws Exception {
        String got = text(Roles.vocabulary());
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, got);
        assertEquals(Files.readString(GOLDEN), got);
    }

    @Test
    void everyVerbRoleAndLabelTheGoldenProgramsCarryIsInIt() {
        Map<String, Roles.Verb> vocabulary = Roles.vocabulary();
        Set<String> seen = new TreeSet<>();
        assertEquals(Set.of(), outside(programs(), vocabulary, seen));
        assertTrue(seen.size() >= 50, "the golden programs carry " + seen.size() + " verb-role pairs; too few to refute anything");
    }

    @Test
    void theCheckFailsWhenARoleIsMissing() {
        Map<String, Roles.Verb> vocabulary = new java.util.TreeMap<>(Roles.vocabulary());
        Roles.Verb move = vocabulary.get("MOVE");
        vocabulary.put("MOVE", new Roles.Verb(Set.of(), move.branches()));
        Set<String> outside = outside(programs(), vocabulary, new TreeSet<>());
        assertTrue(outside.stream().anyMatch(o -> o.startsWith("MOVE ")), "dropping MOVE's roles went unnoticed");
    }

    /** What the programs (and those nested in them) carry that the vocabulary does not; `seen` gets each verb-role pair. */
    static Set<String> outside(List<Program> programs, Map<String, Roles.Verb> vocabulary, Set<String> seen) {
        Set<String> outside = new TreeSet<>();
        List<Program> all = new ArrayList<>();
        programs.forEach(p -> add(p, all));
        for (Program p : all) for (Procedure.Paragraph para : p.procedure().paragraphs()) check(para.statements(), vocabulary, outside, seen);
        return outside;
    }

    private static void check(List<Statement> statements, Map<String, Roles.Verb> vocabulary, Set<String> outside, Set<String> seen) {
        for (Statement s : statements) {
            Roles.Verb v = vocabulary.get(s.verb());
            if (v == null) {
                outside.add(s.verb() + ": not a verb");
                continue;
            }
            for (Statement.Operand o : s.operands()) {
                seen.add(s.verb() + " " + o.role());
                if (!v.roles().contains(o.role())) outside.add(s.verb() + " role " + o.role());
            }
            for (Statement.Branch b : s.branches()) {
                if (!v.branches().contains(b.label())) outside.add(s.verb() + " branch " + b.label());
                check(b.statements(), vocabulary, outside, seen);
            }
        }
    }

    /** The golden programs. */
    private static List<Program> programs() {
        Path golden = ROOT.resolve("sir/golden");
        List<Path> sources = new ArrayList<>(List.of(golden.resolve("PROBE.cbl")));
        for (String p : IrGoldenTest.NIST) sources.add(golden.resolve(p + ".cbl"));
        List<Program> out = new ArrayList<>();
        for (Path source : sources) {
            Parse.Result parse = Parse.file(source, new Copybooks(List.of(golden), n -> 8), 8);
            for (Build.Result r : Build.programs(parse, "sha256:" + "0".repeat(64))) out.add(r.program());
        }
        return out;
    }

    private static void add(Program p, List<Program> out) {
        out.add(p);
        for (Program n : p.nested()) add(n, out);
    }
}
