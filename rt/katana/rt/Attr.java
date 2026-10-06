package katana.rt;

/**
 * An item's attribute tuple as `rt/` holds it (R-018, R-019): `rt/` imports nothing, so the lowering
 * writes each `Field` with these values copied from the IR's `Attributes`: its usage, its category,
 * its digits and scale (an alphanumeric item's length in `digits`), whether it is signed, a sign separate
 * or leading, JUSTIFIED RIGHT, and an edited item's `Edit` (null for any other).
 */
public record Attr(Usage usage, Category category, int digits, int scale, boolean signed, boolean signSeparate, boolean signLeading, boolean justified, Edit edit) {
    /** How an item is stored (the IR's usages `rt/` reads and writes so far). */
    public enum Usage { DISPLAY, BINARY, NATIVE_BINARY, PACKED }

    /** What an item holds. */
    public enum Category { ALPHANUMERIC, NUMERIC, NUMERIC_EDITED, ALPHANUMERIC_EDITED }

    /** A numeric DISPLAY item: zoned, its sign overpunched in the last digit unless said otherwise. */
    public static Attr zoned(int digits, int scale, boolean signed) {
        return new Attr(Usage.DISPLAY, Category.NUMERIC, digits, scale, signed, false, false, false, null);
    }

    /** An alphanumeric item of `length` characters, JUSTIFIED RIGHT or not. */
    public static Attr text(int length, boolean justified) {
        return new Attr(Usage.DISPLAY, Category.ALPHANUMERIC, length, 0, false, false, false, justified, null);
    }

    /** A numeric item of another usage. */
    public static Attr numeric(Usage usage, int digits, int scale, boolean signed) {
        return new Attr(usage, Category.NUMERIC, digits, scale, signed, false, false, false, null);
    }

    /** A numeric-edited item: its mask's digit positions as `digits`, `scale` of them after the point (the IR's Attributes). */
    public static Attr numericEdited(Edit edit, int digits, int scale) {
        return new Attr(Usage.DISPLAY, Category.NUMERIC_EDITED, digits, scale, false, false, false, false, edit);
    }

    /** An alphanumeric-edited item. */
    public static Attr alphanumericEdited(Edit edit) {
        return new Attr(Usage.DISPLAY, Category.ALPHANUMERIC_EDITED, 0, 0, false, false, false, false, edit);
    }

    /** The bytes an item of these attributes takes (IBM's sizes, as `sir/build/Placement` gives them). */
    public int length() {
        return switch (usage) {
            case DISPLAY -> edit != null ? edit.mask().length() : digits + (signed && signSeparate ? 1 : 0);
            case PACKED -> digits / 2 + 1;
            case BINARY, NATIVE_BINARY -> digits <= 4 ? 2 : digits <= 9 ? 4 : 8;
        };
    }
}
