package katana.oracle.nist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * What changes when the NIST run moves from one dialect to another (1.2): the runs whose outcome
 * differs between two goldens, each named in a change record with its cause from a fixed list. A
 * cause the profile must model carries the failures it was measured to produce (the cobol85 run
 * with that one GnuCOBOL flag added); their sum must be the run's added failures.
 */
public final class DialectChanges {
    /** Causes the profile must model, each a GnuCOBOL flag the IBM dialect sets (P-12). */
    public static final Map<String, String> PROFILE = Map.of(
            "arithmetic-osvs", "-farithmetic-osvs",
            "binary-truncation", "-fno-binary-truncate",
            "section-segments", "-fsection-segments=ignore");
    /** Causes that are the test's or the run's, not the profile's. */
    public static final Set<String> OTHER = Set.of("reserved-word", "follows");

    /** One line: a program's run, its causes with the failures each produced, and what it follows. */
    public record Change(String program, int run, Map<String, Integer> causes, String follows) {}

    private DialectChanges() {}

    /** The table rows: | Program | Run | Causes | Follows | Why |; a cause is "name" or "name=failures". */
    public static List<Change> read(String markdown) {
        List<Change> changes = new ArrayList<>();
        for (String row : markdown.lines().toList()) {
            if (!row.startsWith("| ") || row.startsWith("| Program") || row.startsWith("| ---")) continue;
            String[] c = row.split("\\|");
            Map<String, Integer> causes = new LinkedHashMap<>();
            for (String cause : c[3].replace("`", "").trim().split("\\s*,\\s*")) {
                String[] kv = cause.split("=");
                causes.put(kv[0], kv.length == 2 ? Integer.parseInt(kv[1]) : null);
            }
            String follows = c[4].trim();
            changes.add(new Change(c[1].trim(), Integer.parseInt(c[2].trim()), causes, follows.equals("-") ? null : follows));
        }
        return changes;
    }

    /** What is unexplained between two goldens; empty when the record names every change and its cause holds. */
    public static List<String> check(List<Reconciliation.Run> from, List<Reconciliation.Run> to, List<Change> changes) {
        List<String> problems = new ArrayList<>();
        Map<String, Reconciliation.Run> before = index(from), after = index(to);
        Set<String> differ = new TreeSet<>();
        for (String key : before.keySet()) if (!same(before.get(key), after.get(key))) differ.add(key);
        for (String key : after.keySet()) if (!before.containsKey(key)) differ.add(key);
        Set<String> recorded = new TreeSet<>();
        for (Change c : changes) {
            String key = c.program() + " " + c.run();
            recorded.add(key);
            if (!differ.contains(key)) problems.add(key + " is recorded as changed but is the same in both runs");
            problems.addAll(causes(key, c, before.get(key), after));
        }
        for (String key : differ) if (!recorded.contains(key)) problems.add(key + " changed and is not recorded");
        return problems;
    }

    /** Whether a change's causes hold: known, measured and adding up, a stopped compile, a broken chain. */
    private static List<String> causes(String key, Change c, Reconciliation.Run a, Map<String, Reconciliation.Run> after) {
        List<String> problems = new ArrayList<>();
        Reconciliation.Run b = after.get(key);
        Integer measured = measured(key, c, problems);
        if (measured != null && a != null && b != null && b.fail() - a.fail() != measured) {
            problems.add(key + ": its causes account for " + measured + " failures, the run added " + (b.fail() - a.fail()));
        }
        if (c.causes().containsKey("reserved-word") && (b == null || !b.outcome().equals("compile"))) {
            problems.add(key + ": a reserved word should stop the compile");
        }
        if (c.causes().containsKey("follows")) {
            Reconciliation.Run cause = c.follows() == null ? null : after.get(c.follows() + " 1");
            if (cause == null || !cause.outcome().equals("compile")) problems.add(key + " follows " + c.follows() + ", which compiles under the dialect");
        }
        return problems;
    }

    /** The failures a change's profile causes were measured to produce, or null when it names none. */
    private static Integer measured(String key, Change c, List<String> problems) {
        Integer sum = null;
        for (var cause : c.causes().entrySet()) {
            if (OTHER.contains(cause.getKey())) continue;
            if (!PROFILE.containsKey(cause.getKey())) problems.add(key + ": unknown cause " + cause.getKey());
            else if (cause.getValue() == null) problems.add(key + ": " + cause.getKey() + " needs its measured failures");
            else sum = (sum == null ? 0 : sum) + cause.getValue();
        }
        return sum;
    }

    private static Map<String, Reconciliation.Run> index(List<Reconciliation.Run> runs) {
        Map<String, Reconciliation.Run> byKey = new LinkedHashMap<>();
        for (Reconciliation.Run r : runs) byKey.put(r.program() + " " + r.run(), r);
        return byKey;
    }

    private static boolean same(Reconciliation.Run a, Reconciliation.Run b) {
        return b != null && a.outcome().equals(b.outcome()) && a.total() == b.total() && a.pass() == b.pass()
                && a.fail() == b.fail() && a.deleted() == b.deleted() && a.inspect() == b.inspect();
    }
}
