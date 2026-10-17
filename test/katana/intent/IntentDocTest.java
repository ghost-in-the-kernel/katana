package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** docs/INTENT-IR.md and intent/types name the same types and the same invariants, as SirDocTest holds SEMANTIC-IR.md. */
class IntentDocTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Pattern DECLARED = Pattern.compile("(?m)^\\s*(?:public )?(?:static )?(?:final )?(sealed interface|record|enum|class|interface) (\\w+)");

    private static String doc() throws Exception {
        return Files.readString(ROOT.resolve("docs/INTENT-IR.md"));
    }

    private static String sources() throws Exception {
        StringBuilder all = new StringBuilder();
        try (Stream<Path> files = Files.list(ROOT.resolve("intent/types"))) {
            for (Path f : files.filter(f -> f.toString().endsWith(".java")).sorted().toList()) all.append(Files.readString(f));
        }
        return all.toString();
    }

    private static Set<String> all(Pattern pattern, int group, String text) {
        Set<String> found = new TreeSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) found.add(m.group(group));
        return found;
    }

    @Test
    void theDocumentNamesEveryTypeAndNoOther() throws Exception {
        Set<String> documented = all(Pattern.compile("`([A-Z][a-z][A-Za-z]*)`"), 1, doc());
        documented.removeIf(name -> name.endsWith("Test"));
        assertEquals(all(DECLARED, 2, sources()), documented);
    }

    @Test
    void theDocumentAndTheCodeNameTheSameInvariants() throws Exception {
        assertEquals(all(Pattern.compile("(?m)^- \\*\\*(INT-\\d\\d)"), 1, doc()), all(Pattern.compile("INT-\\d\\d"), 0, sources()));
    }

    /** R-007: the Intent IR is data; only Invalid, the refusal, is a class. */
    @Test
    void everyTypeIsARecordAnEnumOrASealedInterface() throws Exception {
        Set<String> other = new TreeSet<>();
        Matcher m = DECLARED.matcher(sources());
        while (m.find()) if (m.group(1).equals("interface") || (m.group(1).equals("class") && !m.group(2).equals("Invalid"))) other.add(m.group(2));
        assertEquals(Set.of(), other);
    }
}
