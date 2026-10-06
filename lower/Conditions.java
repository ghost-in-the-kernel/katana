package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Item;
import katana.sir.types.Value;

/** Conditions as Java booleans: relations (numeric by value, else alphanumeric in the collating sequence), class, sign, condition names, AND OR NOT. */
final class Conditions {
    private final Exprs x;
    private final Arith arith;

    Conditions(Exprs x, Arith arith) {
        this.x = x;
        this.arith = arith;
    }

    // Decision: R-019
    String of(Value v) {
        return switch (v) {
            case Value.Expression e when e.operator().equals("AND") || e.operator().equals("OR") -> join(e, e.operator().equals("AND") ? " && " : " || ");
            case Value.Expression e when e.operator().equals("NOT") -> "!(" + of(e.operands().get(0)) + ")";
            case Value.Expression e when relation(e.operator()) != null -> compare(e.operands().get(0), e.operands().get(1)) + " " + relation(e.operator()) + " 0";
            case Value.Expression e when List.of("POSITIVE", "NEGATIVE", "ZERO").contains(e.operator()) -> sign(e);
            case Value.Expression e when e.operator().startsWith("NUMERIC") -> "Classes.numeric(" + x.sending(e.operands().get(0)) + ")";
            case Value.Expression e when e.operator().startsWith("ALPHABETIC") -> alphabetic(e);
            case Value.ConditionName c -> conditionName(c);
            default -> x.unsupported("condition " + v);
        };
    }

    private String join(Value.Expression e, String op) {
        List<String> parts = new ArrayList<>();
        for (Value o : e.operands()) parts.add(of(o));
        return "(" + String.join(op, parts) + ")";
    }

    private static String relation(String operator) {
        return switch (operator) {
            case "=" -> "==";
            case "<", ">", "<=", ">=" -> operator;
            case "<>", "NOT =" -> "!=";
            default -> null;
        };
    }

    /** -1, 0 or 1: numeric operands by value, any other pair as alphanumeric (LR pp. 274-279). */
    String compare(Value a, Value b) {
        if (x.numeric(a) && x.numeric(b)) {
            int dmax = Math.max(arith.decimals(a), arith.decimals(b));
            return "Compare.numbers(" + arith.result(a, dmax, false) + ".value(), " + arith.result(b, dmax, false) + ".value())";
        }
        if (x.pattern(b) != null) return "Compare.figurative(" + x.sending(a) + ", " + x.pattern(b) + ", co)";
        if (x.pattern(a) != null) return "-Compare.figurative(" + x.sending(b) + ", " + x.pattern(a) + ", co)";
        return "Compare.alphanumeric(" + x.sending(a) + ", " + x.sending(b) + ", co)";
    }

    private String sign(Value.Expression e) {
        String op = switch (e.operator()) {
            case "POSITIVE" -> "> 0";
            case "NEGATIVE" -> "< 0";
            default -> "== 0";
        };
        return "Compare.numbers(" + arith.result(e.operands().get(0), 0, false) + ".value(), new Decimal(java.math.BigInteger.ZERO, 0)) " + op;
    }

    private String alphabetic(Value.Expression e) {
        boolean upper = !e.operator().endsWith("LOWER"), lower = !e.operator().endsWith("UPPER");
        return "Classes.alphabetic(" + x.sending(e.operands().get(0)) + ", " + upper + ", " + lower + ")";
    }

    /** A condition name: its item equal to one of its values, or within one of its THRU ranges. */
    private String conditionName(Value.ConditionName c) {
        Item i = x.item(c.item());
        Value.Field f = new Value.Field(c.item(), c.subscripts(), null, null);
        List<String> alternatives = new ArrayList<>();
        for (Item.Condition k : i.conditions()) {
            if (!k.name().equals(c.name())) continue;
            for (Value v : k.values()) {
                if (v instanceof Value.Expression e && e.operator().equals("THRU")) alternatives.add("(" + compare(f, e.operands().get(0)) + " >= 0 && " + compare(f, e.operands().get(1)) + " <= 0)");
                else alternatives.add(compare(f, v) + " == 0");
            }
        }
        if (alternatives.isEmpty()) return x.unsupported("condition name " + c.name());
        return "(" + String.join(" || ", alternatives) + ")";
    }
}
