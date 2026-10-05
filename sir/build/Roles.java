package katana.sir.build;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import katana.front.grammar.CobolParser;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.RuleStopState;
import org.antlr.v4.runtime.atn.RuleTransition;
import org.antlr.v4.runtime.atn.Transition;

/**
 * The statement vocabulary the walker can produce (tithe pass 2, A6), read from the grammar rather than
 * from any program: every verb, the roles its operands can carry, the labels its branches can have. The
 * walk's own rules decide it (`Paragraphs.walk`): from the verb's rule, a rule that is a value or a
 * procedure name is an operand of the rule calling it, a rule holding statements is a branch, any other
 * rule is walked into. Phase 3's lowering switches on it, so its table must cover this.
 */
public final class Roles {
    /** A verb's vocabulary: its operand roles and its branch labels, sorted. */
    public record Verb(Set<String> roles, Set<String> branches) {}

    private Roles() {}

    /** Every verb of the `statement` rule, by name, sorted. */
    public static Map<String, Verb> vocabulary() {
        ATN atn = CobolParser._ATN;
        Map<String, Verb> out = new TreeMap<>();
        for (int verb : called(atn, CobolParser.RULE_statement).calls()) {
            Verb v = new Verb(new TreeSet<>(), new TreeSet<>());
            walk(atn, verb, v, new BitSet());
            out.put(Paragraphs.verbName(context(verb)), v);
        }
        return out;
    }

    /** One rule the walk enters: a role if it can hold a word or a value directly; its callees sorted into operands, branches and rules walked into. */
    private static void walk(ATN atn, int rule, Verb v, BitSet seen) {
        if (seen.get(rule)) return;
        seen.set(rule);
        Rule r = called(atn, rule);
        String role = Paragraphs.role(context(rule));
        if (r.token()) v.roles().add(role);
        for (int callee : r.calls()) {
            Class<?> c = context(callee);
            if (c == CobolParser.ProcedureNameContext.class || Values.VALUES.contains(c)) v.roles().add(role);
            else if (Paragraphs.holdsStatements(c)) v.branches().add(Paragraphs.role(c));
            else walk(atn, callee, v, seen);
        }
    }

    private record Rule(Set<Integer> calls, boolean token) {}

    /** The rules a rule calls directly, and whether it matches a token itself, from its ATN. */
    private static Rule called(ATN atn, int rule) {
        Set<Integer> calls = new TreeSet<>();
        boolean token = false;
        Deque<ATNState> todo = new ArrayDeque<>();
        BitSet seen = new BitSet();
        todo.push(atn.ruleToStartState[rule]);
        while (!todo.isEmpty()) {
            ATNState s = todo.pop();
            if (seen.get(s.stateNumber) || s instanceof RuleStopState) continue;
            seen.set(s.stateNumber);
            for (Transition t : s.getTransitions()) {
                if (t instanceof RuleTransition rt) {
                    calls.add(rt.ruleIndex);
                    todo.push(rt.followState);
                } else {
                    token |= !t.isEpsilon();
                    todo.push(t.target);
                }
            }
        }
        return new Rule(calls, token);
    }

    /** A rule's generated context class. */
    private static Class<?> context(int rule) {
        String name = CobolParser.ruleNames[rule];
        try {
            return Class.forName(CobolParser.class.getName() + "$" + Character.toUpperCase(name.charAt(0)) + name.substring(1) + "Context");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("no context class for rule " + name, e);
        }
    }
}
