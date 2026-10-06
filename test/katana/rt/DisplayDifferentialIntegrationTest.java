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
 * DISPLAY of numeric items (phase 3.1), cobc -std=ibm with the lane profile's binary truncation its
 * oracle: 300 items from a fixed seed, every usage, signed or not, the sign leading or trailing,
 * overpunched or separate, each given a value and DISPLAYed. `rt/Display` under DISPSIGN(SEP), the IBM
 * form nearest cobc's, must give the same, apart from two ways cobc parts from IBM's Table 48, each
 * checked against a model of cobc's display: a binary item shown with the digits of its binary field
 * rather than its PICTURE, and an overpunched zoned item's sign shown separate where the overpunch was
 * rather than leading. Its mutation: DISPSIGN(COMPAT) differs.
 */
@Tag("integration")
class DisplayDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    /** An item and its value. */
    record Case(Attr attr, String value) {}

    static List<Case> cases() {
        Random r = new Random(20261013L);
        List<Case> cases = new ArrayList<>();
        for (int n = 0; n < 300; n++) {
            Attr.Usage usage = Attr.Usage.values()[n % 4];
            int digits = (usage == Attr.Usage.NATIVE_BINARY ? 3 : 1) + r.nextInt(9), scale = r.nextInt(Math.min(digits, 3) + 1);
            boolean signed = r.nextBoolean(), display = usage == Attr.Usage.DISPLAY;
            Attr a = new Attr(usage, Attr.Category.NUMERIC, digits, scale, signed, display && signed && r.nextBoolean(), display && signed && r.nextBoolean(), false, null);
            StringBuilder s = new StringBuilder();
            for (int k = 0; k < digits; k++) s.append((char) ('0' + r.nextInt(10)));
            BigInteger unscaled = new BigInteger(s.toString());
            if (signed && r.nextBoolean()) unscaled = unscaled.negate();
            String plain = new Decimal(unscaled, scale).toBigDecimal().toPlainString();
            cases.add(new Case(a, plain.startsWith("-.") ? "-0" + plain.substring(1) : plain.startsWith(".") ? "0" + plain : plain));
        }
        return cases;
    }

    private static String picture(Attr a) {
        int integer = a.digits() - a.scale();
        String p = (a.signed() ? "S" : "") + (integer > 0 ? "9(" + integer + ")" : "") + (a.scale() > 0 ? "V9(" + a.scale() + ")" : "");
        return p + switch (a.usage()) {
            case DISPLAY -> a.signed() && (a.signSeparate() || a.signLeading()) ? " SIGN " + (a.signLeading() ? "LEADING" : "TRAILING") + (a.signSeparate() ? " SEPARATE" : "") : "";
            case PACKED -> " COMP-3";
            case BINARY -> " COMP";
            case NATIVE_BINARY -> " COMP-5";
        };
    }

    static List<String> cobc(List<Case> cases) throws Exception {
        StringBuilder data = new StringBuilder(), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            data.append("01 F").append(n).append(" PIC ").append(picture(cases.get(n).attr())).append(".\n");
            proc.append("MOVE ").append(cases.get(n).value()).append(" TO F").append(n).append("\nDISPLAY F").append(n).append('\n');
        }
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-display"));
        Files.writeString(dir.resolve("SHOWS.cbl"), "IDENTIFICATION DIVISION.\nPROGRAM-ID. SHOWS.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n");
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "SHOWS.cbl", List.of("-free", "-fbinary-truncate"));
        assertEquals(0, compiled.exit(), compiled.output());
        List<String> lines = cobc.execute(dir, "SHOWS", Map.of()).output().lines().toList();
        assertEquals(cases.size(), lines.size(), "one line a case");
        return lines;
    }

    private static Field field(Case c) {
        Field f = new Field(Record.of(c.attr().length(), Codepage.COBC_ASCII), 0, c.attr());
        f.put(Decimal.of(c.value()));
        return f;
    }

    static String rt(Case c, Display.Sign sign) {
        return new String(Display.of(field(c), sign), StandardCharsets.ISO_8859_1);
    }

    /** cobc's display as it shows itself (J-109): a binary item with its binary field's digits, an overpunched zoned item's sign separate in the overpunch's place. */
    static String cobcReading(Case c) {
        Attr a = c.attr();
        Decimal v = field(c).get();
        boolean negative = v.unscaled().signum() < 0;
        String sign = a.signed() ? (negative ? "-" : "+") : "";
        if (a.usage() == Attr.Usage.DISPLAY) {
            String digits = String.format("%0" + a.digits() + "d", v.unscaled().abs());
            return a.signLeading() ? sign + digits : digits + sign;
        }
        int width = a.usage() == Attr.Usage.PACKED ? a.digits() : switch (a.length()) {
            case 2 -> 5;
            case 4 -> 10;
            default -> 20;
        };
        return sign + String.format("%0" + width + "d", v.unscaled().abs());
    }

    /** How many cases fall in each way cobc parts from Table 48 (each checked against `cobcReading`), so a change shows: D-004 and D-005. */
    static final Map<String, Integer> FINDINGS = Map.of("binary digits by capacity", 150, "zoned sign in place", 5);

    @Test
    void rtDisplaysAsCobcDoes() throws Exception {
        List<Case> cases = cases();
        List<String> cobc = cobc(cases), differ = new ArrayList<>();
        Map<String, List<Integer>> findings = new java.util.TreeMap<>();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            if (rt(c, Display.Sign.SEP).equals(cobc.get(n))) continue;
            String family = c.attr().usage() == Attr.Usage.DISPLAY ? "zoned sign in place" : "binary digits by capacity";
            if (cobcReading(c).equals(cobc.get(n))) findings.computeIfAbsent(family, k -> new ArrayList<>()).add(n);
            else differ.add(n + " " + c + ": rt " + rt(c, Display.Sign.SEP) + ", cobc " + cobc.get(n));
        }
        Map<String, Integer> counts = new java.util.TreeMap<>();
        findings.forEach((k, v) -> counts.put(k, v.size()));
        System.out.println(counts + " " + findings);
        assertEquals(List.of(), differ.subList(0, Math.min(15, differ.size())), differ.size() + " of " + cases.size() + " differ");
        assertEquals(new java.util.TreeMap<>(FINDINGS), counts, "the cases where cobc parts from IBM's Table 48");
    }

    /** R-056's mutation: DISPSIGN(COMPAT) differs from cobc, which is D-003: cobc's ibm dialect does not overpunch as IBM's default does. */
    @Test
    void compatDiffers() throws Exception {
        List<Case> cases = cases();
        List<String> cobc = cobc(cases);
        long differ = java.util.stream.IntStream.range(0, cases.size()).filter(n -> !rt(cases.get(n), Display.Sign.COMPAT).equals(cobc.get(n))).count();
        assertTrue(differ > 50);
    }
}
