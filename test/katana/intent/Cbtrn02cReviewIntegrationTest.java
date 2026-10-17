package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.review.Review;
import katana.intent.rules.Rules;
import katana.intent.types.State;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 5.3's trial (research §30): CBTRN02C's review, a person's edits on it, and three rebuilds: of the same
 * program, of it with a WORKING-STORAGE item added above the others (every later item renumbered), and of it
 * with a confirmed function's paragraph renamed. The edits are the trial's, not a reading of the program.
 */
@Tag("integration")
class Cbtrn02cReviewIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CARDDEMO = ROOT.resolve("corpus/carddemo/app");

    private static Program build(String source) throws Exception {
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        return Build.program(Parse.text(source, new Copybooks(List.of(CARDDEMO.resolve("cpy")), n -> 8)), ibm.hash()).program();
    }

    private static Review.Result rebuild(Program p, String review) {
        return Review.merge(p, Rules.infer(p), review);
    }

    @Test
    void aPersonsWordOnCbtrn02cOutlivesARenumberingAndARenameIsOrphaned() throws Exception {
        String source = Files.readString(CARDDEMO.resolve("cbl/CBTRN02C.cbl"));
        Program p = build(source);
        String first = rebuild(p, "").text();
        String edited = first.replace("inferred | function | 1500-A-LOOKUP-XREF | function.perform-range v1 | 1",
                        "confirmed | function | 1500-A-LOOKUP-XREF | function.perform-range v1 | 1 | Ghost 2026-10-17")
                .replace("inferred | enum | APPL-RESULT: APPL-AOK, APPL-EOF | enum.disjoint-88s v1 | 0.5",
                        "confirmed | enum | APPL-RESULT: APPL-AOK, APPL-EOF | enum.disjoint-88s v1 | 0.5 | Ghost 2026-10-17")
                .replace("inferred | function | Z-GET-DB2-FORMAT-TIMESTAMP | function.perform-range v1 | 1",
                        "refuted | function | Z-GET-DB2-FORMAT-TIMESTAMP | function.perform-range v1 | 1 | Ghost 2026-10-17 | the trial's refutation")
                + "confirmed | money | DALYTRAN-AMT | person v1 | 1 | Ghost 2026-10-17\n";
        Review.Result again = rebuild(p, edited);
        assertEquals(List.of(), again.reported());
        long said = again.intent().facts().stream().filter(f -> f.state().status() != State.Status.INFERRED).count();
        assertEquals(4, said);

        String added = source.replaceFirst("(?m)^(.{6} *WORKING-STORAGE SECTION\\..*)$", "$1\n       01  KATANA-TRIAL-ITEM          PIC X.");
        assertTrue(!added.equals(source));
        Review.Result renumbered = rebuild(build(added), again.text());
        assertEquals(again.text(), renumbered.text());
        assertEquals(List.of(), renumbered.reported());

        Review.Result renamed = rebuild(build(source.replace("1500-A-LOOKUP-XREF", "1500-A-XREF-LOOKUP")), again.text());
        assertTrue(renamed.text().contains("orphaned | confirmed | function | 1500-A-LOOKUP-XREF | function.perform-range v1 | 1 | Ghost 2026-10-17\n"), renamed.text());
        assertTrue(renamed.text().contains("inferred | function | 1500-A-XREF-LOOKUP | function.perform-range v1 | 1\n"), renamed.text());
        assertEquals(1, renamed.reported().size(), renamed.reported().toString());
        assertEquals(3, renamed.intent().facts().stream().filter(f -> f.state().status() != State.Status.INFERRED).count());
    }
}
