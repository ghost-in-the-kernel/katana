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
    private int next, counters;
    private String sentenceEnd = "";
    String paragraph = "";

    Flow(Emitter e, Program p, Exprs x) {
        this.e = e;
        this.p = p;
        this.x = x;
        this.arith = new Arith(x);
        this.conditions = new Conditions(x, arith);
        this.end = p.procedure().paragraphs().size();
        this.next = end + 1;
        this.verbs = new Verbs(this);
    }

    // Decision: R-021
    void procedure() {
        e.line("private static final ArithProfile P = ArithProfile.IBM_COMPAT;");
        e.line("private final Collation co;");
        e.line("private boolean sz, ov, ae;");
        e.open("public void run()");
        e.line("int pc = " + entry() + ";");
        boolean declaratives = p.procedure().sections().stream().anyMatch(Procedure.Section::declarative);
        if (declaratives) e.open("try");
        e.line("while (pc != " + end + ") pc = step(pc);");
        if (declaratives) {
            e.close();
            e.line("catch (Halt h) { }");
        }
        e.close();
        declaratives();
        List<Procedure.Paragraph> paragraphs = p.procedure().paragraphs();
        for (Procedure.Paragraph para : paragraphs) {
            paragraph = para.name();
            open(para.index());
            sentences(para);
            e.line("return d.end(" + para.index() + ", " + (para.index() + 1) + ");");
            e.close();
        }
        if (paragraphs.isEmpty()) labels.add(0);
        step();
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
        e.line("private static final class Halt extends RuntimeException { Halt() { super(null, null, false, false); } }");
        e.open("private void use(int first, int last)");
        e.line("int pc = d.perform(first, last, -2);");
        e.open("while (pc != -2)");
        e.line("if (pc == " + end + ") throw new Halt();");
        e.line("pc = step(pc);");
        e.close();
        e.close();
    }

    private void open(int label) {
        labels.add(label);
        e.open("private int s" + label + "()");
    }

    /** Ends this stretch and opens the one at `label`. */
    void split(int label) {
        e.close();
        open(label);
    }

    int label() {
        return next++;
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
            sentenceEnd = after >= 0 ? String.valueOf(after) : "d.end(" + para.index() + ", " + (para.index() + 1) + ")";
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

    /** Statements in order, each after its provenance comment. */
    void statements(List<Statement> list) {
        for (Statement s : list) {
            e.provenance(s.origin(), paragraph, s.verb());
            verbs.statement(s);
        }
    }

    /** Whether a statement holds an out-of-line PERFORM, a VARYING loop or a SEARCH, here or in a phrase, so it cannot be structured Java. */
    static boolean splits(Statement s) {
        if (s.verb().equals("PERFORM") && (target(s) != null || Performs.varies(s))) return true;
        if (s.verb().equals("SEARCH")) return true;
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) if (splits(t)) return true;
        return false;
    }

    static boolean splits(List<Statement> list) {
        return list.stream().anyMatch(Flow::splits);
    }

    /** An out-of-line PERFORM's procedures, from the first named through the last (`A THRU B` is two targets), null for an inline one. */
    static Value.Target target(Statement s) {
        Value.Target first = null, last = null;
        for (Statement.Operand o : s.operands()) {
            if (!(o.value() instanceof Value.Target t) || !o.role().equals("perform-procedure-statement")) continue;
            if (first == null) first = t;
            last = t;
        }
        return first == null ? null : new Value.Target(first.paragraph(), last.thru());
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
        e.line("if (true) return " + to + ";");
    }
}
