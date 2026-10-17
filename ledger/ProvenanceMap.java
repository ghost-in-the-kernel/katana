package katana.ledger;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** The provenance map (docs/ROADMAP.md 3.2): for each Java symbol a build holds, the COBOL it stands on, the passes that made it, the facts that licensed them and the runs that proved them, from its live lines. */
public final class ProvenanceMap {
    private ProvenanceMap() {}

    /** One line a symbol, `symbol: node by pass[ on fact (state)] proven by run`, a symbol's lines joined by `; `. */
    public static String write(Ledger ledger) {
        StringBuilder out = new StringBuilder("# The provenance map: each Java symbol, what it stands on, from the proof ledger's live lines (docs/LEDGER.md).\n");
        for (Map.Entry<String, List<Entry>> s : new TreeMap<>(ledger.live()).entrySet()) {
            out.append(s.getKey()).append(": ");
            List<String> said = s.getValue().stream().map(e -> e.node() + " by " + e.pass() + (e.fact().equals("-") ? "" : " on " + e.fact() + " (" + e.state() + ")")
                    + (e.run().equals("-") ? ", not yet proven" : " proven by " + e.run())).toList();
            out.append(String.join("; ", said)).append('\n');
        }
        return out.toString();
    }
}
