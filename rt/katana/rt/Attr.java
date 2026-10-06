package katana.rt;

/**
 * An item's attribute tuple as `rt/` holds it (R-018, R-019): `rt/` imports nothing, so the lowering
 * writes each `Field` with these values copied from the IR's `Attributes`: its usage, its category,
 * its digits and scale, whether it is signed, and a sign separate or leading.
 */
public record Attr(Usage usage, Category category, int digits, int scale, boolean signed, boolean signSeparate, boolean signLeading) {
    /** How an item is stored (the IR's usages `rt/` reads and writes so far). */
    public enum Usage { DISPLAY, BINARY, NATIVE_BINARY, PACKED }

    /** What an item holds. */
    public enum Category { ALPHANUMERIC, NUMERIC }

    /** A numeric DISPLAY item: zoned, its sign overpunched in the last digit unless said otherwise. */
    public static Attr zoned(int digits, int scale, boolean signed) {
        return new Attr(Usage.DISPLAY, Category.NUMERIC, digits, scale, signed, false, false);
    }

    /** A numeric item of another usage. */
    public static Attr numeric(Usage usage, int digits, int scale, boolean signed) {
        return new Attr(usage, Category.NUMERIC, digits, scale, signed, false, false);
    }

    /** The bytes an item of these attributes takes (IBM's sizes, as `sir/build/Placement` gives them). */
    public int length() {
        return switch (usage) {
            case DISPLAY -> digits + (signed && signSeparate ? 1 : 0);
            case PACKED -> digits / 2 + 1;
            case BINARY, NATIVE_BINARY -> digits <= 4 ? 2 : digits <= 9 ? 4 : 8;
        };
    }
}
