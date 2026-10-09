package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The lowering verb by verb (phase 3.3): small programs written for it (test/katana/lower/programs/), each
 * run by cobc -std=ibm and through the Java lane (`JavaRunner`), a `NAME.in` beside one its standard input
 * in both, their output equal line for line. A
 * program's lowering findings are in the failure, with the IR of what was not lowered. Its mutation: the
 * outputs of two programs swapped differ.
 */
@Tag("integration")
class LowerDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path PROGRAMS = ROOT.resolve("test/katana/lower/programs");

    record Ran(String name, String cobc, String java) {}

    static List<Ran> run() throws Exception {
        List<Path> sources;
        try (Stream<Path> list = Files.list(PROGRAMS)) {
            sources = list.filter(p -> p.toString().endsWith(".cbl")).sorted().toList();
        }
        List<Ran> out = new ArrayList<>();
        Cobc cobc = new Cobc(Duration.ofSeconds(60), "ibm");
        JavaRunner lane = new JavaRunner(60);
        for (Path source : sources) {
            String name = source.getFileName().toString().replace(".cbl", "");
            Path dir = Files.createDirectories(ROOT.resolve("build/lower-differential/" + name));
            Files.copy(source, dir.resolve(name + ".cbl"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Path in = PROGRAMS.resolve(name + ".in");
            byte[] input = Files.exists(in) ? Files.readAllBytes(in) : new byte[0];
            katana.oracle.nist.Runner.Result c = cobc.compile(dir, name + ".cbl", List.of());
            String cobcOut = c.ok() ? cobc.execute(dir, name, Map.of(), input).output() : "cobc: " + c.output();
            katana.oracle.nist.Runner.Result j = lane.program(dir, name + ".cbl");
            String javaOut = j.ok() ? lane.run(dir, name, Map.of(), input).output() : "lower or javac: " + j.output();
            String findings = Files.exists(dir.resolve(name + ".class-name")) ? Files.readString(dir.resolve(name + ".class-name"), StandardCharsets.UTF_8) : "";
            out.add(new Ran(name, cobcOut, javaOut + (findings.lines().count() > 1 ? "\nfindings:\n" + findings : "")));
        }
        return out;
    }

    /**
     * Programs where cobc's reading is not IBM's, each held instead to its output worked by hand from
     * IBM's text, and cobc still differing from it (so a change shows). SEARCHVARY: a VARYING item that
     * is not the table's own index "is increased by the same amount as the search index-name and at the
     * same time", an integer item "simultaneously increased by 1" (LR 6.4 p. 437): OX from 1 and N from 0,
     * each stepped twice; cobc sets each to the search index's occurrence number. A finding against
     * cobc, D-014 (J-122). ACCEPTEOF: with nothing on the input device, "padding will not take place and the
     * contents of the receiving area are unchanged" (LR 6.4 p. 308), "the system input device" being
     * assumed when FROM is omitted (p. 307); cobc fills the item with spaces, D-015 (J-122). POWERIBM: 10 ** 2
     * + 12 * 2 is 124, 13 ** 2 is 169 and 3 ** -1 is 0.333333 to six places, each exact and within its
     * receiver under any of IBM's place rules (PG 6.4 pp. 796-797); cobc -std=ibm prints 100, 0 and
     * 27 (the last 27 under its default dialect too), D-016 (J-124). ALPHAIBM: under a PROGRAM COLLATING
     * SEQUENCE, LOW-VALUE is "the character that has the lowest ordinal position in the collating
     * sequence used" (LR 6.4 p. 16), and the sequence "determines the truth value of" alphanumeric
     * comparisons "explicitly specified in relation conditions" (p. 123); cobc gives a VALUE of LOW-VALUE
     * X'00', D-017, and compares two literals in native order, D-018 (J-131). ADDIBM: NIST NC177A's
     * ADD-TEST-F2-11 to -13, seven addends in three orders into S999V9(10), every intermediate within
     * Appendix A's 30 digits, so each sum is exact, 242.4332220110 (PG 6.4 p. 795: "(i1 or i2) + 1, whichever is greater" integer and "d1
     * or d2, whichever is greater" decimal places); cobc -std=ibm gives 242.4332220010, its default dialect the exact sum, D-019
     * (J-132). ACCEPTREC: "The system input device is read until the receiving area is filled or EOF is
     * encountered", "Each input record is concatenated with the previous input record", the final one
     * "truncated as required" (LR 6.4 pp. 307-308); cobc reads one record into a 100-character item and
     * the next ACCEPT gets the rest of it, D-020 (J-136). NOTRSVD: NOTHING is not in IBM's reserved words
     * (LR 6.4 Appendix E, Table 83, p. 770: NO, NOT, NULL, NULLS); cobc -std=ibm refuses it as a data name,
     * D-021 (J-138). MULTRES: a statement with several receivers "performs all arithmetic operations to find
     * the result", "stores that result in a temporary location", then combines it "with each single
     * receiving field" left to right, a receiver's subscript taken after the receivers before it (LR 6.4
     * pp. 297-298): ADD A TO A B with A 2 and B 3 leaves B 5; cobc adds A's new value, B 7, D-022 (J-141).
     * RNDTRIAL: R-020 C-2's trial, twelve COMPUTE ... ROUNDED with every intermediate cut to dmax and
     * ROUNDED's extra place on the last operation only (`ArithProfile.last`; IBM's "might be carried",
     * PG 6.4 p. 794, is the open text), where cobc agrees; then a literal operand, which counts in dmax
     * like any other (p. 795), and with which cobc keeps a second place in the division before it: A / T
     * + 1 is 2.7, A / T + ONE 2.6, D-023 (J-142). RELDMAX: "In an explicit relation condition where at least
     * one of the comparands is an arithmetic expression, dmax is the maximum number of decimal places for
     * any operand of either comparand" (PG 6.4 p. 801), so D + 0 EQUAL TO 1 (D 1) is true whatever else the
     * condition holds; cobc -std=ibm finds it false when an item with decimal places is elsewhere in the
     * same condition (B EQUAL TO C OR D + 0 EQUAL TO 1), NIST NC205A's CON-TEST-GF-3, D-024 (J-143).
     * STATRIAL: FILE STATUS and EXCEPTION/ERROR declaratives through fifteen statements (research §12):
     * an OPEN INPUT that fails runs the INPUT declarative, the file being "in the process of being opened
     * in INPUT mode" (LR 6.4 p. 706), where cobc runs none, D-025; a CLOSE of a file whose OPEN failed is
     * 42, "a CLOSE statement was attempted for a file not in the open mode" (p. 302), where cobc gives 00
     * after a CLOSE WITH LOCK, D-026 (J-152).
     */
    static final Map<String, String> IBM_READINGS = Map.ofEntries(
            Map.entry("SEARCHVARY", "DD W\nN 002\n"),
            Map.entry("ACCEPTEOF", "X KEPT \nX KEPT \n"),
            Map.entry("POWERIBM", "SUM 0000001240000\nEXPR 0000001690000\nNEG      0.333333\n"),
            Map.entry("ALPHAIBM", "VALUE LOW IS F\nU < N\n"),
            Map.entry("ADDIBM", "SUM 242.4332220110\nSUM 242.4332220110\nSUM 242.4332220110\nEQUAL\nSUB -344777.777443\nSUB P  100\n"),
            Map.entry("ACCEPTREC", "FIRST 10 [ABCDEFGHIJ]\nLAST 20 [*001*002*003*004*005]\nNEXT [NEXT RECORD]\n"),
            Map.entry("NOTRSVD", "NOTHING ZILC\n"),
            Map.entry("MULTRES", "1 040 050\n2 040 060\n3 010 020\n4 2 010\n"),
            Map.entry("RNDTRIAL", "01  1.2\n02  1.5\n03 10.2\n04 10.5\n05  0.6\n06  0.5\n07 10.2\n08 10.0\n09  0.2\n10  0.5\n11 10.6\n12 10.0\n13  2.6\n14  2.6\n15 10.5\n"),
            Map.entry("RELDMAX", "N F\nO T\nP T\nQ T\nR T\nS T\nT T\nU T\nV T\n"),
            Map.entry("STATRIAL", " 1 05\n   AE\n 2 10\n   DI 46      \n 3 46\n 4 00\n   D2 35\n 5 35\n   D2 47\n 6 47\n   D2 42\n 7 42\n 8 00\n 9 41\n10 00\n11 47\n12 00\n   DI 00 42 38\n13 38\n14 42\n15 48\n"));

    @Test
    void eachProgramPrintsWhatCobcPrints() throws Exception {
        List<String> differ = new ArrayList<>();
        for (Ran r : run()) {
            String ibm = IBM_READINGS.get(r.name());
            if (ibm != null && (!ibm.equals(r.java()) || ibm.equals(r.cobc()))) differ.add(r.name() + " (IBM's reading)\njava:\n" + r.java());
            else if (ibm == null && !r.cobc().equals(r.java())) differ.add(r.name() + "\ncobc:\n" + r.cobc() + "\njava:\n" + r.java());
        }
        assertEquals(List.of(), differ);
    }

    /** R-056's mutation: each program's output held to the next program's cobc output differs. */
    @Test
    void anotherProgramsOutputDiffers() throws Exception {
        List<Ran> ran = run();
        for (int k = 0; k < ran.size(); k++) org.junit.jupiter.api.Assertions.assertNotEquals(ran.get((k + 1) % ran.size()).cobc(), ran.get(k).java(), ran.get(k).name());
    }
}
