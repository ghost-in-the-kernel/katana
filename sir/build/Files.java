package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.FileDescriptor;

/**
 * FILE-CONTROL and the FDs to FileDescriptors: each SELECT's name, ASSIGN, organization (SEQUENTIAL
 * unless stated; LINE SEQUENTIAL kept apart), access (SEQUENTIAL unless stated), RECORD KEY, ALTERNATE
 * keys, OPTIONAL, FILE STATUS items, and the 01s under its FD with the smallest and largest of their lengths.
 */
final class Files {
    private Files() {}

    /** Each SELECT of the program, with its clauses and its FD's records. */
    static List<FileDescriptor> build(Parse.Result parse, CobolParser.ProgramUnitContext unit, Names names, Map<String, List<Integer>> records, Map<Integer, Integer> lengths) {
        List<FileDescriptor> files = new ArrayList<>();
        Map<String, CobolParser.FileDescriptionEntryContext> fds = fds(unit);
        Map<String, FileDescriptor.Organization> organizations = new java.util.HashMap<>();
        for (CobolParser.FileControlEntryContext e : entries(unit)) {
            for (CobolParser.FileControlClauseContext c : e.fileControlClause()) if (c.organizationClause() != null) organizations.put(Values.upper(e.selectClause().fileName().getText()), organization(c.organizationClause()));
        }
        Map<String, List<String>> shared = shared(unit, organizations);
        for (CobolParser.FileControlEntryContext e : entries(unit)) {
            String name = Values.upper(e.selectClause().fileName().getText());
            String assign = "";
            FileDescriptor.Organization organization = FileDescriptor.Organization.SEQUENTIAL;
            FileDescriptor.Access access = FileDescriptor.Access.SEQUENTIAL;
            int key = 0, relative = 0, status = 0, vsamStatus = 0;
            List<FileDescriptor.AlternateKey> alternates = new ArrayList<>();
            String where = Origins.at(parse, e).toString();
            for (CobolParser.FileControlClauseContext c : e.fileControlClause()) {
                if (c.assignClause() != null) assign = assign(c.assignClause());
                if (c.organizationClause() != null) organization = organization(c.organizationClause());
                if (c.accessModeClause() != null) access = access(c.accessModeClause());
                if (c.recordKeyClause() != null) key = item(names, c.recordKeyClause().qualifiedDataName(), where, "SELECT clause");
                if (c.alternateRecordKeyClause() != null) {
                    int item = item(names, c.alternateRecordKeyClause().qualifiedDataName(), where, "SELECT clause");
                    if (item != 0) alternates.add(new FileDescriptor.AlternateKey(item, c.alternateRecordKeyClause().DUPLICATES() != null));
                }
                if (c.relativeKeyClause() != null) relative = item(names, c.relativeKeyClause().qualifiedDataName(), where, "SELECT clause");
                if (c.fileStatusClause() != null) status = item(names, c.fileStatusClause().qualifiedDataName(0), where, "SELECT clause");
                if (c.fileStatusClause() != null && c.fileStatusClause().qualifiedDataName(1) != null) vsamStatus = item(names, c.fileStatusClause().qualifiedDataName(1), where, "SELECT clause");
            }
            List<Integer> mine = records.getOrDefault(name, List.of());
            int min = mine.stream().mapToInt(lengths::get).min().orElse(0), max = mine.stream().mapToInt(lengths::get).max().orElse(0);
            FileDescriptor.Fd fd = fds.containsKey(name) ? fd(parse, names, fds.get(name)) : null;
            files.add(new FileDescriptor(name, assign, e.selectClause().OPTIONAL() != null, organization, access, key, alternates, relative, status, vsamStatus, fd, mine, min, max, shared.getOrDefault(name, List.of()), Origins.at(parse, e)));
        }
        return files;
    }

