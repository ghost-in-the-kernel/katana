package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.Attributes;
import katana.sir.types.Attributes.Category;
import katana.sir.types.Attributes.Usage;
import katana.sir.types.Item;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

/**
 * The data map (docs/ROADMAP.md 2.5): each section's entries, in textual order, to items with their
 * parent and record, length and offset, attribute tuple, REDEFINES, OCCURS, SYNC slack, VALUE and 88s.
 * Sizes are IBM's: a binary item of 1-4 digits takes 2 bytes, 5-9 four, 10-18 eight; packed takes
 * digits/2+1; COMP-1, INDEX, POINTER and FUNCTION-POINTER 4; COMP-2 and PROCEDURE-POINTER 8; DISPLAY one
 * byte a position, a separate sign one more. A group's length is its children's, each times its
 * OCCURS maximum, a REDEFINES child taking none; SYNC aligns a binary or floating item to its own
 * size from the record's start. An INDEXED BY name is an item of its own, level 77, USAGE INDEX. A 66
 * that renames one item has that item's attributes; one that renames a range is a group. BLANK WHEN
 * ZERO makes a numeric DISPLAY item numeric-edited.
 */
final class DataMap {
    /** One entry while the map is built; a group's length is known only after its children's. */
    static final class Node {
        int id, level, parent, record, redefines, length, offset, syncSlack, occursMin, occursMax = 0, dependingOn;
        String name, picture = "", value, dependingName, redefinesName;
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
        ParserRuleContext ctx;
    }

