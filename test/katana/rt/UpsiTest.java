package katana.rt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** LE's UPSI option (SA38-0683-60 p. 87): eight bits, the leftmost UPSI-0. */
class UpsiTest {
    @Test
    void theLeftmostBitIsTheFirstSwitch() {
        Upsi u = Upsi.of("10000001");
        assertTrue(u.on(0));
        assertFalse(u.on(1));
        assertTrue(u.on(7));
    }

    @Test
    void anythingButEightBitsIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> Upsi.of("1"));
        assertThrows(IllegalArgumentException.class, () -> Upsi.of("0000000x"));
    }
}
