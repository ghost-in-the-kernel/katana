package katana.lower;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.sir.types.Call;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * CALL and CANCEL (LANE-19, research §24): a program-name a literal gives is resolved here, a nested
 * program it can see first (LR 6.4 p. 87), else a separately compiled one through the run unit (static
 * under NODYNAM); one an identifier gives is resolved when the statement runs (`byName`, `cancelName`).
 */
final class CallVerbs {
    private final Flow f;

    CallVerbs(Flow f) {
        this.f = f;
    }

    /**
     * CALL: the arguments (BY REFERENCE the item itself, BY CONTENT a copy, OMITTED none); the called
     * program run; ON EXCEPTION (or OVERFLOW) when it cannot be found, else an abend, and NOT ON
     * EXCEPTION after it returns (LR 6.4 pp. 323-324). LE's CEE3ABD ends the run with an abend of its
     * first argument (LE 3.1 p. 111, research §22; R-027).
     */
    // Decision: R-062
    // Unsure: CEE3ABD's clean-up 0 performs no termination activity, so IBM may leave a QSAM file's last buffer unwritten, where the lane closes every file at an abend (LANE-08). Stays: no CardDemo or NIST run reaches CEE3ABD; a mainframe run with a QSAM file open at CEE3ABD settles it.
    void call(Statement s) {
        Call c = f.p.calls().stream().filter(x -> x.origin().equals(s.origin())).findFirst().orElse(null);
        if (c == null) {
            f.e.line(f.x.unsupported("CALL at " + s.origin() + " " + s.operands()) + ";");
            return;
        }
        if (c.target() instanceof Value.Literal l && l.payload().equals("CEE3ABD") && !c.using().isEmpty()) {
            f.e.line("if (true) throw new Abend((int) " + f.x.index(c.using().get(0).value()) + ", \"CALL CEE3ABD\");");
            return;
        }
        if (c.returning() != null) {
            f.e.line(f.x.unsupported("CALL ... RETURNING at " + s.origin()) + ";");
            return;
        }
        String args = "new Field[] {" + String.join(", ", arguments(c)) + "}";
        String found, name;
        if (c.target() instanceof Value.Literal l) {
            name = Emitter.quote(l.payload());
            String owner = Programs.visible(f.p, f.containers).get(l.payload());
            found = owner != null ? owner + "c_" + Lower.className(l.payload()) + "(" + args + ")" : "u.call(" + name + ", true, " + args + ")";
        } else if (c.target() instanceof Value.Field t) {
            name = "u.name(" + f.x.field(t) + ")";
            found = "byName(" + name + ", " + args + ")";
        } else {
            f.e.line(f.x.unsupported("CALL of " + c.target() + " at " + s.origin()) + ";");
            return;
        }
        List<Statement> exception = new ArrayList<>(Verbs.branch(s, "on-exception-clause"));
        exception.addAll(Verbs.branch(s, "on-overflow-phrase"));
        boolean phrase = s.branches().stream().anyMatch(b -> b.label().equals("on-exception-clause") || b.label().equals("on-overflow-phrase"));
        if (phrase) {
            f.e.line("cx = !" + found + ";");
            f.branch("cx", exception, Verbs.branch(s, "not-on-exception-clause"));
        } else {
            f.e.line("u.found(" + found + ", " + name + ");");
            f.statements(Verbs.branch(s, "not-on-exception-clause"));
        }
    }

    /** CANCEL: each program named, a nested one it can see by a literal's name, else the run unit's dynamic copy (LR 6.4 pp. 326-327). */
    // Decision: R-062
    void cancel(Statement s) {
        Map<String, String> visible = Programs.visible(f.p, f.containers);
        for (Value v : Verbs.operands(s, "cancel-call")) {
            if (v instanceof Value.Literal l) {
                String owner = visible.get(l.payload());
                f.e.line(owner != null ? owner + "k_" + Lower.className(l.payload()) + "();" : "u.cancel(" + Emitter.quote(l.payload()) + ");");
            } else if (v instanceof Value.Field t) f.e.line("cancelName(u.name(" + f.x.field(t) + "));");
            else f.e.line(f.x.unsupported("CANCEL of " + v + " at " + s.origin()) + ";");
        }
    }

    /** Each argument as a Field: BY REFERENCE the item, BY CONTENT a copy made now, a literal its own storage, OMITTED null. */
    private List<String> arguments(Call c) {
        List<String> out = new ArrayList<>();
        for (Call.Argument a : c.using()) {
            Value v = a.value();
            if (v instanceof Value.Name n && n.kind().equals("keyword") && n.text().equals("OMITTED")) out.add("null");
            else if (a.mode() == Call.Mode.VALUE) out.add(f.x.unsupported("CALL ... BY VALUE " + v + " at " + c.origin()));
            else if (v instanceof Value.Field fld) out.add(a.mode() == Call.Mode.CONTENT ? "RunUnit.content(" + f.x.field(fld) + ")" : f.x.field(fld));
            else if (v instanceof Value.Literal l && !l.numeric()) out.add(f.x.sending(l));
            else out.add(f.x.unsupported("CALL argument " + v + " at " + c.origin()));
        }
        return out;
    }
}
