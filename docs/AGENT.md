# The agent's environment: its contract and its metrics

What an agent working in Katana can count on, each line a measurable (docs/ROADMAP.md 4.4), and the
metrics the navigation tithe ratchets (ROADMAP 5, R-008). A metric may improve; it never regresses
without a decision in `theGhost.md`. `test/metrics.test` holds this file once the metrics script
exists (phase 0.6); until then the measured column is empty, not guessed.

## The contract

- **AGT-01 A clean clone builds offline.** `./gradlew build` with no network, from `third_party/` (R-001).
- **AGT-02 The fast loop runs under 20 s.** Unit tests only: no `cobc`, no `javac` of generated Java.
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

Measured by `./gradlew metrics` (phase 0.6) and recorded here at every tithe pass.

| Metric | Phase 0 target | Measured | Why it matters to the agent |
| --- | --- | --- | --- |
| Mandatory context (words) | < 1,500 | | every task pays it |
| Fast loop (s) | < 20 | | the agent runs it after every edit |
| Largest source file (lines) | < 400 | | fits a read without paging |
| Files a routing row opens | ≤ 5 | | first five reads decide the task |
| Functions over 20 paths | 0 new | | branching is where agents misread |
| Duplicate helpers (same body, two names) | 0 | | the agent edits one and misses the other |
| Decisions without `See also` | 0 | | grep misses what links find (T-18) |
| Docs lines per source line | < 0.35 | | Suite Builder reached 0.37 and it hurt |
| Stale generated artifacts | 0 | | detected by test |
| Skipped tests reported on the fast loop | 0 | | a real skip hides among 119 (T-15) |
