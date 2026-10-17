package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** The z/OS storage facade's blocks as research §25 reads them (R-063, hypothesis): PSA, TCB and TIOT from the step's facts. */
class StorageTest {
    private static String text(Field f) {
        return new String(f.record().bytes(), f.offset(), f.attr().length(), StandardCharsets.ISO_8859_1);
    }

    @Test
    void thePsaLeadsToTheTcbAndTheTcbToTheTiot() {
        Storage s = Storage.of("CREASTMT STEP040 SYSOUT:n TRNXFILE:u", Codepage.COBC_ASCII);
        int tcb = Storage.pointer(s.at(536, 4));
        assertEquals(tcb, Storage.pointer(s.at(540, 4)), "PSATNEW and PSATOLD: the one task's TCB");
        int tiot = Storage.pointer(s.at(tcb + 12, 4));
        assertEquals("CREASTMTSTEP040         ", text(s.at(tiot, 24)));
        assertEquals("SYSOUT  ", text(s.at(tiot + 28, 8)));
        Field length = s.at(tiot + 24, 1);
        assertEquals(20, length.record().bytes()[length.offset()], "an entry's length");
        assertEquals("\0\0\0", text(s.at(tiot + 24 + 17, 3)), "SYSOUT has no UCB");
        assertEquals("TRNXFILE", text(s.at(tiot + 44 + 4, 8)));
        assertEquals(false, text(s.at(tiot + 44 + 17, 3)).equals("\0\0\0"), "a dataset DD has one");
        assertEquals(0, Storage.pointer(s.at(tiot + 64, 4)), "a fullword of zeros ends the TIOT");
    }

    @Test
    void anAddressNoBlockHoldsEndsTheRun() {
        Storage s = Storage.of("", Codepage.COBC_ASCII);
        assertThrows(Abend.class, () -> s.at(4090, 8), "past the PSA's 4096 bytes");
        assertThrows(Abend.class, () -> s.at(0x7000, 4));
    }

    @Test
    void aPointerIsItsAddressBigEndian() {
        Field p = new Field(Record.of(4, Codepage.COBC_ASCII), 0, Attr.text(4, false));
        Storage.set(p, 0x00009018);
        assertEquals(0x00009018, Storage.pointer(p));
        assertEquals(0x18, p.record().bytes()[3]);
    }
}
