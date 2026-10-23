import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * docs/research/INDEX.md, the research notes' contents (J-244): each file under docs/research/ by its title, and
 * each numbered section of `ibm-options.md` (§1 onward) by its heading, so a section is found without reading
 * the file. `gradle researchIndex` writes it; test/DocTests holds it fresh.
 */
final class ResearchIndex {
    static final String DIR = "docs/research";
    static final String PATH = DIR + "/INDEX.md";
    private static final Pattern TITLE = Pattern.compile("(?m)^# (.+)$");
    private static final Pattern SECTION = Pattern.compile("(?m)^## (\\d+)\\. (.+)$");

    private ResearchIndex() {}

    /** The notes, every `.md` under docs/research/ but the index, in path order. */
    static List<String> notes(Path root) {
        try (Stream<Path> s = Files.walk(root.resolve(DIR))) {
            return s.filter(p -> p.toString().endsWith(".md")).map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .filter(p -> !p.equals(PATH)).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The index: the files, then ibm-options.md's sections. */
    static String render(Path root) {
        StringBuilder out = new StringBuilder("# Research: the contents\n\nWritten by `gradle researchIndex`; do not edit by hand.\n\n## The notes\n\n");
        try {
            for (String n : notes(root)) {
                Matcher t = TITLE.matcher(Files.readString(root.resolve(n)));
                out.append("- `").append(n).append("`").append(t.find() ? ": " + t.group(1).trim() : "").append('\n');
            }
            out.append("\n## The sections of `docs/research/ibm-options.md`\n\n| § | Heading |\n| --- | --- |\n");
            Matcher s = SECTION.matcher(Files.readString(root.resolve(DIR + "/ibm-options.md")));
            while (s.find()) out.append("| ").append(s.group(1)).append(" | ").append(s.group(2).replace("|", "\\|")).append(" |\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : System.getProperty("katana.root", "."));
        Files.writeString(root.resolve(PATH), render(root));
    }
}
