# DONE: the items finished, with their numbers kept

Moved here from `TODO.md` when done; the number stays the roadmap's (docs/ROADMAP.md section 6).
Newest last. A line says what was done and where; the decisions an item produced are in `theGhost.md`.

## Phase 0: founding

### 0.1 Repo and license

The founding commit: the repository, `LICENSE`, `.gitignore` refusing what a customer gave us and
what a run produced.

### 0.4 Import the corpus

The founding commit, as `corpus/fetch.sh`, `corpus/scan.sh` and `corpus/MANIFEST.md`; clones are
fetched by script and never committed. The check's test (a file with no license fails) is not yet
written.

### 0.5 Import the research artifacts

The founding commit: the study's prototypes under `front/prototypes/`, `lower/prototypes/` and
`oracle/prototypes/`, each a prototype to be absorbed, not a dependency.

### 0.2 The documents of 4.1 and their tests

The founding commit: `theGhost.md`, `docs/GLOSSARY.md`, `CLAUDE.md`. Then `docs/modules.json`,
`docs/AGENT.md`, `TODO.md` and this file, `docs/SEMANTIC-IR.md`, `docs/PROFILE.md` and
`docs/PROOF.md` as headings with their numbering reserved, `docs/KERNEL-MAP.md` with its format; and
the doc tests under `test/` (ghost, glossary, docs, modules, todo), run by `test/run.sh` (R-038), each
held to inputs it must reject. Decisions: R-038, R-039.

### 0.3 The container

`Dockerfile`: the official Gradle 8.14.3 / JDK 21 / Ubuntu 24.04 image pinned by digest, with
`gnucobol3` 3.1.2-5.1ubuntu1 (R-003). `build.gradle` declares no repository; its only dependencies are
the jars in `third_party/` (`third_party/MANIFEST.md`), and it refuses any Gradle but 8.14.3. The doc
tests run under JUnit (`test/DocTests.java`). `.github/workflows/ci.yml` builds the image and runs
`gradle build --offline` with `--network none`. Checked: a fresh clone built that way in 12 s.

### 0.6 Kernel map and metrics scripts

Ported from Suite Builder's `kernel-scan.mjs` and `kernel-metrics.mjs` to Java under `scripts/`,
reading source with the JDK's own parser (no dependency). `gradle metrics` prints the table of
ROADMAP 5 and the warnings of 4.2; `gradle kernelMap` appends a row per new function in a module to
`docs/KERNEL-MAP.md` (empty: no module has source yet). `test/MetricsTest.java` ratchets the recorded
values; `test/KernelMapTest.java` fails on a function the map lacks or one gone with no closing row.
Found on the way: `gradle test` skipped the doc tests after a doc-only change, because the task's
declared inputs were its classes and not the documents they read, so a broken document passed
locally (CI, from a fresh clone, was never fooled). Found by deleting a module file in a scratch clone
and watching the build stay green; fixed in its own commit, the test task now always runs.

### 0.7 Tithe pass 0

Three fresh-agent trials ("add a module"). The first two read "add" as permission, reached
`docs/modules.json` 4th and 5th and changed files; between and after them: the `adding` line in
`docs/modules.json`, the routing row naming it, `settings.gradle` saying modules are not Gradle
subprojects, the module count kept only in `modules.json`, stale test names fixed, and R-043 (a new
module is proposed, not added). The third, phrased as a proposal and asked by a contributor, passed:
`CLAUDE.md` then `docs/modules.json`, nothing changed, §3.3 named as Ghost's. Metrics recorded in
`docs/AGENT.md` at tithe pass 0. Phase 0 is done.

## Phase 1: the oracle and the corpus

### 1.1b The population reader (a step of 1.1, which is still open)

`oracle/nist/Population.java` reads NIST's population format. The corpus README and the User Guide
both describe a member as `*HEADER,kind,name` to `*END-OF,name`; 68 of `newcob.val`'s 512 members
say otherwise, naming their main program first and themselves last
(`*HEADER,COBOL,IC101A,SUBPRG,IC102A`), and two headers carry sequence text past column 72. Found
by reading the real file rather than trusting the description; the reader's count (512: 459
programs, 51 copy members, 2 data files) now says which is true.
