package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.Attributes.Usage;
import katana.sir.types.Item;
import katana.sir.types.Value;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

/**
 * The data map (docs/ROADMAP.md 2.5): each section's entries, in textual order, to items with their
 * parent and record, length and offset, attribute tuple, REDEFINES, OCCURS, SYNC slack, VALUE and 88s.
 * This class reads the entries and their clauses into nodes; `Placement` resolves their names and lays
 * them out, `Tuples` makes them items. An INDEXED BY name is an item of its own, level 77, USAGE INDEX.
 */
final class DataMap {
    /** One entry while the map is built; a group's length is known only after its children's. */
    static final class Node {
        int id, level, parent, record, redefines, length, offset, syncSlack, occursMin, occursMax = 0, dependingOn;
        String name, picture = "", dependingName, redefinesName;
        Value value;
        char currency = '$';
        boolean decimalComma, blankWhenZero;
        Node renamesOne;
        Item.Section section;
        Usage usage = Usage.DISPLAY;
        boolean usageStated, signSeparate, leading, sync, signClause;
        Pictures pic;
        final List<Node> children = new ArrayList<>();
        final List<Item.Condition> conditions = new ArrayList<>();
        final List<String> indexNames = new ArrayList<>();
        final List<String[]> keyNames = new ArrayList<>();
        final List<Item.Flag> flags = new ArrayList<>();
        ParserRuleContext ctx;
    }

    private final Parse.Result parse;
    char currency = '$';
    boolean decimalComma;
    final List<Node> nodes = new ArrayList<>();
    private Node lastEntry;
    final List<String> findings;

    DataMap(Parse.Result parse, List<String> findings) {
        this.parse = parse;
        this.findings = findings;
    }

    /** Every entry of one section, in order: records, their children, their 88s and 66s. */
    void section(Item.Section section, List<CobolParser.DataDescriptionEntryContext> entries) {
        List<Node> open = new ArrayList<>();
        for (CobolParser.DataDescriptionEntryContext entry : entries) {
            if (entry.dataDescriptionEntryFormat1() != null) format1(section, entry.dataDescriptionEntryFormat1(), open);
            else if (entry.dataDescriptionEntryFormat3() != null && lastEntry != null) condition(entry.dataDescriptionEntryFormat3());
            else if (entry.dataDescriptionEntryFormat2() != null) renames(section, entry.dataDescriptionEntryFormat2(), open);
        }
    }

    /** A level 01-49 or 77 entry: its place under the open groups, its clauses, its INDEXED BY items. */
    private void format1(Item.Section section, CobolParser.DataDescriptionEntryFormat1Context e, List<Node> open) {
        Node n = new Node();
        n.level = e.LEVEL_NUMBER_77() != null ? 77 : Integer.parseInt(e.INTEGERLITERAL().getText());
        n.name = e.dataName() != null ? e.dataName().getText().toUpperCase(Locale.ROOT) : "FILLER";
        n.section = section;
        n.currency = currency;
        n.decimalComma = decimalComma;
        n.ctx = e;
        while (!open.isEmpty() && (open.get(open.size() - 1).level >= n.level || n.level == 77 || n.level == 1)) open.remove(open.size() - 1);
        Node parent = open.isEmpty() ? null : open.get(open.size() - 1);
        clauses(n, e, parent);
        add(n, parent);
        open.add(n);
        lastEntry = n;
        for (String index : n.indexNames) index(section, index, e);
    }

    private void add(Node n, Node parent) {
        n.id = nodes.size() + 1;
        n.parent = parent == null ? 0 : parent.id;
        n.record = parent == null ? n.id : parent.record;
        if (parent != null) parent.children.add(n);
        nodes.add(n);
    }

    /** An INDEXED BY name: an item of its own, level 77, USAGE INDEX. */
    private void index(Item.Section section, String name, ParserRuleContext ctx) {
        Node n = new Node();
        n.level = 77;
        n.name = name;
        n.section = section;
        n.usage = Usage.INDEX;
        n.usageStated = true;
        n.ctx = ctx;
        add(n, null);
    }

    /** The entry's clauses, usage and sign inherited from the group when the entry states none. */
    private void clauses(Node n, CobolParser.DataDescriptionEntryFormat1Context e, Node parent) {
        if (!e.dataPictureClause().isEmpty()) n.picture = e.dataPictureClause(0).pictureString().getText();
        if (!e.dataUsageClause().isEmpty()) {
            n.usage = usage(e.dataUsageClause(0).getText());
            n.usageStated = true;
        } else if (parent != null) {
            n.usage = parent.usage;
        }
        if (!e.dataSignClause().isEmpty()) sign(n, e.dataSignClause(0));
        else if (parent != null && parent.signClause) {
            n.signClause = true;
            n.leading = parent.leading;
            n.signSeparate = parent.signSeparate;
        }
        n.sync = !e.dataSynchronizedClause().isEmpty();
        n.blankWhenZero = !e.dataBlankWhenZeroClause().isEmpty();
        if (!e.dataJustifiedClause().isEmpty()) n.flags.add(Item.Flag.JUSTIFIED);
        if (n.blankWhenZero) n.flags.add(Item.Flag.BLANK_WHEN_ZERO);
        if (!e.dataExternalClause().isEmpty()) n.flags.add(Item.Flag.EXTERNAL);
        if (!e.dataGlobalClause().isEmpty()) n.flags.add(Item.Flag.GLOBAL);
        if (!e.dataRedefinesClause().isEmpty()) n.redefinesName = e.dataRedefinesClause(0).dataName().getText().toUpperCase(Locale.ROOT);
        if (!e.dataValueClause().isEmpty()) n.value = value(e.dataValueClause(0));
        if (!e.dataOccursClause().isEmpty()) occurs(n, e.dataOccursClause(0));
    }

