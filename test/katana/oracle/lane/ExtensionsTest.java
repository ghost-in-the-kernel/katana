package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Test;

/**
 * R-081 as amended (J-255): the extensions switch (`Settings.extensions`, the GnuCOBOL directives `>>ELIF` and `>>D`) stays the caller's and
 * only the autotest census sets it, until a fixture or a proof first parses under the extensions. So no fixture and no proof path may
 * set it: in the compiler's source (every folder but `test/`), `Settings` is made with the switch off, except in `CompileFlags`, which
 * takes the census's choice as an argument and which only a test calls.
 */
class ExtensionsTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final List<String> SOURCES = List.of("front", "oracle", "intent", "ledger", "lower", "passes", "sir", "profile", "findings");

    /** The compiler sources that make a `Settings` with the switch on, or call the census's mapping. */
    static List<String> offenders(List<Path> roots) {
        return roots.stream().flatMap(root -> {
            try (Stream<Path> walk = Files.walk(root)) {
                return walk.filter(p -> p.toString().endsWith(".java") && !p.toString().contains("/prototypes/")).toList().stream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).filter(p -> {
            String name = p.getFileName().toString();
            if (name.equals("Settings.java") || name.equals("CompileFlags.java")) return false;
            try {
                String code = String.join("\n", Files.readAllLines(p).stream().map(String::strip)
                        .filter(l -> !l.startsWith("*") && !l.startsWith("/*") && !l.startsWith("//")).toList());
                return code.contains("CompileFlags.map") || switchOn(code, "declared(") || switchOn(code, "new Settings(");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).map(Path::toString).toList();
    }

    /** Whether some call that starts with `call` passes anything but the literal `false` as its second argument. */
    private static boolean switchOn(String code, String call) {
        for (int at = code.indexOf(call); at >= 0; at = code.indexOf(call, at + 1)) {
            int depth = 1, comma = -1, i = at + call.length();
            for (; depth > 0 && i < code.length(); i++) {
                char c = code.charAt(i);
                if (c == '(') depth++;
                else if (c == ')') depth--;
                else if (c == ',' && depth == 1 && comma < 0) comma = i;
            }
            String second = comma < 0 ? "" : code.substring(comma + 1, i - 1).strip();
            if (second.contains(",")) second = second.substring(0, second.indexOf(',')).strip();
            if (!second.equals("false")) return true;
        }
        return false;
    }

    @Test
    void noFixtureOrProofPathSetsTheExtensions() {
        assertEquals(List.of(), offenders(SOURCES.stream().map(ROOT::resolve).filter(Files::isDirectory).toList()));
    }

    @Test
    void aFixturesSettingsNeverHaveThem() {
        assertFalse(Definitions.settings(Fixture.read("job T\ndefine A=1\n")).extensions());
        assertFalse(Definitions.settings(Fixture.read("job T\ndefine -\n")).extensions());
        assertFalse(Definitions.settings(Fixture.read("job T\n")).extensions());
    }

    /** R-056's mutation: a source that sets the switch is seen. */
    @Test
    void theCheckSeesASourceThatSetsThem() throws Exception {
        Path dir = Files.createTempDirectory("ext");
        Files.writeString(dir.resolve("Bad.java"), "class Bad { Object s = katana.front.Settings.declared(java.util.Map.of(), true); }\n");
        assertFalse(offenders(List.of(dir)).isEmpty());
        Files.writeString(dir.resolve("Bad.java"), "class Bad { Object s = katana.front.Settings.declared(java.util.Map.of(), false); }\n");
        assertTrue(offenders(List.of(dir)).isEmpty());
    }
}
