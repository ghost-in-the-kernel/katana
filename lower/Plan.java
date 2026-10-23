package katana.lower;

import java.util.Map;
import java.util.Set;
import katana.sir.types.Origin;

/**
 * What a pass asks the lowering to write otherwise (MOD-04: passes/ writes Java only through this emitter):
 * each PERFORM range, by its first paragraph's index to its last's, called as a method `perform<first>`, and the
 * paragraphs whose last statement, a GO TO to the next, is left to fall, and the PERFORM VARYINGs written as
 * a `for` (research §36), and the methods, by their first paragraph, whose paragraphs are the method's own
 * code, out of the switch (6.2, research §37); the main line's paragraphs when they are `run`'s own code, and the
 * loops of either, each header paragraph to the last of its loop (research §38); the items promoted to Java
 * variables (6.3, research §39, R-068); the arithmetic statements written as exact arithmetic, by where each
 * starts (6.4, research §41); the items whose condition names an Enum or Flag fact names, and the promoted
 * items a Money fact makes `Money` (6.5, research §42, R-070); whether the program is stateless, lowered as
 * INITIAL (6.6, research §44); the exact statements whose value always fits their receiver, stored by plain
 * assignment (6.6b, research §45); the records written as classes over their bytes (6.7, research §46).
 * Empty, the literal lowering; it decides nothing (passes/Structuring and passes/Scalars discharge the obligations).
 * A new field is added here (component, copy, `LITERAL`) and set in `passes/Structuring.plan`, its only other
 * construction (docs/PASSES.md, "Adding a pass").
 */
public record Plan(Map<Integer, Integer> methods, Set<Integer> falls, Set<String> loops, Set<Integer> inline,
                   Set<Integer> main, Map<Integer, Integer> cycles, Set<Integer> scalars, Set<String> exact,
                   Set<Integer> enums, Set<Integer> money, boolean stateless, Set<String> fits, Set<Integer> records) {
    public static final Plan LITERAL = new Plan(Map.of(), Set.of(), Set.of(), Set.of(), Set.of(), Map.of(), Set.of(),
            Set.of(), Set.of(), Set.of(), false, Set.of(), Set.of());

    /**
     * The verbs that write a promoted item's bytes through a field of the item's own (research §40): `Promoted.intercept`
     * runs them through `Promoted.through`, and passes/Scalars keeps out an item one of them writes with statements of
     * its own, since `through` holds no statement inside another. One set, read by both.
     */
    public static final Set<String> THROUGH = Set.of("INITIALIZE", "SET", "STRING", "UNSTRING", "ACCEPT");

    public Plan {
        methods = Map.copyOf(methods);
        falls = Set.copyOf(falls);
        loops = Set.copyOf(loops);
        inline = Set.copyOf(inline);
        main = Set.copyOf(main);
        cycles = Map.copyOf(cycles);
        scalars = Set.copyOf(scalars);
        exact = Set.copyOf(exact);
        enums = Set.copyOf(enums);
        money = Set.copyOf(money);
        fits = Set.copyOf(fits);
        records = Set.copyOf(records);
    }

    /** Whether the plan changes the procedure's flow (6.1, 6.2), apart from the items it promotes: a promotion alone leaves the dispatcher as it is. */
    public boolean structured() {
        return !methods.isEmpty() || !falls.isEmpty() || !loops.isEmpty() || !inline.isEmpty() || !main.isEmpty() || !cycles.isEmpty();
    }

    /** A PERFORM VARYING the plan writes as a `for`, by where its statement starts: `file:line:column`. */
    public static String loop(Origin o) {
        return o.file() + ":" + o.line() + ":" + o.column();
    }
}
