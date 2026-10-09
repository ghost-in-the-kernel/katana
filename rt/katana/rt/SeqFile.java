package katana.rt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A sequential file, enough for phase 3's NIST reports (phase 4.1 makes it whole): the name a program
 * ASSIGNs to resolved as the cobc lane resolves it (`DD_name`, then `dd_name`, then the name itself);
 * WRITE with ADVANCING as cobc writes a print line (line feeds before AFTER, after BEFORE, a form feed for
 * PAGE); READ of fixed records, or of lines for LINE SEQUENTIAL. Each statement sets the file status
 * IBM's Table 34 gives it (LR 6.4 pp. 300-302, research §12) and never throws for one; the lowering
 * moves it to the FILE STATUS item and decides what follows (a declarative, the run going on, or an
 * abend).
 */
public final class SeqFile {
    public enum Mode { INPUT, OUTPUT, EXTEND, IO }

    private final String name;
    private final String file;
    private final boolean lines;
    private final boolean status;
    private final boolean optional;
    private OutputStream out;
    private InputStream in;
    private java.io.RandomAccessFile io;
    private long readAt = -1;
    private int readLength;
    private Mode mode, opening;
    private boolean locked, atEnd, absent;
    private String last = "00";
    private Field counter;
    private java.util.function.IntSupplier[] linage;
    private int body, footing, bottom;

    /** A file: the name it is ASSIGNed to, the program's name for it, whether it has a FILE STATUS item, whether it is OPTIONAL. */
    public SeqFile(String name, String file, boolean lineSequential, boolean status, boolean optional) {
        this.name = name;
        this.file = file;
        this.lines = lineSequential;
        this.status = status;
        this.optional = optional;
    }

    /** The last statement's file status, two characters (LR p. 299). */
    public String status() {
        return last;
    }

    /** Whether the last statement succeeded (status 0x). */
    public boolean successful() {
        return last.charAt(0) == '0';
    }

    /** Whether the last statement calls for its error handling: a status 3x or 4x, or at end (10) where the statement has no AT END phrase (LR pp. 705-706). */
    public boolean failed(boolean atEndPhrase) {
        return last.charAt(0) >= '3' || last.equals("10") && !atEndPhrase;
    }

    /**
     * The open mode a mode declarative looks at: the mode the file is open in, or, for an OPEN that
     * failed, the mode it was "in the process of being opened in" (LR p. 706); null for a closed file,
     * so a CLOSE of one is no mode's error (research §12, S2(a)).
     */
    public Mode mode() {
        return mode != null ? mode : opening;
    }

    /** Whether the file has a FILE STATUS item. */
    public boolean hasStatus() {
        return status;
    }

    /** The abend that ends the run when an error has neither a FILE STATUS item nor a declarative (PG 6.4 p. 243; J-144). */
    // Unsure: U4038 is Fable's recollection of LE's abend for an unhandled severity-3 condition in batch, not yet quoted (HANDOFF, J-145).
    public Abend abend(String verb) {
        return new Abend(4038, verb + " " + file + ": file status " + last + " on " + name);
    }

    private Path path() {
        for (String key : new String[] {"DD_" + name, "dd_" + name}) if (System.getenv(key) != null) return Path.of(System.getenv(key));
        return Path.of(name);
    }

