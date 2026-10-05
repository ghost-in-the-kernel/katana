package katana.oracle.sort;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Katana's DFSORT (R-010): a SORT step's cards over its input records, giving SORTOUT's records. Records
 * are selected, sorted stably on the keys (R-026), and rebuilt by OUTREC; a record shorter than SORTOUT's
 * record length is padded on the right with X'00', a longer one truncated (R-051), with the return code
 * PAD and TRUNC give. What the lane chose rather than the cards (the order of equal keys without EQUALS,
 * a padded or truncated record) is a finding that does not stop the step; a card it does not run is one
 * that does.
 */
public final class Dfsort {
    /** DFSORT's PAD and TRUNC options: RC0, RC4 or RC16 (R-051). */
    public record Rules(String pad, String trunc) {
        public static final Rules DEFAULT = new Rules("RC0", "RC0");
    }

    /** What the step did: SORTOUT's records, how many were read and kept, its return code, the findings that let it run, and the one that stopped it (null when it ran). */
    public record Result(byte[] records, int read, int kept, int rc, List<String> notes, String stop) {}

    private Dfsort() {}

    public static Result run(List<String> symnames, List<String> sysin, List<byte[]> input, int lrecl, Rules rules) {
        try {
            SortCards.Spec spec = SortCards.read(symnames, sysin);
            List<byte[]> kept = new ArrayList<>();
            for (byte[] r : input) if (spec.select() == null || spec.select().test(r) != spec.omit()) kept.add(r);
            Comparator<byte[]> order = order(spec.keys());
            kept.sort(order);
            List<String> notes = new ArrayList<>();
            if (!spec.equals()) ties(kept, order, notes);
            return output(spec, input.size(), kept, lrecl, rules, notes);
        } catch (SortCards.Unsupported e) {
            return new Result(new byte[0], input.size(), 0, 16, List.of(), e.getMessage());
        }
    }

    /** The keys in order: CH by unsigned bytes, ZD by value; D reverses one. */
    private static Comparator<byte[]> order(List<SortCards.Key> keys) {
        Comparator<byte[]> order = (a, b) -> 0;
        for (SortCards.Key key : keys) {
            SortCards.Field f = key.field();
            Comparator<byte[]> one = f.format().equals("ZD") ? Comparator.comparing((byte[] r) -> Zoned.value(r, f))
                    : (a, b) -> Arrays.compareUnsigned(a, f.position() - 1, f.position() - 1 + f.length(), b, f.position() - 1, f.position() - 1 + f.length());
            order = order.thenComparing(key.ascending() ? one : one.reversed());
        }
        return order;
    }

    /** R-026: without EQUALS, the runs of equal keys whose order is the lane's, as SORTOUT record ranges. */
    private static void ties(List<byte[]> sorted, Comparator<byte[]> order, List<String> notes) {
        List<String> ranges = new ArrayList<>();
        int tied = 0;
        for (int start = 0; start < sorted.size(); ) {
            int end = start;
            while (end + 1 < sorted.size() && order.compare(sorted.get(start), sorted.get(end + 1)) == 0) end++;
            if (end > start) {
                ranges.add((start + 1) + "-" + (end + 1));
                tied += end - start + 1;
            }
            start = end + 1;
        }
        if (!ranges.isEmpty()) {
            notes.add(tied + " records in " + ranges.size() + " runs of equal keys, and no EQUALS: their order is the lane's, not"
                    + " the program's (R-026); SORTOUT records " + String.join(", ", ranges));
        }
    }

    /** OUTREC applied and each record fitted to SORTOUT's length (R-051): padded with X'00' or truncated, or the step stopped under RC16. */
    private static Result output(SortCards.Spec spec, int read, List<byte[]> kept, int lrecl, Rules rules, List<String> notes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int padded = 0, truncated = 0, built = -1;
        for (byte[] r : kept) {
            byte[] record = spec.outrec() == null ? r : SortOutrec.build(spec.outrec(), r);
            built = record.length;
            if (record.length < lrecl) padded++;
            if (record.length > lrecl) truncated++;
            out.writeBytes(Arrays.copyOf(record, lrecl));
        }
        int rc = 0;
        for (String[] fit : List.of(new String[] {"PAD", rules.pad(), "padded on the right with X'00'", String.valueOf(padded)},
                new String[] {"TRUNC", rules.trunc(), "truncated on the right", String.valueOf(truncated)})) {
            if (fit[3].equals("0")) continue;
            if (fit[1].equals("RC16")) return new Result(new byte[0], read, kept.size(), 16, notes,
                    "records of " + built + " bytes for SORTOUT's lrecl " + lrecl + " under " + fit[0] + "=RC16: DFSORT stops (ICE196A, R-051)");
            if (fit[1].equals("RC4")) rc = 4;
            notes.add("records of " + built + " bytes for SORTOUT's lrecl " + lrecl + ": " + fit[3] + " " + fit[2] + " (ICE171I, "
                    + fit[0] + "=" + fit[1] + "; R-051, under two hypotheses: the rule holds for reformatted records, and RC0 is IBM's"
                    + " shipped default)");
        }
        return new Result(out.toByteArray(), read, kept.size(), rc, notes, null);
    }
}
