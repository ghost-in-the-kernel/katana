package katana.sir.contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import katana.sir.types.Attributes;
import katana.sir.types.Call;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * The facts a back end must honor (docs/ROADMAP.md 2.6, docs/SEMANTIC-IR.md "The contract"): one line
 * each, every line starting with its kind and its program. A kind is a row of KINDS, never a block of
 * code (T-13): its name, what it covers, and how its lines are written from the IR. A back end's output,
 * read back as contract lines, is compared with the IR's fact for fact (`diff`).
 */
public final class Contract {
    /** A kind of fact: its name, what it covers, and its lines for one program (without the kind and program). */
    public record Kind(String name, String covers, Function<Program, List<String>> lines) {}

    public static final List<Kind> KINDS = List.of(
            new Kind("profile", "the profile the program means what it means under", p -> List.of(p.header().profileHash())),
            new Kind("item", "each item's place and attribute tuple", p -> each(p.items(), Contract::item)),
            new Kind("redefines", "each item that redefines another", p -> each(p.items().stream().filter(i -> i.redefines() != 0).toList(), i -> i.id() + " " + i.redefines())),
            new Kind("occurs", "each table's bounds, ODO object, keys and indexes", p -> each(p.items().stream().filter(i -> i.occurs() != null).toList(), Contract::occurs)),
            new Kind("value", "each item's VALUE", p -> each(p.items().stream().filter(i -> i.value() != null).toList(), i -> i.id() + " " + value(i.value()))),
            new Kind("condition", "each 88 and its values", p -> p.items().stream().flatMap(i -> i.conditions().stream().map(c -> i.id() + " " + c.name() + " " + c.values().stream().map(Contract::value).collect(Collectors.joining(" | ")))).toList()),
            new Kind("file", "each file's organization, access, keys, status and records", p -> each(p.files(), Contract::file)),
            new Kind("paragraph", "each paragraph's place in textual order and its section", p -> each(p.procedure().paragraphs(), g -> g.index() + " " + quoted(g.name()) + " " + quoted(g.section()))),
            new Kind("falls", "control that falls from a paragraph into the next", Contract::falls),
            new Kind("edge", "each PERFORM, GO TO and ALTER between paragraphs", p -> each(p.procedure().edges(), e -> e.kind() + " " + e.from() + " " + e.to() + " " + e.thru())),
            new Kind("statement", "each statement's verb, operands with their roles, and branches", Contract::statements),
            new Kind("call", "each CALL's target, arguments with their modes, and RETURNING", p -> each(p.calls(), Contract::call)),
            new Kind("using", "the program's own USING and RETURNING items", p -> List.of(p.procedure().using() + " " + p.procedure().returning())));

    private Contract() {}

    /** The program's contract, then each nested program's, kinds in table order. */
    public static List<String> lines(Program program) {
        List<String> lines = new ArrayList<>();
        for (Kind kind : KINDS) for (String line : kind.lines().apply(program)) lines.add(kind.name() + " " + program.header().program() + " " + line);
        for (Program nested : program.nested()) lines.addAll(lines(nested));
        return lines;
    }

    /** Each fact the expected contract has that the actual lacks (`missing`), and each the actual adds (`extra`). */
    public static List<String> diff(List<String> expected, List<String> actual) {
        Map<String, Integer> left = new HashMap<>();
        actual.forEach(l -> left.merge(l, 1, Integer::sum));
        List<String> out = new ArrayList<>();
        for (String line : expected) {
            if (left.getOrDefault(line, 0) > 0) left.merge(line, -1, Integer::sum);
            else out.add("missing: " + line);
        }
        for (String line : actual) {
            if (left.getOrDefault(line, 0) > 0) {
                out.add("extra: " + line);
                left.merge(line, -1, Integer::sum);
            }
        }
        return out;
    }

    private static <T> List<String> each(List<T> things, Function<T, String> line) {
        return things.stream().map(line).toList();
    }

    private static String item(Item i) {
        Attributes a = i.attributes();
        return i.id() + " " + i.name() + " level " + i.level() + " parent " + i.parent() + " record " + i.record() + " " + i.section() + " offset " + i.offset() + " length "
                + i.length() + " " + a.usage() + " " + a.category() + " digits " + a.digits() + " scale " + a.scale() + (a.signed() ? " signed" : "")
                + (a.signSeparate() ? " separate" : "") + (a.leading() ? " leading" : "") + (i.syncSlack() > 0 ? " slack " + i.syncSlack() : "") + " picture " + quoted(a.picture());
    }

