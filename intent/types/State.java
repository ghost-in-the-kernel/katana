package katana.intent.types;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Where a fact stands: inferred by its rule, or confirmed or refuted by a person, who and when (INT-05). */
public record State(Status status, String who, String when) {
    public enum Status { INFERRED, CONFIRMED, REFUTED }

    /** As a rule leaves it: no person yet. */
    public static final State INFERRED = new State(Status.INFERRED, "", "");

    public State {
        boolean person = !who.isBlank() && date(when);
        if (status == Status.INFERRED ? !who.isEmpty() || !when.isEmpty() : !person) throw new Invalid("INT-05", status + " by '" + who + "' on '" + when + "'");
    }

    private static boolean date(String when) {
        try {
            LocalDate.parse(when);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
