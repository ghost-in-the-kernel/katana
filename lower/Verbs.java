package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Procedure;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** Each verb's Java, by its operands' roles (sir/roles.golden); a verb not yet lowered is a line that fails, and a finding. */
final class Verbs {
    private final Flow f;
    private final ArithVerbs arithmetic;
    private final Performs performs;
    private final TextVerbs text;
    private final FileVerbs files;
    private final Evaluate evaluate;

    Verbs(Flow f) {
        this.f = f;
        this.arithmetic = new ArithVerbs(f);
        this.performs = new Performs(f);
        this.text = new TextVerbs(f);
        this.files = new FileVerbs(f);
        this.evaluate = new Evaluate(f);
    }

    // Decision: R-021
    void statement(Statement s) {
        switch (s.verb()) {
            case "MOVE" -> move(s);
            case "DISPLAY" -> display(s);
            case "ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "COMPUTE" -> arithmetic.statement(s);
            case "IF" -> f.branch(f.conditions.of(operand(s, "if-statement")), branch(s, "if-then"), branch(s, "if-else"));
            case "PERFORM" -> performs.statement(s);
            case "GO TO" -> goTo(s);
            case "STOP", "GOBACK" -> f.jump(String.valueOf(f.end()));
            case "EXIT" -> exit(s);
            case "ALTER" -> alter(s);
            case "INSPECT" -> text.inspect(s);
            case "STRING" -> text.string(s);
            case "UNSTRING" -> text.unstring(s);
            case "INITIALIZE" -> Initialize.statement(f, s);
            case "EVALUATE" -> evaluate.statement(s);
            case "OPEN" -> files.open(s);
            case "CLOSE" -> files.close(s);
            case "WRITE" -> files.write(s);
            case "READ" -> files.read(s);
            case "CONTINUE" -> f.e.line("// CONTINUE");
            default -> f.e.line(f.x.unsupported(s.verb() + " at " + s.origin()) + ";");
        }
    }

    /** The first operand of a role, null when none. */
    static Value operand(Statement s, String role) {
        for (Statement.Operand o : s.operands()) if (o.role().equals(role) && !(o.value() instanceof Value.Name n && n.kind().equals("keyword"))) return o.value();
        return null;
    }

    /** The operands of the given roles, keywords left out. */
    static List<Value> operands(Statement s, String... roles) {
        List<Value> out = new ArrayList<>();
        for (Statement.Operand o : s.operands()) if (List.of(roles).contains(o.role()) && !(o.value() instanceof Value.Name n && n.kind().equals("keyword"))) out.add(o.value());
        return out;
    }

    static boolean keyword(Statement s, String word) {
        for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.Name n && n.kind().equals("keyword") && n.text().equals(word)) return true;
        return false;
    }

    static List<Statement> branch(Statement s, String label) {
        for (Statement.Branch b : s.branches()) if (b.label().equals(label)) return b.statements();
        return List.of();
    }

    private void move(Statement s) {
        Value from = operand(s, "move-to-sending-area");
        if (from == null) {
            f.e.line(f.x.unsupported("MOVE CORRESPONDING at " + s.origin()) + ";");
            return;
        }
        for (Value to : operands(s, "move-to-statement")) {
            Value.Field t = (Value.Field) to;
            f.e.line(f.x.moveInto(from, f.x.field(t), f.x.item(t.item())) + ";");
        }
    }

    // Unsure: a numeric literal is displayed as its sign and digits, no point, as cobc -std=ibm shows it (J-114).
    private void display(Statement s) {
        List<String> parts = new ArrayList<>();
        for (Value v : operands(s, "display-operand")) parts.add(shown(v));
        Value upon = operand(s, "display-upon");
        boolean syserr = upon instanceof Value.Name n && n.text().equals("SYSERR");
        f.e.line((syserr ? "err" : "out") + ".display(" + !keyword(s, "ADVANCING") + (parts.isEmpty() ? "" : ", ") + String.join(", ", parts) + ");");
    }

    private String shown(Value v) {
        return switch (v) {
            case Value.Field fld -> f.x.field(fld);
            case Value.Literal l when l.numeric() -> "Literal.bytes(Literal.text(" + Emitter.quote(l.payload()) + ", cp))";
            case Value.Figurative g when g.all() == null -> f.x.pattern(g);
            default -> "Literal.bytes(" + f.x.sending(v) + ")";
        };
    }

    private void goTo(Statement s) {
        List<Value> targets = operands(s, "go-to-statement-simple", "go-to-depending-on-statement", "go-to-statement");
        Value depending = targets.stream().filter(v -> v instanceof Value.Field).findFirst().orElse(null);
        List<Value.Target> to = targets.stream().filter(v -> v instanceof Value.Target).map(v -> (Value.Target) v).toList();
        if (depending == null) {
            f.jump(altered(to.isEmpty() ? -1 : to.get(0).paragraph()));
            return;
        }
        f.e.open("");
        f.e.line("long g = Table.index(" + f.x.field((Value.Field) depending) + ");");
        for (int k = 0; k < to.size(); k++) f.e.line("if (g == " + (k + 1) + ") return " + to.get(k).paragraph() + ";");
        f.e.close();
    }

    /** A GO TO's target, through the dispatcher's table when an ALTER names this paragraph. */
    private String altered(int target) {
        int here = paragraphIndex();
        for (Procedure.Paragraph p : f.p.procedure().paragraphs()) for (Statement t : p.statements()) if (alters(t, here)) return "d.goTo(" + here + ", " + target + ")";
        return String.valueOf(target);
    }

    private static boolean alters(Statement s, int paragraph) {
        if (s.verb().equals("ALTER") && !operands(s, "alter-statement").isEmpty() && operands(s, "alter-statement").get(0) instanceof Value.Target t && t.paragraph() == paragraph) return true;
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) if (alters(t, paragraph)) return true;
        return false;
    }

    /** ALTER p TO PROCEED TO q: each pair, the paragraph whose GO TO changes and its new target. */
    private void alter(Statement s) {
        List<Value> targets = operands(s, "alter-statement", "alter-proceed-to");
        for (int k = 0; k + 1 < targets.size(); k += 2) {
            if (targets.get(k) instanceof Value.Target from && targets.get(k + 1) instanceof Value.Target to) f.e.line("d.alter(" + from.paragraph() + ", " + to.paragraph() + ");");
        }
    }

    private int paragraphIndex() {
        for (Procedure.Paragraph p : f.p.procedure().paragraphs()) if (p.name().equals(f.paragraph)) return p.index();
        return -1;
    }

    private void exit(Statement s) {
        if (keyword(s, "PROGRAM")) f.jump(String.valueOf(f.end()));
        else if (keyword(s, "PARAGRAPH") || keyword(s, "SECTION")) {
            int last = keyword(s, "SECTION") ? lastOfSection() : paragraphIndex();
            f.jump("d.end(" + last + ", " + (last + 1) + ")");
        }
        else f.e.line("// EXIT");
    }

    private int lastOfSection() {
        String section = f.p.procedure().paragraphs().get(paragraphIndex()).section();
        int last = paragraphIndex();
        for (Procedure.Paragraph p : f.p.procedure().paragraphs()) if (p.section().equals(section)) last = p.index();
        return last;
    }
}
