package katana.oracle.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.oracle.fixture.Content;
import katana.oracle.lane.Gate;

/**
 * The comparator (docs/ROADMAP.md 1.7): two datasets as logical record streams, field by field under
 * the copybook. It normalizes only what the encoding rule allows: text encoding (each side decoded by
 * its declared encoding), the sign-zone scheme of zoned numbers, and framing (RDW, line terminators),
 * and nothing it is not told: masks are declared, and each one applied is listed. It refuses to
 * normalize packed and binary bytes, record counts, record order, rounding and truncation: those are
 * compared as they are. Repeats are grouped by field, so one wrong field over 257 records is one
 * finding, with its first record, byte offset, both bytes and both decoded values. Nothing is compared
 * until the gate has passed on the lane the expected side came from. Two EBCDIC zoned values equal but
 * for their sign nibble are not a difference under NUMPROC(NOPFD) and are counted as a `sign-variant`
 * note; under NUMPROC(PFD) they are a difference, because a PFD-compiled consumer tells them apart (R-011).
 */
public final class Compare {
    /** A dataset as records, and the encoding its text and zoned fields are in. */
    public record Side(String dataset, List<byte[]> records, String encoding) {
        /** Fixed-length records, back to back. */
        public static Side fixed(String dataset, byte[] bytes, int lrecl, String encoding) {
            return new Side(dataset, split(bytes, lrecl), encoding);
        }

        /** One record a line: line terminators are framing, not data. */
        public static Side lines(String dataset, byte[] bytes, int lrecl, String encoding) {
            return new Side(dataset, split(Content.records(bytes, "lines", lrecl, null), lrecl), encoding);
        }

        /** Variable-length records, each after its 4-byte RDW (a big-endian length that counts the RDW, then two zero bytes). */
        public static Side rdw(String dataset, byte[] bytes, String encoding) {
            List<byte[]> records = new ArrayList<>();
            for (int at = 0; at < bytes.length; ) {
                int length = ((bytes[at] & 0xff) << 8) | (bytes[at + 1] & 0xff);
                if (length < 4 || at + length > bytes.length) throw new IllegalArgumentException("a bad RDW at byte " + at);
                records.add(Arrays.copyOfRange(bytes, at + 4, at + length));
                at += length;
            }
            return new Side(dataset, records, encoding);
        }

        private static List<byte[]> split(byte[] bytes, int lrecl) {
            List<byte[]> records = new ArrayList<>();
            for (int at = 0; at + lrecl <= bytes.length; at += lrecl) records.add(Arrays.copyOfRange(bytes, at, at + lrecl));
            return records;
        }
    }

    /** The consumer's NUMPROC option, from the profile's `ibm.numproc` (R-049): whether a sign nibble is a difference. */
    public enum Numproc {
        NOPFD, PFD;

        /** `PFD` from the profile's value; NOPFD otherwise (MIG is refused by the profile itself). */
        public static Numproc of(String ibmNumproc) {
            return "PFD".equals(ibmNumproc) ? PFD : NOPFD;
        }
    }

    /** A declared mask: a field path (with its occurrences and every field under it) not compared, and why. */
    public record Mask(String field, String why) {}

    /** Whether the two sides are the same under the rule, each difference grouped by field, each mask applied, and each sign-variant field counted. */
    public record Result(boolean same, List<String> findings, List<String> masks, List<String> notes) {}

    /** The first difference of a field, and how many records differ there. */
    private static final class Group {
        int records;
        String first;
    }

    private Compare() {}

    /** The gate required, the record counts held, each record pair compared, the groups and masks reported. */
    public static Result compare(Gate.Result gate, Side expected, Side actual, Layout layout, List<Mask> masks, Numproc numproc) {
        Gate.require(gate);
        List<String> findings = new ArrayList<>();
        if (expected.records().size() != actual.records().size()) {
            findings.add(expected.dataset() + ": " + expected.records().size() + " records, then " + actual.records().size() + " (record counts are never normalized)");
        }
        Map<String, Group> groups = new LinkedHashMap<>();
        Map<Mask, Integer> applied = new LinkedHashMap<>();
        Map<String, Integer> variants = new LinkedHashMap<>();
        int offset = 0;
        for (int r = 0; r < Math.min(expected.records().size(), actual.records().size()); r++) {
            record(r, offset, expected, actual, layout, masks, numproc, groups, applied, variants);
            offset += expected.records().get(r).length;
        }
        int total = Math.min(expected.records().size(), actual.records().size());
        groups.forEach((field, g) -> findings.add(expected.dataset() + " " + field + ": " + g.records + " of " + total + " records differ; first " + g.first));
        List<String> listed = new ArrayList<>();
        applied.forEach((m, n) -> listed.add("mask " + m.field() + " (" + m.why() + "): applied to " + n + " records"));
        List<String> notes = new ArrayList<>();
        variants.forEach((field, n) -> notes.add("sign-variant " + expected.dataset() + " " + field + ": " + n + " of " + total + " records equal but for the sign nibble (NUMPROC(NOPFD))"));
        return new Result(findings.isEmpty(), findings, listed, notes);
    }

