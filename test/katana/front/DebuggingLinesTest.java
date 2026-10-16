package katana.front;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Debugging lines, D in column 7: code only WITH DEBUGGING MODE (LR 6.4 p. 122, p. 760; research §17, J-176). */
class DebuggingLinesTest {
    private static String program(boolean mode, String... procedure) {
        StringBuilder text = new StringBuilder("       IDENTIFICATION DIVISION.\n       PROGRAM-ID. P.\n       ENVIRONMENT DIVISION.\n"
                + "       CONFIGURATION SECTION.\n       SOURCE-COMPUTER. IBM-Z" + (mode ? " WITH DEBUGGING MODE" : "") + ".\n"
                + "       PROCEDURE DIVISION.\n       MAIN-PARA.\n");
        for (String line : procedure) text.append(line).append('\n');
        return text.toString();
    }

    private static String text(String source, Copybooks books) {
        return Preprocessor.run(source, books).mapped().toString();
    }

    private static final Copybooks NONE = new Copybooks(List.of(), name -> 8);

    @Test
    void aDebuggingLineIsACommentWithoutDebuggingMode() {
        String without = text(program(false, "      D    DISPLAY \"TRACE\".", "           STOP RUN."), NONE);
        assertFalse(without.contains("TRACE"), without);
        String with = text(program(true, "      D    DISPLAY \"TRACE\".", "           STOP RUN."), NONE);
        assertTrue(with.contains("DISPLAY \"TRACE\""), with);
    }

    @Test
    void textACopyOnADebuggingLineCopiesIsDebuggingText(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("BOOK.cpy"), "           DISPLAY \"COPIED\".\n");
        Copybooks books = new Copybooks(List.of(dir), name -> 8);
        String plain = text(program(false, "           COPY BOOK.", "      D    COPY BOOK.", "           STOP RUN."), books);
        assertTrue(plain.indexOf("COPIED") >= 0 && plain.indexOf("COPIED") == plain.lastIndexOf("COPIED"), "the plain COPY's text stays, the debugging one's goes: " + plain);
        assertTrue(Parse.text(program(false, "           COPY BOOK.", "      D    COPY BOOK.", "           STOP RUN."), books).clean());
    }
}