    /**
     * OPEN (LR pp. 300-302): 41 if already open, 38 after CLOSE WITH LOCK; for INPUT, I-O or EXTEND an
     * absent file is 35, or 05 when OPTIONAL (INPUT then reads nothing, the others create it); 00 else.
     * A failed OPEN leaves the file closed (S1(a)), its mode kept for a mode declarative.
     */
    // Decision: R-019
    public void open(Mode mode) {
        opening = mode;
        if (this.mode != null) {
            last = "41";
            return;
        }
        if (locked) {
            last = "38";
            return;
        }
        boolean exists = Files.exists(path());
        if (!exists && mode != Mode.OUTPUT && !optional) {
            last = "35";
            return;
        }
        try {
            absent = !exists && mode == Mode.INPUT;
            atEnd = false;
            readAt = -1;
            if (mode == Mode.IO) io = new java.io.RandomAccessFile(path().toFile(), "rw");
            else if (mode == Mode.INPUT) in = absent ? InputStream.nullInputStream() : new BufferedInputStream(Files.newInputStream(path()));
            else out = new BufferedOutputStream(mode == Mode.EXTEND ? Files.newOutputStream(path(), StandardOpenOption.CREATE, StandardOpenOption.APPEND) : Files.newOutputStream(path()));
            this.mode = mode;
            opening = null;
            last = exists || mode == Mode.OUTPUT ? "00" : "05";
            if (linage != null && mode != Mode.INPUT) {
                blank(page());
                count(1);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * A LINAGE file (LR 6.4 pp. 189-190): its LINAGE-COUNTER, and the page body, first footing line, top
     * and bottom margins, each read at OPEN and again at each new page, as a data-name's are (J-149's
     * trial, L3); footing null is the page body, a margin null is zero.
     */
    // Decision: R-019
    public SeqFile linage(Field counter, java.util.function.IntSupplier body, java.util.function.IntSupplier footing, java.util.function.IntSupplier top, java.util.function.IntSupplier bottom) {
        this.counter = counter;
        this.linage = new java.util.function.IntSupplier[] {body, footing, top, bottom};
        return this;
    }

    /** LINAGE-COUNTER: "the line number at which the device is positioned within the current page" (LR p. 23). */
    public Field counter() {
        return counter;
    }

    /**
     * WRITE on a LINAGE file, BEFORE or AFTER `n` lines (-1 for PAGE; no ADVANCING is AFTER 1, LR p.
     * 473); true when END-OF-PAGE is reached, the counter at or past the footing line after the write or
     * a page overflow (LR pp. 474-475). An overflow, a WRITE that would pass the page body, repositions
     * to the next page and the counter is 1 (J-149, L1). On disk, cobc's form (J-151, the lane's profile):
     * each record a line of its own, trailing spaces trimmed; ADVANCING n is n - 1 blank lines before
     * (AFTER) or after (BEFORE) it; a new page is the rest of the page body, the bottom margin and the
     * next page's top margin as blank lines.
     */
    // Decision: R-019
    public boolean page(Field record, boolean before, int n) {
        if (!writable()) return false;
        int line = counter.get().unscaled().intValue();
        boolean overflow = n < 0 || line + n > body;
        if (!before) {
            if (overflow) blank(body - line + bottom + page());
            else blank(n - 1);
        }
        line(record);
        if (before) {
            if (overflow) blank(body - line + bottom + page());
            else blank(n - 1);
        }
        count(overflow ? 1 : line + n);
        return overflow && n >= 0 || !overflow && line + n >= footing;
    }

    /** The next page's values read (L3): its top margin, the number of blank lines it starts with. */
    private int page() {
        body = linage[0].getAsInt();
        footing = linage[1] == null ? body : linage[1].getAsInt();
        bottom = linage[3] == null ? 0 : linage[3].getAsInt();
        return linage[2] == null ? 0 : linage[2].getAsInt();
    }

    private void count(int line) {
        counter.put(new Decimal(java.math.BigInteger.valueOf(line), 0));
    }

    private void blank(int lines) {
        try {
            for (int k = 0; k < lines; k++) out.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A record as a line: its bytes, trailing spaces trimmed, and a line feed. */
    private void line(Field record) {
        try {
            byte[] bytes = Literal.bytes(record);
            byte space = record.record().codepage().character(' ');
            int end = bytes.length;
            while (end > 0 && bytes[end - 1] == space) end--;
            out.write(bytes, 0, end);
            out.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** WRITE: `after` line feeds (or a form feed for PAGE, -1) before the record, `before` after it; with no ADVANCING, a line sequential record ends its line. */
    // Decision: R-019
    public void write(Field record, int before, int after) {
        if (!writable()) return;
        try {
            if (after < 0) out.write('\f');
            for (int k = 0; k < after; k++) out.write('\n');
            out.write(Literal.bytes(record));
            if (before < 0) out.write('\f');
            for (int k = 0; k < before; k++) out.write('\n');
            if (lines && before == 0 && after == 0) out.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A WRITE's check: 48 on a file not open for output (LR p. 302), else 00. */
    private boolean writable() {
        opening = null;
        readAt = -1;
        last = mode == Mode.OUTPUT || mode == Mode.EXTEND || mode == Mode.IO ? "00" : "48";
        return last.equals("00");
    }

    /**
     * READ into the record area; false at the end of the file. 47 on a file not open for input, 46 after
     * an at-end condition (S3(a)), 10 at the end or on an absent OPTIONAL file (LR pp. 300-302).
     */
    public boolean read(Field record) {
        opening = null;
        readAt = -1;
        if (mode != Mode.INPUT && mode != Mode.IO) {
            last = "47";
            return true;
        }
        if (atEnd) {
            last = "46";
            return true;
        }
        try {
            byte[] dst = record.record().bytes();
            int n = record.attr().length(), k = 0, c;
            java.util.Arrays.fill(dst, record.offset(), record.offset() + n, record.record().codepage().character(' '));
            boolean got = false;
            long at = io == null ? -1 : io.getFilePointer();
            while (k < n && (c = io == null ? in.read() : io.read()) >= 0) {
                got = true;
                if (lines && c == '\n') break;
                dst[record.offset() + k++] = (byte) c;
            }
            if (lines && k == n) while ((c = in.read()) >= 0 && c != '\n') k++;
            atEnd = !got;
            last = atEnd ? "10" : "00";
            if (got) {
                readAt = at;
                readLength = k;
            }
            return got;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * REWRITE (LR 6.4 pp. 432-433): on a file open I-O (else 49) whose last statement was a successful
     * READ (else 43), the record that READ retrieved replaced by one of the same size (else 44); the
     * file position untouched (research §12, W1-W3 (a)).
     */
    // Decision: R-019
    public void rewrite(Field record) {
        opening = null;
        long at = readAt;
        readAt = -1;
        if (mode != Mode.IO) {
            last = "49";
            return;
        }
        if (at < 0) {
            last = "43";
            return;
        }
        byte[] bytes = Literal.bytes(record);
        if (bytes.length != readLength) {
            last = "44";
            return;
        }
        try {
            long position = io.getFilePointer();
            io.seek(at);
            io.write(bytes);
            io.seek(position);
            last = "00";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * CLOSE [REEL/UNIT] [WITH LOCK | NO REWIND]: 42 on a file not open (LR p. 302), else 00, a lock making
     * a later OPEN 38; on a disk file, "a non-reel/unit medium", REEL/UNIT, FOR REMOVAL and NO REWIND are
     * 07 (p. 300), and REEL/UNIT closes only the current reel, so the file stays open.
     */
    public void close(boolean lock, boolean reel, boolean noRewind) {
        opening = null;
        readAt = -1;
        if (mode == null) {
            last = "42";
            return;
        }
        last = reel || noRewind ? "07" : "00";
        if (reel) return;
        close();
        locked = lock;
    }

    /** OPEN ... WITH NO REWIND on a disk file: opened, and 07 when that succeeds (p. 300). */
    public void noRewind() {
        if (last.equals("00")) last = "07";
    }

    /** The run unit's end closing what is open (PG p. 179), with no status. */
    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (io != null) io.close();
            out = null;
            in = null;
            io = null;
            mode = null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
