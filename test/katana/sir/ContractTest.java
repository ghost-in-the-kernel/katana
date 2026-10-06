package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.contract.Contract;
import katana.sir.json.IrJson;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 2.6's check on COPYREC and the document: the kinds table, the fact-for-fact diff, the same lines from the same IR (the goldens: IrGoldenTest). */
class ContractTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    private static Program copyrec() {
        Path trivial = ROOT.resolve("oracle/fixture/trivial");
        Parse.Result parse = Parse.file(trivial.resolve("cbl/COPYREC.cbl"), new Copybooks(List.of(trivial.resolve("cpy")), n -> 8), 8);
        return Build.program(parse, "sha256:a5298bfd77c97fd3f26b37103ba2e6ed51a164dbd90b0435ff2d88f27939e77b").program();
    }

    @Test
    void theDocumentNamesEveryKindAndNoOther() throws Exception {
        String doc = Files.readString(ROOT.resolve("docs/SEMANTIC-IR.md"));
        List<String> documented = new ArrayList<>();
        Matcher m = Pattern.compile("(?m)^\\| `([a-z]+)` \\|").matcher(doc);
        while (m.find()) documented.add(m.group(1));
        assertEquals(Contract.KINDS.stream().map(Contract.Kind::name).toList(), documented);
    }

    @Test
    void twoContractsCompareFactForFact() {
        List<String> expected = Contract.lines(copyrec());
        List<String> actual = new ArrayList<>(expected);
        String dropped = actual.remove(3);
        actual.add("item COPYREC 99 NOTHING");
        assertEquals(List.of("missing: " + dropped, "extra: item COPYREC 99 NOTHING"), Contract.diff(expected, actual));
        assertEquals(List.of(), Contract.diff(expected, expected));
    }

    @Test
    void theSameIrGivesTheSameLines() {
        Program p = copyrec();
        assertEquals(Contract.lines(p), Contract.lines(IrJson.read(IrJson.write(p), Program.class)));
        assertEquals(Contract.lines(p), Contract.lines(copyrec()));
    }
}
