package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.contract.Contract;
import org.junit.jupiter.api.Test;

/**
 * The IR's goldens (tithe pass 2, A5): each program's contract, every line, so a change to the IR lands
 * as a reviewable diff. COPYREC (oracle/fixture/trivial) and one NIST program of each module, in
 * sir/golden/ as EXEC85 extracted them (NIST is public domain, R-045), built under the ibm profile.
 * `-Dkatana.record=true` rewrites them.
 */
class IrGoldenTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("sir/golden");
    private static final String HASH = "sha256:a5298bfd77c97fd3f26b37103ba2e6ed51a164dbd90b0435ff2d88f27939e77b";
    static final List<String> NIST = List.of("DB204A", "IC237A", "IF142A", "IX111A", "NC127A", "OBIC3A", "RL101A", "RW102A", "SG202A", "SM106A", "SQ210M", "ST104A");

    /** A program's contract under the ibm profile. */
    static List<String> contract(Path program, Path copybooks) {
        Parse.Result parse = Parse.file(program, new Copybooks(List.of(copybooks), n -> 8), 8);
        if (!parse.clean()) throw new IllegalStateException(program + ": " + parse.errors().get(0));
        List<String> lines = new ArrayList<>();
        for (Build.Result r : Build.programs(parse, HASH)) lines.addAll(Contract.lines(r.program()));
        return lines;
    }

    @Test
    void everyGoldenProgramsContractIsItsGolden() throws Exception {
        List<String> differ = new ArrayList<>();
        Path trivial = ROOT.resolve("oracle/fixture/trivial");
        check("COPYREC", contract(trivial.resolve("cbl/COPYREC.cbl"), trivial.resolve("cpy")), differ);
        check("PROBE", contract(GOLDEN.resolve("PROBE.cbl"), GOLDEN), differ);
        for (String p : NIST) check(p, contract(GOLDEN.resolve(p + ".cbl"), GOLDEN), differ);
        assertEquals(List.of(), differ);
    }

    private static void check(String name, List<String> lines, List<String> differ) throws Exception {
        Path golden = GOLDEN.resolve(name + ".contract");
        String text = String.join("\n", lines) + "\n";
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, text);
        if (!Files.exists(golden) || !Files.readString(golden).equals(text)) differ.add(name + ": its contract is not sir/golden/" + name + ".contract");
    }
}
