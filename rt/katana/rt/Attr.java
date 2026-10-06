package katana.rt;

/**
 * An item's attribute tuple as `rt/` holds it (R-018, R-019): `rt/` imports nothing, so the lowering
 * writes each `Field` with these values copied from the IR's `Attributes`: its usage, its category,
 * its digits and scale (an alphanumeric item's length in `digits`), whether it is signed, a sign separate
 * or leading, and JUSTIFIED RIGHT.
 */
public record Attr(Usage usage, Category category, int digits, int scale, boolean signed, boolean signSeparate, boolean signLeading, boolean justified) {
    /** How an item is stored (the IR's usages `rt/` reads and writes so far). */
    public enum Usage { DISPLAY, BINARY, NATIVE_BINARY, PACKED }

    /** What an item holds. */
    public enum Category { ALPHANUMERIC, NUMERIC }

    /** A numeric DISPLAY item: zoned, its sign overpunched in the last digit unless said otherwise. */
    public static Attr zoned(int digits, int scale, boolean signed) {
        return new Attr(Usage.DISPLAY, Category.NUMERIC, digits, scale, signed, false, false, false);
    }

    /** An alphanumeric item of `length` characters, JUSTIFIED RIGHT or not. */
    public static Attr text(int length, boolean justified) {
        return new Attr(Usage.DISPLAY, Category.ALPHANUMERIC, length, 0, false, false, false, justified);
    }

    /** A numeric item of another usage. */
    public static Attr numeric(Usage usage, int digits, int scale, boolean signed) {
        return new Attr(usage, Category.NUMERIC, digits, scale, signed, false, false, false);
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
