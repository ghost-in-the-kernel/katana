package katana.sir.types;


/** An IR that breaks an invariant; its message starts with the invariant's number. */
public final class Invalid extends IllegalArgumentException {
    public Invalid(String invariant, String message) {
        super(invariant + ": " + message);
    }

    /** SIR-07: everything in the IR says where in the COBOL it came from. */
    static void origin(Origin origin, String what) {
        if (origin == null) throw new Invalid("SIR-07", what + " has no origin");
    }
}
