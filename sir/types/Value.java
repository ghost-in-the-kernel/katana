package katana.sir.types;

import java.util.List;

/** What a statement operates on: a field, a file, a paragraph, a name, a literal, a figurative constant, a condition name, or an expression of these. */
public sealed interface Value {
    /** An item, subscripted and reference-modified as written (an empty list, and nulls, when not). */
    record Field(int item, List<Value> subscripts, Value start, Value length) implements Value {
        public Field {
            subscripts = List.copyOf(subscripts);
        }
    }

    /** A paragraph a PERFORM or GO TO names, through `thru` (the same paragraph when none; a section is its paragraphs). */
    record Target(int paragraph, int thru) implements Value {}

    /** A word that is not data: a mnemonic or environment name, a special register, or a name that resolved to nothing (`kind` says which). */
    record Name(String kind, String text) implements Value {}

    /** A file, by its name (OPEN, CLOSE, READ, START, DELETE). */
    record File(String name) implements Value {}

    /**
     * A literal: as written (quotes kept), its kind, and what it means (SIR-08). The payload is the
     * characters for ALPHANUMERIC, NULL_TERMINATED (without its X'00'), NATIONAL and DBCS, quotes undone;
     * the bytes as hex digits for HEX; the unscaled value, signed, for NUMERIC and FLOAT, worth
     * `payload × 10^-scale`; the word for BOOLEAN; the reference as written for CICS (DFHRESP, DFHVALUE).
     */
    record Literal(String text, Kind kind, String payload, int scale) implements Value {
        public enum Kind { ALPHANUMERIC, HEX, NULL_TERMINATED, NATIONAL, DBCS, NUMERIC, FLOAT, BOOLEAN, CICS }

        /** SIR-08: the payload is its kind's: hex digits in pairs, a signed integer, and a scale only for a number. */
        public Literal {
            boolean number = kind == Kind.NUMERIC || kind == Kind.FLOAT;
            if (kind == Kind.HEX && !payload.matches("([0-9A-F]{2})*")) throw new Invalid("SIR-08", "hex literal " + text + " decodes to " + payload);
            if (number && !payload.matches("-?(0|[1-9][0-9]*)")) throw new Invalid("SIR-08", "numeric literal " + text + " decodes to " + payload);
            if (!number && scale != 0) throw new Invalid("SIR-08", "the " + kind + " literal " + text + " has a scale");
        }

        /** Whether it is a number. */
        public boolean numeric() {
            return kind == Kind.NUMERIC || kind == Kind.FLOAT;
        }
    }

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
