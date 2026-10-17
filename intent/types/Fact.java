package katana.intent.types;

/**
 * One line of the Intent IR (R-007): what a thing in the program means, about a subject of its kind's
 * shape, held by its evidence with a confidence, and where it stands. Known by its kind and subject, so a
 * rule's next version that infers it again is the same fact.
 */
public record Fact(Kind kind, Subject subject, Evidence evidence, double confidence, State state) {
    public Fact {
        if (!kind.shape().isInstance(subject)) throw new Invalid("INT-01", kind + " is about " + kind.shape().getSimpleName() + ", not " + subject);
        if (!(confidence > 0 && confidence <= 1)) throw new Invalid("INT-04", "confidence " + confidence);
    }

    /** The fact's identity: its kind and its subject (INT-06). */
    public String id() {
        return kind.name().toLowerCase(java.util.Locale.ROOT) + " " + subject.text();
    }

    /** R-029: a fact no person has confirmed; a pass that acts on one says so. */
    public boolean unconfirmed() {
        return state.status() != State.Status.CONFIRMED;
    }
}
