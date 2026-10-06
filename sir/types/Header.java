package katana.sir.types;

import java.util.List;


/**
 * SIR-01: the program, the file it was read from, the profile it means what it means under, by hash
 * (R-007, R-049), and its SPECIAL-NAMES for editing: the CURRENCY SIGN (`$` when none) and whether
 * DECIMAL-POINT IS COMMA, which a mask's `$` and `.` stand for; and each switch-status condition-name,
 * with its switch's environment-name and the status it stands for (ON true).
 */
public record Header(String program, String source, String profileHash, String currency, boolean decimalComma, List<Switch> switches, Origin origin) {
    /** A switch-status condition-name: true when the switch (`UPSI-0` to `UPSI-7`) has this status (LR 6.4 pp. 126, 283). */
    public record Switch(String condition, String environment, boolean on) {}

    public Header {
        switches = List.copyOf(switches);
        if (!profileHash.matches("sha256:[0-9a-f]{64}")) throw new Invalid("SIR-01", "the profile hash is not sha256:<64 hex>: " + profileHash);
        Invalid.origin(origin, "the header");
    }
}
