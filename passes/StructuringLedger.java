package katana.passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.ledger.Entry;
import katana.ledger.Ledger;
import katana.lower.Lower;

/**
 * The passes' ledger lines (R-030, docs/LEDGER.md), structuring's and scalar promotion's: a line for each symbol the structured build
 * declares that the literal one did not, a range's method on the `Function` fact that licensed it with that
 * fact's state (R-029); and a removal superseding the live line of each symbol it no longer declares, a stretch
 * a PERFORM no longer splits off. Appended to the literal build's ledger, LED-04 holds against the structured build.
 */
public final class StructuringLedger {
    private StructuringLedger() {}

    // Decision: R-030
    public static List<Ledger.Draft> lines(Ledger literal, Lower.Result structured, Structuring.Result pass, String run, String profile, String time) {
        Map<String, Structuring.Applied> byMethod = new HashMap<>();
        for (Structuring.Applied a : pass.applied()) byMethod.put("perform" + a.first(), a);
        Map<String, List<Entry>> live = literal.live();
        Set<String> now = new HashSet<>();
        List<Ledger.Draft> out = new ArrayList<>();
        for (Lower.Symbol s : structured.symbols()) {
            now.add(s.name());
            if (live.containsKey(s.name())) continue;
            Structuring.Applied a = byMethod.get(s.name().substring(s.name().lastIndexOf('.') + 1));
            katana.intent.types.Fact licence = a != null ? a.fact() : typed(pass, s.node());
            String fact = licence == null ? "-" : licence.id().replace("|", "/"), state = licence == null ? "-" : licence.unconfirmed() ? "unconfirmed" : "confirmed";
            out.add(new Ledger.Draft(pass(s.node()), fact, state, run, s.name(), s.node(), profile, time, 0));
        }
        for (Map.Entry<String, List<Entry>> e : live.entrySet()) {
            if (now.contains(e.getKey())) continue;
            Entry last = e.getValue().get(e.getValue().size() - 1);
            out.add(new Ledger.Draft(pass(last.node()), "-", "-", run, "-", last.node(), profile, time, last.seq()));
        }
        return out;
    }

    /** The pass a symbol's change is the work of: a condition's name, 6.5's types; an item's, scalar promotion (6.3); any other, structuring. */
    private static String pass(String node) {
        return node.startsWith("condition ") ? Types.PASS : node.startsWith("item ") ? Scalars.PASS : Structuring.PASS;
    }

    /** The Enum, Flag or Money fact behind an item's or a condition's new name (6.5), whose state the line carries (R-029); null for none. */
    private static katana.intent.types.Fact typed(Structuring.Result pass, String node) {
        if (!node.startsWith("item ") && !node.startsWith("condition ")) return null;
        return pass.typed().get(Integer.parseInt(node.substring(node.indexOf(' ') + 1)));
    }
}
