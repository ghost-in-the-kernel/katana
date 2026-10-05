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
 * keys, FILE STATUS item, and the 01s under its FD with the smallest and largest of their lengths.
 */
final class Files {
    private Files() {}

    /** Each SELECT of the program, with its clauses and its FD's records. */
    static List<FileDescriptor> build(Parse.Result parse, CobolParser.ProgramUnitContext unit, Names names, Map<String, List<Integer>> records, Map<Integer, Integer> lengths) {
        List<FileDescriptor> files = new ArrayList<>();
        for (CobolParser.FileControlEntryContext e : entries(unit)) {
            String name = Values.upper(e.selectClause().fileName().getText());
            String assign = "";
            FileDescriptor.Organization organization = FileDescriptor.Organization.SEQUENTIAL;
            FileDescriptor.Access access = FileDescriptor.Access.SEQUENTIAL;
            int key = 0, relative = 0, status = 0;
            List<FileDescriptor.AlternateKey> alternates = new ArrayList<>();
            String where = Origins.at(parse, e).toString();
            for (CobolParser.FileControlClauseContext c : e.fileControlClause()) {
                if (c.assignClause() != null) assign = assign(c.assignClause());
                if (c.organizationClause() != null) organization = organization(c.organizationClause());
                if (c.accessModeClause() != null) access = access(c.accessModeClause());
                if (c.recordKeyClause() != null) key = names.item(Values.upper(c.recordKeyClause().qualifiedDataName().getText()), List.of(), null, where);
                if (c.alternateRecordKeyClause() != null) {
                    int item = names.item(Values.upper(c.alternateRecordKeyClause().qualifiedDataName().getText()), List.of(), null, where);
                    if (item != 0) alternates.add(new FileDescriptor.AlternateKey(item, c.alternateRecordKeyClause().DUPLICATES() != null));
                }
                if (c.relativeKeyClause() != null) relative = names.item(Values.upper(c.relativeKeyClause().qualifiedDataName().getText()), List.of(), null, where);
                if (c.fileStatusClause() != null) status = names.item(Values.upper(c.fileStatusClause().qualifiedDataName(0).getText()), List.of(), null, where);
            }
            List<Integer> mine = records.getOrDefault(name, List.of());
            int min = mine.stream().mapToInt(lengths::get).min().orElse(0), max = mine.stream().mapToInt(lengths::get).max().orElse(0);
            files.add(new FileDescriptor(name, assign, organization, access, key, alternates, relative, status, mine, min, max, Origins.at(parse, e)));
        }
        return files;
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
