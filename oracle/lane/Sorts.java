package katana.oracle.lane;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import katana.oracle.fixture.Fixture;

/**
 * A SORT step in the cobc lane (docs/ROADMAP.md 1.5): SYMNAMES and SYSIN read by SortCards, SORTIN's
 * records (each concatenated dataset in turn) selected, sorted on the keys with equal keys kept in
 * input order, rebuilt by OUTREC, and written to SORTOUT, whose record length must be what OUTREC
 * builds. Anything the cards ask that the lane does not do is a finding quoting it.
 */
final class Sorts {
    private Sorts() {}

    /** Runs a SORT step with the Java sort, or with the generated COBOL one when `cobol` holds the programs to build it with. */
    static Steps.Outcome run(Fixture.Step step, Steps.Bound bound, Datasets datasets, Report report, Programs cobol) {
        try {
            SortCards.Spec spec = SortCards.read(Utilities.cards(step, "SYMNAMES", bound.dsns(), datasets), Utilities.cards(step, "SYSIN", bound.dsns(), datasets));
            if (cobol != null) return generated(step, bound, datasets, report, spec, cobol);
            List<byte[]> records = input(bound, datasets);
            List<byte[]> kept = new ArrayList<>();
            for (byte[] r : records) if (spec.select() == null || spec.select().test(r) != spec.omit()) kept.add(r);
            Comparator<byte[]> order = order(spec.keys());
            kept.sort(order);
            for (int i = 1; i < kept.size(); i++) {
                if (order.compare(kept.get(i - 1), kept.get(i)) == 0) {
                    report.note("step " + step.name() + ": equal SORT keys kept in input order; DFSORT promises that only with EQUALS (R-026)");
                    break;
                }
            }
            return output(step, bound, datasets, report, spec, records.size(), kept);
        } catch (SortCards.Unsupported e) {
            return new Steps.Outcome("SORT not run", 16, false, "SORT: " + e.getMessage());
        }
    }

    /** The step through the generated COBOL sort: SORTIN unloaded to a sequential file, the program built and run. */
    private static Steps.Outcome generated(Fixture.Step step, Steps.Bound bound, Datasets datasets, Report report, SortCards.Spec spec, Programs cobol) {
        List<String> dsns = bound.inputs().get("SORTIN");
        if (dsns == null || dsns.size() != 1) throw new SortCards.Unsupported("the generated sort reads one SORTIN dataset");
        Datasets.Entry in = datasets.get(dsns.get(0));
        Datasets.Entry out = bound.dsns().containsKey("SORTOUT") ? datasets.get(bound.dsns().get("SORTOUT")) : null;
        if (out == null || !out.org().equals("PS") || out.lrecl() != in.lrecl()) throw new SortCards.Unsupported("the generated sort writes a sequential SORTOUT of SORTIN's lrecl");
        int exit = cobol.runGenerated("ksort-" + step.name(), SortCobol.source(spec, in.lrecl()), datasets.unload(in), out.path());
        if (exit != 0) return new Steps.Outcome("SORT rc=" + exit, exit, false, "the generated sort ended with exit " + exit);
        report.line("sort " + step.name() + " engine=cobol kept=" + datasets.unload(out).length / out.lrecl());
        return new Steps.Outcome("SORT rc=0", 0, false, null);
    }

    /** SORTIN's records: each dataset of the DD (and its concatenation) in order, all of one record length. */
    private static List<byte[]> input(Steps.Bound bound, Datasets datasets) {
        List<String> dsns = bound.inputs().get("SORTIN");
        if (dsns == null) throw new SortCards.Unsupported("no SORTIN dataset");
        List<byte[]> records = new ArrayList<>();
        int lrecl = -1;
        for (String dsn : dsns) {
            Datasets.Entry entry = datasets.get(dsn);
            if (lrecl >= 0 && entry.lrecl() != lrecl) throw new SortCards.Unsupported("SORTIN concatenates lrecl " + lrecl + " and " + entry.lrecl());
            lrecl = entry.lrecl();
            byte[] bytes = datasets.unload(entry);
            for (int at = 0; at + lrecl <= bytes.length; at += lrecl) records.add(Arrays.copyOfRange(bytes, at, at + lrecl));
        }
        return records;
    }

    /** The keys in order: CH by unsigned bytes, ZD by value; D reverses one. */
    private static Comparator<byte[]> order(List<SortCards.Key> keys) {
        Comparator<byte[]> order = (a, b) -> 0;
        for (SortCards.Key key : keys) {
            SortCards.Field f = key.field();
            Comparator<byte[]> one = f.format().equals("ZD") ? Comparator.comparing((byte[] r) -> Zoned.value(r, f))
                    : (a, b) -> Arrays.compareUnsigned(a, f.position() - 1, f.position() - 1 + f.length(), b, f.position() - 1, f.position() - 1 + f.length());
            order = order.thenComparing(key.ascending() ? one : one.reversed());
        }
        return order;
    }

    /** OUTREC applied, the length checked against SORTOUT's, and SORTOUT written. */
    private static Steps.Outcome output(Fixture.Step step, Steps.Bound bound, Datasets datasets, Report report, SortCards.Spec spec, int read, List<byte[]> kept) {
        Datasets.Entry out = bound.dsns().containsKey("SORTOUT") ? datasets.get(bound.dsns().get("SORTOUT")) : null;
        if (out == null || !out.org().equals("PS")) throw new SortCards.Unsupported("SORTOUT must be a sequential dataset");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (byte[] r : kept) {
            byte[] built = spec.outrec() == null ? r : SortOutrec.build(spec.outrec(), r);
            if (built.length != out.lrecl()) throw new SortCards.Unsupported("records of " + built.length + " bytes for SORTOUT's lrecl " + out.lrecl());
            bytes.writeBytes(built);
        }
        datasets.sequential(out.dsn(), out.lrecl(), bytes.toByteArray());
        report.line("sort " + step.name() + " read=" + read + " kept=" + kept.size());
        return new Steps.Outcome("SORT rc=0", 0, false, null);
    }
}
