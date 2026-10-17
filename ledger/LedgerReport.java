package katana.ledger;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** The ledger's part of the proof report: how many lines each pass wrote, the symbols live, those not yet proven, and each act on a fact no person confirmed (R-029). */
public final class LedgerReport {
    private LedgerReport() {}

    public static String write(Ledger ledger) {
        Map<String, Integer> byPass = new TreeMap<>();
        ledger.entries().forEach(e -> byPass.merge(e.pass(), 1, Integer::sum));
        Map<String, List<Entry>> live = ledger.live();
        long unproven = live.values().stream().filter(lines -> lines.stream().anyMatch(e -> e.run().equals("-"))).count();
        StringBuilder out = new StringBuilder("## The ledger\n\n");
        out.append(ledger.entries().size()).append(" lines; ").append(live.size()).append(" Java symbols live, ").append(unproven).append(" of them not yet proven by an oracle run.\n\n");
        out.append("| Pass | Lines |\n| --- | --- |\n");
        byPass.forEach((pass, n) -> out.append("| ").append(pass).append(" | ").append(n).append(" |\n"));
        List<Entry> unconfirmed = ledger.entries().stream().filter(e -> e.state().equals("unconfirmed")).toList();
        out.append('\n').append(unconfirmed.isEmpty() ? "No pass acted on a fact no person confirmed.\n" : "Acted on facts no person confirmed (R-029):\n");
        for (Entry e : unconfirmed) out.append("- line ").append(e.seq()).append(": ").append(e.pass()).append(" on ").append(e.fact()).append(", ").append(e.symbol()).append('\n');
        return out.toString();
    }
}
