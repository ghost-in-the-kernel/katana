import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** R-054's ceilings (J-163): a time past its ceiling refuses the push, the refusal first. */
class CeilingsTest {
    private static final String AGENT = """
            | Metric | Ceiling | Measured | Why |
            | --- | --- | --- | --- |
            | Fast loop (s) | ≤ 25 (20 from tithe pass 3's close) | 17 | x |
            | Pre-push check, the suites a push selects (min) | ≤ 10 | 6 | x |
            | Pre-push check, every suite (min) | ≤ 12 | 10.5 | x |
            """;

    @Test
    void aFastLoopPastItsCeilingIsRefused() {
        assertEquals(1, Ceilings.over(AGENT, 26, 300, false).size());
    }

    @Test
    void aCheckPastItsCeilingIsRefusedByWhichSuitesItRan() {
        assertEquals(1, Ceilings.over(AGENT, 17, 660, false).size());
        assertEquals(List.of(), Ceilings.over(AGENT, 17, 660, true));
        assertEquals(1, Ceilings.over(AGENT, 17, 780, true).size());
    }

    @Test
    void timesWithinTheirCeilingsPass() {
        assertEquals(List.of(), Ceilings.over(AGENT, 25, 600, false));
    }

    @Test
    void aMetricWithNoCeilingIsAnError() {
        assertThrows(IllegalStateException.class, () -> Ceilings.over(AGENT.replace("| ≤ 10 |", "| none |"), 17, 300, false));
    }
}
