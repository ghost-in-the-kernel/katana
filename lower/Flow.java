package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * The procedure division as R-021's dispatcher: `run` steps `pc` through methods `s<label>`, each a
 * stretch of statements returning the next label; a paragraph's label is its index, its last stretch
 * ends by asking `Dispatcher.end`; an out-of-line PERFORM ends a stretch and its return is the next.
 * A construct holding such a PERFORM is flattened into stretches; any other is structured Java.
 */
final class Flow {
    final Emitter e;
    final Program p;
    final Exprs x;
    final Conditions conditions;
    final Arith arith;
    private final Verbs verbs;
    private final List<Integer> labels = new ArrayList<>();
    private final int end;
    private int next, counters, at;
    private final boolean segmented;
    final Debugging debugging;
    private Procedure.Paragraph current;
    /** The line of the statement being lowered, for DEBUG-LINE. */
    int line;
    private String sentenceEnd = "";
    String paragraph = "";

    /** The programs this one is nested in, the outermost first (LANE-19). */
    final List<Program> containers;

    /** A pass's plan: the ranges called as methods and the GO TOs left to fall (6.1). */
    final Lower.Plan plan;

    /** The pass's structured control: its methods, regions and `dispatch` (6.1, 6.2). */
    final Regions regions;

    Flow(Emitter e, Program p, Exprs x, List<Program> containers, Lower.Plan plan) {
        this.plan = plan;
        this.regions = new Regions(this);
        this.containers = containers;
        this.e = e;
        this.p = p;
        this.x = x;
        this.arith = new Arith(x);
        this.conditions = new Conditions(x, arith);
        this.end = p.procedure().paragraphs().size();
        this.next = end + 1;
        this.verbs = new Verbs(this);
        this.segmented = segmented(p);
        this.debugging = new Debugging(p, x);
    }

    /**
     * Whether the program's control depends on its segments: an independent segment (priority 50
     * through 99) and an ALTER, the one state a segment's initial state restores here (research §16).
     */
    static boolean segmented(Program p) {
        return p.procedure().sections().stream().anyMatch(s -> s.priority() >= 50)
                && p.procedure().edges().stream().anyMatch(g -> g.kind() == Procedure.Kind.ALTER);
    }