    private static String occurs(Item i) {
        Item.Occurs o = i.occurs();
        return i.id() + " " + o.min() + ".." + o.max() + " depending " + o.dependingOn() + " keys "
                + o.keys().stream().map(k -> k.item() + (k.ascending() ? "A" : "D")).toList() + " indexed " + o.indexedBy();
    }

    private static String file(FileDescriptor f) {
        return f.name() + " assign " + quoted(f.assign()) + " " + f.organization() + " " + f.access() + " key " + f.recordKey() + " alternate "
                + f.alternateKeys().stream().map(k -> k.item() + (k.duplicates() ? "D" : "")).toList() + " status " + f.status() + " records " + f.records()
                + " " + f.minRecord() + ".." + f.maxRecord();
    }

    /** A paragraph falls into the next unless its last statement leaves (GO TO, STOP, GOBACK, EXIT PROGRAM). */
    private static List<String> falls(Program p) {
        List<Procedure.Paragraph> all = p.procedure().paragraphs();
        List<String> lines = new ArrayList<>();
        for (int i = 0; i + 1 < all.size(); i++) {
            List<Statement> s = all.get(i).statements();
            String last = s.isEmpty() ? "" : s.get(s.size() - 1).verb();
            if (!List.of("GO TO", "STOP", "GOBACK", "EXIT PROGRAM").contains(last)) lines.add(i + " " + (i + 1));
        }
        return lines;
    }

    /** Each statement, numbered within its paragraph as `paragraph.n`, its branches' statements after it as `paragraph.n.label.m`. */
    private static List<String> statements(Program p) {
        List<String> lines = new ArrayList<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) {
            for (int n = 0; n < g.statements().size(); n++) statement(g.index() + "." + n, g.statements().get(n), lines);
        }
        return lines;
    }

    private static void statement(String at, Statement s, List<String> lines) {
        lines.add(at + " " + s.verb() + s.operands().stream().map(o -> " " + o.role() + "=" + value(o.value())).collect(Collectors.joining())
                + s.branches().stream().map(b -> " | " + b.label() + (b.condition() == null ? "" : "=" + value(b.condition()))).collect(Collectors.joining()));
        for (Statement.Branch b : s.branches()) {
            for (int m = 0; m < b.statements().size(); m++) statement(at + "." + b.label() + "." + m, b.statements().get(m), lines);
        }
    }

    private static String call(Call c) {
        return value(c.target()) + " using " + c.using().stream().map(a -> a.mode() + ":" + value(a.value())).toList() + " returning " + value(c.returning());
    }

    /**
     * A value in one token: #item, #item(subscripts)[start:length], @paragraph..thru, file:NAME, a literal
     * as written (with `{kind payload scale}` after it when it is neither ALPHANUMERIC nor NUMERIC), (operator operands).
     */
    static String value(Value v) {
        return switch (v) {
            case null -> "-";
            case Value.Field f -> "#" + f.item() + (f.subscripts().isEmpty() ? "" : f.subscripts().stream().map(Contract::value).collect(Collectors.joining(",", "(", ")")))
                    + (f.start() == null ? "" : "[" + value(f.start()) + ":" + value(f.length()) + "]");
            case Value.File f -> "file:" + f.name();
            case Value.Target t -> "@" + t.paragraph() + ".." + t.thru();
            case Value.Name n -> n.kind().replace(' ', '-') + ":" + n.text();
            case Value.Literal l -> literal(l);
            case Value.Figurative f -> f.name() + (f.all() == null ? "" : " " + literal(f.all()));
            case Value.ConditionName c -> "#" + c.item() + "." + c.name();
            case Value.Expression e -> "(" + e.operator() + e.operands().stream().map(o -> " " + value(o)).collect(Collectors.joining()) + ")";
        };
    }

    private static String literal(Value.Literal l) {
        if (l.kind() == Value.Literal.Kind.ALPHANUMERIC || l.kind() == Value.Literal.Kind.NUMERIC) return l.text();
        return l.text() + "{" + l.kind().name().toLowerCase(java.util.Locale.ROOT) + " " + l.payload() + (l.scale() == 0 ? "" : " " + l.scale()) + "}";
    }

    private static String quoted(String s) {
        return "\"" + s + "\"";
    }
}
