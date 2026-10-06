package katana.rt.autotest;

import java.util.List;
import katana.rt.Collation;
import katana.rt.Compare;
import katana.rt.Field;
import katana.rt.Strings;

/** run_misc.at:2799, "STRING / UNSTRING [[NOT]] ON OVERFLOW". */
final class RunMisc2799 implements Lowered {
    public String id() {
        return "run_misc-2799";
    }

    public void run(Out out) {
        // DISPLAY ... UPON SYSERR goes to stderr, which this check does not hold.
        Field simple = Out.text(20, false, "");
        Field data = Out.alphanumeric("data"), big = Out.alphanumeric("data is too big here...");
        for (int k = 0; k < 2; k++) {
            Field from = k == 0 ? data : big;
            java.util.Arrays.fill(simple.record().bytes(), Out.CP.character(' '));
            boolean overflow = Strings.string(List.of(new Strings.Sender(Out.ref(from, 1, from.attr().length()), null)), simple.record().bytes(), 0, 20, 1).overflow();
            out.display(k == 0 ? (overflow ? "1 failed" : "1 passed") : (overflow ? "2 passed" : "2 failed"));
            String want = k == 0 ? "data" : "data is too big here";
            if (cmp(simple, want) != 0) out.display(k == 0 ? "STRING ERROR (1): \"" : "STRING ERROR (2): \"", simple, "\"");
        }
        for (int k = 0; k < 2; k++) {
            Field from = k == 0 ? data : big;
            java.util.Arrays.fill(simple.record().bytes(), Out.CP.character(' '));
            boolean overflow = Strings.unstring(Out.ref(from, 1, from.attr().length()), 0, from.attr().length(), List.of(), List.of(new Strings.Receiver(simple, null)), 1, 0).result().overflow();
            out.display(k == 0 ? (overflow ? "3 failed" : "3 passed") : (overflow ? "4 passed" : "4 failed"));
            String want = k == 0 ? "data" : "data is too big here";
            if (cmp(simple, want) != 0) out.display(k == 0 ? "UNSTRING ERROR (1): \"" : "UNSTRING ERROR (2): \"", simple, "\"");
        }
    }

    private static int cmp(Field f, String literal) {
        byte[] l = literal.getBytes(Out.CP.charset);
        return Compare.text(f.record().bytes(), f.offset(), f.attr().length(), l, 0, l.length, Out.CP.character(' '), Collation.nativeOrder());
    }
}
