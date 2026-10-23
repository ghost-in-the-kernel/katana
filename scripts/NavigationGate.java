import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * R-079's gate: docs/NAVIGATION.md, the agents' debriefs on finding their way, is reviewed about every ten
 * commits. The file's line `Reviewed through: <commit>` names the last review; `gradle metrics` warns past
 * REVIEW commits since, and the gate refuses a push past LIMIT, as R-008's does for a tithe (J-070: a rule held by
 * habit lapses). A push whose head is a review (titled "Navigation review") always goes.
 */
// Decision: R-079
final class NavigationGate {
    static final String PATH = "docs/NAVIGATION.md";
    static final int REVIEW = 10;
    static final int LIMIT = 20;
    private static final Pattern REVIEWED = Pattern.compile("(?m)^Reviewed through: ([0-9a-f]{7,40})");

    /** Commits since the reviewed-through commit, in a history newest first (`sha subject` each); -1 when it is not there. */
    static int since(List<String> log, String navigation) {
        Matcher m = REVIEWED.matcher(navigation);
        if (!m.find()) return -1;
        for (int i = 0; i < log.size(); i++) if (log.get(i).startsWith(m.group(1))) return i;
        return -1;
    }

    /** Null when the push may go, else why not. */
    static String verdict(List<String> log, String navigation) {
        if (!log.isEmpty() && log.get(0).split(" ", 2).length > 1 && log.get(0).split(" ", 2)[1].startsWith("Navigation review")) return null;
        int since = since(log, navigation);
        if (since < 0) return PATH + " names no `Reviewed through: <commit>` in the recent history (R-079)";
        if (since <= LIMIT) return null;
        return since + " commits since the last navigation review, past R-079's " + LIMIT + ": the next push is a commit titled \"Navigation review\"";
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : ".");
        Process git = new ProcessBuilder("git", "log", "--format=%H %s", "-n", "400").directory(root.toFile()).redirectErrorStream(true).start();
        List<String> log = new String(git.getInputStream().readAllBytes()).lines().toList();
        if (git.waitFor() != 0) throw new IllegalStateException("git log failed: " + log);
        String refused = verdict(log, Files.readString(root.resolve(PATH)));
        if (refused == null) return;
        System.err.println("pre-push check: refused, " + refused);
        System.exit(1);
    }
}
