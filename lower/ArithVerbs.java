package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Item;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * ADD, SUBTRACT, MULTIPLY, DIVIDE and COMPUTE: the operands as Appendix A terms, each receiver stored by
 * `rt/Arithmetic` (ROUNDED, the size error condition), then the SIZE ERROR phrases on `sz`.
 */
final class ArithVerbs {
    private final Flow f;

    ArithVerbs(Flow f) {
        this.f = f;
    }

    /** A receiver and whether it is ROUNDED. */
    private record Receiver(Value.Field field, boolean rounded) {}

    /** One arithmetic statement: `sz` cleared, each receiver stored, then its SIZE ERROR phrases. */
    // Decision: R-020
    void statement(Statement s) {
        boolean phrase = !Verbs.branch(s, "on-size-error-phrase").isEmpty() || !Verbs.branch(s, "not-on-size-error-phrase").isEmpty();
        f.e.line("sz = false;");
        f.e.open("");
        switch (s.verb()) {
            case "ADD" -> add(s, phrase);
            case "SUBTRACT" -> subtract(s, phrase);
            case "MULTIPLY" -> multiply(s, phrase);
            case "DIVIDE" -> divide(s, phrase);
            default -> compute(s, phrase);
        }
        f.e.close();
        if (phrase) f.branch("sz", Verbs.branch(s, "on-size-error-phrase"), Verbs.branch(s, "not-on-size-error-phrase"));
    }

    /** The receivers of the given roles in order, each ROUNDED when the keyword follows it. */
    private static List<Receiver> receivers(Statement s, String... roles) {
        List<Receiver> out = new ArrayList<>();
        for (Statement.Operand o : s.operands()) {
            if (!List.of(roles).contains(o.role())) continue;
            if (o.value() instanceof Value.Field fld) out.add(new Receiver(fld, false));
            else if (o.value() instanceof Value.Name n && n.text().equals("ROUNDED") && !out.isEmpty()) out.set(out.size() - 1, new Receiver(out.get(out.size() - 1).field(), true));
        }
        return out;
    }

    private List<Item> items(List<Receiver> rs) {
        return rs.stream().map(r -> f.x.item(r.field().item())).toList();
    }

    /** Each receiver given `value(receiver)`, the expression built as last for its ROUNDED. */
    private void store(List<Receiver> rs, java.util.function.BiFunction<Receiver, Boolean, String> value, boolean phrase) {
        for (Receiver r : rs) f.e.line("sz |= Arithmetic.store(" + f.x.field(r.field()) + ", " + value.apply(r, r.rounded()) + ", " + r.rounded() + ", " + phrase + ");");
    }

    /**
     * A result that does not depend on its receiver (GIVING, COMPUTE): computed once, with ROUNDED's place
     * when any receiver is ROUNDED (truncating it again gives the others the same digits), and stored into
     * each receiver in turn, so a receiver that is also an operand does not change what the next receives
     * (NC172A; IBM's multiple-results rule, LR 6.4 pp. 297-298).
     */
    // Decision: R-020
    private void storeOnce(List<Receiver> rs, java.util.function.BiFunction<Receiver, Boolean, String> value, boolean phrase) {
        if (rs.size() == 1) {
            store(rs, value, phrase);
            return;
        }
        boolean any = rs.stream().anyMatch(Receiver::rounded);
        f.e.line("Decimal.Term w = " + value.apply(rs.get(0), any) + ";");
        store(rs, (r, rd) -> "w", phrase);
    }

    /**
     * The sending operands of a statement with several receivers, evaluated once into a temporary before
     * any receiver changes (LR 6.4 pp. 297-298: the statement "stores that result in a temporary
     * location", then combines it "with each single receiving field" in the order written), so `ADD A TO
     * A B` adds A's first value to B. A receiver's subscript is evaluated when it is stored, after the
     * receivers before it (IBM's `D(C)`).
     */
    // Decision: R-020
    // Unsure: IBM's example is an ADD; for MULTIPLY and DIVIDE without GIVING the temporary is taken to be the multiplier or divisor (J-141).
    private String shared(List<Receiver> rs, String term) {
        if (rs.size() == 1) return term;
        f.e.line("Decimal.Term v = " + term + ";");
        return "v";
    }

