package katana.intent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import katana.intent.types.Fact;
import katana.intent.types.Subject;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** The review file's docs lines (R-066): what CardDemo's authors wrote about an item, a block of steps or a program, quoted with file and line, or none. */
final class BatchReviewDocs {
    private BatchReviewDocs() {}

    
    /** The README's line for the program, quoted, or none. */
    private static String readme(String program) throws Exception {
        for (String line : Files.readAllLines(BatchReview.CARDDEMO.getParent().resolve("README.md"))) {
            if (line.contains("| " + program + " ")) return "README, its jobs table: \"" + String.join(", ", java.util.Arrays.stream(line.split("\\|")).map(String::strip).filter(c -> !c.isEmpty()).toList()) + "\"";
        }
        return "none";
    }

    /** The comment right above the item's declaration, or above its nearest group or record that has one; for a paragraph, the comment above it or just inside it; none where there is none. */
    static String docs(Program p, Fact f) throws Exception {
        switch (f.subject()) {
            case Subject.Datum d -> { return itemDocs(p, d.item()); }
            case Subject.Choice c -> { return itemDocs(p, c.item()); }
            case Subject.Range r -> {
                katana.sir.types.Origin at = p.procedure().paragraphs().get(r.first()).origin();
                String above = comment(at.file(), at.line(), -1), inside = comment(at.file(), at.line(), +1);
                return above != null ? above : inside != null ? inside : "none";
            }
            default -> { return "none"; }
        }
    }

    private static String itemDocs(Program p, int id) throws Exception {
        for (int at = id; at > 0; at = p.items().get(at - 1).parent()) {
            Item i = p.items().get(at - 1);
            String said = comment(i.origin().file(), i.origin().line(), -1);
            if (said != null) return at == id ? said : "of its " + (i.record() == i.id() ? "record" : "group") + ", " + i.name() + ": " + said;
        }
        return "none";
    }

    /** The program's header Function lines, the comment above the JCL step that runs it, and its README row, each quoted where there is one. */
    static List<String> programDocs(Program p) throws Exception {
        List<String> out = new ArrayList<>();
        String name = p.header().program();
        Path source = Path.of(p.header().source());
        if (!Files.exists(source)) source = BatchReview.CARDDEMO.resolve("cbl").resolve(BatchReview.file(p.header().source()));
        if (Files.exists(source)) {
            List<String> lines = Files.readAllLines(source, java.nio.charset.StandardCharsets.ISO_8859_1);
            for (int k = 0; k < Math.min(40, lines.size()); k++) {
                String text = lines.get(k).length() > 7 ? lines.get(k).substring(7, Math.min(72, lines.get(k).length())).strip() : "";
                if (text.matches("(?i)function\\s*:.*")) out.add("its header: \"" + text.replaceAll("\\s+", " ") + "\" (" + BatchReview.file(source.toString()) + " line " + (k + 1) + ")");
            }
        }
        try (java.util.stream.Stream<Path> jcl = Files.list(BatchReview.CARDDEMO.resolve("jcl"))) {
            for (Path job : jcl.sorted().toList()) {
                List<String> lines = Files.readAllLines(job, java.nio.charset.StandardCharsets.ISO_8859_1);
                for (int k = 0; k < lines.size(); k++) {
                    if (!lines.get(k).matches("//\\S*\\s+EXEC\\s+PGM=" + name + "\\b.*")) continue;
                    List<String> said = new ArrayList<>();
                    int first = k;
                    for (int j = k - 1; j >= 0 && lines.get(j).startsWith("//*"); j--) {
                        String text = lines.get(j).substring(3).replaceAll("[*]{3,}", "").strip();
                        if (text.isEmpty() && !said.isEmpty()) break;
                        if (!text.isEmpty()) {
                            said.add(0, text);
                            first = j;
                        }
                    }
                    if (!said.isEmpty()) out.add("the job step that runs it: \"" + String.join(" ", said).replaceAll("\\s+", " ") + "\" (" + job.getFileName() + " line " + (first + 1) + ")");
                }
            }
        }
        String readme = readme(name);
        if (!readme.equals("none")) out.add(readme);
        if (out.isEmpty()) out.add("none");
        return out;
    }

    /** The comment lines (an asterisk in column 7) directly above a line (`step` -1) or directly below it (+1), quoted with their file and first line, or null. */
    private static String comment(String file, int line, int step) throws Exception {
        Path path = Path.of(file);
        if (!Files.exists(path)) path = BatchReview.CARDDEMO.resolve("cpy").resolve(BatchReview.file(file));
        if (!Files.exists(path)) return null;
        List<String> lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.ISO_8859_1);
        List<String> said = new ArrayList<>();
        int first = 0;
        for (int k = line - 1 + step; k >= 0 && k < lines.size() && lines.get(k).length() > 6 && lines.get(k).charAt(6) == '*'; k += step) {
            String text = lines.get(k).substring(7, Math.min(72, lines.get(k).length())).replaceAll("[*-]{3,}", "").replaceAll("\\s*\\*+\\s*$", "").replace("|", "/").strip();
            if (text.isEmpty()) continue;
            if (step < 0) said.add(0, text);
            else said.add(text);
            if (step < 0 || first == 0) first = k + 1;
        }
        return said.isEmpty() ? null : "\"" + String.join(" ", said) + "\" (" + BatchReview.file(file) + " line " + first + ")";
    }
}
