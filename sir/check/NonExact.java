package katana.sir.check;

import java.util.ArrayList;
import java.util.List;
import katana.findings.Finding;
import katana.profile.Profile;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Origin;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * F-08's non-exact classes found in the IR (docs/ROADMAP.md 2.7), before any lowering: TRUNC(OPT) over
 * binary items; PERFORM ranges that overlap without one containing the other; a PERFORM of a range
 * from inside it; DIVIDE, or a COMPUTE that divides, with no ON SIZE ERROR; each OCCURS DEPENDING
 * table; each COMP-1 or COMP-2 item. The profile must be the one the IR was built under.
 */
public final class NonExact {
    private NonExact() {}

    public static List<Finding> findings(Program program, Profile profile) {
        if (!profile.hash().equals(program.header().profileHash())) {
            throw new IllegalArgumentException("the IR was built under " + program.header().profileHash() + ", not " + profile.hash());
        }
        List<Finding> out = new ArrayList<>();
        check(program, profile, out);
        return out;
    }

    private static void check(Program p, Profile profile, List<Finding> out) {
        String in = p.header().program() + ": ";
        boolean opt = "OPT".equals(profile.ibm().get("ibm.trunc"));
        for (Item i : p.items()) {
            Attributes.Usage u = i.attributes().usage();
            if (opt && (u == Attributes.Usage.BINARY) && i.attributes().category() == Attributes.Category.NUMERIC) {
                out.add(new Finding(Finding.Kind.NE_01, where(i.origin()), in + i.name() + " is BINARY under TRUNC(OPT)"));
            }
            if (u == Attributes.Usage.FLOAT_SHORT || u == Attributes.Usage.FLOAT_LONG) out.add(new Finding(Finding.Kind.NE_06, where(i.origin()), in + i.name() + " is " + u));
            if (i.occurs() != null && i.occurs().dependingOn() != 0) {
                out.add(new Finding(Finding.Kind.NE_05, where(i.origin()), in + i.name() + " OCCURS DEPENDING ON " + p.items().get(i.occurs().dependingOn() - 1).name()));
            }
        }
        ranges(p, in, out);
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) division(s, in, out);
        for (Program nested : p.nested()) check(nested, profile, out);
    }

    /** Two PERFORM ranges that share a paragraph and neither contains the other; a PERFORM from inside its own range. */
    private static void ranges(Program p, String in, List<Finding> out) {
        List<Procedure.Edge> performs = p.procedure().edges().stream().filter(e -> e.kind() == Procedure.Kind.PERFORM).toList();
        List<Procedure.Paragraph> paragraphs = p.procedure().paragraphs();
        for (int a = 0; a < performs.size(); a++) {
            Procedure.Edge x = performs.get(a);
            if (x.from() >= x.to() && x.from() <= x.thru()) {
                out.add(new Finding(Finding.Kind.NE_03, where(x.origin()), in + name(paragraphs, x.from()) + " performs " + range(paragraphs, x) + ", which it is in"));
            }
            for (int b = a + 1; b < performs.size(); b++) {
                Procedure.Edge y = performs.get(b);
                boolean share = x.to() <= y.thru() && y.to() <= x.thru();
                boolean nested = (x.to() <= y.to() && y.thru() <= x.thru()) || (y.to() <= x.to() && x.thru() <= y.thru());
                if (share && !nested) out.add(new Finding(Finding.Kind.NE_02, where(y.origin()), in + range(paragraphs, x) + " and " + range(paragraphs, y) + " overlap"));
            }
        }
    }

    /** DIVIDE, or COMPUTE with `/`, whose branches have no ON SIZE ERROR phrase. */
    private static void division(Statement s, String in, List<Finding> out) {
        boolean divides = s.verb().equals("DIVIDE") || (s.verb().equals("COMPUTE") && s.operands().stream().anyMatch(o -> divides(o.value())));
        boolean guarded = s.branches().stream().anyMatch(b -> b.label().contains("size-error") && !b.label().contains("not-on"));
        if (divides && !guarded) out.add(new Finding(Finding.Kind.NE_04, where(s.origin()), in + s.verb() + " with no ON SIZE ERROR"));
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) division(t, in, out);
    }

    private static boolean divides(Value v) {
        return v instanceof Value.Expression e && (e.operator().contains("/") || e.operands().stream().anyMatch(NonExact::divides));
    }

    private static String range(List<Procedure.Paragraph> paragraphs, Procedure.Edge e) {
        return e.to() == e.thru() ? name(paragraphs, e.to()) : name(paragraphs, e.to()) + " THRU " + name(paragraphs, e.thru());
    }

    private static String name(List<Procedure.Paragraph> paragraphs, int index) {
        Procedure.Paragraph g = paragraphs.get(index);
        return g.name().isEmpty() ? "(paragraph " + index + (g.section().isEmpty() ? "" : " of " + g.section()) + ")" : g.name();
    }

    private static String where(Origin o) {
        return o.file() + ":" + o.line() + ":" + o.column();
    }
}
