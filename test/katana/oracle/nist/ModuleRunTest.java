package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** An abnormal ending's reason (LANE-08): libcob's message and rt's give one form; only a file status 35 keeps its statement, OPEN. */
class ModuleRunTest {
    @Test
    void bothLanesStateTheSameReason() {
        String reason = "OPEN TFIL status 35 on TAPE1";
        assertEquals(reason, ModuleRun.reason("libcob: error: file does not exist (status = 35) for file TFIL ('TAPE1')\n"));
        assertEquals(reason, ModuleRun.reason("PRINTED LINE\nCEE3ABD abend U4038\nOPEN TFIL: file status 35 on TAPE1\n"));
    }

    @Test
    void anotherStatusNamesNoStatementAndNoStatusIsNoReason() {
        assertEquals("TFIL status 38 on TAPE2", ModuleRun.reason("libcob: error: unknown file error (status = 38) for file TFIL ('TAPE2')\n"));
        assertEquals("TFIL status 38 on TAPE2", ModuleRun.reason("CEE3ABD abend U4038\nOPEN TFIL: file status 38 on TAPE2\n"));
        assertNull(ModuleRun.reason("libcob: error: subscript of 'T' out of bounds: 4\n"));
    }
}
