package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.Item;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Tree to IR (docs/ROADMAP.md 2.5), mechanically: nothing is judged, and what the tree does not say
 * the IR does not either. What cannot be built is a finding, never a guess; so is what Enterprise
 * COBOL does not have (Report Writer, the COMMUNICATION SECTION), which the profile's compiler rejects.
 */
public final class Build {
    /** The data map of a program and what could not be built. */
    public record Data(List<Item> items, List<String> findings) {}

    private Build() {}

    /** The first program's data map: FILE, WORKING-STORAGE, LOCAL-STORAGE and LINKAGE, in that order of the source. */
    public static Data data(Parse.Result parse) {
        List<CobolParser.ProgramUnitContext> units = units(parse);
        return units.isEmpty() ? new Data(List.of(), List.of("no program")) : data(parse, units.get(0));
    }

    /** Every program's data map, each program before the programs nested in it, in textual order. */
    public static List<Data> all(Parse.Result parse) {
        List<Data> all = new ArrayList<>();
        for (CobolParser.ProgramUnitContext unit : units(parse)) all.add(data(parse, unit));
        return all;
    }

    private static Data data(Parse.Result parse, CobolParser.ProgramUnitContext program) {
        List<String> findings = new ArrayList<>();
        DataMap map = new DataMap(parse, findings);
        CobolParser.CurrencySignClauseContext currency = first(program, CobolParser.CurrencySignClauseContext.class);
        if (currency != null) {
            String symbol = currency.literal().size() > 1 ? currency.literal(1).getText() : currency.literal(0).getText();
            map.currency = symbol.replaceAll("[\"']", "").charAt(0);
        }
        map.decimalComma = first(program, CobolParser.DecimalPointClauseContext.class) != null;
        if (program != null && program.dataDivision() != null) {
            for (CobolParser.DataDivisionSectionContext s : program.dataDivision().dataDivisionSection()) {
                if (s.fileSection() != null) {
                    for (CobolParser.FileDescriptionEntryContext fd : s.fileSection().fileDescriptionEntry()) map.section(Item.Section.FILE, fd.dataDescriptionEntry());
                } else if (s.workingStorageSection() != null) {
                    map.section(Item.Section.WORKING_STORAGE, s.workingStorageSection().dataDescriptionEntry());
                } else if (s.localStorageSection() != null) {
                    map.section(Item.Section.LOCAL_STORAGE, s.localStorageSection().dataDescriptionEntry());
                } else if (s.linkageSection() != null) {
                    map.section(Item.Section.LINKAGE, s.linkageSection().dataDescriptionEntry());
                } else if (s.reportSection() != null) {
                    findings.add("REPORT SECTION at " + parse.origin(s) + ": Report Writer is not in Enterprise COBOL; its groups are not in the data map");
                } else if (s.communicationSection() != null) {
                    findings.add("COMMUNICATION SECTION at " + parse.origin(s) + ": not in Enterprise COBOL; its descriptions are not in the data map");
                }
            }
        }
        map.resolve();
        map.layout();
        return new Data(map.items(), findings);
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
