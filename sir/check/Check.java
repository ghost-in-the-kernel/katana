package katana.sir.check;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * What a program gets wrong, as findings (docs/ROADMAP.md 2.5): a REDEFINES below level 01 longer than
 * what it redefines; a PERFORM or GO TO whose target names nothing; an ODO object declared after its
 * table; a file with no FILE STATUS item, whose I/O errors end the run instead of being seen. Each
 * nested program is checked as well, its findings naming it.
 */
public final class Check {
    private Check() {}

    public static List<String> findings(Program program) {
        List<String> findings = new ArrayList<>();
        check(program, findings);
        return findings;
    }

    /** One program's items, files and statements checked, then its nested programs. */
    private static void check(Program p, List<String> findings) {
        String in = p.header().program() + ": ";
        for (Item i : p.items()) {
            if (i.redefines() != 0 && i.level() != 1) {
                Item target = p.items().get(i.redefines() - 1);
                if (i.length() > target.length()) findings.add(in + i.name() + " REDEFINES " + target.name() + " and is longer (" + i.length() + " bytes, " + target.length() + ") at " + i.origin());
            }
            if (i.occurs() != null && i.occurs().dependingOn() > i.id()) {
                findings.add(in + "the ODO object " + p.items().get(i.occurs().dependingOn() - 1).name() + " of " + i.name() + " is declared after it, at " + i.origin());
            }
        }
        for (FileDescriptor f : p.files()) {
            if (f.status() == 0) findings.add(in + "file " + f.name() + " has no FILE STATUS item: an I/O error ends the run unseen, at " + f.origin());
        }
        for (Procedure.Paragraph paragraph : p.procedure().paragraphs()) for (Statement s : paragraph.statements()) targets(s, in, findings);
        for (Program nested : p.nested()) check(nested, findings);
    }

    private static void targets(Statement s, String in, List<String> findings) {
        for (Statement.Operand o : s.operands()) {
            if (o.value() instanceof Value.Name n && n.kind().equals("unresolved procedure")) {
                findings.add(in + s.verb() + " names " + n.text() + ", which is no paragraph or section, at " + s.origin());
            }
        }
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) targets(t, in, findings);
    }
}
