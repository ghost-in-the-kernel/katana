package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Header;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Value;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Tree to IR (docs/ROADMAP.md 2.5), mechanically: nothing is judged, and what the tree does not say
 * the IR does not either. What cannot be built is a finding, never a guess; so is what Enterprise
 * COBOL does not have (Report Writer, the COMMUNICATION SECTION), which the profile's compiler rejects.
 */
public final class Build {
    /** The data map of a program, what could not be built, and the CURRENCY SIGN and DECIMAL-POINT its pictures were read under. */
    public record Data(List<Item> items, List<String> findings, char currency, boolean decimalComma) {}

    /** A program's IR, with the programs nested in it, and everything that could not be built in any of them. */
    public record Result(Program program, List<String> findings) {}

    /**
     * Every top-level program of a source, in order, each with those nested in it (a source may hold
     * several, separately compiled: NIST IC237A and IC237A-1), under a profile (SIR-01).
     */
    public static List<Result> programs(Parse.Result parse, String profileHash) {
        List<CobolParser.ProgramUnitContext> top = parse.tree() instanceof CobolParser.StartRuleContext start ? start.compilationUnit().programUnit() : List.of();
        List<Result> results = new ArrayList<>();
        for (var unit : top) {
            List<String> findings = new ArrayList<>();
            results.add(new Result(program(parse, unit, profileHash, findings, List.of(), Set.of()), findings));
        }
        return results;
    }

    /** The first program of a parse, under a profile (its hash goes in the header, SIR-01). */
    public static Result program(Parse.Result parse, String profileHash) {
        List<CobolParser.ProgramUnitContext> top = parse.tree() instanceof CobolParser.StartRuleContext start ? start.compilationUnit().programUnit() : List.of();
        if (top.isEmpty()) throw new IllegalArgumentException("no program in " + parse.text().origin(0).file());
        List<String> findings = new ArrayList<>();
        return new Result(program(parse, top.get(0), profileHash, findings, List.of(), Set.of()), findings);
    }

    /** One program unit's IR; `outer` and `outerFiles` are the names of the programs it is nested in. */
    private static Program program(Parse.Result parse, CobolParser.ProgramUnitContext unit, String profileHash, List<String> findings, List<Names> outer, Set<String> outerFiles) {
        Map<String, List<Integer>> records = new HashMap<>();
        Data data = data(parse, unit, records);
        findings.addAll(data.findings());
        Map<Integer, Integer> lengths = new HashMap<>();
        data.items().forEach(i -> lengths.put(i.id(), i.length()));
        Names names = new Names(data.items(), records, findings);
        Values values = new Values(parse, names, findings);
        List<FileDescriptor> files = Files.build(parse, unit, names, records, lengths);
        values.outer.addAll(outer);
        values.outerFiles.addAll(outerFiles);
        files.forEach(f -> values.files.add(f.name()));
        if (unit.environmentDivision() != null) {
            all(unit.environmentDivision(), CobolParser.MnemonicNameContext.class).forEach(m -> values.mnemonics.add(Values.upper(m.getText())));
            for (var status : all(unit.environmentDivision(), CobolParser.EnvironmentSwitchNameSpecialNamesStatusPhraseContext.class)) {
                status.condition().forEach(c -> values.mnemonics.add(Values.upper(c.getText())));
            }
        }
        values.cics = first(unit.procedureDivision(), CobolParser.ExecCicsStatementContext.class) != null;
        Paragraphs paragraphs = new Paragraphs(parse, values, findings);
        Procedure procedure = unit.procedureDivision() == null
                ? new Procedure(List.of(), 0, List.of(), List.of(new Procedure.Paragraph(0, "", "", List.of(), List.of(), Origins.at(parse, unit))), List.of())
                : paragraphs.build(unit.procedureDivision(), using(parse, unit.procedureDivision(), names), returning(parse, unit.procedureDivision(), names));
        List<Program> nested = new ArrayList<>();
        List<Names> inside = new ArrayList<>(outer);
        inside.add(names);
        Set<String> insideFiles = new HashSet<>(outerFiles);
        files.forEach(f -> insideFiles.add(f.name()));
        for (var inner : unit.programUnit()) nested.add(program(parse, inner, profileHash, findings, inside, insideFiles));
        var id = unit.identificationDivision().programIdParagraph();
        Header header = new Header(Values.upper(id.programName().getText()).replaceAll("[\"']", ""), Origins.at(parse, unit).file(), profileHash,
                String.valueOf(data.currency()), data.decimalComma(), debuggingMode(unit), switches(unit), mnemonics(unit), classes(unit), alphabets(unit), collating(unit), Origins.at(parse, id));
        return new Program(header, data.items(), files, procedure, paragraphs.calls, nested);
    }