    /** The values added together, as one term that is not the last operation: `a + b + c`. */
    private String sum(List<Value> values, int dmax) {
        return f.arith.term(sumExpression(values), dmax);
    }

    private String op(String name, String a, String b, int dmax, boolean rounded) {
        return "Decimal." + name + "(P, " + a + ", " + b + ", " + dmax + ", true, " + rounded + ")";
    }

    /** ADD: CORRESPONDING by its pairs; TO with the sending sum taken once before any receiver (R-059); GIVING computed once into every receiver. */
    private void add(Statement s, boolean phrase) {
        List<Value> from = Verbs.operands(s, "add-from");
        List<Receiver> to = receivers(s, "add-to"), giving = receivers(s, "add-giving");
        if (from.isEmpty()) {
            corresponding(s, "add", "add-corresponding-statement", to, phrase);
            return;
        }
        if (giving.isEmpty()) {
            int dmax = f.arith.dmax(items(to), from);
            String total = shared(to, sum(from, dmax));
            store(to, (r, rd) -> op("add", f.arith.operand(r.field()), total, dmax, rd), phrase);
            return;
        }
        List<Value> all = new ArrayList<>(from);
        all.addAll(Verbs.operands(s, "add-to-giving"));
        int dmax = f.arith.dmax(items(giving), all);
        storeOnce(giving, (r, rd) -> all.size() == 1 ? f.arith.operand(all.get(0)) : f.arith.result(sumExpression(all), dmax, rd), phrase);
    }

    private static Value sumExpression(List<Value> values) {
        Value total = values.get(0);
        for (int k = 1; k < values.size(); k++) total = new Value.Expression("+", List.of(total, values.get(k)));
        return total;
    }

    private void subtract(Statement s, boolean phrase) {
        List<Value> subtrahends = Verbs.operands(s, "subtract-subtrahend");
        List<Receiver> minuends = receivers(s, "subtract-minuend"), giving = receivers(s, "subtract-giving");
        if (subtrahends.isEmpty()) {
            corresponding(s, "subtract", "subtract-corresponding-statement", receivers(s, "subtract-minuend-corresponding"), phrase);
            return;
        }
        List<Value> all = new ArrayList<>(subtrahends);
        Value minuend = Verbs.operand(s, "subtract-minuend-giving");
        if (minuend != null) all.add(minuend);
        int dmax = f.arith.dmax(items(giving.isEmpty() ? minuends : giving), all);
        String total = shared(giving.isEmpty() ? minuends : List.of(), sum(subtrahends, dmax));
        if (giving.isEmpty()) store(minuends, (r, rd) -> op("subtract", f.arith.operand(r.field()), total, dmax, rd), phrase);
        else storeOnce(giving, (r, rd) -> op("subtract", f.arith.operand(minuend), total, dmax, rd), phrase);
    }

    /**
     * ADD and SUBTRACT CORRESPONDING (LR 6.4 p. 295): each pair of elementary numeric items the two groups
     * share, the receiver's own ROUNDED on each; SIZE ERROR's statement after all of them (`sz` gathers).
     */
    // Decision: R-020
    private void corresponding(Statement s, String op, String senderRole, List<Receiver> receivers, boolean phrase) {
        Value.Field group = (Value.Field) Verbs.operand(s, senderRole);
        for (Receiver r : receivers) {
            for (Value.Field[] pair : Corresponding.pairs(f.x, group, r.field(), true)) {
                Receiver into = new Receiver(pair[1], r.rounded());
                int dmax = f.arith.dmax(items(List.of(into)), List.of(pair[0]));
                store(List.of(into), (t, rd) -> op(op, f.arith.operand(t.field()), f.arith.operand(pair[0]), dmax, rd), phrase);
            }
        }
    }

