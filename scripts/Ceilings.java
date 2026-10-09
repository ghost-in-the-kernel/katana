import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * R-054's ceilings (amended 2026-10-09, J-163), held on every push: the warm fast loop and the pre-push
 * check's own time against the ceilings `docs/AGENT.md` gives them, the check over the suites a push
 * selects or, with `--all`, over every suite. A time past its ceiling refuses the push, as the tithe
 * gate does; a reported number nobody read doubled between passes (14 s to 35 s).
 */
// Decision: R-054
final class Ceilings {
    static final String FAST = "Fast loop (s)", SELECTED = "Pre-push check, the suites a push selects (min)", ALL = "Pre-push check, every suite (min)";
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    /** Each time past its ceiling, as the refusal says it; empty when the push may go. `check` in seconds. */
    static List<String> over(String agent, double fastSeconds, double checkSeconds, boolean all) {
        List<String> over = new ArrayList<>();
        double fast = ceiling(agent, FAST), check = ceiling(agent, all ? ALL : SELECTED);
        if (fastSeconds > fast) over.add("the warm fast loop took " + (int) fastSeconds + " s, past its ceiling of " + fmt(fast) + " s");
        if (checkSeconds / 60 > check) over.add("the check took " + fmt(checkSeconds / 60) + " min, past its ceiling of " + fmt(check) + " min (" + (all ? "--all" : "the selected suites") + ")");
        return over;
    }

    /** A metric's ceiling, the first number of its target cell in docs/AGENT.md's table. */
    static double ceiling(String agent, String metric) {
        for (String line : agent.split("\n")) {
            String[] cells = line.split("\\|");
            if (cells.length < 4 || !cells[1].trim().equals(metric)) continue;
            Matcher m = NUMBER.matcher(cells[2]);
            if (m.find()) return Double.parseDouble(m.group());
        }
        throw new IllegalStateException("docs/AGENT.md gives `" + metric + "` no ceiling");
    }

    private static String fmt(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.format("%.1f", d);
    }

    /** `java scripts/Ceilings.java <root> <warm fast loop s> <check s> [--all]`: exit 1 when a time is past its ceiling. */
    public static void main(String[] args) throws Exception {
        List<String> over = over(Files.readString(Path.of(args[0]).resolve("docs/AGENT.md")), Double.parseDouble(args[1]), Double.parseDouble(args[2]), args.length > 3 && args[3].equals("--all"));
        if (over.isEmpty()) return;
        over.forEach(o -> System.err.println("pre-push check: refused, " + o + " (R-054)"));
        System.exit(1);
    }
}
