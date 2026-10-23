# The super tithe's plan: Fable's advice (2026-10-23)

A Fable advisor wrote this read-only, at 4f98b8f, on Ghost's word ("lets do a hard pass on optimization for
traversability, ease of navigation, and low agent context again. for this pass, lets spin up a fable advisor"; "we
want to make sure the project is refactored and has good seperation of concerns. think of this as a super tithe").
It is a model's reading (R-029).

Ghost's answers (2026-10-23):
- the journal is split, behind a generated index;
- HANDOFF's done sections are archived;
- a `.gitattributes` is added.

He added that CLAUDE.md becomes a pointer file: a new routing row goes into the instructions file its area points
to, never into CLAUDE.md (R-074).

Opus checked these claims against the code (2026-10-23):
- no `.gitattributes` and no `docs/decisions.md`;
- Lower 380, Verbs 379 and Flow 354 lines;
- JOURNAL.md 4,028 lines;
- the duplicated `program(...)` helper in nine unit tests.

## Diagnosis: what stops a Sonnet agent

1. **`lower/` shares mutable state through a chain nothing documents.** `Lower.program` builds `Exprs`, then replaces
   `x.promoted`, then sets `x.promoted.views`. Every verb class reaches `f.e`, `f.x` and `f.x.promoted` through `Flow`,
   which carries cursor state.
2. **`lower/Lower.java` is the hottest file and does three jobs**: the class-skeleton emitter, storage and
   attributes, and the pass contract (`Plan`, 13 positional fields). It is at 380 of 400 lines.
3. **Test support was duplicated and hidden inside tests**:
   - the small-program helper nine times;
   - javac called by hand in six files;
   - `CobcLane.Setup` built in five;
   - `CardDemoJavaLaneIntegrationTest` used as a library.
4. **The flat long documents**:
   - JOURNAL.md, about 4,000 lines with no index;
   - theGhost.md, about 1,500, with the promised `docs/decisions.md` missing;
   - HANDOFF.md, whose lines 13 to 615 are done sections;
   - TODO.md, with no status per item.
5. **Names that mislead at a module's edge**: `CobcLane.run` runs both lanes; `passes/Structuring.java` is both the
   pass chain and the first pass.
6. **Routing gaps and a thin word budget**: no row for test support, `modules.json`'s `entryPoints` empty, CLAUDE.md
   near its 1,500 words.
7. **Three files within 50 lines of the 400 ceiling**, with no early warning.
8. **AGT-05 claims a `.gitattributes` that does not exist.**

## The plan, in order

1. `test/katana/support/Small`, the nine copies gone.
2. One helper for lowering and compiling a program in a test.
3. `CardDemoLane`, the CardDemo lanes' support, so no test is another's library.
4. `lower/Lower.java` split: storage and attributes to `lower/Storage.java`.
5. `Lower.Plan` its own file, `lower/Plan.java`, with `THROUGH` beside it.
6. `Plan` built by name, so adding a field edits one place.
7. The pass chain out of the first pass: `passes/Passes.java`.
8. lower/'s construction order in one place, the late binding behind an accessor, and a `lower/README.md` with
   the object graph.
9. The promoted-item policy out of `Verbs.statement`.
10. `docs/decisions.md` generated, held fresh by a test.
11. The journal split behind a generated index.
12. HANDOFF's done sections archived.
13. A `Status:` line per TODO item.
14. `modules.json`'s `entryPoints` filled, and a short README in `lower/`, `passes/`, `oracle/`, `rt/` and `test/`.
15. CLAUDE.md's rows corrected (the java lane's row opens `CobcLane.Setup`; a row for test support).
16. Metrics warn at files over 350 lines and functions over 15 paths.
17. A generated table of contents for the research notes.
18. A `.gitattributes`.

## Separation of concerns

- `Lower.java`: the emitter, storage, and the pass contract.
- `Structuring.java`: the chain and one pass.
- `Exprs`, `Promoted` and `Views`: construction split across three constructors and two late assignments.
- `Verbs.statement`: dispatch plus promoted-item policy.
- `passes/Scalars`: reads `THROUGH` from the lowering it feeds.
- `CobcLane`: one class, two lanes, chosen by a nullable `Setup.java`.
- The CardDemo test acting as a library.
- `Metrics.duplicates`: blind to `test/`.

## What not to do

- No subpackages in `lower/`, whose package-private access is the sharing mechanism.
- No narrow interface replacing `Flow` in the verb classes.
- No renumbering of sections or decisions, and no split of theGhost.md.
- No behaviour fixes under a tithe (`Floats`' seed, WHEN-COMPILED's clock).
- No `project/` module without an architecture decision.
- Do not shrink `Verbs.verb`'s dispatch table to pass a metric.
- Do not rewrite journal entries.
