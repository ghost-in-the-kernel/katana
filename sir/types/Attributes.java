package katana.sir.types;

/**
 * An item's attribute tuple (docs/ROADMAP.md 2.4): its usage, its category, how many digits and
 * where the decimal point is (scale; negative for a P to the left), whether it is signed, with the
 * sign separate and leading or trailing, the PICTURE as written after expansion ("" for none), and,
 * for an edited item, its mask: one symbol a character position, the PICTURE's S, V and P dropped (CR
 * and DB two), the program's currency sign written `$` and its decimal point `.` (`Header` says which
 * they are); "" for any other item. Tithe pass 2, A4: the lowering reads no PICTURE string.
 */
public record Attributes(Usage usage, Category category, int digits, int scale, boolean signed, boolean signSeparate, boolean leading, String picture, String mask) {
    /** How an item is stored. */
    public enum Usage { DISPLAY, BINARY, NATIVE_BINARY, PACKED, FLOAT_SHORT, FLOAT_LONG, INDEX, POINTER, PROCEDURE_POINTER, FUNCTION_POINTER, NATIONAL }

    /** What an item holds. */
    public enum Category { GROUP, ALPHABETIC, ALPHANUMERIC, ALPHANUMERIC_EDITED, NUMERIC, NUMERIC_EDITED, NATIONAL, INDEX, POINTER }

    /** SIR-04: a numeric item has 1 to 38 digits and every other item none; only a numeric item is signed; only an edited item has a mask, of no S, V or P. */
    public Attributes {
        boolean numeric = category == Category.NUMERIC || category == Category.NUMERIC_EDITED;
        if (numeric && usage != Usage.FLOAT_SHORT && usage != Usage.FLOAT_LONG && (digits < 1 || digits > 38)) throw new Invalid("SIR-04", "a numeric item has " + digits + " digits");
        if (!numeric && (digits != 0 || signed)) throw new Invalid("SIR-04", "a " + category + " item has digits or a sign");
        if (picture == null) throw new Invalid("SIR-04", "no picture (\"\" for none)");
        boolean edited = category == Category.NUMERIC_EDITED || category == Category.ALPHANUMERIC_EDITED;
        if (mask == null || edited == mask.isEmpty() || mask.matches(".*[SVP].*")) throw new Invalid("SIR-04", "a " + category + " item's mask is \"" + mask + "\"");
    }
}
