package katana.rt;

/**
 * The eight UPSI switches, as Language Environment's UPSI run-time option sets them (z/OS 3.1 LE
 * Programming Reference SA38-0683-60, p. 87): `nnnnnnnn`, "the leftmost n representing the first
 * switch", each 0 (off) or 1 (on); UPSI(00000000) the default.
 */
public final class Upsi {
    private final char[] bits;

    private Upsi(String bits) {
        this.bits = bits.toCharArray();
    }

    /** The switches from the option's value. */
    public static Upsi of(String bits) {
        if (!bits.matches("[01]{8}")) throw new IllegalArgumentException("UPSI must be eight 0s and 1s: " + bits);
        return new Upsi(bits);
    }

    /** Whether UPSI-n is on. */
    public boolean on(int n) {
        return bits[n] == '1';
    }

    /** SET mnemonic-name TO ON or OFF: UPSI-n set (LR 6.4, SET for switches); every program of the run shares one copy (PG 6.4). */
    public void set(int n, boolean on) {
        bits[n] = on ? '1' : '0';
    }
}