    /** Each paragraph's section's priority-number, as the `Dispatcher` takes them. */
    static String segments(Program p) {
        java.util.Map<String, Integer> priority = new java.util.HashMap<>();
        for (Procedure.Section s : p.procedure().sections()) priority.put(s.name(), s.priority());
        List<String> out = new ArrayList<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) out.add(String.valueOf(priority.getOrDefault(g.section(), -1)));
        return "new int[] {" + String.join(", ", out) + "}";
    }

    /** The control flow (R-021): run() from the first paragraph after the declaratives, a stretch method per label, and the step switch over them all. */
    // Decision: R-021
    void procedure() {
        e.line("private static final ArithProfile P = ArithProfile.IBM_COMPAT;");
        e.line("private final Collation co;");
        e.line("private boolean sz, ov, ae, cx;");
        e.open("public void run()");
        boolean main = !plan.main().isEmpty();
        if (!main) e.line("int pc = " + entry() + ";");
        if (!main) debugging.start(e, entry());
        boolean declaratives = p.procedure().sections().stream().anyMatch(Procedure.Section::declarative), halts = declaratives || !plan.methods().isEmpty() || main;
        if (halts) e.open("try");
        if (main) regions.region(new ArrayList<>(new java.util.TreeSet<>(plan.main())));
        else e.line("while (pc != " + end + ") pc = step(pc);");
        if (halts) {
            e.close();
            e.line("catch (Halt h) { }");
        }
        e.close();
        if (halts) e.line("private static final class Halt extends RuntimeException { Halt() { super(null, null, false, false); } }");
        declaratives();
        List<Procedure.Paragraph> paragraphs = p.procedure().paragraphs();
        for (Procedure.Paragraph para : paragraphs) {
            if (regions.inlined(para.index())) continue;
            paragraph = para.name();
            at = para.index();
            current = para;
            open(para.index());
            sentences(para);
            e.line("return " + end(para.index()) + ";");
            e.close();
        }
        if (paragraphs.isEmpty() && plan.main().isEmpty()) labels.add(0);
        regions.methods();
        // A switch with no case left is no switch (research §38): `step` and `dispatch` go with it.
        if (!labels.isEmpty()) step();
        if (!labels.isEmpty() && plan.structured()) regions.dispatch();
        if (counters > 0) e.line("private final long[] t = new long[" + counters + "];");
    }

    /**
     * Where the run begins: the first paragraph after the declaratives, whose procedures run only when
     * their USE condition does ("The USE statement itself is never executed", LR 6.4 p. 57).
     */
    // Decision: R-021
    private int entry() {
        java.util.Set<String> declarative = new java.util.HashSet<>();
        for (Procedure.Section sec : p.procedure().sections()) if (sec.declarative()) declarative.add(sec.name());
        for (Procedure.Paragraph g : p.procedure().paragraphs()) if (!declarative.contains(g.section())) return g.index();
        return end;
    }

    /**
     * A declarative's procedures run as a PERFORM from the I-O statement that called for them, control
     * then "returned to the next executable statement following the input/output statement" (LR p.
     * 706); a STOP RUN inside them ends the run (`Halt`).
     */
    // Decision: R-021
    private void declaratives() {
        if (p.procedure().sections().stream().noneMatch(Procedure.Section::declarative)) return;
        e.open("private void use(int first, int last)");
        e.line("int pc = d.perform(first, last, -2);");
        e.open("while (pc != -2)");
        e.line("if (pc == " + end + ") throw new Halt();");
        e.line("pc = step(pc);");
        e.close();
        e.close();
    }

    /** A paragraph as a region's own code: its trace line, then its sentences (6.2). */
    void paragraphCode(int g) {
        current = p.procedure().paragraphs().get(g);
        paragraph = current.name();
        at = g;
        trace(current);
        sentences(current);
        current = null;
    }

    /** How a statement leaves for the run's end: from a stretch its end label, from an inlined range `Halt`. */
    String leave() {
        return regions.active() ? "throw new Halt()" : "return " + end;
    }

    /** Whether the plan writes this PERFORM VARYING as a `for` (research §36). */
    boolean loop(Statement s) {
        return plan.loops().contains(Lower.Plan.loop(s.origin()));
    }

    /** Whether the plan calls this PERFORM's range as a method. */
    boolean method(Value.Target target) {
        return target != null && Integer.valueOf(target.thru()).equals(plan.methods().get(target.paragraph()));
    }

    /** Whether a PERFORM of the range is a call: under a pass's plan, of its method or through `dispatch` (research §38). */
    boolean called(Value.Target target) {
        return target != null && plan.structured();
    }

    /** The call of a PERFORMed range: its method, or `dispatch`. */
    String call(Value.Target target) {
        return method(target) ? "perform" + target.paragraph() + "();" : "dispatch(" + target.paragraph() + ", " + target.thru() + ");";
    }

    private void open(int label) {
        labels.add(label);
        e.open("private int s" + label + "()");
        e.symbol("s" + label, "paragraph " + at);
        if (segmented) e.line(label == at ? "d.enter(" + at + ");" : "d.resume(" + at + ");");
        if (label == at && current != null) {
            debugging.entry(e, current);
            trace(current);
        }
    }

    /**
     * The java lane's paragraph trace (rt/Trace, research §27): a paragraph's start says its section when it
     * is the section's first, then its own name; an unnamed paragraph says nothing.
     */
    private void trace(Procedure.Paragraph g) {
        String program = Emitter.quote(p.header().program());
        boolean first = !g.section().isEmpty() && p.procedure().paragraphs().stream().filter(o -> o.section().equals(g.section())).findFirst().orElseThrow() == g;
        if (first) e.line("Trace.section(" + program + ", " + Emitter.quote(g.section()) + ");");
        if (!g.name().isEmpty()) e.line("Trace.paragraph(" + program + ", " + Emitter.quote(g.name()) + ");");
    }

    /** Ends this stretch and opens the one at `label`. */
    void split(int label) {
        e.close();
        open(label);
    }

    int label() {
        return next++;
    }

    /** The index of the paragraph being lowered. */
    int paragraphIndex() {
        return at;
    }

    int counter() {
        return counters++;
    }

    int end() {
        return end;
    }

    private void step() {
        e.open("private int step(int pc)");
        e.open("return switch (pc)");
        for (int l : labels) e.line("case " + l + " -> s" + l + "();");
        e.line("default -> throw new IllegalStateException(\"pc \" + pc);");
        indentEnd();
        e.close();
    }

    private void indentEnd() {
        e.close();
        e.line(";");
    }

    /** A paragraph's sentences in order; one that holds a NEXT SENTENCE ends its stretch, the next sentence (or the paragraph's end) its target. */
    // Decision: R-021
    private void sentences(Procedure.Paragraph para) {
        List<Integer> starts = new ArrayList<>(para.sentences());
        starts.add(para.statements().size());
        for (int k = 0; k + 1 < starts.size(); k++) {
            List<Statement> sentence = para.statements().subList(starts.get(k), starts.get(k + 1));
            boolean last = k + 2 == starts.size();
            int after = !last && holdsNextSentence(sentence) ? label() : -1;
            sentenceEnd = after >= 0 ? String.valueOf(after) : end(para.index());
            statements(sentence);
            if (after < 0) continue;
            e.line("return " + after + ";");
            split(after);
        }
    }

    private static boolean holdsNextSentence(List<Statement> list) {
        for (Statement s : list) {
            if (s.verb().equals("NEXT SENTENCE")) return true;
            for (Statement.Branch b : s.branches()) if (holdsNextSentence(b.statements())) return true;
        }
        return false;
    }

    /** NEXT SENTENCE: to the first statement after the sentence's period (LR 6.4 p. 435). */
    void nextSentence() {
        jump(sentenceEnd);
    }

    /** The end of paragraph `paragraph`: back from a PERFORM whose range ends here, else on to the next (`Dispatcher.end`). */
    String end(int paragraph) {
        return debugging.end(paragraph, "d.end(" + paragraph + ", " + (paragraph + 1) + ")");
    }

    /** An out-of-line PERFORM of `target`, coming back to `back`. */
    String perform(Value.Target target, int back) {
        return debugging.perform("d.perform(" + target.paragraph() + ", " + target.thru() + ", " + back + ")", line, target.section());
    }

    /** Statements in order, each after its provenance comment. */
    void statements(List<Statement> list) {
        for (Statement s : list) {
            line = s.origin().line();
            e.provenance(s.origin(), paragraph, s.verb());
            if (falls(s)) e.line("// GO TO the next paragraph: left to fall (structuring, 6.1)");
            else verbs.statement(s);
        }
    }

    /** Whether the plan leaves this statement, its paragraph's last and a GO TO to the next, to fall. */
    private boolean falls(Statement s) {
        if (current == null || !plan.falls().contains(at) || !s.verb().equals("GO TO")) return false;
        List<Statement> all = current.statements();
        return !all.isEmpty() && all.get(all.size() - 1) == s;
    }

    /** Whether a statement holds an out-of-line PERFORM (but one a pass's plan makes a call), a VARYING loop, a SEARCH or a SORT's procedure, here or in a phrase, so it cannot be structured Java. */
    boolean splits(Statement s) {
        if (s.verb().equals("PERFORM") && (Performs.varies(s) && !loop(s) || target(s) != null && !called(target(s)))) return true;
        if (s.verb().equals("SEARCH")) return true;
        // A SORT or MERGE with an INPUT or OUTPUT PROCEDURE performs it (SortVerbs).
        if ((s.verb().equals("SORT") || s.verb().equals("MERGE")) && s.operands().stream().anyMatch(o -> o.value() instanceof Value.Target)) return true;
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) if (splits(t)) return true;
        return false;
    }

    boolean splits(List<Statement> list) {
        return list.stream().anyMatch(this::splits);
    }

    /** An out-of-line PERFORM's procedures, from the first named through the last (`A THRU B` is two targets), null for an inline one. */
    static Value.Target target(Statement s) {
        Value.Target first = null, last = null;
        for (Statement.Operand o : s.operands()) {
            if (!(o.value() instanceof Value.Target t) || !o.role().equals("perform-procedure-statement")) continue;
            if (first == null) first = t;
            last = t;
        }
        return first == null ? null : new Value.Target(first.paragraph(), last.thru(), first.section());
    }

    /** `then` when `condition` holds, else `otherwise`: structured, or as stretches when either holds a PERFORM. */
    void branch(String condition, List<Statement> then, List<Statement> otherwise) {
        if (!splits(then) && !splits(otherwise)) {
            e.open("if (" + condition + ")");
            statements(then);
            if (!otherwise.isEmpty()) {
                e.close();
                e.open("else");
                statements(otherwise);
            }
            e.close();
            return;
        }
        int elseLabel = label(), after = label();
        e.line("if (!(" + condition + ")) return " + elseLabel + ";");
        statements(then);
        e.line("return " + after + ";");
        split(elseLabel);
        statements(otherwise);
        e.line("return " + after + ";");
        split(after);
    }

    /** A jump: `if (true)` so that what follows it in the same stretch still compiles. */
    void jump(String to) {
        if (!regions.active()) {
            e.line("if (true) return " + to + ";");
            return;
        }
        String reach = regions.jump(to, at);
        e.line("if (true) " + (reach != null ? reach : "throw new IllegalStateException(\"a jump to " + to + " no region reaches\")") + ";");
    }
}
