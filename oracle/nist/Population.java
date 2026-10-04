package katana.oracle.nist;

import java.util.ArrayList;
import java.util.List;

/**
 * A population file, NIST CCVS85's format for holding many sources in one (CCVS85 User Guide 4.2,
 * 3.1 and 3.5): each member runs from `*HEADER,kind,name` to `*END-OF,name`, where kind is COBOL (a
 * program), CLBRY (a copy library member) or DATA* (a data file). A subprogram's header names the
 * main program first, `*HEADER,COBOL,IC101A,SUBPRG,IC102A` (or SUBRTN), and is closed by its own
 * name. Cards are read in columns 1 to 72, the text area; two headers carry sequence text past it.
 * EXEC85's output holds the same members with both cards indented six places into comments (3.5.3).
 */
public final class Population {
    /**
     * One member: its kind, its name, the main program it belongs to (its own name if it is one), its
     * role under that program ("" for a main, SUBPRG for a program run after it, SUBRTN for one it
     * CALLs), and its lines.
     */
    public record Member(String kind, String name, String owner, String role, List<String> lines) {}

    private static final String HEADER = "*HEADER,";
    private static final String END = "*END-OF,";

    private Population() {}

    /** The members in file order; text outside any member (the file's first card) is not one. */
    public static List<Member> read(List<String> lines) {
        List<Member> members = new ArrayList<>();
        String[] header = null;
        List<String> body = null;
        for (String line : lines) {
            String card = card(line);
            if (card.startsWith(HEADER)) {
                if (header != null) throw new IllegalArgumentException("*HEADER " + header[1] + " has no *END-OF before " + card.trim());
                header = card.substring(HEADER.length()).trim().split(",");
                if (header.length != 2 && header.length != 4) throw new IllegalArgumentException("an unknown header: " + card.trim());
                body = new ArrayList<>();
            } else if (card.startsWith(END)) {
                String name = card.substring(END.length()).trim();
                if (header == null || !header[header.length - 1].equals(name)) throw new IllegalArgumentException("*END-OF," + name + " closes no open member");
                members.add(new Member(header[0], name, header[1], header.length == 4 ? header[2] : "", List.copyOf(body)));
                header = null;
            } else if (header != null) {
                body.add(line);
            }
        }
        if (header != null) throw new IllegalArgumentException("*HEADER " + header[1] + " is never closed");
        return members;
    }

    /** A line's text area, columns 1 to 72, with a card indented into a comment by EXEC85 brought to column 1. */
    private static String card(String line) {
        String text = line.length() > 72 ? line.substring(0, 72) : line;
        return text.startsWith("      *HEADER,") || text.startsWith("      *END-OF,") ? text.substring(6) : text;
    }
}
