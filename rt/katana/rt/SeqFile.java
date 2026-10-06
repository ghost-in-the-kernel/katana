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
    private final String file;
    private final boolean lines;
    private final boolean status;
    private OutputStream out;
    private InputStream in;

    /** A file: the name it is ASSIGNed to, the program's name for it, and whether it has a FILE STATUS item. */
    public SeqFile(String name, String file, boolean lineSequential, boolean status) {
        this.name = name;
        this.file = file;
        this.lines = lineSequential;
        this.status = status;
    }

    private Path path() {
        for (String key : new String[] {"DD_" + name, "dd_" + name}) if (System.getenv(key) != null) return Path.of(System.getenv(key));
        return Path.of(name);
    }

    /**
     * OPEN. An input file that is not there, with no FILE STATUS item, ends the run unit: "If you do not
     * handle an input or output error in any of these ways, a severity-3 Language Environment condition
     * is signaled, which causes the run unit to end" (PG 6.4 p. 243), by an abend under LE's default
     * ABTERMENC(ABEND); the reason is file status 35 on the file.
     */
    // Decision: R-019
    // Unsure: U4038 is Fable's recollection of LE's abend for an unhandled severity-3 condition in batch, not yet quoted (HANDOFF, J-145); OPTIONAL and declaratives are not in the IR yet, so an OPTIONAL file is taken as required.
    public void open(Mode mode) {
        if (mode == Mode.INPUT && !status && !Files.exists(path())) throw new Abend(4038, "OPEN " + file + ": file status 35 on " + name);
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
