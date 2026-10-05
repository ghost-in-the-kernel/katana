# The agent's environment: its contract and its metrics

What an agent working in Katana can count on, each line a measurable (docs/ROADMAP.md 4.4), and the
metrics the navigation tithe ratchets (ROADMAP 5, R-008). A metric may improve; it never regresses
without a decision in `theGhost.md`. `test/MetricsTest.java` holds the table: within its target a
metric may move freely; past it, it may not get worse than the value measured here. A metric is
**armed** the first time it is measured within its target, or at the first phase boundary after real
code at its value there (R-042, R-054); until then it is "not armed" and not compared.

## The contract

- **AGT-01 A clean clone builds offline.** `gradle build --offline` in the image with `--network none`, from
  `third_party/` (R-001).
- **AGT-02 The fast loop runs under 20 s, warm (R-054).** Unit tests only: no `cobc`, no `javac` of generated Java.
  Integration is its own target with its own budget: NIST in full under 3 minutes, CardDemo under 1.
- **AGT-03 Mandatory context stays under 1,500 words.** `CLAUDE.md` plus whatever an agent must read
  before a task.
- **AGT-04 A routing row opens at most five files, none over 400 lines.**
- **AGT-05 Generated files are marked and kept out of diff review** (`.gitattributes`
  `linguist-generated -diff`).
- **AGT-06 Every generated artifact has a command that regenerates it and a test that fails when it
  is stale.**
- **AGT-07 The container image is pinned** (GnuCOBOL 3.1.2 by digest per R-003, JDK 21, Gradle, the
  corpus mounted). An agent never installs tooling.
- **AGT-08 Rendered views are committed where the source is indirect** (`docs/rendered/`, if
  templates or slots are ever used).
- **AGT-09 Logs are structured and short.** The oracle's first-difference report fits on one screen.

## The metrics

Measured by `gradle metrics --args=--time` in the image at every tithe pass; the fast loop (warm, and
cold beside it) and the skipped tests are measured after a run, so the test does not hold them.

| Metric | Phase 0 target | Measured (tithe pass 1, 2026-10-05; R-054 at the boundary) | Why it matters to the agent | Pass 0 (2026-10-04) |
| --- | --- | --- | --- | --- |
| Mandatory context (words) | < 1,500 | 872 | every task pays it | 636 |
| Fast loop (s) | < 20 | 9 (warm, the daemon running: R-054) | the agent runs it after every edit | 12 |
| Fast loop, cold (s) | < 30 | 24 (reported, not held: R-054) | a fresh session pays it once | — |
| Largest source file (lines) | < 400 | 241 | fits a read without paging | 179 |
| Files a routing row opens | ≤ 5 | 4 | first five reads decide the task | 4 |
| Functions over 20 paths | 0 new | 0 | branching is where agents misread | 0 |
| Duplicate helpers (same body, two names) | 0 | 0 | the agent edits one and misses the other | 0 |
| Decisions without `See also` | 0 | 0 | grep misses what links find (T-18) | 0 |
| Docs lines per source line | < 0.35 | 0.45 (armed at the phase-1 boundary: R-054) | Suite Builder reached 0.37 and it hurt | 1.13 |
| Stale generated artifacts | 0 | 0 | detected by test | 0 |
| Skipped tests reported on the fast loop | 0 | 0 | a real skip hides among 119 (T-15) | 0 |
