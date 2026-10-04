package katana.oracle.fixture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Holds docs/FIXTURE.md to oracle/fixture/Schema.java: the lines, their attributes and words, and the attribute values. */
class FixtureDocTest {
    private static final Pattern CODE = Pattern.compile("`([a-z0-9-]+)`");

    private static String doc() throws Exception {
        return Files.readString(Path.of(System.getProperty("katana.root", "."), "docs/FIXTURE.md"));
    }

    @Test
    void theLinesTableIsTheSchema() throws Exception {
        Map<String, String> documented = new LinkedHashMap<>(), schema = new LinkedHashMap<>();
        for (String row : doc().lines().filter(l -> l.matches("\\| `[a-z]+` \\| (\\d|rest) \\|.*")).toList()) {
            String[] cells = row.split("\\|");
            documented.put(names(cells[1]).get(0), cells[2].trim() + " " + names(cells[3]) + " " + names(cells[4]));
        }
        Schema.SPECS.forEach((keyword, spec) -> schema.put(keyword, (spec.positional() < 0 ? "rest" : String.valueOf(spec.positional()))
                + " " + new ArrayList<>(spec.attributes()) + " " + new ArrayList<>(spec.words())));
        assertEquals(schema, documented);
    }

    @Test
    void theAttributeTableNamesEveryAttributeInTheSchemasOrder() throws Exception {
        List<String> table = doc().lines().dropWhile(l -> !l.startsWith("| Attribute |")).skip(2)
                .takeWhile(l -> l.startsWith("|")).map(l -> names(l.split("\\|")[1]).get(0)).toList();
        assertEquals(new ArrayList<>(Schema.VALUES.keySet()), table);
    }

    private static List<String> names(String cell) {
        List<String> names = new ArrayList<>();
        Matcher m = CODE.matcher(cell);
        while (m.find()) names.add(m.group(1));
        return names;
    }
}
