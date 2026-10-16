package katana.rt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * A relative file (LR 6.4 p. 147, research §20): record areas numbered from 1, each empty or holding one
 * record; on disk, each record that exists as its relative record number, length and bytes (LANE-15).
 * What it shares with an indexed file is `KeyedFile`'s.
 *
 * The file position indicator is read as the 85 Standard reads it (research §20): after OPEN or START it
 * points at a record, which the next sequential READ returns; after a READ it points past the record
 * read, and the next sequential READ returns the one after.
 */
public final class RelFile extends KeyedFile {
    private final TreeMap<Long, byte[]> areas = new TreeMap<>();
    private boolean pointsAt;
    private long position, written, retrieved = -1;

    /** A file: the name it is ASSIGNed to, the program's name for it, whether it has a FILE STATUS item, whether it is OPTIONAL, its access mode. */
    public RelFile(String name, String file, boolean status, boolean optional, Access access) {
        super(name, file, status, optional, access);
    }

    @Override
    protected void clear() {
        areas.clear();
    }

    @Override
    protected boolean load(DataInputStream in) throws IOException {
        while (in.available() > 0) {
            long number = in.readLong();
            byte[] bytes = new byte[in.readInt()];
            in.readFully(bytes);
            areas.put(number, bytes);
        }
        return true;
    }

    @Override
    protected void save(DataOutputStream out) throws IOException {
        for (Map.Entry<Long, byte[]> a : areas.entrySet()) {
            out.writeLong(a.getKey());
            out.writeInt(a.getValue().length);
            out.write(a.getValue());
        }
    }

    /** INPUT and I-O point at record 1; EXTEND writes after the highest relative record number (p. 410). */
    @Override
    protected void opened(SeqFile.Mode mode) {
        position = 1;
        pointsAt = true;
        written = mode == SeqFile.Mode.EXTEND && !areas.isEmpty() ? areas.lastKey() : 0;
    }

    @Override
    protected void forget() {
        retrieved = -1;
    }

    /**
     * A sequential READ (pp. 427-428): the next record that exists in ascending relative record number, its
     * number into the RELATIVE KEY (`key`, null with none); 10 at the end, 46 after it, 14 when the number
     * has more digits than the key, 47 when not open for input.
     */
    // Decision: R-019
    public void readNext(Field record, Field key) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        forget();
        if (ended) {
            last = "46";
            return;
        }
        Map.Entry<Long, byte[]> next = pointsAt ? areas.ceilingEntry(position) : areas.higherEntry(position);
        if (next == null) {
            ended = true;
            last = "10";
            return;
        }
        if (key != null && digits(next.getKey()) > key.attr().digits()) {
            ended = true;
            last = "14";
            return;
        }
        take(next.getKey(), next.getValue(), record);
        if (key != null) key.put(new Decimal(java.math.BigInteger.valueOf(next.getKey()), 0));
    }

    /** A random READ (p. 428): the record whose number is in the RELATIVE KEY; 23 when there is none, 47 when not open for input. */
    public void read(Field record, Field key) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        forget();
        long number = number(key);
        byte[] bytes = absent ? null : areas.get(number);
        if (bytes == null) {
            last = "23";
            return;
        }
        ended = false;
        take(number, bytes, record);
    }

    /** A record made available (`KeyedFile.take`); the file now points past it. */
    private void take(long number, byte[] bytes, Field record) {
        take(bytes, record);
        position = number;
        pointsAt = false;
        retrieved = number;
    }

    /**
     * WRITE (pp. 475, 478): with sequential access, at the number after the last written, put into the
     * RELATIVE KEY, 24 when it has more digits than the key; with random or dynamic access, at the number in
     * the RELATIVE KEY, 22 when a record is there, 24 when it is not a record number. 48 when the open mode
     * does not allow a WRITE: I-O with sequential access, or INPUT (Table 50).
     */
    // Decision: R-019
    public void write(Field record, Field key) {
        boolean sequential = access == Access.SEQUENTIAL;
        if (sequential ? refused("48", SeqFile.Mode.OUTPUT, SeqFile.Mode.EXTEND) : refused("48", SeqFile.Mode.OUTPUT, SeqFile.Mode.EXTEND, SeqFile.Mode.IO)) return;
        forget();
        long number;
        if (sequential) {
            number = written + 1;
            if (key != null && digits(number) > key.attr().digits()) {
                last = "24";
                return;
            }
            if (key != null) key.put(new Decimal(java.math.BigInteger.valueOf(number), 0));
        } else {
            number = number(key);
            if (number < 1) {
                last = "24";
                return;
            }
            if (areas.containsKey(number)) {
                last = "22";
                return;
            }
        }
        byte[] bytes = bytes(record);
        if (bytes == null) return;
        areas.put(number, bytes);
        written = Math.max(written, number);
        changed = true;
        last = "00";
    }

    /**
     * REWRITE (p. 434): on a file open I-O (else 49), with sequential access the record the last
     * statement, a successful READ, retrieved (else 43); with random or dynamic access the record whose
     * number is in the RELATIVE KEY (23 when there is none). The record may be of another length.
     */
    // Decision: R-019
    public void rewrite(Field record, Field key) {
        long number = target(key);
        if (number < 0) return;
        byte[] bytes = bytes(record);
        if (bytes == null) return;
        areas.put(number, bytes);
        changed = true;
        last = "00";
    }

    /** DELETE (p. 332): the record REWRITE would replace removed, by the same rules; the file position is not changed. */
    // Decision: R-019
    public void delete(Field key) {
        long number = target(key);
        if (number < 0) return;
        areas.remove(number);
        changed = true;
        last = "00";
    }

    /** The record a REWRITE or DELETE names, or -1 with the status set. */
    private long target(Field key) {
        long read = retrieved;
        if (refused("49", SeqFile.Mode.IO)) return -1;
        forget();
        if (access == Access.SEQUENTIAL) {
            if (read < 0) last = "43";
            return read < 0 ? -1 : read;
        }
        long number = number(key);
        if (!areas.containsKey(number)) {
            last = "23";
            return -1;
        }
        return number;
    }

    /**
     * START (p. 456): the file points at the first record whose number satisfies the relation with the
     * RELATIVE KEY's value, compared as numbers; 23 when none does, 47 when not open for input.
     */
    // Decision: R-019
    public void start(Field key, Relation relation) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        forget();
        long number = number(key);
        Long found = absent ? null : switch (relation) {
            case EQUAL -> areas.containsKey(number) ? number : null;
            case GREATER -> areas.higherKey(number);
            case NOT_LESS -> areas.ceilingKey(number);
        };
        if (found == null) {
            last = "23";
            return;
        }
        position = found;
        pointsAt = true;
        ended = false;
        last = "00";
    }

    /** The RELATIVE KEY's value as a record number; 0 with no key. */
    private static long number(Field key) {
        return key == null ? 0 : key.get().unscaled().longValue();
    }

    private static int digits(long n) {
        return Long.toString(n).length();
    }
}
