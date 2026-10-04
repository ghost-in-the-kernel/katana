package katana.oracle.nist;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What a NIST audit routine says about itself. Every routine ends its printed report with the same
 * four lines, from its own END-ROUTINE (newcob.val, CCVS85's standard report code):
 * "nnn OF mmm  TESTS WERE EXECUTED SUCCESSFULLY", then "nnn TEST(S) FAILED", "... DELETED" and
 * "... REQUIRE INSPECTION", each count a three-character field that reads "NO " for zero. The counts
 * are taken from those lines, never from the per-test lines, whose headings repeat the same words.
 * NIST's manual programs end with a second form, two lines only: "nn TESTS REQUIRE VISUAL
 * INSPECTION" and "nn TESTS DELETED" (NC113M); every test there is inspection or deleted.
 */
public final class Report {
    /** A routine's own count of its tests: how many in all, and how many passed, failed, were deleted or need a person. */
    public record Counts(int total, int pass, int fail, int deleted, int inspect) {}

    private static final Pattern EXECUTED = Pattern.compile("(?m)^\\s*(\\d{1,3}|NO ) OF\\s+(\\d{1,3}|NO )\\s+TESTS WERE EXECUTED SUCCESSFULLY");
    private static final Pattern FAILED = footer("TEST\\(S\\) FAILED");
    private static final Pattern DELETED = footer("TEST\\(S\\) DELETED");
    private static final Pattern INSPECT = footer("TEST\\(S\\) REQUIRE INSPECTION");
    private static final Pattern VISUAL = footer("TESTS REQUIRE VISUAL INSPECTION");
    private static final Pattern VISUAL_DELETED = footer("TESTS DELETED");

    private Report() {}

    /**
     * The counts in a printed report, or empty when it has no complete footer (the routine stopped
     * before its END-ROUTINE, or printed nothing). When a report holds more than one footer, the last
     * is the routine's final word.
     */
    public static Optional<Counts> read(String printed) {
        String text = printed.replace("\f", "\n");
        MatchResult executed = last(EXECUTED, text);
        MatchResult failed = last(FAILED, text);
        MatchResult deleted = last(DELETED, text);
        MatchResult inspect = last(INSPECT, text);
        if (executed == null || failed == null || deleted == null || inspect == null) return visual(text);
        return Optional.of(new Counts(count(executed.group(2)), count(executed.group(1)), count(failed.group(1)),
                count(deleted.group(1)), count(inspect.group(1))));
    }

    private static Pattern footer(String words) {
        return Pattern.compile("(?m)^\\s*(\\d{1,3}|NO)\\s+" + words);
    }

    /** The manual programs' footer: every test either needs a person or was deleted. */
    private static Optional<Counts> visual(String text) {
        MatchResult inspect = last(VISUAL, text);
        MatchResult deleted = last(VISUAL_DELETED, text);
        if (inspect == null || deleted == null) return Optional.empty();
        int i = count(inspect.group(1)), d = count(deleted.group(1));
        return Optional.of(new Counts(i + d, 0, 0, d, i));
    }

    /** The last match of a pattern in a text, or null when there is none. */
    private static MatchResult last(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        MatchResult found = null;
        while (m.find()) found = m.toMatchResult();
        return found;
    }

    /** A footer count: "NO " is zero, anything else its digits. */
    private static int count(String field) {
        return field.trim().equals("NO") ? 0 : Integer.parseInt(field.trim());
    }
}
