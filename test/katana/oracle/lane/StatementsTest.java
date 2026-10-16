package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** CREASTMT's statement check (research §25) fails on a statement whose total is not its lines' sum, and on a transaction left out. */
class StatementsTest {
    private static String line(String s) {
        return String.format("%-80s", s);
    }

    private static byte[] statements(String total) {
        return (line("0000000000000001 Purchase at A                                    $       10.00 ")
                + line("0000000000000002 Return item at B                                 $        2.50-") + line("Total EXP:                                                        $        " + total + " "))
                .getBytes(StandardCharsets.ISO_8859_1);
    }

    private static byte[] transactions(String... ids) {
        StringBuilder t = new StringBuilder();
        for (String id : ids) t.append(String.format("%-16s%s%-318s", "4111", id, ""));
        return t.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    @Test
    void aStatementHoldsItsTransactionsAndTheirSum() {
        assertEquals("1 statements, 2 transactions, each once, every total its lines' sum", Statements.check(statements("7.50"), transactions("0000000000000001", "0000000000000002")));
    }

    @Test
    void aWrongTotalOrAMissingTransactionIsCaught() {
        assertTrue(Statements.check(statements("8.50"), transactions("0000000000000001", "0000000000000002")).startsWith("statement 1: its lines sum to 7.50"));
        assertTrue(Statements.check(statements("7.50"), transactions("0000000000000001", "0000000000000002", "0000000000000003")).startsWith("the statements list 2 transactions, the input holds 3"));
    }
}
