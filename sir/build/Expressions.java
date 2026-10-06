package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import katana.front.grammar.CobolParser;
import katana.sir.types.Value;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Arithmetic and conditions as binary trees over a fixed vocabulary (tithe pass 2, A1), not the
 * grammar's rule shapes. Arithmetic: `+ - * / **` left to right within a level, `neg` and `pos` for a
 * unary minus and plus, which bind before `**`. Conditions: `= <> < <= > >=` (NOT GREATER is `<=`), `NOT`, `AND` over
 * `OR`; `POSITIVE NEGATIVE ZERO` of an expression; `NUMERIC ALPHABETIC ALPHABETIC-LOWER ALPHABETIC-UPPER
 * DBCS KANJI` or `CLASS name` of a field. An abbreviated relation (`A > 1 AND < 5`) is written out
 * whole, its subject and operator the last relation's; a combined one (`A = (1 OR 2)`) likewise; and
 * so is an object that is a data item standing alone (`A = B OR C`, J-124), which the grammar reads as
 * a condition-name. The last relation is the same condition's, never an earlier statement's.
 */
final class Expressions {
    private final Values values;
    private Value subject;
    private String operator = "=";

    Expressions(Values values) {
        this.values = values;
    }

    Value arithmetic(CobolParser.ArithmeticExpressionContext a) {
        Value v = multDivs(a.multDivs());
        for (var pm : a.plusMinus()) v = binary(pm.PLUSCHAR() != null ? "+" : "-", v, multDivs(pm.multDivs()));
        return v;
    }

    private Value multDivs(CobolParser.MultDivsContext m) {
        Value v = powers(m.powers());
        for (var md : m.multDiv()) v = binary(md.ASTERISKCHAR() != null ? "*" : "/", v, powers(md.powers()));
        return v;
    }

    private Value powers(CobolParser.PowersContext p) {
        Value v = basis(p.basis());
        if (p.MINUSCHAR() != null) v = new Value.Expression("neg", List.of(v));
        if (p.PLUSCHAR() != null) v = new Value.Expression("pos", List.of(v));
        for (var pw : p.power()) v = binary("**", v, basis(pw.basis()));
        return v;
    }

    private Value basis(CobolParser.BasisContext b) {
        if (b.arithmeticExpression() != null) return arithmetic(b.arithmeticExpression());
        return values.of(b.identifier() != null ? b.identifier() : b.literal());
    }

    /** A statement's condition, with no relation before it to abbreviate. */
    Value top(CobolParser.ConditionContext c) {
        subject = null;
        operator = "=";
        return condition(c);
    }

    /** A condition: its terms joined by AND before OR, as written left to right. */
    Value condition(CobolParser.ConditionContext c) {
        List<Value> terms = new ArrayList<>();
        List<String> joins = new ArrayList<>();
        terms.add(combinable(c.combinableCondition()));
        for (var ao : c.andOrCondition()) {
            String join = ao.AND() != null ? "AND" : "OR";
            if (ao.combinableCondition() != null) {
                joins.add(join);
                terms.add(combinable(ao.combinableCondition()));
            } else {
                for (var ab : ao.abbreviation()) {
                    joins.add(join);
                    terms.add(abbreviation(ab));
                }
            }
        }
        List<Value> ors = new ArrayList<>();
        Value and = terms.get(0);
        for (int i = 0; i < joins.size(); i++) {
            if (joins.get(i).equals("AND")) {
                and = binary("AND", and, terms.get(i + 1));
            } else {
                ors.add(and);
                and = terms.get(i + 1);
            }
        }
        ors.add(and);
        Value v = ors.get(0);
        for (int i = 1; i < ors.size(); i++) v = binary("OR", v, ors.get(i));
        return v;
    }

    private Value combinable(CobolParser.CombinableConditionContext c) {
        Value v = simple(c.simpleCondition());
        return c.NOT() != null ? new Value.Expression("NOT", List.of(v)) : v;
    }

