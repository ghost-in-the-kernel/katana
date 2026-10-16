package katana.rt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * A relative file (LR 6.4 p. 147, research §20): record areas numbered from 1, each empty or holding one
 * record, kept in memory while the file is open and written back when it closes, as relative record
 * number, length and bytes (the java lane's own form; only the java lane reads it). The name it is
 * ASSIGNed to resolves as `SeqFile`'s does. Each statement sets the status IBM's Table 34 gives it and
 * never throws for one; the lowering moves it to the FILE STATUS item and runs the INVALID KEY or AT END
 * phrase, a declarative, or the abend.
 *
 * The file position indicator is read as the 85 Standard reads it (research §20): after OPEN or START it
 * points at a record, which the next sequential READ returns; after a READ it points past the record
 * read, and the next sequential READ returns the one after.
 */
public final class RelFile {
    /** How the program's file-control entry lets it reach records (LR p. 150). */
    public enum Access { SEQUENTIAL, RANDOM, DYNAMIC }

    private final String name;
    private final String file;
    private final boolean status;
    private final boolean optional;
    private final Access access;
    private final TreeMap<Long, byte[]> areas = new TreeMap<>();
    private SeqFile.Mode mode, opening;
    private boolean locked, atEnd, absent, pointsAt, changed;
    private long position, written, retrieved = -1;
    private String last = "00";
    private Varying varying;
    private int readLength;

    /** A file: the name it is ASSIGNed to, the program's name for it, whether it has a FILE STATUS item, whether it is OPTIONAL, its access mode. */
    public RelFile(String name, String file, boolean status, boolean optional, Access access) {
        this.name = name;
        this.file = file;
        this.status = status;
        this.optional = optional;
        this.access = access;
    }

    /** The last statement's file status, two characters (LR p. 299). */
    public String status() {
        return last;
    }

    /** Whether the last statement succeeded (status 0x). */
    public boolean successful() {
        return last.charAt(0) == '0';
    }

    /** Whether the last statement found an invalid key condition (status 2x, LR p. 303). */
    public boolean invalid() {
        return last.charAt(0) == '2';
    }

    /** Whether the last statement found an at-end condition (status 1x, LR p. 300). */
    public boolean atEnd() {
        return last.charAt(0) == '1';
    }

    /**
     * Whether the last statement calls for its error handling: a status 3x or 4x, an at-end condition with
     * no AT END phrase, or an invalid key condition with no INVALID KEY phrase (LR pp. 303-304, 705-706).
     */
    public boolean failed(boolean phrase) {
        return last.charAt(0) >= '3' || (atEnd() || invalid()) && !phrase;
    }

    /** The open mode a mode declarative looks at, as `SeqFile.mode`. */
    public SeqFile.Mode mode() {
        return mode != null ? mode : opening;
    }

    /** Whether the file has a FILE STATUS item. */
    public boolean hasStatus() {
        return status;
    }

    /** The abend that ends the run when an error has neither a FILE STATUS item nor a declarative, as `SeqFile.abend`. */
    public Abend abend(String verb) {
        return new Abend(4038, verb + " " + file + ": file status " + last + " on " + name);
    }

    /** A variable-length file (LR 6.4 pp. 186-188, research §13), as `SeqFile.varying`: `Varying` gives a WRITE's length and bounds and sets DEPENDING ON at a READ. */
    public RelFile varying(int least, int greatest, boolean bounded, int min, int max, Field depending) {
        this.varying = new Varying(least, greatest, bounded, min, max, depending);
        return this;
    }

    /** The record just read as a sending item, its own length: READ INTO's group move from a variable file (pp. 188, 425). */
    public Field current(Field record) {
        return new Field(record.record(), record.offset(), Attr.text(Math.min(readLength, record.record().bytes().length - record.offset()), false));
    }

    /** The bytes a WRITE or REWRITE puts in a record area, or null with 44 set when outside RECORD IS VARYING's bounds. */
    private byte[] bytes(Field record) {
        if (varying == null) return Literal.bytes(record);
        int length = varying.length(record);
        String outside = varying.outside(length);
        if (outside != null) {
            last = outside;
            return null;
        }
        return varying.bytes(record, length);
    }

    private Path path() {
        for (String key : new String[] {"DD_" + name, "dd_" + name}) if (System.getenv(key) != null) return Path.of(System.getenv(key));
        return Path.of(name);
    }

    /**
     * OPEN (LR pp. 300-302, 410): 41 if already open, 38 after CLOSE WITH LOCK; for INPUT, I-O or EXTEND an
     * absent file is 35, or 05 when OPTIONAL; OUTPUT empties the file. INPUT and I-O point at record 1;
     * EXTEND writes after the highest relative record number.
     */
    // Decision: R-019
    public void open(SeqFile.Mode mode) {
        opening = mode;
        retrieved = -1;
        if (this.mode != null) {
            last = "41";
            return;
        }
        if (locked) {
            last = "38";
            return;
        }
        boolean exists = Files.exists(path());
        if (!exists && mode != SeqFile.Mode.OUTPUT && !optional) {
            last = "35";
            return;
        }
        areas.clear();
        if (exists && mode != SeqFile.Mode.OUTPUT) load();
        absent = !exists && mode == SeqFile.Mode.INPUT;
        changed = mode != SeqFile.Mode.INPUT && !exists || mode == SeqFile.Mode.OUTPUT;
        atEnd = false;
        position = 1;
        pointsAt = true;
        written = mode == SeqFile.Mode.EXTEND && !areas.isEmpty() ? areas.lastKey() : 0;
        this.mode = mode;
        opening = null;
        last = exists || mode == SeqFile.Mode.OUTPUT ? "00" : "05";
    }

    private void load() {
        try (DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(Files.newInputStream(path())))) {
            while (in.available() > 0) {
                long number = in.readLong();
                byte[] bytes = new byte[in.readInt()];
                in.readFully(bytes);
                areas.put(number, bytes);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** OPEN ... WITH NO REWIND: not valid for a relative file, so 07 as on any non-reel medium (p. 300). */
    public void noRewind() {
        if (last.equals("00")) last = "07";
    }

    /** The statement's check of the open mode: true, with `refused` as the status, when the mode does not allow it (Table 50, p. 412). */
    private boolean refused(String refused, SeqFile.Mode... allowed) {
        opening = null;
        for (SeqFile.Mode m : allowed) if (mode == m) return false;
        last = refused;
        retrieved = -1;
        return true;
    }

    /**
     * A sequential READ (pp. 427-428): the next record that exists in ascending relative record number, its
     * number into the RELATIVE KEY (`key`, null with none); 10 at the end, 46 after it, 14 when the number
     * has more digits than the key, 47 when not open for input.
     */
    // Decision: R-019
    public void readNext(Field record, Field key) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        retrieved = -1;
        if (atEnd) {
            last = "46";
            return;
        }
        Map.Entry<Long, byte[]> next = pointsAt ? areas.ceilingEntry(position) : areas.higherEntry(position);
        if (next == null) {
            atEnd = true;
            last = "10";
            return;
        }
        if (key != null && digits(next.getKey()) > key.attr().digits()) {
            atEnd = true;
            last = "14";
            return;
        }
        take(next.getKey(), next.getValue(), record);
        if (key != null) key.put(new Decimal(java.math.BigInteger.valueOf(next.getKey()), 0));
    }

    /** A random READ (p. 428): the record whose number is in the RELATIVE KEY; 23 when there is none, 47 when not open for input. */
    public void read(Field record, Field key) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        retrieved = -1;
        long number = number(key);
        byte[] bytes = absent ? null : areas.get(number);
        if (bytes == null) {
            last = "23";
            return;
        }
        atEnd = false;
        take(number, bytes, record);
    }

    /**
     * A record made available: into the record area, as long as it was written, the area past it kept as a
     * sequential file's is (R-060); its length into DEPENDING ON, 04 when no 01 record is that long; the file
     * now points past it.
     */
    private void take(long number, byte[] bytes, Field record) {
        byte[] dst = record.record().bytes();
        System.arraycopy(bytes, 0, dst, record.offset(), Math.min(bytes.length, dst.length - record.offset()));
        position = number;
        pointsAt = false;
        retrieved = number;
        readLength = bytes.length;
        last = "00";
        if (varying != null) {
            varying.took(bytes.length);
            last = varying.status(bytes.length);
        }
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
        retrieved = -1;
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
        retrieved = -1;
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

    /** The relations of START's KEY phrase (p. 456); EQUAL without one. */
    public enum Relation { EQUAL, GREATER, NOT_LESS }

    /**
     * START (p. 456): the file points at the first record whose number satisfies the relation with the
     * RELATIVE KEY's value, compared as numbers; 23 when none does, 47 when not open for input.
     */
    // Decision: R-019
    public void start(Field key, Relation relation) {
        if (refused("47", SeqFile.Mode.INPUT, SeqFile.Mode.IO)) return;
        retrieved = -1;
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
        atEnd = false;
        last = "00";
    }

    /**
     * CLOSE [WITH LOCK]: 42 on a file not open (p. 302), else 00 and the records written back; REEL/UNIT,
     * FOR REMOVAL and NO REWIND are 07 on this non-reel medium (p. 300), REEL/UNIT leaving the file open.
     */
    public void close(boolean lock, boolean reel, boolean noRewind) {
        opening = null;
        retrieved = -1;
        if (mode == null) {
            last = "42";
            return;
        }
        last = reel || noRewind ? "07" : "00";
        if (reel) return;
        close();
        locked = lock;
    }

    /** The run unit's end closing what is open (PG p. 179), with no status: the records written back when they changed. */
    public void close() {
        if (mode == null) return;
        if (changed) {
            try (DataOutputStream out = new DataOutputStream(new java.io.BufferedOutputStream(Files.newOutputStream(path())))) {
                for (Map.Entry<Long, byte[]> a : areas.entrySet()) {
                    out.writeLong(a.getKey());
                    out.writeInt(a.getValue().length);
                    out.write(a.getValue());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        areas.clear();
        mode = null;
        changed = false;
    }

    /** The RELATIVE KEY's value as a record number; 0 with no key. */
    private static long number(Field key) {
        return key == null ? 0 : key.get().unscaled().longValue();
    }

    private static int digits(long n) {
        return Long.toString(n).length();
    }
}
