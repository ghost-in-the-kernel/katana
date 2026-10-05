package katana.sir.build;

import java.util.List;
import katana.front.Parse;
import katana.sir.build.DataMap.Node;

/**
 * The data map's names and places (tithe pass 2, C3, from DataMap): REDEFINES, ODO objects and a 66's
 * range resolved, then each record laid out. Sizes are IBM's: a binary item of 1-4 digits takes 2
 * bytes, 5-9 four, 10-18 eight; packed takes digits/2+1; COMP-1, INDEX, POINTER and FUNCTION-POINTER 4;
 * COMP-2 and PROCEDURE-POINTER 8; DISPLAY one byte a position, a separate sign one more. A group's
 * length is its children's, each times its OCCURS maximum, a REDEFINES child taking none; SYNC aligns a
 * binary or floating item to its own size from the record's start.
 */
final class Placement {
    private final Parse.Result parse;
    private final List<Node> nodes;
    private final List<String> findings;

    Placement(Parse.Result parse, List<Node> nodes, List<String> findings) {
        this.parse = parse;
        this.nodes = nodes;
        this.findings = findings;
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
        findings.add("REDEFINES " + name + " at " + Origins.at(parse, n.ctx) + " names no preceding item of its level");
        return 0;
    }

    /** The first item of a name (its qualifiers were dropped where it was read). */
    int named(String name) {
        for (Node m : nodes) if (m.name.equals(name)) return m.id;
        findings.add(name + " names no item");
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

    /** The item of a name in a record, or 0 and a finding. */
    private int inRecord(String name, int record) {
        for (Node m : nodes) if (m.record == record && m.level != 66 && m.name.equals(name)) return m.id;
        findings.add("RENAMES " + name + " names no item of its record");
        return 0;
    }

    private void renamed(Node n) {
        int from = inRecord(n.redefinesName, n.record), thru = inRecord(n.dependingName, n.record);
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

}
