package katana.rt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * An indexed file (LR 6.4 pp. 146-152, research §21): records identified by their prime key, each key,
 * prime and alternate, an index of the records in its key's binary order, duplicates of an alternate key
 * in the order they were written (p. 150). The key of reference (p. 147) is the index a sequential READ
 * walks: the prime key until a READ with KEY or a START names another. The file position indicator is read
 * as the 85 Standard reads it, as `RelFile`'s is: at a record after OPEN or START, past one after a READ.
 * On disk (LANE-15) the file keeps its keys' positions and lengths and its largest record, which an OPEN
 * compares with the program's for status 39 (p. 301); then each record with its alternate keys' write order.
 * What it shares with a relative file is `KeyedFile`'s.
 */
public final class IdxFile extends KeyedFile {
    /** One key: where it is in the record, how long, whether its values may repeat. */
    private record Key(int offset, int length, boolean duplicates) {}

    /** A record in a key's index: the key's value, its place among equal values, the record's prime key. */
    private record Entry(byte[] value, long order, byte[] prime) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry o) {
            int c = Arrays.compareUnsigned(value, o.value);
            return c != 0 ? c : Long.compare(order, o.order);
        }
    }

    /** A record held: its bytes and, for each alternate key, its place among equal values. */
    private record Held(byte[] bytes, long[] orders) {}

    private Field area;
    private int largest;
    private final List<Key> keys = new ArrayList<>();
    private final TreeMap<byte[], Held> records = new TreeMap<>(Arrays::compareUnsigned);
    private final List<TreeSet<Entry>> indexes = new ArrayList<>();
    private long written;
    private int reference;
    private Entry position;
    private boolean pointsAt;
    private byte[] retrieved, lastWritten;

    /** A file: the name it is ASSIGNed to, the program's name for it, whether it has a FILE STATUS item, whether it is OPTIONAL, its access mode. */
    public IdxFile(String name, String file, boolean status, boolean optional, Access access) {
        super(name, file, status, optional, access);
    }

    /**
     * The file's record area, its largest record, and its keys as fields of the area: the prime RECORD KEY,
     * then each ALTERNATE RECORD KEY with whether it has DUPLICATES (LR pp. 150-152).
     */
    public IdxFile keys(Field area, int largest, Field[] keys, boolean[] duplicates) {
        this.area = area;
        this.largest = largest;
        for (int k = 0; k < keys.length; k++) {
            this.keys.add(new Key(keys[k].offset() - area.offset(), keys[k].attr().length(), duplicates[k]));
            indexes.add(new TreeSet<>());
        }
        return this;
    }

    /** A key's value in a record's bytes, spaces past its end. */
    private byte[] value(int key, byte[] bytes) {
        Key k = keys.get(key);
        byte[] v = new byte[k.length()];
        Arrays.fill(v, area.record().codepage().character(' '));
        if (k.offset() < bytes.length) System.arraycopy(bytes, k.offset(), v, 0, Math.min(k.length(), bytes.length - k.offset()));
        return v;
    }

    /** A key's value in the record area now. */
    private byte[] value(int key) {
        return value(key, Arrays.copyOfRange(area.record().bytes(), area.offset(), area.record().bytes().length));
    }

    @Override
    protected void clear() {
        records.clear();
        for (TreeSet<Entry> index : indexes) index.clear();
        written = 0;
    }

    private void hold(byte[] bytes, long[] orders) {
        byte[] prime = value(0, bytes);
        records.put(prime, new Held(bytes, orders));
        indexes.get(0).add(new Entry(prime, 0, prime));
        for (int k = 1; k < keys.size(); k++) indexes.get(k).add(new Entry(value(k, bytes), orders[k - 1], prime));
        for (long o : orders) written = Math.max(written, o);
    }

    private void drop(byte[] prime) {
        Held h = records.remove(prime);
        indexes.get(0).remove(new Entry(prime, 0, prime));
        for (int k = 1; k < keys.size(); k++) indexes.get(k).remove(new Entry(value(k, h.bytes()), h.orders()[k - 1], prime));
    }

    /** The kept keys and largest record, then the records; false when the keys or the size are not the program's. */
    @Override
    protected boolean load(DataInputStream in) throws IOException {
        int count = in.readInt();
        boolean same = count == keys.size();
        for (int k = 0; k < count; k++) {
            Key kept = new Key(in.readInt(), in.readInt(), in.readBoolean());
            same &= k < keys.size() && kept.equals(keys.get(k));
        }
        same &= in.readInt() == largest;
        if (!same) return false;
        while (in.available() > 0) {
            byte[] bytes = new byte[in.readInt()];
            in.readFully(bytes);
            long[] orders = new long[keys.size() - 1];
            for (int k = 0; k < orders.length; k++) orders[k] = in.readLong();
            hold(bytes, orders);
        }
        return true;
    }

    @Override
    protected void save(DataOutputStream out) throws IOException {
        out.writeInt(keys.size());
        for (Key k : keys) {
            out.writeInt(k.offset());
            out.writeInt(k.length());
            out.writeBoolean(k.duplicates());
        }
        out.writeInt(largest);
        for (Held h : records.values()) {
            out.writeInt(h.bytes().length);
            out.write(h.bytes());
            for (long o : h.orders()) out.writeLong(o);
        }
    }

    /** INPUT and I-O point at the lowest prime key (p. 410); a sequential WRITE after EXTEND must pass the highest. */
    @Override
    protected void opened(SeqFile.Mode mode) {
        reference = 0;
        position = null;
        pointsAt = true;
        lastWritten = mode == SeqFile.Mode.EXTEND && !records.isEmpty() ? records.lastKey() : null;
    }

    @Override
    protected void forget() {
        retrieved = null;
    }

    /**
     * A sequential READ (pp. 427-428): the next record in the key of reference's order; 10 at the end, 46
     * after it, 47 when not open for input; 02 when the next record has the same alternate key (p. 300).
     */
    // Decision: R-019
    public void readNext(Field record) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        forget();
        if (ended) {
            last = "46";
            return;
        }
        TreeSet<Entry> index = indexes.get(reference);
        Entry next = position == null ? (index.isEmpty() ? null : index.first()) : pointsAt ? index.ceiling(position) : index.higher(position);
        if (next == null) {
            ended = true;
            last = "10";
            return;
        }
        take(next, record);
    }

    /**
     * A random READ (p. 428): the first record written whose key equals the key data item's value, the
     * prime key unless the KEY phrase names another (`key`, a field of the record area); it becomes the key
     * of reference. 23 when there is none, 47 when not open for input.
     */
    // Decision: R-019
    public void read(Field record, Field key) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        forget();
        int k = key(key);
        byte[] value = value(k);
        Entry found = absent ? null : indexes.get(k).ceiling(new Entry(value, Long.MIN_VALUE, value));
        if (found == null || !Arrays.equals(found.value(), value)) {
            last = "23";
            return;
        }
        reference = k;
        ended = false;
        take(found, record);
    }

    /** A record made available (`KeyedFile.take`); the file now points past it; 02 when the next in the key of reference has its value. */
    private void take(Entry e, Field record) {
        take(records.get(e.prime()).bytes(), record);
        position = e;
        pointsAt = false;
        retrieved = e.prime();
        Entry after = indexes.get(reference).higher(e);
        if (last.equals("00") && reference > 0 && after != null && Arrays.equals(after.value(), e.value())) last = "02";
    }

    /** The key whose leftmost position is the item's: the KEY phrase's data-name, or a leftmost part of a key (p. 456); the prime key with none. */
    private int key(Field item) {
        if (item == null) return 0;
        int at = item.offset() - area.offset();
        for (int k = 0; k < keys.size(); k++) if (keys.get(k).offset() == at) return k;
        return 0;
    }

    /**
     * WRITE (pp. 475, 477): the record at its prime key; 22 when that key or an alternate key without
     * DUPLICATES is already in the file; with sequential access, 21 when the prime key is not above the last
     * written; 02 when an alternate key with DUPLICATES now repeats; 48 when the open mode does not allow it.
     */
    // Decision: R-019
    public void write(Field record) {
        boolean sequential = access == Access.SEQUENTIAL;
        if (sequential ? refused("48", SeqFile.Mode.OUTPUT, SeqFile.Mode.EXTEND) : refused("48", SeqFile.Mode.OUTPUT, SeqFile.Mode.EXTEND, SeqFile.Mode.IO)) return;
        forget();
        byte[] bytes = bytes(record);
        if (bytes == null) return;
        byte[] prime = value(0, bytes);
        if (sequential && lastWritten != null && Arrays.compareUnsigned(prime, lastWritten) <= 0) {
            last = "21";
            return;
        }
        if (records.containsKey(prime) || clash(bytes, null)) {
            last = "22";
            return;
        }
        long[] orders = new long[keys.size() - 1];
        for (int k = 0; k < orders.length; k++) orders[k] = ++written;
        hold(bytes, orders);
        if (sequential) lastWritten = prime;
        changed = true;
        last = repeats(bytes) ? "02" : "00";
    }

    /** Whether an alternate key without DUPLICATES has the record's value in another record than `self`. */
    private boolean clash(byte[] bytes, byte[] self) {
        for (int k = 1; k < keys.size(); k++) {
            if (keys.get(k).duplicates()) continue;
            byte[] v = value(k, bytes);
            Entry e = indexes.get(k).ceiling(new Entry(v, Long.MIN_VALUE, v));
            if (e != null && Arrays.equals(e.value(), v) && (self == null || !Arrays.equals(e.prime(), self))) return true;
        }
        return false;
    }

    /** Whether an alternate key with DUPLICATES has the record's value in more than one record. */
    private boolean repeats(byte[] bytes) {
        for (int k = 1; k < keys.size(); k++) {
            if (!keys.get(k).duplicates()) continue;
            byte[] v = value(k, bytes);
            Entry e = indexes.get(k).ceiling(new Entry(v, Long.MIN_VALUE, v));
            Entry next = e == null ? null : indexes.get(k).higher(e);
            if (next != null && Arrays.equals(next.value(), v)) return true;
        }
        return false;
    }

    /**
     * REWRITE (p. 433): on a file open I-O (else 49), the record with the prime key in the record area; with
     * sequential access it must be the last record read (43 without a successful READ, 21 for another key);
     * else 23 when the file has none. 22 when an alternate key without DUPLICATES would repeat; an alternate
     * key whose value changes goes last among its equal values; 02 as for WRITE.
     */
    // Decision: R-019
    public void rewrite(Field record) {
        byte[] prime = target();
        if (prime == null) return;
        byte[] bytes = bytes(record);
        if (bytes == null) return;
        if (clash(bytes, prime)) {
            last = "22";
            return;
        }
        Held old = records.get(prime);
        long[] orders = old.orders().clone();
        for (int k = 1; k < keys.size(); k++) if (!Arrays.equals(value(k, old.bytes()), value(k, bytes))) orders[k - 1] = ++written;
        drop(prime);
        hold(bytes, orders);
        changed = true;
        last = repeats(bytes) ? "02" : "00";
    }

    /** DELETE (p. 332): the record REWRITE would replace removed, by the same rules; the file position is not changed. */
    // Decision: R-019
    public void delete() {
        byte[] prime = target();
        if (prime == null) return;
        drop(prime);
        changed = true;
        last = "00";
    }

    /** The prime key a REWRITE or DELETE names, or null with the status set. */
    private byte[] target() {
        byte[] read = retrieved;
        if (refused("49", SeqFile.Mode.IO)) return null;
        forget();
        byte[] prime = value(0);
        if (access == Access.SEQUENTIAL) {
            if (read == null) last = "43";
            else if (!Arrays.equals(read, prime)) last = "21";
            return read == null || !Arrays.equals(read, prime) ? null : prime;
        }
        if (!records.containsKey(prime)) {
            last = "23";
            return null;
        }
        return prime;
    }

    /**
     * START (pp. 455-456): the key whose leftmost position is `item`'s becomes the key of reference, and the
     * file points at the first record whose key, cut to the shorter of the two lengths, satisfies the
     * relation with the item's value, compared byte by byte; 23 when none does, 47 when not open for input.
     */
    // Decision: R-019
    public void start(Field item, Relation relation) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        forget();
        int k = key(item);
        byte[] value = item == null ? value(0) : Arrays.copyOfRange(item.record().bytes(), item.offset(), item.offset() + item.attr().length());
        int n = Math.min(value.length, keys.get(k).length());
        byte[] cut = Arrays.copyOf(value, n);
        Entry found = null;
        if (!absent) {
            for (Entry e : indexes.get(k)) {
                int c = Arrays.compareUnsigned(Arrays.copyOf(e.value(), n), cut);
                if (relation == Relation.EQUAL ? c == 0 : relation == Relation.GREATER ? c > 0 : c >= 0) {
                    found = e;
                    break;
                }
            }
        }
        if (found == null) {
            last = "23";
            return;
        }
        reference = k;
        position = found;
        pointsAt = true;
        ended = false;
        last = "00";
    }
}
