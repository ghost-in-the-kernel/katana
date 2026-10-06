package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/** Tithe pass 2, A1, in the fast loop: no token of a statement is left out of the IR, on COPYREC, the probe and the NIST goldens. */
class NoTokenLeftTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String HASH = "sha256:a5298bfd77c97fd3f26b37103ba2e6ed51a164dbd90b0435ff2d88f27939e77b";

    static List<String> missing(Path program, Path copybooks) {
        Parse.Result parse = Parse.file(program, new Copybooks(List.of(copybooks), n -> 8), 8);
        return TokenCoverage.missing(parse, Build.programs(parse, HASH).stream().map(Build.Result::program).toList());
    }

    @Test
    void everyTokenIsInTheIr() {
        List<String> missing = new ArrayList<>();
        Path trivial = ROOT.resolve("oracle/fixture/trivial"), golden = ROOT.resolve("sir/golden");
        missing.addAll(missing(trivial.resolve("cbl/COPYREC.cbl"), trivial.resolve("cpy")));
        missing.addAll(missing(golden.resolve("PROBE.cbl"), golden));
        for (String p : IrGoldenTest.NIST) missing.addAll(missing(golden.resolve(p + ".cbl"), golden));
        assertEquals(List.of(), missing);
    }

    /** The check's own failing case: the probe's IR with its keywords taken out leaves ROUNDED, TALLYING and RUN behind. */
    @Test
    void theCheckFailsWhenAKeywordIsDropped() {
        Path golden = ROOT.resolve("sir/golden");
        Parse.Result parse = Parse.file(golden.resolve("PROBE.cbl"), new Copybooks(List.of(golden), n -> 8), 8);
        Program p = Build.program(parse, HASH).program();
        List<Procedure.Paragraph> stripped = new ArrayList<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) {
            stripped.add(new Procedure.Paragraph(g.index(), g.name(), g.section(), g.statements().stream().map(NoTokenLeftTest::strip).toList(), g.origin()));
        }
        Procedure q = p.procedure();
        Program without = new Program(p.header(), p.items(), p.files(), new Procedure(q.using(), q.returning(), q.sections(), stripped, q.edges()), p.calls(), p.nested());
        String missing = String.join(" ", TokenCoverage.missing(parse, without));
        for (String word : List.of("ROUNDED in COMPUTE", "TALLYING in INSPECT", "RUN in STOP", "ANY in EVALUATE", "AFTER in PERFORM")) assertTrue(missing.contains(word), word + " in " + missing);
    }

    private static Statement strip(Statement s) {
        List<Statement.Operand> kept = s.operands().stream().filter(o -> !(o.value() instanceof Value.Name n && n.kind().equals("keyword"))).toList();
        List<Statement.Branch> branches = s.branches().stream().map(b -> new Statement.Branch(b.label(), strip(b.condition()), b.statements().stream().map(NoTokenLeftTest::strip).toList())).toList();
        return new Statement(s.verb(), kept, branches, s.origin());
    }

    private static Value strip(Value v) {
        if (!(v instanceof Value.Expression e)) return v;
        return new Value.Expression(e.operator(), e.operands().stream().filter(o -> !(o instanceof Value.Name n && n.kind().equals("keyword"))).map(NoTokenLeftTest::strip).toList());
    }
}
