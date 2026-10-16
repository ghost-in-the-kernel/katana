package katana.rt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The java lane's z/OS storage facade (R-027, research §25): the few blocks a batch program reads through
 * a POINTER, built from the step's own facts. The PSA at address 0 holds the running TCB's address at
 * X'218' and X'21C'; the TCB holds the TIOT's at X'0C'; the TIOT holds the job, step and procedure step
 * names, then a 20-byte entry for each DD (its length, its name at +4, its UCB address in its last three
 * bytes, zeros for none), then a fullword of zeros. The layouts are what CBSTM03A declares and
 * recollection, not IBM's text (R-063, hypothesis). An address no block holds ends the run.
 */
public final class Storage {
    private static final int PSA = 0, TCB = 0x8000, TIOT = 0x9000, UCB = 0x00F000;
    private final TreeMap<Integer, Record> blocks = new TreeMap<>();
    private final Map<Record, Integer> bases = new java.util.IdentityHashMap<>();

    private Storage() {}

    /** A DD as the TIOT lists it: its name, and whether it has a UCB (a device; not SYSOUT, in-stream or DUMMY). */
    public record Dd(String name, boolean ucb) {}

    /**
     * The step's storage from `KATANA_TIOT`, the lane's `JOB STEP DD:u DD:n ...` (LANE-03); with none, a
     * TIOT naming no job, no step and no DD.
     */
    public static Storage of(String tiot, Codepage cp) {
        String[] words = tiot == null || tiot.isBlank() ? new String[0] : tiot.trim().split("\\s+");
        List<Dd> dds = new ArrayList<>();
        for (int k = 2; k < words.length; k++) dds.add(new Dd(words[k].substring(0, words[k].indexOf(':')), words[k].endsWith(":u")));
        return of(words.length > 0 ? words[0] : "", words.length > 1 ? words[1] : "", dds, cp);
    }

    /** The PSA, the TCB and the TIOT of one step (research §25, H1 and H2). */
    // Decision: R-063
    public static Storage of(String job, String step, List<Dd> dds, Codepage cp) {
        Storage s = new Storage();
        Record psa = new Record(new byte[4096], cp), tcb = new Record(new byte[256], cp), tiot = new Record(new byte[24 + 20 * dds.size() + 4], cp);
        word(psa, 0x218, TCB);
        word(psa, 0x21C, TCB);
        word(tcb, 0x0C, TIOT);
        text(tiot, 0, job, cp);
        text(tiot, 8, step, cp);
        text(tiot, 16, "", cp);
        for (int k = 0; k < dds.size(); k++) {
            int at = 24 + 20 * k;
            tiot.bytes()[at] = 20;
            text(tiot, at + 4, dds.get(k).name(), cp);
            if (dds.get(k).ucb()) {
                int ucb = UCB + 0x40 * (k + 1);
                tiot.bytes()[at + 17] = (byte) (ucb >>> 16);
                tiot.bytes()[at + 18] = (byte) (ucb >>> 8);
                tiot.bytes()[at + 19] = (byte) ucb;
            }
        }
        s.add(PSA, psa);
        s.add(TCB, tcb);
        s.add(TIOT, tiot);
        return s;
    }

    private void add(int base, Record r) {
        blocks.put(base, r);
        bases.put(r, base);
    }

    /** `length` bytes at an address, as a field over the block that holds them; an address no block holds is the run's abend. */
    // Decision: R-063
    // Unsure: the abend code is LE's for an unhandled condition (U4038); z/OS reports the program check as S0C4. Stays: no CardDemo run reaches one; a mainframe run of a stray pointer settles it.
    public Field at(int address, int length) {
        var block = blocks.floorEntry(address);
        if (block == null || address + length > block.getKey() + block.getValue().bytes().length) {
            throw new Abend(4038, "storage at X'" + Integer.toHexString(address).toUpperCase() + "' for " + length + " bytes is no block of the lane's z/OS storage");
        }
        return new Field(block.getValue(), address - block.getKey(), Attr.text(length, false));
    }

    /** The address of a field over a block of this storage (SET pointer TO ADDRESS OF). */
    public int addressOf(Field f) {
        Integer base = bases.get(f.record());
        if (base == null) throw new Abend(4038, "the address of an item outside the lane's z/OS storage is not held");
        return base + f.offset();
    }

    /** A pointer's address: its four bytes, big-endian (research §25). */
    public static int pointer(Field pointer) {
        byte[] b = pointer.record().bytes();
        int o = pointer.offset();
        return (b[o] & 0xFF) << 24 | (b[o + 1] & 0xFF) << 16 | (b[o + 2] & 0xFF) << 8 | (b[o + 3] & 0xFF);
    }

    /** A pointer set to an address. */
    public static void set(Field pointer, int address) {
        word(pointer.record(), pointer.offset(), address);
    }

    private static void word(Record r, int at, int value) {
        r.bytes()[at] = (byte) (value >>> 24);
        r.bytes()[at + 1] = (byte) (value >>> 16);
        r.bytes()[at + 2] = (byte) (value >>> 8);
        r.bytes()[at + 3] = (byte) value;
    }

    private static void text(Record r, int at, String s, Codepage cp) {
        byte[] b = String.format("%-8.8s", s).getBytes(cp.charset);
        System.arraycopy(b, 0, r.bytes(), at, 8);
    }
}
