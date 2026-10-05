package katana.sir.types;

import java.util.List;

/**
 * A statement as an operation over fields: its verb, its operands each with the role it plays (MOVE's
 * `from` and `to`, ADD's `addend` and `giving`), its phrases' branches (IF's `then` and `else`, READ's
 * `at-end` and `not-at-end`, an inline PERFORM's `body`), each with a condition when it has one, and
 * its origin. A PERFORM, GO TO or ALTER names its targets as paragraph indexes in its operands' roles
 * and as edges of the paragraph graph.
 */
public record Statement(String verb, List<Operand> operands, List<Branch> branches, Origin origin) {
    /** An operand and its role. */
    public record Operand(String role, Value value) {}

    /** A phrase's statements, with its condition (null for none). */
    public record Branch(String label, Value condition, List<Statement> statements) {
        public Branch {
            statements = List.copyOf(statements);
        }
    }

    public Statement {
        operands = List.copyOf(operands);
        branches = List.copyOf(branches);
        Invalid.origin(origin, verb);
    }
}
