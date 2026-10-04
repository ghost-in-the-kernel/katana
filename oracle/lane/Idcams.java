package katana.oracle.lane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * IDCAMS as far as CardDemo's jobs use it (docs/ROADMAP.md 1.4): DELETE, DEFINE CLUSTER (an indexed
 * cluster), REPRO between DDs, and SET MAXCC/LASTCC, read from its control cards (columns 2 to 72,
 * comments, `-` and `+` continuation). Each command's condition code follows IDCAMS (0, 8 for an
 * entry not found, 12 for a failure); the step's return code is the highest. A command, a keyword
 * or a case outside this is a finding that stops the job, never a guess.
 */
final class Idcams {
    /** A word, or a word with a parenthesized list: `KEYS(32 0)`. */
    record Node(String word, List<Node> list) {
        String value() {
            return list == null || list.isEmpty() ? "" : String.join(" ", list.stream().map(Node::word).toList());
        }
    }

    /** What the step did: its return code, a line per command, and the finding that stopped it, or null. */
    record Result(int rc, List<String> lines, String finding) {}

    /** DEFINE keywords that place or size a cluster on a volume and change nothing in its records. */
    private static final Set<String> PLACEMENT = Set.of("VOLUMES", "SHAREOPTIONS", "ERASE", "NOERASE", "CYL", "CYLINDERS",
            "TRACKS", "TRK", "RECORDS", "CISZ", "CONTROLINTERVALSIZE", "FREESPACE", "SPEED", "RECOVERY", "UNIQUE", "SUBALLOCATION");
    private static final Set<String> DELETE_WORDS = Set.of("CLUSTER", "PURGE", "NOPURGE", "NONVSAM", "SCRATCH", "ERASE", "NOERASE");

    private final Datasets datasets;
    private final Map<String, String> dds;
    private int lastcc;
    private int maxcc;

    /** IDCAMS over a catalog, with the step's DDs by name (each a dataset's name). */
    Idcams(Datasets datasets, Map<String, String> dds) {
        this.datasets = datasets;
        this.dds = dds;
    }

