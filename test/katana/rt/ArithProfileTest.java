package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

/**
 * IBM's Appendix A place tables (ArithProfile), held to the text (docs/research/ibm-options.md §4) row
 * by row, and to the one measured point: `COMPUTE X = 1 / 3 * 300` into `999V99` is 99.00 under
 * `cobc -std=ibm` (oracle/nist/IBM-CHANGES.md), which the table gives and an exact evaluation does not.
 */
class ArithProfileTest {
    private static final ArithProfile COMPAT = ArithProfile.IBM_COMPAT, EXTEND = ArithProfile.IBM_EXTEND;

    @Test
    void theTheoreticalPlaces() {
        assertEquals(new ArithProfile.Places(6, 3), COMPAT.add(5, 2, 3, 3, 0));
        assertEquals(new ArithProfile.Places(8, 5), COMPAT.multiply(5, 2, 3, 3, 0));
        // 12345.67 / 1.234: op1 the divisor (1, 3), op2 the dividend (5, 2); i = 5 + 3, d = max(2 - 3, dmax 4)
        assertEquals(new ArithProfile.Places(8, 4), COMPAT.divide(1, 3, 5, 2, 4));
    }

    @Test
    void eachRowOfTheCapTable() {
        assertEquals(new ArithProfile.Places(20, 10), COMPAT.carried(20, 10, 2));
        // over 30 with d <= dmax: integer places lost
        assertEquals(new ArithProfile.Places(25, 5), COMPAT.carried(28, 5, 6));
        // over 30, d > dmax, i + dmax <= 30: decimals cut to 30 - i
        assertEquals(new ArithProfile.Places(20, 10), COMPAT.carried(20, 15, 4));
        // over 30, d > dmax, i + dmax > 30: 30 - dmax and dmax
        assertEquals(new ArithProfile.Places(26, 4), COMPAT.carried(28, 10, 4));
        // the same rows at 31 under ARITH(EXTEND)
        assertEquals(new ArithProfile.Places(20, 11), EXTEND.carried(20, 15, 4));
        assertEquals(new ArithProfile.Places(28, 10), ArithProfile.EXACT.carried(28, 10, 4));
    }

    /** Exponentiation's places (PG 6.4 pp. 796-797), each rule as the text words it. */
    @Test
    void exponentiationsPlaces() {
        // an integral literal exponent: d = d1 * |n|, i = i1 * |n| for a data-name op1
        assertEquals(new ArithProfile.Places(9, 6), COMPAT.power(3, 2, 3L, -1, 4));
        assertEquals(new ArithProfile.Places(9, 6), COMPAT.power(3, 2, -3L, -1, 4));
        // a literal op1: i the integers of op1 ** |n| (12 ** 3 = 1728, four)
        assertEquals(new ArithProfile.Places(4, 0), COMPAT.power(2, 0, 3L, 4, 0));
        // i + d at 30 with op1 of an odd number of digits: carried; of an even number: as a data-name exponent
        assertEquals(new ArithProfile.Places(30, 0), COMPAT.power(3, 0, 10L, -1, 2));
        assertEquals(new ArithProfile.Places(28, 2), COMPAT.power(2, 0, 15L, -1, 2));
        // a data-name or variable exponent: 30 - dmax integer and dmax decimal places, 31 - dmax under EXTEND
        assertEquals(new ArithProfile.Places(26, 4), COMPAT.power(3, 2, null, -1, 4));
        assertEquals(new ArithProfile.Places(27, 4), EXTEND.power(3, 2, null, -1, 4));
    }

    /** The value: exact repeated multiplication cut to the places; zero gives 1; a negative exponent divides 1 by the result. */
    @Test
    void exponentiationsValue() {
        Decimal.Term twelve = Decimal.operand(Decimal.of("12"), 3), threeHalves = Decimal.operand(Decimal.of("1.5"), 1);
        assertEquals("1728", Decimal.power(COMPAT, twelve, Decimal.operand(Decimal.of("3"), 1), null, false, 0, true, false).value().toString());
        assertEquals("3.375", Decimal.power(COMPAT, threeHalves, Decimal.operand(Decimal.of("3"), 1), 3L, false, 0, true, false).value().toString());
        assertEquals("1", Decimal.power(COMPAT, twelve, Decimal.operand(Decimal.of("0"), 1), 0L, false, 0, true, false).value().toString());
        assertEquals("0.666666", Decimal.power(COMPAT, threeHalves, Decimal.operand(Decimal.of("-1"), 1), -1L, false, 6, true, false).value().toString());
        Decimal.Term zero = Decimal.operand(Decimal.of("0"), 1);
        assertEquals(true, Decimal.power(COMPAT, zero, Decimal.operand(Decimal.of("-1"), 1), -1L, false, 2, true, false).divisionByZero());
    }

    /** 1 / 3 * 300 into 999V99: dmax 2 (the receiver); 1 / 3 carries 2 decimals, 0.33; times 300 is 99.00. */
    @Test
    void theMeasuredReproducer() {
        int dmax = 2;
        ArithProfile.Places quotient = COMPAT.divide(1, 0, 1, 0, dmax);
        assertEquals(new ArithProfile.Places(1, 2), quotient);
        BigDecimal ir1 = BigDecimal.ONE.divide(BigDecimal.valueOf(3), quotient.decimal(), RoundingMode.DOWN);
        ArithProfile.Places product = COMPAT.multiply(quotient.integer(), quotient.decimal(), 3, 0, dmax);
        BigDecimal ir2 = ir1.multiply(BigDecimal.valueOf(300)).setScale(product.decimal(), RoundingMode.DOWN);
        assertEquals("99.00", ir2.setScale(2, RoundingMode.DOWN).toPlainString());
    }

    /**
     * R-056's mutations for the trial: each wrong reading turns it red. Intermediates rounded, case 1 is
     * 2.01; ROUNDED adding no place, case 5 is 0.6; ROUNDED's place in dmax, case 2 is 2.0.
     */
    @Test
    void eachWrongReadingWouldFailTheTrial() {
        var truth = ArithTrialIntegrationTest.EXPECTED;
        assertEquals(truth, ArithTrialIntegrationTest.rt(RoundingMode.DOWN, ArithTrialIntegrationTest.Reading.LAST));
        assertEquals("2.01", ArithTrialIntegrationTest.rt(RoundingMode.HALF_UP, ArithTrialIntegrationTest.Reading.LAST).get(0));
        assertEquals("0.6", ArithTrialIntegrationTest.rt(RoundingMode.DOWN, ArithTrialIntegrationTest.Reading.NONE).get(4));
        assertEquals("2.0", ArithTrialIntegrationTest.rt(RoundingMode.DOWN, ArithTrialIntegrationTest.Reading.DMAX).get(1));
    }
}
