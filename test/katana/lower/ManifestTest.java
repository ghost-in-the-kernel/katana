package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** The lowering's manifest (research §31): every field, record and stretch method the Java declares, each on its COBOL node, a nested program's inside its container's class. */
public class ManifestTest {
    private static final Pattern DECLARED = Pattern.compile("private (?:Field|Record) ([fr]\\d+);|private int (s\\d+)\\(\\)");

    /** The members the Java text declares, in order. */
    public static List<String> declared(String java) {
        List<String> out = new ArrayList<>();
        Matcher m = DECLARED.matcher(java);
        while (m.find()) out.add(m.group(1) != null ? m.group(1) : m.group(2));
        return out;
    }

    /** The manifest's members (its classes left out), in order. */
    public static List<String> members(List<Lower.Symbol> symbols) {
        return symbols.stream().filter(s -> !s.node().startsWith("program ")).map(s -> s.name().substring(s.name().lastIndexOf('.') + 1)).toList();
    }

    @Test
    void theManifestNamesWhatTheJavaDeclaresOnItsNodes() {
        String source = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. OUTER.", "DATA DIVISION.", "WORKING-STORAGE SECTION.", "01 A.", "   05 A1 PIC X.",
                "PROCEDURE DIVISION.", "MAIN.", "    CALL 'INNER'.", "    STOP RUN.", "IDENTIFICATION DIVISION.", "PROGRAM-ID. INNER.", "DATA DIVISION.",
                "WORKING-STORAGE SECTION.", "01 B PIC X.", "PROCEDURE DIVISION.", "P1.", "    MOVE 'X' TO B.", "    EXIT PROGRAM.", "END PROGRAM INNER.", "END PROGRAM OUTER.")
                .stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        Program p = Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
        Lower.Result r = Lower.java(p, "OUTER");
        List<String> names = r.symbols().stream().map(s -> s.name() + " " + s.node()).toList();
        for (String expected : List.of("OUTER program OUTER", "OUTER.r1 item 1", "OUTER.f1 item 1", "OUTER.f2 item 2", "OUTER.s1 paragraph 1", "OUTER.INNER program INNER",
                "OUTER.INNER.r1 item 1", "OUTER.INNER.f1 item 1", "OUTER.INNER.s1 paragraph 1")) {
            assertEquals(true, names.contains(expected), expected + " in " + names);
        }
        assertEquals(declared(r.java()), members(r.symbols()));
    }
}
