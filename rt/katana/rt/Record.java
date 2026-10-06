package katana.rt;

/** A 01-level's storage (R-019): its bytes, in its program's codepage. */
public record Record(byte[] bytes, Codepage codepage) {
    /** A record of `length` bytes, every byte the codepage's space. */
    public static Record of(int length, Codepage codepage) {
        byte[] b = new byte[length];
        java.util.Arrays.fill(b, codepage.character(' '));
        return new Record(b, codepage);
    }
}
