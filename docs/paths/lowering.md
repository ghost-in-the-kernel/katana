# Workpath: the runtime and the lowering (rt/, lower/)

Pointed to by CLAUDE.md (R-074). A new row for this path goes in this file's table, never in CLAUDE.md.

## Instructions

- Start at `lower/README.md`: the entry points and the per-program object graph, in its load-bearing order.
- `rt/` imports nothing of Katana's and holds no `MathContext` (R-020); arithmetic is Appendix A's (`rt/Decimal`).
- `lower/` writes what the IR and the `Plan` say and decides nothing (R-024, MOD-04); all Java text goes through
  `Emitter`, so every line keeps its provenance.
- The literal lowering is held by NIST and CardDemo through both lanes; a change selects the java lane and the
  passes in the gate. Read only its verdict and failures.

## Rows

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| The runtime: a record's bytes, arithmetic, MOVE, files | `rt/katana/rt/Field.java`, `rt/katana/rt/Decimal.java`, `rt/katana/rt/Arithmetic.java`, `rt/katana/rt/Move.java` | `docs/PROFILE.md`, R-019, R-020 | `test/katana/rt/`, `test/katana/rt/autotest/AutotestRtIntegrationTest.java` |
| The literal lowering: a statement to Java | `lower/README.md`, `lower/Lower.java` (a program's class), `lower/Storage.java` (its items), `lower/Verbs.java` (each verb) | `docs/SEMANTIC-IR.md`, R-024 | `test/katana/lower/`, `test/katana/lower/NistJavaLaneIntegrationTest.java` |
| A test that lowers a small program | `test/katana/support/Small.java`, `test/katana/support/Javac.java` | | `test/katana/lower/` |
