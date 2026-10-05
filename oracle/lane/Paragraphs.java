package katana.oracle.lane;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Procedure;
import katana.sir.types.Program;

/**
 * A program's paragraphs and sections, and which of them a cobc trace entered (docs/ROADMAP.md 1.9):
 * the names in its paragraph graph (sir/, those a COPY brings in too; tithe pass 2, B1), and the
 * `Paragraph:` and `Section:` lines of `-ftrace` with COB_SET_TRACE (Program-Id, then the name).
 */
final class Paragraphs {
    private static final Pattern TRACED = Pattern.compile("^Program-Id:\\s+(\\S+)\\s+(?:Paragraph|Section):\\s+(\\S+)");

    private Paragraphs() {}

    /** A profile hash for building the paragraph graph alone: the graph does not depend on the profile. */
    private static final String ANY_PROFILE = "sha256:" + "0".repeat(64);

    /** The section and paragraph names of a program and those nested in it, from the IR's paragraph graph, in textual order. */
    static List<String> of(Path source, List<Path> copybooks, ToIntFunction<String> widths) {
        Parse.Result parse = Parse.file(source, new Copybooks(copybooks, widths), widths.applyAsInt(source.getFileName().toString()));
        if (!parse.clean()) throw new IllegalStateException(source + " does not parse: " + parse.errors().get(0));
        List<String> names = new ArrayList<>();
        for (Build.Result r : Build.programs(parse, ANY_PROFILE)) names(r.program(), names);
        return names;
    }

    private static void names(Program p, List<String> names) {
        Set<String> sections = new LinkedHashSet<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) {
            if (!g.section().isEmpty() && sections.add(g.section())) names.add(g.section());
            if (!g.name().isEmpty()) names.add(g.name());
        }
        for (Program n : p.nested()) names(n, names);
    }

    /** The names a trace shows a program entered. */
    static Set<String> entered(String trace, String program) {
        Set<String> names = new LinkedHashSet<>();
        for (String line : trace.split("\r?\n")) {
            Matcher m = TRACED.matcher(line);
            if (m.find() && m.group(1).equals(program)) names.add(m.group(2));
        }
        return names;
    }
}
