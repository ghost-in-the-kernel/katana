package katana.rt;

import java.nio.charset.Charset;

/**
 * A collating sequence: each byte's weight. `native` the bytes' own values (LR p. 277: "the higher
 * collating value is determined using the hexadecimal value of characters"); a PROGRAM COLLATING
 * SEQUENCE written by the lowering from the ALPHABET clause, such as EBCDIC's order over the cobc lane's
 * bytes or the ASCII order over EBCDIC bytes.
 */
public record Collation(int[] weights) {
    /** The bytes' own order. */
    public static Collation nativeOrder() {
        int[] w = new int[256];
        for (int b = 0; b < 256; b++) w[b] = b;
        return new Collation(w);
    }

    /** Another character set's order over a codepage's bytes: each byte weighed by its character's code in `order`. */
    // Decision: R-019
    public static Collation of(Codepage codepage, Charset order) {
        int[] w = new int[256];
        for (int b = 0; b < 256; b++) {
            String c = new String(new byte[] {(byte) b}, codepage.charset);
            byte[] coded = c.getBytes(order);
            w[b] = coded.length == 1 && new String(coded, order).equals(c) ? coded[0] & 0xFF : b;
        }
        return new Collation(w);
    }

    /** A byte's weight. */
    public int weight(byte b) {
        return weights[b & 0xFF];
    }
}
