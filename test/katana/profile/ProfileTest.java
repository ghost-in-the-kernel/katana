package katana.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Holds docs/PROFILE.md to profile/ (docs/ROADMAP.md 4.1): its fields are the reader's, and its invariants hold. */
class ProfileTest {
    private static final String IBM = "ibm.trunc STD\nibm.numproc NOPFD\nibm.arith COMPAT\nibm.codepage 1140\nibm.invdata NO\nibm.version 6.4\nibm.dispsign COMPAT\nibm.wsinit NONE\n";
    private static final String IBM_SHUFFLED = "ibm.wsinit NONE\nibm.version 6.4\nibm.codepage   1140\nibm.trunc STD\nibm.dispsign COMPAT\nibm.invdata NO\nibm.arith COMPAT\nibm.numproc NOPFD\n";
    private static String read(String path) throws Exception {
        return Files.readString(Path.of(System.getProperty("katana.root", "."), path));
    }

    @Test
    void theDocumentNamesTheFieldsTheReaderReads() throws Exception {
        String doc = read("docs/PROFILE.md");
        int start = doc.indexOf("## The fields");
        String fields = doc.substring(start, doc.indexOf("\n## ", start + 1));
        List<String> named = new ArrayList<>();
        Matcher row = Pattern.compile("(?m)^\\| `([a-z.]+)` \\|").matcher(fields);
        while (row.find()) named.add(row.group(1));
        assertEquals(Profile.FIELDS, named);
    }

    @Test
    void theHashIsOverTheCanonicalTextAlone() {
        Profile a = Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag b=no\nflag a=yes\nflag binary-truncate=yes\n" + IBM);
        Profile b = Profile.read("# a comment\n  dialect   ibm\nflag binary-truncate=yes\nflag a=yes # set\n" + IBM_SHUFFLED + "version 3.1.2\ncompiler GnuCOBOL\n\nflag b=no\n");
        assertEquals(a.hash(), b.hash());
        assertEquals("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag a=yes\nflag b=no\nflag binary-truncate=yes\n" + IBM, a.canonical());
        assertNotEquals(a.hash(), Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\n" + IBM).hash());
        assertNotEquals(a.hash(), Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag b=no\nflag a=yes\nflag binary-truncate=yes\n" + IBM.replace("ibm.codepage 1140", "ibm.codepage 37")).hash());
    }

    @Test
    void aMissingUnknownOrRepeatedFieldIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\n"));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\ntrunc std\n" + IBM));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\ndialect mf\n" + IBM));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\nflag a=1\nflag a=2\n" + IBM));
    }

    @Test
    void everyIbmFieldIsStatedWithAValueFromItsSet() {
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\n" + IBM.replace("ibm.arith COMPAT\n", "")));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\n" + IBM.replace("ibm.trunc STD", "ibm.trunc std")));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\n" + IBM.replace("ibm.codepage 1140", "ibm.codepage IBM-1140")));
        assertEquals(List.of(), Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\n" + IBM).findings());
        assertEquals(1, Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\n" + IBM.replace("NOPFD", "MIG")).findings().size());
        assertEquals("FORCENUMCMP,CLEANSIGN", Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\n"
                + IBM.replace("ibm.invdata NO", "ibm.invdata FORCENUMCMP,CLEANSIGN")).ibm().get("ibm.invdata"));
    }

    @Test
    void theLaneTruncatesBinaryItemsAsIbmTruncSays() {
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\n" + IBM));
        assertThrows(IllegalArgumentException.class, () -> Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=no\n" + IBM));
        assertEquals("no", Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=no\n"
                + IBM.replace("ibm.trunc STD", "ibm.trunc BIN")).flags().get("binary-truncate"));
        assertEquals(Map.of(), Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect cobol85\n" + IBM).flags());
    }

    @Test
    void theCobcArgumentsAreTheDialectThenEachFlag() {
        Profile p = Profile.read("compiler GnuCOBOL\nversion 3.1.2\ndialect ibm\nflag binary-truncate=yes\nflag arithmetic-osvs=no\nflag binary-size=1-2-4-8\n" + IBM);
        assertEquals(List.of("-std=ibm", "-fno-arithmetic-osvs", "-fbinary-size=1-2-4-8", "-fbinary-truncate"), p.cobcArguments());
    }

    @Test
    void theCommittedProfilesRead() throws Exception {
        assertEquals("ibm", Profile.read(read("profile/ibm.profile")).dialect());
        assertEquals("cobol85", Profile.read(read("profile/cobol85.profile")).dialect());
    }
}
