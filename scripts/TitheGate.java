import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * R-008's gate (amended 2026-10-09, J-154): past sixteen commits since the last one titled "Tithe", a
 * push whose head is not a tithe is refused, unless theGhost.md records a deferral, a line `Tithe
 * deferred through <commit>`, naming a commit at or after which the count restarts. Merge commits are not
 * counted (J-254): R-040 merges every finished step at once, so they mark steps, not work. The metrics tool's
 * "one is due" warned seventy-three times and stopped nothing; this is its verdict given teeth.
 */
// Decision: R-008
final class TitheGate {
    static final int GRACE = 16;
    private static final Pattern DEFERRAL = Pattern.compile("Tithe deferred through ([0-9a-f]{7,40})");

    /**
     * The verdict on a history, newest first (`sha subject` each), and theGhost.md's text: null when the
     * push may go, else why not. Commits count from the newest tithe or deferred-through commit.
     */
    static String verdict(List<String> log, String ghost) {
        if (log.isEmpty() || subject(log.get(0)).startsWith("Tithe")) return null;
        List<String> deferred = DEFERRAL.matcher(ghost).results().map(m -> m.group(1)).toList();
        int since = 0;
        while (since < log.size() && !subject(log.get(since)).startsWith("Tithe") && !deferredAt(log.get(since), deferred)) since++;
        if (since <= GRACE) return null;
        return since + " commits since the last tithe, past R-008's " + GRACE + ": the next push is a commit titled \"Tithe\", or theGhost.md records `Tithe deferred through <commit>`";
    }

    private static boolean deferredAt(String line, List<String> deferred) {
        String sha = line.split(" ", 2)[0];
        return deferred.stream().anyMatch(sha::startsWith);
    }

    private static String subject(String line) {
        String[] parts = line.split(" ", 2);
        return parts.length < 2 ? "" : parts[1];
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : ".");
        Process git = new ProcessBuilder("git", "log", "--no-merges", "--format=%H %s", "-n", "400").directory(root.toFile()).redirectErrorStream(true).start();
        List<String> log = new String(git.getInputStream().readAllBytes()).lines().toList();
        if (git.waitFor() != 0) throw new IllegalStateException("git log failed: " + log);
        String refused = verdict(log, Files.readString(root.resolve("theGhost.md")));
        if (refused == null) return;
        System.err.println("pre-push check: refused, " + refused);
        System.exit(1);
    }
}
