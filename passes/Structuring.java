package katana.passes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.Kind;
import katana.intent.types.Subject;
import katana.lower.Lower;
import katana.sir.contract.Contract;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.1, pattern structuring (research §35): a PERFORM range a `Function` fact names becomes a method its
 * callers call, and a GO TO to the next paragraph is left to fall, each only where its obligations hold on
 * sir/'s graph and contract (R-032); a fact no person confirmed licenses it, and its ledger line says so
 * (R-029). It decides; lower/ writes (MOD-04).
 */
public final class Structuring {
    /** The pass as the ledger names it. */
    public static final String PASS = "structuring-v1";

    /** A range made a method, and the fact that licenses it. */
    public record Applied(int first, int last, Fact fact) {}

    /** The plan lower/ takes, what was applied, each range or item not changed with why, and the fact behind each item 6.5 names or types. */
    /** `viewed`: the items 6.7's record classes name, each a member of a record written as a class (research §46). */
    public record Result(Lower.Plan plan, List<Applied> applied, Map<String, List<String>> refused, Map<Integer, Fact> typed, Set<Integer> viewed) {}

    private Structuring() {}

    /** The pass on one program and its intent: the ranges that discharge every obligation, and the GO TOs that fall. */
    // Decision: R-032
    public static Result plan(Program p, Intent intent) {
        List<Subject.Range> ranges = new ArrayList<>();
        Map<Subject.Range, Fact> facts = new HashMap<>();
        for (Fact f : intent.actionable()) {
            if (f.kind() == Kind.FUNCTION && f.subject() instanceof Subject.Range r) {
                ranges.add(r);
                facts.put(r, f);
            }
        }
        Map<Integer, Integer> methods = new HashMap<>();
        List<Applied> applied = new ArrayList<>();
        Map<String, List<String>> refused = new java.util.LinkedHashMap<>();
        List<String> program = program(p);
        for (Subject.Range r : ranges) {
            List<String> why = new ArrayList<>(program);
            if (why.isEmpty()) why.addAll(Obligations.refusals(p, r, ranges));
            if (why.isEmpty()) {
                methods.put(r.first(), r.last());
                applied.add(new Applied(r.first(), r.last(), facts.get(r)));
            } else refused.put(name(p, r), why);
        }
        Set<String> loops = program.isEmpty() ? Loops.loops(p, methods, refused) : Set.of();
        Map<Integer, Integer> cycles = new HashMap<>();
        Set<Integer> inline = program.isEmpty() ? Inline.inline(p, methods, loops, cycles, refused) : Set.of();
        Set<Integer> main = program.isEmpty() ? MainLine.main(p, methods, loops, cycles, refused) : Set.of();
        Set<Integer> scalars = Scalars.promote(p, refused);
        Set<String> exact = Exact.exact(p, scalars, refused);
        Set<String> fits = Fits.fits(p, scalars, exact, refused);
        Types.Result types = Types.types(p, intent, scalars, refused);
        Set<Integer> records = Records.records(p, !program.isEmpty(), refused);
        Set<Integer> viewed = new java.util.TreeSet<>();
        for (katana.sir.types.Item i : p.items()) if (records.contains(i.record()) && !scalars.contains(i.id())) viewed.add(i.id());
        List<String> stateful = Stateless.refusals(p);
        if (!stateful.isEmpty()) refused.put("program " + p.header().program() + " stateless", stateful);
        return new Result(new Lower.Plan(methods, program.isEmpty() ? falls(p) : Set.of(), loops, inline, main, cycles, scalars, exact, types.enums(), types.money(), stateful.isEmpty(), fits, records),
                applied, refused, types.facts(), viewed);
    }

    /** What keeps a whole program literal: an independent segment with an ALTER, whose state the dispatcher keeps (research §16); declaratives, which run as PERFORMs of their own. */
    private static List<String> program(Program p) {
        List<String> why = new ArrayList<>();
        boolean independent = p.procedure().sections().stream().anyMatch(s -> s.priority() >= 50);
        if (independent && p.procedure().edges().stream().anyMatch(e -> e.kind() == Procedure.Kind.ALTER)) why.add("the program is segmented and ALTERs");
        if (p.procedure().sections().stream().anyMatch(Procedure.Section::declarative)) why.add("the program has declaratives");
        return why;
    }

    /**
     * The paragraphs whose last top-level statement is a GO TO to the next, not alterable, where no PERFORM
     * range ends: a fall reaches the same paragraph by the same `Dispatcher.end`.
     */
    // Decision: R-032
    static Set<Integer> falls(Program p) {
        Set<Integer> out = new HashSet<>(), ends = new HashSet<>(), altered = new HashSet<>();
        for (Procedure.Edge e : p.procedure().edges()) {
            if (e.kind() == Procedure.Kind.PERFORM) ends.add(e.thru());
            if (e.kind() == Procedure.Kind.ALTER) altered.add(e.from());
        }
        List<Procedure.Paragraph> all = p.procedure().paragraphs();
        for (int i = 0; i + 1 < all.size(); i++) {
            List<Statement> s = all.get(i).statements();
            if (s.isEmpty() || ends.contains(i) || altered.contains(i)) continue;
            if (toNext(s.get(s.size() - 1), i + 1)) out.add(i);
        }
        return out;
    }

