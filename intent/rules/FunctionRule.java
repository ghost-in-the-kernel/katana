package katana.intent.rules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;

/**
 * `function.perform-range`: a PERFORM range that only a PERFORM enters, that leaves only at its end, that no
 * ALTER reaches, that does not PERFORM itself and that no other range overlaps is a function (docs/ROADMAP.md
 * 5.2), read on the paragraph graph. Ending the run inside it (STOP RUN, GOBACK) is not leaving it.
 */
final class FunctionRule implements Rule {
    public String name() {
        return "function.perform-range";
    }

    public int version() {
        return 1;
    }

    public double confidence() {
        return 1;
    }

    /** A range, first THRU last by paragraph index, and the PERFORM edges naming it. */
    private record Range(int first, int last, List<Integer> edges) {
        boolean holds(int paragraph) {
            return paragraph >= first && paragraph <= last;
        }

        boolean overlaps(Range o) {
            return first <= o.last && o.first <= last;
        }
    }

    /** Each distinct PERFORM range, by the edges naming it, kept where only a PERFORM enters it, it leaves only at its end and it does not PERFORM itself. */
    public List<Fact> infer(Program p) {
        List<Procedure.Edge> edges = p.procedure().edges();
        Map<List<Integer>, Range> ranges = new LinkedHashMap<>();
        for (int k = 0; k < edges.size(); k++) {
            Procedure.Edge e = edges.get(k);
            if (e.kind() != Procedure.Kind.PERFORM || e.thru() < e.to()) continue;
            ranges.computeIfAbsent(List.of(e.to(), e.thru()), r -> new Range(r.get(0), r.get(1), new ArrayList<>())).edges().add(k);
        }
        BitSet main = mainLine(p), jumped = new BitSet();
        for (Procedure.Edge e : edges) if (e.kind() != Procedure.Kind.PERFORM) jumped.set(e.to());
        List<Fact> facts = new ArrayList<>();
        for (Range r : ranges.values()) {
            if (!enteredOnlyByPerform(p, r, ranges.values(), main, jumped) || !leavesAtItsEnd(edges, r) || recursive(edges, r, ranges.values())) continue;
            List<Node> read = r.edges().stream().map(k -> (Node) new Node.EdgeNode(k)).toList();
            facts.add(new Fact(Kind.FUNCTION, new Subject.Range(r.first(), r.last()), new Evidence(name(), version(), read), confidence(), State.INFERRED));
        }
        return facts;
    }

    /**
     * No paragraph of it on the main line, in a declarative section or reached by a GO TO or ALTER from outside;
     * no other range overlapping it; and the paragraph before it, if it falls into it, neither on the main line,
     * nor jumped to, nor inside a range that runs on past it.
     */
    private static boolean enteredOnlyByPerform(Program p, Range r, Iterable<Range> all, BitSet main, BitSet jumped) {
        for (Range o : all) if (o != r && o.overlaps(r)) return false;
        for (Procedure.Edge e : p.procedure().edges()) {
            if (e.kind() == Procedure.Kind.ALTER && (r.holds(e.from()) || r.holds(e.to()))) return false;
            if (e.kind() == Procedure.Kind.GO_TO && r.holds(e.to()) && !r.holds(e.from())) return false;
        }
        for (int g = r.first(); g <= r.last(); g++) if (main.get(g) || declarative(p, g)) return false;
        int before = r.first() - 1;
        return before < 0 || !falls(p, paragraph(p, before)) || !(main.get(before) || jumped.get(before));
    }

    /** No GO TO from inside it to a paragraph outside it. */
    private static boolean leavesAtItsEnd(List<Procedure.Edge> edges, Range r) {
        for (Procedure.Edge e : edges) if (e.kind() == Procedure.Kind.GO_TO && r.holds(e.from()) && !r.holds(e.to())) return false;
        return true;
    }

    /** Whether a PERFORM inside it reaches, through the ranges it performs, a range overlapping it. */
    private static boolean recursive(List<Procedure.Edge> edges, Range r, Iterable<Range> all) {
        Deque<Range> work = new ArrayDeque<>(List.of(r));
        List<Range> seen = new ArrayList<>();
        while (!work.isEmpty()) {
            Range at = work.pop();
            for (Procedure.Edge e : edges) {
                if (e.kind() != Procedure.Kind.PERFORM || !at.holds(e.from())) continue;
                Range target = new Range(e.to(), e.thru(), List.of());
                if (target.overlaps(r)) return true;
                for (Range o : all) if (o.first() == e.to() && o.last() == e.thru() && !seen.contains(o)) {
                    seen.add(o);
                    work.push(o);
                }
            }
        }
        return false;
    }

    /** The paragraphs control reaches from the procedure's start without a PERFORM: by falling through, by GO TO, by an ALTERed GO TO. */
    private static BitSet mainLine(Program p) {
        BitSet main = new BitSet();
        Deque<Integer> work = new ArrayDeque<>(List.of(0));
        int count = p.procedure().paragraphs().size();
        while (!work.isEmpty()) {
            int g = work.pop();
            if (g >= count || main.get(g)) continue;
            main.set(g);
            if (falls(p, paragraph(p, g))) work.push(g + 1);
            for (Procedure.Edge e : p.procedure().edges()) {
                if (e.kind() == Procedure.Kind.GO_TO && e.from() == g) work.push(e.to());
                if (e.kind() == Procedure.Kind.ALTER && main.get(e.from())) work.push(e.to());
            }
        }
        return main;
    }

    /**
     * Whether control passes the paragraph's end into the next: no statement at its top level ends it, an
     * unconditional GO TO, STOP RUN, GOBACK, or EXIT PROGRAM in a subprogram (what follows one is never reached,
     * as CBSTM03A's `GO TO 0000-START. EXIT.`).
     */
    private static boolean falls(Program p, Procedure.Paragraph g) {
        for (Statement s : g.statements()) if (ends(p, s)) return false;
        return true;
    }

    // Unsure: EXIT PROGRAM ends a program with PROCEDURE DIVISION USING, read as a subprogram, and is passed over in one without, as IBM's does where no CALL reached it; a program without USING that is CALLed is read as falling (research §29).
    private static boolean ends(Program p, Statement s) {
        boolean keyword = s.operands().stream().anyMatch(o -> o.value() instanceof katana.sir.types.Value.Name n && (n.text().equals("RUN") || n.text().equals("PROGRAM")));
        return switch (s.verb()) {
            case "GO TO" -> s.operands().size() == 1 && s.operands().get(0).value() instanceof katana.sir.types.Value.Target;
            case "STOP" -> keyword;
            case "GOBACK" -> true;
            case "EXIT" -> keyword && !p.procedure().using().isEmpty();
            default -> false;
        };
    }

    private static boolean declarative(Program p, int index) {
        String section = paragraph(p, index).section();
        return p.procedure().sections().stream().anyMatch(s -> s.declarative() && s.name().equals(section));
    }

    private static Procedure.Paragraph paragraph(Program p, int index) {
        return p.procedure().paragraphs().get(index);
    }
}