    private Value simple(CobolParser.SimpleConditionContext s) {
        if (s.condition() != null) return condition(s.condition());
        if (s.classCondition() != null) return classCondition(s.classCondition());
        if (s.conditionNameReference() != null) {
            Value v = values.of(s.conditionNameReference());
            return v instanceof Value.Field && subject != null ? binary(operator, subject, v) : v;
        }
        return relation(s.relationCondition());
    }

    private Value relation(CobolParser.RelationConditionContext r) {
        if (r.relationSignCondition() != null) {
            var s = r.relationSignCondition();
            String sign = s.POSITIVE() != null ? "POSITIVE" : s.NEGATIVE() != null ? "NEGATIVE" : "ZERO";
            Value v = new Value.Expression(sign, List.of(arithmetic(s.arithmeticExpression())));
            return s.NOT() != null ? new Value.Expression("NOT", List.of(v)) : v;
        }
        if (r.relationArithmeticComparison() != null) {
            var a = r.relationArithmeticComparison();
            subject = arithmetic(a.arithmeticExpression(0));
            operator = operator(a.relationalOperator());
            return binary(operator, subject, arithmetic(a.arithmeticExpression(1)));
        }
        var c = r.relationCombinedComparison();
        subject = arithmetic(c.arithmeticExpression());
        operator = operator(c.relationalOperator());
        var combined = c.relationCombinedCondition();
        Value v = binary(operator, subject, arithmetic(combined.arithmeticExpression(0)));
        for (int i = 1; i < combined.arithmeticExpression().size(); i++) {
            String join = combined.getChild(2 * i - 1).getText().equalsIgnoreCase("AND") ? "AND" : "OR";
            v = binary(join, v, binary(operator, subject, arithmetic(combined.arithmeticExpression(i))));
        }
        return v;
    }

    /** `AND < 5` after `A > 1`: the last subject, this operator or the last, this object. */
    private Value abbreviation(CobolParser.AbbreviationContext a) {
        if (a.relationalOperator() != null) operator = operator(a.relationalOperator());
        Value object = arithmetic(a.arithmeticExpression());
        Value v = binary(operator, subject, object);
        if (a.abbreviation() != null) v = binary("AND", v, abbreviation(a.abbreviation()));
        return a.NOT() != null ? new Value.Expression("NOT", List.of(v)) : v;
    }

    private Value classCondition(CobolParser.ClassConditionContext c) {
        String test = c.className() != null ? "CLASS " + Values.upper(c.className().getText()) : Values.upper(c.getChild(c.getChildCount() - 1).getText());
        Value v = new Value.Expression(test, List.of(values.of(c.identifier())));
        return c.NOT() != null ? new Value.Expression("NOT", List.of(v)) : v;
    }

    /** A relational operator's words as its symbol. */
    static String operator(CobolParser.RelationalOperatorContext r) {
        String t = Values.upper(DataMap.text(r)).replaceAll("\\b(IS|ARE|THAN|TO)\\b", "").replaceFirst("^\\s*NOT(?=[=<>])", "NOT ").replaceAll("\\s+", " ").trim();
        boolean not = t.startsWith("NOT ");
        String rest = not ? t.substring(4) : t;
        String op = switch (rest) {
            case "GREATER OR EQUAL", ">=" -> ">=";
            case "LESS OR EQUAL", "<=" -> "<=";
            case "GREATER", ">" -> ">";
            case "LESS", "<" -> "<";
            case "EQUAL", "=" -> "=";
            case "<>" -> "<>";
            default -> throw new IllegalArgumentException("relational operator " + t);
        };
        if (!not) return op;
        return switch (op) {
            case ">" -> "<=";
            case "<" -> ">=";
            case "=" -> "<>";
            case ">=" -> "<";
            case "<=" -> ">";
            default -> "=";
        };
    }

    private static Value binary(String operator, Value a, Value b) {
        return new Value.Expression(operator, List.of(a, b));
    }
}
