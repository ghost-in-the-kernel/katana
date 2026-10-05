package katana.sir.types;

/**
 * An item's attribute tuple (docs/ROADMAP.md 2.4): its usage, its category, how many digits and
 * where the decimal point is (scale; negative for a P to the left), whether it is signed, with the
 * sign separate and leading or trailing, and the PICTURE as written after expansion ("" for none).
 */
public record Attributes(Usage usage, Category category, int digits, int scale, boolean signed, boolean signSeparate, boolean leading, String picture) {
    /** How an item is stored. */
    public enum Usage { DISPLAY, BINARY, NATIVE_BINARY, PACKED, FLOAT_SHORT, FLOAT_LONG, INDEX, POINTER, PROCEDURE_POINTER, FUNCTION_POINTER, NATIONAL }

    /** What an item holds. */
    public enum Category { GROUP, ALPHABETIC, ALPHANUMERIC, ALPHANUMERIC_EDITED, NUMERIC, NUMERIC_EDITED, NATIONAL, INDEX, POINTER }

    /** SIR-04: a numeric item has 1 to 38 digits and every other item none; only a numeric item is signed. */
    public Attributes {
        boolean numeric = category == Category.NUMERIC || category == Category.NUMERIC_EDITED;
        if (numeric && usage != Usage.FLOAT_SHORT && usage != Usage.FLOAT_LONG && (digits < 1 || digits > 38)) throw new Invalid("SIR-04", "a numeric item has " + digits + " digits");
        if (!numeric && (digits != 0 || signed)) throw new Invalid("SIR-04", "a " + category + " item has digits or a sign");
        if (picture == null) throw new Invalid("SIR-04", "no picture (\"\" for none)");
    }
}