    private void multiply(Statement s, boolean phrase) {
        Value a = Verbs.operand(s, "multiply-statement");
        List<Receiver> regular = receivers(s, "multiply-regular-operand"), giving = receivers(s, "multiply-giving-result");
        Value b = Verbs.operand(s, "multiply-giving-operand");
        int dmax = f.arith.dmax(items(giving.isEmpty() ? regular : giving), b == null ? List.of(a) : List.of(a, b));
        if (giving.isEmpty()) {
            String multiplier = shared(regular, f.arith.operand(a));
            store(regular, (r, rd) -> op("multiply", multiplier, f.arith.operand(r.field()), dmax, rd), phrase);
        } else storeOnce(giving, (r, rd) -> op("multiply", f.arith.operand(a), f.arith.operand(b), dmax, rd), phrase);
    }

    /** DIVIDE INTO (each receiver the dividend), INTO GIVING and BY GIVING; REMAINDER is not lowered yet. */
    private void divide(Statement s, boolean phrase) {
        Value a = Verbs.operand(s, "divide-statement");
        List<Receiver> into = receivers(s, "divide-into"), giving = receivers(s, "divide-giving");
        Value byValue = Verbs.operand(s, "divide-by-giving-statement"), intoValue = Verbs.operand(s, "divide-into-giving-statement");
        List<Receiver> remainder = receivers(s, "divide-remainder");
        if (!remainder.isEmpty() && !giving.isEmpty()) {
            remainder(s, a, byValue, intoValue, giving.get(0), remainder.get(0), phrase);
            return;
        }
        if (giving.isEmpty()) {
            int dmax = f.arith.dmax(items(into), List.of());
            String divisor = shared(into, f.arith.operand(a));
            store(into, (r, rd) -> op("divide", f.arith.operand(r.field()), divisor, dmax, rd), phrase);
            return;
        }
        Value dividend = byValue != null ? a : intoValue, divisor = byValue != null ? byValue : a;
        int dmax = f.arith.dmax(items(giving), List.of(dividend));
        storeOnce(giving, (r, rd) -> op("divide", f.arith.operand(dividend), f.arith.operand(divisor), dmax, rd), phrase);
    }

    /**
     * DIVIDE ... GIVING q REMAINDER r (LR p. 336): the quotient stored as usual; the remainder the dividend
     * less the product of the divisor and the quotient as stored without rounding; after a division by
     * zero, or a size error in the quotient under the SIZE ERROR phrase, the remainder is unchanged (p. 336).
     */
    // Decision: R-020
    private void remainder(Statement s, Value a, Value byValue, Value intoValue, Receiver q, Receiver r, boolean phrase) {
        Value dividend = byValue != null ? a : intoValue, divisor = byValue != null ? byValue : a;
        Item qi = f.x.item(q.field().item());
        int dmax = f.arith.dmax(List.of(qi, f.x.item(r.field().item())), List.of(dividend));
        String quotient = op("divide", f.arith.operand(dividend), f.arith.operand(divisor), dmax, q.rounded());
        f.e.open("");
        f.e.line("Decimal.Term qt = " + quotient + ";");
        f.e.line("boolean qs = Arithmetic.store(" + f.x.field(q.field()) + ", qt, " + q.rounded() + ", " + phrase + ");");
        f.e.line("sz |= qs;");
        f.e.open("if (!qt.divisionByZero()" + (phrase ? " && !qs" : "") + ")");
        f.e.line("Decimal.Term tq = Decimal.operand(Decimal.store(qt, " + Lower.stored(qi.attributes()) + ", " + qi.attributes().scale() + ", true, false).value(), " + Arith.integerPlaces(qi) + ");");
        f.e.line("Decimal.Term rt = Decimal.subtract(P, " + f.arith.operand(dividend) + ", Decimal.multiply(P, tq, " + f.arith.operand(divisor) + ", " + dmax + ", false, false), " + dmax + ", true, false);");
        f.e.line("sz |= Arithmetic.store(" + f.x.field(r.field()) + ", rt, false, " + phrase + ");");
        f.e.close();
        f.e.close();
    }

    private void compute(Statement s, boolean phrase) {
        List<Receiver> to = receivers(s, "compute-store");
        Value expression = Verbs.operand(s, "compute-statement");
        int dmax = f.arith.dmax(items(to), List.of(expression));
        storeOnce(to, (r, rd) -> f.arith.result(expression, dmax, rd), phrase);
    }
}
