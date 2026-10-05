package katana.oracle.nist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The run notes (oracle/nist/run-notes): what NIST says about running particular programs, read as
 * data so each condition sits beside the page that states it. A program's own line overrides its
 * module's ("DB*").
 */
public final class RunNotes {
    /** How one program is run: the debugging switch for each run (one entry per run), and files to remove first. */
    public record Note(List<String> debug, List<String> absent) {
        static final Note NONE = new Note(List.of(""), List.of());
    }

    private final Map<String, Note> notes;

    private RunNotes(Map<String, Note> notes) {
        this.notes = notes;
    }

    /** The run notes file: one note a line, `#` to the end a comment. */
    public static RunNotes read(String text) {
        Map<String, Note> notes = new HashMap<>();
        for (String line : text.lines().toList()) {
            String body = line.contains("#") ? line.substring(0, line.indexOf('#')) : line;
            String[] fields = body.trim().split("\\s+");
            if (fields[0].isEmpty()) continue;
            List<String> debug = List.of("");
            List<String> absent = new ArrayList<>();
            for (int i = 1; i < fields.length; i++) {
                String[] setting = fields[i].split("=", 2);
                if (setting.length != 2) throw new IllegalArgumentException("not name=value: " + fields[i] + " in " + line);
                switch (setting[0]) {
                    case "debug" -> debug = List.of(setting[1].split(","));
                    case "absent" -> absent.addAll(List.of(setting[1].split(",")));
                    default -> throw new IllegalArgumentException("unknown setting " + setting[0] + " in " + line);
                }
            }
            notes.put(fields[0], new Note(debug, List.copyOf(absent)));
        }
        return new RunNotes(notes);
    }

    /** The note for a program: its own line, else its module's ("DB*"), else none. */
    public Note of(String program, String module) {
        return notes.getOrDefault(program, notes.getOrDefault(module + "*", Note.NONE));
    }
}
