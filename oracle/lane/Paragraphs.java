package katana.oracle.lane;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A program's paragraphs and sections, and which of them a cobc trace entered (docs/ROADMAP.md 1.9):
 * the names written in area A of the PROCEDURE DIVISION, and the `Paragraph:` and `Section:` lines of
 * `-ftrace` with COB_SET_TRACE (Program-Id, then the name).
 */
// Unsure: R-053 default: the lane reads paragraph names itself until phase 2's front end gives the paragraph graph (HANDOFF "For Fable").
final class Paragraphs {
    private static final Pattern NAME = Pattern.compile("^ {7} {0,3}([A-Z0-9][A-Z0-9-]*)( SECTION)?\\s*\\.\\s*$");
    private static final Pattern TRACED = Pattern.compile("^Program-Id:\\s+(\\S+)\\s+(?:Paragraph|Section):\\s+(\\S+)");

    private Paragraphs() {}

    /** The paragraph and section names of a fixed-form source, in order, after PROCEDURE DIVISION. */
    static List<String> of(String source) {
        List<String> names = new ArrayList<>();
        boolean procedure = false;
        for (String line : source.split("\r?\n")) {
            String code = line.length() > 72 ? line.substring(0, 72) : line;
            if (code.length() > 6 && (code.charAt(6) == '*' || code.charAt(6) == '/')) continue;
            if (code.matches("^.{7}\\s*PROCEDURE\\s+DIVISION.*")) procedure = true;
            Matcher m = NAME.matcher(code.length() > 6 ? "       " + code.substring(7) : code);
            if (procedure && m.matches() && !List.of("DECLARATIVES", "END").contains(m.group(1))) names.add(m.group(1));
        }
        return names;
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
