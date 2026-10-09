package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Header;
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

    /** A condition as a Java boolean: AND, OR, NOT, relations (alphanumeric ones in the collating sequence), sign, class, switch and condition-name tests. */
    // Decision: R-019
    String of(Value v) {
        return switch (v) {
            case Value.Expression e when e.operator().equals("AND") || e.operator().equals("OR") -> join(e, e.operator().equals("AND") ? " && " : " || ");
            case Value.Expression e when e.operator().equals("NOT") -> "!(" + of(e.operands().get(0)) + ")";
            case Value.Expression e when relation(e.operator()) != null -> compare(e.operands().get(0), e.operands().get(1)) + " " + relation(e.operator()) + " 0";
            case Value.Expression e when List.of("POSITIVE", "NEGATIVE", "ZERO").contains(e.operator()) -> sign(e);
            case Value.Expression e when e.operator().startsWith("NUMERIC") -> "Classes.numeric(" + x.sending(e.operands().get(0)) + ")";
            case Value.Expression e when e.operator().startsWith("ALPHABETIC") -> alphabetic(e);
            case Value.Expression e when e.operator().startsWith("CLASS ") -> charClass(e.operator().substring(6), e.operands().get(0));
            case Value.ConditionName c -> conditionName(c);
            case Value.Name n when n.kind().equals("switch") -> switchStatus(n.text());
            default -> "(boolean) " + x.unsupported("condition " + v);
        };
    }

    /**
     * A class condition on a CLASS name: its members as byte ranges in the program's codepage, an
     * alphanumeric literal each of its characters, a numeric literal the character at that ordinal
     * position (from 1) in the native collating sequence, which is the byte one less (LR 6.4 p. 129).
     */
    // Decision: R-019
    private String charClass(String name, Value item) {
        for (Header.CharClass k : x.p.header().classes()) {
            if (!k.name().equals(name)) continue;
            List<String> ranges = new ArrayList<>();
            for (Value m : k.members()) {
                boolean thru = m instanceof Value.Expression e && e.operator().equals("THRU");
                List<Value> bounds = thru ? ((Value.Expression) m).operands() : List.of(m);
                List<String> codes = new ArrayList<>();
                for (Value b : bounds) codes.addAll(codes(b));
                if (thru && codes.size() == 2) ranges.add(codes.get(0) + ", " + codes.get(1));
                else for (String c : codes) ranges.add(c + ", " + c);
            }
            if (ranges.isEmpty() || ranges.stream().anyMatch(r -> r.contains("null"))) return "(boolean) " + x.unsupported("CLASS " + name + " " + k.members());
            return "Classes.member(" + x.sending(item) + ", " + String.join(", ", ranges) + ")";
        }
        return "(boolean) " + x.unsupported("CLASS " + name);
    }

    /** A CLASS literal's bytes: each character's, or for a numeric literal the ordinal's. */
    private List<String> codes(Value v) {
        List<String> out = new ArrayList<>();
        if (v instanceof Value.Literal l && l.numeric()) out.add(String.valueOf(Integer.parseInt(l.payload()) - 1));
        else if (v instanceof Value.Literal l && l.kind() == Value.Literal.Kind.ALPHANUMERIC) for (char c : l.payload().toCharArray()) out.add("Classes.code(Literal.text(" + Emitter.quote(String.valueOf(c)) + ", cp))");
        else out.add("null");
        return out;
    }

    /** A switch-status condition (LR 6.4 p. 283): its UPSI switch has the status the condition-name stands for. */
    private String switchStatus(String name) {
        for (Header.Switch s : x.p.header().switches()) {
            if (!s.condition().equals(name)) continue;
            if (!s.environment().matches("UPSI-[0-7]")) return "(boolean) " + x.unsupported("switch " + s.environment() + ", not UPSI-0 to UPSI-7");
            return (s.on() ? "" : "!") + "upsi.on(" + s.environment().charAt(5) + ")";
        }
        return "(boolean) " + x.unsupported("switch condition " + name);
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
        return "Compare.alphanumeric(" + text(a) + ", " + text(b) + ", co)";
    }

    /** An operand of an alphanumeric comparison: a numeric literal as its characters as written (`012345678912`, J-130), any other as it sends. */
    private String text(Value v) {
        return v instanceof Value.Literal l && l.numeric() ? "Literal.text(" + Emitter.quote(Exprs.written(l)) + ", cp)" : x.sending(v);
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
        if (alternatives.isEmpty()) return "(boolean) " + x.unsupported("condition name " + c.name());
        return "(" + String.join(" || ", alternatives) + ")";
    }
}
