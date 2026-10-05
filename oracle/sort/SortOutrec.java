package katana.oracle.sort;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * OUTREC FIELDS (or BUILD): the output record built from columns (`c:`), fields (`p,l` or a symbol),
 * blanks (`X`, `nX`), character constants, and a ZD field through an EDIT mask of `T` (a digit), `I`
 * (a digit, blank while leading zeros last) and the punctuation `.,/:-` and blank. Anything else is
 * refused with the item quoted.
 */
final class SortOutrec {
    /** One item: appends its bytes for a record. */
    interface Item {
        void append(byte[] record, ByteArrayOutputStream out);
    }

    private SortOutrec() {}

    static List<Item> parse(List<String> items, SortCards cards) {
        List<Item> built = new ArrayList<>();
        for (int i = 0; i < items.size(); ) {
            String item = items.get(i);
            if (item.matches("\\d+:.+")) {
                int column = Integer.parseInt(item.substring(0, item.indexOf(':')));
                built.add(column(column));
                items = new ArrayList<>(items);
                items.set(i, item.substring(item.indexOf(':') + 1));
                continue;
            }
            i = item(items, i, cards, built);
        }
        return built;
    }

    /** One item at i, appended to built; the index after it. */
    private static int item(List<String> items, int i, SortCards cards, List<Item> built) {
        String item = items.get(i);
        if (item.matches("\\d*X")) {
            int n = item.length() == 1 ? 1 : Integer.parseInt(item.substring(0, item.length() - 1));
            built.add((r, out) -> out.writeBytes(" ".repeat(n).getBytes(StandardCharsets.ISO_8859_1)));
            return i + 1;
        }
        if (item.matches("C'([^']|'')*'")) {
            byte[] text = item.substring(2, item.length() - 1).replace("''", "'").getBytes(StandardCharsets.ISO_8859_1);
            built.add((r, out) -> out.writeBytes(text));
            return i + 1;
        }
        SortCards.Field field = cards.field(item);
        int next = i + 1;
        if (field == null && item.matches("\\d+") && next < items.size() && items.get(next).matches("\\d+")) {
            boolean format = next + 1 < items.size() && items.get(next + 1).matches("[A-Z]{2}");
            field = new SortCards.Field(Integer.parseInt(item), Integer.parseInt(items.get(next)), format ? items.get(next + 1) : "CH");
            next += format ? 2 : 1;
        }
        if (field == null) throw new SortCards.Unsupported("OUTREC item " + item);
        if (next < items.size() && items.get(next).startsWith("EDIT=(")) {
            built.add(edit(field, items.get(next)));
            return next + 1;
        }
        SortCards.Field f = field;
        built.add((r, out) -> out.write(r, f.position() - 1, f.length()));
        return next;
    }

    /** `c:` pads with blanks to column c; a column already passed is refused. */
    private static Item column(int column) {
        return (r, out) -> {
            if (out.size() > column - 1) throw new SortCards.Unsupported("OUTREC column " + column + " is behind the " + out.size() + " bytes already built");
            out.writeBytes(" ".repeat(column - 1 - out.size()).getBytes(StandardCharsets.ISO_8859_1));
        };
    }

    /** EDIT=(mask) on a ZD field: its digits right-aligned into the mask's T and I positions, the sign not shown. */
    private static Item edit(SortCards.Field field, String edit) {
        String mask = edit.substring(6, edit.length() - 1);
        if (!field.format().equals("ZD")) throw new SortCards.Unsupported("EDIT on format " + field.format());
        if (!mask.matches("[TI.,/: -]+")) throw new SortCards.Unsupported("EDIT mask " + mask + ": the lane edits with T, I and punctuation");
        int digits = mask.replaceAll("[^TI]", "").length();
        return (r, out) -> {
            String value = Zoned.value(r, field).abs().toString();
            if (value.length() > digits) throw new SortCards.Unsupported("a value of " + value.length() + " digits into EDIT mask " + mask);
            String padded = "0".repeat(digits - value.length()) + value;
            StringBuilder text = new StringBuilder();
            boolean leading = true;
            int d = 0;
            for (char m : mask.toCharArray()) {
                if (m != 'T' && m != 'I') {
                    text.append(m);
                    continue;
                }
                char digit = padded.charAt(d++);
                leading = leading && digit == '0' && m == 'I';
                text.append(leading ? ' ' : digit);
            }
            out.writeBytes(text.toString().getBytes(StandardCharsets.ISO_8859_1));
        };
    }

    /** The output record for one input record. */
    static byte[] build(List<Item> items, byte[] record) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Item item : items) item.append(record, out);
        return out.toByteArray();
    }
}
