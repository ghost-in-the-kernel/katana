package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** `Dispatcher`'s segments (research §16, J-175): an independent segment's ALTERs as written each time it is entered from another segment. */
class DispatcherTest {
    /** Paragraphs 0 and 1 in fixed segment 0, 2 and 3 in independent segment 60, 4 in independent segment 70. */
    private static Dispatcher segmented() {
        return new Dispatcher(new int[] {0, 0, 60, 60, 70});
    }

    @Test
    void anIndependentSegmentEnteredFromAFixedOneIsInItsInitialState() {
        Dispatcher d = segmented();
        d.enter(2);
        d.alter(3, 1);
        d.enter(0);
        d.enter(2);
        assertEquals(4, d.goTo(3, 4), "R1, LR p. 265: entered from fixed segment 0, its GO TO is as written");
    }

    @Test
    void anIndependentSegmentEnteredFromAnotherIndependentOneIsInItsInitialState() {
        Dispatcher d = segmented();
        d.enter(2);
        d.alter(3, 1);
        d.enter(4);
        d.enter(3);
        assertEquals(4, d.goTo(3, 4));
    }

    @Test
    void withinItsSegmentAnAlterHolds() {
        Dispatcher d = segmented();
        d.enter(2);
        d.alter(3, 1);
        d.enter(3);
        assertEquals(1, d.goTo(3, 4));
    }

    @Test
    void aPerformsReturnLeavesTheSegmentAsItWas() {
        Dispatcher d = segmented();
        d.enter(2);
        d.alter(3, 1);
        d.enter(0);
        d.resume(2);
        d.enter(3);
        assertEquals(1, d.goTo(3, 4), "P2: the return to 60 is no entry, so 60 entering 60 resets nothing");
    }

    @Test
    void aFixedSegmentKeepsItsLastUsedState() {
        Dispatcher d = segmented();
        d.alter(1, 3);
        d.enter(2);
        d.enter(1);
        assertEquals(3, d.goTo(1, 2));
    }
}
