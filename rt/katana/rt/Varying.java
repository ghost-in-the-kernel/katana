package katana.rt;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * A variable-length sequential file's records (LR 6.4 pp. 186-188, 426; research §13): the length a
 * WRITE writes, the bounds RECORD IS VARYING sets, and DEPENDING ON set by a READ. On disk each record
 * follows IBM's format-V "cc", 4 bytes, the length with them big-endian then two zero bytes (PG p.
 * 171). `SeqFile` keeps the status and the position; this frames the records.
 */
final class Varying {
    private final int least, greatest, min, max;
    private final boolean bounded;
    private final Field depending;

    /** `least` and `greatest` the 01 records' sizes; `min` and `max` VARYING's bounds when `bounded` (format 3); `depending` its DEPENDING ON item or null. */
    Varying(int least, int greatest, boolean bounded, int min, int max, Field depending) {
        this.least = least;
        this.greatest = greatest;
        this.bounded = bounded;
        this.min = min;
        this.max = max;
        this.depending = depending;
    }

    /** The bytes a WRITE or REWRITE puts out: DEPENDING ON's value, else the record-name's (its current length for an ODO record) (p. 188). */
    int length(Field record) {
        return depending == null ? record.attr().length() : depending.get().unscaled().intValue();
    }

    /** 44 for a length outside RECORD IS VARYING's bounds, "larger than the largest or smaller than the smallest record allowed" (p. 302), else null. */
    String outside(int length) {
        return bounded && (length < min || length > max) ? "44" : null;
    }

    /** One record: its "cc" then `length` bytes of the record area, from the record's start. */
    void write(OutputStream out, Field record, int length) throws IOException {
        out.write(new byte[] {(byte) ((length + 4) >> 8), (byte) (length + 4), 0, 0});
        byte[] area = record.record().bytes();
        int n = Math.min(length, area.length - record.offset());
        out.write(area, record.offset(), n);
        for (int k = n; k < length; k++) out.write(record.record().codepage().character(' '));
    }

    /**
     * The next record into the record area, its length returned, -1 at the end. Only the bytes read are
     * replaced; past a short record the area is kept (p. 426's "undefined", the lane's choice with cobc,
     * R-060, a hypothesis); a longer one is cut to the area. DEPENDING ON takes the length (p. 188).
     */
    // Decision: R-060
    int read(DataInput in, Field area) throws IOException {
        int length;
        try {
            length = in.readUnsignedShort() - 4;
            in.readUnsignedShort();
        } catch (EOFException e) {
            return -1;
        }
        byte[] record = new byte[length];
        in.readFully(record);
        byte[] dst = area.record().bytes();
        System.arraycopy(record, 0, dst, area.offset(), Math.min(length, dst.length - area.offset()));
        if (depending != null) depending.put(new Decimal(java.math.BigInteger.valueOf(length), 0));
        return length;
    }

    /** A record of `length` bytes read by another file's own framing (`RelFile`): DEPENDING ON takes the length (p. 188). */
    void took(int length) {
        if (depending != null) depending.put(new Decimal(java.math.BigInteger.valueOf(length), 0));
    }

    /** The `length` bytes a WRITE or REWRITE puts out, from the record's start, spaces past the area. */
    byte[] bytes(Field record, int length) {
        byte[] out = new byte[length], area = record.record().bytes();
        int n = Math.min(length, area.length - record.offset());
        System.arraycopy(area, record.offset(), out, 0, n);
        java.util.Arrays.fill(out, n, length, record.record().codepage().character(' '));
        return out;
    }

    /** A READ's status: 04 for a record shorter than the shortest 01 or longer than the longest, VLR(STANDARD) being the default (PG pp. 422-423), else 00. */
    String status(int length) {
        return length < least || length > greatest ? "04" : "00";
    }
}
