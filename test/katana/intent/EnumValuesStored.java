package katana.intent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * T-E1 (research §33, §34): an `enum` fact's reading, that the item holds only values its condition-names
 * name, checked on what the program stores into it (a MOVE of a literal, a SET of a condition-name) and the
 * literals it compares it with; where something outside the program sets it too (the file system, a
 * caller, a program it is handed to), the program alone cannot tell.
 */
final class EnumValuesStored {
    static final String TEST = "T-E1";
    private static final Set<String> RELATIONS = Set.of("=", "<", ">", "<=", ">=", "NOT =", "<>", "EQUAL", "NOT EQUAL");

    private EnumValuesStored() {}

    static Verdict verdict(Program p, int item, Corpus corpus) {
        Item it = p.items().get(item - 1);
        Map<String, Set<String>> unnamed = new LinkedHashMap<>();
        List<String> outside = new ArrayList<>(), followed = new ArrayList<>();
        int stored = stores(p, item, it, "", unnamed) + ends(p, item, corpus, unnamed, outside, followed);
        outside.addAll(0, outside(p, item));
        String count = stored + " statement" + (stored == 1 ? "" : "s") + (followed.isEmpty() ? "" : ", here and in " + String.join(" and ", followed));
        if (!unnamed.isEmpty()) {
            List<String> said = new ArrayList<>();
            unnamed.forEach((v, at) -> said.add(v + " (in " + list(at) + ")"));
            return Verdict.refutes(TEST, "the program stores or compares it with " + String.join("; ", said) + ", which no condition-name names");
        }
        String own = stored == 0 ? "the program itself stores nothing into it and compares it with no literal" : "every value the program itself stores is named (" + count + ")";
        if (!outside.isEmpty()) return Verdict.cannotTell(TEST, own + ", but it is also " + String.join("; ", outside) + ", and what that stores the program does not say");
        return Verdict.holds(TEST, "every value stored into it or compared with it is named (" + count + "), and nothing else sets it");
    }

    /** The statements of `q` that store into or compare its item `there`, each value no condition-name of `it` names kept with its paragraph. */
    private static int stores(Program q, int there, Item it, String program, Map<String, Set<String>> unnamed) {
        int stored = 0;
        for (BatchReviewEvidence.At a : BatchReviewEvidence.statements(q)) {
            for (Value v : storedOrCompared(a.s(), there)) {
                stored++;
                if (!named(it, v, key(v, it))) unnamed.computeIfAbsent(BatchReviewEvidence.value(v), k -> new LinkedHashSet<>()).add(program + a.paragraph());
            }
        }
        return stored;
    }

    /** The other ends of a CALL that shares the item: a CardDemo program's stores are read as this one's; any other end is something outside. */
    private static int ends(Program p, int item, Corpus corpus, Map<String, Set<String>> unnamed, List<String> outside, List<String> followed) {
        Item it = p.items().get(item - 1);
        int stored = 0;
        List<CallEnds.End> callers = it.section() == Item.Section.LINKAGE ? CallEnds.callers(p, item, corpus) : List.of();
        if (it.section() == Item.Section.LINKAGE && (callers.isEmpty() || callers.stream().anyMatch(e -> e.there() == null))) outside.add("in the LINKAGE SECTION, so the program that calls this one sets it");
        List<CallEnds.End> ends = new ArrayList<>(CallEnds.callees(p, item, corpus));
        if (!outside.isEmpty()) callers = List.of();
        ends.addAll(callers);
        Set<String> seen = new java.util.HashSet<>();
        for (CallEnds.End e : ends) {
            if (!seen.add(e.program() + " " + (e.there() == null ? "" : e.there().id()) + " " + e.holder())) continue;
            if (e.there() == null) {
                outside.add("handed by CALL to " + e.program() + (e.holder().isEmpty() ? "" : " inside " + e.holder()) + ", which may set it");
                continue;
            }
            stored += stores(e.other(), e.there().id(), it, e.program() + " ", unnamed);
            followed.add(e.program() + (e.callee() ? ", which a CALL hands it to" : ", which CALLs this one") + ", as " + e.there().name());
        }
        return stored;
    }

    static String list(Set<String> s) {
        return s.size() <= 4 ? String.join(", ", s) : String.join(", ", new ArrayList<>(s).subList(0, 4)) + " and " + (s.size() - 4) + " more";
    }

