package katana.sir.types;


/**
 * SIR-01: the program, the file it was read from, the profile it means what it means under, by hash
 * (R-007, R-049), and its SPECIAL-NAMES for editing: the CURRENCY SIGN (`$` when none) and whether
 * DECIMAL-POINT IS COMMA, which a mask's `$` and `.` stand for.
 */
public record Header(String program, String source, String profileHash, String currency, boolean decimalComma, Origin origin) {
    public Header {
        if (!profileHash.matches("sha256:[0-9a-f]{64}")) throw new Invalid("SIR-01", "the profile hash is not sha256:<64 hex>: " + profileHash);
        Invalid.origin(origin, "the header");
    }
}
