package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** INSPECT, STRING and UNSTRING: their phrases read from the operands in order (the IR keeps the keywords), then one `rt` call. */
final class TextVerbs {
    private final Flow f;

    TextVerbs(Flow f) {
        this.f = f;
    }

    private static boolean isKeyword(Value v, String... words) {
        return v instanceof Value.Name n && n.kind().equals("keyword") && List.of(words).contains(n.text());
    }

    private static String word(Value v) {
        return ((Value.Name) v).text();
    }

    /** A comparand, substitution or delimiter's bytes: an item as INSPECT sees it, a literal's bytes, a figurative constant's character. */
    String bytes(Value v) {
        if (v instanceof Value.Field fld) return "Inspect.view(" + f.x.field(fld) + ")";
        if (v instanceof Value.Figurative g && g.all() != null) return bytes(g.all());
        String pattern = f.x.pattern(v);
        return pattern != null ? pattern : "Literal.bytes(" + f.x.sending(v) + ")";
    }

    /** One INSPECT phrase as it is read: kind, comparand, substitution, delimiters, and the count field it tallies into. */
    private static final class Phrase {
        String kind, before, after, counter;
        Value subject, by;
    }

    // Decision: R-019
    void inspect(Statement s) {
        Value target = Verbs.operand(s, "inspect-statement");
        List<Phrase> tallying = new ArrayList<>(), replacing = new ArrayList<>();
        String converting = converting(s);
        read(s, tallying, replacing);
        String item = f.x.field((Value.Field) target);
        f.e.open("");
        f.e.line("byte[] v = Inspect.view(" + item + ");");
        if (!tallying.isEmpty()) {
            f.e.line("long[] c = Inspect.tally(v, 0, v.length, List.of(" + phrases(tallying) + "));");
            for (int k = 0; k < tallying.size(); k++) f.e.line("Inspect.add(" + tallying.get(k).counter + ", c[" + k + "]);");
        }
        if (!replacing.isEmpty()) f.e.line("Inspect.replace(v, 0, v.length, List.of(" + phrases(replacing) + "));");
        if (converting != null) f.e.line(converting);
        f.e.line("Inspect.back(" + item + ", v);");
        f.e.close();
    }

    /** The phrases, in order: TALLYING ones carry their count field, REPLACING ones their substitution. */
    private void read(Statement s, List<Phrase> tallying, List<Phrase> replacing) {
        Reader r = new Reader(tallying, replacing);
        for (Statement.Operand o : s.operands()) r.take(o);
    }

    /** INSPECT's operands read one at a time: the keyword sets what the next value is. */
    private final class Reader {
        private final List<Phrase> tallying, replacing;
        private String counter, kind = "ALL", delimiter;
        private boolean replace;
        private Phrase last;

        Reader(List<Phrase> tallying, List<Phrase> replacing) {
            this.tallying = tallying;
            this.replacing = replacing;
        }

        void take(Statement.Operand o) {
            Value v = o.value();
            if (isKeyword(v, "REPLACING")) replace = true;
            else if (isKeyword(v, "ALL", "LEADING", "FIRST")) kind = word(v);
            else if (isKeyword(v, "CHARACTERS")) last = add(replace ? replacing : tallying, "CHARACTERS", null, counter);
            else if (isKeyword(v, "BEFORE", "AFTER")) delimiter = word(v);
            else if (!(v instanceof Value.Name)) value(o.role(), v);
        }

        private void value(String role, Value v) {
            switch (role) {
                case "inspect-for" -> counter = f.x.field((Value.Field) v);
                case "inspect-by" -> {
                    if (last != null) last.by = v;
                }
                case "inspect-before-after" -> {
                    if (last != null && "BEFORE".equals(delimiter)) last.before = bytes(v);
                    else if (last != null) last.after = bytes(v);
                }
                case "inspect-all-leading", "inspect-replacing-all-leading" -> last = add(replace ? replacing : tallying, v instanceof Value.Figurative g && g.all() != null ? "ALL" : kind, v, counter);
                default -> { }
            }
        }
    }

    private static Phrase add(List<Phrase> list, String kind, Value subject, String counter) {
        Phrase p = new Phrase();
        p.kind = kind;
        p.subject = subject;
        p.counter = counter;
        list.add(p);
        return p;
    }

    private String phrases(List<Phrase> list) {
        List<String> out = new ArrayList<>();
        for (Phrase p : list) {
            String subject = p.subject == null ? "null" : bytes(p.subject);
            String by = p.by == null ? "null" : p.subject == null ? bytes(p.by) : "Inspect.repeat(" + bytes(p.by) + ", " + subject + ".length)";
            out.add("new Inspect.Phrase(Inspect.Kind." + p.kind + ", " + subject + ", " + by + ", " + p.before + ", " + p.after + ")");
        }
        return String.join(", ", out);
    }

