import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Holds docs/ESCALATIONS.md (R-075): every row has a date, a replay (`yes` or `no`) and a known gap kind, and a kind
 * other than `none` logged three times or more, or a `conflict` once (R-077), is raised to Ghost under "Waiting on Ghost" in docs/HANDOFF.md.
 */
final class EscalationsTest {
    static final Set<String> KINDS = Set.of("missing-row", "missing-instruction", "not-read", "wrong-instruction", "no-replay", "none", "conflict");
    /** R-077: a conflict between agents goes to Ghost at its first row. */
    private static final int RAISE_CONFLICT = 1;
    private static final int RAISE = 3;

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        String row = "| 2026-10-23 | a task | no | no-replay | nothing replayed |\n";
        String log = "| Date | Task | Replay | Gap kind | What |\n| --- | --- | --- | --- | --- |\n";
        String waiting = "## Waiting on Ghost\n\n- Escalations, no-replay: three agents could not replay.\n\n## Waiting on a run\n";
        return new Check("escalations")
                .repository(check(Check.read("docs/ESCALATIONS.md"), Check.read("docs/HANDOFF.md")))
                .rejects("an unknown gap kind", () -> check(log + row.replace("no-replay", "lost"), ""))
                .rejects("a replay neither yes nor no", () -> check(log + row.replace("| no |", "| maybe |"), ""))
                .rejects("a kind three times, not raised", () -> check(log + row + row + row, "## Waiting on Ghost\n\n## Waiting on a run\n"))
                .accepts("a kind three times, raised", () -> check(log + row + row + row, waiting))
                .rejects("one conflict, not raised", () -> check(log + row.replace("| no-replay |", "| conflict |"), "## Waiting on Ghost\n\n## Waiting on a run\n"))
                .accepts("one conflict, raised", () -> check(log + row.replace("| no-replay |", "| conflict |"), "## Waiting on Ghost\n\n- Escalations, conflict: two tasks.\n\n## Waiting on a run\n"))
                .accepts("`none` three times", () -> check(log + (row + row + row).replace("| no-replay |", "| none |"), ""));
    }

    /** What the log gets wrong: a malformed row, or a kind at three or more not raised to Ghost. */
    static List<String> check(String log, String handoff) {
        List<String> failures = new ArrayList<>();
        Map<String, Integer> counts = new TreeMap<>();
        for (String line : log.split("\n")) {
            if (!line.startsWith("| ") || line.startsWith("| Date") || line.startsWith("| ---")) continue;
            String[] cells = line.split("\\|");
            if (cells.length < 6) {
                failures.add("a row with fewer than five cells: " + line);
                continue;
            }
            String date = cells[1].trim(), replay = cells[3].trim(), kind = cells[4].trim();
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) failures.add("a row whose date is not yyyy-mm-dd: " + line);
            if (!replay.equals("yes") && !replay.equals("no")) failures.add("a row whose replay is not yes or no: " + line);
            if (!KINDS.contains(kind)) failures.add("a row whose gap kind is not one of " + KINDS + ": " + line);
            else counts.merge(kind, 1, Integer::sum);
        }
        String waiting = waiting(handoff);
        counts.forEach((kind, n) -> {
            int raise = kind.equals("conflict") ? RAISE_CONFLICT : RAISE;
            if (!kind.equals("none") && n >= raise && !waiting.contains("Escalations, " + kind + ":"))
                failures.add("gap kind " + kind + " is logged " + n + " times and not raised under \"Waiting on Ghost\" in docs/HANDOFF.md (" + (raise == 1 ? "R-077" : "R-075") + ")");
        });
        return failures;
    }

    /** HANDOFF.md's "Waiting on Ghost" section. */
    private static String waiting(String handoff) {
        int start = handoff.indexOf("## Waiting on Ghost");
        if (start < 0) return "";
        int end = handoff.indexOf("\n## ", start + 1);
        return end < 0 ? handoff.substring(start) : handoff.substring(start, end);
    }
}
