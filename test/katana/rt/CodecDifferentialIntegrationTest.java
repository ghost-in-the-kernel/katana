package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The byte images of numeric fields (phase 3.1, J-102), cobc -std=ibm its oracle: 300 fields from a
 * fixed seed (zoned with every sign position, packed, binary, native binary; signed and unsigned; up to
 * 18 digits), each given a value by MOVE and DISPLAYed as its group's raw bytes. `rt/Field` in the
 * cobc lane's codepage must write the same bytes, and read cobc's bytes back to the same value; a
 * native binary of 1 or 2 digits, which cobc sizes differently from IBM, is left out and counted. Its
 * mutation: the EBCDIC codepage writes different bytes for the same values.
 */
@Tag("integration")
class CodecDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    /** A field's attributes and the value MOVEd into it. */
    record Case(Attr attr, Decimal value) {}

    static List<Case> cases() {
        Random r = new Random(20261006L);
        List<Case> cases = new ArrayList<>();
        Attr.Usage[] usages = Attr.Usage.values();
        for (int n = 0; n < 300; n++) {
            Attr.Usage usage = usages[n % usages.length];
            int digits = 1 + r.nextInt(18), scale = r.nextInt(Math.min(digits, 4) + 1);
            boolean signed = r.nextBoolean(), separate = usage == Attr.Usage.DISPLAY && signed && r.nextBoolean(),
                    leading = usage == Attr.Usage.DISPLAY && signed && r.nextBoolean();
            Attr attr = new Attr(usage, Attr.Category.NUMERIC, digits, scale, signed, separate, leading, false, null);
            StringBuilder s = new StringBuilder();
            for (int k = 0; k < digits; k++) s.append((char) ('0' + (n % 7 == 0 ? 0 : r.nextInt(10))));
            BigInteger unscaled = new BigInteger(s.toString());
            if (signed && r.nextBoolean()) unscaled = unscaled.negate();
            cases.add(new Case(attr, new Decimal(unscaled, scale)));
        }
        return cases;
    }

    private static String picture(Attr a) {
        int integer = a.digits() - a.scale();
        String p = (a.signed() ? "S" : "") + (integer > 0 ? "9(" + integer + ")" : "") + (a.scale() > 0 ? "V9(" + a.scale() + ")" : "");
        String usage = switch (a.usage()) {
            case DISPLAY -> a.signed() && (a.signSeparate() || a.signLeading()) ? " SIGN " + (a.signLeading() ? "LEADING" : "TRAILING") + (a.signSeparate() ? " SEPARATE" : "") : "";
            case PACKED -> " COMP-3";
            case BINARY -> " COMP";
            case NATIVE_BINARY -> " COMP-5";
        };
        return p + usage;
    }

    static String program(List<Case> cases) {
        StringBuilder data = new StringBuilder(), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            data.append("01 G").append(n).append(".\n  05 F").append(n).append(" PIC ").append(picture(c.attr())).append(".\n");
            String literal = c.value().toBigDecimal().toPlainString();
            proc.append("MOVE ").append(literal.startsWith("-.") ? "-0" + literal.substring(1) : literal.startsWith(".") ? "0" + literal : literal)
                    .append(" TO F").append(n).append("\nDISPLAY G").append(n).append('\n');
        }
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. CODEC.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    /**
     * The bytes cobc gives a field: IBM's size, but a native binary (COMP-5) of 1 or 2 digits takes 1
     * byte under cobc where IBM gives every binary of 1 to 4 digits a halfword (J-102; `-fbinary-size`
     * does not change it). A finding against cobc's layout, D-012; `rt/` keeps IBM's (LR p. 238).
     */
    static int cobcLength(Attr a) {
        return a.usage() == Attr.Usage.NATIVE_BINARY && a.digits() <= 2 ? 1 : a.length();
    }

    /** cobc's bytes for each case, read in order by each field's length and its newline. */
    static List<byte[]> cobc(List<Case> cases) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-codec"));
        Files.writeString(dir.resolve("CODEC.cbl"), program(cases));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "CODEC.cbl", List.of("-free"));
        assertEquals(0, compiled.exit(), compiled.output());
        byte[] out = cobc.execute(dir, "CODEC", Map.of()).output().getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> images = new ArrayList<>();
        int at = 0;
        for (Case c : cases) {
            int n = cobcLength(c.attr());
            images.add(java.util.Arrays.copyOfRange(out, at, at + n));
            assertEquals('\n', out[at + n], "a newline after each field's bytes");
            at += n + 1;
        }
        return images;
    }

    /** The cases whose bytes `rt` writes differently from cobc, or reads back to another value. */
    static List<String> differ(List<Case> cases, List<byte[]> images, Codepage codepage) {
        List<String> differ = new ArrayList<>();
        HexFormat hex = HexFormat.of();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            if (cobcLength(c.attr()) != c.attr().length()) continue;
            Field f = new Field(Record.of(c.attr().length(), codepage), 0, c.attr());
            f.put(c.value());
            if (!java.util.Arrays.equals(f.record().bytes(), images.get(n))) differ.add(n + " " + c + ": rt " + hex.formatHex(f.record().bytes()) + ", cobc " + hex.formatHex(images.get(n)));
            Field read = new Field(new Record(images.get(n), codepage), 0, c.attr());
            Decimal back = read.get(), want = c.attr().signed() ? c.value() : new Decimal(c.value().unscaled().abs(), c.value().scale());
            if (!back.equals(want)) differ.add(n + " " + c + ": read back as " + back);
        }
        return differ;
    }

    @Test
    void rtWritesAndReadsCobcsBytes() throws Exception {
        List<Case> cases = cases();
        long sized = cases.stream().filter(c -> cobcLength(c.attr()) != c.attr().length()).count();
        System.out.println("cases " + cases.size() + "; native binaries of 1 or 2 digits, sized by cobc as 1 byte and by IBM as 2, left out: " + sized);
        List<String> differ = differ(cases, cobc(cases), Codepage.COBC_ASCII);
        assertEquals(List.of(), differ.subList(0, Math.min(15, differ.size())), differ.size() + " of " + cases.size() + " differ");
    }

    /** R-056's mutation: the other codepage's bytes are not cobc's. */
    @Test
    void theOtherCodepageDiffers() throws Exception {
        List<Case> cases = cases();
        assertTrue(differ(cases, cobc(cases), Codepage.EBCDIC_1140).size() > 50);
    }
}
