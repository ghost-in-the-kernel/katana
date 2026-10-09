import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** R-008's gate (J-154): its verdict on histories made here, the refusal first. */
class TitheGateTest {
    /** `features` feature commits on top of one tithe, newest first, the head first. */
    private static List<String> log(int features) {
        List<String> log = new ArrayList<>();
        for (int k = features; k >= 1; k--) log.add(sha(k) + " Lower something " + k);
        log.add(sha(1000) + " Tithe 3a: something tidied");
        return log;
    }

    /** A commit id unlike every other here in its first twelve characters. */
    private static String sha(int k) {
        return String.format("%-40s", "c" + k + "e").replace(' ', '0');
    }

    @Test
    void seventeenFeatureCommitsAndNoDeferralAreRefused() {
        assertNotNull(TitheGate.verdict(log(17), ""));
    }

    @Test
    void sixteenAreAllowed() {
        assertNull(TitheGate.verdict(log(16), ""));
    }

    @Test
    void aTitheAtTheHeadIsAlwaysAllowed() {
        List<String> log = log(40);
        log.add(0, sha(2000) + " Tithe 3a: the gate");
        assertNull(TitheGate.verdict(log, ""));
    }

    @Test
    void aDeferralRestartsTheCountAtItsCommit() {
        String through = sha(5).substring(0, 12);
        assertNull(TitheGate.verdict(log(20), "- R-0xx: Tithe deferred through " + through + ", for a release."));
        assertNotNull(TitheGate.verdict(log(40), "- R-0xx: Tithe deferred through " + through + ", for a release."), "a deferral runs to its commit, not past it");
    }
}
