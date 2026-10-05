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

    /** R-056's mutation for the trial: intermediates rounded instead of truncated, case 1 is 2.01, not cobc's 1.98. */
    @Test
    void roundingTheIntermediatesWouldFailTheTrial() {
        assertEquals("1.98", ArithTrialIntegrationTest.rt(RoundingMode.DOWN).get(0));
        assertEquals("2.01", ArithTrialIntegrationTest.rt(RoundingMode.HALF_UP).get(0));
    }
}
