package katana.intent;

import java.util.Map;
import java.util.Set;
import katana.sir.types.Program;

/**
 * T-E2 (research §33, §34): an `enum` fact means the program reads the item as a choice, so some IF,
 * EVALUATE, PERFORM UNTIL or SEARCH WHEN tests it or one of its condition-names, in this program or in the
 * CardDemo program a CALL hands it to; handed to a program outside CardDemo's COBOL, it cannot tell.
 */
final class EnumIsRead {
    static final String TEST = "T-E2";

    private EnumIsRead() {}

    static Verdict verdict(Program p, int item, Corpus corpus) {
        Set<String> here = BatchReviewEvidence.checked(p, item);
        if (!here.isEmpty()) return Verdict.holds(TEST, "tested in " + EnumValuesStored.list(here));
        for (CallEnds.End e : CallEnds.callees(p, item, corpus)) {
            if (e.other() == null) return Verdict.cannotTell(TEST, "not tested in this program; it is handed by CALL to " + e.program() + ", which is not one of CardDemo's COBOL programs, and whether that tests it is not here to read");
            Set<String> tested = e.there() == null ? Set.of() : BatchReviewEvidence.checked(e.other(), e.there().id());
            if (!tested.isEmpty()) return Verdict.holds(TEST, "not tested in this program, but " + e.program() + ", which a CALL hands it to, tests it as " + e.there().name() + " in " + EnumValuesStored.list(tested));
            return Verdict.refutes(TEST, "not tested in this program, nor by " + e.program() + ", which a CALL hands it to" + (e.there() == null ? " (no item there lies where it does)" : " (as " + e.there().name() + ")"));
        }
        return Verdict.refutes(TEST, "no IF, EVALUATE, PERFORM UNTIL or SEARCH tests it or any of its condition-names, and no CALL hands it on: the program only sets it");
    }

}
