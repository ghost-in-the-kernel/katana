package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.intent.review.Review;
import katana.intent.rules.Rules;
import katana.intent.types.State;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * R-066's doc test for the first review file: every fact line is a fact the rules infer on its program,
 * every fact they infer has its line, each line carries its four readings, and the file loads as a review
 * with nothing orphaned. Under `-Dkatana.record=true` it writes the file, refusing to when any line
 * holds a person's or Fable's word.
 */
@Tag("integration")
class BatchReviewIntegrationTest {
    @Test
    void everyLineIsAFactTheRulesInferWithItsReadings() throws Exception {
        List<Program> programs = BatchReview.programs();
        if (Boolean.getBoolean("katana.record")) {
            if (Files.exists(BatchReview.FILE)) {
                String old = Files.readString(BatchReview.FILE);
                assertTrue(old.lines().noneMatch(l -> l.startsWith("confirmed") || l.startsWith("refuted") || l.contains("could not tell:") || (l.startsWith("#   Fable") && !l.contains("___%"))),
                        "the review holds a person's or Fable's word; it is merged, never rewritten");
            }
            Files.writeString(BatchReview.FILE, BatchReview.write(programs));
        }
        String text = Files.readString(BatchReview.FILE);
        Map<String, String> sections = Review.sections(text);
        assertEquals(programs.stream().map(p -> p.header().program()).toList(), new ArrayList<>(sections.keySet()));
        int facts = 0;
        for (Program p : programs) {
            String part = sections.get(p.header().program());
            Review.Result loaded = Review.merge(p, Rules.infer(p), part);
            assertEquals(List.of(), loaded.reported(), p.header().program());
            List<String> lines = part.lines().filter(l -> !l.isBlank() && !l.startsWith("#")).toList();
            assertEquals(Rules.infer(p).facts().size(), lines.size(), p.header().program() + ": a line a fact");
            for (String fact : lines) {
                List<String> after = part.lines().dropWhile(l -> !l.equals(fact)).skip(1).limit(4).toList();
                assertTrue(after.size() == 4 && after.get(0).startsWith("#   evidence: ") && after.get(1).startsWith("#   rule: ") && after.get(2).startsWith("#   Fable (proposal, R-031): ")
                        && after.get(3).startsWith("#   docs: "), fact + " has its four readings");
            }
            facts += lines.size();
            assertTrue(loaded.intent().facts().stream().noneMatch(f -> f.state().status() == State.Status.CONFIRMED && !f.state().who().equals("Ghost")), "only a person confirms");
        }
        System.out.println("review lines: " + facts);
    }
}
