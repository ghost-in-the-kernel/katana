package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Research §41's ground: Appendix A's step-wise `+ - *`, each operand within its places and every step within
 * the cap, carries the exact value, ROUNDED's last place included; so `Decimal.exact` of the exact value stores
 * what the step-wise term stores. Seeded, so a failure repeats.
 */
class ExactArithmeticTest {
    @Test
    void stepWisePlusMinusTimesWithinTheCapIsExact() {
        Random r = new Random(41);
        ArithProfile p = ArithProfile.IBM_COMPAT;
        int checked = 0;
        for (int k = 0; k < 5000; k++) {
            Decimal.Term a = operand(r), b = operand(r), c = operand(r);
            int op1 = r.nextInt(3), op2 = r.nextInt(3);
            boolean rounded = r.nextBoolean();
            Decimal.Term ab = apply(p, op1, a, b, false, false);
            if (ab.places().integer() + ab.places().decimal() > p.cap) continue;
            Decimal.Term step = apply(p, op2, ab, c, true, rounded);
            int i = op2 == 2 ? ab.places().integer() + c.places().integer() : Math.max(ab.places().integer(), c.places().integer()) + 1;
            int d = op2 == 2 ? ab.places().decimal() + c.places().decimal() : Math.max(ab.places().decimal(), c.places().decimal());
            if (i + d > p.cap) continue;
            BigDecimal exact = exact(op2, exact(op1, a.value().toBigDecimal(), b.value().toBigDecimal()), c.value().toBigDecimal());
            assertEquals(0, step.value().toBigDecimal().compareTo(exact), a.value() + " " + op1 + " " + b.value() + " " + op2 + " " + c.value());
            Decimal.Stored s1 = Decimal.store(step, 7, 2, true, rounded), s2 = Decimal.store(Decimal.exact(exact), 7, 2, true, rounded);
            assertEquals(s1.value().toBigDecimal(), s2.value().toBigDecimal());
            assertEquals(s1.sizeError(), s2.sizeError());
            checked++;
        }
        assertEquals(true, checked > 4000, checked + " cases within the cap");
    }

    /**
     * R-069 C-2: an operand whose bytes are no valid number within its PICTURE (a non-digit, a bad overpunched sign, a
     * packed spare nibble, a binary value past its digits) fails the guard, and the guarded statement stores what the
     * step-by-step one stores; with the guard removed, the exact form stores otherwise on at least one (the mutation).
     */
    @Test
    void theGuardRefusesInvalidBytesAndWithoutItTheExactFormDiffers() {
        Codepage cp = Codepage.COBC_ASCII;
        java.util.List<Field> invalid = java.util.List.of(
                new Field(new Record(new byte[] {cp.digit(1, 0), cp.character('A'), cp.digit(3, 0)}, cp), 0, Attr.numeric(Attr.Usage.DISPLAY, 3, 0, false)),
                new Field(new Record(new byte[] {cp.digit(1, 0), cp.digit(2, 0), cp.character('Z')}, cp), 0, Attr.numeric(Attr.Usage.DISPLAY, 3, 0, true)),
                new Field(new Record(new byte[] {(byte) 0x79, (byte) 0x9F}, cp), 0, Attr.numeric(Attr.Usage.PACKED, 2, 0, false)),
                new Field(new Record(new byte[] {(byte) 0x13, (byte) 0x88}, cp), 0, Attr.numeric(Attr.Usage.BINARY, 2, 0, false)));
        ArithProfile p = ArithProfile.IBM_COMPAT;
        Decimal.Term one = Decimal.operand(new Decimal(java.math.BigInteger.ONE, 0), 1);
        boolean differs = false;
        for (Field f : invalid) {
            assertEquals(false, Classes.exact(f), f.attr() + ": the guard refuses it");
            Decimal.Term step = Decimal.add(p, Decimal.operand(f.get(), f.attr().digits() - f.attr().scale()), one, 0, true, false);
            Decimal.Term guarded = Classes.exact(f) ? Decimal.exact(f.get().toBigDecimal().add(BigDecimal.ONE)) : step;
            Decimal.Stored a = Decimal.store(guarded, 3, 0, false, false), b = Decimal.store(step, 3, 0, false, false);
            assertEquals(b.value().toBigDecimal(), a.value().toBigDecimal(), f.attr() + ": the guarded form stores the step-by-step value");
            assertEquals(b.sizeError(), a.sizeError());
            Decimal.Stored unguarded = Decimal.store(Decimal.exact(f.get().toBigDecimal().add(BigDecimal.ONE)), 3, 0, false, false);
            differs |= !unguarded.value().toBigDecimal().equals(b.value().toBigDecimal()) || unguarded.sizeError() != b.sizeError();
        }
        assertEquals(true, differs, "without the guard, the exact form stores otherwise on an invalid operand: the guard is needed");
        Field valid = new Field(new Record(new byte[] {cp.digit(4, 0), cp.digit(2, 0)}, cp), 0, Attr.numeric(Attr.Usage.DISPLAY, 2, 0, false));
        assertEquals(true, Classes.exact(valid), "a valid field passes");
    }

    /** A value of up to 12 integer and 4 decimal digits, signed, as an operand of exactly those places. */
    private static Decimal.Term operand(Random r) {
        int integers = 1 + r.nextInt(12), decimals = r.nextInt(5);
        BigInteger unscaled = new BigInteger(Long.toString(Math.floorMod(r.nextLong(), (long) Math.pow(10, Math.min(18, integers + decimals)))));
        if (r.nextBoolean()) unscaled = unscaled.negate();
        return Decimal.operand(new Decimal(unscaled, decimals), integers);
    }

    private static Decimal.Term apply(ArithProfile p, int op, Decimal.Term a, Decimal.Term b, boolean last, boolean rounded) {
        return switch (op) {
            case 0 -> Decimal.add(p, a, b, 0, last, rounded);
            case 1 -> Decimal.subtract(p, a, b, 0, last, rounded);
            default -> Decimal.multiply(p, a, b, 0, last, rounded);
        };
    }

    private static BigDecimal exact(int op, BigDecimal a, BigDecimal b) {
        return op == 0 ? a.add(b) : op == 1 ? a.subtract(b) : a.multiply(b);
    }
}
