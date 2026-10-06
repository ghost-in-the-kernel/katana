package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * PERFORM and GO TO (phase 3.2, R-021), cobc -std=ibm their oracle: 60 programs from a fixed seed, 20 more of a shape that separates two
 * readings and one written by hand to do so, each
 * 5 to 9 paragraphs of DISPLAYs, PERFORMs of a later paragraph or range (THRU), GO TOs anywhere
 * (some guarded by the step counter, some not) and fall-through, every statement counted and the program
 * stopped at 150. Each program's DISPLAY lines are its trace; `rt/Dispatcher` driven by the same
 * statements must give the same trace. Its mutation: the ANSI reading, where only the innermost
 * PERFORM's exit returns, differs.
 */
@Tag("integration")
class DispatcherDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final int LIMIT = 150;

    /** A statement: DISPLAY, PERFORM first THRU last, or GO TO target when the counter's value mod `mod` is not zero. */
    record Stmt(char kind, int a, int b, int mod) {}

    static List<List<List<Stmt>>> programs() {
        Random r = new Random(20261014L);
        List<List<List<Stmt>>> out = new ArrayList<>();
        for (int n = 0; n < 60; n++) {
            int count = 5 + r.nextInt(5);
            List<List<Stmt>> paragraphs = new ArrayList<>();
            for (int p = 0; p < count; p++) {
                List<Stmt> body = new ArrayList<>(List.of(new Stmt('D', 0, 0, 0)));
                int extra = r.nextInt(4);
                for (int k = 0; k < extra; k++) {
                    int pick = r.nextInt(5);
                    if (pick < 2 && p + 1 < count) {
                        int first = p + 1 + r.nextInt(count - p - 1), last = first + r.nextInt(count - first);
                        body.add(new Stmt('P', first, pick == 0 ? first : last, 0));
                    } else if (pick < 4) body.add(new Stmt('G', r.nextInt(count), 0, r.nextBoolean() ? 1000 : 2 + r.nextInt(3)));
                    else body.add(new Stmt('D', 0, 0, 0));
                }
                paragraphs.add(body);
            }
            out.add(paragraphs);
        }
        // A family that separates the readings (J-113): P0 performs P1 through Pk, P1 performs Pj, and Pj leaves by GO TO a later paragraph inside the outer range.
        for (int n = 0; n < 20; n++) {
            int count = 6 + r.nextInt(4), k = 3 + r.nextInt(count - 4), j = 2 + r.nextInt(k - 2), m = j + 1 + r.nextInt(k - j);
            List<List<Stmt>> paragraphs = new ArrayList<>();
            for (int p = 0; p < count; p++) {
                List<Stmt> body = new ArrayList<>(List.of(new Stmt('D', 0, 0, 0)));
                if (p == 0) body.addAll(List.of(new Stmt('P', 1, k, 0), new Stmt('D', 0, 0, 0), new Stmt('G', j, 0, 3)));
                if (p == 1) body.addAll(List.of(new Stmt('P', j, j, 0), new Stmt('D', 0, 0, 0)));
                if (p == j) body.add(new Stmt('G', m, 0, 1000));
                if (r.nextInt(3) == 0) body.add(new Stmt('D', 0, 0, 0));
                paragraphs.add(body);
            }
            out.add(paragraphs);
        }
        // Written to separate the readings (J-113): PERFORM P1 THRU P3, inside it PERFORM P2, which leaves by GO TO P3.
        out.add(List.of(
                List.of(new Stmt('P', 1, 3, 0), new Stmt('D', 0, 0, 0), new Stmt('G', 4, 0, 1000)),
                List.of(new Stmt('D', 0, 0, 0), new Stmt('P', 2, 2, 0), new Stmt('D', 0, 0, 0)),
                List.of(new Stmt('D', 0, 0, 0), new Stmt('G', 3, 0, 1000)),
                List.of(new Stmt('D', 0, 0, 0)),
                List.of(new Stmt('D', 0, 0, 0))));
        return out;
    }

    static String program(List<List<Stmt>> paragraphs) {
        StringBuilder s = new StringBuilder("IDENTIFICATION DIVISION.\nPROGRAM-ID. FLOW.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n01 STEPS PIC 9(4) VALUE 0.\n01 R PIC 9(4).\n01 Q PIC 9(4).\nPROCEDURE DIVISION.\n");
        for (int p = 0; p < paragraphs.size(); p++) {
            s.append("P").append(p).append(".\n");
            for (int k = 0; k < paragraphs.get(p).size(); k++) {
                Stmt st = paragraphs.get(p).get(k);
                s.append("    ADD 1 TO STEPS IF STEPS > ").append(LIMIT).append(" DISPLAY \"LIMIT\" STOP RUN END-IF\n");
                switch (st.kind()) {
                    case 'D' -> s.append("    DISPLAY \"P").append(p).append('-').append(k).append("\"\n");
                    case 'P' -> s.append("    PERFORM P").append(st.a()).append(st.a() == st.b() ? "" : " THRU P" + st.b()).append('\n');
                    default -> s.append("    DIVIDE STEPS BY ").append(st.mod()).append(" GIVING Q REMAINDER R\n    IF R NOT = 0 GO TO P").append(st.a()).append(" END-IF\n");
                }
            }
            s.append("    .\n");
        }
        return s + "P-END.\n    DISPLAY \"END\"\n    STOP RUN.\n";
    }

    /** The lowering's shape, run: a label per paragraph and per statement after a PERFORM; `ansi` checks only the innermost PERFORM (the mutation). */
    static List<String> rt(List<List<Stmt>> paragraphs, boolean ansi) {
        List<String> trace = new ArrayList<>();
        Dispatcher d = new Dispatcher(paragraphs.size());
        java.util.ArrayDeque<Integer> ansiEnds = new java.util.ArrayDeque<>(), ansiBacks = new java.util.ArrayDeque<>();
        int steps = 0, pc = 0;
        for (;;) {
            int p = pc / 100, k = pc % 100;
            if (p == paragraphs.size()) {
                trace.add("END");
                return trace;
            }
            List<Stmt> body = paragraphs.get(p);
            if (k == body.size()) {
                int next = (p + 1) * 100;
                if (ansi) pc = !ansiEnds.isEmpty() && ansiEnds.peek() == p ? ansiPop(ansiEnds, ansiBacks) : next;
                else pc = d.end(p, next);
                continue;
            }
            if (++steps > LIMIT) {
                trace.add("LIMIT");
                return trace;
            }
            Stmt st = body.get(k);
            switch (st.kind()) {
                case 'D' -> {
                    trace.add("P" + p + "-" + k);
                    pc++;
                }
                case 'P' -> {
                    if (ansi) {
                        ansiEnds.push(st.b());
                        ansiBacks.push(pc + 1);
                        pc = st.a() * 100;
                    } else pc = d.perform(st.a() * 100, st.b(), pc + 1);
                }
                default -> pc = steps % st.mod() != 0 ? st.a() * 100 : pc + 1;
            }
        }
    }

    private static int ansiPop(java.util.ArrayDeque<Integer> ends, java.util.ArrayDeque<Integer> backs) {
        ends.pop();
        return backs.pop();
    }

    static List<List<String>> cobc(List<List<List<Stmt>>> programs) throws Exception {
        List<List<String>> traces = new ArrayList<>();
        Cobc cobc = new Cobc(Duration.ofSeconds(60), "ibm");
        for (int n = 0; n < programs.size(); n++) {
            Path dir = Files.createDirectories(ROOT.resolve("build/rt-flow/" + n));
            Files.writeString(dir.resolve("FLOW.cbl"), program(programs.get(n)));
            katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "FLOW.cbl", List.of("-free"));
            assertEquals(0, compiled.exit(), compiled.output());
            traces.add(cobc.execute(dir, "FLOW", Map.of()).output().lines().toList());
        }
        return traces;
    }

    static List<String> differ(List<List<List<Stmt>>> programs, List<List<String>> cobc, boolean ansi) {
        List<String> differ = new ArrayList<>();
        for (int n = 0; n < programs.size(); n++) {
            List<String> rt = rt(programs.get(n), ansi);
            if (!rt.equals(cobc.get(n))) differ.add(n + ": rt " + rt + "\n   cobc " + cobc.get(n));
        }
        return differ;
    }

    @Test
    void rtFlowsAsCobcDoes() throws Exception {
        List<List<List<Stmt>>> programs = programs();
        List<String> differ = differ(programs, cobc(programs), false);
        assertEquals(List.of(), differ.subList(0, Math.min(5, differ.size())), differ.size() + " of " + programs.size() + " differ");
    }

    /** R-056's mutation: only the innermost PERFORM's exit returning (ANSI's reading) differs from cobc -std=ibm. */
    @Test
    void theInnermostOnlyReadingDiffers() throws Exception {
        List<List<List<Stmt>>> programs = programs();
        int differ = differ(programs, cobc(programs), true).size();
        System.out.println("programs where the innermost-only reading differs: " + differ);
        assertTrue(differ > 3);
    }
}
