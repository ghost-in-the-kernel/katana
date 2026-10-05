package katana.sir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.sir.types.Attributes;
import katana.sir.build.Build;
import katana.sir.types.Item;

/**
 * What `cobc -C` lays out (2.5's oracle, F-06), read from its output only: each program unit's storage
 * (`b_N[size]`, named in a comment) and each field it uses (`f_N = {size, b_M + offset, &a_K}`, named),
 * with the attribute each field points at (`a_K = {type, digits, scale, flags, ...}`, in the `.c.h`).
 * A field without storage (LINKAGE, `NULL`) has no offset to compare.
 */
final class CobcLayout {
    private static final Pattern STORAGE = Pattern.compile("static cob_u8_t\\s+b_(\\d+)\\[(\\d+)\\].*/\\* (.*) \\*/");
    private static final Pattern FIELD = Pattern.compile("static cob_field f_(\\d+)\\s*=\\s*\\{(\\d+), (?:b_(\\d+)(?: \\+ (\\d+))?|NULL), &a_(\\d+)\\};\\s*/\\* (.*) \\*/");
    private static final Pattern ATTRIBUTE = Pattern.compile("static const cob_field_attr a_(\\d+) =\\s*\\{(0x[0-9a-f]+),\\s*(-?\\d+),\\s*(-?\\d+),\\s*(0x[0-9a-f]+),\\s*(p_\\d+|NULL)");
    private static final Pattern PICTURE = Pattern.compile("static const cob_pic_symbol p_(\\d+)\\[\\] = \\{(.*?)\\};", Pattern.DOTALL);
    private static final Pattern SYMBOL = Pattern.compile("\\{'(.)', (\\d+)\\}");
    private static final Pattern EXTERNAL = Pattern.compile("cob_external_addr \\(\"([^\"]+)\"");

    /**
     * One field cobc uses: its name, size, the storage it is in (null for none), its offset there, its
     * attribute, its picture as cobc keeps it for editing (one symbol a position, C and D written CR and
     * DB; null for none), and whether it is EXTERNAL (its address taken by name at run time).
     */
    record Field(String name, int size, String storage, int offset, String type, int digits, int scale, int flags, String picture, boolean external) {}

    private CobcLayout() {}

    /** Each program unit's fields: `NAME.c.l.h` for one program, `NAME.c.l1.h`, `.c.l2.h`, ... for nested ones. */
    static List<List<Field>> read(Path dir, String name) throws Exception {
        String header = Files.readString(dir.resolve(name + ".c.h"));
        Map<String, String> pictures = new HashMap<>();
        Matcher p = PICTURE.matcher(header);
        while (p.find()) pictures.put("p_" + p.group(1), picture(p.group(2)));
        Map<String, String[]> attributes = new HashMap<>();
        Matcher a = ATTRIBUTE.matcher(header);
        while (a.find()) attributes.put(a.group(1), new String[] {a.group(2), a.group(3), a.group(4), a.group(5), pictures.get(a.group(6))});
        java.util.Set<String> externals = new java.util.HashSet<>();
        Matcher e = EXTERNAL.matcher(Files.readString(dir.resolve(name + ".c")));
        while (e.find()) externals.add(e.group(1).toUpperCase(Locale.ROOT));
        List<List<Field>> units = new ArrayList<>();
        if (Files.exists(dir.resolve(name + ".c.l.h"))) units.add(fields(Files.readString(dir.resolve(name + ".c.l.h")), attributes, externals));
        for (int i = 1; Files.exists(dir.resolve(name + ".c.l" + i + ".h")); i++) units.add(fields(Files.readString(dir.resolve(name + ".c.l" + i + ".h")), attributes, externals));
        return units;
    }

    /** A picture's symbols, each as often as it repeats, to the `\\0` that ends them; C is CR and D is DB. */
    private static String picture(String symbols) {
        StringBuilder out = new StringBuilder();
        Matcher m = SYMBOL.matcher(symbols);
        while (m.find() && !m.group(1).equals("\\")) {
            String symbol = m.group(1).equals("C") ? "CR" : m.group(1).equals("D") ? "DB" : m.group(1);
            out.append(symbol.repeat(Integer.parseInt(m.group(2))));
        }
        return out.toString();
    }

    private static List<Field> fields(String text, Map<String, String[]> attributes, java.util.Set<String> externals) {
        Map<String, String> storage = new HashMap<>();
        Matcher b = STORAGE.matcher(text);
        while (b.find()) storage.put(b.group(1), b.group(3).trim().toUpperCase(Locale.ROOT));
        List<Field> fields = new ArrayList<>();
        Matcher f = FIELD.matcher(text);
        while (f.find()) {
            String[] at = attributes.get(f.group(5));
            fields.add(new Field(f.group(6).trim().toUpperCase(Locale.ROOT), Integer.parseInt(f.group(2)), f.group(3) == null ? null : storage.get(f.group(3)),
                    f.group(4) == null ? 0 : Integer.parseInt(f.group(4)), at[0], Integer.parseInt(at[1]), Integer.parseInt(at[2]), Integer.decode(at[3]), at[4],
                    externals.contains(f.group(6).trim().toUpperCase(Locale.ROOT))));
        }
        return fields;
    }

