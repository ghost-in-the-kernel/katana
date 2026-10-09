package katana.rt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.IntSupplier;

/**
 * A LINAGE file's page (LR 6.4 pp. 189-190), split from `SeqFile` (tithe 3a.5): its LINAGE-COUNTER, and
 * the page body, first footing line, top and bottom margins, each read at OPEN and again at each new
 * page, as a data-name's are (J-149's trial, L3); footing null is the page body, a margin null is zero.
 * `SeqFile` checks the WRITE and keeps the status; this writes the page.
 */
final class Linage {
    private final Field counter;
    private final IntSupplier[] values;
    private int body, footing, bottom;

    Linage(Field counter, IntSupplier body, IntSupplier footing, IntSupplier top, IntSupplier bottom) {
        this.counter = counter;
        this.values = new IntSupplier[] {body, footing, top, bottom};
    }

    /** LINAGE-COUNTER: "the line number at which the device is positioned within the current page" (LR p. 23). */
    Field counter() {
        return counter;
    }

    /** OPEN for output: the first page's values read, its top margin written, the counter 1. */
    void open(OutputStream out) {
        blank(out, page());
        count(1);
    }

    /**
     * WRITE BEFORE or AFTER `n` lines (-1 for PAGE; no ADVANCING is AFTER 1, LR p. 473); true when
     * END-OF-PAGE is reached, the counter at or past the footing line after the write or a page
     * overflow (LR pp. 474-475). An overflow, a WRITE that would pass the page body, repositions to the
     * next page and the counter is 1 (J-149, L1). On disk, cobc's form (J-151, the lane's profile): each
     * record a line of its own, trailing spaces trimmed; ADVANCING n is n - 1 blank lines before (AFTER)
     * or after (BEFORE) it; a new page is the rest of the page body, the bottom margin and the next
     * page's top margin as blank lines.
     */
    // Decision: R-019
    boolean write(OutputStream out, Field record, boolean before, int n) {
        int line = counter.get().unscaled().intValue();
        boolean overflow = n < 0 || line + n > body;
        if (!before) {
            if (overflow) blank(out, body - line + bottom + page());
            else blank(out, n - 1);
        }
        line(out, record);
        if (before) {
            if (overflow) blank(out, body - line + bottom + page());
            else blank(out, n - 1);
        }
        count(overflow ? 1 : line + n);
        return overflow && n >= 0 || !overflow && line + n >= footing;
    }

    /** The next page's values read (L3): its top margin, the number of blank lines it starts with. */
    private int page() {
        body = values[0].getAsInt();
        footing = values[1] == null ? body : values[1].getAsInt();
        bottom = values[3] == null ? 0 : values[3].getAsInt();
        return values[2] == null ? 0 : values[2].getAsInt();
    }

    private void count(int line) {
        counter.put(new Decimal(java.math.BigInteger.valueOf(line), 0));
    }

    private static void blank(OutputStream out, int lines) {
        try {
            for (int k = 0; k < lines; k++) out.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A record as a line: its bytes, trailing spaces trimmed, and a line feed. */
    private static void line(OutputStream out, Field record) {
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
}
