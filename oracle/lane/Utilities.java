package katana.oracle.lane;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.oracle.fixture.Fixture;

/** IEBGENER, and the cards a utility reads from SYSIN (inline, or a dataset of 80-byte records). */
final class Utilities {
    private Utilities() {}

    /** Inline cards as a dataset: each padded with spaces to 80 bytes. */
    static byte[] cards(List<String> cards) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String card : cards) out.writeBytes(String.format("%-80s", card).getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }

    /** A utility's SYSIN as cards: the inline DD's, or the records of the dataset it names. */
    static List<String> sysin(Fixture.Step step, Map<String, String> dsns, Datasets datasets) {
        for (Fixture.Dd dd : step.dds()) {
            if (!dd.name().equals("SYSIN")) continue;
            if (dd.line().words().contains("inline")) return dd.cards();
            Datasets.Entry entry = dsns.containsKey("SYSIN") ? datasets.get(dsns.get("SYSIN")) : null;
            if (entry == null) return List.of();
            String text = new String(datasets.unload(entry), StandardCharsets.ISO_8859_1);
            List<String> cards = new ArrayList<>();
            for (int at = 0; at + entry.lrecl() <= text.length(); at += entry.lrecl()) cards.add(text.substring(at, at + entry.lrecl()).stripTrailing());
            return cards;
        }
        return List.of();
    }

    /** IEBGENER with no control statements (SYSIN DUMMY or absent): SYSUT1's records copied to SYSUT2. */
    static Steps.Outcome iebgener(Fixture.Step step, Map<String, String> dsns, Datasets datasets) {
        boolean statements = step.dds().stream().anyMatch(dd -> dd.name().equals("SYSIN") && !dd.line().words().contains("dummy"));
        Datasets.Entry in = dsns.containsKey("SYSUT1") ? datasets.get(dsns.get("SYSUT1")) : null;
        Datasets.Entry out = dsns.containsKey("SYSUT2") ? datasets.get(dsns.get("SYSUT2")) : null;
        String finding = statements ? "IEBGENER control statements are not in the lane"
                : in == null || out == null ? "IEBGENER needs SYSUT1 and SYSUT2 datasets"
                : !out.org().equals("PS") || in.lrecl() != out.lrecl() ? "IEBGENER copies to a sequential dataset of the same lrecl only" : null;
        if (finding != null) return new Steps.Outcome("IEBGENER not run", 0, false, finding);
        datasets.sequential(out.dsn(), out.lrecl(), datasets.unload(in));
        return new Steps.Outcome("IEBGENER rc=0", 0, false, null);
    }
}
