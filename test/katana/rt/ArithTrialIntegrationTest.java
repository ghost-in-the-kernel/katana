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
 * The intermediate-results trial (phase 3.1, J-096), cobc -std=ibm its oracle: `A / B * B` with A 2 and
 * B 3 into 9V99, into 9V9 ROUNDED and into 9V9, and `N / B * H` (1 / 3 * 300) into 999V99. The
 * expected values were written before the run: under ArithProfile's tables with truncated
 * intermediates and no ROUNDED place in dmax, 1.98, 1.8, 1.8 and 99.00. Data items, not literals: cobc
 * folds an all-literal expression at compile time with more places (1.99, 1.9), which IBM's text has no
 * rule for (a finding against cobc's dialect, not a reading of IBM).
 */
@Tag("integration")
class ArithTrialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    static final List<String> EXPECTED = List.of("1.98", "1.8", "1.8", "99.00");

    /** `n / d * m` as ArithProfile carries it, intermediates truncated, stored into `scale` places, rounded half up when asked. */
    static String compute(long n, long d, long m, int mDigits, int scale, boolean rounded, RoundingMode intermediate) {
        ArithProfile p = ArithProfile.IBM_COMPAT;
        int dmax = ArithProfile.dmax(scale, 0, 0);
        ArithProfile.Places q = p.divide(1, 0, 1, 0, dmax);
        BigDecimal ir1 = BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d), q.decimal(), intermediate);
        ArithProfile.Places r = p.multiply(q.integer(), q.decimal(), mDigits, 0, dmax);
        BigDecimal ir2 = ir1.multiply(BigDecimal.valueOf(m)).setScale(r.decimal(), intermediate);
        return ir2.setScale(scale, rounded ? RoundingMode.HALF_UP : RoundingMode.DOWN).toPlainString();
    }

    static List<String> rt(RoundingMode intermediate) {
        return List.of(compute(2, 3, 3, 1, 2, false, intermediate), compute(2, 3, 3, 1, 1, true, intermediate),
                compute(2, 3, 3, 1, 1, false, intermediate), compute(1, 3, 300, 3, 2, false, intermediate));
    }

    @Test
    void cobcAndTheTablePrintTheSame() throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-trial"));
        Files.writeString(dir.resolve("TRIAL.cbl"), String.join("\n", "IDENTIFICATION DIVISION.", "PROGRAM-ID. TRIAL.", "DATA DIVISION.",
                "WORKING-STORAGE SECTION.", "01 A PIC 9 VALUE 2.", "01 B PIC 9 VALUE 3.", "01 N PIC 9 VALUE 1.", "01 H PIC 999 VALUE 300.",
                "01 X2 PIC 9V99.", "01 R1 PIC 9V9.", "01 T1 PIC 9V9.", "01 Q PIC 999V99.", "PROCEDURE DIVISION.",
                "COMPUTE X2 = A / B * B", "COMPUTE R1 ROUNDED = A / B * B", "COMPUTE T1 = A / B * B", "COMPUTE Q = N / B * H",
                "DISPLAY X2 \" \" R1 \" \" T1 \" \" Q", "STOP RUN.") + "\n");
        Cobc cobc = new Cobc(Duration.ofSeconds(60), "ibm");
        assertEquals(0, cobc.compile(dir, "TRIAL.cbl", List.of("-free")).exit());
        String[] printed = cobc.execute(dir, "TRIAL", Map.of()).output().trim().split(" ");
        int[] scales = {2, 1, 1, 2};
        String[] values = new String[4];
        for (int i = 0; i < 4; i++) values[i] = new BigDecimal(new java.math.BigInteger(printed[i]), scales[i]).toPlainString();
        assertEquals(EXPECTED, List.of(values), "cobc -std=ibm");
        assertEquals(EXPECTED, rt(RoundingMode.DOWN), "ArithProfile, intermediates truncated");
    }
}
