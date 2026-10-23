import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds TODO.md and docs/DONE.md (docs/ROADMAP.md 4.1): items in numeric order under their phase,
 * each with a check, the first open item's check a real one, and no number both open and done. A lettered
 * phase (`## Phase 8A`, R-072) is inserted into the chain: its first line `Runs after: 7.1.` names the item it
 * follows, and its items come between that item and the next.
 */
final class TodoTest {
    private static final Pattern PHASE = Pattern.compile("(?m)^## Phase (\\d+[A-Z]?)");
    private static final Pattern ITEM = Pattern.compile("(?m)^### (\\d+[A-Z]?)\\.(\\d+) ");
    private static final Pattern AFTER = Pattern.compile("(?m)^Runs after: (\\d+)\\.(\\d+)\\.");
    private static final String NO_CHECK = "none in the roadmap";

    /** An item: its phase as written (`7`, or a lettered one, `8A`), its step, its body, and its place in the chain, `{phase, step, sub}`. */
    record Item(String phase, int step, String body, int[] place) {
        String number() {
            return phase + "." + step;
        }

        String check() {
            int at = body.indexOf("\nCheck: ");
            return at < 0 ? "" : body.substring(at + 8);
        }
    }

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        return new Check("todo")
                .repository(check(Check.read("TODO.md"), Check.read("docs/DONE.md")))
                .repository(statuses(Check.read("TODO.md")))
                .rejects("an item with no Status line", () -> statuses("### 3.1 A\n\nCheck: x\n"))
                .rejects("a status in no known word", () -> statuses("### 3.1 A\n\nStatus: nearly.\n"))
                .accepts("an item with a status", () -> statuses("### 3.1 A\n\nStatus: partly built (J-001); open: y.\n"))
                .rejects("items out of order", () -> check(OUT_OF_ORDER, ""))
                .accepts("a lettered phase after the item it names", () -> check(INSERTED, ""))
                .rejects("a lettered phase placed before the item it names", () -> check(INSERTED_LATE, ""))
                .rejects("a first open item with no check", () -> check(FIRST_UNCHECKED, ""))
                .rejects("an item both open and done", () -> check(SOUND, "### 0.3 The container\n"))
                .accepts("a sound list", () -> check(SOUND, "### 0.1 Repo\n"));
    }

    /** Each item's state at a glance (J-244): a `Status:` line opening with one of these. */
    static final List<String> STATES = List.of("open", "partly built", "built ahead", "waits on");

    /** The items with no `Status:` line, or one that opens with none of `STATES`. */
    static List<String> statuses(String todo) {
        List<String> failures = new ArrayList<>();
        String[] parts = todo.split("(?m)^(?=### )");
        for (String part : parts) {
            if (!part.startsWith("### ")) continue;
            String number = part.substring(4).split(" ")[0];
            java.util.regex.Matcher st = Pattern.compile("(?m)^Status: (.*)$").matcher(part);
            if (!st.find()) failures.add(number + " has no Status line");
            else if (STATES.stream().noneMatch(st.group(1)::startsWith)) failures.add(number + "'s status opens with none of " + STATES);
        }
        return failures;
    }

    /** What TODO.md gets wrong: items out of order, an item with no Check line, a first open item whose check is not written, or one also in DONE.md. */
    static List<String> check(String todo, String done) {
        List<String> failures = new ArrayList<>();
        List<Item> items = items(todo, failures);
        for (int i = 1; i < items.size(); i++) {
            Item a = items.get(i - 1), b = items.get(i);
            if (java.util.Arrays.compare(a.place(), b.place()) >= 0) {
                failures.add(b.number() + " comes after " + a.number());
            }
        }
        for (Item item : items) {
            if (item.check().isBlank()) failures.add(item.number() + " has no Check line");
        }
        if (!items.isEmpty() && items.get(0).check().contains(NO_CHECK)) {
            failures.add("the first open item, " + items.get(0).number() + ", has no check written yet");
        }
        Set<String> open = new TreeSet<>();
        items.forEach(item -> open.add(item.number()));
        Matcher doneItem = ITEM.matcher(done);
        while (doneItem.find()) {
            String number = doneItem.group(1) + "." + doneItem.group(2);
            if (open.contains(number)) failures.add(number + " is both in TODO.md and in docs/DONE.md");
        }
        return failures;
    }

    /** The items in file order, each with its body; an item outside its phase's heading is a failure. */
    private static List<Item> items(String todo, List<String> failures) {
        List<Item> items = new ArrayList<>();
        Matcher item = ITEM.matcher(todo);
        List<Integer> starts = new ArrayList<>();
        while (item.find()) starts.add(item.start());
        for (int i = 0; i < starts.size(); i++) {
            int end = i + 1 < starts.size() ? starts.get(i + 1) : todo.length();
            String body = todo.substring(starts.get(i), end);
            Matcher m = ITEM.matcher(body);
            m.find();
            String phase = m.group(1);
            int step = Integer.parseInt(m.group(2));
            int[] place = {Integer.parseInt(phase.replaceAll("[A-Z]", "")), step, 0};
            if (!phase.matches("\\d+")) {
                Matcher after = AFTER.matcher(phaseText(todo, phase));
                if (after.find()) place = new int[] {Integer.parseInt(after.group(1)), Integer.parseInt(after.group(2)), step};
                else failures.add("Phase " + phase + " says no `Runs after:` item");
            }
            Item parsed = new Item(phase, step, body, place);
            String heading = phaseAbove(todo, starts.get(i));
            if (!heading.equals(parsed.phase())) failures.add(parsed.number() + " sits under Phase " + heading);
            items.add(parsed);
        }
        return items;
    }

    private static String phaseAbove(String todo, int at) {
        Matcher phase = PHASE.matcher(todo.substring(0, at));
        String last = "none";
        while (phase.find()) last = phase.group(1);
        return last;
    }

    /** A phase's text, from its heading to the next phase's. */
    private static String phaseText(String todo, String phase) {
        int at = todo.indexOf("## Phase " + phase + ":");
        if (at < 0) at = todo.indexOf("## Phase " + phase + "\n");
        if (at < 0) return "";
        Matcher next = PHASE.matcher(todo);
        int end = next.find(at + 3) ? next.start() : todo.length();
        return todo.substring(at, end);
    }

    private static final String SOUND = """
            ## Phase 0
            ### 0.3 The container
            Check: a clean clone builds offline.
            ### 0.6 Scripts
            Check: none in the roadmap; written here before the item starts.
            ## Phase 1
            ### 1.1 NIST
            Check: the summary diff is empty.
            """;
    private static final String OUT_OF_ORDER = SOUND.replace("0.6 Scripts", "0.2 Scripts");
    private static final String INSERTED = SOUND.replace("## Phase 1\n", "## Phase 1A\nRuns after: 0.6.\n### 1A.1 Inserted\nCheck: it is held.\n## Phase 0, continued\n### 0.7 Rest\nCheck: held.\n## Phase 1\n");
    private static final String INSERTED_LATE = INSERTED.replace("Runs after: 0.6.", "Runs after: 0.3.");
    private static final String FIRST_UNCHECKED = SOUND.replace("Check: a clean clone builds offline.",
            "Check: none in the roadmap; written here before the item starts.");
}
