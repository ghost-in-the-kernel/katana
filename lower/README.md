# lower/: a program's Semantic IR to one Java class

The literal lowering (R-024) and the structured one a `Plan` asks for. It decides nothing (MOD-04): every branch
on a COBOL property is in `sir/` or `intent/`, and a pass speaks to it only through `Plan`. Tests:
`test/katana/lower/`; the pass contract: `docs/PASSES.md`.

## Where to start

- `Lower.java`: the entry, `Lower.java(program, className, plan)`, and a program's class skeleton.
- `Plan.java`: what a pass asks (and `Plan.THROUGH`); `Plan.LITERAL` asks nothing.
- `Storage.java`: each item's member, its record and its attributes.
- `Verbs.java`: a statement to its verb's class (`ArithVerbs`, `FileVerbs`, `TextVerbs`, `CallVerbs`, ...).
- `Exprs.java`: an operand as Java; `Emitter.java`, the one place text is written.

## The object graph, per program

Built in `Lower.program`, in this order. The order is load-bearing (J-235, J-241):

1. `new Exprs(p, findings, plan, taken)` builds the per-program state:
   - first `Promoted`, so the promoted items take their names before anything else;
   - then `Views`, the record classes, which take their names from what is left and reach back through the Exprs.
   `Promoted.views()` refuses a read before then; a literal build has an empty `Views`, never null.
2. The class's members, `Storage.declareItems` among them, and its constructor; then `Storage.defineItems`.
3. `new Flow(e, p, x, containers, plan)` holds the cursor (`pc`, the regions) and builds the verb classes:
   - `Arith` and `Conditions` over the Exprs;
   - `Verbs`, which holds each verb's class and reaches all of it through `Flow` (`f.e`, `f.x`, `f.x.promoted`).
4. The procedure, through `Flow`.
5. Last, what the procedure asked for: the predicates (`Promoted.predicates`) and the guard (`x.guarded`).

`Verbs.statement` gives `Promoted.intercept` the first word on a statement; then each verb's class writes it.

## What not to do here

- No subpackages: package-private access is how these classes share state.
- No interface in place of `Flow` in the verb classes.
