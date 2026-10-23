# Workpath: intent and the passes (intent/, passes/, ledger/)

Pointed to by CLAUDE.md (R-074). A new row for this path goes in this file's table, never in CLAUDE.md.

## Instructions

- `docs/PASSES.md` is the map: each pass, its plan field, where the lowering writes it, its proof.
- A pass decides and refuses with why (R-032); it speaks to `lower/` only through `Plan` (`lower/Plan.java`).
  The chain, in its order, is `passes/Passes.java`.
- A fact no person confirmed may license a pass, and its ledger line says so (R-029).
- A pass is proven by structured equal to literal on NIST and CardDemo; the gate's `passes` suite runs it.

- A new integration or golden test also gets its row in `docs/checks.json` (R-056): the oracle, why it is
  independent, a mutation test that exists, and `gates`, which takes a pre-push suite name (as `scripts/prepush.sh`
  lists them: `nist`, `fixture`, `lane`, `front`, `sir`, `autotest`, `rt`, `javalane`, `intent`, `ledger`,
  `passes`) or `fast`. The fast loop's `DocTests > checks()` fails until the row is there.

## Rows

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| The Intent IR: what a program means, as facts with evidence and a state | `intent/types/Fact.java`, `intent/types/Intent.java`, `intent/rules/Rules.java`, `intent/review/Review.java` | `docs/INTENT-IR.md` (INT-01 onward), R-029, R-064 | `test/katana/intent/IntentDocTest.java`, `test/katana/intent/IntentTypesTest.java` |
| A pass: what it proves and where the lowering writes it | `docs/PASSES.md` (its row), `passes/Passes.java` (the order), the pass's file | `docs/PASSES.md`, R-032 | its row's unit test; `test/katana/passes/StructuringLedgerIntegrationTest.java` and the structured runs |
| What a pass writes in `lower/`: a numeric item as a Java variable, a record as a class, exact arithmetic | `docs/PASSES.md` (its row's "Written by"), then that file | `docs/PASSES.md` | its row's unit test |
| The proof ledger: what was done to the Java and why, and the provenance map | `ledger/Ledger.java`, `ledger/Entry.java`, `ledger/ProvenanceMap.java`, `lower/Lower.java` (its manifest) | `docs/LEDGER.md` (LED-01 onward), R-030 | `test/katana/ledger/LedgerTest.java`, `test/katana/ledger/LiteralLedgerIntegrationTest.java` |
