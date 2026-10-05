package katana.oracle.lane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import katana.oracle.fixture.Fixture;
import katana.oracle.sort.Dfsort;

/**
 * A SORT step in the cobc lane (docs/ROADMAP.md 1.5): SYMNAMES and SYSIN as cards, SORTIN's records
 * (each concatenated dataset in turn), Katana's DFSORT over them (R-010) under the fixture's PAD and
 * TRUNC (R-051), and SORTOUT written. What the sort chose for the program is a note; a card it does not
 * run stops the job with the card quoted.
 */
final class Sorts {
    private Sorts() {}

    static Steps.Outcome run(Fixture.Step step, Steps.Bound bound, Datasets datasets, Report report, Dfsort.Rules rules) {
        List<String> dsns = bound.inputs().get("SORTIN");
        Datasets.Entry out = bound.dsns().containsKey("SORTOUT") ? datasets.get(bound.dsns().get("SORTOUT")) : null;
        String refusal = dsns == null ? "no SORTIN dataset" : out == null || !out.org().equals("PS") ? "SORTOUT must be a sequential dataset" : null;
        List<byte[]> records = refusal == null ? new ArrayList<>() : List.of();
        if (refusal == null) refusal = input(dsns, datasets, records);
        if (refusal != null) return new Steps.Outcome("SORT not run", 16, false, "SORT: " + refusal);
        Dfsort.Result result = Dfsort.run(Utilities.cards(step, "SYMNAMES", bound.dsns(), datasets), Utilities.cards(step, "SYSIN", bound.dsns(), datasets),
                records, out.lrecl(), rules);
        result.notes().forEach(n -> report.note("step " + step.name() + ": " + n));
        if (result.stop() != null) return new Steps.Outcome("SORT not run", result.rc(), false, "SORT: " + result.stop());
        datasets.sequential(out.dsn(), out.lrecl(), result.records());
        report.line("sort " + step.name() + " read=" + result.read() + " kept=" + result.kept());
        return new Steps.Outcome("SORT rc=" + result.rc(), result.rc(), false, null);
    }

    /** SORTIN's records into records: each dataset of the DD and its concatenation in order, of one record length; why not, or null. */
    private static String input(List<String> dsns, Datasets datasets, List<byte[]> records) {
        int lrecl = -1;
        for (String dsn : dsns) {
            Datasets.Entry entry = datasets.get(dsn);
            if (lrecl >= 0 && entry.lrecl() != lrecl) return "SORTIN concatenates lrecl " + lrecl + " and " + entry.lrecl();
            lrecl = entry.lrecl();
            byte[] bytes = datasets.unload(entry);
            for (int at = 0; at + lrecl <= bytes.length; at += lrecl) records.add(Arrays.copyOfRange(bytes, at, at + lrecl));
        }
        return null;
    }
}