    /** SOURCE-COMPUTER's WITH DEBUGGING MODE: the compile-time switch for debugging sections and lines (LR 6.4 p. 760). */
    private static boolean debuggingMode(ParseTree unit) {
        CobolParser.SourceComputerParagraphContext s = first(unit, CobolParser.SourceComputerParagraphContext.class);
        return s != null && s.DEBUGGING() != null;
    }

    /** SPECIAL-NAMES' switch-status condition-names, each with its switch and the status it stands for. */
    private static List<Header.Switch> switches(CobolParser.ProgramUnitContext unit) {
        List<Header.Switch> out = new ArrayList<>();
        if (unit.environmentDivision() == null) return out;
        for (var clause : all(unit.environmentDivision(), CobolParser.EnvironmentSwitchNameClauseContext.class)) {
            if (clause.environmentName() == null || clause.environmentSwitchNameSpecialNamesStatusPhrase() == null) continue;
            String environment = Values.upper(clause.environmentName().getText());
            boolean on = true;
            for (ParseTree child : clause.environmentSwitchNameSpecialNamesStatusPhrase().children) {
                if (child instanceof TerminalNode t && (t.getSymbol().getType() == CobolParser.ON || t.getSymbol().getType() == CobolParser.OFF)) on = t.getSymbol().getType() == CobolParser.ON;
                if (child instanceof CobolParser.ConditionContext c) out.add(new Header.Switch(Values.upper(c.getText()), environment, on));
            }
        }
        return out;
    }

    /** SPECIAL-NAMES' mnemonic-names, each with its environment-name: a switch (`UPSI-0`), a channel (`C01`), space suppression (`CSP`) (J-172). */
    private static List<Header.Mnemonic> mnemonics(CobolParser.ProgramUnitContext unit) {
        List<Header.Mnemonic> out = new ArrayList<>();
        if (unit.environmentDivision() == null) return out;
        for (var clause : all(unit.environmentDivision(), CobolParser.EnvironmentSwitchNameClauseContext.class)) {
            if (clause.environmentName() == null || clause.mnemonicName() == null) continue;
            String environment = Values.upper(clause.environmentName().getText());
            out.add(new Header.Mnemonic(Values.upper(clause.mnemonicName().getText()), environment));
        }
        return out;
    }

    /** SPECIAL-NAMES' alphabet-names: their kind, or for literals their members (a literal, THRU, ALSO). */
    private static List<Header.Alphabet> alphabets(CobolParser.ProgramUnitContext unit) {
        List<Header.Alphabet> out = new ArrayList<>();
        if (unit.environmentDivision() == null) return out;
        for (var a : all(unit.environmentDivision(), CobolParser.AlphabetClauseFormat1Context.class)) {
            String name = Values.upper(a.alphabetName().getText());
            if (a.alphabetLiterals().isEmpty()) {
                String kind = a.cobolWord() != null ? "WORD " + Values.upper(a.cobolWord().getText()) : Values.upper(a.getChild(a.getChildCount() - 1).getText());
                out.add(new Header.Alphabet(name, kind, List.of()));
                continue;
            }
            List<Value> members = new ArrayList<>();
            for (var l : a.alphabetLiterals()) {
                Value first = bound(l.literal());
                if (l.alphabetThrough() != null) members.add(new Value.Expression("THRU", List.of(first, bound(l.alphabetThrough().literal()))));
                else if (!l.alphabetAlso().isEmpty()) {
                    List<Value> also = new ArrayList<>(List.of(first));
                    for (var x : l.alphabetAlso()) for (var lit : x.literal()) also.add(bound(lit));
                    members.add(new Value.Expression("ALSO", also));
                } else members.add(first);
            }
            out.add(new Header.Alphabet(name, "LITERAL", members));
        }
        return out;
    }