    /** One record pair, field by field; differences into groups, masks counted. */
    private static void record(int r, int offset, Side expected, Side actual, Layout layout, List<Mask> masks, Numproc numproc,
            Map<String, Group> groups, Map<Mask, Integer> applied, Map<String, Integer> variants) {
        byte[] a = expected.records().get(r), b = actual.records().get(r);
        if (a.length != b.length) {
            note(groups, "(record length)", "at record " + (r + 1) + ": " + a.length + " bytes, then " + b.length + " (truncation is never normalized)");
        }
        for (Layout.Field f : layout.fields()) {
            if (f.offset() + f.length() > Math.min(a.length, b.length)) continue;
            Mask mask = masks.stream().filter(m -> f.path().equals(m.field()) || f.path().startsWith(m.field() + ".") || f.path().startsWith(m.field() + "(")).findFirst().orElse(null);
            if (mask != null) {
                applied.merge(mask, 1, Integer::sum);
                continue;
            }
            if (same(f, a, b, expected.encoding(), actual.encoding())) continue;
            if (numproc == Numproc.NOPFD && !expected.encoding().equals("ascii") && expected.encoding().equals(actual.encoding()) && signVariant(f, a, b)) {
                variants.merge(f.path(), 1, Integer::sum);
            } else {
                note(groups, f.path(), "at record " + (r + 1) + ", byte offset " + (offset + f.offset()) + ": " + Decode.hex(Arrays.copyOfRange(a, f.offset(), f.offset() + f.length()))
                        + " (" + Decode.value(f, a, f.offset(), expected.encoding()) + ") then " + Decode.hex(Arrays.copyOfRange(b, f.offset(), f.offset() + f.length()))
                        + " (" + Decode.value(f, b, f.offset(), actual.encoding()) + ")" + (f.kind() == Layout.Kind.PACKED || f.kind() == Layout.Kind.BINARY ? ", compared as bytes" : ""));
            }
        }
        int end = layout.length();
        if (Math.min(a.length, b.length) > end && !Arrays.equals(a, end, Math.min(a.length, b.length), b, end, Math.min(a.length, b.length))) {
            note(groups, "(beyond the copybook)", "at record " + (r + 1) + ", byte offset " + (offset + end));
        }
    }

    /** Text and zoned fields compare by value across encodings; packed and binary ones by their bytes, always. */
    private static boolean same(Layout.Field f, byte[] a, byte[] b, String ea, String eb) {
        boolean bytes = Arrays.equals(a, f.offset(), f.offset() + f.length(), b, f.offset(), f.offset() + f.length());
        if (f.kind() == Layout.Kind.PACKED || f.kind() == Layout.Kind.BINARY || ea.equals(eb)) return bytes;
        String va = Decode.value(f, a, f.offset(), ea), vb = Decode.value(f, b, f.offset(), eb);
        return !va.startsWith("invalid") && va.equals(vb);
    }

    /**
     * EBCDIC zoned bytes equal but for the last byte's zone (its sign), both valid signs, and the same
     * value: the same sign class on a signed field (D and B negative, the rest positive), any sign on an
     * unsigned one (R-011).
     */
    static boolean signVariant(Layout.Field f, byte[] a, byte[] b) {
        if (f.kind() != Layout.Kind.ZONED) return false;
        int last = f.offset() + f.length() - 1;
        if (!Arrays.equals(a, f.offset(), last, b, f.offset(), last) || (a[last] & 0xf) != (b[last] & 0xf)) return false;
        int za = (a[last] >> 4) & 0xf, zb = (b[last] >> 4) & 0xf;
        if (za < 0xa || zb < 0xa) return false;
        return !f.signed() || negative(za) == negative(zb);
    }

    private static boolean negative(int zone) {
        return zone == 0xd || zone == 0xb;
    }

    private static void note(Map<String, Group> groups, String field, String first) {
        Group g = groups.computeIfAbsent(field, k -> new Group());
        if (g.records++ == 0) g.first = first;
    }
}
