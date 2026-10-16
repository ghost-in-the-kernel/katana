package katana.lower;

import java.util.List;
import java.util.Map;
import katana.sir.types.Attributes;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Item;
import katana.sir.types.Value;

/**
 * The integer places of every operand arithmetic can meet, stated once (tithe, J-170; the class J-167
 * found: an index has no PICTURE, was given none, and VARYING wrapped at 100). Appendix A sizes each
 * intermediate result from its operands' places (PG 6.4 p. 793), so an operand sized too small cuts a
 * value silently. What has a PICTURE: its PICTURE's. What has none: IBM's implicit definition, quoted;
 * or a refusal that says why, which the lowering reports as not lowered, never a guess.
 */
final class Places {
    private Places() {}

    /** An operand's integer places, or why it has none here. */
    record Size(int places, String refused) {
        static Size of(int places) {
            return new Size(places, null);
        }

        static Size refused(String why) {
            return new Size(0, why);
        }
    }

    /**
     * Special registers with a numeric implicit definition (LR 6.4 pp. 22-28): RETURN-CODE "PICTURE
     * S9(4) USAGE BINARY", SORT-RETURN S9(4), TALLY "PICTURE 9(5) USAGE BINARY", SORT-CORE-SIZE and
     * SORT-FILE-SIZE S9(8), JSON-CODE and XML-CODE S9(9). `rt` holds none of them yet; the lowering that
     * holds one takes its places from here.
     */
    static final Map<String, Integer> REGISTERS = Map.of("RETURN-CODE", 4, "SORT-RETURN", 4, "TALLY", 5, "SORT-CORE-SIZE", 8, "SORT-FILE-SIZE", 8,
            "JSON-CODE", 9, "XML-CODE", 9);

    /** An operand's integer places, whatever it is. */
    // Decision: R-020
    static Size of(Value v, Exprs x) {
        if (x.counter(v) != null) return Size.of(counter(v, x));
        return switch (v) {
            case Value.Field f -> item(x.item(f.item()));
            case Value.Literal l when l.numeric() -> Size.of(Math.max(0, l.payload().replace("-", "").length() - l.scale()));
            case Value.Figurative f when x.numeric(f) -> Size.of(1);
            case Value.Name n when n.kind().equals("register") && REGISTERS.containsKey(n.text()) -> Size.of(REGISTERS.get(n.text()));
            // LR 6.4 p. 22: "If LP(32) is in effect, the LENGTH OF special register has the implicit definition: PICTURE 9(9) USAGE IS BINARY"; LP(32) is the default (PG p. 422).
            case Value.Expression e when e.operator().equals("LENGTH OF") -> Size.of(9);
            case Value.Expression e when e.operator().equals("ADDRESS OF") -> Size.refused("ADDRESS OF, a pointer (LR 6.4 p. 18), which no arithmetic takes (p. 243)");
            case Value.Expression e when Funcs.name(e) != null && Funcs.FLOATING.contains(Funcs.name(e)) -> Size.refused(e.operator() + ", a floating-point function: its whole expression is computed in floating point (PG 6.4 p. 800, `rt/Floats`)");
            case Value.Expression e when Funcs.name(e) != null && Funcs.FIXED.contains(Funcs.name(e)) -> Size.of(function(e, x));
            case Value.Expression e when e.operator().startsWith("FUNCTION ") -> Size.refused(e.operator() + ": not a numeric function lowered");
            default -> Size.refused(v + ": not a numeric operand");
        };
    }

    /**
     * An item's: its PICTURE's integer digits, those it stores (`Lower.stored`), numeric or numeric
     * edited; an index, which has no
     * PICTURE, the nine of the fullword `rt` holds it in (`Lower.index`, J-167); a pointer refused, as
     * IBM allows it "only" in ALLOCATE, FREE, SET, a relation condition and USING (LR 6.4 p. 243); a
     * floating-point item refused, `rt` holding none.
     */
    static Size item(Item i) {
        Attributes a = i.attributes();
        return switch (a.category()) {
            case INDEX -> Size.of(9);
            case POINTER -> Size.refused(i.name() + ", a pointer, which no arithmetic takes (LR 6.4 p. 243)");
            case NUMERIC -> a.usage() == Attributes.Usage.FLOAT_SHORT || a.usage() == Attributes.Usage.FLOAT_LONG
                    ? Size.refused(i.name() + ", a floating-point item, which rt does not hold")
                    : Size.of(Math.max(0, Lower.stored(a) - a.scale()));
            // A numeric-edited item has its PICTURE's digits too: a DIVIDE's quotient GIVING one sizes the REMAINDER's (NC203A, J-170).
            case NUMERIC_EDITED -> Size.of(Math.max(0, Lower.stored(a) - a.scale()));
            default -> Size.refused(i.name() + ": not a numeric item");
        };
    }

    /**
     * An integer or mixed function's digit precision (PG 6.4 pp. 798-799): DATE-OF-INTEGER 8, DAY-OF-INTEGER
     * 7, FACTORIAL 30 "in compatibility mode", INTEGER-OF-DATE and INTEGER-OF-DAY 7, LENGTH 9, ORD 3, ORD-MAX
     * and ORD-MIN 9, MOD "min(i1 i2)"; INTEGER "one more digit than in the argument", INTEGER-PART "same
     * number of digits"; MAX, MIN and REM their widest argument's, RANGE and SUM one more for each step, as
     * a subtraction or addition carries one. A term is never given fewer places than its own value has
     * (`Functions.term`).
     */
    private static int function(Value.Expression e, Exprs x) {
        List<Integer> args = new java.util.ArrayList<>();
        for (Value a : e.operands()) {
            Size s = of(a, x);
            args.add(s.refused() == null ? s.places() : 0);
        }
        int widest = args.stream().mapToInt(Integer::intValue).max().orElse(0);
        return switch (Funcs.name(e)) {
            case "DATE-OF-INTEGER" -> 8;
            case "DAY-OF-INTEGER", "INTEGER-OF-DATE", "INTEGER-OF-DAY" -> 7;
            case "FACTORIAL" -> 30;
            case "LENGTH", "ORD-MAX", "ORD-MIN" -> 9;
            case "ORD" -> 3;
            case "MOD" -> args.stream().mapToInt(Integer::intValue).min().orElse(0);
            case "INTEGER" -> widest + 1;
            case "RANGE" -> widest + 1;
            case "SUM" -> widest + Math.max(0, args.size() - 1);
            default -> widest;
        };
    }

    /**
     * LINAGE-COUNTER's (LR 6.4 p. 23): "If the LINAGE clause specifies a data-name, LINAGE-COUNTER has the
     * same PICTURE and USAGE as that data-name. If the LINAGE clause specifies an integer,
     * LINAGE-COUNTER is a binary item with the same number of digits as that integer."
     */
    private static int counter(Value v, Exprs x) {
        FileDescriptor f = v instanceof Value.Expression e && e.operands().get(0) instanceof Value.File file
                ? x.p.files().stream().filter(d -> d.name().equals(file.name())).findFirst().orElseThrow()
                : x.p.files().stream().filter(d -> d.fd() != null && d.fd().linage() != null).findFirst().orElseThrow();
        Value body = f.fd().linage().body();
        return body instanceof Value.Literal l ? l.payload().length() : item(x.item(((Value.Field) body).item())).places();
    }
}