    /**
     * The files each file shares its record area with: those of its SAME RECORD AREA clause ("the same main
     * storage area for processing the current logical record", LR 6.4 p. 157), and those of a SAME AREA
     * clause when both are relative or indexed, VSAM files, for which IBM treats SAME AREA as SAME RECORD
     * AREA (p. 156); SAME AREA between QSAM files is documentation, and SAME SORT AREA has no effect.
     */
    // Decision: R-019
    private static Map<String, List<String>> shared(CobolParser.ProgramUnitContext unit, Map<String, FileDescriptor.Organization> organizations) {
        Map<String, List<String>> shared = new java.util.HashMap<>();
        if (unit.environmentDivision() == null) return shared;
        java.util.function.Predicate<String> vsam = n -> organizations.get(n) == FileDescriptor.Organization.RELATIVE || organizations.get(n) == FileDescriptor.Organization.INDEXED;
        for (var body : unit.environmentDivision().environmentDivisionBody()) {
            if (body.inputOutputSection() == null) continue;
            for (var paragraph : body.inputOutputSection().inputOutputSectionParagraph()) {
                if (paragraph.ioControlParagraph() == null) continue;
                for (var clause : paragraph.ioControlParagraph().ioControlClause()) {
                    var same = clause.sameClause();
                    if (same == null || same.SORT() != null || same.SORT_MERGE() != null) continue;
                    List<String> names = same.fileName().stream().map(n -> Values.upper(n.getText())).filter(n -> same.RECORD() != null || vsam.test(n)).toList();
                    for (String n : names) for (String other : names) if (!other.equals(n)) shared.computeIfAbsent(n, k -> new ArrayList<>()).add(other);
                }
            }
        }
        return shared;
    }

    /** The program's FD and SD entries by file name. */
    private static Map<String, CobolParser.FileDescriptionEntryContext> fds(CobolParser.ProgramUnitContext unit) {
        Map<String, CobolParser.FileDescriptionEntryContext> fds = new java.util.HashMap<>();
        if (unit.dataDivision() == null) return fds;
        for (var s : unit.dataDivision().dataDivisionSection()) {
            if (s.fileSection() != null) for (var fd : s.fileSection().fileDescriptionEntry()) fds.put(Values.upper(fd.fileName().getText()), fd);
        }
        return fds;
    }

    /**
     * What an FD or SD entry says (SIR-11): EXTERNAL, GLOBAL, the RECORD clause, LINAGE, CODE-SET and
     * RECORDING MODE as data. LABEL RECORDS, VALUE OF and BLOCK CONTAINS are each "syntax checked, but
     * has no effect on the execution of the program", DATA RECORDS "serves only as documentation" (LR
     * 6.4 pp. 186-188); a REPORT clause is Report Writer, not in Enterprise COBOL, and a finding.
     */
    // Decision: R-017
    private static FileDescriptor.Fd fd(Parse.Result parse, Names names, CobolParser.FileDescriptionEntryContext e) {
        boolean external = false, global = false, varying = false;
        int from = 0, to = 0, depending = 0;
        FileDescriptor.Linage linage = null;
        String codeSet = "", mode = "", where = Origins.at(parse, e).toString();
        for (CobolParser.FileDescriptionEntryClauseContext c : e.fileDescriptionEntryClause()) {
            external |= c.externalClause() != null;
            global |= c.globalClause() != null;
            if (c.recordContainsClause() != null) {
                var r = c.recordContainsClause();
                List<CobolParser.IntegerLiteralContext> sizes = all(r, CobolParser.IntegerLiteralContext.class);
                if (!sizes.isEmpty()) from = Integer.parseInt(sizes.get(0).getText());
                to = sizes.size() > 1 ? Integer.parseInt(sizes.get(1).getText()) : r.recordContainsClauseFormat1() != null ? from : 0;
                varying = r.recordContainsClauseFormat2() != null;
                if (varying && r.recordContainsClauseFormat2().qualifiedDataName() != null) depending = item(names, r.recordContainsClauseFormat2().qualifiedDataName(), where, "RECORD DEPENDING ON");
            }
            if (c.linageClause() != null) linage = linage(names, c.linageClause(), where);
            if (c.codeSetClause() != null) codeSet = Values.upper(c.codeSetClause().alphabetName().getText());
            if (c.recordingModeClause() != null) mode = Values.upper(c.recordingModeClause().modeStatement().getText());
            if (c.reportClause() != null) names.note("REPORT clause at " + where + ": Report Writer is not in Enterprise COBOL");
        }
        return new FileDescriptor.Fd(e.SD() != null, external, global, from, to, varying, depending, linage, codeSet, mode);
    }

