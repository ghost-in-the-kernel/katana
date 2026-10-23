package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** A program's paragraph names from its paragraph graph, and the ones a cobc trace entered (docs/ROADMAP.md 1.9). */
class ParagraphsTest {
    private static final String SOURCE = String.join("\n",
            "       IDENTIFICATION DIVISION.",
            "       PROGRAM-ID. T.",
            "       PROCEDURE DIVISION.",
            "       MAIN SECTION.",
            "       MAIN-PARA.",
            "           PERFORM A-PARA",
            "      *    B-COMMENTED.",
            "           STOP RUN.",
            "       A-PARA.",
            "           DISPLAY 'A'.",
            "        B-PARA.",
            "           EXIT.");

    @Test
    void theSectionsAndParagraphsInTextualOrder() throws Exception {
        Path source = Files.createTempDirectory("paragraphs").resolve("T.cbl");
        Files.writeString(source, SOURCE + "\n");
        assertEquals(List.of("MAIN", "MAIN-PARA", "A-PARA", "B-PARA"), Paragraphs.of(source, List.of(), f -> 8, katana.front.Settings.UNDECLARED));
    }

    @Test
    void theNamesATraceEnteredForAProgram() {
        String trace = String.join("\n", "Source: 'T.cbl'", "Program-Id:  T                    Entry: T            Line: 3",
                "Program-Id:  T                  Section: MAIN         Line: 4",
                "Program-Id:  T                Paragraph: MAIN-PARA    Line: 5",
                "Program-Id:  U                Paragraph: OTHER        Line: 9",
                "Program-Id:  T                Paragraph: A-PARA       Line: 9");
        assertEquals(Set.of("MAIN", "MAIN-PARA", "A-PARA"), Paragraphs.entered(trace, "T"));
    }
}
