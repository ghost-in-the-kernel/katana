package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.rules.Rules;
import katana.ledger.Ledger;
import katana.lower.Lower;
import katana.lower.ManifestTest;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 6.1's ledger (research §35, ROADMAP 6's check): for each program of the proven jobs, the literal build's
 * ledger with the structuring pass's lines appended covers the structured build (LED-04), a line per method
 * naming its `Function` fact `unconfirmed` (no person's word yet, R-029), and a removal per stretch gone.
 */
@Tag("integration")
class StructuringLedgerIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CARDDEMO = ROOT.resolve("corpus/carddemo/app");
    private static final String TIME = "2026-10-18T00:00:00Z", RUN = "katana.oracle.lane.StructuringCardDemoIntegrationTest";

    @Test
    void theLiteralLedgerWithThePassesLinesCoversTheStructuredBuild() throws Exception {
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        Copybooks books = new Copybooks(List.of(CARDDEMO.resolve("cpy")), n -> n.toUpperCase().startsWith("CUSTREC") ? 4 : 8);
        StringBuilder counts = new StringBuilder();
        for (String file : List.of("CBTRN02C.cbl", "CBTRN03C.cbl", "CBACT04C.cbl", "CBSTM03A.CBL", "CBSTM03B.CBL")) {
            Program p = Build.program(Parse.file(CARDDEMO.resolve("cbl/" + file), books, 8), ibm.hash()).program();
            Lower.Result literal = Lower.java(p, Lower.className(p));
            Passes.Result pass = Passes.plan(p, Rules.infer(p));
            Lower.Result structured = Lower.java(p, Lower.className(p), pass.plan());
            assertEquals(ManifestTest.declared(structured.java()), ManifestTest.members(structured.symbols()), file + ": the manifest is the structured Java's own declarations");
            List<Ledger.Draft> first = new ArrayList<>();
            for (Lower.Symbol s : literal.symbols()) first.add(new Ledger.Draft("literal", "-", "-", "-", s.name(), s.node(), ibm.hash(), TIME, 0));
            Ledger before = new Ledger(List.of()).append(first);
            List<Ledger.Draft> lines = StructuringLedger.lines(before, structured, pass, RUN, ibm.hash(), TIME);
            Ledger after = before.append(lines);
            Ledger.appendedTo(before, after);
            after.against(structured.symbols().stream().map(Lower.Symbol::name).toList());
            assertEquals(after, Ledger.read(after.text()));
            long methods = lines.stream().filter(d -> d.fact().startsWith("function ") && d.state().equals("unconfirmed")).count(), removed = lines.stream().filter(d -> d.symbol().equals("-")).count();
            assertEquals(pass.applied().size(), methods, file + ": a line per method, its fact unconfirmed");
            assertTrue(lines.stream().noneMatch(d -> d.state().equals("confirmed")), "no fact is confirmed without a person's word (R-029)");
            counts.append(file).append(' ').append(methods).append(" methods, ").append(removed).append(" stretches gone; ");
        }
        System.out.println("structuring ledger: " + counts);
    }
}
