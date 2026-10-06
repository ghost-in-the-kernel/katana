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

/** docs/FINDINGS.md's table is Finding.Kind: the same codes, titles and reasons, in order. */
class FindingsDocTest {
    @Test
    void theTableIsTheKinds() throws Exception {
        String doc = Files.readString(Path.of(System.getProperty("katana.root", "."), "docs/FINDINGS.md"));
        List<String> table = new ArrayList<>();
        Matcher m = Pattern.compile("(?m)^\\| `((?:NE|HY)-\\d\\d)` \\| (.+?) \\| (.+?) \\|$").matcher(doc);
        while (m.find()) table.add(m.group(1) + " " + m.group(2) + ": " + m.group(3));
        assertEquals(Arrays.stream(Finding.Kind.values()).map(k -> k.code + " " + k.title + ": " + k.why).toList(), table);
    }
}
