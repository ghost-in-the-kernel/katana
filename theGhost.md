# theGhost.md: every decision Katana stands on

The layer beneath the comments. Each decision has a number (R-001), what was decided, why, what holds
it (a test, a document, a file), who decided and when, a status, and the decisions it touches. A new
decision is appended; a reversed one is marked `superseded by R-nnn` in the same commit that adds its
successor, and the successor names it. `test/GhostTest.java` holds this file: ids unique and sequential,
supersession named on both sides, and no two in-force decisions tagged with the same `subject:`
that carry a `conflicts-with:` line (docs/ROADMAP.md, P-05). `docs/decisions.md` is written from it.

Statuses: `proposed`, `in force`, `superseded by R-nnn`, `withdrawn`, `refused` (decided against, so
the question is closed).

Who may write here: Ghost decides. Any agent may append a decision Ghost has made, and may add or
correct cross-references (`See also`, `Held by`) on any entry without asking, since those change what
a decision points at, not what it says. A change to an entry's `Decision`, `Why` or `Status` text is
Ghost's alone, or an agent's with Ghost's explicit word in the same session (R-042).

---

### R-001: Java 21 for everything
- **Decision:** The compiler, the runtime (`rt/`), the harness (`oracle/`) and the CLI are Java 21,
  built with Gradle. Dependencies are vendored under `third_party/` with their licenses, so a clean
  clone builds with the network off.
- **Why:** The runtime must be Java, so Java is in the tree regardless; ANTLR's home target is Java and
  the forked grammars need no bridge; the harness ships to Java shops as one jar; one toolchain means
  one test runner, one lint, one set of rule tests; the buyer's reviewer reads one language. The
  container the study ran in had Maven Central, Gradle distributions and GitHub release assets
  blocked by its proxy, which is why the dependencies are vendored.
- **Held by:** `build.gradle` (offline build in CI), `docs/AGENT.md` ("clean clone builds offline").
- **Source:** Ghost, 2026-10-04, on Reviewer G's recommendation (docs/ROADMAP.md 3.4).
- **Status:** in force
- **See also:** R-038 (the doc tests before Gradle), R-041 (Gradle from the image)
- **subject:** toolchain

### R-002: Licensing is protective
- **Decision:** The compiler (`front/`, `sir/`, `lower/`, `intent/`, `passes/`, `ledger/`,
  `findings/`, `project/`, `cli/`) is all rights reserved, in a private repository, licensed per
  engagement. The runtime (`rt/`) is proprietary; each customer receives a perpetual, royalty-free
  license to the copy inside their own migrated suite, with no redistribution and no recurring fee.
  The harness (`oracle/`, `bench/`, the fixture formats) is source-available under a non-commercial
  license (PolyForm Noncommercial or counsel's equivalent): anyone may evaluate it; using it in paid
  work requires a license. Forked ProLeap grammars keep their MIT notice; che4z grammars enter only
  per R-015 after counsel; GPL code (`cobc`, `cobj`) never enters the tree, only their outputs as
  oracles. Nothing leaves the repository before R-037's provisional application is filed.
- **Why:** Ghost, 2026-10-04: the revenue funds the work, including the agents' compute; nobody uses it
  commercially without paying. The runtime's per-customer perpetual grant is the argument against the
  compile-to-JVM vendors' recurring runtime fees, which buyers resent (ISG 2024, in
  docs/research/incumbents.md). The harness stays evaluable because the route onto IBM's and the
  hyperscalers' radar is as an independent oracle for their LLM output (docs/research/FEASIBILITY.md).
- **Held by:** `LICENSE`; per-module license files before any copy leaves (`test/license.test`:
  every module named here has its file); `corpus/MANIFEST.md` for third-party terms.
- **Source:** Ghost, 2026-10-04. Not legal advice; to be confirmed with counsel before the first
  external disclosure.
- **Status:** in force
- **See also:** R-015, R-037
- **subject:** license

### R-003: GnuCOBOL 3.1.2, pinned by image digest, is the cobc lane
- **Decision:** The oracle's original lane runs GnuCOBOL 3.1.2 from the Ubuntu package, in a container
  pinned by digest. Revisited only if a NIST reference log differs from the lane's output by more than
  its version string.
