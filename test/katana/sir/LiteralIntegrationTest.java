package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.oracle.nist.Cobc;
import katana.oracle.nist.Runner;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import katana.sir.types.Value.Literal.Kind;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SIR-08's oracle (tithe pass 2, A3): literals DISPLAYed by a program cobc compiles under `-std=ibm`
 * print their payloads: the characters, the bytes, the characters and X'00', or, moved to an
 * `S9(18)V9(18)` field, the unscaled value at its scale. National, DBCS, boolean and CICS literals cobc
 * does not print as Katana means them, and are counted, not compared. Every literal of the corpora is
 * run through it by `CorpusIntegrationTest` (`IrCorpus`), which builds them once (tithe pass 2, C4); here, the
 * mutations that show the comparison failing.
 */
@Tag("integration")
class LiteralIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";

    /** R-056's mutation: a literal whose payload is not what cobc prints is caught, of each compared kind. */
    @Test
    void aWrongPayloadIsCaught() throws Exception {
        List<Value.Literal> wrong = List.of(new Value.Literal("X'C1'", Kind.HEX, "C2", 0), new Value.Literal("'A  B'", Kind.ALPHANUMERIC, "A B", 0),
                new Value.Literal("Z'A'", Kind.NULL_TERMINATED, "B", 0), new Value.Literal("1.50", Kind.NUMERIC, "15", 2),
                new Value.Literal("1.5E+3", Kind.FLOAT, "15", 1), new Value.Literal("-0", Kind.NUMERIC, "0", 0));
        for (Value.Literal l : wrong) assertFalse(mismatches(List.of(l), Files.createDirectories(ROOT.resolve("build/sir-literals/wrong"))).isEmpty(), l + " went unnoticed");
    }

    /** Whether cobc prints it as its payload: characters it can write, a number that fits the field. */
    static boolean printable(Value.Literal l) {
        return switch (l.kind()) {
            case ALPHANUMERIC, NULL_TERMINATED -> !l.payload().isEmpty() && l.payload().chars().allMatch(c -> c < 0x100);
            case HEX -> !l.payload().isEmpty();
            case NUMERIC, FLOAT -> {
                BigDecimal v = new BigDecimal(new BigInteger(l.payload()), l.scale()).stripTrailingZeros();
                yield v.precision() - v.scale() <= 18 && v.scale() <= 18;
            }
            default -> false;
        };
    }

    /** Each literal whose line is not its payload: a number written with a decimal comma in a program of `DECIMAL-POINT IS COMMA`, the rest in another. */
    static List<String> mismatches(List<Value.Literal> literals, Path dir) throws Exception {
        List<String> out = new ArrayList<>(mismatches(literals.stream().filter(l -> !comma(l)).toList(), dir, false));
        out.addAll(mismatches(literals.stream().filter(LiteralIntegrationTest::comma).toList(), dir, true));
        return out;
    }

    private static boolean comma(Value.Literal l) {
        return l.numeric() && l.text().contains(",");
    }

    /** One program DISPLAYing each literal, run under cobc; each literal whose line is not its payload. */
    private static List<String> mismatches(List<Value.Literal> literals, Path dir, boolean comma) throws Exception {
        if (literals.isEmpty()) return List.of();
        StringBuilder source = new StringBuilder("""
                IDENTIFICATION DIVISION.
                PROGRAM-ID. LITERALS.
                """ + (comma ? "ENVIRONMENT DIVISION.\nCONFIGURATION SECTION.\nSPECIAL-NAMES. DECIMAL-POINT IS COMMA.\n" : "") + """
                DATA DIVISION.
                WORKING-STORAGE SECTION.
                01 W PIC S9(18)V9(18) SIGN LEADING SEPARATE.
                PROCEDURE DIVISION.
                """);
        List<byte[]> expected = new ArrayList<>();
        for (Value.Literal l : literals) {
            source.append(l.numeric() ? "MOVE " + l.text() + " TO W\nDISPLAY W\n" : "DISPLAY " + l.text() + "\n");
            expected.add(expected(l));
        }
        source.append("STOP RUN.\n");
        Files.writeString(dir.resolve("LITERALS.cbl"), source, StandardCharsets.ISO_8859_1);
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        Runner.Result compiled = cobc.compile(dir, "LITERALS.cbl", List.of("-free"));
        if (!compiled.ok()) return List.of("cobc refused the program: " + compiled.output());
        byte[] out = cobc.execute(dir, "LITERALS", Map.of()).output().getBytes(StandardCharsets.ISO_8859_1);
        List<String> mismatches = new ArrayList<>();
        int at = 0;
        for (int i = 0; i < literals.size(); i++) {
            byte[] e = expected.get(i);
            boolean same = at + e.length < out.length && java.util.Arrays.equals(out, at, at + e.length, e, 0, e.length) && out[at + e.length] == '\n';
            if (!same) {
                int end = at;
                while (end < out.length && out[end] != '\n') end++;
                mismatches.add(literals.get(i) + " printed " + HexFormat.of().formatHex(out, Math.min(at, out.length), end) + ", not " + HexFormat.of().formatHex(e));
                if (mismatches.size() >= 20) break;
                at = end + 1;
            } else {
                at += e.length + 1;
            }
        }
        return mismatches;
    }

    /** What cobc should print for a literal, by SIR-08's meaning of its payload. */
    private static byte[] expected(Value.Literal l) {
        return switch (l.kind()) {
            case HEX -> HexFormat.of().parseHex(l.payload());
            case NULL_TERMINATED -> (l.payload() + "\0").getBytes(StandardCharsets.ISO_8859_1);
            case NUMERIC, FLOAT -> {
                BigDecimal v = new BigDecimal(new BigInteger(l.payload()), l.scale());
                String digits = v.abs().setScale(18).unscaledValue().toString();
                yield ((l.payload().startsWith("-") ? "-" : "+") + "0".repeat(36 - digits.length()) + digits).getBytes(StandardCharsets.ISO_8859_1);
            }
            default -> l.payload().getBytes(StandardCharsets.ISO_8859_1);
        };
    }

    /** Every literal of a program's IR (its items' VALUEs and 88s, its statements, its nested programs) into `out`. */
    static void literals(Program p, Set<Value.Literal> out) {
        program(p, out);
    }

    private static void program(Program p, Set<Value.Literal> out) {
        for (Item i : p.items()) {
            value(i.value(), out);
            for (Item.Condition c : i.conditions()) c.values().forEach(v -> value(v, out));
        }
        for (Procedure.Paragraph para : p.procedure().paragraphs()) statements(para.statements(), out);
        for (Program n : p.nested()) program(n, out);
    }

    private static void statements(List<Statement> statements, Set<Value.Literal> out) {
        for (Statement s : statements) {
            s.operands().forEach(o -> value(o.value(), out));
            for (Statement.Branch b : s.branches()) {
                value(b.condition(), out);
                statements(b.statements(), out);
            }
        }
    }

    private static void value(Value v, Set<Value.Literal> out) {
        switch (v) {
            case Value.Literal l -> out.add(l);
            case Value.Figurative f -> value(f.all(), out);
            case Value.Field f -> {
                f.subscripts().forEach(s -> value(s, out));
                value(f.start(), out);
                value(f.length(), out);
            }
            case Value.Expression e -> e.operands().forEach(o -> value(o, out));
            case Value.ConditionName c -> c.subscripts().forEach(s -> value(s, out));
            case null, default -> { }
        }
    }
}
