# A navigation trial with a Haiku agent (2026-10-22)

On Ghost's word ("run a test with a haiku agent to see how easy it is for the agent to navigate the codebase"),
after tithe J-226 added docs/PASSES.md. A Haiku agent, new to the repository and read-only, answered seven questions
whose answers Opus knew. It started from the repository's own guidance. Its session had loaded another project's
CLAUDE.md, so it found Katana's on its own. This is a model's reading (R-029, R-031); the answers were checked
against the code by Opus.

## Result

All seven answers were right, in about 22 tool calls. It was highly confident in five and fairly confident in two
(Q6 and Q7).

| Question | Found through | Calls |
| --- | --- | --- |
| Q1. Who promotes a numeric item, and who declares it | `docs/PASSES.md`, then `passes/Scalars.java` and `lower/Promoted.java` | ~4 |
| Q2. Which pass writes `c = (a + b);`, and its obligations | `passes/Fits.java` and `lower/Arith.java` | ~3 |
| Q3. Which test proves structured CardDemo equals literal | `StructuringCardDemoIntegrationTest` | ~3 |
| Q4. The fast loop's command | CLAUDE.md | 2 |
| Q5. Where the tithe rule lives | R-008 and `scripts/TitheGate.java` | ~2 |
| Q6. Where a LINKAGE record class is bound | `lower/Programs.bind` and `lower/Views.bind` | ~4 |
| Q7. The chain's first open item | 3.1 | ~5 |

## The friction it reported

1. The routing table has no row for a numeric item as a Java variable; `docs/PASSES.md` was the only way in, and
   `lower/Promoted.java`'s name does not say it writes declarations.
2. R-008 says one commit in eight, CLAUDE.md says every eighth, and `scripts/TitheGate.java` allows sixteen
   (J-154's amendment explains it, but the three are not reconciled).
3. TODO.md has no status per item: "open" is read from an item's absence in DONE.md. 3.2 is missing (moved by
   R-071, which TODO.md does not say at the gap), and 3.1's check says 359 autotests where its body says 378
   runnable.
4. The CardDemo job lists are hard-coded in two tests and do not match `oracle/fixture/carddemo/`'s 12 entries;
   nothing says why the study jobs are left out.
5. `passes/Fits.java`'s header omits its precondition, a statement in Exact's set. `docs/AGENT.md` gives the fast
   loop's ceiling as both 20 s and 25 s.
