import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds theGhost.md (docs/ROADMAP.md 4.1, P-05): ids unique, in order, and with no gap that the
 * reserved list does not name; every decision has its fields and a known status; supersession named
 * on both sides (`superseded by R-n` and `Supersedes: R-m`); and no in-force decision carries a
 * `conflicts-with:` line naming another in-force decision.
 */
final class GhostTest {
    private static final Pattern HEADING = Pattern.compile("(?m)^### R-(\\d{3}): ");
    private static final Pattern FIELD = Pattern.compile("(?m)^- \\*\\*([A-Za-z -]+):\\*\\* (.*(?:\\n  .*)*)");
    private static final Pattern ID = Pattern.compile("R-(\\d{3})");
    private static final Pattern STATUS = Pattern.compile("proposed|in force|withdrawn|refused|superseded by R-\\d{3}");
    private static final List<String> REQUIRED = List.of("Decision", "Why", "Held by", "Source", "Status", "subject");

    record Decision(int id, Map<String, String> fields) {
        String field(String name) {
            return fields.getOrDefault(name, "");
        }

        List<String> statuses() {
            List<String> parts = new ArrayList<>();
            for (String part : field("Status").split(";")) parts.add(part.replaceAll("\\(.*?\\)", "").trim());
            return parts;
        }