    /** CONVERTING's call, or null. */
    private String converting(Statement s) {
        Value from = Verbs.operand(s, "inspect-converting-phrase"), to = Verbs.operand(s, "inspect-to");
        if (from == null || to == null) return null;
        String before = null, after = null, delimiter = null;
        for (Statement.Operand o : s.operands()) {
            if (isKeyword(o.value(), "BEFORE", "AFTER")) delimiter = word(o.value());
            else if (o.role().equals("inspect-before-after") && !(o.value() instanceof Value.Name) && delimiter != null) {
                if (delimiter.equals("BEFORE")) before = bytes(o.value());
                else after = bytes(o.value());
            }
        }
        return "Inspect.convert(v, 0, v.length, " + bytes(from) + ", " + bytes(to) + ", " + before + ", " + after + ");";
    }

    /** A STRING sender's bytes: the item as if alphanumeric (LR p. 459), a literal's, a figurative constant's character. */
    private String sent(Value v) {
        if (v instanceof Value.Field fld) return "Literal.bytes(" + f.x.field(fld) + ")";
        String pattern = f.x.pattern(v);
        return pattern != null ? pattern : "Literal.bytes(" + f.x.sending(v) + ")";
    }

    // Decision: R-019
    void string(Statement s) {
        List<String> senders = new ArrayList<>(), pending = new ArrayList<>();
        for (Statement.Operand o : s.operands()) {
            Value v = o.value();
            if (o.role().equals("string-sending") && !(v instanceof Value.Name)) pending.add(sent(v));
            else if (o.role().equals("string-delimited-by-phrase") && (isKeyword(v, "SIZE") || !(v instanceof Value.Name))) {
                String delimiter = isKeyword(v, "SIZE") ? "null" : sent(v);
                for (String p : pending) senders.add("new Strings.Sender(" + p + ", " + delimiter + ")");
                pending.clear();
            }
        }
        for (String p : pending) senders.add("new Strings.Sender(" + p + ", null)");
        String into = f.x.field((Value.Field) Verbs.operand(s, "string-into-phrase"));
        Value pointer = Verbs.operand(s, "string-with-pointer-phrase");
        f.e.open("");
        f.e.line("Strings.Result r = Strings.string(List.of(" + String.join(", ", senders) + "), " + into + ".record().bytes(), " + into + ".offset(), " + into + ".attr().length(), " + (pointer == null ? "1" : f.x.index(pointer)) + ");");
        f.e.line("ov = r.overflow();");
        if (pointer != null) f.e.line("Move.move(Literal.number(String.valueOf(r.pointer()), 0, cp), " + f.x.field((Value.Field) pointer) + ");");
        f.e.close();
        overflow(s);
    }

    private void overflow(Statement s) {
        List<Statement> on = Verbs.branch(s, "on-overflow-phrase"), not = Verbs.branch(s, "not-on-overflow-phrase");
        if (!on.isEmpty() || !not.isEmpty()) f.branch("ov", on, not);
    }

    // Decision: R-019
    void unstring(Statement s) {
        List<String> delimiters = new ArrayList<>(), receivers = new ArrayList<>(), counts = new ArrayList<>();
        boolean all = false;
        for (Statement.Operand o : s.operands()) {
            Value v = o.value();
            boolean phrase = o.role().equals("unstring-delimited-by-phrase") || o.role().equals("unstring-or-all-phrase");
            if (phrase && isKeyword(v, "ALL")) all = true;
            else if (phrase && !(v instanceof Value.Name)) {
                delimiters.add("new Strings.Delimiter(" + sent(v) + ", " + all + ")");
                all = false;
            } else if (o.role().equals("unstring-into") && v instanceof Value.Field fld) {
                receivers.add("new Strings.Receiver(" + f.x.field(fld) + ", null)");
                counts.add(null);
            } else if (o.role().equals("unstring-delimiter-in") && v instanceof Value.Field fld) {
                String last = receivers.remove(receivers.size() - 1);
                receivers.add(last.replace(", null)", ", " + f.x.field(fld) + ")"));
            } else if (o.role().equals("unstring-count-in") && v instanceof Value.Field fld) counts.set(counts.size() - 1, f.x.field(fld));
        }
        String from = f.x.field((Value.Field) Verbs.operand(s, "unstring-sending-phrase"));
        Value pointer = Verbs.operand(s, "unstring-with-pointer-phrase"), tally = Verbs.operand(s, "unstring-tallying-phrase");
        f.e.open("");
        f.e.line("Strings.Unstrung u = Strings.unstring(" + from + ".record().bytes(), " + from + ".offset(), " + from + ".attr().length(), List.of(" + String.join(", ", delimiters) + "), List.of(" + String.join(", ", receivers) + "), "
                + (pointer == null ? "1" : f.x.index(pointer)) + ", " + (tally == null ? "0" : f.x.index(tally)) + ");");
        f.e.line("ov = u.result().overflow();");
        for (int k = 0; k < counts.size(); k++) if (counts.get(k) != null) f.e.line("if (u.counts()[" + k + "] >= 0) Move.move(Literal.number(String.valueOf(u.counts()[" + k + "]), 0, cp), " + counts.get(k) + ");");
        if (pointer != null) f.e.line("Move.move(Literal.number(String.valueOf(u.result().pointer()), 0, cp), " + f.x.field((Value.Field) pointer) + ");");
        if (tally != null) f.e.line("Move.move(Literal.number(String.valueOf(u.result().tally()), 0, cp), " + f.x.field((Value.Field) tally) + ");");
        f.e.close();
        overflow(s);
    }
}