    /** The values one statement stores into the item, or compares it with: a MOVE's literal, a SET's condition-name, a relation's literal. */
    static List<Value> storedOrCompared(Statement s, int item) {
        List<Value> out = new ArrayList<>();
        if (s.verb().equals("MOVE") && s.operands().stream().anyMatch(o -> o.role().equals("move-to-statement") && whole(o.value(), item))) {
            for (Statement.Operand o : s.operands()) if (o.role().equals("move-to-sending-area") && (o.value() instanceof Value.Literal || o.value() instanceof Value.Figurative)) out.add(o.value());
        }
        if (s.verb().equals("SET")) for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.ConditionName c && c.item() == item) out.add(c);
        for (Statement.Operand o : s.operands()) compared(o.value(), item, out);
        for (Statement.Branch b : s.branches()) if (b.condition() != null) compared(b.condition(), item, out);
        return out;
    }

    private static void compared(Value v, int item, List<Value> out) {
        if (!(v instanceof Value.Expression e)) return;
        if (RELATIONS.contains(e.operator()) && e.operands().size() == 2) {
            Value a = e.operands().get(0), b = e.operands().get(1);
            if (whole(a, item) && constant(b)) out.add(b);
            else if (whole(b, item) && constant(a)) out.add(a);
        }
        e.operands().forEach(o -> compared(o, item, out));
    }

    private static boolean constant(Value v) {
        return v instanceof Value.Literal || v instanceof Value.Figurative;
    }

    /** The item itself, not a part of it by reference modification. */
    private static boolean whole(Value v, int item) {
        return v instanceof Value.Field f && f.item() == item && f.start() == null;
    }

    private static boolean named(Item it, Value v, String key) {
        if (v instanceof Value.ConditionName) return true;
        for (Item.Condition c : it.conditions()) for (Value w : c.values()) {
            if (w instanceof Value.Expression e && e.operator().equals("THRU") && numeric(it)) {
                BigDecimal n = number(key), lo = number(key(e.operands().get(0), it)), hi = number(key(e.operands().get(1), it));
                if (n != null && lo != null && hi != null && n.compareTo(lo) >= 0 && n.compareTo(hi) <= 0) return true;
            } else if (key(w, it).equals(key)) return true;
        }
        return false;
    }

    /** A value as the item would hold it: a number by its worth, text without its trailing spaces, ZERO as the item's zeros. */
    static String key(Value v, Item it) {
        if (v instanceof Value.Literal l && l.numeric()) return new BigDecimal(l.payload()).scaleByPowerOfTen(-l.scale()).stripTrailingZeros().toPlainString();
        if (v instanceof Value.Literal l) return numeric(it) && number(l.payload().strip()) != null ? number(l.payload().strip()).stripTrailingZeros().toPlainString() : l.payload().stripTrailing();
        if (v instanceof Value.Figurative f) {
            String name = f.name().replaceAll("E?S$", "");
            if (name.equals("ZERO")) return numeric(it) ? "0" : "0".repeat(it.length());
            if (name.equals("SPACE")) return "";
            return name;
        }
        return String.valueOf(v);
    }

    private static boolean numeric(Item it) {
        return it.attributes().category() == Attributes.Category.NUMERIC;
    }

    private static BigDecimal number(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Who besides the program's own MOVEs and SETs stores into the item, but a CALL's other end: the file system, a READ or a group MOVE over it. */
    static List<String> outside(Program p, int item) {
        List<String> out = new ArrayList<>();
        for (katana.sir.types.FileDescriptor d : p.files()) if (d.status() == item) out.add("the status of " + d.name() + ", which the file system sets after each operation");
        Item it = p.items().get(item - 1);
        if (it.section() == Item.Section.FILE) out.add("in a file's record, so a READ sets it");
        for (BatchReviewEvidence.At a : BatchReviewEvidence.statements(p)) {
            Statement s = a.s();
            for (Statement.Operand o : s.operands()) {
                boolean into = Set.of("move-to-statement", "read-into", "move-corresponding-to-statement", "unstring-into", "accept-statement", "string-into-phrase").contains(o.role());
                if (into && o.value() instanceof Value.Field f && f.item() != item && holds(p, f.item(), item)) {
                    out.add("set as part of " + p.items().get(f.item() - 1).name() + " by " + s.verb() + " in " + a.paragraph());
                    break;
                }
            }
        }
        return out.stream().distinct().toList();
    }

    /** Whether the group `outer` holds the item. */
    static boolean holds(Program p, int outer, int item) {
        for (int at = item; at > 0; at = p.items().get(at - 1).parent()) if (at == outer) return true;
        return false;
    }
}
