package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The intermediate-results trial (phase 3.1, J-097, J-099), cobc -std=ibm its oracle, A 2, B 3, N 1, H 300:
 * `A / B * B` into 9V99, into 9V9 ROUNDED and into 9V9; `N / B * H` into 999V99; `A / B` into 9V9 ROUNDED
 * and into 9V9; `DIVIDE A BY B GIVING R ROUNDED` into 9V9. Each case's value was written before its run.
 * The three readings of ROUNDED each give a different list, so the cases decide between them: NONE (no
 * extra place: case 5 is 0.6), DMAX (one more place in dmax, every intermediate: case 2 is 2.0), and
 * LAST (one more place on the last operation, `ArithProfile.last`), the one cobc prints. Data items, not
 * literals: cobc folds an all-literal expression at compile time with more places (D-002).
 */
@Tag("integration")
class ArithTrialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    static final List<String> EXPECTED = List.of("1.98", "1.8", "1.8", "99.00", "0.7", "0.6", "0.7");

    /** Where ROUNDED's extra decimal place goes: nowhere, into dmax, or into the last operation. */
    enum Reading { NONE, DMAX, LAST }

    /** `n / d`, times `m` when `m` is not 0, as ArithProfile carries it under a reading; stored into `scale` places, rounded half up when asked. */
    static String compute(long n, long d, long m, int mDigits, int scale, boolean rounded, RoundingMode intermediate, Reading reading) {
        ArithProfile p = ArithProfile.IBM_COMPAT;
        int dmax = ArithProfile.dmax(scale + (rounded && reading == Reading.DMAX ? 1 : 0), 0, 0);
        ArithProfile.Places q = p.divide(1, 0, 1, 0, dmax);
        if (m == 0 && reading == Reading.LAST) q = p.last(q, rounded);
        BigDecimal ir = BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d), q.decimal(), intermediate);
        if (m != 0) {
            ArithProfile.Places r = p.multiply(q.integer(), q.decimal(), mDigits, 0, dmax);
            if (reading == Reading.LAST) r = p.last(r, rounded);
            ir = ir.multiply(BigDecimal.valueOf(m)).setScale(r.decimal(), intermediate);
        }
        return ir.setScale(scale, rounded ? RoundingMode.HALF_UP : RoundingMode.DOWN).toPlainString();
    }

    /** The seven cases under a rule for intermediates and a reading of ROUNDED. */
    static List<String> rt(RoundingMode intermediate, Reading reading) {
        return List.of(compute(2, 3, 3, 1, 2, false, intermediate, reading), compute(2, 3, 3, 1, 1, true, intermediate, reading),
                compute(2, 3, 3, 1, 1, false, intermediate, reading), compute(1, 3, 300, 3, 2, false, intermediate, reading),
                compute(2, 3, 0, 0, 1, true, intermediate, reading), compute(2, 3, 0, 0, 1, false, intermediate, reading),
                compute(2, 3, 0, 0, 1, true, intermediate, reading));
    }

    @Test
    void cobcAndTheTablePrintTheSame() throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-trial"));
        Files.writeString(dir.resolve("TRIAL.cbl"), String.join("\n", "IDENTIFICATION DIVISION.", "PROGRAM-ID. TRIAL.", "DATA DIVISION.",
                "WORKING-STORAGE SECTION.", "01 A PIC 9 VALUE 2.", "01 B PIC 9 VALUE 3.", "01 N PIC 9 VALUE 1.", "01 H PIC 999 VALUE 300.",
                "01 X2 PIC 9V99.", "01 R1 PIC 9V9.", "01 T1 PIC 9V9.", "01 Q PIC 999V99.", "01 R5 PIC 9V9.", "01 T6 PIC 9V9.", "01 R7 PIC 9V9.",
                "PROCEDURE DIVISION.",
                "COMPUTE X2 = A / B * B", "COMPUTE R1 ROUNDED = A / B * B", "COMPUTE T1 = A / B * B", "COMPUTE Q = N / B * H",
                "COMPUTE R5 ROUNDED = A / B", "COMPUTE T6 = A / B", "DIVIDE A BY B GIVING R7 ROUNDED",
                "DISPLAY X2 \" \" R1 \" \" T1 \" \" Q \" \" R5 \" \" T6 \" \" R7", "STOP RUN.") + "\n");
        Cobc cobc = new Cobc(Duration.ofSeconds(60), "ibm");
        assertEquals(0, cobc.compile(dir, "TRIAL.cbl", List.of("-free")).exit());
        String[] printed = cobc.execute(dir, "TRIAL", Map.of()).output().trim().split(" ");
        int[] scales = {2, 1, 1, 2, 1, 1, 1};
        String[] values = new String[scales.length];
        for (int i = 0; i < scales.length; i++) values[i] = new BigDecimal(new java.math.BigInteger(printed[i]), scales[i]).toPlainString();
        assertEquals(EXPECTED, List.of(values), "cobc -std=ibm");
        assertEquals(EXPECTED, rt(RoundingMode.DOWN, Reading.LAST), "ArithProfile, intermediates truncated, ROUNDED's place on the last operation");
    }
}
