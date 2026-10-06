package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** R-020's rule on rt/'s source: `MathContext` (significant-digit precision) is never used in code, a comment naming it aside; COBOL's limits are integer plus decimal digits. */
class RtSourceTest {
    private static final Path RT = Path.of(System.getProperty("katana.root", "."), "rt");

    static List<String> offenders(Stream<Path> sources) {
        return sources.filter(p -> p.toString().endsWith(".java")).filter(p -> {
            try {
                return Files.readAllLines(p).stream().map(String::strip)
                        .anyMatch(l -> !l.startsWith("*") && !l.startsWith("/*") && !l.startsWith("//") && l.contains("MathContext"));
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }).map(Path::toString).toList();
    }

    @Test
    void noMathContextInRt() throws Exception {
        try (Stream<Path> walk = Files.walk(RT)) {
            assertEquals(List.of(), offenders(walk));
        }
    }

    @Test
    void theCheckSeesOne() throws Exception {
        Path dir = Files.createTempDirectory("rt");
        Files.writeString(dir.resolve("Bad.java"), "import java.math.MathContext;\n");
        try (Stream<Path> walk = Files.walk(dir)) {
            assertFalse(offenders(walk).isEmpty());
        }
    }
}
