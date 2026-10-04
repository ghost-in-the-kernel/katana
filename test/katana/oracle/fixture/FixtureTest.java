package katana.oracle.fixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/** Holds the fixture model (docs/ROADMAP.md 1.3): the four CardDemo jobs are expressible; an unpinned clock is refused. */
class FixtureTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Function<String, String> PROFILES = FixtureCheck.profiles(ROOT);

    private static String carddemo(String job) throws Exception {
        return Files.readString(ROOT.resolve("oracle/fixture/carddemo/" + job + ".fixture"));
    }

    private static List<String> refusals(String text) {
        return FixtureCheck.refusals(Fixture.read(text), PROFILES);
    }

    @Test
    void theFourCardDemoJobsAreExpressible() throws Exception {
        for (String job : List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "CREASTMT")) assertEquals(List.of(), refusals(carddemo(job)), job);
    }

    @Test
    void anUnpinnedClockIsRefused() throws Exception {
        String posttran = carddemo("POSTTRAN");
        String pinned = "clock 2024-03-01 12:00:00.00 +00:00\n";
        assertRefused(posttran.replace(pinned, ""), "unpinned clock: no clock line");
        assertRefused(posttran.replace(pinned, "clock 2024-03-01 12:00:00\n"), "unpinned clock: 2024-03-01 12:00:00 is not");
        assertRefused(posttran.replace(pinned, "clock 2024-03-01 12:00:00.00\n"), "unpinned clock");
        assertRefused(posttran.replace(pinned, "clock 2024-02-30 12:00:00.00 +00:00\n"), "is not a date");
        assertRefused(posttran.replace("tz UTC\n", ""), "no tz line");
    }

    @Test
    void aFixtureValidForNoProfileOrAnotherIsRefused() throws Exception {
        String posttran = carddemo("POSTTRAN");
        String profile = posttran.lines().filter(l -> l.startsWith("profile ")).findFirst().orElseThrow();
        assertRefused(posttran.replace(profile + "\n", ""), "no profile line");
        assertRefused(posttran.replace(profile, profile.replace("profile ibm", "profile cobol85")), "profile cobol85 is sha256:");
        assertRefused(posttran.replace(profile, profile.replace("profile ibm", "profile mf")), "profile mf does not exist");
    }

    @Test
    void whatTheSchemaDoesNotKnowIsRefusedNotDropped() throws Exception {
        String posttran = carddemo("POSTTRAN");
        assertRefused(posttran + "region 4M\n", "unknown keyword region");
        assertRefused(posttran.replace("dd SYSOUT sysout", "dd SYSOUT sysout class=A"), "dd takes no class");
        assertRefused(posttran.replace("disp=SHR recfm=F lrecl=350 copybook=CVTRA06Y", "disp=SHR recfm=F lrecl=350 copybook=cvtra06y"), "bad copybook=cvtra06y");
        assertRefused(posttran + "| a card\n", "a card that does not follow an inline dd");
    }

    @Test
    void aDdIsOneOfItsFourForms() throws Exception {
        String posttran = carddemo("POSTTRAN");
        assertRefused(posttran.replace(" copybook=CVTRA06Y", ""), "a dataset DD needs copybook=");
        assertRefused(posttran.replace("dd SYSOUT sysout", "dd SYSOUT sysout dummy"), "one of sysout, dummy, inline or a dataset");
        assertRefused(posttran.replace("dd SYSOUT sysout", "dd SYSOUT inline"), "inline with no cards");
    }

    @Test
    void everyDatasetSaysHowItStarts() throws Exception {
        String posttran = carddemo("POSTTRAN");
        assertRefused(posttran.replace("keys=16,0 empty", "keys=16,0"), "starts either empty or from=");
        assertRefused(posttran.replace(" framing=lines sha256=5b25", " sha256=5b25"), "a file source needs framing=");
        assertRefused(posttran.replace("lrecl=350 keys=16,0 empty", "lrecl=350 empty"), "a KSDS needs keys=");
        assertRefused(posttran.replace("dataset AWS.M2.CARDDEMO.DALYREJS org=GDG", "dataset AWS.M2.CARDDEMO.DALYREJS org=GDG empty"), "a GDG base holds no records");
    }

    @Test
    void aStepReadsOnlyWhatExistsWhenItStarts() throws Exception {
        String posttran = carddemo("POSTTRAN");
        assertRefused(posttran.replaceAll("(?m)^dataset AWS.M2.CARDDEMO.DALYTRAN.PS .*\n", ""), "AWS.M2.CARDDEMO.DALYTRAN.PS does not exist");
        assertRefused(posttran.replace("dsn=AWS.M2.CARDDEMO.DALYREJS(+1) disp=NEW", "dsn=AWS.M2.CARDDEMO.DALYTRAN.PS disp=NEW"), "NEW, but AWS.M2.CARDDEMO.DALYTRAN.PS already exists");
        assertRefused(posttran.replace("DALYREJS(+1)", "DALYREJS(0)"), "generation 0 needs generations");
        assertRefused(posttran.replace("lrecl=350 copybook=CVTRA06Y", "lrecl=351 copybook=CVTRA06Y"), "lrecl=351, but AWS.M2.CARDDEMO.DALYTRAN.PS has 350");
        String creastmt = carddemo("CREASTMT");
        assertRefused(creastmt.replace("defined-by=DELDEF01", "defined-by=STEP999"), "no step STEP999");
        assertRefused(creastmt.replace("step STEP030 pgm=IEFBR14 cond=0,NE", "step STEP040 pgm=IEFBR14 cond=0,NE"), "step STEP040 twice");
    }

    @Test
    void theAddressIsTheCanonicalTextAlone() throws Exception {
        String posttran = carddemo("POSTTRAN");
        Fixture a = Fixture.read(posttran);
        String respaced = "# another comment\n" + posttran.replace("dd TRANFILE dsn=", "dd   TRANFILE   dsn=")
                .replace("disp=SHR recfm=F lrecl=50 copybook=CVACT03Y", "recfm=F disp=SHR lrecl=50 copybook=CVACT03Y");
        assertEquals(a.hash(), Fixture.read(respaced).hash());
        assertNotEquals(a.hash(), Fixture.read(posttran.replace("12:00:00.00", "12:00:00.01")).hash());
        assertTrue(a.canonical().startsWith("job POSTTRAN\nsource carddemo:app/jcl/POSTTRAN.jcl\n"), a.canonical());
    }

    @Test
    void aSourcesRecordsAreItsLinesPaddedAndAKsdsIsInKeyOrder() {
        byte[] source = "B2xx\r\nA1\nC3yy".getBytes(StandardCharsets.US_ASCII);
        assertEquals("B2xxA1  C3yy", new String(Content.records(source, "lines", 4, null), StandardCharsets.US_ASCII));
        assertEquals("A1  B2xxC3yy", new String(Content.records(source, "lines", 4, "1,0"), StandardCharsets.US_ASCII));
        assertThrows(IllegalArgumentException.class, () -> Content.records(source, "lines", 3, null));
        assertThrows(IllegalArgumentException.class, () -> Content.records("A1A2".getBytes(StandardCharsets.US_ASCII), "fixed", 2, "1,0"));
        assertThrows(IllegalArgumentException.class, () -> Content.records("ABC".getBytes(StandardCharsets.US_ASCII), "fixed", 2, null));
    }

    private static void assertRefused(String text, String expected) {
        List<String> refusals = refusals(text);
        assertTrue(refusals.stream().anyMatch(r -> r.contains(expected)), "expected a refusal containing '" + expected + "', got " + refusals);
    }
}
