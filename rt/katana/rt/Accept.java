package katana.rt;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

/**
 * ACCEPT format 2 (LR 6.4 pp. 309-310): a conceptual data item, of implicit USAGE DISPLAY, MOVEd into
 * the item. DATE is YYMMDD, PICTURE 9(6); DATE YYYYMMDD 9(8); DAY YYDDD, 9(5); DAY YYYYDDD 9(7);
 * DAY-OF-WEEK 9(1), 1 for Monday to 7 for Sunday; TIME HHMMSShh, 9(8), in hundredths of a second.
 */
public final class Accept {
    private Accept() {}

    /** ACCEPT identifier FROM the conceptual item named (its words joined by a space), now. */
    // Decision: R-019
    public static void from(Field into, String conceptual) {
        from(into, conceptual, LocalDateTime.now());
    }

    // Decision: R-019
    static void from(Field into, String conceptual, LocalDateTime now) {
        String digits = switch (conceptual) {
            case "DATE" -> String.format("%02d%02d%02d", now.getYear() % 100, now.getMonthValue(), now.getDayOfMonth());
            case "DATE YYYYMMDD" -> String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
            case "DAY" -> String.format("%02d%03d", now.getYear() % 100, now.getDayOfYear());
            case "DAY YYYYDDD" -> String.format("%04d%03d", now.getYear(), now.getDayOfYear());
            case "DAY-OF-WEEK" -> String.valueOf(now.getDayOfWeek().getValue());
            case "TIME" -> String.format("%02d%02d%02d%02d", now.getHour(), now.getMinute(), now.getSecond(), now.get(ChronoField.MILLI_OF_SECOND) / 10);
            default -> throw new IllegalArgumentException("ACCEPT FROM " + conceptual);
        };
        Codepage cp = into.record().codepage();
        byte[] bytes = new byte[digits.length()];
        for (int k = 0; k < bytes.length; k++) bytes[k] = cp.digit(digits.charAt(k) - '0', 0);
        Move.move(new Field(new Record(bytes, cp), 0, Attr.zoned(bytes.length, 0, false)), into);
    }
}
