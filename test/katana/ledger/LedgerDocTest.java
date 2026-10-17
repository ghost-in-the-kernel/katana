package katana.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** docs/LEDGER.md and ledger/ name the same line and the same invariants, as SirDocTest holds SEMANTIC-IR.md. */
class LedgerDocTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    private static String doc() throws Exception {
        return Files.readString(ROOT.resolve("docs/LEDGER.md"));
    }

    @Test
    void theLineIsTheEntrysFieldsInOrder() throws Exception {
        String fields = Arrays.stream(Entry.class.getRecordComponents()).map(c -> c.getName()).collect(Collectors.joining(" | "));
        assertEquals(true, doc().contains("`" + fields + "`"), fields);
    }

    @Test
    void theDocumentAndTheCodeNameTheSameInvariants() throws Exception {
        Set<String> code = new TreeSet<>();
        try (Stream<Path> files = Files.list(ROOT.resolve("ledger"))) {
            for (Path f : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher m = Pattern.compile("LED-\\d\\d").matcher(Files.readString(f));
                while (m.find()) code.add(m.group());
            }
        }
        Set<String> documented = new TreeSet<>();
        Matcher m = Pattern.compile("(?m)^- \\*\\*(LED-\\d\\d)").matcher(doc());
        while (m.find()) documented.add(m.group(1));
        assertEquals(documented, code);
    }
}
