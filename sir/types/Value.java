package katana.sir.types;

import java.util.List;

/** What a statement operates on: a field, a file, a literal, a figurative constant, a condition name, or an expression of these. */
public sealed interface Value {
    /** An item, subscripted and reference-modified as written (an empty list, and nulls, when not). */
    record Field(int item, List<Value> subscripts, Value start, Value length) implements Value {
        public Field {
            subscripts = List.copyOf(subscripts);
        }
    }

    /** A file, by its name (OPEN, CLOSE, READ, START, DELETE). */
    record File(String name) implements Value {}

    /** A literal as written, quotes kept for a nonnumeric one. */
    record Literal(String text, boolean numeric) implements Value {}

    /** ZERO, SPACE, HIGH-VALUE, LOW-VALUE, QUOTE, NULL, or ALL with its literal. */
    record Figurative(String name, Literal all) implements Value {}

    /** An 88 of an item, by the item and the condition's name. */
    record ConditionName(int item, String name, List<Value> subscripts) implements Value {
        public ConditionName {
            subscripts = List.copyOf(subscripts);
        }
    }

    /** An operator and its operands: arithmetic (`+`, `**`), relational (`=`, `<`), logical (`AND`, `NOT`), class (`NUMERIC`), sign (`POSITIVE`), or an intrinsic (`FUNCTION TRIM`). */
    record Expression(String operator, List<Value> operands) implements Value {
        public Expression {
            operands = List.copyOf(operands);
        }
    }
}
