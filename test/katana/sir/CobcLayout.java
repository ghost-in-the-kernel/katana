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
    private static final Pattern ATTRIBUTE = Pattern.compile("static const cob_field_attr a_(\\d+) =\\s*\\{(0x[0-9a-f]+),\\s*(-?\\d+),\\s*(-?\\d+),\\s*(0x[0-9a-f]+),");

    /** One field cobc uses: its name, size, the storage it is in (null for none), its offset there, and its attribute. */
    record Field(String name, int size, String storage, int offset, String type, int digits, int scale, int flags) {}

    private CobcLayout() {}

    /** Each program unit's fields: `NAME.c.l.h` for one program, `NAME.c.l1.h`, `.c.l2.h`, ... for nested ones. */
    static List<List<Field>> read(Path dir, String name) throws Exception {
        Map<String, String[]> attributes = new HashMap<>();
        Matcher a = ATTRIBUTE.matcher(Files.readString(dir.resolve(name + ".c.h")));
        while (a.find()) attributes.put(a.group(1), new String[] {a.group(2), a.group(3), a.group(4), a.group(5)});
        List<List<Field>> units = new ArrayList<>();
        if (Files.exists(dir.resolve(name + ".c.l.h"))) units.add(fields(Files.readString(dir.resolve(name + ".c.l.h")), attributes));
        for (int i = 1; Files.exists(dir.resolve(name + ".c.l" + i + ".h")); i++) units.add(fields(Files.readString(dir.resolve(name + ".c.l" + i + ".h")), attributes));
        return units;
    }

    private static List<Field> fields(String text, Map<String, String[]> attributes) {
        Map<String, String> storage = new HashMap<>();
        Matcher b = STORAGE.matcher(text);
        while (b.find()) storage.put(b.group(1), b.group(3).trim().toUpperCase(Locale.ROOT));
        List<Field> fields = new ArrayList<>();
        Matcher f = FIELD.matcher(text);
        while (f.find()) {
            String[] at = attributes.get(f.group(5));
            fields.add(new Field(f.group(6).trim().toUpperCase(Locale.ROOT), Integer.parseInt(f.group(2)), f.group(3) == null ? null : storage.get(f.group(3)),
                    f.group(4) == null ? 0 : Integer.parseInt(f.group(4)), at[0], Integer.parseInt(at[1]), Integer.parseInt(at[2]), Integer.decode(at[3])));
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
    static String difference(Item i, Field f) {
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
        return null;
    }

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
