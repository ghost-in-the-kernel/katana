package katana.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Holds docs/PROFILE.md to profile/ (docs/ROADMAP.md 4.1): its fields are the reader's, and its invariants hold. */
class ProfileTest {
    private static String read(String path) throws Exception {
        return Files.readString(Path.of(System.getProperty("katana.root", "."), path));
    }

    @Test
    void theDocumentNamesTheFieldsTheReaderReads() throws Exception {
        String doc = read("docs/PROFILE.md");
        String fields = doc.substring(doc.indexOf("## The fields"), doc.indexOf("## The GnuCOBOL mapping"));
        List<String> named = new ArrayList<>();
        Matcher row = Pattern.compile("(?m)^\\| `([a-z]+)` \\|").matcher(fields);
        while (row.find()) named.add(row.group(1));
        assertEquals(Profile.FIELDS, named);
    }

    @Test
    void theHashIsOverTheCanonicalTextAlone() {
        Profile a = Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag b=no\nflag a=yes\n");
        Profile b = Profile.read("# a comment\n  dialect   ibm\nflag a=yes # set\nversion 3.1.2\ncompiler GnuCOBOL\n\nflag b=no\n");
        assertEquals(a.hash(), b.hash());
        assertEquals("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag a=yes\nflag b=no\n", a.canonical());
        assertNotEquals(a.hash(), Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\n").hash());
    }

    @Test
    void aMissingUnknownOrRepeatedFieldIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\n"));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\ntrunc std\n"));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\ndialect mf\n"));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag a=1\nflag a=2\n"));
    }

    @Test
    void theCobcArgumentsAreTheDialectThenEachFlag() {
        Profile p = Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\nflag arithmetic-osvs=no\nflag binary-size=1-2-4-8\n");
        assertEquals(List.of("-std=ibm", "-fno-arithmetic-osvs", "-fbinary-size=1-2-4-8", "-fbinary-truncate"), p.cobcArguments());
    }

    @Test
    void theCommittedProfilesRead() throws Exception {
        assertEquals("ibm", Profile.read(read("profile/ibm.profile")).dialect());
        assertEquals("cobol85", Profile.read(read("profile/cobol85.profile")).dialect());
    }
}
