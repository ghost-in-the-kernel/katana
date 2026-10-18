package katana.intent;

/** A built review test's word on one fact (R-066, research §34): it holds, it refutes, or it cannot tell, and why, in the program's own names. */
record Verdict(String test, Outcome outcome, String why) {
    enum Outcome { HOLDS, REFUTES, CANNOT_TELL }

    static Verdict holds(String test, String why) {
        return new Verdict(test, Outcome.HOLDS, why);
    }

    static Verdict refutes(String test, String why) {
        return new Verdict(test, Outcome.REFUTES, why);
    }

    static Verdict cannotTell(String test, String why) {
        return new Verdict(test, Outcome.CANNOT_TELL, why);
    }

    /** As the review file writes it, under the fact's tests line. */
    String line() {
        return test + " " + outcome.name().toLowerCase().replace('_', ' ') + ": " + why;
    }
}
