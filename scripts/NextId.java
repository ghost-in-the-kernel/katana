import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The next free decision, journal entry and research section (J-232): ids were taken by eye, and two sessions took
 * the same R-number (R-069, J-219). Each is one past the highest in the working tree and in `origin/main` as last
 * fetched; fetch main first (R-040), since a number another session pushed since is not seen otherwise.
 * `gradle nextId`, or `java scripts/NextId.java <root>`.
 */
final class NextId {
    /** Each id, its file and the heading that takes one. */
    static final Map<String, String[]> IDS = Map.of(
            "R", new String[] {"theGhost.md", "(?m)^### R-(\\d+)"},
            "J", new String[] {JournalIndex.DIR, "(?m)^### J-(\\d+)"},
            "§", new String[] {"docs/research/ibm-options.md", "(?m)^## (\\d+)\\. "});

    private NextId() {}

    /** The highest id the text takes, 0 for none. */
    static int highest(String text, String heading) {
        Matcher m = Pattern.compile(heading).matcher(text);
        int max = 0;
        while (m.find()) max = Math.max(max, Integer.parseInt(m.group(1)));
        return max;
    }

    /** A file in the working tree; the journal, every part of it (J-244). */
    static String here(Path root, String file) throws IOException {
        return file.equals(JournalIndex.DIR) ? JournalIndex.text(root) : Files.readString(root.resolve(file));
    }

    /** A file as `origin/main` holds it, "" when it cannot be read; the journal, its parts there and the one file it was before J-244. */
    static String onMain(Path root, String file) throws IOException, InterruptedException {
        if (file.equals(JournalIndex.DIR)) {
            StringBuilder all = new StringBuilder(onMain(root, "docs/JOURNAL.md"));
            for (String p : JournalIndex.parts(root)) all.append(onMain(root, file + "/" + p));
            return all.toString();
        }
        Process git = new ProcessBuilder("git", "show", "origin/main:" + file).directory(root.toFile()).start();
        String text = new String(git.getInputStream().readAllBytes());
        return git.waitFor() == 0 ? text : "";
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : System.getProperty("katana.root", "."));
        for (String id : new String[] {"R", "J", "§"}) {
            String[] at = IDS.get(id);
            int next = 1 + Math.max(highest(here(root, at[0]), at[1]), highest(onMain(root, at[0]), at[1]));
            System.out.println(id.equals("§") ? "research §" + next : id + "-" + String.format("%03d", next));
        }
        System.out.println("(against origin/main as last fetched: `git fetch origin main` first)");
    }
}
