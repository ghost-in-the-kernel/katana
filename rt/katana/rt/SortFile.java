package katana.rt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A sort file (an SD) as SORT and MERGE use it (LR 6.4 pp. 396, 429-430, 447-453; research §23): each
 * record released (by RELEASE, or read from a USING file into the SD's record area) is kept with its keys'
 * values taken from the record area at that moment; `sort` orders them by the keys in their order of
 * significance, ASCENDING or DESCENDING, a numeric key by its value and any other by its bytes' weights in
 * the collating sequence; RETURN, and GIVING through it, take them back in order. The sort is stable, so
 * equal keys keep the order they were released in: the DUPLICATES order, which IBM leaves undefined
 * without the phrase (R-026).
 */
public final class SortFile {
    /** A key of the SD's record: the field over its record area, whether DESCENDING. */
    public record Key(Field field, boolean descending) {}

    private record Held(byte[] bytes, Object[] keys) {}

    private final List<Held> records = new ArrayList<>();
    private List<Key> keys = List.of();
    private Collation collation = Collation.nativeOrder();
    private int next, length;

    /** A SORT or MERGE begins: no records, these keys, this collating sequence for its alphanumeric keys. */
    public void begin(List<Key> keys, Collation collation) {
        this.keys = List.copyOf(keys);
        this.collation = collation;
        records.clear();
        next = 0;
    }

    /** RELEASE: the record area's `length` bytes from its start kept, with its keys as they are now (LR p. 429). */
    public void release(Field area, int length) {
        Object[] values = new Object[keys.size()];
        for (int k = 0; k < values.length; k++) {
            Field f = keys.get(k).field();
            values[k] = f.attr().category() == Attr.Category.NUMERIC ? f.get() : weights(f);
        }
        records.add(new Held(Arrays.copyOfRange(area.record().bytes(), area.offset(), area.offset() + length), values));
    }

    private int[] weights(Field f) {
        int[] w = new int[f.attr().length()];
        byte[] b = f.record().bytes();
        for (int i = 0; i < w.length; i++) w[i] = collation.weights()[b[f.offset() + i] & 0xFF];
        return w;
    }

    /** The records ordered by the keys, most significant first; equal keys in the order released (a stable sort, R-026). */
    // Decision: R-026
    public void sort() {
        Comparator<Held> order = (a, b) -> 0;
        for (int k = 0; k < keys.size(); k++) {
            int at = k;
            boolean descending = keys.get(k).descending();
            Comparator<Held> one = (a, b) -> compare(a.keys()[at], b.keys()[at]);
            order = order.thenComparing(descending ? one.reversed() : one);
        }
        records.sort(order);
        next = 0;
    }

    private static int compare(Object a, Object b) {
        if (a instanceof Decimal x && b instanceof Decimal y) return compareDecimal(x, y);
        return Arrays.compare((int[]) a, (int[]) b);
    }

    private static int compareDecimal(Decimal x, Decimal y) {
        int scale = Math.max(x.scale(), y.scale());
        BigInteger a = x.unscaled().multiply(BigInteger.TEN.pow(scale - x.scale())), b = y.unscaled().multiply(BigInteger.TEN.pow(scale - y.scale()));
        return a.compareTo(b);
    }

    /** The record just returned as a sending item of its own length, for a GIVING file's WRITE (LR p. 453). */
    public Field current(Field area) {
        return new Field(area.record(), area.offset(), Attr.text(Math.min(length, area.record().bytes().length - area.offset()), false));
    }

    /** The sorted records from the first again, for the next GIVING file (LR p. 453: each output file receives them all). */
    public void rewind() {
        next = 0;
    }

    /**
     * RETURN: the next record into the record area, as many bytes as it holds, the rest of the area kept
     * (LR p. 430: "undefined"; as a variable-length READ keeps it, R-060); false when there is none.
     */
    // Decision: R-060
    public boolean returnNext(Field area) {
        if (next >= records.size()) return false;
        byte[] bytes = records.get(next++).bytes();
        length = bytes.length;
        byte[] dst = area.record().bytes();
        System.arraycopy(bytes, 0, dst, area.offset(), Math.min(bytes.length, dst.length - area.offset()));
        return true;
    }
}
