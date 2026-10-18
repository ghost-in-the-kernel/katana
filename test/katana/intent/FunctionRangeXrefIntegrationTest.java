package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import katana.intent.review.Review;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * T-F2 on the review file (research §33, §34): every `function` line of carddemo-batch.review holds on
 * cobc's own cross-reference listing, and cobc's PERFORM and GO TO references number the IR's edges, so the
 * test cannot pass on a listing it failed to read; a copy of CBTRN02C with a GO TO into 1500-B-LOOKUP-ACCT
 * refutes that range.
 */
@Tag("integration")
class FunctionRangeXrefIntegrationTest {
    @Test
    void everyFunctionLineHoldsOnCobcsListing() throws Exception {
        Map<String, String> sections = Review.sections(Files.readString(BatchReview.FILE));
        Map<String, List<String>> refused = new TreeMap<>();
        int checked = 0;
        List<Program> programs = BatchReview.programs();
        for (int i = 0; i < programs.size(); i++) {
            Program p = programs.get(i);
            FunctionRangeXref.Listing l = FunctionRangeXref.parse(FunctionRangeXref.listing(BatchReview.CARDDEMO.resolve("cbl/" + BatchReview.PROGRAMS.get(i))));
            long edges = p.procedure().edges().stream().filter(e -> e.kind() == Procedure.Kind.PERFORM || e.kind() == Procedure.Kind.GO_TO).count();
            assertEquals(edges, FunctionRangeXref.jumps(l), p.header().program() + ": cobc's PERFORM and GO TO references number the IR's edges");
            assertEquals(l.counted(), l.labels().values().stream().mapToInt(b -> b.references().size()).sum(), p.header().program() + ": every reference cobc counts is read");
            for (String line : sections.get(p.header().program()).lines().filter(x -> x.startsWith("inferred | function | ")).toList()) {
                checked++;
                FunctionRangeStructure.Range r = FunctionRangeStructure.range(p, line.split(" \\| ")[2]);
                List<String> why = FunctionRangeXref.refusals(l, p, r);
                if (!why.isEmpty()) refused.put(p.header().program() + " " + line.split(" \\| ")[2], why);
            }
        }
        assertEquals(175, checked);
        assertEquals(Map.of(), refused);
    }

    @Test
    void aGoToIntoARangeRefutesIt() throws Exception {
        Path dir = Files.createTempDirectory("katana-xref-mutant");
        List<String> source = Files.readAllLines(BatchReview.CARDDEMO.resolve("cbl/CBTRN02C.cbl"), StandardCharsets.ISO_8859_1);
        int goback = 0;
        while (!source.get(goback).contains(" GOBACK")) goback++;
        source.add(goback, source.get(goback).replace("GOBACK.", "GO TO 1500-B-LOOKUP-ACCT."));
        Path mutant = Files.write(dir.resolve("CBTRN02C.cbl"), source, StandardCharsets.ISO_8859_1);
        Program p = BatchReview.programs().get(0);
        FunctionRangeXref.Listing l = FunctionRangeXref.parse(FunctionRangeXref.listing(mutant));
        List<String> why = FunctionRangeXref.refusals(l, p, FunctionRangeStructure.range(p, "1500-B-LOOKUP-ACCT"));
        assertTrue(why.stream().anyMatch(w -> w.contains("outside it, names") || w.contains("otherwise than PERFORM")), why.toString());
        assertEquals(List.of(), FunctionRangeXref.refusals(FunctionRangeXref.parse(FunctionRangeXref.listing(BatchReview.CARDDEMO.resolve("cbl/CBTRN02C.cbl"))), p, FunctionRangeStructure.range(p, "1500-B-LOOKUP-ACCT")));
    }
}
