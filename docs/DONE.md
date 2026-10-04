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
