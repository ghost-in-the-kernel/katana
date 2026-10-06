package katana.rt.autotest;

import java.util.List;
import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Record;
import katana.rt.Strings;

/** run_misc.at:2916, "STRING preserve source". */
final class RunMisc2916 implements Lowered {
    public String id() {
        return "run_misc-2916";
    }

    public void run(Out out) {
        Record src = Record.of(11, Out.CP);
        Field srcX = Out.at(src, 0, Attr.text(3, false)), srcY = Out.at(src, 3, Attr.text(8, false));
        System.arraycopy("123".getBytes(Out.CP.charset), 0, src.bytes(), 0, 3);
        Field dstX = Out.text(8, false, "");
        out.display("SRC-Y BEFORE: '", srcY, "'");
        Strings.string(List.of(new Strings.Sender("DATA ".getBytes(Out.CP.charset), null), new Strings.Sender(Out.ref(srcX, 1, 3), null)), dstX.record().bytes(), 0, 8, 1);
        out.display("SRC-Y AFTER:  '", srcY, "'");
        out.display("DST-X AFTER:  '", dstX, "'");
    }
}