- **Why:** It installs in about a minute, produced the study's `Total 426 426 0 0 9610 0 20 118 9748`
  NIST run (oracle/prototypes/nist/summary.log) matching GnuCOBOL's shipped summary, and the
  `eval-nist-cobol85` reference logs (made with 3.2.0) differed from it only in the version string
  on the program checked (NC101A).
- **Held by:** `Dockerfile` (digest); `oracle/nist` CI job (summary diff empty).
- **Source:** Ghost, 2026-10-04, on recommendation.
- **Status:** in force
- **See also:** R-A in docs/ROADMAP.md (GnuCOBOL stands in for IBM; every claim says so until a
  customer's z/OS output reconciles the lanes).
- **subject:** oracle-lane

### R-004: No model runs at conversion time
- **Decision:** Nothing in the pipeline from COBOL to shipped Java calls a language model. The one
  place a model may ever appear is to *propose* Intent IR facts that a person confirms, and not in v1
  (R-031).
- **Why:** Determinism is the product. A model in the loop makes every run non-reproducible, which is
  the incumbents' position and the gap Katana fills (docs/research/FEASIBILITY.md: no incumbent
  publishes audited pass/fail numbers; IBM's own paper calls dynamic testing the lowest-ROI check).
- **Held by:** `modules.json` (no module may import a model client); `test/source-rules`.
- **Source:** Ghost, 2026-10-03 (the original idea: "a compiler that does the work of LLMs").
- **Status:** in force
- **See also:** R-007 (the Intent IR, the only place a model could ever propose anything)
- **subject:** determinism

### R-005: Batch only, for v1
- **Decision:** Katana v1 proves and migrates batch programs and jobs. CICS online transactions and
  IMS are out of scope; `EXEC CICS` and `EXEC SQL` are lowered through facades with recording stubs
  (phase 7) so programs containing them still build, but their online behaviour is not proven.
- **Why:** The oracle for batch is byte-for-byte on datasets and exists (R-003). Online equivalence
  needs a transaction replay the study found only in Google's Dual Run and Mechanical Orchard; every
  open tool stops at CICS, DB2 and JCL (docs/research/open_source_tools.md).
- **Held by:** `docs/PROOF.md` (what a proof covers); `findings/` (a CICS program's proof report says
  what is not covered).
- **Source:** Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-003 (the batch oracle that makes batch provable), R-006 (the other scope cut)
- **subject:** scope

### R-006: One target language; no C++
- **Decision:** Java is the only back end. The Java-to-C++ idea is dropped.
- **Why:** A second target before the Semantic IR is proven stable repeats Suite Builder's Cypress
  mistake (a second generator copied instead of shared). Java to C++ is a port, not a translation
  (memory model, exceptions, strings, GC), and no mainframe buyer asks for it.
- **Held by:** `modules.json` (`lower/` has one emitter).
- **Source:** Ghost, 2026-10-04.
- **Status:** refused (C++); in force (one target)
- **See also:** R-005 (scope), R-007 (the Semantic IR a second target would have to wait for)
- **subject:** targets

### R-007: Three IRs and a ledger, kept apart
- **Decision:** The Semantic IR (what the program does; exact; mechanically derived), the Intent IR
  (what it means; inferred facts with evidence, confidence and confirmation state), and the proof
  ledger (what was done and why it was allowed; append-only) are three structures. The dialect
  profile is a fourth, pinned input whose hash appears in the IR header, the Java manifest, every
  fixture and every ledger line. Findings are the fifth. Determinism comes from the Semantic IR and
  the ledger adds nothing to it; provenance comes from the ledger and the IR adds nothing to that.
- **Why:** Intent cannot be proven by an oracle and semantics cannot carry a "why"; one structure
  bent to do both does both badly (docs/ROADMAP.md 3.2). The ledger is Suite Builder's kernel map
  pointed at the customer's program: the thing that stops a later change from destroying evidence of
  an earlier one.
- **Held by:** `modules.json` (`sir/`, `intent/`, `ledger/`, `profile/`, `findings/` as separate
  modules with their import rules); `docs/SEMANTIC-IR.md`, `docs/PROOF.md`.
- **Source:** Ghost and Reviewer G, 2026-10-04 (the morning's architecture discussion).
- **Status:** in force
- **See also:** R-004 (why the Intent IR is kept apart), R-008 (the kernel map, the ledger pointed at the tool)
- **subject:** architecture

### R-008: The navigation tithe
- **Decision:** One commit in eight changes no behaviour and improves the agent's environment or the
  code's navigability; every phase ends with a tithe pass of one to two days; the metrics in
  `docs/AGENT.md` ratchet and may not regress without a decision.
- **Why:** Ghost's finding in Suite Builder: delivery speed rose measurably once the agent's ability
  to find, read and change code with little context was treated as work; about a tenth of effort
  went there. Katana is larger, has two languages in the tree, a runtime that ships and a mounted
  corpus, so the share rises.
- **Held by:** `docs/AGENT.md` and `test/MetricsTest.java`; the kernel map (`docs/KERNEL-MAP.md`).
- **Source:** Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-040 (a tithe lands like any step), R-042 (when a metric arms)
- **subject:** process

### R-037: Patent protection is intended; nothing public before a provisional is filed
- **Decision:** Ghost intends to seek patent protection. Candidate claims: per-pass translation
  validation for legacy migration with an append-only provenance ledger; the double-run determinism
  gate before any comparison; the copybook-driven comparator that normalizes representation and
  refuses to normalize semantics. No public disclosure (public repository, post, talk, the phase-9
  publication) before a provisional application is on file; the full application within its twelve
  months. Demonstrations before then are under NDA or an evaluation license.
- **Why:** Public disclosure before filing forfeits patentability outside the US and starts the
  one-year US clock. Prior art to distinguish: Pnueli's translation validation (1998), Alive2 (PLDI
  2021), Google Dual Run, Mechanical Orchard's byte-for-byte gate; the combination applied to legacy
  migration with a provenance ledger is what is novel (docs/research/literature_and_failures.md).
- **Held by:** docs/ROADMAP.md phase 9.0 (gate); the repository's visibility; counsel.
- **Source:** Ghost, 2026-10-04. Not legal advice.
- **Status:** in force
- **See also:** R-002
- **subject:** disclosure

### R-038: Doc tests are plain Java 21, no dependencies, until Gradle arrives
- **Decision:** The phase-0 document tests (`ghost`, `glossary`, `docs`, `modules`, `todo`) are plain
  Java 21 classes under `test/` with shared helpers (`Check.java`, `Json.java`), compiled together and
  run by `test/run.sh`; the exit code is the verdict; no dependency. Phase 0.3 moves them under
  Gradle and JUnit with their bodies unchanged.
- **Why:** They are needed before the build system exists (phase 0.2 precedes 0.3), and a dependency
  taken for a test is a dependency the offline build must vendor. The first wording ("single-file
  programs run with `java test/<Name>.java`") was wrong: a Java single-file program cannot use a
  second source file, so each test would have carried its own copy of the helpers, against the
  "duplicate helpers: 0" metric (docs/AGENT.md).
- **Held by:** `test/DocTests.java`, run by `gradle test` since phase 0.3.
- **Source:** Opus, during phase 0.2; wording corrected by Opus; confirmed by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-001, R-041 (where the tests went after 0.3)
- **subject:** toolchain

### R-039: Glossary aliases are enforced only when marked
- **Decision:** In `docs/GLOSSARY.md`, an alias in backticks is enforced: `test/glossary.test` fails
  when it appears in a document outside the glossary and `docs/research/`. An alias without backticks
  is a review guide and no test judges it. "Golden" is a term and is not an alias of anything.
- **Why:** The first rule ("a listed alias appearing in a doc outside the glossary fails") gave 60+
  failures on the day's documents, most of them words that are wrong for one concept and right for
  another: "run" is an execution, "source" is a source map, "error" is the tool failing, "map" is the
  provenance map. A test that fails on correct prose is a test that gets switched off (Suite Builder,
  G-107: rules that fired on good code became warnings). The study's notes under `docs/research/`
  were written before the glossary and keep their words.
- **Held by:** `test/GlossaryTest.java`; the glossary's own header.
- **Source:** Opus, during phase 0.2; decided by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-008 (the tithe adds backticks as slips are caught)
- **subject:** glossary

### R-040: Each finished step lands on main
- **Decision:** Work happens on a branch; each roadmap step (or each commit an item is split into)
  is merged to `main` as soon as it is done and its tests pass, not held until the phase ends. Before
  merging, fetch `main`; if it moved, rebase onto it and run the tests again. `main` is never left
  with a half-done step: whatever is on it passes the fast loop and the doc tests.
- **Why:** Ghost, 2026-10-04: merging often lets a second agent be started on `main` to take one part
  of the work while the first goes on, and a branch that waits for a phase's end is a fork the second
  agent cannot stand on. Phase 0.2 sat on a branch while the plan was corrected on `main`
  twice, and needed two rebases.
- **Held by:** `CLAUDE.md` ("Before calling work done"); review.
- **Source:** Ghost, 2026-10-04; drafted by Opus at Ghost's request.
- **Status:** in force
- **See also:** R-008 (a tithe commit lands the same way)
- **subject:** process

### R-041: Gradle comes from the pinned image; no wrapper
- **Decision:** The build runs with the Gradle inside the image the `Dockerfile` pins by digest
  (8.14.3), invoked as `gradle`, not through a committed wrapper (`./gradlew`). `build.gradle` refuses
  any other Gradle version, so a build outside the image fails loudly instead of drifting.
- **Why:** The wrapper downloads its distribution from `services.gradle.org`, which redirects every
  download to `downloads.gradle.org`; the proxy the project works behind refuses that host (phase
  0.3, 2026-10-04, as the study found). The official `gradle:8.14.3-jdk21-noble` image carries the
  same Gradle with JDK 21 on Ubuntu 24.04 in one digest, so one pin replaces two. Where
  docs/ROADMAP.md says `./gradlew`, read `gradle` in the image.
- **Held by:** `build.gradle` (the version check); `Dockerfile` (the digest); CI.
- **Source:** Opus, during phase 0.3; decided by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-001, R-038
- **subject:** toolchain

### R-042: Who writes theGhost.md; a metric arms when first met; tools live outside the modules
- **Decision:** Three housekeeping rules from phase 0.6. (a) Any agent may append a decision Ghost
  made and may add or correct `See also` and `Held by` lines on any entry; `Decision`, `Why` and
  `Status` text changes need Ghost. (b) A metric in `docs/AGENT.md` is *armed* the first time it is
  measured within its target; until then it is measured and reported, never ratcheted. "Docs lines per
  source line" (target < 0.35) arms when the first module carries real code (phase 2); the other nine
  armed in phase 0. (c) `scripts/` (the kernel-map and metrics tools) and `test/` are outside the
  thirteen modules; `modules.test` treats them as outside the product.
- **Why:** (a) `theGhost.md` is Ghost's file; the agent that proposed a decision and the agent that
  links it need no permission to keep its cross-references true, and the "decisions without See also"
  metric cannot be worked otherwise. (b) A ratchet on a metric that cannot be met before code exists
  can only ever report "worse", which trains agents to ignore it; the target is from Suite Builder at
  maturity and stays. (c) The tools that measure the product are not the product.
- **Held by:** this file's header; `test/MetricsTest.java` (an unarmed metric is reported with
  "not armed", not compared); `docs/modules.json` intro.
- **Source:** Opus raised all three during 0.6; decided by Ghost with Reviewer G, 2026-10-04.
- **Status:** in force
- **See also:** R-008, R-038
- **subject:** process

---

Reserved, to be recorded when their phase arrives (docs/ROADMAP.md names each): R-009 fixture
schema; R-010 DFSORT implementation; R-011 what the comparator normalizes and refuses; R-012 the
double-run gate; R-013 what a proof covers and never covers; R-014 CICS/DB2 grammar graft; R-015
EPL-2.0 grammars (counsel); R-016 indicator-column and tab policy; R-017 the IR is complete; R-018
the attribute tuple; R-019 byte-image representation; R-020 Decimal with explicit scale; R-021 the
dispatcher; R-022 TRUNC(OPT) as STD with a finding; R-023 float as IEEE with a finding; R-024 one
emitter; R-025 indexed-file comparison on logical unload; R-026 SORT stability; R-027 stub facade
contract; R-028 coverage in every proof; R-029 refuted intent never acted on; R-030 ledger
append-only; R-031 models propose intent only, later, never v1; R-032 passes apply only when
obligations are discharged; R-033 Erosa–Hendren total structuring refused; R-034 the residual
dispatcher is acceptable output; R-035 the refactoring-distance report; R-036 the Hercules lane's
legal status.
