package katana.rt;

import java.util.Arrays;
import java.util.List;

/**
 * INSPECT (phase 3.1), as IBM's Language Reference 6.4 gives it (pp. 353-360), over an item's bytes
 * (an alphanumeric view of it, LR Table 39): TALLYING and REPLACING by comparison cycles, the phrases
 * tried left to right at each position, the first that matches taking its comparand's length and the
 * scan moving past it; CHARACTERS, ALL, LEADING, FIRST; BEFORE and AFTER INITIAL; CONVERTING as a
 * series of ALL phrases, one a character. TALLYING then REPLACING in one statement is the two run one
 * after the other (p. 357), which the lowering writes as two calls.
 */
public final class Inspect {
    private Inspect() {}

    /** What a phrase compares: every character, every occurrence, the occurrences leading its region, the first. */
    public enum Kind { CHARACTERS, ALL, LEADING, FIRST }

    /**
     * A TALLYING or REPLACING phrase: its kind, its comparand (null for CHARACTERS), the substitution
     * (REPLACING only: as long as the comparand, one character for CHARACTERS), and its BEFORE and AFTER
     * INITIAL delimiters (null when absent).
     */
    public record Phrase(Kind kind, byte[] subject, byte[] by, byte[] before, byte[] after) {
        int width() {
            return kind == Kind.CHARACTERS ? 1 : subject.length;
        }
    }

    /** A phrase's region: from AFTER's delimiter's end to BEFORE's delimiter, each delimiter the first in the whole item. */
    // Decision: R-019
    // Unsure: with both phrases IBM's text does not say where BEFORE's delimiter is searched from; this is cobc's reading, from the item's start, not from AFTER's (J-105).
    static int[] region(byte[] data, int off, int len, Phrase p) {
        int start = off, end = off + len;
        if (p.after() != null) {
            int at = find(data, off, end, p.after());
            start = at < 0 ? end : at + p.after().length;
        }
        if (p.before() != null) {
            int at = find(data, off, off + len, p.before());
            if (at >= 0) end = at;
        }
        return new int[] {start, Math.max(start, end)};
    }

    private static int find(byte[] data, int from, int end, byte[] what) {
        for (int i = from; i + what.length <= end; i++) if (matches(data, i, what)) return i;
        return -1;
    }

    private static boolean matches(byte[] data, int at, byte[] what) {
        for (int k = 0; k < what.length; k++) if (data[at + k] != what[k]) return false;
        return true;
    }

    /** INSPECT ... TALLYING: each phrase's count, in order (the lowering adds each to its count field). */
    // Decision: R-019
    public static long[] tally(byte[] data, int off, int len, List<Phrase> phrases) {
        long[] counts = new long[phrases.size()];
        cycles(data, off, len, phrases, (k, at) -> counts[k]++);
        return counts;
    }

    /** INSPECT ... REPLACING: each matched comparand replaced by its phrase's substitution, no position twice. */
    // Decision: R-019
    public static void replace(byte[] data, int off, int len, List<Phrase> phrases) {
        cycles(data, off, len, phrases, (k, at) -> {
            Phrase p = phrases.get(k);
            System.arraycopy(p.by(), 0, data, at, p.width());
        });
    }

    /** INSPECT ... CONVERTING: each character of `from` within the region becomes the same position's character of `to`. */
    // Decision: R-019
    public static void convert(byte[] data, int off, int len, byte[] from, byte[] to, byte[] before, byte[] after) {
        int[] r = region(data, off, len, new Phrase(Kind.CHARACTERS, null, null, before, after));
        for (int i = r[0]; i < r[1]; i++) {
            for (int k = 0; k < from.length; k++) {
                if (data[i] == from[k]) {
                    data[i] = to[k];
                    break;
                }
            }
        }
    }

    private interface Matched { void at(int phrase, int position); }

    /**
     * The comparison cycles (LR p. 360, "Data flow"): from the leftmost position, the phrases compared in
     * the order written; a LEADING phrase stays eligible only while its matches are contiguous from its
     * region's start, a FIRST phrase until it has matched once. Regions are found before anything is replaced.
     */
    private static void cycles(byte[] data, int off, int len, List<Phrase> phrases, Matched matched) {
        int n = phrases.size();
        int[][] regions = new int[n][];
        int[] leadingAt = new int[n];
        boolean[] done = new boolean[n];
        for (int k = 0; k < n; k++) {
            regions[k] = region(data, off, len, phrases.get(k));
            leadingAt[k] = regions[k][0];
        }
        byte[] original = Arrays.copyOfRange(data, 0, data.length);
        int p = off;
        while (p < off + len) {
            int hit = -1;
            for (int k = 0; k < n && hit < 0; k++) {
                if (done[k] || p < regions[k][0] || p >= regions[k][1]) continue;
                Phrase ph = phrases.get(k);
                if (ph.kind() == Kind.LEADING && p != leadingAt[k]) {
                    done[k] = true;
                    continue;
                }
                boolean fits = p + ph.width() <= regions[k][1];
                boolean match = fits && (ph.kind() == Kind.CHARACTERS || matches(original, p, ph.subject()));
                if (match) hit = k;
                else if (ph.kind() == Kind.LEADING) done[k] = true;
            }
            if (hit < 0) {
                p++;
                continue;
            }
            Phrase ph = phrases.get(hit);
            matched.at(hit, p);
            if (ph.kind() == Kind.FIRST) done[hit] = true;
            p += ph.width();
            if (ph.kind() == Kind.LEADING) leadingAt[hit] = p;
        }
    }
}
