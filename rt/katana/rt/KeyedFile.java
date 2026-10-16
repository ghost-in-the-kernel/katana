package katana.rt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * What a relative file (`RelFile`) and an indexed file (`IdxFile`) share: the name it is ASSIGNed to,
 * resolved as `SeqFile`'s is; the records held in memory while the file is open and written back when it
 * closes, in the java lane's own form (LANE-15); the status each statement sets, as IBM's Table 34 gives
 * it (LR 6.4 pp. 300-302), never thrown; the open modes Table 50 allows each statement (p. 412); and the
 * INVALID KEY and AT END conditions the lowering acts on (pp. 303-304).
 */
public abstract class KeyedFile {
    /** How the program's file-control entry lets it reach records (LR p. 150). */
    public enum Access { SEQUENTIAL, RANDOM, DYNAMIC }

    /** The relations of START's KEY phrase (p. 455); EQUAL without one. */
    public enum Relation { EQUAL, GREATER, NOT_LESS }

    private final String name;
    private final String file;
    private final boolean status;
    private final boolean optional;
    protected final Access access;
    protected SeqFile.Mode mode;
    private SeqFile.Mode opening;
    private boolean locked;
    protected boolean ended, absent, changed;
    protected String last = "00";
    protected Varying varying;
    protected int readLength;

    protected KeyedFile(String name, String file, boolean status, boolean optional, Access access) {
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
    public KeyedFile varying(int least, int greatest, boolean bounded, int min, int max, Field depending) {
        this.varying = new Varying(least, greatest, bounded, min, max, depending);
        return this;
    }

    /** The record just read as a sending item, its own length: READ INTO's group move from a variable file (pp. 188, 425). */
    public Field current(Field record) {
        return new Field(record.record(), record.offset(), Attr.text(Math.min(readLength, record.record().bytes().length - record.offset()), false));
    }

    /** The bytes a WRITE or REWRITE puts in the file, or null with 44 set when outside RECORD IS VARYING's bounds. */
    protected byte[] bytes(Field record) {
        if (varying == null) return Literal.bytes(record);
        int length = varying.length(record);
        String outside = varying.outside(length);
        if (outside != null) {
            last = outside;
            return null;
        }
        return varying.bytes(record, length);
    }

    /**
     * A record made available: into the record area, as long as it was written, the area past it kept as a
     * sequential file's is (R-060); its length into DEPENDING ON, 04 when no 01 record is that long.
     */
    // Decision: R-060
    protected void take(byte[] bytes, Field record) {
        byte[] dst = record.record().bytes();
        System.arraycopy(bytes, 0, dst, record.offset(), Math.min(bytes.length, dst.length - record.offset()));
        readLength = bytes.length;
        last = "00";
        if (varying != null) {
            varying.took(bytes.length);
            last = varying.status(bytes.length);
        }
    }

    private Path path() {
        for (String key : new String[] {"DD_" + name, "dd_" + name}) if (System.getenv(key) != null) return Path.of(System.getenv(key));
        return Path.of(name);
    }

    /**
     * OPEN (LR pp. 300-302, 410): 41 if already open, 38 after CLOSE WITH LOCK; for INPUT, I-O or EXTEND an
     * absent file is 35, or 05 when OPTIONAL; OUTPUT empties the file; a file whose kept attributes are not
     * the program's is 39 (`load`). Where the file then points is the subclass's (`opened`).
     */
    // Decision: R-019
    public void open(SeqFile.Mode mode) {
        opening = mode;
        forget();
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
        clear();
        if (exists && mode != SeqFile.Mode.OUTPUT) {
            try (DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(Files.newInputStream(path())))) {
                if (!load(in)) {
                    clear();
                    last = "39";
                    return;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        absent = !exists && mode == SeqFile.Mode.INPUT;
        changed = mode != SeqFile.Mode.INPUT && !exists || mode == SeqFile.Mode.OUTPUT;
        ended = false;
        this.mode = mode;
        opening = null;
        opened(mode);
        last = exists || mode == SeqFile.Mode.OUTPUT ? "00" : "05";
    }

    /** OPEN ... WITH NO REWIND: not valid for this file, so 07 as on any non-reel medium (p. 300). */
    public void noRewind() {
        if (last.equals("00")) last = "07";
    }

    /** The statement's check of the open mode: true, with `refused` as the status, when the mode does not allow it (Table 50, p. 412). */
    protected boolean refused(String refused, SeqFile.Mode... allowed) {
        opening = null;
        for (SeqFile.Mode m : allowed) if (mode == m) return false;
        last = refused;
        forget();
        return true;
    }

    /**
     * CLOSE [WITH LOCK]: 42 on a file not open (p. 302), else 00 and the records written back; REEL/UNIT,
     * FOR REMOVAL and NO REWIND are 07 on this non-reel medium (p. 300), REEL/UNIT leaving the file open.
     */
    public void close(boolean lock, boolean reel, boolean noRewind) {
        opening = null;
        forget();
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
                save(out);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        clear();
        mode = null;
        changed = false;
    }

    /** The records forgotten, the file closed or about to be read in. */
    protected abstract void clear();

    /** The file's records read in; false when its kept attributes are not the program's (39). */
    protected abstract boolean load(DataInputStream in) throws IOException;

    /** The file's records written out, in the form `load` reads. */
    protected abstract void save(DataOutputStream out) throws IOException;

    /** Where a file just opened in `mode` points. */
    protected abstract void opened(SeqFile.Mode mode);

    /** The record a sequential REWRITE or DELETE would name forgotten: the last statement was not a successful READ. */
    protected abstract void forget();
}
