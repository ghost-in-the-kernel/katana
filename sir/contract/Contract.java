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
            new Kind("editing", "the currency sign and decimal point a mask's `$` and `.` stand for", p -> List.of("currency " + p.header().currency() + " point " + (p.header().decimalComma() ? "," : "."))),
            new Kind("debugging", "WITH DEBUGGING MODE, where written", p -> p.header().debuggingMode() ? List.of("mode") : List.of()),
            new Kind("attribute", "the PROGRAM-ID's INITIAL and COMMON, where written", p -> {
                List<String> lines = new ArrayList<>();
                if (p.header().initial()) lines.add("INITIAL");
                if (p.header().common()) lines.add("COMMON");
                return lines;
            }),
            new Kind("switch", "each switch-status condition-name, its switch and status", p -> each(p.header().switches(), s -> s.condition() + " " + s.environment() + " " + (s.on() ? "ON" : "OFF"))),
            new Kind("mnemonic", "each mnemonic-name with its environment-name", p -> each(p.header().mnemonics(), m -> m.name() + " " + m.environment())),
            new Kind("alphabet", "each alphabet-name, its kind and members, and the program collating sequence", p -> {
                List<String> lines = new ArrayList<>(each(p.header().alphabets(), a -> a.name() + " " + a.kind() + (a.members().isEmpty() ? "" : " " + a.members().stream().map(Contract::value).collect(Collectors.joining(" | ")))));
                if (!p.header().collating().isEmpty()) lines.add("collating " + p.header().collating());
                return lines;
            }),
            new Kind("class", "each CLASS name and its members", p -> each(p.header().classes(), c -> c.name() + " " + c.members().stream().map(Contract::value).collect(Collectors.joining(" | ")))),
            new Kind("item", "each item's place, attribute tuple, edit mask and flags", p -> each(p.items(), Contract::item)),
            new Kind("redefines", "each item that redefines another", p -> each(p.items().stream().filter(i -> i.redefines() != 0).toList(), i -> i.id() + " " + i.redefines())),
            new Kind("occurs", "each table's bounds, ODO object, keys and indexes", p -> each(p.items().stream().filter(i -> i.occurs() != null).toList(), Contract::occurs)),
            new Kind("value", "each item's VALUE", p -> each(p.items().stream().filter(i -> i.value() != null).toList(), i -> i.id() + " " + value(i.value()))),
            new Kind("condition", "each 88 and its values", p -> p.items().stream().flatMap(i -> i.conditions().stream().map(c -> i.id() + " " + c.name() + " " + c.values().stream().map(Contract::value).collect(Collectors.joining(" | ")))).toList()),
            new Kind("file", "each file's organization, access, keys, status and records", p -> each(p.files(), Contract::file)),
            new Kind("fd", "each file's FD or SD entry: EXTERNAL, GLOBAL, the RECORD clause, LINAGE, CODE-SET, RECORDING MODE", p -> each(p.files().stream().filter(f -> f.fd() != null).toList(), Contract::fd)),
            new Kind("declarative", "each declarative section's USE: what it follows, GLOBAL, and its files or mode, or what it debugs", p -> each(p.procedure().sections().stream().filter(Procedure.Section::declarative).toList(), Contract::declarative)),
            new Kind("segment", "each section's priority-number, where one is written", p -> each(p.procedure().sections().stream().filter(s -> s.priority() >= 0).toList(), s -> quoted(s.name()) + " " + s.priority())),
            new Kind("paragraph", "each paragraph's place in textual order and its section", p -> each(p.procedure().paragraphs(), g -> g.index() + " " + quoted(g.name()) + " " + quoted(g.section()))),
            new Kind("falls", "control that falls from a paragraph into the next, `returns-if-called` after EXIT PROGRAM", Contract::falls),
            new Kind("edge", "each PERFORM, GO TO and ALTER between paragraphs", p -> each(p.procedure().edges(), e -> e.kind() + " " + e.from() + " " + e.to() + " " + e.thru())),
            new Kind("statement", "each statement's verb, operands with their roles, and branches", Contract::statements),
            new Kind("call", "each CALL's target, arguments with their modes, and RETURNING", p -> each(p.calls(), Contract::call)),
            new Kind("using", "the program's own USING and RETURNING items", p -> List.of(p.procedure().using() + " " + p.procedure().returning())),
            new Kind("global", "each GLOBAL record of a containing program it sees: its item here, how many programs out, its item there", p -> each(p.globals(), g -> g.record() + " " + g.depth() + " " + g.outer())));

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
                + (a.signSeparate() ? " separate" : "") + (a.leading() ? " leading" : "") + (i.syncSlack() > 0 ? " slack " + i.syncSlack() : "") + " picture " + quoted(a.picture())
                + (a.mask().isEmpty() ? "" : " mask " + quoted(a.mask())) + i.flags().stream().map(f -> " " + f.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-')).collect(Collectors.joining());
    }

    private static String occurs(Item i) {
        Item.Occurs o = i.occurs();
        return i.id() + " " + o.min() + ".." + o.max() + " depending " + o.dependingOn() + " keys "
                + o.keys().stream().map(k -> k.item() + (k.ascending() ? "A" : "D")).toList() + " indexed " + o.indexedBy();
    }

    /** An FD's contract line (SIR-11): FD or SD, EXTERNAL, GLOBAL, the RECORD clause, LINAGE's four values, CODE-SET and RECORDING MODE, each only as written. */
    private static String fd(FileDescriptor f) {
        FileDescriptor.Fd d = f.fd();
        StringBuilder line = new StringBuilder(f.name() + (d.sort() ? " SD" : " FD"));
        if (d.external()) line.append(" EXTERNAL");
        if (d.global()) line.append(" GLOBAL");
        if (d.recordFrom() != 0 || d.recordTo() != 0 || d.varying()) line.append(" record ").append(d.recordFrom()).append("..").append(d.recordTo()).append(d.varying() ? " varying" : "");
        if (d.recordDepending() != 0) line.append(" depending #").append(d.recordDepending());
        FileDescriptor.Linage l = d.linage();
        if (l != null) line.append(" linage ").append(value(l.body())).append(" footing ").append(l.footing() == null ? "-" : value(l.footing()))
                .append(" top ").append(l.top() == null ? "-" : value(l.top())).append(" bottom ").append(l.bottom() == null ? "-" : value(l.bottom()));
        if (!d.codeSet().isEmpty()) line.append(" code-set ").append(d.codeSet());
        if (!d.recordingMode().isEmpty()) line.append(" mode ").append(d.recordingMode());
        return line.toString();
    }

    private static String declarative(Procedure.Section s) {
        Procedure.Use u = s.use();
        String scope = u.kind() == Procedure.Use.Kind.DEBUGGING ? "on " + u.debugging() : !u.mode().isEmpty() ? "mode " + u.mode() : "files " + u.files() + (u.outerFiles().isEmpty() ? "" : " outer " + u.outerFiles());
        return s.name() + " " + u.kind() + (u.global() ? " GLOBAL " : " ") + scope;
    }

    private static String file(FileDescriptor f) {
        return f.name() + " assign " + quoted(f.assign()) + (f.optional() ? " OPTIONAL" : "") + " " + f.organization() + " " + f.access() + " key " + f.recordKey() + " relative " + f.relativeKey() + " alternate "
                + f.alternateKeys().stream().map(k -> k.item() + (k.duplicates() ? "D" : "")).toList() + " status " + f.status() + (f.vsamStatus() == 0 ? "" : " vsam-status " + f.vsamStatus()) + " records " + f.records()
                + " " + f.minRecord() + ".." + f.maxRecord() + (f.sharesRecordArea().isEmpty() ? "" : " shares-record-area " + f.sharesRecordArea());
    }

    /** A paragraph falls into the next unless its last statement leaves (GO TO, STOP RUN, GOBACK); an altered GO TO, naming no procedure, falls until an ALTER sends it (LR 6.4 p. 347), and STOP literal resumes at the next statement (p. 457). */
    private static boolean stopLiteral(Statement s) {
        return s.verb().equals("STOP") && s.operands().stream().noneMatch(o -> o.value() instanceof Value.Name n && n.text().equals("RUN"));
    }

    /** EXIT PROGRAM, which the IR keeps as EXIT with PROGRAM its operand (R-017). */
    private static boolean exitProgram(Statement s) {
        return s.verb().equals("EXIT") && s.operands().stream().anyMatch(o -> o.value() instanceof Value.Name n && n.text().equals("PROGRAM"));
    }

    /**
     * Each paragraph that falls through into the next, as `i i+1`: its last statement no GO TO, STOP RUN or
     * GOBACK (an altered GO TO and STOP literal fall through). One ending in EXIT PROGRAM is a conditional
     * edge, `i i+1 returns-if-called`: IBM's reading, a return where the program was CALLed and a fall into
     * the next paragraph where it is the run unit's main program, as the lowering does (J-199, J-200).
     */
    private static List<String> falls(Program p) {
        List<Procedure.Paragraph> all = p.procedure().paragraphs();
        List<String> lines = new ArrayList<>();
        for (int i = 0; i + 1 < all.size(); i++) {
            List<Statement> s = all.get(i).statements();
            Statement end = s.isEmpty() ? null : s.get(s.size() - 1);
            String last = end == null || (end.verb().equals("GO TO") && end.operands().isEmpty()) || stopLiteral(end) ? "" : end.verb();
            if (end != null && exitProgram(end)) lines.add(i + " " + (i + 1) + " returns-if-called");
            else if (!List.of("GO TO", "STOP", "GOBACK").contains(last)) lines.add(i + " " + (i + 1));
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
            case Value.Target t -> "@" + t.paragraph() + ".." + t.thru() + (t.section() ? " section" : "");
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
