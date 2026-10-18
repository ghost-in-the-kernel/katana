package katana.intent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import katana.intent.types.Fact;
import katana.intent.types.Subject;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** The review file's evidence lines (R-066): what the program does with an item or a block of steps, and where, in the program's own names and plain words. */
final class BatchReviewEvidence {
    private BatchReviewEvidence() {}

    
    /** One statement with the paragraph it is in. */
    record At(String paragraph, Statement s) {}

    static List<At> statements(Program p) {
        List<At> out = new ArrayList<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) add(where(g), s, out);
        return out;
    }

    private static void add(String paragraph, Statement s, List<At> out) {
        out.add(new At(paragraph, s));
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) add(paragraph, t, out);
    }

    private static String where(Procedure.Paragraph g) {
        return g.name().isBlank() ? "the main line (the statements before the first paragraph name)" : g.name();
    }

    static String evidence(Program p, Fact f) {
        NAMES.get().clear();
        p.items().forEach(i -> NAMES.get().put(i.id(), i.name()));
        return switch (f.subject()) {
            case Subject.Datum d -> f.kind().name().equals("MONEY") ? money(p, d.item()) : flag(p, d.item());
            case Subject.Choice c -> "has " + c.conditions().size() + " named values: " + values(p.items().get(c.item() - 1)) + unnamed(p, c.item()) + unused(p, c.item()) + "; " + touched(p, c.item());
            case Subject.Range r -> function(p, r);
            default -> f.subject().text();
        };
    }

    private static String money(Program p, int item) {
        List<String> said = new ArrayList<>();
        for (At a : statements(p)) {
            Statement s = a.s();
            boolean stored = s.operands().stream().anyMatch(o -> Set.of("add-to", "add-giving", "subtract-minuend", "subtract-giving", "compute-store").contains(o.role()) && refers(o.value(), item));
            if (stored) {
                Set<String> from = names(p, s, item, Set.of("add-from", "subtract-subtrahend", "compute-statement")), also = names(p, s, item, Set.of("add-to", "add-giving", "subtract-minuend", "subtract-giving", "compute-store"));
                String verb = switch (s.verb()) {
                    case "ADD" -> "ADD adds " + String.join(", ", from) + " into it";
                    case "SUBTRACT" -> "SUBTRACT takes " + String.join(", ", from) + " from it";
                    default -> "COMPUTE sets it to " + formula(s);
                };
                if (!from.isEmpty() || !also.isEmpty()) said.add(verb + (also.isEmpty() ? "" : " (and into " + String.join(", ", also) + ")") + " in " + a.paragraph());
            }
            if (s.verb().equals("MOVE") && s.operands().stream().anyMatch(o -> o.role().equals("move-to-sending-area") && refers(o.value(), item))) {
                for (Statement.Operand o : s.operands()) {
                    if (!o.role().equals("move-to-statement") || !(o.value() instanceof Value.Field t)) continue;
                    Item to = p.items().get(t.item() - 1);
                    if (to.attributes().category() == katana.sir.types.Attributes.Category.NUMERIC_EDITED && (to.attributes().picture().contains(",") || to.attributes().picture().contains(p.header().currency()))) {
                        said.add("MOVE copies it to " + to.name() + ", a field printed as " + to.attributes().picture() + " (" + printed(to.attributes().picture()) + "), in " + a.paragraph());
                    }
                }
            }
        }
        return a2s(said) + " (" + kind(p.items().get(item - 1)) + ")";
    }

    /** The two-place items other than this one a statement's operands of these roles name. */
    private static Set<String> names(Program p, Statement s, int item, Set<String> roles) {
        Set<String> out = new LinkedHashSet<>();
        for (Statement.Operand o : s.operands()) {
            if (!roles.contains(o.role())) continue;
            for (Item i : p.items()) if (i.id() != item && i.attributes().scale() == 2 && refers(o.value(), i.id())) out.add(i.name());
        }
        return out;
    }

    /** The literal values the program MOVEs into the item that none of its condition-names names. */
    private static String unnamed(Program p, int item) {
        Set<String> named = new java.util.HashSet<>(), stored = new LinkedHashSet<>();
        for (Item.Condition c : p.items().get(item - 1).conditions()) for (Value v : c.values()) named.add(value(v));
        for (At a : statements(p)) {
            Statement s = a.s();
            if (!s.verb().equals("MOVE") || s.operands().stream().noneMatch(o -> o.role().equals("move-to-statement") && o.value() instanceof Value.Field f && f.item() == item)) continue;
            for (Statement.Operand o : s.operands()) if (o.role().equals("move-to-sending-area") && o.value() instanceof Value.Literal l && !named.contains(value(l))) stored.add(value(l));
        }
        return stored.isEmpty() ? "" : " (the program also stores " + String.join(", ", stored) + ", which no condition-name names)";
    }

    /** The condition-names no statement of the program sets or tests. */
    private static String unused(Program p, int item) {
        Set<String> used = new java.util.HashSet<>();
        for (At a : statements(p)) {
            for (Statement.Operand o : a.s().operands()) names(o.value(), item, used);
            for (Statement.Branch b : a.s().branches()) if (b.condition() != null) names(b.condition(), item, used);
        }
        List<String> never = p.items().get(item - 1).conditions().stream().map(Item.Condition::name).filter(n -> !used.contains(n)).toList();
        return never.isEmpty() ? "" : "; never set or tested by name in this program: " + String.join(", ", never);
    }

    private static void names(Value v, int item, Set<String> out) {
        if (v instanceof Value.ConditionName c && c.item() == item) out.add(c.name());
        if (v instanceof Value.Expression e) e.operands().forEach(o -> names(o, item, out));
    }

    private static String flag(Program p, int item) {
        Item i = p.items().get(item - 1);
        return "a yes-or-no: " + values(i) + "; " + touched(p, item) + " (" + kind(i) + ")";
    }

    /** Where the program sets the item, where it checks it, and what it hands it to. */
    private static String touched(Program p, int item) {
        Set<String> checked = checked(p, item);
        return String.join("; ", setting(p, item)) + (checked.isEmpty() ? "; not tested in this program" : "; tested in " + list(checked)) + handed(p, item);
    }

    /** The paragraphs that MOVE into the item or SET one of its condition-names, and who else sets it: the file system, a caller. */
    private static List<String> setting(Program p, int item) {
        Set<String> set = new LinkedHashSet<>();
        for (At a : statements(p)) {
            Statement s = a.s();
            boolean moves = s.verb().equals("MOVE") && s.operands().stream().anyMatch(o -> o.role().equals("move-to-statement") && refers(o.value(), item));
            boolean sets = s.verb().equals("SET") && s.operands().stream().anyMatch(o -> o.value() instanceof Value.ConditionName c && c.item() == item);
            if (moves || sets) set.add(a.paragraph());
        }
        List<String> setting = new ArrayList<>();
        if (!set.isEmpty()) setting.add("set in " + list(set));
        for (katana.sir.types.FileDescriptor d : p.files()) if (d.status() == item) setting.add("set by the file system after each operation on " + d.name() + ": it is that file's status, a two-character code of which this program names only some values");
        if (p.items().get(item - 1).section() == Item.Section.LINKAGE) setting.add("set by the program that CALLs this one (it is in the LINKAGE SECTION)");
        if (setting.isEmpty()) setting.add("set nowhere in this program");
        return setting;
    }

    /** The paragraphs whose IF, EVALUATE, PERFORM UNTIL or SEARCH, or a branch's condition, reads the item. */
    static Set<String> checked(Program p, int item) {
        Set<String> checked = new LinkedHashSet<>();
        for (At a : statements(p)) {
            Statement s = a.s();
            for (Statement.Branch b : s.branches()) if (b.condition() != null && refers(b.condition(), item)) checked.add(a.paragraph());
            if (Set.of("IF", "EVALUATE", "PERFORM", "SEARCH").contains(s.verb()) && s.operands().stream().anyMatch(o -> refers(o.value(), item))) checked.add(a.paragraph());
        }
        return checked;
    }

    /** Each CALL that hands the item, or a group holding it, to another program. */
    private static String handed(Program p, int item) {
        Set<String> called = new LinkedHashSet<>();
        for (At a : statements(p)) {
            if (!a.s().verb().equals("CALL")) continue;
            for (int at = item; at > 0; at = p.items().get(at - 1).parent()) {
                final int held = at;
                if (a.s().operands().stream().anyMatch(o -> refers(o.value(), held))) called.add(callee(a.s()) + " in " + a.paragraph() + (held == item ? "" : " (inside " + p.items().get(held - 1).name() + ")"));
            }
        }
        return called.isEmpty() ? "" : "; handed to another program by CALL " + list(called);
    }

    private static String callee(Statement s) {
        return s.operands().stream().filter(o -> o.value() instanceof Value.Literal).map(o -> ((Value.Literal) o.value()).payload()).findFirst().orElse("a program named at run time");
    }

    private static String function(Program p, Subject.Range r) {
        String first = p.procedure().paragraphs().get(r.first()).name(), last = p.procedure().paragraphs().get(r.last()).name();
        Set<String> callers = new LinkedHashSet<>();
        for (Procedure.Edge e : p.procedure().edges()) if (e.kind() == Procedure.Kind.PERFORM && e.to() == r.first() && e.thru() == r.last()) callers.add(where(p.procedure().paragraphs().get(e.from())));
        java.util.Map<String, Integer> verbs = new java.util.LinkedHashMap<>();
        boolean abend = false;
        for (int g = r.first(); g <= r.last(); g++) for (Statement s : p.procedure().paragraphs().get(g).statements()) {
            verbs.merge(s.verb(), 1, Integer::sum);
            abend |= s.verb().equals("CALL") && s.operands().stream().anyMatch(o -> o.value() instanceof Value.Literal l && l.payload().equals("CEE3ABD"));
        }
        int statements = verbs.values().stream().mapToInt(Integer::intValue).sum();
        String done = abend ? "; it ends the job (CALL 'CEE3ABD'), so on the real system it never returns, though in the test runs CEE3ABD is a stand-in that does"
                : " and, when it runs off its end, returns to the statement after that PERFORM";
        return "the block " + first + (r.first() == r.last() ? "" : " through " + last) + " (" + statements + " statement" + (statements == 1 ? "" : "s") + ": " + verbs(verbs) + ") is run by PERFORM from " + list(callers)
                + done + "; nothing jumps into it or out of it with GO TO";
    }

    /** Statements by verb in plain counting words, EXIT glossed as the no-op it is. */
    private static String verbs(java.util.Map<String, Integer> verbs) {
        List<String> out = new ArrayList<>();
        for (var e : verbs.entrySet()) {
            String verb = e.getKey().equals("EXIT") ? "EXIT (which does nothing; it marks the end)" : e.getKey();
            out.add(e.getValue() == 1 ? (verb.matches("^[AEIOU].*") ? "an " : "a ") + verb : e.getValue() + " " + verb + "s");
        }
        return String.join(", ", out);
    }

    /** A COMPUTE's formula by the program's names, multiplication and division as the signs a reader knows. */
    private static String formula(Statement s) {
        return s.operands().stream().filter(o -> o.role().equals("compute-statement")).map(o -> expression(o.value(), true)).findFirst().orElse("a formula");
    }

    private static String expression(Value v, boolean top) {
        if (v instanceof Value.Expression e && e.operands().size() == 2) {
            String op = switch (e.operator()) {
                case "*" -> " × ";
                case "/" -> " ÷ ";
                default -> " " + e.operator() + " ";
            };
            String inner = expression(e.operands().get(0), false) + op + expression(e.operands().get(1), false);
            return top ? inner : "(" + inner + ")";
        }
        return v instanceof Value.Literal l ? l.payload() : v instanceof Value.Field f ? NAMES.get().get(f.item()) : String.valueOf(v);
    }

    /** The current program's item names, for `expression`. */
    private static final ThreadLocal<java.util.Map<Integer, String>> NAMES = ThreadLocal.withInitial(java.util.HashMap::new);

    /** An edited picture in words: thousands commas, places after the point, a sign. */
    private static String printed(String picture) {
        List<String> said = new ArrayList<>();
        if (picture.contains(",")) said.add("thousands commas");
        int point = picture.indexOf('.');
        if (point >= 0) said.add((picture.length() - point - 1) + " decimals");
        if (picture.startsWith("-")) said.add("a minus sign when negative");
        if (picture.startsWith("+")) said.add("a plus or minus sign");
        if (picture.contains("$")) said.add("a dollar sign");
        return String.join(", ", said);
    }

    private static String values(Item i) {
        List<String> out = new ArrayList<>();
        for (Item.Condition c : i.conditions()) out.add(c.name() + " = " + String.join(" or ", c.values().stream().map(BatchReviewEvidence::value).toList()));
        return out.size() <= 6 ? String.join(", ", out) : String.join(", ", out.subList(0, 4)) + " and " + (out.size() - 4) + " more (every name is on the fact line)";
    }

    static String value(Value v) {
        if (v instanceof Value.Literal l) return l.kind() == Value.Literal.Kind.NUMERIC ? l.payload() : "'" + l.payload() + "'";
        if (v instanceof Value.Figurative f) return f.name().toLowerCase().replace('-', ' ');
        if (v instanceof Value.Expression e && e.operator().equals("THRU")) return value(e.operands().get(0)) + " to " + value(e.operands().get(1));
        return String.valueOf(v);
    }

    private static String kind(Item i) {
        var a = i.attributes();
        return "it is " + (a.category() == katana.sir.types.Attributes.Category.NUMERIC ? "a number of " + a.digits() + " digits, " + a.scale() + " after the point" + (a.signed() ? ", signed" : "") : "text of " + i.length() + " characters")
                + ", picture " + a.picture() + ", declared in " + BatchReview.file(i.origin().file());
    }

    private static String list(Set<String> s) {
        return s.isEmpty() ? "none" : s.size() <= 4 ? String.join(", ", s) : String.join(", ", new ArrayList<>(s).subList(0, 4)) + " and " + (s.size() - 4) + " more";
    }

    private static String a2s(List<String> said) {
        return said.size() <= 3 ? String.join("; ", said) : String.join("; ", said.subList(0, 3)) + "; and " + (said.size() - 3) + " more such";
    }

    static boolean refers(Value v, int item) {
        if (v instanceof Value.Field f) return f.item() == item || f.subscripts().stream().anyMatch(s -> refers(s, item));
        if (v instanceof Value.ConditionName c) return c.item() == item;
        if (v instanceof Value.Expression e) return e.operands().stream().anyMatch(o -> refers(o, item));
        return false;
    }
}
