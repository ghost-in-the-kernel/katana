package katana.intent.types;

import java.util.List;

/** Why a fact is held: the rule that inferred it, at its version, and the IR nodes it read (INT-03). A person's own fact names the rule `person`. */
public record Evidence(String rule, int version, List<Node> nodes) {
    public Evidence {
        nodes = List.copyOf(nodes);
        if (rule == null || rule.isBlank() || version < 1 || nodes.isEmpty()) throw new Invalid("INT-03", "rule '" + rule + "' version " + version + " with " + nodes.size() + " nodes");
    }
}
