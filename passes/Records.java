package katana.passes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.7, record classes (research §46): the group 01-levels of WORKING-STORAGE and the FILE SECTION nothing
 * aliases, which `lower/` writes as a class over the record's bytes, each member a named `Field`, a numeric one
 * with a typed getter and a table's with an accessor. The bytes stay the truth: no byte and no operation
 * changes. A record that fails an obligation keeps its fields, and the pass says why.
 */
public final class Records {
    public static final String PASS = "records-v1";
    /** The usages a view leaves to the bytes alone (research §46's survey: pointers, floating point, national, native binary). */
    private static final Set<Attributes.Usage> ALIASED = Set.of(Attributes.Usage.POINTER, Attributes.Usage.PROCEDURE_POINTER, Attributes.Usage.FUNCTION_POINTER,
            Attributes.Usage.FLOAT_SHORT, Attributes.Usage.FLOAT_LONG, Attributes.Usage.NATIONAL, Attributes.Usage.NATIVE_BINARY);

    private Records() {}

    /** The records written as classes, by their 01's id; each refused, by `record NAME`, with why. */
    // Decision: R-032
    public static Set<Integer> records(Program p, boolean literal, Map<String, List<String>> refused) {
        if (literal) return Set.of();
        Set<Integer> refmod = new HashSet<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) refmod(s, refmod);
        Set<Integer> out = new TreeSet<>();
        for (Item r : p.items()) {
            if (r.level() != 1 || r.attributes().category() != Attributes.Category.GROUP) continue;
            List<String> why = refusals(p, r, refmod);
            if (why.isEmpty()) out.add(r.id());
            else refused.put("record " + r.name(), why);
        }
        return out;
    }

    /** The obligations: WORKING-STORAGE or a file's; nothing aliasing its bytes. */
    private static List<String> refusals(Program p, Item r, Set<Integer> refmod) {
        List<String> why = new ArrayList<>();
        if (r.section() != Item.Section.WORKING_STORAGE && r.section() != Item.Section.FILE) why.add("in " + r.section() + ", whose bytes another program binds");
        if (!r.flags().isEmpty()) why.add("it is " + r.flags());
        for (Item i : p.items()) {
            if (i.redefines() == r.id()) why.add(i.name() + " redefines it");
            if (i.record() != r.id()) continue;
            if (i.redefines() != 0) why.add(i.name() + " redefines");
            if (i.level() == 66) why.add(i.name() + " RENAMES part of it");
            if (refmod.contains(i.id())) why.add(i.name() + " is reference-modified");
            if (i.occurs() != null && i.occurs().dependingOn() != 0) why.add(i.name() + " occurs depending on");
            if (ALIASED.contains(i.attributes().usage())) why.add(i.name() + " is " + i.attributes().usage());
        }
        return why;
    }

    private static void refmod(Statement s, Set<Integer> out) {
        for (Statement.Operand o : s.operands()) refmod(o.value(), out);
        for (Statement.Branch b : s.branches()) {
            refmod(b.condition(), out);
            for (Statement t : b.statements()) refmod(t, out);
        }
    }

    private static void refmod(Value v, Set<Integer> out) {
        switch (v) {
            case Value.Field f -> {
                if (f.start() != null) out.add(f.item());
                f.subscripts().forEach(x -> refmod(x, out));
            }
            case Value.Expression e -> e.operands().forEach(x -> refmod(x, out));
            case null, default -> {}
        }
    }
}