    /** A GO TO with one procedure, the paragraph `next` (not a section). */
    private static boolean toNext(Statement s, int next) {
        if (!s.verb().equals("GO TO") || !s.branches().isEmpty()) return false;
        List<Value.Target> to = s.operands().stream().filter(o -> o.value() instanceof Value.Target).map(o -> (Value.Target) o.value()).toList();
        boolean depending = s.operands().stream().anyMatch(o -> o.value() instanceof Value.Field);
        return !depending && to.size() == 1 && to.get(0).paragraph() == next && !to.get(0).section();
    }

    /** A range by its paragraphs' names, `FIRST` or `FIRST THRU LAST`. */
    public static String name(Program p, Subject.Range r) {
        String first = p.procedure().paragraphs().get(r.first()).name(), last = p.procedure().paragraphs().get(r.last()).name();
        return r.first() == r.last() ? first : first + " THRU " + last;
    }

    /** The obligations a range discharges to become a method (research §35, 2 to 4), on the graph and the contract. */
    static final class Obligations {
        private Obligations() {}

        /** What keeps a Function range from being a method: an entry or exit not its PERFORM's, the main line, another range overlapping it, a PERFORM of itself. */
        static List<String> refusals(Program p, Subject.Range r, List<Subject.Range> all) {
            List<String> why = new ArrayList<>();
            edges(p, r, why);
            BitSet main = mainLine(p);
            for (int g = r.first(); g <= r.last(); g++) if (main.get(g)) why.add(p.procedure().paragraphs().get(g).name() + " is on the main line");
            for (Subject.Range o : all) if (!o.equals(r) && o.first() <= r.last() && r.first() <= o.last()) why.add("it overlaps " + name(p, o));
            if (recursive(p, r)) why.add("it PERFORMs itself");
            return why;
        }

        /** Only a PERFORM of the whole range enters it, no GO TO enters or leaves it, no ALTER touches it. */
        private static void edges(Program p, Subject.Range r, List<String> why) {
            for (Procedure.Edge e : p.procedure().edges()) {
                boolean to = in(r, e.to()), from = in(r, e.from());
                if (e.kind() == Procedure.Kind.PERFORM && to && (e.to() != r.first() || e.thru() != r.last())) why.add("a PERFORM enters it otherwise, at " + e.origin().line());
                if (e.kind() == Procedure.Kind.GO_TO && to != from) why.add("a GO TO " + (to ? "enters" : "leaves") + " it, at " + e.origin().line());
                if (e.kind() == Procedure.Kind.ALTER && (to || from)) why.add("an ALTER reaches it, at " + e.origin().line());
            }
        }

        private static boolean in(Subject.Range r, int g) {
            return g >= r.first() && g <= r.last();
        }

        /**
         * The paragraphs control reaches from the start without a PERFORM: the contract's falls, GO TOs, ALTERs. A
         * `returns-if-called` fall counts in every program: one with USING may run as the main program, its PARM
         * its USING (`Lower`'s `parm`), and then EXIT PROGRAM falls (one reading, J-213; it was skipped for USING).
         */
        static BitSet mainLine(Program p) {
            Set<Integer> falls = new HashSet<>();
            for (String line : Contract.lines(p)) {
                String[] w = line.split(" ");
                if (w[0].equals("falls") && w[1].equals(p.header().program())) falls.add(Integer.parseInt(w[2]));
            }
            BitSet seen = new BitSet();
            Deque<Integer> work = new ArrayDeque<>(List.of(0));
            while (!work.isEmpty()) {
                int g = work.pop();
                if (g >= p.procedure().paragraphs().size() || seen.get(g)) continue;
                seen.set(g);
                if (falls.contains(g)) work.push(g + 1);
                for (Procedure.Edge e : p.procedure().edges()) if (e.kind() != Procedure.Kind.PERFORM && (e.from() == g || e.kind() == Procedure.Kind.ALTER && seen.get(e.from()))) work.push(e.to());
            }
            return seen;
        }

        /** Whether a PERFORM inside the range, or inside a range it PERFORMs, reaches the range again. */
        private static boolean recursive(Program p, Subject.Range r) {
            Deque<int[]> work = new ArrayDeque<>();
            work.push(new int[] {r.first(), r.last()});
            Set<Long> seen = new HashSet<>();
            while (!work.isEmpty()) {
                int[] at = work.pop();
                if (!seen.add(((long) at[0] << 32) | at[1])) continue;
                for (Procedure.Edge e : p.procedure().edges()) {
                    if (e.kind() != Procedure.Kind.PERFORM || e.from() < at[0] || e.from() > at[1]) continue;
                    if (e.to() <= r.last() && r.first() <= e.thru()) return true;
                    work.push(new int[] {e.to(), e.thru()});
                }
            }
            return false;
        }
    }
}
