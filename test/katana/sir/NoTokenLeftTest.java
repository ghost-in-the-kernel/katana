package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/** Tithe pass 2, A1, in the fast loop: no token of a statement is left out of the IR, on COPYREC, the probe and the NIST goldens. */
class NoTokenLeftTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";

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
            stripped.add(new Procedure.Paragraph(g.index(), g.name(), g.section(), g.statements().stream().map(NoTokenLeftTest::strip).toList(), g.sentences(), g.origin()));
        }
        Procedure q = p.procedure();
        Program without = new Program(p.header(), p.items(), p.files(), new Procedure(q.using(), q.returning(), q.sections(), stripped, q.edges()), p.calls(), p.nested());
        String missing = String.join(" ", TokenCoverage.missing(parse, without));
        for (String word : List.of("ROUNDED in COMPUTE", "TALLYING in INSPECT", "RUN in STOP", "ANY in EVALUATE", "AFTER in PERFORM")) assertTrue(missing.contains(word), word + " in " + missing);
    }

    /** The same for SIR-09 and SIR-10: OPTIONAL, both FILE STATUS items, GLOBAL and a USE's file taken out are left behind. */
    @Test
    void theCheckFailsWhenAFileOrUseWordIsDropped() {
        Path golden = ROOT.resolve("sir/golden");
        Parse.Result parse = Parse.file(golden.resolve("PROBE.cbl"), new Copybooks(List.of(golden), n -> 8), 8);
        Program p = Build.program(parse, HASH).program();
        List<FileDescriptor> files = p.files().stream().map(f -> new FileDescriptor(f.name(), f.assign(), false, f.organization(), f.access(), f.recordKey(), f.alternateKeys(),
                f.relativeKey(), 0, 0, f.records(), f.minRecord(), f.maxRecord(), f.origin())).toList();
        List<Procedure.Section> sections = p.procedure().sections().stream().map(s -> s.use() == null ? s : new Procedure.Section(s.name(), true,
                new Procedure.Use(s.use().kind(), false, s.use().mode(), s.use().files().isEmpty() ? List.of() : List.of("REL-FILE"), s.use().outerFiles(), s.use().debugging()), s.origin())).toList();
        Procedure q = p.procedure();
        Program without = new Program(p.header(), p.items(), files, new Procedure(q.using(), q.returning(), sections, q.paragraphs(), q.edges()), p.calls(), p.nested());
        String missing = String.join(" ", TokenCoverage.missing(parse, without));
        for (String word : List.of("OPTIONAL in SELECT SEQ-FILE", "STATUS in SELECT SEQ-FILE", "FS in SELECT SEQ-FILE", "VS in SELECT SEQ-FILE", "SEQ-FILE in USE of SEQ-ERRORS", "GLOBAL in USE of INPUT-ERRORS")) {
            assertTrue(missing.contains(word), word + " in " + missing);
        }
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
