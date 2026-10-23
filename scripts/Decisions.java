import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * docs/decisions.md, theGhost.md's index (docs/ROADMAP.md §5): one line per decision, its number, its title and its
 * status, so an agent finds a decision without reading the 1,500-line log. `gradle decisions` writes it;
 * test/DocTests holds it fresh.
 */
final class Decisions {
    static final String PATH = "docs/decisions.md";
    private static final Pattern DECISION = Pattern.compile("(?m)^### (R-\\d+): (.+)$");
    private static final Pattern STATUS = Pattern.compile("(?m)^- \\*\\*Status:\\*\\* (.+)$");

    private Decisions() {}

    /** The index of the decisions in `ghost`, in their order. */
    static String render(String ghost) {
        StringBuilder out = new StringBuilder("# Decisions: the index\n\nWritten from `theGhost.md` by `gradle decisions`; do not edit by hand. A decision's"
                + " reasons, source and what holds it are in `theGhost.md` under its number.\n\n| Decision | Title | Status |\n| --- | --- | --- |\n");
        Matcher d = DECISION.matcher(ghost);
        while (d.find()) {
            int end = ghost.indexOf("\n---", d.end());
            Matcher s = STATUS.matcher(ghost.substring(d.end(), end < 0 ? ghost.length() : end));
            out.append("| ").append(d.group(1)).append(" | ").append(d.group(2).replace("|", "\\|")).append(" | ")
                    .append(s.find() ? s.group(1).trim() : "").append(" |\n");
        }
        return out.toString();
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : System.getProperty("katana.root", "."));
        Files.writeString(root.resolve(PATH), render(Files.readString(root.resolve("theGhost.md"))));
    }
}
