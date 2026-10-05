package katana.sir.types;

/**
 * Where something in the IR came from (2.3's source map): a file, a line and a column, counted after
 * the file's tabs are expanded (R-016), and how the character got there. The IR's own type, so a module
 * that reads the IR (`lower/`) needs nothing of the front end (tithe pass 2, A2).
 */
public record Origin(String file, int line, int column, Kind kind) {
    /** The source's own character; put there by REPLACE or REPLACING; written by the front end. */
    public enum Kind { SOURCE, REPLACED, ADDED }

    @Override
    public String toString() {
        return file + ":" + line + ":" + column + (kind == Kind.SOURCE ? "" : " (" + kind.name().toLowerCase(java.util.Locale.ROOT) + ")");
    }
}
