package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * STRING and UNSTRING (phase 3.1), cobc -std=ibm their oracle: 200 of each from a fixed seed, and
 * one of each written to settle a reading IBM's text leaves open. STRING:
 * one to three senders DELIMITED BY a literal or SIZE into a receiver filled beforehand, from a pointer
 * that may start outside it. UNSTRING: a sender over a small alphabet, none to two delimiters (ALL or
 * not), one to three receivers alphanumeric or numeric with DELIMITER IN and COUNT IN, a pointer and a
 * tally. Each statement's receivers, pointer, tally and overflow are DISPLAYed; `rt/Strings` must give
 * the same. Its mutation: delimiters ignored (SIZE for STRING, ALL dropped for UNSTRING) differs.
 */
@Tag("integration")
class StringsDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final List<String> DELIMITERS = List.of(",", ";", ",;", "AB");

    /** A STRING: senders and their delimiters (null for SIZE), the receiver's size and the pointer. */
    record Stringing(List<String> senders, List<String> delimiters, int size, int pointer) {}

    /** A receiver: its size, numeric or not. */
    record Into(int size, boolean numeric) {}

    /** An UNSTRING: the sender, its delimiters with ALL, its receivers, whether they take DELIMITER IN and COUNT IN, the pointer and the tally. */
    record Unstringing(String sender, List<String> delimiters, List<Boolean> all, List<Into> into, int pointer, int tally) {}

    static List<Stringing> stringings(Random r) {
        List<Stringing> out = new ArrayList<>();
        for (int n = 0; n < 200; n++) {
            int count = 1 + r.nextInt(3);
            List<String> senders = new ArrayList<>(), delimiters = new ArrayList<>();
            for (int k = 0; k < count; k++) {
                senders.add(text(r, 1 + r.nextInt(6), "AB12,;"));
                delimiters.add(r.nextInt(3) == 0 ? null : DELIMITERS.get(r.nextInt(DELIMITERS.size())));
            }
            int size = 2 + r.nextInt(10);
            out.add(new Stringing(senders, delimiters, size, r.nextInt(5) == 0 ? r.nextInt(size + 3) : 1 + r.nextInt(size)));
        }
        out.add(new Stringing(List.of("ABCD"), java.util.Arrays.asList((String) null), 4, 1)); // a receiver filled exactly: no overflow (J-106)
        return out;
    }

    static List<Unstringing> unstringings(Random r) {
        List<Unstringing> out = new ArrayList<>();
        for (int n = 0; n < 200; n++) {
            // A numeric receiver is only ever given digits (IBM leaves a nonnumeric MOVE into one undefined): every other character is a delimiter.
            boolean numeric = n % 4 == 3, delimited = n % 5 != 4;
            String sender = text(r, 3 + r.nextInt(12), !numeric ? "AB12,;" : delimited ? "12,;" : "12");
            int dc = !delimited ? 0 : numeric ? 2 : 1 + r.nextInt(2);
            List<String> delimiters = new ArrayList<>();
            List<Boolean> all = new ArrayList<>();
            for (int k = 0; k < dc; k++) {
                delimiters.add(numeric ? List.of(",", ";").get(k) : DELIMITERS.get(r.nextInt(DELIMITERS.size())));
                all.add(r.nextBoolean());
            }
            List<Into> into = new ArrayList<>();
            int rc = 1 + r.nextInt(3);
            for (int k = 0; k < rc; k++) into.add(new Into(1 + r.nextInt(5), numeric));
            out.add(new Unstringing(sender, delimiters, all, into, r.nextInt(6) == 0 ? r.nextInt(sender.length() + 3) : 1 + r.nextInt(3), r.nextInt(4)));
        }
        // ALL takes repeats of the same delimiter as one, not of any (J-106): "A,;B" is A, nothing, B.
        out.add(new Unstringing("A,;B", List.of(",", ";"), List.of(true, true), List.of(new Into(3, false), new Into(3, false), new Into(3, false)), 1, 0));
        return out;
    }

    private static String text(Random r, int length, String alphabet) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < length; k++) s.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return s.toString();
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String str(byte[] b) {
        return new String(b, StandardCharsets.ISO_8859_1);
    }

    static String program(List<Stringing> ss, List<Unstringing> us) {
        StringBuilder data = new StringBuilder("01 OV PIC X.\n"), proc = new StringBuilder();
        for (int n = 0; n < ss.size(); n++) {
            Stringing s = ss.get(n);
            StringBuilder st = new StringBuilder("STRING");
            for (int k = 0; k < s.senders().size(); k++) {
                data.append("01 S").append(n).append('-').append(k).append(" PIC X(").append(s.senders().get(k).length()).append(") VALUE \"").append(s.senders().get(k)).append("\".\n");
                st.append(" S").append(n).append('-').append(k).append(" DELIMITED BY ").append(s.delimiters().get(k) == null ? "SIZE" : "\"" + s.delimiters().get(k) + "\"");
            }
            data.append("01 R").append(n).append(" PIC X(").append(s.size()).append(") VALUE ALL \"#\".\n01 P").append(n).append(" PIC 99 VALUE ").append(s.pointer()).append(".\n");
            proc.append("MOVE \"N\" TO OV\n").append(st).append(" INTO R").append(n).append(" WITH POINTER P").append(n).append(" ON OVERFLOW MOVE \"Y\" TO OV END-STRING\n");
            proc.append("DISPLAY R").append(n).append(" \"|\" P").append(n).append(" \"|\" OV\n");
        }
        for (int n = 0; n < us.size(); n++) {
            Unstringing u = us.get(n);
            String id = "U" + n;
            data.append("01 ").append(id).append(" PIC X(").append(u.sender().length()).append(") VALUE \"").append(u.sender()).append("\".\n");
            data.append("01 ").append(id).append("P PIC 99 VALUE ").append(u.pointer()).append(".\n01 ").append(id).append("T PIC 99 VALUE ").append(u.tally()).append(".\n");
            StringBuilder st = new StringBuilder("UNSTRING " + id);
            for (int k = 0; k < u.delimiters().size(); k++) st.append(k == 0 ? " DELIMITED BY " : " OR ").append(u.all().get(k) ? "ALL " : "").append('"').append(u.delimiters().get(k)).append('"');
            st.append(" INTO");
            StringBuilder display = new StringBuilder("DISPLAY");
            for (int k = 0; k < u.into().size(); k++) {
                Into i = u.into().get(k);
                String r = id + "R" + k;
                data.append("01 ").append(r).append(i.numeric() ? " PIC 9(" + i.size() + ") VALUE 7" : " PIC X(" + i.size() + ") VALUE ALL \"#\"").append(".\n");
                st.append(' ').append(r);
                display.append(' ').append(r).append(" \"|\"");
                if (!u.delimiters().isEmpty()) {
                    data.append("01 ").append(r).append("D PIC X(2) VALUE \"##\".\n01 ").append(r).append("C PIC 99 VALUE 99.\n");
                    st.append(" DELIMITER IN ").append(r).append("D COUNT IN ").append(r).append('C');
                    display.append(' ').append(r).append("D \"|\" ").append(r).append("C \"|\"");
                }
            }
            proc.append("MOVE \"N\" TO OV\n").append(st).append(" WITH POINTER ").append(id).append("P TALLYING IN ").append(id).append("T ON OVERFLOW MOVE \"Y\" TO OV END-UNSTRING\n");
            proc.append(display).append(' ').append(id).append("P \"|\" ").append(id).append("T \"|\" OV\n");
        }
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. STRINGS.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    static List<String> cobc(List<Stringing> ss, List<Unstringing> us) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-strings"));
        Files.writeString(dir.resolve("STRINGS.cbl"), program(ss, us));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "STRINGS.cbl", List.of("-free"));
        assertEquals(0, compiled.exit(), compiled.output());
        List<String> lines = cobc.execute(dir, "STRINGS", Map.of()).output().lines().toList();
        assertEquals(ss.size() + us.size(), lines.size(), "one line a statement");
        return lines;
    }

    /** `rt/Strings`'s line for a STRING; `delimit` false takes every sender whole (the mutation). */
    static String rt(Stringing s, boolean delimit) {
        byte[] into = bytes("#".repeat(s.size()));
        List<Strings.Sender> senders = new ArrayList<>();
        for (int k = 0; k < s.senders().size(); k++) senders.add(new Strings.Sender(bytes(s.senders().get(k)), delimit && s.delimiters().get(k) != null ? bytes(s.delimiters().get(k)) : null));
        Strings.Result r = Strings.string(senders, into, 0, into.length, s.pointer());
        return str(into) + "|" + String.format("%02d", r.pointer() % 100) + "|" + (r.overflow() ? "Y" : "N");
    }

    /** `rt/Strings`'s line for an UNSTRING; `delimit` false drops ALL (the mutation). */
    static String rt(Unstringing u, boolean delimit) {
        Codepage cp = Codepage.COBC_ASCII;
        List<Strings.Delimiter> delimiters = new ArrayList<>();
        for (int k = 0; k < u.delimiters().size(); k++) delimiters.add(new Strings.Delimiter(bytes(u.delimiters().get(k)), delimit && u.all().get(k)));
        List<Strings.Receiver> receivers = new ArrayList<>();
        for (Into i : u.into()) {
            Attr a = i.numeric() ? Attr.zoned(i.size(), 0, false) : Attr.text(i.size(), false);
            Field f = new Field(Record.of(a.length(), cp), 0, a);
            if (i.numeric()) f.put(new Decimal(BigInteger.valueOf(7), 0));
            else java.util.Arrays.fill(f.record().bytes(), (byte) '#');
            Field d = new Field(new Record(bytes("##"), cp), 0, Attr.text(2, false));
            receivers.add(new Strings.Receiver(f, u.delimiters().isEmpty() ? null : d));
        }
        byte[] from = bytes(u.sender());
        Strings.Unstrung r = Strings.unstring(from, 0, from.length, delimiters, receivers, u.pointer(), u.tally());
        StringBuilder line = new StringBuilder();
        for (int k = 0; k < receivers.size(); k++) {
            line.append(str(receivers.get(k).into().record().bytes())).append('|');
            if (!u.delimiters().isEmpty()) line.append(str(receivers.get(k).delimiterIn().record().bytes())).append('|').append(String.format("%02d", r.counts()[k] < 0 ? 99 : r.counts()[k] % 100)).append('|');
        }
        return line + String.format("%02d", r.result().pointer() % 100) + "|" + String.format("%02d", r.result().tally() % 100) + "|" + (r.result().overflow() ? "Y" : "N");
    }

    static List<String> differ(boolean delimit) throws Exception {
        Random r = new Random(20261010L);
        List<Stringing> ss = stringings(r);
        List<Unstringing> us = unstringings(r);
        List<String> cobc = cobc(ss, us), differ = new ArrayList<>();
        for (int n = 0; n < ss.size(); n++) {
            String rt = rt(ss.get(n), delimit);
            if (!rt.equals(cobc.get(n))) differ.add("STRING " + n + " " + ss.get(n) + ": rt " + rt + ", cobc " + cobc.get(n));
        }
        for (int n = 0; n < us.size(); n++) {
            String rt = rt(us.get(n), delimit);
            if (!rt.equals(cobc.get(ss.size() + n))) differ.add("UNSTRING " + n + " " + us.get(n) + ": rt " + rt + ", cobc " + cobc.get(ss.size() + n));
        }
        return differ;
    }

    @Test
    void rtStringsAsCobcDoes() throws Exception {
        List<String> differ = differ(true);
        assertEquals(List.of(), differ.subList(0, Math.min(15, differ.size())), differ.size() + " of 402 differ");
    }

    /** R-056's mutation: delimiters ignored differs from cobc. */
    @Test
    void ignoringDelimitersDiffers() throws Exception {
        assertTrue(differ(false).size() > 20);
    }
}
