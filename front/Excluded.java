package katana.front;

/** Lines `from` to `to` (1-based, in `file`) that a false `>>` condition excluded from the program, and the directive that did (R-081). */
public record Excluded(String file, int from, int to, String directive) {}
