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