    private final Parse.Result parse;
    char currency = '$';
    boolean decimalComma;
    final List<Node> nodes = new ArrayList<>();
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
            else if (entry.dataDescriptionEntryFormat3() != null && !nodes.isEmpty()) condition(entry.dataDescriptionEntryFormat3());
            else if (entry.dataDescriptionEntryFormat2() != null) renames(section, entry.dataDescriptionEntryFormat2(), open);
        }
    }

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
        if (!e.dataRedefinesClause().isEmpty()) n.redefinesName = e.dataRedefinesClause(0).dataName().getText().toUpperCase(Locale.ROOT);
        if (!e.dataValueClause().isEmpty()) n.value = text(e.dataValueClause(0)).replaceFirst("(?i)^VALUES?\\s+(IS\\s+|ARE\\s+)?", "");
        if (!e.dataOccursClause().isEmpty()) occurs(n, e.dataOccursClause(0));
    }

    private static void sign(Node n, CobolParser.DataSignClauseContext s) {
        n.signClause = true;
        n.leading = s.LEADING() != null;
        n.signSeparate = s.SEPARATE() != null;
    }

    private void occurs(Node n, CobolParser.DataOccursClauseContext o) {
        int first = o.integerLiteral() != null ? Integer.parseInt(o.integerLiteral().getText()) : 1;
        if (o.dataOccursTo() != null) {
            n.occursMin = first;
            n.occursMax = Integer.parseInt(o.dataOccursTo().integerLiteral().getText());
        } else {
            n.occursMin = first;
            n.occursMax = first;
        }
        if (o.dataOccursDepending() != null) n.dependingName = o.dataOccursDepending().qualifiedDataName().getText().toUpperCase(Locale.ROOT);
        for (var sort : o.dataOccursSort()) for (var key : sort.qualifiedDataName()) n.keyNames.add(new String[] {key.getText().toUpperCase(Locale.ROOT), sort.ASCENDING() != null ? "A" : "D"});
        for (var indexed : o.dataOccursIndexed()) for (var name : indexed.indexName()) n.indexNames.add(name.getText().toUpperCase(Locale.ROOT));
    }

    private void condition(CobolParser.DataDescriptionEntryFormat3Context c) {
        List<String> values = new ArrayList<>();
        for (var interval : c.dataValueClause().dataValueInterval()) values.add(text(interval));
        nodes.get(nodes.size() - 1).conditions.add(new Item.Condition(c.conditionName().getText().toUpperCase(Locale.ROOT), values, parse.origin(c)));
    }

    /** A 66: the record's bytes from its first item through its last, as an item of the record. */
    private void renames(Item.Section section, CobolParser.DataDescriptionEntryFormat2Context r, List<Node> open) {
        if (open.isEmpty()) return;
        Node n = new Node();
        n.level = 66;
        n.name = r.dataName().getText().toUpperCase(Locale.ROOT);
        n.section = section;
        n.ctx = r;
        n.redefinesName = r.dataRenamesClause().qualifiedDataName(0).getText().toUpperCase(Locale.ROOT);
        n.dependingName = r.dataRenamesClause().qualifiedDataName().size() > 1 ? r.dataRenamesClause().qualifiedDataName(1).getText().toUpperCase(Locale.ROOT) : n.redefinesName;
        Node record = nodes.get(open.get(0).record - 1);
        n.id = nodes.size() + 1;
        n.parent = record.id;
        n.record = record.id;
        nodes.add(n);
    }

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

    /** Names to items: REDEFINES the preceding sibling of that name, ODO objects and keys by name, a 66's range in its record. */
    void resolve() {
        for (Node n : nodes) {
            if (n.redefinesName != null && n.level != 66) n.redefines = sibling(n, n.redefinesName);
            if (n.dependingName != null && n.level != 66) n.dependingOn = named(n.dependingName);
        }
    }

    private int sibling(Node n, String name) {
        for (int i = n.id - 2; i >= 0; i--) {
            Node m = nodes.get(i);
            if (m.parent == n.parent && m.name.equals(name) && (n.parent != 0 || m.level == n.level)) return m.id;
        }
        findings.add("REDEFINES " + name + " at " + parse.origin(n.ctx) + " names no preceding item of its level");
        return 0;
    }

    /** The first item of a name, qualifiers (`A OF B`) dropped. */
    int named(String qualified) {
        String name = qualified.split("(?i)(OF|IN)(?=[A-Z])")[0];
        for (Node m : nodes) if (m.name.equals(name)) return m.id;
        findings.add(qualified + " names no item");
        return 0;
    }

    /** Each record laid out from offset 0; then each 66 over its record. */
    void layout() {
        for (Node n : nodes) if (n.parent == 0 && n.level != 66) place(n, 0, 0);
        for (Node n : nodes) if (n.level == 66) renamed(n);
    }

    /** An item at an offset in its record: an elementary one by its size, a group by its children, SYNC aligned. */
    private void place(Node n, int offset, int recordStart) {
        n.offset = offset;
        if (n.children.isEmpty()) {
            n.length = size(n);
            return;
        }
        int at = offset;
        for (Node child : n.children) {
            if (child.redefines != 0) {
                place(child, nodes.get(child.redefines - 1).offset, recordStart);
                at = Math.max(at, child.offset + extent(child));
                continue;
            }
            if (child.sync && child.children.isEmpty() && alignment(child) > 1) {
                int align = alignment(child);
                child.syncSlack = (align - (at - recordStart) % align) % align;
                at += child.syncSlack;
            }
            place(child, at, recordStart);
            at += extent(child);
        }
        n.length = at - offset;
    }

    private static int extent(Node n) {
        return n.length * Math.max(1, n.occursMax);
    }

    private void renamed(Node n) {
        int from = named(n.redefinesName), thru = named(n.dependingName);
        n.redefinesName = null;
        n.dependingName = null;
        if (from == 0 || thru == 0) return;
        Node a = nodes.get(from - 1), b = nodes.get(thru - 1);
        n.offset = a.offset;
        n.length = b.offset + extent(b) - a.offset;
        if (from == thru) n.renamesOne = a;
    }

    private static int alignment(Node n) {
        return switch (n.usage) {
            case BINARY, NATIVE_BINARY, FLOAT_SHORT, FLOAT_LONG, INDEX, POINTER, PROCEDURE_POINTER, FUNCTION_POINTER -> size(n);
            default -> 1;
        };
    }

    /** An elementary item's size in bytes, by its usage and PICTURE (IBM's sizes, above). */
    static int size(Node n) {
        Pictures p = n.picture.isEmpty() ? null : (n.pic = Pictures.read(n.picture, n.currency, n.decimalComma));
        return switch (n.usage) {
            case BINARY, NATIVE_BINARY -> p == null ? 4 : p.digits() <= 4 ? 2 : p.digits() <= 9 ? 4 : 8;
            case PACKED -> p == null ? 0 : p.digits() / 2 + 1;
            case FLOAT_SHORT, INDEX, POINTER, FUNCTION_POINTER -> 4;
            case FLOAT_LONG, PROCEDURE_POINTER -> 8;
            case NATIONAL, DISPLAY -> p == null ? 0 : p.positions() + (n.signSeparate && p.signed() ? 1 : 0);
        };
    }

    /** The built items, in textual order. */
    List<Item> items() {
        List<Item> items = new ArrayList<>();
        for (Node n : nodes) {
            Item.Occurs occurs = n.occursMax == 0 ? null : new Item.Occurs(n.occursMin, n.occursMax, n.dependingOn, keys(n), indexes(n));
            items.add(new Item(n.id, n.name, n.level, n.parent, n.record, n.section, n.offset, n.length, attributes(n), n.redefines, occurs, n.syncSlack, n.value,
                    n.conditions, parse.origin(n.ctx)));
        }
        return items;
    }

    private List<Item.Key> keys(Node n) {
        List<Item.Key> keys = new ArrayList<>();
        for (String[] k : n.keyNames) {
            int id = named(k[0]);
            if (id != 0) keys.add(new Item.Key(id, k[1].equals("A")));
        }
        return keys;
    }

    private List<Integer> indexes(Node n) {
        List<Integer> ids = new ArrayList<>();
        for (String name : n.indexNames) {
            for (int i = n.id; i < nodes.size(); i++) {
                if (nodes.get(i).name.equals(name) && nodes.get(i).usage == Usage.INDEX) {
                    ids.add(nodes.get(i).id);
                    break;
                }
            }
        }
        return ids;
    }

    /** The attribute tuple: a group's is GROUP; an elementary item's from its usage and PICTURE. */
    static Attributes attributes(Node n) {
        if (n.renamesOne != null) return attributes(n.renamesOne);
        if (!n.children.isEmpty() || n.level == 66) return new Attributes(n.usage, Category.GROUP, 0, 0, false, false, false, "");
        Pictures p = n.pic;
        Category category = switch (n.usage) {
            case FLOAT_SHORT, FLOAT_LONG -> Category.NUMERIC;
            case INDEX -> Category.INDEX;
            case POINTER, PROCEDURE_POINTER, FUNCTION_POINTER -> Category.POINTER;
            default -> p == null ? Category.ALPHANUMERIC : p.category() == Category.NUMERIC && n.blankWhenZero && n.usage == Usage.DISPLAY ? Category.NUMERIC_EDITED : p.category();
        };
        boolean numeric = category == Category.NUMERIC || category == Category.NUMERIC_EDITED;
        if (!numeric || p == null) return new Attributes(n.usage, category, 0, 0, false, false, false, p == null ? "" : p.expanded());
        boolean signed = p.signed();
        return new Attributes(n.usage, category, Math.max(1, p.digits()), p.scale(), signed, signed && n.signSeparate, signed && n.leading, p.expanded());
    }
}