    /** Whether a field is one of the program's items: not a file's record area, a register or a debug item. */
    static boolean compared(Field f) {
        return !f.name().endsWith(" RECORD") && !f.name().contains(" ") && !f.name().startsWith("RETURN-CODE") && !f.name().startsWith("DEBUG-");
    }

    /**
     * Our item for a cobc field: of its name, at its offset when it has storage, in the record cobc's
     * storage is named after when more than one could be; null when none.
     */
    static Item match(Field f, List<Item> items) {
        List<Item> named = items.stream().filter(i -> i.name().equals(f.name())).toList();
        List<Item> here = named.stream().filter(i -> f.storage() == null || i.offset() == f.offset()).toList();
        return here.stream().filter(i -> f.storage() == null || items.get(i.record() - 1).name().equals(f.storage())).findFirst().orElse(here.isEmpty() ? null : here.get(0));
    }

    /**
     * How our item differs from cobc's field, or null when it does not. cobc stores an alphabetic item
     * as alphanumeric, an INDEX as native binary, and a 66 that renames a range as alphanumeric; those
     * are its representations of the same category, not differences.
     */
    static String difference(Item i, Field f, Build.Data unit) {
        Attributes a = i.attributes();
        if (i.length() != f.size()) return "size " + i.length() + " vs " + f.size();
        if (a.category() == Attributes.Category.INDEX) return f.type().equals("0x1b") ? null : "type INDEX vs " + f.type();
        if (i.level() == 66 && a.category() == Attributes.Category.GROUP && f.type().equals("0x21")) return null;
        String type = type(a);
        if (!type.equals(f.type())) return "type " + type + " vs " + f.type();
        boolean numeric = type.startsWith("0x1") || type.equals("0x24");
        if (numeric && a.digits() != f.digits() && a.usage() != Attributes.Usage.FLOAT_SHORT && a.usage() != Attributes.Usage.FLOAT_LONG) return "digits " + a.digits() + " vs " + f.digits();
        if (numeric && a.scale() != f.scale()) return "scale " + a.scale() + " vs " + f.scale();
        if (numeric && a.signed() != ((f.flags() & 1) != 0)) return "signed " + a.signed() + " vs " + ((f.flags() & 1) != 0);
        return editing(i, f, unit);
    }

    /** How our item's mask and flags differ from cobc's picture and flag bits (0x08 BLANK WHEN ZERO, 0x10 JUSTIFIED, as observed), or null. */
    private static String editing(Item i, Field f, Build.Data unit) {
        Attributes a = i.attributes();
        if (f.picture() != null && !a.mask().equals(mask(f.picture(), unit))) return "mask " + a.mask() + " vs " + mask(f.picture(), unit);
        if (f.picture() == null && !a.mask().isEmpty()) return "mask " + a.mask() + " vs none";
        if (i.flags().contains(Item.Flag.JUSTIFIED) != ((f.flags() & 0x10) != 0)) return "justified " + i.flags().contains(Item.Flag.JUSTIFIED) + " vs " + ((f.flags() & 0x10) != 0);
        if (i.flags().contains(Item.Flag.BLANK_WHEN_ZERO) != ((f.flags() & 0x08) != 0)) return "blank when zero " + i.flags().contains(Item.Flag.BLANK_WHEN_ZERO) + " vs " + ((f.flags() & 0x08) != 0);
        if (i.flags().contains(Item.Flag.EXTERNAL) != f.external()) return "external " + i.flags().contains(Item.Flag.EXTERNAL) + " vs " + f.external();
        return null;
    }

    /** cobc's picture as an IR mask: the program's currency sign written `$`, its point `.` (DECIMAL-POINT IS COMMA swapped), S, V and P dropped. */
    static String mask(String picture, Build.Data unit) {
        String p = picture.replace(unit.currency(), '$');
        if (unit.decimalComma()) p = p.replace('.', '\u0001').replace(',', '.').replace('\u0001', ',');
        return p.replaceAll("[SVP]", "");
    }

    /** The cob_field_attr type cobc gives a category and usage, as observed in its output. */
    private static String type(Attributes a) {
        return switch (a.category()) {
            case GROUP -> "0x01";
            case NUMERIC -> switch (a.usage()) {
                case BINARY, NATIVE_BINARY -> "0x11";
                case PACKED -> "0x12";
                case FLOAT_SHORT -> "0x13";
                case FLOAT_LONG -> "0x14";
                default -> "0x10";
            };
            case NUMERIC_EDITED -> "0x24";
            case ALPHANUMERIC, ALPHABETIC -> "0x21";
            case ALPHANUMERIC_EDITED -> "0x23";
            case NATIONAL -> "0x40";
            case INDEX -> "0x1b";
            case POINTER -> "0x80";
        };
    }
}
