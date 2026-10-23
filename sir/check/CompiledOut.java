package katana.sir.check;

import java.util.List;
import katana.findings.Finding;
import katana.front.Excluded;
import katana.front.Parse;

/**
 * The source a false `>>` condition excluded (R-081), as findings: one for each range, with the directive that
 * excluded it, so the proof report and a reviewer see the code the proof does not cover. The front end's parse
 * holds the ranges; the Semantic IR does not, the lines being gone before there is a program.
 */
public final class CompiledOut {
    private CompiledOut() {}

    // Decision: R-081
    public static List<Finding> findings(Parse.Result parse) {
        return parse.excluded().stream().map(CompiledOut::finding).toList();
    }

    private static Finding finding(Excluded e) {
        String lines = e.from() == e.to() ? "line " + e.from() : "lines " + e.from() + " to " + e.to();
        String where = (e.file().isEmpty() ? "" : e.file() + ":") + e.from() + ":1";
        return new Finding(Finding.Kind.CO_01, where, lines + " excluded by " + e.directive());
    }
}
