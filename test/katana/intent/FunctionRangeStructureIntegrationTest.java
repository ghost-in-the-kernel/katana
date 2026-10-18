package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import katana.intent.review.Review;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * T-F1 on the review file (research §33, R-066): every `function` line of carddemo-batch.review holds on the
 * graph sir/ builds, one entry, one exit, no GO TO out; and the lines a wrong rule would write, CBSTM03B's
 * 1000-TRNXFILE-PROC alone and CBSTM03A's ALTERed and main-line paragraphs, are refused.
 */
@Tag("integration")
class FunctionRangeStructureIntegrationTest {
    @Test
    void everyFunctionLineHoldsOnTheGraphAndAWrongOneIsRefused() throws Exception {
        Map<String, String> sections = Review.sections(Files.readString(BatchReview.FILE));
        Map<String, List<String>> refused = new TreeMap<>();
        int checked = 0;
        Map<String, Program> byName = new TreeMap<>();
        for (Program p : BatchReview.programs()) byName.put(p.header().program(), p);
        for (Map.Entry<String, Program> e : byName.entrySet()) {
            Program p = e.getValue();
            List<FunctionRangeStructure.Range> ranges = new ArrayList<>();
            for (String line : sections.get(e.getKey()).lines().filter(l -> l.startsWith("inferred | function | ")).toList()) ranges.add(FunctionRangeStructure.range(p, line.split(" \\| ")[2]));
            for (FunctionRangeStructure.Range r : ranges) {
                checked++;
                List<String> why = FunctionRangeStructure.refusals(p, r, ranges);
                if (!why.isEmpty()) refused.put(e.getKey() + " " + FunctionRangeStructure.name(p, r.first()), why);
            }
        }
        assertEquals(175, checked);
        assertEquals(Map.of(), refused);
        Program b = byName.get("CBSTM03B"), a = byName.get("CBSTM03A");
        assertTrue(FunctionRangeStructure.refusals(b, FunctionRangeStructure.range(b, "1000-TRNXFILE-PROC"), List.of()).stream().anyMatch(w -> w.contains("leaves it")));
        assertTrue(FunctionRangeStructure.refusals(a, FunctionRangeStructure.range(a, "8100-TRNXFILE-OPEN"), List.of()).stream().anyMatch(w -> w.contains("main line") || w.contains("ALTER")));
        assertTrue(FunctionRangeStructure.refusals(a, FunctionRangeStructure.range(a, "1000-MAINLINE"), List.of()).stream().anyMatch(w -> w.contains("main line")));
    }
}
