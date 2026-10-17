package katana.intent.rules;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** What the rules read of a program's statements: every statement, branches included, and the items a value refers to. */
final class Walk {
    private Walk() {}

    /** Every statement of the procedure, each branch's statements after the statement holding them. */
    static List<Statement> statements(Program p) {
        List<Statement> out = new ArrayList<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) add(s, out);
        return out;
    }

    private static void add(Statement s, List<Statement> out) {
        out.add(s);
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) add(t, out);
    }

    /** How many times a value refers to an item, through expressions. */
    static int refers(Value v, int item) {
        if (v instanceof Value.Field f) return f.item() == item ? 1 : 0;
        if (v instanceof Value.Expression e) return e.operands().stream().mapToInt(o -> refers(o, item)).sum();
        return 0;
    }
}
