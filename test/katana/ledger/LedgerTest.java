package katana.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/** The roadmap's `ledger.test` (5.4): an edit to any line refused, every symbol a line, a removed symbol superseded; LED-01 to LED-04 each refusing a violation. */
class LedgerTest {
    private static final String HASH = "sha256:" + "0".repeat(64), TIME = "2026-10-17T00:00:00Z";

    private static Ledger.Draft literal(String symbol, String node) {
        return new Ledger.Draft("literal", "-", "-", "POSTTRAN.v1 abcd", symbol, node, HASH, TIME, 0);
    }

    private static Ledger built() {
        return new Ledger(List.of()).append(List.of(literal("P", "program P"), literal("P.f1", "item 1"), literal("P.f2", "item 2"), literal("P.s1", "paragraph 1")));
    }

    private static void refused(String invariant, Supplier<Object> build) {
        Invalid e = assertThrows(Invalid.class, build::get);
        assertTrue(e.getMessage().startsWith(invariant + ": "), e.getMessage());
    }

    @Test
    void anEditToAnyLineIsRefused() {
        String text = built().text();
        assertEquals(built(), Ledger.read(text));
        List<String> lines = new ArrayList<>(text.lines().filter(l -> !l.startsWith("#")).toList());
        String end = lines.remove(lines.size() - 1);
        for (int i = 0; i < lines.size(); i++) {
            List<String> edited = new ArrayList<>(lines);
            edited.set(i, lines.get(i).replace("literal", "literaL"));
            refused("LED-01", () -> Ledger.read(String.join("\n", edited) + "\n" + end));
        }
        List<String> removed = new ArrayList<>(lines);
        removed.remove(1);
        refused("LED-01", () -> Ledger.read(String.join("\n", removed) + "\n" + end));
        refused("LED-01", () -> Ledger.read(String.join("\n", lines)));
        List<String> swapped = new ArrayList<>(lines);
        swapped.set(1, lines.get(2));
        swapped.set(2, lines.get(1));
        refused("LED-01", () -> Ledger.read(String.join("\n", swapped) + "\n" + end));
        Ledger longer = built().append(List.of(literal("P.s2", "paragraph 2")));
        Ledger.appendedTo(built(), longer);
        refused("LED-01", () -> { Ledger.appendedTo(longer, built()); return null; });
    }

    @Test
    void everySymbolHasALineAndARemovedOneIsSuperseded() {
        Ledger ledger = built();
        ledger.against(List.of("P", "P.f1", "P.f2", "P.s1"));
        refused("LED-04", () -> { ledger.against(List.of("P", "P.f1", "P.f2", "P.s1", "P.s2")); return null; });
        refused("LED-04", () -> { ledger.against(List.of("P", "P.f1", "P.s1")); return null; });
        Ledger removed = ledger.append(List.of(new Ledger.Draft("scalar-promotion v1", "money item 2", "confirmed", "POSTTRAN.v1 ef01", "-", "item 2", HASH, TIME, 3)));
        removed.against(List.of("P", "P.f1", "P.s1"));
        Ledger replaced = ledger.append(List.of(new Ledger.Draft("scalar-promotion v1", "money item 2", "unconfirmed", "-", "P.amount", "item 2", HASH, TIME, 3)));
        replaced.against(List.of("P", "P.f1", "P.s1", "P.amount"));
        assertTrue(ProvenanceMap.write(replaced).contains("P.amount: item 2 by scalar-promotion v1 on money item 2 (unconfirmed), not yet proven\n"), ProvenanceMap.write(replaced));
        assertTrue(ProvenanceMap.write(replaced).contains("P.f1: item 1 by literal proven by POSTTRAN.v1 abcd\n"), ProvenanceMap.write(replaced));
        String report = LedgerReport.write(replaced);
        assertTrue(report.contains("5 lines; 4 Java symbols live, 1 of them not yet proven"), report);
        assertTrue(report.contains("- line 5: scalar-promotion v1 on money item 2, P.amount\n"), report);
    }

    @Test
    void eachInvariantRefusesAViolation() {
        refused("LED-02", () -> new Entry(1, Entry.FIRST, "literal", "-", "-", "-", "", "item 1", HASH, TIME, 0));
        refused("LED-02", () -> new Entry(1, Entry.FIRST, "literal", "-", "-", "-", "P.f1", "item 1", "sha256:nope", TIME, 0));
        refused("LED-02", () -> new Entry(1, Entry.FIRST, "p v1", "enum item 1 A,B", "refuted", "-", "P.f1", "item 1", HASH, TIME, 0));
        refused("LED-02", () -> new Entry(1, Entry.FIRST, "p v1", "-", "confirmed", "-", "P.f1", "item 1", HASH, TIME, 0));
        refused("LED-02", () -> new Entry(1, Entry.FIRST, "literal", "-", "-", "-", "-", "item 1", HASH, TIME, 0));
        refused("LED-02", () -> new Entry(1, Entry.FIRST, "literal", "-", "-", "-", "P.f1", "item 1", HASH, "today", 0));
        refused("LED-02", () -> new Entry(1, Entry.FIRST, "literal", "-", "-", "a | b", "P.f1", "item 1", HASH, TIME, 0));
        refused("LED-03", () -> new Entry(2, Entry.FIRST, "literal", "-", "-", "-", "P.f1", "item 1", HASH, TIME, 2));
        refused("LED-01", () -> new Ledger(List.of(new Entry(2, Entry.FIRST, "literal", "-", "-", "-", "P.f1", "item 1", HASH, TIME, 0))));
    }
}
