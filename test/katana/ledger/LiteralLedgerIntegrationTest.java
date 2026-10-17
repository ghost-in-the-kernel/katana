package katana.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.lower.Lower;
import katana.lower.ManifestTest;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 5.4's trial (research §31): the programs of the four proven jobs lowered, each manifest the Java's own
 * declarations, each build's literal ledger covering it and proven by its job's v1 report; then a pass
 * simulated on CBTRN02C that removes a field, refused until a line supersedes it.
 */
@Tag("integration")
class LiteralLedgerIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CARDDEMO = ROOT.resolve("corpus/carddemo/app");
    private static final String TIME = "2026-10-17T00:00:00Z";
    private static final Map<String, String> PROVEN_BY = new LinkedHashMap<>(Map.of("CBTRN02C.cbl", "POSTTRAN", "CBTRN03C.cbl", "TRANREPT-STUDY", "CBACT04C.cbl", "INTCALC",
            "CBSTM03A.CBL", "CREASTMT", "CBSTM03B.CBL", "CREASTMT"));

    /** A job's proof: its v1 report's path and the first 16 hex digits of its SHA-256. */
    private static String run(String job) throws Exception {
        Path report = ROOT.resolve("oracle/proof/" + job + ".v1.report.md");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(report));
        return "oracle/proof/" + job + ".v1.report.md sha256:" + HexFormat.of().formatHex(digest).substring(0, 16);
    }

    private static Lower.Result lowered(String file, Profile ibm) {
        Copybooks books = new Copybooks(List.of(CARDDEMO.resolve("cpy")), n -> n.toUpperCase().startsWith("CUSTREC") ? 4 : 8);
        Program p = Build.program(Parse.file(CARDDEMO.resolve("cbl/" + file), books, 8), ibm.hash()).program();
        return Lower.java(p, Lower.className(p));
    }

    private static List<Ledger.Draft> literal(Lower.Result r, String run, String profile) {
        List<Ledger.Draft> lines = new ArrayList<>();
        for (Lower.Symbol s : r.symbols()) lines.add(new Ledger.Draft("literal", "-", "-", run, s.name(), s.node(), profile, TIME, 0));
        return lines;
    }

    @Test
    void theProvenJobsLiteralLedgersCoverTheirBuildsAndARemovalIsSuperseded() throws Exception {
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        StringBuilder counts = new StringBuilder();
        Ledger cbtrn02c = null;
        List<String> cbtrn02cSymbols = null;
        for (Map.Entry<String, String> program : PROVEN_BY.entrySet()) {
            Lower.Result r = lowered(program.getKey(), ibm);
            assertEquals(ManifestTest.declared(r.java()), ManifestTest.members(r.symbols()), program.getKey());
            Ledger ledger = new Ledger(List.of()).append(literal(r, run(program.getValue()), ibm.hash()));
            List<String> symbols = r.symbols().stream().map(Lower.Symbol::name).toList();
            ledger.against(symbols);
            assertEquals(ledger, Ledger.read(ledger.text()));
            String map = ProvenanceMap.write(ledger);
            assertEquals(symbols.size(), map.lines().filter(l -> !l.startsWith("#")).count(), program.getKey());
            counts.append(program.getKey()).append(' ').append(symbols.size()).append("; ");
            if (program.getKey().equals("CBTRN02C.cbl")) {
                cbtrn02c = ledger;
                cbtrn02cSymbols = symbols;
            }
        }
        System.out.println("symbols: " + counts);
        Ledger literal = cbtrn02c;
        List<String> after = new ArrayList<>(cbtrn02cSymbols);
        Entry field = literal.entries().stream().filter(e -> e.symbol().equals("CBTRN02C.f2")).findFirst().orElseThrow();
        after.remove("CBTRN02C.f2");
        assertThrows(Invalid.class, () -> literal.against(after));
        Ledger pass = literal.append(List.of(new Ledger.Draft("trial-removal v1", "-", "-", "-", "-", field.node(), field.profile(), TIME, field.seq())));
        pass.against(after);
        Ledger.appendedTo(literal, pass);
        assertTrue(LedgerReport.write(pass).contains("| trial-removal v1 | 1 |"), LedgerReport.write(pass));
    }
}
