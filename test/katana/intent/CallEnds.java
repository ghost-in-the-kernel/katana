package katana.intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.sir.types.Call;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Value;

/** The other end of a CALL that shares an item (research §34): the CardDemo program it is handed to, or that hands it to this one, and its name there. */
final class CallEnds {
    /** One end: the program by name, its IR when it is one of CardDemo's COBOL programs (null when not), and the item there (null when none lies where this one does). */
    record End(String program, Program other, Item there, boolean callee, String holder) {}

    private CallEnds() {}

    /** The programs this one hands the item to by CALL BY REFERENCE, inside whatever group it hands. */
    static List<End> callees(Program p, int item, Corpus corpus) {
        List<End> out = new ArrayList<>();
        for (Call c : p.calls()) {
            for (int arg = 0; arg < c.using().size(); arg++) {
                Call.Argument a = c.using().get(arg);
                if (a.mode() != Call.Mode.REFERENCE || !(a.value() instanceof Value.Field f) || !EnumValuesStored.holds(p, f.item(), item)) continue;
                String name = c.target() instanceof Value.Literal l ? l.payload() : "a program named at run time";
                Program q = corpus.get(name);
                Item there = q == null || arg >= q.procedure().using().size() ? null : across(p, f.item(), item, q, q.procedure().using().get(arg));
                out.add(new End(name, q, there, true, f.item() == item ? "" : p.items().get(f.item() - 1).name()));
            }
        }
        return out;
    }

    /** The CardDemo programs that CALL this one and hand it what its USING parameter holding the item receives. */
    static List<End> callers(Program p, int item, Corpus corpus) {
        List<End> out = new ArrayList<>();
        for (int index = 0; index < p.procedure().using().size(); index++) {
            int param = p.procedure().using().get(index);
            if (!EnumValuesStored.holds(p, param, item)) continue;
            for (Program q : corpus.mentioning(p.header().program())) for (Call c : q.calls()) {
                if (!(c.target() instanceof Value.Literal l) || !l.payload().equals(p.header().program()) || index >= c.using().size()) continue;
                Item there = c.using().get(index).value() instanceof Value.Field f ? across(p, param, item, q, f.item()) : null;
                out.add(new End(q.header().program(), q, there, false, ""));
            }
        }
        return out;
    }

    /** The item of `q` at the same place in its group `outerQ` as `item` is in `outerP`: the same offset from the group's start, the same length. */
    private static Item across(Program p, int outerP, int item, Program q, int outerQ) {
        Item a = p.items().get(outerP - 1), i = p.items().get(item - 1), g = q.items().get(outerQ - 1);
        Item found = null;
        for (Item j : q.items()) {
            if (j.record() != g.record() || j.offset() - g.offset() != i.offset() - a.offset() || j.length() != i.length() || !EnumValuesStored.holds(q, g.id(), j.id())) continue;
            if (found == null || !j.conditions().isEmpty() || found.attributes().category() == katana.sir.types.Attributes.Category.GROUP) found = j;
        }
        return found;
    }
}
