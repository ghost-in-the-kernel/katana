package katana.sir.types;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A program's Semantic IR (R-007): what it does, derived from the parse tree and the profile with no
 * judgment anywhere. Its header, its data map (every item, in textual order), its files, its paragraph
 * graph and statements, its CALLs, and the programs nested in it.
 */
public record Program(Header header, List<Item> items, List<FileDescriptor> files, Procedure procedure, List<Call> calls, List<Program> nested) {
    public Program {
        items = List.copyOf(items);
        files = List.copyOf(files);
        calls = List.copyOf(calls);
        nested = List.copyOf(nested);
        dataMap(items);
        references(items, files, procedure, calls);
    }

    /**
     * SIR-02: items are numbered 1, 2, ... in textual order, a parent before its children, and a record
     * (level 01 or 77) is its own record. SIR-03: an item lies inside its parent and its record.
     */
    private static void dataMap(List<Item> items) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.id() != i + 1) throw new Invalid("SIR-02", item.name() + " is numbered " + item.id() + ", not " + (i + 1));
            if (item.parent() >= item.id() || (item.parent() == 0) != (item.record() == item.id())) throw new Invalid("SIR-02", item.name() + "'s parent or record is wrong");
            if (item.record() < 1 || item.record() > item.id()) throw new Invalid("SIR-02", item.name() + "'s record " + item.record() + " does not precede it");
            Item record = items.get(item.record() - 1);
            if (item.offset() + item.length() > record.length() && item.redefines() == 0) throw new Invalid("SIR-03", item.name() + " ends at " + (item.offset() + item.length()) + ", past its record's " + record.length());
            if (item.parent() > 0) {
                Item parent = items.get(item.parent() - 1);
                int extent = parent.occurs() == null ? parent.length() : parent.length() * parent.occurs().max();
                if (item.offset() < parent.offset() || item.offset() + item.length() > parent.offset() + extent) throw new Invalid("SIR-03", item.name() + " lies outside its parent " + parent.name());
            }
        }
    }

    /** SIR-05: every item a value, a file, a key, an ODO object or the procedure names is in the data map, and every file a statement names is a file of the program. */
    private static void references(List<Item> items, List<FileDescriptor> files, Procedure procedure, List<Call> calls) {
        Consumer<Integer> item = id -> {
            if (id < 1 || id > items.size()) throw new Invalid("SIR-05", "item " + id + " is not in the data map");
        };
        Consumer<Integer> optional = id -> {
            if (id != 0) item.accept(id);
        };
        for (Item i : items) {
            optional.accept(i.redefines());
            if (i.occurs() != null) {
                optional.accept(i.occurs().dependingOn());
                i.occurs().keys().forEach(k -> item.accept(k.item()));
                i.occurs().indexedBy().forEach(item);
            }
        }
        for (FileDescriptor f : files) {
            optional.accept(f.recordKey());
            optional.accept(f.relativeKey());
            optional.accept(f.status());
            optional.accept(f.vsamStatus());
            f.alternateKeys().forEach(k -> item.accept(k.item()));
            f.records().forEach(item);
        }
        procedure.using().forEach(item);
        optional.accept(procedure.returning());
        Set<String> names = new HashSet<>();
        files.forEach(f -> names.add(f.name()));
        Consumer<Integer> any = item;
        for (Procedure.Paragraph p : procedure.paragraphs()) p.statements().forEach(s -> statement(s, any, names));
        for (Procedure.Section s : procedure.sections()) {
            if (s.use() == null) continue;
            for (String f : s.use().files()) if (!names.contains(f)) throw new Invalid("SIR-05", "declarative " + s.name() + " is USE'd for file " + f + ", not a file of the program");
        }
        for (Call c : calls) {
            value(c.target(), item);
            c.using().forEach(a -> value(a.value(), item));
            value(c.returning(), item);
        }
    }

    private static void statement(Statement s, Consumer<Integer> item, Set<String> files) {
        for (Statement.Operand o : s.operands()) {
            if (o.value() instanceof Value.File f && !files.contains(f.name())) throw new Invalid("SIR-05", s.verb() + " names file " + f.name() + ", not a file of the program");
            value(o.value(), item);
        }
        for (Statement.Branch b : s.branches()) {
            value(b.condition(), item);
            b.statements().forEach(t -> statement(t, item, files));
        }
    }

    /** Every item a value names, through its subscripts, reference modification and operands. */
    private static void value(Value v, Consumer<Integer> item) {
        switch (v) {
            case null -> { }
            case Value.Field f -> {
                item.accept(f.item());
                f.subscripts().forEach(s -> value(s, item));
                value(f.start(), item);
                value(f.length(), item);
            }
            case Value.ConditionName c -> {
                item.accept(c.item());
                c.subscripts().forEach(s -> value(s, item));
            }
            case Value.Expression e -> e.operands().forEach(o -> value(o, item));
            case Value.File f -> { }
            case Value.Target t -> { }
            case Value.Name n -> { }
            case Value.Literal l -> { }
            case Value.Figurative f -> { }
        }
    }
}
