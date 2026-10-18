package katana.intent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import katana.sir.contract.Contract;
import katana.sir.types.Procedure;
import katana.sir.types.Program;

/**
 * T-F1 (research §33): R-066's structural reading of a `function` fact, one entry, one exit, no GO TO out,
 * checked on what sir/ says of the program (its paragraph graph and its contract's `falls` lines), never on
 * intent/rules' own code, so the rule's errors are not its expected side.
 */
final class FunctionRangeStructure {
    private FunctionRangeStructure() {}

    /** A range by paragraph index, first THRU last. */
    record Range(int first, int last) {
        boolean holds(int g) {
            return g >= first && g <= last;
        }
    }

    /** Why a range is not a function on the graph, each reason with the edge or paragraph that shows it; empty when it is. */
    static List<String> refusals(Program p, Range r, List<Range> others) {
        List<String> why = new ArrayList<>();
        edges(p, r, why);
        mainLine(p, r, why);
        for (Range o : others) if (!o.equals(r) && o.first() <= r.last() && r.first() <= o.last()) why.add("another range, " + name(p, o.first()) + " THRU " + name(p, o.last()) + ", overlaps it");
        for (int g = r.first(); g <= r.last(); g++) if (declarative(p, g)) why.add(name(p, g) + " is in a declarative section");
        return why;
    }

    /** One entry and one exit by the graph's edges: a PERFORM only of the whole range, no GO TO in or out, no ALTER. */
    private static void edges(Program p, Range r, List<String> why) {
        for (Procedure.Edge e : p.procedure().edges()) {
            boolean into = r.holds(e.to()) && !r.holds(e.from());
            if (e.kind() == Procedure.Kind.PERFORM && r.holds(e.to()) && (e.to() != r.first() || e.thru() != r.last())) why.add("a PERFORM of " + name(p, e.to()) + " THRU " + name(p, e.thru()) + " from " + name(p, e.from()) + " enters it otherwise");
            if (e.kind() == Procedure.Kind.GO_TO && into) why.add("GO TO " + name(p, e.to()) + " from " + name(p, e.from()) + " enters it");
            if (e.kind() == Procedure.Kind.ALTER && (r.holds(e.from()) || r.holds(e.to()))) why.add("an ALTER reaches it (" + name(p, e.from()) + " to " + name(p, e.to()) + ")");
            if (e.kind() == Procedure.Kind.GO_TO && r.holds(e.from()) && !r.holds(e.to())) why.add("GO TO " + name(p, e.to()) + " from " + name(p, e.from()) + " leaves it");
        }
    }

    /** No paragraph of the range on the main line, and the main line not falling into its first. */
    private static void mainLine(Program p, Range r, List<String> why) {
        BitSet main = mainLine(p);
        for (int g = r.first(); g <= r.last(); g++) if (main.get(g)) why.add(name(p, g) + " is on the main line");
        if (r.first() > 0 && falls(p).contains(r.first() - 1) && main.get(r.first() - 1)) why.add("the main line falls into it from " + name(p, r.first() - 1));
    }

    /** The paragraphs whose end falls into the next, read from the contract's `falls` lines; a `returns-if-called` one falls only in a program that is not a subprogram (no USING). */
    static List<Integer> falls(Program p) {
        List<Integer> out = new ArrayList<>();
        boolean subprogram = !p.procedure().using().isEmpty();
        for (String line : Contract.lines(p)) {
            if (!line.startsWith("falls ")) continue;
            String[] w = line.split(" ");
            if (w.length > 4 && w[4].equals("returns-if-called") && subprogram) continue;
            out.add(Integer.parseInt(w[2]));
        }
        return out;
    }

    /** The paragraphs control reaches from the start without a PERFORM: by the contract's falls, GO TO edges, and an ALTER's new target. */
    static BitSet mainLine(Program p) {
        List<Integer> falls = falls(p);
        BitSet seen = new BitSet();
        Deque<Integer> work = new ArrayDeque<>(List.of(0));
        while (!work.isEmpty()) {
            int g = work.pop();
            if (g >= p.procedure().paragraphs().size() || seen.get(g)) continue;
            seen.set(g);
            if (falls.contains(g)) work.push(g + 1);
            for (Procedure.Edge e : p.procedure().edges()) {
                if (e.kind() == Procedure.Kind.GO_TO && e.from() == g) work.push(e.to());
                if (e.kind() == Procedure.Kind.ALTER && seen.get(e.from())) work.push(e.to());
            }
        }
        return seen;
    }

    private static boolean declarative(Program p, int g) {
        String section = p.procedure().paragraphs().get(g).section();
        return p.procedure().sections().stream().anyMatch(s -> s.declarative() && s.name().equals(section));
    }

    static String name(Program p, int g) {
        String n = p.procedure().paragraphs().get(g).name();
        return n.isBlank() ? "the main line's opening statements" : n;
    }

    /** A range as a review line writes it, `FIRST` or `FIRST THRU LAST`, by paragraph name; null where a name is not the program's. */
    static Range range(Program p, String subject) {
        String[] ends = subject.split("\\s+THRU\\s+");
        int first = index(p, ends[0]), last = index(p, ends[ends.length - 1]);
        return first < 0 || last < 0 ? null : new Range(first, last);
    }

    private static int index(Program p, String name) {
        for (Procedure.Paragraph g : p.procedure().paragraphs()) if (g.name().equals(name.trim())) return g.index();
        return -1;
    }
}
