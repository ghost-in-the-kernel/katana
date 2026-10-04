package katana.oracle.fixture;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * A job fixture (docs/FIXTURE.md, docs/ROADMAP.md 1.3): a content-addressed description of a job as
 * the lanes run it: its steps, their DDs, the datasets that exist when it starts, its environment,
 * the stubs and modules it needs, and optional mainframe gold. Read from its text; what the text
 * says that the schema does not know is refused (FixtureCheck), never dropped.
 */
public record Fixture(List<Schema.Line> header, List<Schema.Line> datasets, List<Step> steps, List<String> refusals, String canonical) {
    /** A step and its DDs, in order. */
    public record Step(Schema.Line line, List<Dd> dds) {
        public String name() {
            return line.values().get(0);
        }
    }

    /** A DD; an inline DD carries its cards, each as written, trailing spaces dropped (a lane pads to 80). */
    public record Dd(Schema.Line line, List<String> cards) {
        public String name() {
            return line.values().get(0);
        }

        public Map<String, String> attributes() {
            return line.attributes();
        }
    }

    /** Reads a fixture; `refusals` holds what its lines got wrong against the schema (FIXTURE-01). */
    public static Fixture read(String text) {
        List<String> refusals = new ArrayList<>();
        List<Schema.Line> header = new ArrayList<>(), datasets = new ArrayList<>();
        List<Step> steps = new ArrayList<>();
        List<String> canonical = new ArrayList<>();
        List<String> lines = text.lines().toList();
        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            if (raw.isBlank() || raw.trim().startsWith("#")) continue;
            if (raw.startsWith("|")) {
                card(i + 1, raw, steps, refusals);
                canonical.add(raw.stripTrailing());
                continue;
            }
            Schema.Line line = Schema.line(i + 1, raw.trim(), refusals);
            canonical.add(canonical(line));
            place(line, header, datasets, steps, refusals);
        }
        return new Fixture(header, datasets, steps, refusals, String.join("\n", canonical) + "\n");
    }

    private static void place(Schema.Line line, List<Schema.Line> header, List<Schema.Line> datasets, List<Step> steps, List<String> refusals) {
        switch (line.keyword()) {
            case "dataset", "gold" -> datasets.add(line);
            case "step" -> steps.add(new Step(line, new ArrayList<>()));
            case "dd" -> {
                if (steps.isEmpty()) refusals.add("line " + line.number() + ": a dd before any step");
                else steps.get(steps.size() - 1).dds().add(new Dd(line, new ArrayList<>()));
            }
            default -> header.add(line);
        }
    }

    /** A `| ` line: one card of the inline DD just above it, or a refusal when there is none. */
    private static void card(int number, String raw, List<Step> steps, List<String> refusals) {
        List<Dd> dds = steps.isEmpty() ? List.of() : steps.get(steps.size() - 1).dds();
        Dd dd = dds.isEmpty() ? null : dds.get(dds.size() - 1);
        if (dd == null || !dd.line().words().contains("inline")) {
            refusals.add("line " + number + ": a card that does not follow an inline dd");
        } else if (raw.length() > 1 && raw.charAt(1) != ' ') {
            refusals.add("line " + number + ": a card is '| ' and then its text");
        } else {
            dd.cards().add(raw.length() > 2 ? raw.substring(2).stripTrailing() : "");
        }
    }

    /** A line as the hash sees it: keyword, values, attributes and words, single-spaced, quoted where needed. */
    private static String canonical(Schema.Line line) {
        Schema.Spec spec = Schema.SPECS.get(line.keyword());
        if (spec == null) return line.keyword();
        if (spec.positional() < 0) return line.keyword() + " " + line.values().get(0).replaceAll("\\s+", " ");
        List<String> tokens = new ArrayList<>(List.of(line.keyword()));
        line.values().forEach(v -> tokens.add(quoted(v)));
        for (String name : spec.attributes()) if (line.attributes().containsKey(name)) tokens.add(quoted(name + "=" + line.attributes().get(name)));
        for (String word : spec.words()) if (line.words().contains(word)) tokens.add(word);
        return String.join(" ", tokens);
    }

    private static String quoted(String token) {
        return token.contains(" ") || token.contains("'") ? "'" + token.replace("'", "''") + "'" : token;
    }

    /** The first header line with this keyword, or null. */
    public Schema.Line first(String keyword) {
        return header.stream().filter(l -> l.keyword().equals(keyword)).findFirst().orElse(null);
    }

    /** "sha256:" and the hex SHA-256 of the canonical text: the fixture's address (FIXTURE-02). */
    public String hash() {
        return sha256(canonical.getBytes(StandardCharsets.UTF_8));
    }

    static String sha256(byte[] bytes) {
        try {
            return "sha256:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
