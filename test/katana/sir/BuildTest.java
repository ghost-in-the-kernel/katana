package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.check.Check;
import katana.sir.json.IrJson;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/** 2.5 on COPYREC: the IR built from its source says what the hand-written one (SirTypesTest) says, and round-trips through JSON. */
class BuildTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String HASH = "sha256:feca25654120f84bcd60468e51f9dfe083ab409a44bac92c65771ab6e525fcfc";

    private static Build.Result copyrec() {
        Path trivial = ROOT.resolve("oracle/fixture/trivial");
        Parse.Result parse = Parse.file(trivial.resolve("cbl/COPYREC.cbl"), new Copybooks(List.of(trivial.resolve("cpy")), n -> 8), 8);
        return Build.program(parse, HASH);
    }

    @Test
    void theDataMapIsTheHandWrittenOne() {
        Program p = copyrec().program();
        List<Item> hand = SirTypesTest.items();
        assertEquals(hand.size(), p.items().size());
        for (int i = 0; i < hand.size(); i++) {
            Item a = hand.get(i), b = p.items().get(i);
            assertEquals(List.of(a.name(), a.level(), a.parent(), a.record(), a.section(), a.offset(), a.length(), a.attributes(), String.valueOf(a.value())),
                    List.of(b.name(), b.level(), b.parent(), b.record(), b.section(), b.offset(), b.length(), b.attributes(), String.valueOf(b.value())));
        }
        assertEquals("COPYREC", p.header().program());
    }

    @Test
    void theProcedureAndFilesAreThere() {
        Program p = copyrec().program();
        assertEquals(List.of("IN-FILE", "OUT-FILE"), p.files().stream().map(f -> f.name()).toList());
        assertEquals(List.of(List.of(1), List.of(5)), p.files().stream().map(f -> f.records()).toList());
        Procedure.Paragraph only = p.procedure().paragraphs().get(0);
        assertEquals(List.of("OPEN", "PERFORM", "CLOSE", "DISPLAY", "STOP"), only.statements().stream().map(Statement::verb).toList());
        Statement perform = only.statements().get(1);
        Statement read = perform.branches().get(0).statements().get(0);
        assertEquals("READ", read.verb());
        assertTrue(read.operands().stream().anyMatch(o -> o.value().equals(new Value.File("IN-FILE"))), read.toString());
        assertEquals(List.of("WRITE", "ADD"), read.branches().get(1).statements().stream().map(Statement::verb).toList());
    }

    @Test
    void theIrRoundTripsThroughJson() {
        Program p = copyrec().program();
        String json = IrJson.write(p);
        Program back = IrJson.read(json, Program.class);
        assertEquals(p, back);
        assertEquals(json, IrJson.write(back));
    }

    @Test
    void theCheckFindsTheFilesWithoutAStatus() {
        List<String> findings = Check.findings(copyrec().program());
        assertEquals(2, findings.size(), findings.toString());
        assertTrue(findings.get(0).startsWith("COPYREC: file IN-FILE has no FILE STATUS item"), findings.get(0));
    }

    /** A Name has a component `kind`; the codec's own key must not be one (it was, and every CICS program failed to round-trip). */
    @Test
    void everyValueKindRoundTrips() {
        List<Value> values = List.of(new Value.Name("cics", "EIBCALEN"), new Value.Target(3, 4), new Value.File("F"),
                new Value.Figurative("ALL", new Value.Literal("'*'", Value.Literal.Kind.ALPHANUMERIC, "*", 0)), new Value.Expression("+", List.of(new Value.Literal("1", Value.Literal.Kind.NUMERIC, "1", 0))));
        for (Value v : values) assertEquals(v, IrJson.read(IrJson.write(v), Value.class), IrJson.write(v));
    }
}
