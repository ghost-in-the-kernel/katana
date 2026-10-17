package katana.intent.types;

/** A fact or a set of facts that breaks an invariant of docs/INTENT-IR.md; the message starts with its number (INT-nn). */
public final class Invalid extends IllegalArgumentException {
    public Invalid(String invariant, String message) {
        super(invariant + ": " + message);
    }
}
