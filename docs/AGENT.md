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

Measured by `gradle metrics --args=--time` in the image at every tithe pass. The warm fast loop and
the pre-push check are held on every push (R-054 amended, J-163): `scripts/prepush.sh` times both and
`scripts/Ceilings.java` refuses a push past the ceiling in the target column; `MetricsTest` refuses a
recorded time past it. The cold fast loop and the skipped tests are reported, not held.

| Metric | Phase 0 target | Measured (tithe pass 5, 2026-10-18) | Why it matters to the agent | Pass 4 | Pass 3a | Pass 2 | Pass 2a | Pass 1 | Pass 0 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Mandatory context (words) | < 1,500 | 861 | every task pays it | 804 | 804 | 804 | 795 | 872 | 636 |
| Fast loop (s) | ≤ 25 (20 from tithe pass 3's close; held, R-054 amended) | 19 (warm, in the pre-push container at 5516d15; 18 by `gradle metrics`; 1 under its ceiling, HANDOFF) | the agent runs it after every edit | 15 | 17 | 14 | 11 | 9 | 12 |
| Fast loop, cold (s) | < 30 | 36 (reported, not held: R-054) | a fresh session pays it once | 31 | 26 | 22 | 20 | 24 | — |
| Pre-push check, every suite (min) | ≤ 10 (held; ratcheted from 15 on 2026-10-17, 8.2 measured, R-054; "under 3" retired; 12 corrected to 15, R-054 amended) | 9.5 (569 s at 5516d15, all ten suites; intent and ledger new; 0.5 under its ceiling, HANDOFF) | paid before every push while CI has no minutes (R-050) | 8.2 | 10.5 | 3.5 | 3.4 | — | — |
| Pre-push check, the suites a push selects (min) | ≤ 10 (held, R-054 amended) | 9.5 at most (every suite, above) | what each push waits | 8.2 | 5.7 | — | — | — | — |
| Largest source file (lines) | < 400 | 360 (lower/Verbs.java) | fits a read without paging | 360 | 327 | 258 | 344 | 241 | 179 |
| Files a routing row opens | ≤ 5 | 4 | first five reads decide the task | 4 | 4 | 4 | 4 | 4 | 4 |
| Functions over 20 paths | 0 new | 0 | branching is where agents misread | 0 | 0 | 0 | 0 | 0 | 0 |
| Duplicate helpers (same body, two names) | 0 | 0 | the agent edits one and misses the other | 0 | 0 | 0 | 0 | 0 | 0 |
| Decisions without `See also` | 0 | 0 | grep misses what links find (T-18) | 0 | 0 | 0 | 0 | 0 | 0 |
| Docs lines per source line | < 0.35 | 0.24 | Suite Builder reached 0.37 and it hurt | 0.24 | 0.26 | 0.32 | 0.33 | 0.45 | 1.13 |
| Stale generated artifacts | 0 | 0 | detected by test | 0 | 0 | 0 | 0 | 0 | 0 |
| Skipped tests reported on the fast loop | 0 | 0 | a real skip hides among 119 (T-15) | 0 | 0 | 0 | 0 | 0 | 0 |
