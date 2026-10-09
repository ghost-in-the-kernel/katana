package katana.rt;

import java.util.Arrays;
import java.util.List;

/**
 * STRING and UNSTRING (phase 3.1), as IBM's Language Reference 6.4 gives them (pp. 457-471), over
 * alphanumeric bytes. STRING: each sender transferred whole (SIZE) or up to its delimiter, character by
 * character from the pointer, nothing space-filled, overflow when the pointer starts or runs outside the
 * receiver. UNSTRING: the sender examined from the pointer up to a delimiter (or a receiver's size when
 * there is none), each part MOVEd into its receiver, with its delimiter, its count, the pointer and the
 * tally; overflow when the pointer starts outside the sender or the receivers run out first.
 */
public final class Strings {
    private Strings() {}

    /** A STRING sender: its bytes and its delimiter (null for DELIMITED BY SIZE). */
    public record Sender(byte[] data, byte[] delimiter) {}

    /** What a STRING or UNSTRING leaves: the pointer, the tally (UNSTRING), whether it overflowed. */
    public record Result(long pointer, long tally, boolean overflow) {}

    /** STRING ... INTO the receiver's `len` bytes at `off`, from `pointer` (1 when there is no POINTER phrase). */
    // Decision: R-019
    public static Result string(List<Sender> senders, byte[] into, int off, int len, long pointer) {
        if (pointer < 1 || pointer > len) return new Result(pointer, 0, true);
        long p = pointer;
        for (Sender s : senders) {
            int n = s.delimiter() == null ? s.data().length : indexOf(s.data(), 0, s.data().length, s.delimiter());
            if (n < 0) n = s.data().length;
            for (int k = 0; k < n; k++) {
                if (p > len) return new Result(p, 0, true);
                into[off + (int) p - 1] = s.data()[k];
                p++;
            }
        }
        return new Result(p, 0, false);
    }

    /** An UNSTRING delimiter: its characters, and ALL (contiguous occurrences taken as one). */
    public record Delimiter(byte[] text, boolean all) {}

    /** An UNSTRING receiver: the data receiving field, and its DELIMITER IN field (or null); its COUNT IN is in the result's counts. */
    public record Receiver(Field into, Field delimiterIn) {}

    /** UNSTRING's result: the pointer, tally and overflow, and each receiver's count of examined characters (-1 for one not acted upon). */
    public record Unstrung(Result result, long[] counts) {}

    /** UNSTRING the sender's `len` bytes at `off` from `pointer`, into the receivers in order; `tally` the TALLYING field's value before. */
    // Decision: R-019
    public static Unstrung unstring(byte[] from, int off, int len, List<Delimiter> delimiters, List<Receiver> receivers, long pointer, long tally) {
        long[] counts = new long[receivers.size()];
        Arrays.fill(counts, -1);
        if (pointer < 1 || pointer > len) return new Unstrung(new Result(pointer, tally, true), counts);
        int at = off + (int) pointer - 1, end = off + len;
        long acted = tally;
        for (int r = 0; r < receivers.size() && at < end; r++) {
            Receiver rc = receivers.get(r);
            int stop, next;
            byte[] found = null;
            if (delimiters.isEmpty()) {
                stop = Math.min(end, at + examined(rc.into().attr()));
                next = stop;
            } else {
                int[] hit = nextDelimiter(from, at, end, delimiters);
                stop = hit[0];
                next = hit[1];
                if (hit[2] >= 0) found = delimiters.get(hit[2]).text();
            }
            Move.move(text(from, at, stop - at, rc.into().record().codepage()), rc.into());
            if (rc.delimiterIn() != null) {
                byte[] d = found == null ? new byte[0] : found;
                Move.move(text(d, 0, d.length, rc.delimiterIn().record().codepage()), rc.delimiterIn());
            }
            counts[r] = stop - at;
            acted++;
            at = next;
        }
        return new Unstrung(new Result(at - off + 1, acted, at < end), counts);
    }

    /** Characters examined for a receiver when there is no DELIMITED BY (LR Table 54): a numeric item's integer places, else its size. */
    private static int examined(Attr a) {
        return a.category() == Attr.Category.NUMERIC ? a.digits() - a.scale() : a.length();
    }

    /**
     * The next delimiter from `at`: where the data stops, where examination resumes, and which delimiter
     * (-1 at the end of the sender). The delimiters are tried in the order written at each position; ALL
     * takes further contiguous occurrences of the same delimiter as one.
     */
    // Unsure: IBM's "multiple contiguous occurrences of any delimiters" could read as any of the delimiters; this is cobc's reading, the same one (J-106). Stays: an IBM run of UNSTRING over adjacent different delimiters settles it.
    private static int[] nextDelimiter(byte[] from, int at, int end, List<Delimiter> delimiters) {
        for (int p = at; p < end; p++) {
            for (int k = 0; k < delimiters.size(); k++) {
                byte[] d = delimiters.get(k).text();
                if (!startsWith(from, p, end, d)) continue;
                int next = p + d.length;
                while (delimiters.get(k).all() && startsWith(from, next, end, d)) next += d.length;
                return new int[] {p, next, k};
            }
        }
        return new int[] {end, end, -1};
    }

    private static Field text(byte[] bytes, int at, int n, Codepage cp) {
        return new Field(new Record(Arrays.copyOfRange(bytes, at, at + n), cp), 0, Attr.text(n, false));
    }

    private static boolean startsWith(byte[] data, int at, int end, byte[] what) {
        if (at + what.length > end) return false;
        for (int k = 0; k < what.length; k++) if (data[at + k] != what[k]) return false;
        return true;
    }

    private static int indexOf(byte[] data, int from, int end, byte[] what) {
        for (int i = from; i + what.length <= end; i++) if (startsWith(data, i, end, what)) return i;
        return -1;
    }
}
