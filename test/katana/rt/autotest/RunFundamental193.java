package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_fundamental.at:193, "DISPLAY data items with MOVE statement". */
final class RunFundamental193 implements Lowered {
    public String id() {
        return "run_fundamental-193";
    }

    public void run(Out out) {
        Field abc = Out.text(3, false, "abc");
        Move.move(Out.alphanumeric("abc"), abc);
        out.display(abc);
        String[][] moves = {{"123", "3", "0", "u"}, {"+123", "3", "0", "s"}, {"-123", "3", "0", "s"}, {"12.3", "3", "1", "u"}, {"+12.3", "3", "1", "s"}, {"-12.3", "3", "1", "s"}};
        for (String[] m : moves) {
            Field f = Out.item(Attr.zoned(Integer.parseInt(m[1]), Integer.parseInt(m[2]), m[3].equals("s")), m[0].replace("+", "").replace("-", ""));
            Move.move(Out.literal(m[0]), f);
            out.display(f);
        }
    }
}
