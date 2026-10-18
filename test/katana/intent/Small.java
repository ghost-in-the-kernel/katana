package katana.intent;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Program;

/** A program small enough to read, for the review tests' killer mutations: its working storage and its procedure, each line indented to area B. */
final class Small {
    private Small() {}

    static Program program(String name, String storage, String procedure) {
        return program(name, storage, "", "", procedure);
    }

    /** With a LINKAGE SECTION and the PROCEDURE DIVISION's USING, for a CALL's other end. */
    static Program program(String name, String storage, String linkage, String using, String procedure) {
        String source = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. " + name + ".\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + storage + (linkage.isEmpty() ? "" : "LINKAGE SECTION.\n" + linkage)
                + "PROCEDURE DIVISION" + (using.isEmpty() ? "" : " USING " + using) + ".\n" + procedure)
                .lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
    }

    static int item(Program p, String name) {
        return p.items().stream().filter(i -> i.name().equals(name)).mapToInt(Item::id).findFirst().orElseThrow();
    }
}
