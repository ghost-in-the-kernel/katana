package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import katana.sir.types.Origin;
import katana.sir.types.Attributes;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Header;
import katana.sir.types.Invalid;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/** 2.4's check: COPYREC (oracle/fixture/trivial) written as IR by hand constructs, and each construction-checked invariant refuses a violation. */
class SirTypesTest {
    private static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";

    private static Origin at(String file, int line, int column) {
        return new Origin(file, line, column, Origin.Kind.SOURCE);
    }

    private static Attributes text(int length) {
        return new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.ALPHANUMERIC, 0, 0, false, false, false, "X".repeat(length), "");
    }

    private static Attributes number(int digits, int scale, String picture) {
        return new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.NUMERIC, digits, scale, false, false, false, picture, "");
    }

    private static final Attributes GROUP = new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.GROUP, 0, 0, false, false, false, "", "");

    private static Item item(int id, String name, int level, int parent, int record, Item.Section section, int offset, int length, Attributes a, Value value, Origin origin) {
        return new Item(id, name, level, parent, record, section, offset, length, a, 0, null, 0, value, List.of(), List.of(), origin);
    }

    /** COPYREC's data map: COPY-REC from the copybook, OUT-REC, EOF and COPIED. */
    static List<Item> items() {
        String cpy = "oracle/fixture/trivial/cpy/COPYREC.cpy", cbl = "oracle/fixture/trivial/cbl/COPYREC.cbl";
        List<Item> items = new ArrayList<>();
        items.add(item(1, "COPY-REC", 1, 0, 1, Item.Section.FILE, 0, 20, GROUP, null, at(cpy, 2, 8)));
        items.add(item(2, "ITEM", 5, 1, 1, Item.Section.FILE, 0, 10, text(10), null, at(cpy, 3, 12)));
        items.add(item(3, "AMOUNT", 5, 1, 1, Item.Section.FILE, 10, 7, number(7, 2, "99999V99"), null, at(cpy, 4, 12)));
        items.add(item(4, "FILLER", 5, 1, 1, Item.Section.FILE, 17, 3, text(3), null, at(cpy, 5, 12)));
        items.add(item(5, "OUT-REC", 1, 0, 5, Item.Section.FILE, 0, 20, text(20), null, at(cbl, 17, 8)));
        items.add(item(6, "EOF", 1, 0, 6, Item.Section.WORKING_STORAGE, 0, 1, text(1), new Value.Literal("'N'", Value.Literal.Kind.ALPHANUMERIC, "N", 0), at(cbl, 19, 8)));
        items.add(item(7, "COPIED", 1, 0, 7, Item.Section.WORKING_STORAGE, 0, 5, number(5, 0, "99999"), new Value.Literal("0", Value.Literal.Kind.NUMERIC, "0", 0), at(cbl, 20, 8)));
        return items;
    }

    private static Value field(int item) {
        return new Value.Field(item, List.of(), null, null);
    }

    private static Statement statement(String verb, int line, List<Statement.Operand> operands, List<Statement.Branch> branches) {
        return new Statement(verb, operands, branches, at("oracle/fixture/trivial/cbl/COPYREC.cbl", line, 12));
    }

    private static Statement.Operand operand(String role, Value value) {
        return new Statement.Operand(role, value);
    }

    /** COPYREC's one unnamed paragraph: OPEN, the PERFORM UNTIL with its READ, CLOSE, DISPLAY, STOP RUN. */
    static Procedure procedure(List<Statement> statements) {
        String cbl = "oracle/fixture/trivial/cbl/COPYREC.cbl";
        return new Procedure(List.of(), 0, List.of(), List.of(new Procedure.Paragraph(0, "", "", statements, List.of(0), at(cbl, 22, 12))), List.of());
    }

    static List<Statement> statements() {
        Statement read = statement("READ", 24, List.of(operand("file", new Value.File("IN-FILE"))), List.of(
                new Statement.Branch("at-end", null, List.of(statement("MOVE", 25, List.of(operand("from", new Value.Literal("'Y'", Value.Literal.Kind.ALPHANUMERIC, "Y", 0)), operand("to", field(6))), List.of()))),
                new Statement.Branch("not-at-end", null, List.of(
                        statement("WRITE", 27, List.of(operand("record", field(5)), operand("from", field(1))), List.of()),
                        statement("ADD", 28, List.of(operand("addend", new Value.Literal("1", Value.Literal.Kind.NUMERIC, "1", 0)), operand("to", field(7))), List.of())))));
        Value until = new Value.Expression("=", List.of(field(6), new Value.Literal("'Y'", Value.Literal.Kind.ALPHANUMERIC, "Y", 0)));
        return List.of(
                statement("OPEN", 22, List.of(operand("input", new Value.File("IN-FILE")), operand("output", new Value.File("OUT-FILE"))), List.of()),
                statement("PERFORM", 23, List.of(), List.of(new Statement.Branch("body", until, List.of(read)))),
                statement("CLOSE", 31, List.of(operand("file", new Value.File("IN-FILE")), operand("file", new Value.File("OUT-FILE"))), List.of()),
                statement("DISPLAY", 32, List.of(operand("item", new Value.Literal("'COPIED '", Value.Literal.Kind.ALPHANUMERIC, "COPIED ", 0)), operand("item", field(7)), operand("item", new Value.Literal("' RECORDS'", Value.Literal.Kind.ALPHANUMERIC, " RECORDS", 0))), List.of()),
                statement("STOP RUN", 33, List.of(), List.of()));
    }

    static List<FileDescriptor> files() {
        String cbl = "oracle/fixture/trivial/cbl/COPYREC.cbl";
        return List.of(
                new FileDescriptor("IN-FILE", "INFILE", false, FileDescriptor.Organization.SEQUENTIAL, FileDescriptor.Access.SEQUENTIAL, 0, List.of(), 0, 0, 0, null, List.of(1), 20, 20, at(cbl, 8, 12)),
                new FileDescriptor("OUT-FILE", "OUTFILE", false, FileDescriptor.Organization.SEQUENTIAL, FileDescriptor.Access.SEQUENTIAL, 0, List.of(), 0, 0, 0, null, List.of(5), 20, 20, at(cbl, 10, 12)));
    }

    static Header header() {
        return new Header("COPYREC", "oracle/fixture/trivial/cbl/COPYREC.cbl", HASH, "$", false, List.of(), List.of(), List.of(), List.of(), "", at("oracle/fixture/trivial/cbl/COPYREC.cbl", 4, 8));
    }

    @Test
    void copyrecWrittenByHandIsAProgram() {
        Program p = new Program(header(), items(), files(), procedure(statements()), List.of(), List.of());
        assertEquals(7, p.items().size());
        assertEquals("AMOUNT", p.items().get(2).name());
    }

    private static void refused(String invariant, Supplier<Object> build) {
        Invalid e = assertThrows(Invalid.class, build::get);
        assertTrue(e.getMessage().startsWith(invariant + ": "), e.getMessage());
    }

    @Test
    void eachInvariantRefusesAViolation() {
        refused("SIR-01", () -> new Header("COPYREC", "x", "sha256:nope", "$", false, List.of(), List.of(), List.of(), List.of(), "", at("x", 1, 1)));
        List<Item> renumbered = new ArrayList<>(items());
        renumbered.set(3, item(9, "FILLER", 5, 1, 1, Item.Section.FILE, 17, 3, text(3), null, at("x", 1, 1)));
        refused("SIR-02", () -> new Program(header(), renumbered, files(), procedure(statements()), List.of(), List.of()));
        List<Item> outside = new ArrayList<>(items());
        outside.set(3, item(4, "FILLER", 5, 1, 1, Item.Section.FILE, 17, 4, text(4), null, at("x", 1, 1)));
        refused("SIR-03", () -> new Program(header(), outside, files(), procedure(statements()), List.of(), List.of()));
        refused("SIR-04", () -> number(0, 0, "9"));
        refused("SIR-04", () -> new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.ALPHANUMERIC, 0, 0, true, false, false, "X", ""));
        refused("SIR-04", () -> new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.NUMERIC_EDITED, 3, 0, false, false, false, "ZZ9", ""));
        refused("SIR-04", () -> new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.NUMERIC_EDITED, 3, 2, false, false, false, "Z9V99", "Z9V99"));
        refused("SIR-04", () -> new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.ALPHANUMERIC, 0, 0, false, false, false, "X", "X"));
        List<Statement> stray = List.of(statement("MOVE", 1, List.of(operand("to", field(8))), List.of()));
        refused("SIR-05", () -> new Program(header(), items(), files(), procedure(stray), List.of(), List.of()));
        List<Statement> noFile = List.of(statement("CLOSE", 1, List.of(operand("file", new Value.File("NOPE"))), List.of()));
        refused("SIR-05", () -> new Program(header(), items(), files(), procedure(noFile), List.of(), List.of()));
        Procedure plain = procedure(statements());
        Procedure.Section useNope = new Procedure.Section("ERRS", true, new Procedure.Use(Procedure.Use.Kind.ERROR, false, "", List.of("NOPE"), List.of(), List.of()), -1, at("x", 1, 1));
        refused("SIR-05", () -> new Program(header(), items(), files(), new Procedure(plain.using(), plain.returning(), List.of(useNope), plain.paragraphs(), plain.edges()), List.of(), List.of()));
        refused("SIR-10", () -> new Procedure.Section("ERRS", true, null, -1, at("x", 1, 1)));
        Value five = new Value.Literal("5", Value.Literal.Kind.NUMERIC, "5", 0), zero = new Value.Literal("0", Value.Literal.Kind.NUMERIC, "0", 0);
        refused("SIR-11", () -> new FileDescriptor.Linage(zero, null, null, null));
        refused("SIR-11", () -> new FileDescriptor.Linage(five, new Value.Literal("6", Value.Literal.Kind.NUMERIC, "6", 0), null, null));
        refused("SIR-11", () -> new FileDescriptor.Linage(five, null, new Value.Literal("-1", Value.Literal.Kind.NUMERIC, "-1", 0), null));
        refused("SIR-11", () -> new FileDescriptor.Fd(false, false, false, 30, 10, false, 0, null, "", ""));
        refused("SIR-10", () -> new Procedure.Section("MAIN", false, useNope.use(), -1, at("x", 1, 1)));
        refused("SIR-10", () -> new Procedure.Use(Procedure.Use.Kind.ERROR, false, "", List.of(), List.of(), List.of()));
        refused("SIR-10", () -> new Procedure.Use(Procedure.Use.Kind.ERROR, false, "INPUT", List.of("IN-FILE"), List.of(), List.of()));
        refused("SIR-06", () -> new Procedure(List.of(), 0, List.of(), List.of(new Procedure.Paragraph(1, "P", "", List.of(), List.of(), at("x", 1, 1))), List.of()));
        refused("SIR-06", () -> new Procedure(List.of(), 0, List.of(), List.of(new Procedure.Paragraph(0, "P", "", List.of(), List.of(), at("x", 1, 1))),
                List.of(new Procedure.Edge(Procedure.Kind.PERFORM, 0, 3, 3, at("x", 1, 1)))));
        refused("SIR-06", () -> new Procedure.Paragraph(0, "P", "", List.of(), List.of(0), at("x", 1, 1)));
        refused("SIR-07", () -> new Statement("MOVE", List.of(), List.of(), null));
        refused("SIR-08", () -> new Value.Literal("X'C'", Value.Literal.Kind.HEX, "C", 0));
        refused("SIR-08", () -> new Value.Literal("1.5", Value.Literal.Kind.NUMERIC, "1.5", 0));
        refused("SIR-08", () -> new Value.Literal("'A'", Value.Literal.Kind.ALPHANUMERIC, "A", 1));
    }
}