    /** OBJECT-COMPUTER's PROGRAM COLLATING SEQUENCE: the alphabet-name for alphanumeric data, "" when none. */
    private static String collating(CobolParser.ProgramUnitContext unit) {
        if (unit.environmentDivision() == null) return "";
        for (var c : all(unit.environmentDivision(), CobolParser.CollatingSequenceClauseContext.class)) {
            if (c.collatingSequenceClauseAlphanumeric() != null) return Values.upper(c.collatingSequenceClauseAlphanumeric().alphabetName().getText());
            if (!c.alphabetName().isEmpty()) return Values.upper(c.alphabetName(0).getText());
        }
        return "";
    }

    /** SPECIAL-NAMES' CLASS names, each with its literals and THRU ranges. */
    private static List<Header.CharClass> classes(CobolParser.ProgramUnitContext unit) {
        List<Header.CharClass> out = new ArrayList<>();
        if (unit.environmentDivision() == null) return out;
        for (var clause : all(unit.environmentDivision(), CobolParser.ClassClauseContext.class)) {
            List<Value> members = new ArrayList<>();
            for (var t : clause.classClauseThrough()) {
                Value from = bound(t.classClauseFrom().literal());
                members.add(t.classClauseTo() == null ? from : new Value.Expression("THRU", List.of(from, bound(t.classClauseTo().literal()))));
            }
            out.add(new Header.CharClass(Values.upper(clause.className().getText()), members));
        }
        return out;
    }

    /** A CLASS literal; an identifier there (the grammar allows one, IBM does not) is a Name. */
    private static Value bound(CobolParser.LiteralContext l) {
        return l == null ? new Value.Name("unresolved", "CLASS bound") : Values.literal(l);
    }

    private static List<Integer> using(Parse.Result parse, CobolParser.ProcedureDivisionContext pd, Names names) {
        List<Integer> using = new ArrayList<>();
        if (pd.procedureDivisionUsingClause() == null) return using;
        for (var p : all(pd.procedureDivisionUsingClause(), CobolParser.DataNameContext.class)) {
            int item = names.resolve(Values.upper(p.getText()), List.of(), null, Origins.at(parse, p).toString(), "PROCEDURE DIVISION USING");
            if (item != 0) using.add(item);
        }
        return using;
    }

    private static int returning(Parse.Result parse, CobolParser.ProcedureDivisionContext pd, Names names) {
        var giving = pd.procedureDivisionGivingClause();
        if (giving == null) return 0;
        return names.resolve(Values.upper(giving.dataName().getText()), List.of(), null, Origins.at(parse, giving).toString(), "PROCEDURE DIVISION RETURNING");
    }

    /** Every node of a kind under a node, in order. */
    static <T> List<T> all(ParseTree node, Class<T> kind) {
        List<T> found = new ArrayList<>();
        if (kind.isInstance(node)) found.add(kind.cast(node));
        for (int i = 0; i < node.getChildCount(); i++) found.addAll(all(node.getChild(i), kind));
        return found;
    }

    private Build() {}

    /** The first program's data map: FILE, WORKING-STORAGE, LOCAL-STORAGE and LINKAGE, in that order of the source. */
    public static Data data(Parse.Result parse) {
        List<CobolParser.ProgramUnitContext> units = units(parse);
        return units.isEmpty() ? new Data(List.of(), List.of("no program"), '$', false) : data(parse, units.get(0));
    }

