package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** The roadmap's `sir-doc.test`: docs/SEMANTIC-IR.md and sir/types name the same types and the same invariants. */
class SirDocTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Pattern DECLARED = Pattern.compile("(?m)^\\s*(?:public )?(?:static )?(?:final )?(sealed interface|record|enum|class|interface) (\\w+)");

    private static String doc() throws Exception {
        return Files.readString(ROOT.resolve("docs/SEMANTIC-IR.md"));
    }

    private static List<String> sources() throws Exception {
        try (Stream<Path> files = Files.list(ROOT.resolve("sir/types"))) {
            return files.filter(f -> f.toString().endsWith(".java")).sorted().map(f -> {
                try {
                    return Files.readString(f);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).toList();
        }
    }

    private static Set<String> all(Pattern pattern, int group, String text) {
        Set<String> found = new TreeSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) found.add(m.group(group));
        return found;
    }

    @Test
    void theDocumentNamesEveryTypeAndNoOther() throws Exception {
        Set<String> code = new TreeSet<>();
        for (String source : sources()) code.addAll(all(DECLARED, 2, source));
        Set<String> documented = all(Pattern.compile("`([A-Z][A-Za-z]+)`"), 1, doc());
        documented.removeIf(name -> name.endsWith("Test"));
        assertEquals(code, documented);
    }

    @Test
    void theDocumentAndTheCodeNameTheSameInvariants() throws Exception {
        Set<String> code = new TreeSet<>();
        for (String source : sources()) code.addAll(all(Pattern.compile("SIR-\\d\\d"), 0, source));
        assertEquals(all(Pattern.compile("(?m)^- \\*\\*(SIR-\\d\\d)"), 1, doc()), code);
    }

    /** R-007: the IR is data; only Invalid, the refusal, is a class. */
    @Test
    void everyTypeIsARecordAnEnumOrASealedInterface() throws Exception {
        Set<String> other = new TreeSet<>();
        for (String source : sources()) {
            Matcher m = DECLARED.matcher(source);
            while (m.find()) if (m.group(1).equals("interface") || (m.group(1).equals("class") && !m.group(2).equals("Invalid"))) other.add(m.group(2));
        }
        assertEquals(Set.of(), other);
    }
}
