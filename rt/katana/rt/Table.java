package katana.rt;

import java.util.function.Consumer;

/** A table's occurrences: an item repeated by the OCCURS of it and of its groups, each dimension a count and a stride in bytes. */
public final class Table {
    private Table() {}

    /** Each occurrence of `first` (the field at its first occurrence), outermost dimension first. */
    public static void each(Field first, int[] counts, int[] strides, Consumer<Field> action) {
        walk(first, counts, strides, 0, 0, action);
    }

    private static void walk(Field first, int[] counts, int[] strides, int dim, int delta, Consumer<Field> action) {
        if (dim == counts.length) {
            action.accept(new Field(first.record(), first.offset() + delta, first.attr()));
            return;
        }
        for (int k = 0; k < counts[dim]; k++) walk(first, counts, strides, dim + 1, delta + k * strides[dim], action);
    }

    /** A subscripted reference: `first` moved by each subscript (from 1) times its dimension's stride. */
    public static Field at(Field first, int[] strides, long... subscripts) {
        int delta = 0;
        for (int k = 0; k < subscripts.length; k++) delta += (int) (subscripts[k] - 1) * strides[k];
        return new Field(first.record(), first.offset() + delta, first.attr());
    }

    /** Reference modification `(start:length)` from 1; `length` 0 for the rest of the field. */
    public static Field ref(Field f, long start, long length) {
        int n = length == 0 ? f.attr().length() - (int) start + 1 : (int) length;
        return new Field(f.record(), f.offset() + (int) start - 1, Attr.text(n, false));
    }

    /** A field's value as a subscript or a count. */
    public static long index(Field f) {
        return f.get().toBigDecimal().longValue();
    }

    /** An index or a count set to a whole number (SET, SEARCH's stepping), as the MOVE of that integer. */
    // Decision: R-019
    public static void set(Field f, long n) {
        Move.move(Literal.number(Long.toString(n), 0, f.record().codepage()), f);
    }

    /** A group holding an OCCURS DEPENDING ON table, at its current length: its first `length` bytes (LR 6.4 p. 205). */
    // Decision: R-019
    public static Field current(Field group, long length) {
        return new Field(group.record(), group.offset(), Attr.text((int) length, false));
    }

    /** An item after an OCCURS DEPENDING ON table, moved by how far the table's current length falls short of its maximum (R-088). */
    // Decision: R-088
    public static Field slide(Field f, long delta) {
        return new Field(f.record(), f.offset() + (int) delta, f.attr());
    }
}
