package katana.intent;

import java.util.LinkedHashSet;
import java.util.Set;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * T-E3's word on a file-status `enum` fact (research §33, §34): FileStatusAbsentIntegrationTest shows, in
 * both lanes, that an OPEN INPUT of an absent file stores '35'; where the program opens the item's file for
 * input and no condition-name names '35', the item holds a value its fact says it never does.
 */
final class FileStatusAbsent {
    static final String TEST = "T-E3";

    private FileStatusAbsent() {}

    static Verdict verdict(Program p, int item) {
        FileDescriptor file = p.files().stream().filter(d -> d.status() == item).findFirst().orElse(null);
        if (file == null) return Verdict.cannotTell(TEST, "it is no file's status, so an absent file does not set it");
        Set<String> opened = opened(p, file.name());
        if (opened.isEmpty()) return Verdict.cannotTell(TEST, "the program never opens " + file.name() + " for input");
        Item it = p.items().get(item - 1);
        boolean named = it.conditions().stream().flatMap(c -> c.values().stream()).anyMatch(v -> v instanceof Value.Literal l && l.payload().equals("35"));
        if (named) return Verdict.holds(TEST, "a condition-name names 35, the status an absent " + file.name() + " leaves");
        return Verdict.refutes(TEST, "OPEN INPUT of " + file.name() + " in " + EnumValuesStored.list(opened) + " leaves 35 when the file is absent (both lanes, FileStatusAbsentIntegrationTest), and no condition-name names 35");
    }

    /** The paragraphs that OPEN the file INPUT or I-O. */
    private static Set<String> opened(Program p, String file) {
        Set<String> out = new LinkedHashSet<>();
        for (BatchReviewEvidence.At a : BatchReviewEvidence.statements(p)) {
            if (!a.s().verb().equals("OPEN")) continue;
            String mode = "";
            for (Statement.Operand o : a.s().operands()) {
                if (o.value() instanceof Value.Name n && n.kind().equals("keyword")) mode = n.text();
                if (o.value() instanceof Value.File f && f.name().equals(file) && (mode.equals("INPUT") || mode.equals("I-O"))) out.add(a.paragraph());
            }
        }
        return out;
    }
}
