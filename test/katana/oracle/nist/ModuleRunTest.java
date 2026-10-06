package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** An abnormal ending's reason (LANE-08): libcob's message and rt's give one form, and only a file status 35 is OPEN's. */
class ModuleRunTest {
    @Test
    void bothLanesStateTheSameReason() {
        String reason = "OPEN TFIL status 35 on TAPE1";
        assertEquals(reason, ModuleRun.reason("libcob: error: file does not exist (status = 35) for file TFIL ('TAPE1')\n"));
        assertEquals(reason, ModuleRun.reason("PRINTED LINE\nCEE3ABD abend U4038\nOPEN TFIL: file status 35 on TAPE1\n"));
    }

    @Test
    void anotherStatusOrNoStatusIsNoReason() {
        assertNull(ModuleRun.reason("libcob: error: permanent error (status = 30) for file TFIL ('TAPE1')\n"));
        assertNull(ModuleRun.reason("libcob: error: subscript of 'T' out of bounds: 4\n"));
    }
}
