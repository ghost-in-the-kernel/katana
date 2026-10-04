import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds docs/AGENT.md's metrics (docs/ROADMAP.md 5, R-008): a metric may improve and may grow within
 * its target, but past its target it may not get worse than the value recorded. The fast loop and
 * the skipped tests are measured after a run (`gradle metrics --args=--time`), so they are recorded
 * at each tithe pass and not held here.
 */
final class MetricsTest {
    private static final Pattern NUMBER = Pattern.compile("\\d[\\d,]*(?:\\.\\d+)?");
    private static final List<String> AFTER_A_RUN = List.of("Fast loop (s)", "Skipped tests reported on the fast loop");

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        Map<String, String> now;
        try {
            now = Metrics.measure(false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new Check("metrics")
                .repository(check(Check.read("docs/AGENT.md"), now))
                .rejects("worse than recorded, past the target", () -> check(table("0", "3"), Map.of("M", "5")))
                .accepts("worse than recorded, within the target", () -> check(table("< 400", "179"), Map.of("M", "250 (x.java)")))
                .rejects("past the target, never recorded", () -> check(table("< 400", ""), Map.of("M", "401")))
                .rejects("a metric the script does not measure", () -> check(table("0", "0"), Map.of()));
    }

    static List<String> check(String agent, Map<String, String> now) {
        List<String> failures = new ArrayList<>();
        for (String line : agent.split("\n")) {
            String[] cells = line.split("\\|");
            if (cells.length < 5 || line.startsWith("| Metric") || line.startsWith("| ---")) continue;
            String name = cells[1].trim();
            if (AFTER_A_RUN.contains(name)) continue;
            if (!now.containsKey(name)) {
                failures.add("`" + name + "` is not measured by scripts/Metrics.java");
                continue;
            }
            Double current = number(now.get(name));
            if (current == null || withinTarget(cells[2].trim(), current)) continue;
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
