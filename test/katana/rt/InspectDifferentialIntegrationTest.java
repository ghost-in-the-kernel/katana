package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * INSPECT (phase 3.1), cobc -std=ibm its oracle: 400 statements from a fixed seed and two written to separate two readings over short strings of
 * a small alphabet (so comparands meet, overlap and lead): TALLYING, REPLACING, both in one statement,
 * and CONVERTING, one to three phrases of every kind with BEFORE and AFTER INITIAL delimiters. Each
 * statement's item and counts are DISPLAYed; `rt/Inspect` must give the same. IBM's own examples are
 * `InspectTest`. Its mutation: LEADING compared as ALL differs.
 */
@Tag("integration")
class InspectDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String ALPHABET = "AAB.-";

    /** A statement: the item's value, its TALLYING phrases, its REPLACING phrases, or a CONVERTING (from, to, before, after). */
    record Case(String value, List<Inspect.Phrase> tallying, List<Inspect.Phrase> replacing, String[] converting) {}

    static List<Case> cases() {
        Random r = new Random(20261009L);
        List<Case> cases = new ArrayList<>();
        for (int n = 0; n < 400; n++) {
            String value = text(r, 4 + r.nextInt(14), ALPHABET);
            int form = n % 4;
            if (form == 3) {
                String from = distinct(r, 1 + r.nextInt(3)), to = text(r, from.length(), "xyz");
                cases.add(new Case(value, List.of(), List.of(), new String[] {from, to, delimiter(r), delimiter(r)}));
                continue;
            }
            List<Inspect.Phrase> tallying = form == 1 ? List.of() : phrases(r, false);
            List<Inspect.Phrase> replacing = form == 0 ? List.of() : phrases(r, true);
            cases.add(new Case(value, tallying, replacing, null));
        }
        // Two that separate IBM's comparison cycles from a phrase-at-a-time reading (J-105): IBM gives zzyyyy.
        Inspect.Phrase afterB = new Inspect.Phrase(Inspect.Kind.CHARACTERS, null, bytes("y"), null, bytes("B"));
        for (Inspect.Kind kind : List.of(Inspect.Kind.FIRST, Inspect.Kind.ALL))
            cases.add(new Case("BA-ABA", List.of(), List.of(afterB, new Inspect.Phrase(kind, bytes("BA"), bytes("zz"), null, null)), null));
        return cases;
    }

    private static List<Inspect.Phrase> phrases(Random r, boolean replacing) {
        List<Inspect.Phrase> out = new ArrayList<>();
        int count = 1 + r.nextInt(3);
        for (int k = 0; k < count; k++) {
            Inspect.Kind kind = Inspect.Kind.values()[r.nextInt(replacing ? 4 : 3)];
            String subject = kind == Inspect.Kind.CHARACTERS ? null : text(r, 1 + r.nextInt(2), ALPHABET);
            String by = replacing ? text(r, subject == null ? 1 : subject.length(), "xyz") : null;
            out.add(new Inspect.Phrase(kind, bytes(subject), bytes(by), bytes(delimiter(r)), bytes(delimiter(r))));
        }
        return out;
    }

    private static String delimiter(Random r) {
        return r.nextInt(3) == 0 ? text(r, 1 + r.nextInt(2), ALPHABET) : null;
    }

    private static String distinct(Random r, int n) {
        StringBuilder s = new StringBuilder();
        while (s.length() < n) {
            char c = "AB.-".charAt(r.nextInt(4));
            if (s.indexOf(String.valueOf(c)) < 0) s.append(c);
        }
        return s.toString();
    }

    private static String text(Random r, int length, String alphabet) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < length; k++) s.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return s.toString();
    }

    private static byte[] bytes(String s) {
        return s == null ? null : s.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String str(byte[] b) {
        return new String(b, StandardCharsets.ISO_8859_1);
    }

    /** A phrase as COBOL writes it, `counter FOR ...` when tallying. */
    private static String phrase(Inspect.Phrase p, String counter) {
        StringBuilder s = new StringBuilder(counter == null ? "" : counter + " FOR ");
        s.append(p.kind());
        if (p.subject() != null) s.append(" \"").append(str(p.subject())).append('"');
        if (p.by() != null) s.append(" BY \"").append(str(p.by())).append('"');
        if (p.before() != null) s.append(" BEFORE INITIAL \"").append(str(p.before())).append('"');
        if (p.after() != null) s.append(" AFTER INITIAL \"").append(str(p.after())).append('"');
        return s.toString();
    }

    static String program(List<Case> cases) {
        StringBuilder data = new StringBuilder(), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            data.append("01 D").append(n).append(" PIC X(").append(c.value().length()).append(").\n");
            proc.append("MOVE \"").append(c.value()).append("\" TO D").append(n).append('\n');
            StringBuilder display = new StringBuilder("DISPLAY D" + n);
            StringBuilder inspect = new StringBuilder("INSPECT D" + n);
            if (!c.tallying().isEmpty()) inspect.append(" TALLYING");
            for (int k = 0; k < c.tallying().size(); k++) {
                String counter = "C" + n + "-" + k;
                data.append("01 ").append(counter).append(" PIC 9(4) VALUE 0.\n");
                inspect.append("\n  ").append(phrase(c.tallying().get(k), counter));
                display.append(" \"|\" ").append(counter);
            }
            if (!c.replacing().isEmpty()) inspect.append(" REPLACING");
            for (Inspect.Phrase p : c.replacing()) inspect.append("\n  ").append(phrase(p, null));
            if (c.converting() != null) {
                String[] v = c.converting();
                inspect.append(" CONVERTING \"").append(v[0]).append("\" TO \"").append(v[1]).append('"');
                if (v[2] != null) inspect.append(" BEFORE INITIAL \"").append(v[2]).append('"');
                if (v[3] != null) inspect.append(" AFTER INITIAL \"").append(v[3]).append('"');
            }
            proc.append(inspect).append('\n').append(display).append('\n');
        }
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. INSPECTS.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    static List<String> cobc(List<Case> cases) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-inspect"));
        Files.writeString(dir.resolve("INSPECTS.cbl"), program(cases));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "INSPECTS.cbl", List.of("-free"));
        assertEquals(0, compiled.exit(), compiled.output());
        List<String> lines = cobc.execute(dir, "INSPECTS", Map.of()).output().lines().toList();
        assertEquals(cases.size(), lines.size(), "one line a case");
        return lines;
    }

    /** `rt/Inspect`'s line for a case: the item after, then each count; `leading` false compares LEADING as ALL (the mutation). */
    static String rt(Case c, boolean leading) {
        byte[] d = bytes(c.value());
        List<Inspect.Phrase> tallying = leading ? c.tallying() : asAll(c.tallying()), replacing = leading ? c.replacing() : asAll(c.replacing());
        long[] counts = tallying.isEmpty() ? new long[0] : Inspect.tally(d, 0, d.length, tallying);
        if (!replacing.isEmpty()) Inspect.replace(d, 0, d.length, replacing);
        if (c.converting() != null) {
            String[] v = c.converting();
            Inspect.convert(d, 0, d.length, bytes(v[0]), bytes(v[1]), bytes(v[2]), bytes(v[3]));
        }
        StringBuilder line = new StringBuilder(str(d));
        for (long k : counts) line.append('|').append(String.format("%04d", k));
        return line.toString();
    }

    private static List<Inspect.Phrase> asAll(List<Inspect.Phrase> phrases) {
        return phrases.stream().map(p -> p.kind() == Inspect.Kind.LEADING ? new Inspect.Phrase(Inspect.Kind.ALL, p.subject(), p.by(), p.before(), p.after()) : p).toList();
    }

    static List<String> differ(List<Case> cases, List<String> cobc, boolean leading) {
        List<String> differ = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            String rt = rt(cases.get(n), leading);
            if (!rt.equals(cobc.get(n))) differ.add(n + " " + cases.get(n).value() + " " + describe(cases.get(n)) + ": rt " + rt + ", cobc " + cobc.get(n));
        }
        return differ;
    }

    private static String describe(Case c) {
        if (c.converting() != null) return "CONVERTING " + Arrays.toString(c.converting());
        return "T" + c.tallying().stream().map(p -> phrase(p, "")).toList() + " R" + c.replacing().stream().map(p -> phrase(p, null)).toList();
    }

    /**
     * cobc's INSPECT as it shows itself (J-105): each phrase run alone in written order over its region,
     * a position one phrase has claimed barred to every later one, instead of IBM's comparison cycles
     * (LR p. 360: position by position, the phrases compared at each in written order). Written here only
     * to check that each listed finding is that and nothing else.
     */
    static String phraseAtATime(Case c) {
        byte[] d = bytes(c.value());
        long[] counts = new long[c.tallying().size()];
        claim(d, c.tallying(), (k, at) -> counts[k]++);
        claim(d, c.replacing(), (k, at) -> System.arraycopy(c.replacing().get(k).by(), 0, d, at, c.replacing().get(k).width()));
        StringBuilder line = new StringBuilder(str(d));
        for (long k : counts) line.append('|').append(String.format("%04d", k));
        return line.toString();
    }

    private interface Claimed { void at(int phrase, int position); }

    private static void claim(byte[] d, List<Inspect.Phrase> phrases, Claimed claimed) {
        byte[] original = d.clone();
        boolean[] taken = new boolean[d.length];
        for (int k = 0; k < phrases.size(); k++) {
            Inspect.Phrase p = phrases.get(k);
            int[] r = Inspect.region(original, 0, d.length, p);
            int w = p.width();
            for (int at = r[0]; at + w <= r[1]; ) {
                boolean free = true;
                for (int j = at; j < at + w; j++) free &= !taken[j];
                boolean match = free && (p.kind() == Inspect.Kind.CHARACTERS || Arrays.equals(Arrays.copyOfRange(original, at, at + w), p.subject()));
                if (!match) {
                    if (p.kind() == Inspect.Kind.LEADING) break;
                    at++;
                    continue;
                }
                claimed.at(k, at);
                Arrays.fill(taken, at, at + w, true);
                at += w;
                if (p.kind() == Inspect.Kind.FIRST) break;
            }
        }
    }

    /** D-008: the cases where cobc's phrase-at-a-time INSPECT gives another answer than IBM's comparison cycles, each checked to be exactly that. */
    static final List<Integer> CYCLE_FINDINGS = List.of(134, 400, 401);

    @Test
    void rtInspectsAsCobcDoes() throws Exception {
        List<Case> cases = cases();
        List<String> cobc = cobc(cases), differ = new ArrayList<>();
        List<Integer> findings = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            String rt = rt(c, true);
            if (rt.equals(cobc.get(n))) continue;
            if (c.converting() == null && phraseAtATime(c).equals(cobc.get(n))) findings.add(n);
            else differ.add(n + " " + c.value() + " " + describe(c) + ": rt " + rt + ", cobc " + cobc.get(n));
        }
        long misfit = java.util.stream.IntStream.range(0, cases.size()).filter(n -> cases.get(n).converting() == null && !phraseAtATime(cases.get(n)).equals(cobc.get(n))).count();
        System.out.println("phrase-at-a-time findings " + findings + "; cases where that reading misses cobc " + misfit);
        assertEquals(List.of(), differ.subList(0, Math.min(15, differ.size())), differ.size() + " of " + cases.size() + " differ");
        assertEquals(0, misfit, "the phrase-at-a-time reading describes cobc on every case");
        assertEquals(CYCLE_FINDINGS, findings, "the cases where cobc runs INSPECT's phrases one at a time");
    }

    /** R-056's mutation: LEADING compared as ALL differs from cobc. */
    @Test
    void leadingAsAllDiffers() throws Exception {
        List<Case> cases = cases();
        assertTrue(differ(cases, cobc(cases), false).size() > 10);
    }
}
