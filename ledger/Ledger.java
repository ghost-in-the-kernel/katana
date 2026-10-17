package katana.ledger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** The proof ledger (R-030): lines only ever appended, each chained to the one before, read and checked whole. */
public record Ledger(List<Entry> entries) {
    /** What a pass or a build adds: a line before the ledger numbers and chains it. */
    public record Draft(String pass, String fact, String state, String run, String symbol, String node, String profile, String time, int supersedes) {}

    /** LED-01: lines numbered 1, 2, ... each carrying the hash of the one before; any line edited, removed, inserted or moved breaks it there. */
    // Decision: R-030
    public Ledger {
        entries = List.copyOf(entries);
        String prev = Entry.FIRST;
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.seq() != i + 1 || !e.prev().equals(prev)) throw new Invalid("LED-01", "line " + (i + 1) + " is not the line that followed line " + i);
            prev = e.hash();
        }
    }

    /** A ledger from its file, whose `end` line carries the last line's hash, so the last line is held as the others are. */
    public static Ledger read(String text) {
        List<Entry> entries = new ArrayList<>();
        String end = null;
        for (String line : text.lines().toList()) {
            if (line.isBlank() || line.startsWith("#")) continue;
            if (end != null) throw new Invalid("LED-01", "a line after the end");
            if (line.startsWith(END)) end = line.substring(END.length());
            else entries.add(Entry.read(line));
        }
        Ledger ledger = new Ledger(entries);
        if (!ledger.last().equals(end)) throw new Invalid("LED-01", "the end does not carry the last line's hash");
        return ledger;
    }

    private static final String END = "end" + Entry.SEP;

    private String last() {
        return entries.isEmpty() ? Entry.FIRST : entries.get(entries.size() - 1).hash();
    }

    /** The file. */
    public String text() {
        StringBuilder out = new StringBuilder("# The proof ledger (docs/LEDGER.md): append-only, each line chained to the one before (R-030).\n");
        for (Entry e : entries) out.append(e.text()).append('\n');
        return out.append(END).append(last()).append('\n').toString();
    }

    /** This ledger with lines appended, numbered and chained after its own; nothing it holds changes. */
    // Decision: R-030
    public Ledger append(List<Draft> drafts) {
        List<Entry> all = new ArrayList<>(entries);
        for (Draft d : drafts) {
            String prev = all.isEmpty() ? Entry.FIRST : all.get(all.size() - 1).hash();
            all.add(new Entry(all.size() + 1, prev, d.pass(), d.fact(), d.state(), d.run(), d.symbol(), d.node(), d.profile(), d.time(), d.supersedes()));
        }
        return new Ledger(all);
    }

    /** LED-01 again, for a ledger and the one it was: the earlier one's lines are the later's first, unchanged. */
    public static void appendedTo(Ledger earlier, Ledger later) {
        if (later.entries.size() < earlier.entries.size() || !later.entries.subList(0, earlier.entries.size()).equals(earlier.entries)) {
            throw new Invalid("LED-01", "the ledger's earlier lines changed");
        }
    }

    /** Each line no later line supersedes, by the symbol it names; a removal (`-`) names none. */
    public Map<String, List<Entry>> live() {
        Set<Integer> superseded = new TreeSet<>();
        for (Entry e : entries) if (e.supersedes() > 0) superseded.add(e.supersedes());
        Map<String, List<Entry>> live = new LinkedHashMap<>();
        for (Entry e : entries) if (!superseded.contains(e.seq()) && !e.symbol().equals("-")) live.computeIfAbsent(e.symbol(), s -> new ArrayList<>()).add(e);
        return live;
    }

    /** LED-04: against a build's symbols, every one has a live line, and every live line's symbol is still in the build (one a pass removed has a line superseding it). */
    public void against(Collection<String> symbols) {
        Map<String, List<Entry>> live = live();
        List<String> wrong = new ArrayList<>();
        for (String s : new TreeSet<>(symbols)) if (!live.containsKey(s)) wrong.add(s + " has no line");
        Set<String> built = new TreeSet<>(symbols);
        for (String s : live.keySet()) if (!built.contains(s)) wrong.add(s + " is gone from the build with no line superseding line " + live.get(s).get(live.get(s).size() - 1).seq());
        if (!wrong.isEmpty()) throw new Invalid("LED-04", String.join("; ", wrong.subList(0, Math.min(5, wrong.size()))) + (wrong.size() > 5 ? "; and " + (wrong.size() - 5) + " more" : ""));
    }
}
