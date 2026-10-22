# Readability, maintainability and reentrancy for an AI agent: Fable's advice (2026-10-22)

A Fable advisor wrote this, read-only, on Ghost's word ("spin up a fable advisor to ask for ways to make the codebase
more readable, and more maintainable for an ai agent, and lower reentrancy issues"). It had the Haiku navigation
trial's friction list as input. It is a model's reading (R-029, R-031); each item becomes work only when Ghost places
it.

**Opus checked these claims against the code (2026-10-22):**
- Confirmed:
  - HANDOFF.md lines 514–613 and 631–730 are identical (the same md5).
  - The `THROUGH` set is duplicated in `lower/Verbs.java:51` and `passes/Scalars.java:30`.
  - RANDOM's seed is a static in `rt/katana/rt/Floats.java:124-125`.
  - `Lower.compiled()` (`lower/Lower.java:357`) reads the clock for WHEN-COMPILED.
- Corrected: the Scalars refusal the advisor cites at line 586 is at `passes/Scalars.java:98`.

## A. Agent re-entry, ranked

1. **HANDOFF.md is an append-only mailbox, partly stale and duplicated.**
   - It is 860 lines, and one 100-line section is pasted twice.
   - Its head still says phase 6 "starts after the boundary".
   - Done items stay in it.
   - *Change:* open asks only, at most about 80 lines, each naming a TODO item or R-id; answered sections move to
     JOURNAL or DONE; a duplicate-paragraph test. Small.
2. **Ids are taken by eye; a collision is caught only after the merge.**
   - Two sessions can take the same R or J number (as R-069 did).
   - 13 id schemes exist, and nothing says which file defines each prefix.
   - *Change:* a `scripts/NextId.java` that fetches main and prints the next free R and J; a GLOSSARY row per
     prefix. Small.
3. **The tithe cadence is stated three ways.**
   - CLAUDE.md says every eighth commit.
   - `scripts/Metrics.java` warns past 8.
   - `scripts/TitheGate.java` allows `GRACE = 16`.
   - *Change:* Metrics reads `TitheGate.GRACE`; CLAUDE.md says "one in eight; the gate refuses past 16". Small.
4. **docs/AGENT.md gives the fast loop's ceiling as both 20 s and 25 s.** *Change:* one number in
   `scripts/Ceilings.java`, quoted into both by MetricsTest. Small.
5. **TODO.md carries status as prose.**
   - "Open" is an item's absence from DONE.md.
   - 3.1's check says 359 autotests and its body 378.
   - *Change:* a `Status:` line per item held by TodoTest; counts quoted from the golden. Medium.
6. **No routing row for what a pass writes in lower/.** *Change:* a CLAUDE.md row pointing at PASSES.md's
   "Written by" column; `lower/Promoted.java`'s first line saying it declares, reads and stores a promoted item.
   Small.
7. **CardDemo program lists are hard-coded in tests.** *Change:* derived from `oracle/fixture/carddemo/`, with a
   doc test. Small.
8. **No "state at HEAD" page.** *Change:* a short `docs/STATE.md` written by `gradle metrics` (latest R and J, what
   is built, what is red), with a staleness test. Medium.

## B. Code reentrancy, ranked

Lowering two programs at once is safe today except for items 6 and 7. Recursive re-entry of a statement is
prevented by the passes, not the code.

1. **`Promoted.bound`, toggled around a statement** (`addAll`, run, `removeAll`, with no try/finally).
   - It does not nest: an inner `through` sharing an item unbinds the outer's.
   - It is safe only because Scalars refuses a statement with ON OVERFLOW or ON EXCEPTION statements, an invariant
     two modules away.
   - *Change:* try/finally, a throw on nested entry, and one shared `THROUGH` constant. Latent; small.
2. **The three-phase construction Exprs → Promoted → Views.**
   - `Exprs.promoted` is replaced after construction.
   - `Promoted.views` stays null until set.
   - `Views` takes names through `Promoted.reserve`, so every generated name depends on construction order.
   - *Change:* one factory building an immutable name table. Latent; medium.
3. **`Dataflow.initializing`, a field read inside `target()`.** *Change:* a parameter. Latent; small.
4. **Flow's cursor state, and `Exprs.guarded`.** `guard()` is written only if the procedure was lowered first.
   *Change:* `Flow.procedure()` returns what the class needs; Lower.program's emission order written as a numbered
   list. Latent; small.
5. **Pass order is positional** in `passes/Structuring.plan`: Fits needs Exact's set, Types needs scalars.
   *Change:* each pass takes the previous pass's result type, and PASSES.md gets an "after" column. Medium.
6. **WHEN-COMPILED reads the clock** at lowering, so two builds of such a program differ. *Change:* the instant as
   a parameter with a fixed default. Small.
7. **rt/ statics shared in a JVM.**
   - RANDOM's seed (`Floats`) means a program's values depend on what drew before it in the JVM.
   - The trace streams are global but benign.
   - *Change:* RANDOM's state onto `RunUnit`. Real if the lane runs in-process (SUSPECTED); small to medium.
8. **Two `Arith` instances per program.** Harmless, but a reader must check that. Small.

## C. Readability

- Fully qualified names inline (`java.util.Set`, `katana.sir.types.Value.Literal`) in Promoted, Lower and Views:
  imports would shorten them.
- Line length is unmeasured, so a 300-character ternary passes. Add a longest-line ceiling (under 160).
- `passes/Structuring.java:36-37` has two consecutive doc comments on one record.

## The advisor's three first

1. HANDOFF.md as open asks only, with the duplicate-paragraph test (and STATE.md if time).
2. NextId and the single tithe constant.
3. `Promoted.bound`'s try/finally and assertion with the shared `THROUGH` constant, then the name-table factory.
