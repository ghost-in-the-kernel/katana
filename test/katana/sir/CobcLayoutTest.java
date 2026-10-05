package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import katana.sir.build.Build;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Origin;
import org.junit.jupiter.api.Test;

/** The data map oracle's A4 comparisons can fail (R-056): a mask, a flag bit, EXTERNAL, each a difference when ours is not cobc's. */
class CobcLayoutTest {
    private static final Origin AT = new Origin("x", 1, 1, Origin.Kind.SOURCE);
    private static final Build.Data PLAIN = new Build.Data(List.of(), List.of(), '$', false);

    private static Item edited(String mask, List<Item.Flag> flags) {
        Attributes a = new Attributes(Attributes.Usage.DISPLAY, Attributes.Category.NUMERIC_EDITED, 3, 0, false, false, false, mask, mask);
        return new Item(1, "E", 1, 0, 1, Item.Section.WORKING_STORAGE, 0, mask.length(), a, 0, null, 0, null, List.of(), flags, AT);
    }

    private static CobcLayout.Field field(String picture, int flags, boolean external) {
        return new CobcLayout.Field("E", picture.length(), "E", 0, "0x24", 3, 0, flags, picture, external);
    }

    @Test
    void eachEditingFactIsCompared() {
        assertNull(CobcLayout.difference(edited("ZZ9", List.of()), field("ZZ9", 0, false), PLAIN));
        assertEquals("mask ZZ9 vs Z99", CobcLayout.difference(edited("ZZ9", List.of()), field("Z99", 0, false), PLAIN));
        assertEquals("justified false vs true", CobcLayout.difference(edited("ZZ9", List.of()), field("ZZ9", 0x10, false), PLAIN));
        assertEquals("blank when zero true vs false", CobcLayout.difference(edited("ZZ9", List.of(Item.Flag.BLANK_WHEN_ZERO)), field("ZZ9", 0, false), PLAIN));
        assertEquals("external false vs true", CobcLayout.difference(edited("ZZ9", List.of()), field("ZZ9", 0, true), PLAIN));
    }

    /** R-056's mutation for the layout itself: an item one byte off from cobc's field is not matched, so it is reported. */
    @Test
    void anOffsetOffByOneFindsNoItem() {
        Item item = edited("ZZ9", List.of());
        CobcLayout.Field at = new CobcLayout.Field("E", 3, "E", 0, "0x24", 3, 0, 0, "ZZ9", false);
        CobcLayout.Field off = new CobcLayout.Field("E", 3, "E", 1, "0x24", 3, 0, 0, "ZZ9", false);
        assertEquals(item, CobcLayout.match(at, List.of(item)));
        assertNull(CobcLayout.match(off, List.of(item)));
    }

    @Test
    void cobcsPictureIsReadUnderTheProgramsCurrencyAndPoint() {
        Build.Data f = new Build.Data(List.of(), List.of(), 'F', true);
        assertEquals("$$,$$9.99DB", CobcLayout.mask("FF.FF9,99DB", f));
    }
}
