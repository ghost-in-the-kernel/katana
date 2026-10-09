import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds docs/AGENT.md's metrics (docs/ROADMAP.md 5, R-008): a metric may improve and may grow within
 * its target, but past its target it may not get worse than the value recorded. A metric marked "not
 * armed" is reported, not compared, until it is first met or a phase boundary arms it at its value
 * (R-042, R-054). The warm fast loop and the pre-push check are measured on every push, where
 * `scripts/Ceilings.java` holds them (R-054 amended, J-163); here their recorded value is held to its
 * ceiling. The cold fast loop and the skipped tests are measured after a run and only reported.
 */
final class MetricsTest {
    private static final Pattern NUMBER = Pattern.compile("\\d[\\d,]*(?:\\.\\d+)?");
    private static final List<String> REPORTED = List.of("Fast loop, cold (s)", "Skipped tests reported on the fast loop");
    private static final List<String> HELD_TIMES = List.of("Fast loop (s)", "Pre-push check, the suites a push selects (min)", "Pre-push check, every suite (min)");

    public static void main(String[] args) {
        run().exit();
    }

    /** The measured table against docs/AGENT.md, and the cases that show the rule refuses and accepts what it should. */
    static Check run() {
        Map<String, String> now;
        try {
            now = Metrics.measure(false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        List<String> notes = new ArrayList<>();
        List<String> failures = check(Check.read("docs/AGENT.md"), now, notes);
        notes.forEach(note -> System.out.println("metrics: " + note));
        return new Check("metrics")
                .repository(failures)
                .rejects("worse than recorded, past the target", () -> check(table("0", "3"), Map.of("M", "5")))
                .accepts("worse than recorded, within the target", () -> check(table("< 400", "179"), Map.of("M", "250 (x.java)")))
                .rejects("past the target, never recorded", () -> check(table("< 400", ""), Map.of("M", "401")))
                .rejects("a metric the script does not measure", () -> check(table("0", "0"), Map.of()))
                .accepts("an unarmed metric worse than recorded", () -> check(table("< 0.35", "1.13 (not armed)"), Map.of("M", "1.50")))
                .rejects("an unarmed metric now within its target", () -> check(table("< 0.35", "1.13 (not armed)"), Map.of("M", "0.30")))
                .rejects("a recorded time past its ceiling", () -> check(table("≤ 25", "35").replace("| M |", "| Fast loop (s) |"), Map.of()))
                .accepts("a recorded time within its ceiling", () -> check(table("≤ 25", "17").replace("| M |", "| Fast loop (s) |"), Map.of()));
    }

    static List<String> check(String agent, Map<String, String> now) {
        return check(agent, now, new ArrayList<>());
    }

    /** The failures; each unarmed metric is added to notes instead of compared. */
    // Decision: R-042
    static List<String> check(String agent, Map<String, String> now, List<String> notes) {
        List<String> failures = new ArrayList<>();
        for (String line : agent.split("\n")) {
            String[] cells = line.split("\\|");
            if (cells.length < 5 || line.startsWith("| Metric") || line.startsWith("| ---")) continue;
            String name = cells[1].trim();
            if (REPORTED.contains(name)) continue;
            if (HELD_TIMES.contains(name)) {
                Double recorded = number(cells[3]);
                if (recorded == null || !withinTarget(cells[2].trim(), recorded)) failures.add("`" + name + "` is recorded at " + cells[3].trim() + ", past its ceiling " + cells[2].trim() + " (R-054 amended)");
                continue;
            }
            if (!now.containsKey(name)) {
                failures.add("`" + name + "` is not measured by scripts/Metrics.java");
                continue;
            }
            Double current = number(now.get(name));
            boolean met = current != null && withinTarget(cells[2].trim(), current);
            if (cells[3].contains("not armed")) {
                if (met) failures.add("`" + name + "` is " + now.get(name) + ", within its target: arm it (record the value, drop \"not armed\")");
                else notes.add("`" + name + "` is " + now.get(name) + ", not armed (R-042)");
                continue;
            }
            if (current == null || met) continue;
            Double recorded = number(cells[3]);
            if (recorded == null) failures.add("`" + name + "` is " + now.get(name) + ", past its target, and has no recorded value");
            else if (current > recorded) failures.add("`" + name + "` is " + now.get(name) + ", worse than the " + recorded + " recorded; fix it, or record why in a decision");
        }
        return failures;
    }

    /** "< 400" is strict; "≤ 5", "0" and "0 new" allow the number itself. */
    private static boolean withinTarget(String target, double value) {
        Double limit = number(target);
        if (limit == null) return false;
        return target.startsWith("<") ? value < limit : value <= limit;
    }

    private static Double number(String text) {
        Matcher m = NUMBER.matcher(text);
        return m.find() ? Double.valueOf(m.group().replace(",", "")) : null;
    }

    private static String table(String target, String measured) {
        return "| Metric | Phase 0 target | Measured | Why |\n| --- | --- | --- | --- |\n| M | " + target + " | " + measured + " | a reason |\n";
    }
}
