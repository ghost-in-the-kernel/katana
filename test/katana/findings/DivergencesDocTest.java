package katana.findings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** docs/DIVERGENCES.md's table is Divergence.Kind: the same codes, titles, rules, exact and wrong shapes, in order. */
class DivergencesDocTest {
    @Test
    void theTableIsTheKinds() throws Exception {
        String doc = Files.readString(Path.of(System.getProperty("katana.root", "."), "docs/DIVERGENCES.md"));
        List<String> table = new ArrayList<>();
        Matcher m = Pattern.compile("(?m)^\\| `(DV-\\d\\d)` \\| (.+?) \\| (.+?) \\| (.+?) \\| (.+?) \\|$").matcher(doc);
        while (m.find()) table.add(String.join(" | ", m.group(1), m.group(2), m.group(3), m.group(4), m.group(5)));
        assertEquals(Arrays.stream(Divergence.Kind.values()).map(k -> String.join(" | ", k.code, k.title, k.rule, k.exact, k.wrong)).toList(), table);
    }
}
