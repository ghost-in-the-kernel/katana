package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** data_packed.at:1026, "PACKED-DECIMAL used with INITIALIZE". */
final class DataPacked1026 implements Lowered {
    public String id() {
        return "data_packed-1026";
    }

    public void run(Out out) {
        Field[] items = {Out.item(Attr.numeric(Attr.Usage.PACKED, 2, 0, false)), Out.item(Attr.numeric(Attr.Usage.PACKED, 2, 0, true)),
            Out.item(Attr.numeric(Attr.Usage.PACKED, 3, 0, false)), Out.item(Attr.numeric(Attr.Usage.PACKED, 3, 0, true))};
        for (Field f : items) {
            Move.move(Out.literal("0"), f);
            out.display(f);
        }
    }
}
