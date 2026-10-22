package katana.oracle.prove;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import katana.oracle.compare.Layout;
import katana.sir.types.Attributes;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 8A.3's boundary inputs (research §50): from the IR of the programs a job runs, the numeric items its arithmetic
 * names; in an input dataset whose copybook holds a field of such a name, that field driven record by record to its
 * PICTURE's largest magnitude, the same negative, a value whose decimals are all nines, and one record in four left
 * as it was. Keys and every other field stay, so the records still meet across files.
 */
public final class Boundary {
    private static final Set<String> ARITHMETIC = Set.of("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "COMPUTE");
    /** EBCDIC overpunch as CardDemo's ASCII data holds it (FIXTURE-10): a signed zoned field's last byte, +0 to +9 and -0 to -9. */
    private static final String POSITIVE = "{ABCDEFGHI", NEGATIVE = "}JKLMNOPQR";

    private Boundary() {}

    /** The names of the numeric items any arithmetic statement of the programs names, operand or receiver. */
    // Decision: R-072
    public static Set<String> names(List<Program> programs) {
        Set<String> out = new TreeSet<>();
        for (Program p : programs) for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) statement(p, s, out);
        return out;
    }

    private static void statement(Program p, Statement s, Set<String> out) {
        if (ARITHMETIC.contains(s.verb())) for (Statement.Operand o : s.operands()) fields(p, o.value(), out);
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) statement(p, t, out);
    }

    private static void fields(Program p, Value v, Set<String> out) {
        if (v instanceof Value.Field f && p.items().get(f.item() - 1).attributes().category() == Attributes.Category.NUMERIC) out.add(p.items().get(f.item() - 1).name());
        if (v instanceof Value.Expression e) for (Value x : e.operands()) fields(p, x, out);
    }

    /** The fields of a layout the names hold: zoned ones, which ASCII data carries as digits and overpunch. */
    public static List<Layout.Field> targets(Layout layout, Set<String> names) {
        List<Layout.Field> out = new ArrayList<>();
        for (Layout.Field f : layout.fields()) {
            String leaf = f.path().substring(f.path().lastIndexOf('.') + 1).replaceAll("\\(\\d+\\)", "");
            if (f.kind() == Layout.Kind.ZONED && names.contains(leaf)) out.add(f);
        }
        return out;
    }

    /** The records with each target field driven to its boundary in turn: largest, largest negative, decimals all nines, as it was. */
    public static byte[] perturb(byte[] records, int lrecl, List<Layout.Field> targets) {
        byte[] out = records.clone();
        for (int r = 0; r * lrecl + lrecl <= out.length; r++) {
            if (r % 4 == 3) continue;
            for (Layout.Field f : targets) {
                String digits = switch (r % 4) {
                    case 2 -> "0".repeat(Math.max(0, f.length() - Math.max(1, f.scale()))) + "9".repeat(Math.min(f.length(), Math.max(1, f.scale())));
                    default -> "9".repeat(f.length());
                };
                boolean negative = r % 4 == 1 && f.signed();
                if (f.signed()) {
                    int last = digits.charAt(digits.length() - 1) - '0';
                    digits = digits.substring(0, digits.length() - 1) + (negative ? NEGATIVE : POSITIVE).charAt(last);
                }
                System.arraycopy(digits.getBytes(StandardCharsets.US_ASCII), 0, out, r * lrecl + f.offset(), f.length());
            }
        }
        return out;
    }
}
