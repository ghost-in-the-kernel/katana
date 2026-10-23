# The passes: what each proves, and where it is written

Phase 6's passes (`passes/`) prove facts about one program and hand the lowering (`lower/`) a plan,
`Plan` (`lower/Plan.java`); the lowering writes what the plan says and decides nothing (MOD-04). `passes/Structuring.plan`
runs them in this order, each refusing what fails its obligations with why (R-032): the refusals are in its
`Result.refused`, by `paragraph`, `item`, `record` or a statement's `VERB at line N`. The literal lowering is
`Plan.LITERAL`, every field empty. `test/PassesDocTest.java` holds this table to the code: every pass's id and
every field of the plan is named here, and every path exists.

| Pass (ledger id) | Proves, in `passes/` | Plan field(s) | Written by, in `lower/` | Research, decisions | Unit test | Journal |
| --- | --- | --- | --- | --- | --- | --- |
| `structuring-v1` | `passes/Structuring.java` (ranges as methods), `passes/Loops.java`, `passes/Inline.java`, `passes/MainLine.java`, `passes/Cycles.java` | `methods`, `falls`, `loops`, `inline`, `main`, `cycles` | `lower/Flow.java`, `lower/Regions.java`, `lower/Performs.java` | §35 to §38; R-032, R-033, R-034 | `test/katana/passes/StructuringTest.java` | J-210 to J-213 |
| `scalars-v1` | `passes/Scalars.java` | `scalars` | `lower/Promoted.java` | §39, §40; R-068 | `test/katana/passes/ScalarsTest.java` | J-215, J-216 |
| `exact-v1` | `passes/Exact.java` | `exact` | `lower/ArithVerbs.java`, `lower/Arith.java` (the guard `valid`, `lower/Lower.java`) | §41, §43; R-069 | `test/katana/passes/ExactTest.java` | J-217, J-219 |
| `fits-v1` | `passes/Fits.java` | `fits` | `lower/Arith.java` (`assign`) | §45; R-068 | `test/katana/passes/FitsTest.java` | J-222 |
| `types-v1` | `passes/Types.java` | `enums`, `money` | `lower/Promoted.java`, `lower/Conditions.java` | §42; R-070 | `test/katana/passes/TypesTest.java` | J-218 |
| `records-v1` | `passes/Records.java` | `records` | `lower/Views.java`, `lower/Programs.java` (LINKAGE) | §46, §47 | `test/katana/passes/RecordsTest.java` | J-223, J-224 |
| `stateless-v1` | `passes/Stateless.java`, `passes/Dataflow.java` | `stateless` | `lower/Programs.java` | §44 | `test/katana/passes/DataflowTest.java` | J-221 |

Research sections are in `docs/research/ibm-options.md`. A pass's ledger lines come from
`passes/StructuringLedger.java`: one per symbol a pass adds or removes, its pass found from the symbol's node.
Stateless and the statement rewrites (exact, fits) change no symbol, so they have no line.

## The proof every pass shares

Each pass is proven by the same runs, structured against literal: every NIST module
(`test/katana/lower/NistJavaLaneIntegrationTest.java`), every CardDemo job, its report and paragraph trace
(`test/katana/oracle/lane/StructuringCardDemoIntegrationTest.java`), and the ledger against the build
(`test/katana/passes/StructuringLedgerIntegrationTest.java`). The unit tests hold each obligation kept and broken
on small programs.

## Adding a pass

1. Research first: a section in `docs/research/ibm-options.md` with Ghost's choice and the prediction, committed
   before the build.
2. The pass in `passes/`: a `PASS` id, one public function returning what it proves, each refusal with why.
3. A field on `Plan` (`lower/Plan.java`, and `Plan.LITERAL`), set in `passes/Structuring.plan`.
4. The lowering reads the field where it writes the construct; nothing else in `lower/` decides.
5. Its ledger attribution in `passes/StructuringLedger.java` if it adds or removes a symbol.
6. A unit test in `test/katana/passes/`, a row here, a journal entry, then the proof runs and the gate.