    /** A LINAGE clause's four values, each an integer or an item (null when its phrase is not written). */
    private static FileDescriptor.Linage linage(Names names, CobolParser.LinageClauseContext l, String where) {
        katana.sir.types.Value footing = null, top = null, bottom = null;
        for (var at : l.linageAt()) {
            if (at.linageFootingAt() != null) footing = value(names, at.linageFootingAt().qualifiedDataName(), at.linageFootingAt().integerLiteral(), where);
            if (at.linageLinesAtTop() != null) top = value(names, at.linageLinesAtTop().qualifiedDataName(), at.linageLinesAtTop().integerLiteral(), where);
            if (at.linageLinesAtBottom() != null) bottom = value(names, at.linageLinesAtBottom().qualifiedDataName(), at.linageLinesAtBottom().integerLiteral(), where);
        }
        return new FileDescriptor.Linage(value(names, l.qualifiedDataName(), l.integerLiteral(), where), footing, top, bottom);
    }

    private static katana.sir.types.Value value(Names names, CobolParser.QualifiedDataNameContext name, CobolParser.IntegerLiteralContext integer, String where) {
        if (integer != null) return Literals.of(integer.getText());
        int item = item(names, name, where, "LINAGE");
        return item == 0 ? new katana.sir.types.Value.Name("unresolved", Values.upper(name.getText())) : new katana.sir.types.Value.Field(item, List.of(), null, null);
    }

    private static <T> List<T> all(org.antlr.v4.runtime.tree.ParseTree node, Class<T> kind) {
        List<T> found = new ArrayList<>();
        if (kind.isInstance(node)) found.add(kind.cast(node));
        for (int i = 0; i < node.getChildCount(); i++) found.addAll(all(node.getChild(i), kind));
        return found;
    }

    /**
     * The item a SELECT or FD clause names, with its qualifiers (`SQ-FS4-STATUS OF STATUS-GROUP`); one
     * that names no item is a finding, never a silent 0 (SIR-09). `what` names the clause in the finding.
     */
    private static int item(Names names, CobolParser.QualifiedDataNameContext q, String where, String what) {
        CobolParser.QualifiedDataNameFormat1Context f = q.qualifiedDataNameFormat1();
        String name = Values.upper(f == null ? q.getText() : f.getChild(0).getText());
        List<String> qualifiers = new ArrayList<>();
        if (f != null) for (var in : f.qualifiedInData()) qualifiers.add(Values.upper(in.inData() != null ? in.inData().dataName().getText() : in.inTable().tableCall().qualifiedDataName().getText()));
        String file = f == null || f.inFile() == null ? null : Values.upper(f.inFile().fileName().getText());
        return names.resolve(name, qualifiers, file, where, what);
    }

    /**
     * What a file is ASSIGNed to: a literal's characters exactly as written (a file name's case and
     * spaces are its own, LANE-07), or its words upper-cased (a name, `DISK`, `EXTERNAL X`).
     */
    private static String assign(CobolParser.AssignClauseContext a) {
        if (a.literal() != null && Values.literal(a.literal()) instanceof katana.sir.types.Value.Literal l) return l.payload();
        return Values.upper(DataMap.text(a).replaceFirst("(?i)^ASSIGN\\s+(TO\\s+)?", ""));
    }

    private static List<CobolParser.FileControlEntryContext> entries(CobolParser.ProgramUnitContext unit) {
        List<CobolParser.FileControlEntryContext> entries = new ArrayList<>();
        if (unit.environmentDivision() == null) return entries;
        for (var body : unit.environmentDivision().environmentDivisionBody()) {
            if (body.inputOutputSection() == null) continue;
            for (var p : body.inputOutputSection().inputOutputSectionParagraph()) if (p.fileControlParagraph() != null) entries.addAll(p.fileControlParagraph().fileControlEntry());
        }
        return entries;
    }

    private static FileDescriptor.Organization organization(CobolParser.OrganizationClauseContext o) {
        if (o.INDEXED() != null) return FileDescriptor.Organization.INDEXED;
        if (o.RELATIVE() != null) return FileDescriptor.Organization.RELATIVE;
        return o.LINE() != null ? FileDescriptor.Organization.LINE_SEQUENTIAL : FileDescriptor.Organization.SEQUENTIAL;
    }

    private static FileDescriptor.Access access(CobolParser.AccessModeClauseContext a) {
        if (a.RANDOM() != null) return FileDescriptor.Access.RANDOM;
        return a.DYNAMIC() != null ? FileDescriptor.Access.DYNAMIC : FileDescriptor.Access.SEQUENTIAL;
    }
}