    /** Every program's data map, each program before the programs nested in it, in textual order. */
    public static List<Data> all(Parse.Result parse) {
        List<Data> all = new ArrayList<>();
        for (CobolParser.ProgramUnitContext unit : units(parse)) all.add(data(parse, unit));
        return all;
    }

    private static Data data(Parse.Result parse, CobolParser.ProgramUnitContext program) {
        return data(parse, program, new HashMap<>());
    }

    /** A program unit's data map; each FD's 01s go in `records` under the file's name. */
    private static Data data(Parse.Result parse, CobolParser.ProgramUnitContext program, Map<String, List<Integer>> records) {
        List<String> findings = new ArrayList<>();
        DataMap map = new DataMap(parse, findings);
        map.currency = currency(program);
        map.decimalComma = first(program, CobolParser.DecimalPointClauseContext.class) != null;
        if (program != null && program.dataDivision() != null) {
            for (CobolParser.DataDivisionSectionContext s : program.dataDivision().dataDivisionSection()) {
                if (s.fileSection() != null) {
                    for (CobolParser.FileDescriptionEntryContext fd : s.fileSection().fileDescriptionEntry()) {
                        int before = map.nodes.size();
                        map.section(Item.Section.FILE, fd.dataDescriptionEntry());
                        List<Integer> mine = new ArrayList<>();
                        for (int i = before; i < map.nodes.size(); i++) if (map.nodes.get(i).parent == 0 && map.nodes.get(i).level != 77) mine.add(map.nodes.get(i).id);
                        records.put(Values.upper(fd.fileName().getText()), mine);
                    }
                } else if (s.workingStorageSection() != null) {
                    map.section(Item.Section.WORKING_STORAGE, s.workingStorageSection().dataDescriptionEntry());
                } else if (s.localStorageSection() != null) {
                    map.section(Item.Section.LOCAL_STORAGE, s.localStorageSection().dataDescriptionEntry());
                } else if (s.linkageSection() != null) {
                    map.section(Item.Section.LINKAGE, s.linkageSection().dataDescriptionEntry());
                } else if (s.reportSection() != null) {
                    findings.add("REPORT SECTION at " + Origins.at(parse, s) + ": Report Writer is not in Enterprise COBOL; its groups are not in the data map");
                } else if (s.communicationSection() != null) {
                    findings.add("COMMUNICATION SECTION at " + Origins.at(parse, s) + ": not in Enterprise COBOL; its descriptions are not in the data map");
                }
            }
        }
        Placement placement = new Placement(parse, map.nodes, findings);
        placement.resolve();
        placement.layout();
        return new Data(new Tuples(parse, map.nodes, placement).items(), findings, map.currency, map.decimalComma);
    }

    /** A unit's CURRENCY SIGN (its PICTURE symbol, when it names two), `$` when none. */
    private static char currency(ParseTree program) {
        CobolParser.CurrencySignClauseContext currency = first(program, CobolParser.CurrencySignClauseContext.class);
        if (currency == null) return '$';
        String symbol = currency.literal().size() > 1 ? currency.literal(1).getText() : currency.literal(0).getText();
        return symbol.replaceAll("[\"']", "").charAt(0);
    }

    /** The first node of a kind under a node, depth first, or null. */
    static <T> T first(ParseTree node, Class<T> kind) {
        if (node == null) return null;
        if (kind.isInstance(node)) return kind.cast(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            T found = first(node.getChild(i), kind);
            if (found != null) return found;
        }
        return null;
    }

    /** The program units, each before those nested in it. */
    private static List<CobolParser.ProgramUnitContext> units(Parse.Result parse) {
        List<CobolParser.ProgramUnitContext> units = new ArrayList<>();
        if (parse.tree() instanceof CobolParser.StartRuleContext start) start.compilationUnit().programUnit().forEach(u -> nested(u, units));
        return units;
    }

    private static void nested(CobolParser.ProgramUnitContext unit, List<CobolParser.ProgramUnitContext> units) {
        units.add(unit);
        unit.programUnit().forEach(u -> nested(u, units));
    }
}