        boolean inForce() {
            return statuses().contains("in force");
        }
    }

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        return new Check("ghost")
                .repository(check(Check.read("theGhost.md")))
                .rejects("a conflicting pair both in force", () -> check(PAIR_IN_CONFLICT))
                .accepts("the same pair once one is superseded", () -> check(PAIR_SUPERSEDED))
                .rejects("supersession named on one side only", () -> check(ONE_SIDED))
                .rejects("a duplicate id", () -> check(DUPLICATE))
                .rejects("a gap the reserved list does not name", () -> check(GAP));
    }

    static List<String> check(String text) {
        List<String> failures = new ArrayList<>();
        Map<Integer, Decision> byId = new LinkedHashMap<>();
        int previous = 0;
        for (Decision d : parse(text)) {
            if (byId.containsKey(d.id())) failures.add(name(d.id()) + " appears twice");
            else if (d.id() < previous) failures.add(name(d.id()) + " is out of order, after " + name(previous));
            byId.putIfAbsent(d.id(), d);
            previous = Math.max(previous, d.id());
            fieldsAndStatus(d, failures);
        }
        gaps(byId.keySet(), reserved(text), failures);
        for (Decision d : byId.values()) supersession(d, byId, failures);
        for (Decision d : byId.values()) conflicts(d, byId, failures);
        return failures;
    }

    private static List<Decision> parse(String text) {
        List<Decision> decisions = new ArrayList<>();
        Matcher heading = HEADING.matcher(text);
        List<int[]> starts = new ArrayList<>();
        while (heading.find()) starts.add(new int[] {heading.start(), Integer.parseInt(heading.group(1))});
        for (int i = 0; i < starts.size(); i++) {
            int end = i + 1 < starts.size() ? starts.get(i + 1)[0] : text.length();
            String body = text.substring(starts.get(i)[0], end);
            int cut = body.indexOf("\n---");
            if (cut >= 0) body = body.substring(0, cut);
            Map<String, String> fields = new LinkedHashMap<>();
            Matcher field = FIELD.matcher(body);
            while (field.find()) fields.put(field.group(1), field.group(2).replaceAll("\\s+", " ").trim());
            decisions.add(new Decision(starts.get(i)[1], fields));
        }
        return decisions;
    }

    /** The ids the paragraph opening "Reserved" names: numbers held for a phase, not gaps. */
    private static Set<Integer> reserved(String text) {
        Set<Integer> ids = new TreeSet<>();
        int at = text.indexOf("\nReserved");
        if (at < 0) return ids;
        int end = text.indexOf("\n\n", at + 1);
        Matcher id = ID.matcher(text.substring(at, end < 0 ? text.length() : end));
        while (id.find()) ids.add(Integer.parseInt(id.group(1)));
        return ids;
    }

    private static void fieldsAndStatus(Decision d, List<String> failures) {
        for (String required : REQUIRED) {
            if (d.field(required).isEmpty()) failures.add(name(d.id()) + " has no " + required);
        }
        for (String status : d.statuses()) {
            if (!d.field("Status").isEmpty() && !STATUS.matcher(status).matches()) {
                failures.add(name(d.id()) + " has an unknown status: " + status);
            }
        }
    }

    private static void gaps(Set<Integer> recorded, Set<Integer> reserved, List<String> failures) {
        int max = recorded.stream().mapToInt(Integer::intValue).max().orElse(0);
        for (int id = 1; id <= max; id++) {
            if (!recorded.contains(id) && !reserved.contains(id)) failures.add(name(id) + " is missing and not reserved");
            if (recorded.contains(id) && reserved.contains(id)) failures.add(name(id) + " is recorded and still listed as reserved");
        }
    }

    private static void supersession(Decision d, Map<Integer, Decision> byId, List<String> failures) {
        for (String status : d.statuses()) {
            Matcher by = ID.matcher(status);
            if (!status.startsWith("superseded") || !by.find()) continue;
            Decision successor = byId.get(Integer.parseInt(by.group(1)));
            if (successor == null) failures.add(name(d.id()) + " is superseded by " + by.group() + ", which is not recorded");
            else if (!ids(successor.field("Supersedes")).contains(d.id())) {
                failures.add(name(successor.id()) + " supersedes " + name(d.id()) + " but has no Supersedes line naming it");
            }
        }
        for (int old : ids(d.field("Supersedes"))) {
            Decision predecessor = byId.get(old);
            if (predecessor == null) failures.add(name(d.id()) + " supersedes " + name(old) + ", which is not recorded");
            else if (!predecessor.statuses().contains("superseded by " + name(d.id()))) {
                failures.add(name(old) + " is superseded by " + name(d.id()) + " but its status does not say so");
            }
        }
    }

    private static void conflicts(Decision d, Map<Integer, Decision> byId, List<String> failures) {
        for (int other : ids(d.field("conflicts-with"))) {
            Decision target = byId.get(other);
            if (target == null) failures.add(name(d.id()) + " conflicts with " + name(other) + ", which is not recorded");
            else if (d.inForce() && target.inForce()) {
                failures.add(name(d.id()) + " and " + name(other) + " are both in force and conflict: supersede one");
            }
        }
    }

    private static Set<Integer> ids(String text) {
        Set<Integer> ids = new TreeSet<>();
        Matcher id = ID.matcher(text);
        while (id.find()) ids.add(Integer.parseInt(id.group(1)));
        return ids;
    }

    private static String name(int id) {
        return "R-%03d".formatted(id);
    }

    private static String decision(int id, String status, String extra) {
        return """
                ### R-%03d: a decision
                - **Decision:** something.
                - **Why:** a reason.
                - **Held by:** a test.
                - **Source:** Ghost.
                - **Status:** %s
                - **subject:** rounding
                %s
                """.formatted(id, status, extra);
    }

    private static final String PAIR_IN_CONFLICT =
            decision(1, "in force", "") + decision(2, "in force", "- **conflicts-with:** R-001");
    private static final String PAIR_SUPERSEDED = decision(1, "superseded by R-002", "")
            + decision(2, "in force", "- **conflicts-with:** R-001\n- **Supersedes:** R-001");
    private static final String ONE_SIDED = decision(1, "superseded by R-002", "") + decision(2, "in force", "");
    private static final String DUPLICATE = decision(1, "in force", "") + decision(1, "in force", "");
    private static final String GAP = decision(1, "in force", "") + decision(3, "in force", "")
            + "\nReserved, to be recorded later: R-004.\n";
}
