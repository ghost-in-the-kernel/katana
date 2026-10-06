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
 * PAGE); READ of fixed records, or of lines for LINE SEQUENTIAL.
 */
public final class SeqFile {
    public enum Mode { INPUT, OUTPUT, EXTEND, IO }

    private final String name;
    private final boolean lines;
    private OutputStream out;
    private InputStream in;

    public SeqFile(String name, boolean lineSequential) {
        this.name = name;
        this.lines = lineSequential;
    }

    private Path path() {
        for (String key : new String[] {"DD_" + name, "dd_" + name}) if (System.getenv(key) != null) return Path.of(System.getenv(key));
        return Path.of(name);
    }

    // Decision: R-019
    public void open(Mode mode) {
        try {
            if (mode == Mode.INPUT) in = new BufferedInputStream(Files.newInputStream(path()));
            else out = new BufferedOutputStream(mode == Mode.EXTEND ? Files.newOutputStream(path(), StandardOpenOption.CREATE, StandardOpenOption.APPEND) : Files.newOutputStream(path()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** WRITE: `after` line feeds (or a form feed for PAGE, -1) before the record, `before` after it; with no ADVANCING, a line sequential record ends its line. */
    // Decision: R-019
    public void write(Field record, int before, int after) {
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

    /** READ into the record area; false at the end of the file. */
    public boolean read(Field record) {
        try {
            byte[] dst = record.record().bytes();
            int n = record.attr().length(), k = 0, c;
            java.util.Arrays.fill(dst, record.offset(), record.offset() + n, record.record().codepage().character(' '));
            while (k < n && (c = in.read()) >= 0) {
                if (lines && c == '\n') return true;
                dst[record.offset() + k++] = (byte) c;
            }
            if (lines) while ((c = in.read()) >= 0 && c != '\n') k++;
            return k > 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            out = null;
            in = null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
