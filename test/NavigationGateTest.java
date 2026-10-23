import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** R-079's gate: its verdict on histories made here, the refusal first. */
class NavigationGateTest {
    private static final String REVIEWED = "Reviewed through: c1000e0000\n";

    /** `commits` commits on top of the reviewed one, newest first. */
    private static List<String> log(int commits) {
        List<String> log = new ArrayList<>();
        for (int k = commits; k >= 1; k--) log.add(String.format("%-40s", "c" + k + "e").replace(' ', '0') + " Lower something " + k);
        log.add(String.format("%-40s", "c1000e").replace(' ', '0') + " Navigation review 1");
        return log;
    }

    @Test
    void twentyOneCommitsSinceTheReviewAreRefused() {
        assertNotNull(NavigationGate.verdict(log(21), REVIEWED));
    }

    @Test
    void twentyAreAllowed() {
        assertNull(NavigationGate.verdict(log(20), REVIEWED));
    }

    @Test
    void aReviewAtTheHeadIsAlwaysAllowed() {
        List<String> log = log(40);
        log.add(0, String.format("%-40s", "c2000e").replace(' ', '0') + " Tithe: navigation review 2: two converged");
        assertNull(NavigationGate.verdict(log, REVIEWED));
    }

    @Test
    void aFileNamingNoReviewIsRefused() {
        assertNotNull(NavigationGate.verdict(log(1), "no line"));
    }

    @Test
    void sinceCountsFromTheReviewedCommit() {
        assertEquals(3, NavigationGate.since(log(3), REVIEWED));
    }
}
