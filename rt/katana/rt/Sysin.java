package katana.rt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Where ACCEPT format 1 reads (LR 6.4 pp. 307-308): the input device's records, each a line, read into
 * the item's bytes "until the receiving area is filled or EOF is encountered", each record concatenated
 * with the one before, the last cut to fit; padded with spaces when EOF comes after some data, the item
 * unchanged when it comes before any. No editing and no MOVE: the bytes are the data, in the lane's
 * codepage, as DISPLAY writes its bytes.
 */
public final class Sysin {
    private final InputStream in;

    public Sysin(InputStream in) {
        this.in = in;
    }

    /** ACCEPT identifier, from the input device. */
    // Decision: R-019
    // Unsure: a line is a record of its own length (IBM's variable-length format); a fixed-length input device would keep each record's trailing spaces when records are concatenated, which only a receiver longer than a line shows (J-122).
    public void accept(Field into) {
        int length = into.attr().length();
        ByteArrayOutputStream got = new ByteArrayOutputStream();
        while (got.size() < length) {
            byte[] record = record();
            if (record == null) break;
            got.writeBytes(record);
        }
        if (got.size() == 0) return;
        byte[] data = got.toByteArray(), bytes = into.record().bytes();
        for (int k = 0; k < length; k++) bytes[into.offset() + k] = k < data.length ? data[k] : into.record().codepage().character(' ');
    }

    /** The next line, without its line end; null at the end of the input. */
    private byte[] record() {
        try {
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            int b = in.read();
            if (b < 0) return null;
            while (b >= 0 && b != '\n') {
                line.write(b);
                b = in.read();
            }
            byte[] out = line.toByteArray();
            return out.length > 0 && out[out.length - 1] == '\r' ? java.util.Arrays.copyOf(out, out.length - 1) : out;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
