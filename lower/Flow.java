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
        e.line("// Unsure: the native order stands for the program collating sequence until the IR carries it (J-114).");
        e.line("private final Collation co = Collation.nativeOrder();");
        e.line("private boolean sz, ov;");
        e.open("public void run()");
        e.line("int pc = 0;");
        e.line("while (pc != " + end + ") pc = step(pc);");
        e.close();
        List<Procedure.Paragraph> paragraphs = p.procedure().paragraphs();
        for (Procedure.Paragraph para : paragraphs) {
            paragraph = para.name();
            open(para.index());
            statements(para.statements());
            e.line("return d.end(" + para.index() + ", " + (para.index() + 1) + ");");
            e.close();
        }
        if (paragraphs.isEmpty()) labels.add(0);
        step();
        if (counters > 0) e.line("private final long[] t = new long[" + counters + "];");
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

    /** Statements in order, each after its provenance comment. */
    void statements(List<Statement> list) {
        for (Statement s : list) {
            e.provenance(s.origin(), paragraph, s.verb());
            verbs.statement(s);
        }
    }

    /** Whether a statement holds an out-of-line PERFORM or a VARYING loop, here or in a phrase, so it cannot be structured Java. */
    static boolean splits(Statement s) {
        if (s.verb().equals("PERFORM") && (target(s) != null || !Verbs.operands(s, "perform-varying-clause", "perform-varying-phrase").isEmpty())) return true;
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) if (splits(t)) return true;
        return false;
    }

    static boolean splits(List<Statement> list) {
        return list.stream().anyMatch(Flow::splits);
    }

    /** An out-of-line PERFORM's procedures, null for an inline one. */
    static Value.Target target(Statement s) {
        for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.Target t && o.role().equals("perform-procedure-statement")) return t;
        return null;
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
