package katana.support;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * A program small enough to read, built for a unit test (J-240: one helper where nine tests each kept a copy): its
 * WORKING-STORAGE and its PROCEDURE DIVISION, each line indented to area B, parsed and built under a zero profile hash.
 */
public final class Small {
    private Small() {}

    /** PROGRAM-ID P: its storage, then its procedure as written (paragraph names and all). */
    public static Program program(String storage, String procedure) {
        return program("P", storage, procedure);
    }

    public static Program program(String name, String storage, String procedure) {
        return program(name, storage, "", "", procedure);
    }

    /** With a LINKAGE SECTION and the PROCEDURE DIVISION's USING, for a CALL's other end. */
    public static Program program(String name, String storage, String linkage, String using, String procedure) {
        String source = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. " + name + ".\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + storage + (linkage.isEmpty() ? "" : "LINKAGE SECTION.\n" + linkage)
                + "PROCEDURE DIVISION" + (using.isEmpty() ? "" : " USING " + using) + ".\n" + procedure)
                .lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
    }

    /** An item's id by its name. */
    public static int item(Program p, String name) {
        return p.items().stream().filter(i -> i.name().equals(name)).mapToInt(Item::id).findFirst().orElseThrow();
    }
}
