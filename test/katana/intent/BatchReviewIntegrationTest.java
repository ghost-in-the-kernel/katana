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
 * every fact they infer has its line, each line carries its readings and each built test's verdict as the test
 * computes it now, and the file loads as a review
 * with nothing orphaned. Under `-Dkatana.record=true` it writes the file, refusing to when any line
 * holds a person's or Fable's word.
 */
@Tag("integration")
class BatchReviewIntegrationTest {
    private static final String CATALOGUE;

    static {
        try {
            String research = Files.readString(BatchReview.ROOT.resolve("docs/research/ibm-options.md"));
            CATALOGUE = research.substring(research.indexOf("## 33."));
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    @Test
    void everyLineIsAFactTheRulesInferWithItsReadings() throws Exception {
        List<Program> programs = BatchReview.programs();
        if (Boolean.getBoolean("katana.record")) {
            if (Files.exists(BatchReview.FILE)) {
                String old = Files.readString(BatchReview.FILE);
                assertTrue(old.lines().filter(l -> !l.startsWith("#")).noneMatch(l -> l.startsWith("confirmed") || l.startsWith("refuted") || l.contains("could not tell:")),
                        "the review holds a person's word; it is merged, never rewritten (the proposal column is written from carddemo-batch.proposals)");
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
            Map<String, katana.intent.types.Fact> byLine = BatchReview.byLine(p);
            for (String fact : lines) {
                List<String> after = part.lines().dropWhile(l -> !l.equals(fact)).skip(1).takeWhile(l -> l.startsWith("#")).toList();
                List<String> verdicts = after.stream().skip(4).takeWhile(l -> l.startsWith("#   T-")).toList();
                assertTrue(after.size() >= 5 + verdicts.size() && after.get(0).startsWith("#   evidence: ") && after.get(1).startsWith("#   rule: ") && after.get(2).matches("#   Fable \\(proposal, R-031\\): \\d+% because .+")
                        && after.get(3).startsWith("#   tests: ") && after.get(4 + verdicts.size()).startsWith("#   docs: "), fact + " has its readings, its proposal, the tests it names and their verdicts");
                for (String id : after.get(3).replaceAll("\\([^)]*\\)", "").replaceAll(";.*", "").replace("#   tests: ", "").split(",\\s*")) {
                    assertTrue(id.isBlank() || java.util.regex.Pattern.compile("\\*\\*" + id.strip() + "[ *]").matcher(CATALOGUE).find(), id + " is in research section 33's catalogue");
                }
                String named = BatchReview.proposals().get(p.header().program() + " | " + fact.split("\\s*\\|\\s*")[1] + " | " + fact.split("\\s*\\|\\s*")[2])[2];
                assertEquals(ReviewTests.verdicts(p, byLine.get(fact), named).stream().map(v -> "#   " + v.line()).toList(), verdicts, fact + ": each built test's verdict is what it computes now");
            }
            facts += lines.size();
            assertTrue(loaded.intent().facts().stream().noneMatch(f -> f.state().status() == State.Status.CONFIRMED && !f.state().who().equals("Ghost")), "only a person confirms");
        }
        System.out.println("review lines: " + facts);
        for (String key : BatchReview.notes().keySet()) {
            String[] k = key.split(" \\| ", -1);
            String part = sections.get(k[0]);
            boolean held = part != null && (k[1].equals("program") || part.lines().anyMatch(l -> l.startsWith("inferred | " + k[1] + " | " + k[2] + " | ")));
            assertTrue(held, "the note on " + key + " names no line of the review file");
            assertTrue(text.contains(BatchReview.notes().get(key)), "the note on " + key + " is in the file");
        }
    }
}
