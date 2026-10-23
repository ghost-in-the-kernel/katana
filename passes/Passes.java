package katana.passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.lower.Plan;
import katana.sir.types.Program;

/**
 * The pass chain (docs/PASSES.md): each pass in the order it needs the one before, their answers one `Plan` lower/
 * takes. A pass decides; lower/ writes (MOD-04). The first, `Structuring`, is a pass like the others.
 */
public final class Passes {
    /**
     * The plan lower/ takes, the ranges made methods, each range or item not changed with why, the fact behind each
     * item 6.5 names or types, and `viewed`, the items 6.7's record classes name (research §46).
     */
    public record Result(Plan plan, List<Structuring.Applied> applied, Map<String, List<String>> refused, Map<Integer, Fact> typed, Set<Integer> viewed) {}

    private Passes() {}

    /** Every pass on one program and its intent. */
    // Decision: R-032
    public static Result plan(Program p, Intent intent) {
        List<Structuring.Applied> applied = new ArrayList<>();
        Map<String, List<String>> refused = new java.util.LinkedHashMap<>();
        List<String> program = Structuring.program(p);
        Map<Integer, Integer> methods = Structuring.methods(p, intent, program, applied, refused);
        Set<String> loops = program.isEmpty() ? Loops.loops(p, methods, refused) : Set.of();
        Map<Integer, Integer> cycles = new HashMap<>();
        Set<Integer> inline = program.isEmpty() ? Inline.inline(p, methods, loops, cycles, refused) : Set.of();
        Set<Integer> main = program.isEmpty() ? MainLine.main(p, methods, loops, cycles, refused) : Set.of();
        // The passes after structuring, in the order each needs the one before (docs/PASSES.md): 1. Scalars promotes
        // items; 2. Exact takes their statements; 3. Fits only Exact's; 4. Types names and types promoted items; 5. Records
        // views the records left in bytes; 6. Stateless reads the whole program.
        Set<Integer> scalars = Scalars.promote(p, refused);
        Set<String> exact = Exact.exact(p, scalars, refused);
        Set<String> fits = Fits.fits(p, scalars, exact, refused);
        Types.Result types = Types.types(p, intent, scalars, refused);
        Set<Integer> records = Records.records(p, !program.isEmpty(), refused);
        Set<Integer> viewed = new java.util.TreeSet<>();
        for (katana.sir.types.Item i : p.items()) if (records.contains(i.record()) && !scalars.contains(i.id())) viewed.add(i.id());
        List<String> stateful = Stateless.refusals(p);
        if (!stateful.isEmpty()) refused.put("program " + p.header().program() + " stateless", stateful);
        return new Result(new Plan(methods, program.isEmpty() ? Structuring.falls(p) : Set.of(), loops, inline, main, cycles, scalars, exact, types.enums(), types.money(), stateful.isEmpty(), fits, records),
                applied, refused, types.facts(), viewed);
    }
}
