package katana.intent;

import katana.support.Small;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import katana.intent.types.Subject;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** T-F3 on traces small enough to read, in rt/PerformTrace's form (research §33, §34): a range only PERFORMed counts clean; a main line that falls into it counts as fallen in and out. */
class PerformTracesTest {
    private static final Program P = Small.program("P", "", "MAIN.\n    PERFORM A.\n    STOP RUN.\nA.\n    DISPLAY 'A'.\n");

    private static final int A = P.procedure().paragraphs().stream().filter(g -> g.name().equals("A")).findFirst().orElseThrow().index();

    /** The lines, `a` the index of A and `m` of MAIN, `z` the paragraph after A. */
    private static PerformTraces.Counts counts(String... lines) {
        for (int i = 0; i < lines.length; i++) lines[i] = lines[i].replace("{a}", String.valueOf(A)).replace("{m}", String.valueOf(A - 1)).replace("{z}", String.valueOf(A + 1));
        Map<Subject.Range, PerformTraces.Counts> all = PerformTraces.counts(P, List.of(List.of(lines)));
        assertEquals(1, all.size(), "the rules infer A a function");
        return all.values().iterator().next();
    }

    @Test
    void aRangeOnlyPerformedCountsClean() {
        assertEquals("performed 1 | entered 1 | returned 1 | entered otherwise 0 | fell in 0 | fell out 0", counts("Enter: P MAIN", "Perform: P {a} {a}", "Enter: P A", "Return: P {a}").line());
    }

    @Test
    void aMainLineThatFallsIntoItIsCounted() {
        assertEquals("performed 0 | entered 1 | returned 0 | entered otherwise 0 | fell in 1 | fell out 1", counts("Enter: P MAIN", "Fall: P {m} {a}", "Enter: P A", "Fall: P {a} {z}").line());
        assertEquals("performed 0 | entered 1 | returned 0 | entered otherwise 1 | fell in 0 | fell out 0", counts("Enter: P MAIN", "Enter: P A", "Exit: P 0").line(), "a GO TO in");
        assertEquals("performed 0 | entered 0 | returned 0 | entered otherwise 0 | fell in 0 | fell out 0", counts("Enter: Q A", "Fall: Q {a} {z}").line(), "another program's lines");
    }
}
