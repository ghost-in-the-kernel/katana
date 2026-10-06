package katana.rt.autotest;

import java.util.List;
import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Inspect;

/** run_misc.at:2947, "INSPECT preserve source". */
final class RunMisc2947 implements Lowered {
    public String id() {
        return "run_misc-2947";
    }

    public void run(Out out) {
        Field srcX = Out.item(Attr.zoned(3, 0, true), "123"), srcY = Out.item(Attr.zoned(3, 0, true), "456");
        Field dstX = Out.item(Attr.zoned(7, 0, true), "-6540321");
        out.display("SRC-X BEFORE: '", srcX, "'");
        out.display("SRC-Y BEFORE: '", srcY, "'");
        out.display("DST-X BEFORE: '", dstX, "'");
        byte[] d = Inspect.view(dstX);
        Inspect.replace(d, 0, d.length, List.of(
                new Inspect.Phrase(Inspect.Kind.LEADING, "654".getBytes(Out.CP.charset), Inspect.view(srcX), null, null),
                new Inspect.Phrase(Inspect.Kind.ALL, "321".getBytes(Out.CP.charset), Inspect.view(srcY), null, null)));
        Inspect.back(dstX, d);
        out.display("SRC-X AFTER: '", srcX, "'");
        out.display("SRC-Y AFTER: '", srcY, "'");
        out.display("DST-X AFTER: '", dstX, "'");
    }
}
