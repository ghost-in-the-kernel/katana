package katana.rt.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** What `CaseCommand` reads of an expected check's shell text (TODO 3.1, J-278); the text is never run. */
class CaseCommandTest {
    @Test
    void thePlainCommandGivesTheRunNothing() {
        CaseCommand c = CaseCommand.read("$COBCRUN_DIRECT ./prog\n");
        assertNull(c.unread());
        assertEquals(Map.of(), c.env());
        assertEquals(List.of(), c.args());
        assertNull(c.stdin());
        assertEquals(-1, c.head());
    }

    @Test
    void prefixedVariablesQuotesAndContinuationsAreRead() {
        CaseCommand c = CaseCommand.read("COB_LS_SPLIT=FALSE DD_INFILE=./inp_data OUTFILE=TEST-FILE \\\n$COBCRUN_DIRECT ./prog\n");
        assertNull(c.unread());
        assertEquals(Map.of("COB_LS_SPLIT", "FALSE", "DD_INFILE", "./inp_data", "OUTFILE", "TEST-FILE"), c.env());
        assertEquals("2015/02/06 16:40:52", CaseCommand.read("COB_CURRENT_DATE=\"2015/02/06 16:40:52\" $COBCRUN_DIRECT ./prog").env().get("COB_CURRENT_DATE"));
        assertEquals("\"QUOTE\"", CaseCommand.read("FILENAME2=\"\\\"QUOTE\\\"\" $COBCRUN_DIRECT ./prog2").env().get("FILENAME2"));
        assertEquals("'APOST'", CaseCommand.read("FILENAME2=\\'APOST\\' $COBCRUN_DIRECT ./prog2").env().get("FILENAME2"));
        assertEquals("cat >>prt.log", CaseCommand.read("COB_DISPLAY_PRINT_PIPE='cat >>prt.log' \\\nCOB_DISPLAY_PUNCH_FILE='punch.out' \\\n$COBCRUN_DIRECT ./prog PIPE.").env().get("COB_DISPLAY_PRINT_PIPE"));
    }

    @Test
    void anExportLineBeforeTheCommandIsTheEnvironment() {
        CaseCommand c = CaseCommand.read("export COB_SOMETHING=x\n$COBCRUN_DIRECT ./prog\n");
        assertNull(c.unread());
        assertEquals(Map.of("COB_SOMETHING", "x"), c.env());
    }

    @Test
    void argumentsKeepTheirQuotingAndTheCommandNeedsNoWrapper() {
        assertEquals(List.of("a b", "param", "c'd"), CaseCommand.read("X=1 $COBCRUN_DIRECT ./prog \"a b\" param 'c'\\''d'").args());
        CaseCommand bare = CaseCommand.read("X=1 ./prog arg");
        assertNull(bare.unread());
        assertEquals(List.of("arg"), bare.args());
        assertEquals(List.of("v"), CaseCommand.read("$COBCRUN_DIRECT prog v").args());
    }

    @Test
    void anInputFileAHeadPipeAndAStderrRedirectAreRead() {
        assertEquals("input.txt", CaseCommand.read("$COBCRUN_DIRECT ./prog < input.txt").stdin());
        CaseCommand head = CaseCommand.read("$COBCRUN_DIRECT ./prog | head -n 2");
        assertNull(head.unread());
        assertEquals(2, head.head());
        CaseCommand err = CaseCommand.read("A=1 B=2 $COBCRUN_DIRECT ./prog 2>stderr.txt");
        assertNull(err.unread());
        assertEquals(List.of(), err.args());
    }

    /** R-056's mutation: a shape the reader does not know is unread, not guessed at; reading `cat f | prog` as a plain run would lose its input silently. */
    @Test
    void aShapeItDoesNotReadIsUnreadWithNothingGiven() {
        for (String shell : List.of("cat TEST-FILE | $COBCRUN_DIRECT ./prog", "echo \"a\" && $COBCRUN_DIRECT ./prog && echo b", "A=1 $COBCRUN_DIRECT ./prog 2>e & cobpid=$!\nsleep 1\nkill -2 $cobpid",
                "$COBCRUN_DIRECT ./prog > out.txt", "$COBCRUN_DIRECT ./prog | sort", "$COBCRUN_DIRECT ./prog | head -n 2 | cat", "$COBCRUN_DIRECT ./prog $HOME", "$DIFF a b", "$COBCRUN_DIRECT ./prog < ../x", "X=\"unclosed $COBCRUN_DIRECT ./prog")) {
            CaseCommand c = CaseCommand.read(shell);
            assertNotNull(c.unread(), shell);
            assertEquals(Map.of(), c.env(), shell);
            assertEquals(List.of(), c.args(), shell);
            assertEquals(-1, c.head(), shell);
        }
        assertFalse(CaseCommand.read("$COBCRUN_DIRECT ./prog | head -n 2").head() < 0);
        assertEquals("an expansion in the value of COB_PRE_LOAD", CaseCommand.read("COB_PRE_LOAD=\"prog\"$PATHSEP\"prog1\" $COBCRUN_DIRECT ./main-prog").unread());
    }

    @Test
    void theLanesNamesAreTranslatedAndNothingElse() {
        CaseCommand c = CaseCommand.read("COB_CURRENT_DATE=\"2015/02/06 16:40:52\" COB_SET_DEBUG=1 DD_X=y $COBCRUN_DIRECT ./prog");
        Map<String, String> env = CaseRun.environment(c);
        assertEquals("2015-02-06 16:40:52.00 +00:00", env.get("COB_CURRENT_DATE"));
        assertEquals("1", env.get("COB_SET_DEBUG"));
        assertEquals("y", env.get("DD_X"));
        assertEquals(3, env.size());
        assertEquals(true, CaseRun.debug("1") && CaseRun.debug("Y") && CaseRun.debug("y"));
        assertEquals(false, CaseRun.debug("0") || CaseRun.debug(null) || CaseRun.debug(""));
        assertEquals("2024-03-01 12:00:00.00 +00:00", CaseRun.environment(CaseCommand.read("COB_CURRENT_DATE=\"2024-03-01 12:00:00.00 +00:00\" ./prog")).get("COB_CURRENT_DATE"));
    }
}