    private static void sign(Node n, CobolParser.DataSignClauseContext s) {
        n.signClause = true;
        n.leading = s.LEADING() != null;
        n.signSeparate = s.SEPARATE() != null;
    }

    /** OCCURS n, n TO m, DEPENDING ON, ASCENDING/DESCENDING KEY and INDEXED BY, as names resolved later. */
    private void occurs(Node n, CobolParser.DataOccursClauseContext o) {
        int first = o.integerLiteral() != null ? Integer.parseInt(o.integerLiteral().getText()) : 1;
        if (o.dataOccursTo() != null) {
            n.occursMin = first;
            n.occursMax = Integer.parseInt(o.dataOccursTo().integerLiteral().getText());
        } else {
            n.occursMin = first;
            n.occursMax = first;
        }
        if (o.dataOccursDepending() != null) n.dependingName = first(o.dataOccursDepending().qualifiedDataName());
        for (var sort : o.dataOccursSort()) for (var key : sort.qualifiedDataName()) n.keyNames.add(new String[] {first(key), sort.ASCENDING() != null ? "A" : "D"});
        for (var indexed : o.dataOccursIndexed()) for (var name : indexed.indexName()) n.indexNames.add(name.getText().toUpperCase(Locale.ROOT));
    }

    private void condition(CobolParser.DataDescriptionEntryFormat3Context c) {
        List<Value> values = new ArrayList<>();
        for (var interval : c.dataValueClause().dataValueInterval()) values.add(interval(interval));
        lastEntry.conditions.add(new Item.Condition(c.conditionName().getText().toUpperCase(Locale.ROOT), values, Origins.at(parse, c)));
    }

    /** An item's VALUE: its one value, or a `VALUES` expression of several. */
    private static Value value(CobolParser.DataValueClauseContext v) {
        List<Value> values = v.dataValueInterval().stream().map(DataMap::interval).toList();
        return values.size() == 1 ? values.get(0) : new Value.Expression("VALUES", values);
    }

    /** One value of a VALUE clause: a literal (a word, as a Name, should one stand there), a range as `THRU`. */
    private static Value interval(CobolParser.DataValueIntervalContext i) {
        var from = i.dataValueIntervalFrom();
        Value low = from.literal() != null ? Values.literal(from.literal()) : new Value.Name("word", from.getText().toUpperCase(Locale.ROOT));
        return i.dataValueIntervalTo() == null ? low : new Value.Expression("THRU", List.of(low, Values.literal(i.dataValueIntervalTo().literal())));
    }

    /** A 66: the record's bytes from its first item through its last, as an item of the record. */
    private void renames(Item.Section section, CobolParser.DataDescriptionEntryFormat2Context r, List<Node> open) {
        if (open.isEmpty()) return;
        Node n = new Node();
        n.level = 66;
        n.name = r.dataName().getText().toUpperCase(Locale.ROOT);
        n.section = section;
        n.ctx = r;
        n.redefinesName = first(r.dataRenamesClause().qualifiedDataName(0));
        n.dependingName = r.dataRenamesClause().qualifiedDataName().size() > 1 ? first(r.dataRenamesClause().qualifiedDataName(1)) : n.redefinesName;
        Node record = nodes.get(open.get(0).record - 1);
        n.id = nodes.size() + 1;
        n.parent = record.id;
        n.record = record.id;
        nodes.add(n);
    }

    /** A USAGE clause's words to the usage; DISPLAY when none of these. */
    static Usage usage(String clause) {
        String u = clause.toUpperCase(Locale.ROOT).replaceFirst("^USAGE(IS)?", "");
        return switch (u) {
            case "COMP", "COMPUTATIONAL", "COMP-4", "COMPUTATIONAL-4", "BINARY" -> Usage.BINARY;
            case "COMP-5", "COMPUTATIONAL-5" -> Usage.NATIVE_BINARY;
            case "COMP-3", "COMPUTATIONAL-3", "PACKED-DECIMAL" -> Usage.PACKED;
            case "COMP-1", "COMPUTATIONAL-1" -> Usage.FLOAT_SHORT;
            case "COMP-2", "COMPUTATIONAL-2" -> Usage.FLOAT_LONG;
            case "INDEX" -> Usage.INDEX;
            case "POINTER" -> Usage.POINTER;
            case "PROCEDURE-POINTER" -> Usage.PROCEDURE_POINTER;
            case "FUNCTION-POINTER" -> Usage.FUNCTION_POINTER;
            case "NATIONAL" -> Usage.NATIONAL;
            default -> Usage.DISPLAY;
        };
    }

    /** The context's text as written, its blanks folded to one. */
    static String text(ParserRuleContext ctx) {
        return ctx.getStart().getInputStream().getText(Interval.of(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex())).replaceAll("\\s+", " ").trim();
    }

    /** A qualified name's first word: an ODO object, a key, or what a 66 renames inside its own record. */
    private static String first(CobolParser.QualifiedDataNameContext q) {
        return q.getChild(0).getChild(0).getText().toUpperCase(Locale.ROOT);
    }

}
