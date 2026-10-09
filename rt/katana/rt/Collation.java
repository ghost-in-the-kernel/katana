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

    /**
     * An ALPHABET of literals (LR 6.4 pp. 127-129): each part one position (an int[] of the bytes sharing
     * it, ALSO) or several (an int[][], a literal's characters or a THRU range), in ascending order; every
     * byte not listed after them, in native order.
     */
    // Decision: R-019
    public static Collation literal(Object... parts) {
        java.util.List<int[]> positions = new java.util.ArrayList<>();
        for (Object p : parts) {
            if (p instanceof int[][] many) positions.addAll(java.util.List.of(many));
            else positions.add((int[]) p);
        }
        int[] w = new int[256];
        java.util.Arrays.fill(w, -1);
        for (int k = 0; k < positions.size(); k++) for (int b : positions.get(k)) w[b] = k;
        int next = positions.size();
        for (int b = 0; b < 256; b++) if (w[b] < 0) w[b] = next++;
        return new Collation(w);
    }

    /** The bytes from `from` through `to` in the native set, either way round, each a position (THRU). */
    public static int[][] thru(int from, int to) {
        int step = from <= to ? 1 : -1;
        int[][] out = new int[Math.abs(to - from) + 1][];
        for (int k = 0, b = from; k < out.length; k++, b += step) out[k] = new int[] {b};
        return out;
    }

    /** LOW-VALUE: the byte of the lowest position (LR p. 129). */
    // Unsure: when ALSO shares the lowest position IBM's LOW-VALUE is the first character named; the weights do not keep that order, and the lowest byte of the position is taken (J-131). Stays: an IBM run of LOW-VALUE under an alphabet whose lowest position is shared settles it.
    public byte low() {
        int best = 0;
        for (int b = 1; b < 256; b++) if (weights[b] < weights[best]) best = b;
        return (byte) best;
    }

    /** HIGH-VALUE: the byte of the highest position, the last when several share it (LR p. 128). */
    public byte high() {
        int best = 0;
        for (int b = 1; b < 256; b++) if (weights[b] >= weights[best]) best = b;
        return (byte) best;
    }
}
