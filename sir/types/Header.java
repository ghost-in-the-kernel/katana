package katana.sir.types;

import java.util.List;


/**
 * SIR-01: the program, the file it was read from, the profile it means what it means under, by hash
 * (R-007, R-049), and its SPECIAL-NAMES for editing: the CURRENCY SIGN (`$` when none) and whether
 * DECIMAL-POINT IS COMMA, which a mask's `$` and `.` stand for; each switch-status condition-name,
 * with its switch's environment-name and the status it stands for (ON true); each switch's mnemonic-name;
 * each CLASS name with its members; each alphabet-name; and the alphabet the program collating
 * sequence names ("" for the native one); and the PROGRAM-ID's INITIAL and COMMON attributes (SIR-12).
 */
public record Header(String program, String source, String profileHash, String currency, boolean decimalComma, boolean debuggingMode, List<Switch> switches, List<Mnemonic> mnemonics, List<CharClass> classes,
        List<Alphabet> alphabets, String collating, boolean initial, boolean common, Origin origin) {
    /**
     * An alphabet-name (LR 6.4 pp. 127-129): `kind` EBCDIC, ASCII, STANDARD-1, STANDARD-2, NATIVE or
     * LITERAL; for LITERAL its members in order, each a literal (each of its characters a position), a
     * `THRU` range (each character of it, in the native set, a position) or an `ALSO` group (one position).
     */
    public record Alphabet(String name, String kind, List<Value> members) {
        public Alphabet {
            members = List.copyOf(members);
        }
    }

    /**
     * A CLASS name and its members (LR 6.4 p. 129), as an 88's values are: each a literal or a `THRU`
     * range; an alphanumeric literal stands for each of its characters, a numeric one for the character at
     * that ordinal position (from 1) in the native collating sequence; a range runs either way.
     */
    public record CharClass(String name, List<Value> members) {
        public CharClass {
            members = List.copyOf(members);
        }
    }

    /**
     * A mnemonic-name and its environment-name: a switch (`UPSI-0 IS SW-1`), which SET mnemonic-name TO ON
     * or OFF sets (LR 6.4 p. 126); a channel or space suppression (`C01 IS TOP-OF-PAGE`), which WRITE ...
     * ADVANCING names (p. 473); the console or another device (J-172).
     */
    public record Mnemonic(String name, String environment) {}

    /** A switch-status condition-name: true when the switch (`UPSI-0` to `UPSI-7`) has this status (LR 6.4 pp. 126, 283). */
    public record Switch(String condition, String environment, boolean on) {}

    public Header {
        switches = List.copyOf(switches);
        mnemonics = List.copyOf(mnemonics);
        classes = List.copyOf(classes);
        alphabets = List.copyOf(alphabets);
        if (!profileHash.matches("sha256:[0-9a-f]{64}")) throw new Invalid("SIR-01", "the profile hash is not sha256:<64 hex>: " + profileHash);
        Invalid.origin(origin, "the header");
    }
}
