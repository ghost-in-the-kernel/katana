package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Collation;
import katana.rt.Compare;
import katana.rt.Field;
import katana.rt.Move;
import katana.rt.Record;

/** run_initialize.at:90, "INITIALIZE OCCURS with SIGN LEADING / TRAILING". */
final class RunInitialize90 implements Lowered {
    public String id() {
        return "run_initialize-90";
    }

    public void run(Out out) {
        Record g1 = Record.of(8, Out.CP);
        Attr lead = new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 1, 0, true, true, true, false, null);
        Attr trail = new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 1, 0, true, true, false, false, null);
        Field[] x = {Out.at(g1, 0, lead), Out.at(g1, 2, lead)}, y = {Out.at(g1, 4, trail), Out.at(g1, 6, trail)};
        Attr int18 = new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 18, 0, true, true, false, false, null);
        Attr frac18 = new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 18, 18, true, true, false, false, null);
        Record mn = Record.of(114, Out.CP);
        Field minus1 = Out.at(mn, 0, int18), even1 = Out.at(mn, 19, int18), plus1 = Out.at(mn, 38, int18);
        Field minus3 = Out.at(mn, 57, frac18), even2 = Out.at(mn, 76, frac18), plus3 = Out.at(mn, 95, frac18);
        String[][] values = {{"-999999999999999999"}, {"1"}, {"999999999999999999"}, {"-.999999999999999999"}, {".1"}, {".999999999999999999"}};
        Field[] mns = {minus1, even1, plus1, minus3, even2, plus3};
        for (int k = 0; k < 6; k++) Move.move(Out.literal(values[k][0]), mns[k]);
        initialize(x[0], x[1], y[0], y[1]);
        Move.move(Out.literal("5"), x[0]);
        Move.move(Out.literal("5"), plus1);
        Move.move(Out.literal("-9"), y[1]);
        Move.move(Out.literal("-9"), minus1);
        if (!equal(g1, 0, 8, "+5+00+9-")) out.display("MOVE G \"", Out.ref(Out.at(g1, 0, Attr.text(8, false)), 1, 8), "\"");
        Move.move(Out.literal(".123"), plus3);
        if (!equal(mn, 0, 57, "000000000000000009-000000000000000001+000000000000000005+") || !equal(mn, 57, 57, "999999999999999999-100000000000000000+123000000000000000+")) {
            out.display("MOVE MN1 \"", java.util.Arrays.copyOfRange(mn.bytes(), 0, 57), "\"");
            out.display("MOVE MN2 \"", java.util.Arrays.copyOfRange(mn.bytes(), 57, 114), "\"");
        }
        initialize(x[0], x[1], y[0], y[1], minus1, even1, plus1, minus3, even2, plus3);
        if (!equal(g1, 0, 8, "+0+00+0+")) out.display("INIT G1 \"", java.util.Arrays.copyOfRange(g1.bytes(), 0, 8), "\"");
        String zeros = "000000000000000000+".repeat(3);
        if (!equal(mn, 0, 57, zeros) || !equal(mn, 57, 57, zeros)) {
            out.display("MOVE MN1 \"", java.util.Arrays.copyOfRange(mn.bytes(), 0, 57), "\"");
            out.display("MOVE MN2 \"", java.util.Arrays.copyOfRange(mn.bytes(), 57, 114), "\"");
        }
        Move.move(Out.literal(".123"), plus3);
        Move.move(Out.literal("-.456"), minus3);
        out.display(plus3);
        out.display(minus3);
    }

    /** INITIALIZE's implicit moves (LR p. 352): each numeric receiver given ZERO. */
    private static void initialize(Field... numeric) {
        for (Field f : numeric) Move.move(Out.literal("0"), f);
    }

    /** A group compared with an alphanumeric literal: as alphanumeric bytes (LR p. 279). */
    private static boolean equal(Record r, int at, int length, String literal) {
        byte[] l = literal.getBytes(Out.CP.charset);
        return Compare.text(r.bytes(), at, length, l, 0, l.length, Out.CP.character(' '), Collation.nativeOrder()) == 0;
    }
}
