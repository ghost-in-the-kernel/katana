package katana.ledger;

/** A ledger, or a line of it, that breaks an invariant of docs/LEDGER.md; the message starts with its number (LED-nn). */
public final class Invalid extends IllegalArgumentException {
    public Invalid(String invariant, String message) {
        super(invariant + ": " + message);
    }
}
