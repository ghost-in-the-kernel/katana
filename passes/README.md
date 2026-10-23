# passes/: the proven passes

Each pass proves a fact about one program and asks the lowering to write it otherwise; it refuses, with why,
what fails its obligations (R-032). A pass writes no Java: it fills a `Plan` (`lower/Plan.java`, MOD-04).
The map of every pass, its plan field, where the lowering writes it and its proof is `docs/PASSES.md`.

## Where to start

- `Passes.java`: the chain, `Passes.plan`, each pass in the order it needs the one before.
- `Structuring.java`, the first pass, with `Loops`, `Inline`, `MainLine` and `Cycles`: control flow (6.1, 6.2).
- `Scalars`, `Exact`, `Fits`, `Types`, `Records`, `Stateless` (with `Dataflow`): 6.3 to 6.7.
- `StructuringLedger.java`: the ledger's line for each symbol a pass adds or removes.