    /** Runs the commands in order; a card that opens a comment in column 1 is a finding, since column 1 is not read. */
    Result run(List<String> cards) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).startsWith("/*")) {
                return new Result(12, lines, "IDCAMS: card " + (i + 1) + " opens a comment in column 1, which IDCAMS does not read"
                        + " (columns 2 to 72), so its text is not a comment");
            }
        }
        for (String command : commands(cards)) {
            List<Node> nodes = nodes(command);
            String finding = command(nodes, lines);
            if (finding != null) return new Result(Math.max(maxcc, 12), lines, "IDCAMS " + nodes.get(0).word() + ": " + finding);
        }
        return new Result(maxcc, lines, null);
    }

    /** One command; null when it ran (its condition code set), else why the lane cannot run it. */
    private String command(List<Node> nodes, List<String> lines) {
        String verb = nodes.get(0).word();
        String finding = switch (verb) {
            case "DELETE" -> delete(nodes);
            case "DEFINE" -> define(nodes);
            case "REPRO" -> repro(nodes);
            case "SET" -> set(nodes);
            default -> "not a command the lane's IDCAMS knows";
        };
        if (finding == null && !verb.equals("SET")) {
            maxcc = Math.max(maxcc, lastcc);
            Node object = nodes.size() > 1 ? nodes.get(1) : new Node("", null);
            lines.add(verb + " " + object.word() + (object.list() == null || object.word().equals("CLUSTER") ? "" : "(" + object.value() + ")") + " cc=" + lastcc);
        }
        return finding;
    }

    private String delete(List<Node> nodes) {
        if (nodes.size() < 2) return "DELETE needs a name";
        for (Node n : nodes.subList(2, nodes.size())) if (!DELETE_WORDS.contains(n.word()) || n.list() != null) return "no " + n.word();
        lastcc = datasets.delete(nodes.get(1).word()) ? 0 : 8;
        return null;
    }

    private String set(List<Node> nodes) {
        if (nodes.size() != 4 || !nodes.get(2).word().equals("=") || !nodes.get(3).word().matches("\\d+")) return "SET is MAXCC|LASTCC = n";
        int value = Integer.parseInt(nodes.get(3).word());
        if (nodes.get(1).word().equals("MAXCC")) maxcc = value;
        else if (nodes.get(1).word().equals("LASTCC")) lastcc = value;
        else return "no " + nodes.get(1).word();
        return null;
    }

    /** DEFINE CLUSTER (... INDEXED KEYS RECORDSIZE ...) [DATA(...)] [INDEX(...)]: an empty indexed dataset. */
    private String define(List<Node> nodes) {
        Node cluster = nodes.size() > 1 ? nodes.get(1) : null;
        if (cluster == null || !cluster.word().equals("CLUSTER") || cluster.list() == null) return "only DEFINE CLUSTER";
        for (Node n : nodes.subList(2, nodes.size())) if (!n.word().equals("DATA") && !n.word().equals("INDEX")) return "no " + n.word();
        Map<String, String> fields = new java.util.HashMap<>();
        String finding = cluster(cluster, fields);
        if (finding != null) return finding;
        String[] r = fields.get("RECORDSIZE").split(" "), k = fields.get("KEYS").split(" ");
        if (r.length != 2 || !r[0].equals(r[1])) return "RECORDSIZE(" + fields.get("RECORDSIZE") + "): only fixed-length records";
        if (datasets.get(fields.get("NAME")) != null) {
            lastcc = 12;
            return null;
        }
        Generated.Shape shape = new Generated.Shape(Integer.parseInt(r[0]), new Generated.Key(Integer.parseInt(k[0]), Integer.parseInt(k[1]), true), List.of());
        lastcc = datasets.indexed(fields.get("NAME"), shape, new byte[0]).isEmpty() ? 0 : 12;
        return null;
    }

    /** The cluster's NAME, KEYS and RECORDSIZE into fields; it must be INDEXED, and any other keyword only placement. */
    private static String cluster(Node cluster, Map<String, String> fields) {
        boolean indexed = false;
        for (Node n : cluster.list()) {
            if (List.of("NAME", "KEYS", "RECORDSIZE").contains(n.word())) fields.put(n.word(), n.value());
            else if (n.word().equals("INDEXED")) indexed = true;
            else if (!PLACEMENT.contains(n.word())) return "no " + n.word();
        }
        return indexed && fields.size() == 3 ? null : "a cluster here is INDEXED with NAME, KEYS and RECORDSIZE";
    }

    /** REPRO INFILE(dd) OUTFILE(dd): the records of one into the other, an indexed one in key order. */
    private String repro(List<Node> nodes) {
        String in = null, out = null;
        for (Node n : nodes.subList(1, nodes.size())) {
            if (n.word().equals("INFILE")) in = n.value();
            else if (n.word().equals("OUTFILE")) out = n.value();
            else return "no " + n.word();
        }
        Datasets.Entry from = in == null || !dds.containsKey(in) ? null : datasets.get(dds.get(in));
        Datasets.Entry to = out == null || !dds.containsKey(out) ? null : datasets.get(dds.get(out));
        if (from == null || to == null) return "INFILE and OUTFILE name DDs of this step that hold datasets";
        if (from.lrecl() != to.lrecl()) return "from lrecl " + from.lrecl() + " to " + to.lrecl();
        byte[] records = datasets.unload(from);
        if (to.org().equals("PS")) {
            datasets.sequential(to.dsn(), to.lrecl(), records);
            lastcc = 0;
        } else if (datasets.unload(to).length > 0) {
            return "into an indexed dataset that holds records: merging is not in the lane yet";
        } else {
            lastcc = datasets.load(to, records).isEmpty() ? 0 : 12;
        }
        return null;
    }

    /** The commands in the cards: columns 2 to 72, comments dropped, continuations joined. */
    static List<String> commands(List<String> cards) {
        StringBuilder text = new StringBuilder();
        for (String card : cards) {
            String body = card.length() <= 1 ? "" : card.substring(1, Math.min(72, card.length()));
            text.append(body.stripTrailing()).append('\n');
        }
        String clean = text.toString().replaceAll("(?s)/\\*.*?\\*/", " ");
        List<String> commands = new ArrayList<>();
        StringBuilder command = new StringBuilder();
        for (String line : clean.split("\n")) {
            String trimmed = line.stripTrailing();
            boolean more = trimmed.endsWith("-") || trimmed.endsWith("+");
            command.append(more ? trimmed.substring(0, trimmed.length() - 1) : trimmed).append(' ');
            if (!more && !command.toString().isBlank()) {
                commands.add(command.toString().trim());
                command.setLength(0);
            }
        }
        return commands;
    }

    /** A command's words and parenthesized lists, nested, `=` a word of its own. */
    static List<Node> nodes(String command) {
        int[] at = {0};
        return list(command.replace("=", " = ").replace(",", " "), at);
    }

    /** The nodes up to the closing parenthesis; a list written after a space belongs to the word before it. */
    private static List<Node> list(String text, int[] at) {
        List<Node> nodes = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        while (at[0] < text.length()) {
            char c = text.charAt(at[0]++);
            if (c == '(' && word.isEmpty() && !nodes.isEmpty() && nodes.get(nodes.size() - 1).list() == null) {
                nodes.set(nodes.size() - 1, new Node(nodes.get(nodes.size() - 1).word(), list(text, at)));
            } else if (c == '(') {
                nodes.add(new Node(word.toString(), list(text, at)));
                word.setLength(0);
            } else if (c == ')') {
                break;
            } else if (c == ' ') {
                if (!word.isEmpty()) nodes.add(new Node(word.toString(), null));
                word.setLength(0);
            } else {
                word.append(c);
            }
        }
        if (!word.isEmpty()) nodes.add(new Node(word.toString(), null));
        return nodes;
    }
}
