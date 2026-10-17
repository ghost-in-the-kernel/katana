package katana.ledger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.List;

/**
 * One line of the proof ledger (docs/LEDGER.md): what was done to a Java symbol and why it was allowed. Pass
 * `pass` made `symbol`, which stands on the COBOL `node`, licensed by intent fact `fact` (`-` for none) in the
 * state a person left it (`confirmed` or `unconfirmed`, R-029), proven by oracle run `run` (`-` where none
 * has), under `profile`, at `time`, standing in for line `supersedes` (0 for none). `seq` numbers it and `prev`
 * is the hash of the line before, so no line can change without the next one saying so (R-030).
 */
public record Entry(int seq, String prev, String pass, String fact, String state, String run, String symbol, String node, String profile, String time, int supersedes) {
    public static final String FIRST = "0".repeat(16);
    static final String SEP = " | ";

    /** LED-02: a line names its pass, symbol and node, the profile by its hash, the time as an instant; a fact's state is confirmed or unconfirmed, never refuted (R-029). */
    public Entry {
        for (String field : List.of(pass, fact, state, run, symbol, node, profile, time)) if (field.isBlank() || field.contains("|") || field.contains("\n")) throw new Invalid("LED-02", "line " + seq + " has a field '" + field + "'");
        if (!profile.matches("sha256:[0-9a-f]{64}")) throw new Invalid("LED-02", "line " + seq + " names no profile hash");
        if (fact.equals("-") != state.equals("-") || !(state.equals("-") || state.equals("confirmed") || state.equals("unconfirmed"))) throw new Invalid("LED-02", "line " + seq + " acts on " + fact + " " + state);
        if (symbol.equals("-") && supersedes == 0) throw new Invalid("LED-02", "line " + seq + " removes nothing it supersedes");
        try {
            Instant.parse(time);
        } catch (DateTimeParseException e) {
            throw new Invalid("LED-02", "line " + seq + " at '" + time + "'");
        }
        if (seq < 1 || supersedes < 0 || supersedes >= seq) throw new Invalid("LED-03", "line " + seq + " supersedes " + supersedes + ", not an earlier line");
    }

    /** The line as the file holds it. */
    public String text() {
        return String.join(SEP, String.valueOf(seq), prev, pass, fact, state, run, symbol, node, profile, time, supersedes == 0 ? "-" : String.valueOf(supersedes));
    }

    /** A line read back. */
    static Entry read(String text) {
        String[] f = text.split(" \\| ", -1);
        if (f.length != 11) throw new Invalid("LED-01", "'" + text + "' is not a ledger line");
        try {
            return new Entry(Integer.parseInt(f[0]), f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9], f[10].equals("-") ? 0 : Integer.parseInt(f[10]));
        } catch (NumberFormatException e) {
            throw new Invalid("LED-01", "'" + text + "' is not numbered");
        }
    }

    /** The hash the next line carries: the first 16 hex digits of SHA-256 over this line's text. */
    String hash() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
