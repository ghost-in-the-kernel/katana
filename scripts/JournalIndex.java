import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The journal's parts and its index (J-244): entries live in docs/journal/, a hundred to a file, and
 * docs/journal/INDEX.md lists each by number, title, class and file. `gradle journalIndex` writes the index;
 * test/DocTests holds it fresh. Readers of the journal take `text`, every part in order.
 */
final class JournalIndex {
    static final String DIR = "docs/journal";
    static final String PATH = DIR + "/INDEX.md";
    private static final Pattern ENTRY = Pattern.compile("(?m)^### (J-\\d+) (.+)$");
    private static final Pattern CLASS = Pattern.compile("\\*\\*Class:\\*\\* ([^*\\n]+)");

    private JournalIndex() {}

    /** The parts, `J-001-099.md` and on, in order. */
    static List<String> parts(Path root) {
        try (Stream<Path> s = Files.list(root.resolve(DIR))) {
            return s.map(p -> p.getFileName().toString()).filter(n -> n.matches("J-\\d{3}-\\d{3}\\.md")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Every entry, the parts joined in order. */
    static String text(Path root) {
        StringBuilder all = new StringBuilder();
        try {
            for (String p : parts(root)) all.append(Files.readString(root.resolve(DIR).resolve(p))).append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return all.toString();
    }

    /** The index: one row per entry. */
    static String render(Path root) {
        StringBuilder out = new StringBuilder("# Journal: the index\n\nWritten from `docs/journal/` by `gradle journalIndex`; do not edit by hand."
                + " The fields and the rules are in `docs/JOURNAL.md` (R-046).\n\n| Entry | Title | Class | File |\n| --- | --- | --- | --- |\n");
        try {
            for (String p : parts(root)) {
                String part = Files.readString(root.resolve(DIR).resolve(p));
                Matcher e = ENTRY.matcher(part);
                while (e.find()) {
                    int end = part.indexOf("\n### ", e.end());
                    Matcher c = CLASS.matcher(part.substring(e.end(), end < 0 ? part.length() : end));
                    out.append("| ").append(e.group(1)).append(" | ").append(e.group(2).replace("|", "\\|")).append(" | ")
                            .append(c.find() ? kind(c.group(1)) : "").append(" | `").append(p).append("` |\n");
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return out.toString();
    }

    /** An entry's class, to the end of its sentence: "build (3.1)." is "build (3.1)". */
    private static String kind(String cell) {
        String k = cell.split("\\. ")[0].trim();
        return k.endsWith(".") ? k.substring(0, k.length() - 1) : k;
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : System.getProperty("katana.root", "."));
        Files.writeString(root.resolve(PATH), render(root));
    }
}
