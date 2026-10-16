package katana.rt;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The run's moment: the lane's clock when it sets one, as both lanes read it (LANE-03: `COB_CURRENT_DATE`,
 * `YYYY-MM-DD HH:MM:SS.hh +HH:MM`, held for the whole run as cobc holds it), else the machine's.
 */
public final class Now {
    private static final DateTimeFormatter LANE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS xxx");

    private Now() {}

    /** The moment CURRENT-DATE and ACCEPT FROM DATE, DAY or TIME read. */
    // Decision: R-009
    public static ZonedDateTime now() {
        String clock = System.getenv("COB_CURRENT_DATE");
        if (clock == null || clock.isBlank()) return ZonedDateTime.now();
        return OffsetDateTime.parse(clock.trim(), LANE).toZonedDateTime();
    }
}
