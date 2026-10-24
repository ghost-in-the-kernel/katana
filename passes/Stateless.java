package katana.passes;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.6, stateless subprograms (research §44): a program none of whose calls can observe what an earlier call left in
 * WORKING-STORAGE, every byte a statement writes written before it is read on every path (`Dataflow`), every other
 * byte its initial value always; lowered as INITIAL, so the run unit starts each call fresh. What else carries state
 * between calls keeps a program out: a file, an EXTERNAL or GLOBAL item, a POINTER, a nested program, and what the
 * dataflow does not model.
 */
public final class Stateless {
    public static final String PASS = "stateless-v1";

    private Stateless() {}

    /** Why the program is not stateless; empty when it is. */
    // Decision: R-032
    public static List<String> refusals(Program p) {
        List<String> why = new ArrayList<>();
        if (!p.files().isEmpty()) why.add("it has files, whose state outlives a call");
        if (!p.nested().isEmpty()) why.add("it contains nested programs");
        for (Item i : p.items()) {
            if (i.flags().contains(Item.Flag.EXTERNAL) || i.flags().contains(Item.Flag.GLOBAL)) why.add(i.name() + " is " + i.flags());
            if (i.attributes().category() == Attributes.Category.POINTER) why.add(i.name() + " is a POINTER");
        }
        if (p.procedure().sections().stream().anyMatch(Procedure.Section::declarative)) why.add("it has declaratives");
        if (p.procedure().sections().stream().anyMatch(s -> s.priority() >= 50)) why.add("it has independent segments");
        if (p.procedure().edges().stream().anyMatch(e -> e.kind() == Procedure.Kind.ALTER)) why.add("it ALTERs");
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) unmodelled(s, why);
        if (why.isEmpty()) why.addAll(new Dataflow(p).findings);
        return why;
    }

    /** What the dataflow does not model: NEXT SENTENCE, EXIT PARAGRAPH, SECTION or PERFORM (a body's later statements may not run: R-090), ADDRESS OF. */
    private static void unmodelled(Statement s, List<String> why) {
        if (s.verb().equals("NEXT SENTENCE")) why.add("line " + s.origin().line() + " is a NEXT SENTENCE");
        if (s.verb().equals("EXIT") && s.operands().stream().anyMatch(o -> o.value() instanceof Value.Name n && (n.text().equals("PARAGRAPH") || n.text().equals("SECTION"))))
            why.add("line " + s.origin().line() + " is an EXIT " + "PARAGRAPH or SECTION");
        if (s.verb().equals("EXIT") && s.operands().stream().anyMatch(o -> o.value() instanceof Value.Name n && n.text().equals("PERFORM"))) why.add("line " + s.origin().line() + " is an EXIT PERFORM");
        for (Statement.Operand o : s.operands()) if (address(o.value())) why.add("line " + s.origin().line() + " takes an ADDRESS OF");
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) unmodelled(t, why);
    }

    private static boolean address(Value v) {
        return v instanceof Value.Expression e && (e.operator().equals("ADDRESS OF") || e.operands().stream().anyMatch(Stateless::address));
    }
}
