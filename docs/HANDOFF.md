# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0 and 1 are done
(1.5 with one part of its check deferred, below); the boundary's reversals are built. Next: 2.1.

## For Fable (R-053: answered at the phase boundary; the default taken is named)

(phase 2's six from steps 2.1–2.5 answered mid-phase 2026-10-05 while Fable was present: R-055; all
six defaults hold, three roadmap wordings corrected)

## For Ghost (R-053: answered at the phase boundary; a default is named, or "stopped")

(phase 2's two answered 2026-10-05: the ANTLR tool jar accepted under R-001; che4z parked for good,
R-015)

## For Opus: tithe pass 2a, due now (R-008: 14 commits since the last tithe, none of them one)

In this order, each its own commit, goldens unchanged; measure before and after and record both in
`docs/AGENT.md` and the journal:

1. **The pre-push check, from 8–10 minutes toward 3.** It is the largest cost in the environment and
   not yet a metric; make it one ("pre-push check, minutes", target < 3, armed at the measured value).
   Three levers, each measured alone: (a) the corpus is content-pinned (SHA-256, fixed commits), so a
   fresh clone need not refetch it: keep a verified corpus cache beside the clone and let `fetch.sh`
   verify instead of fetch; (b) a Docker layer cache for the image; (c) **run what the change touches**
   (Suite Builder's `check` rule): a `gradle check` task that maps touched paths to suites (`front/` →
   the front corpus suite; `sir/` → the IR suites; `oracle/` → the lane suites; a doc → the doc tests
   only) and prints why it chose each; `--all` runs everything, and the phase-boundary tithe always
   runs `--all`. The commit message records which suites ran and why.
2. **`CLAUDE.md` back under 800 words.** Two rows are stale beside their replacements ("The front end:
   grammar, preprocessor, source map" and "The Semantic IR" duplicate the two newer rows); remove
   them. Any row over five files is split by task, not padded.
3. **The thirteen uncommented functions** the metrics list: a one-line purpose comment each, or a
   split where the comment would say "and then".
4. **Fresh-agent trial 2**, under R-043's wording: "Propose where a RECFM=V dataset's RDW handling
   would go: say which files you would change and what you would write, then stop." Pass: `CLAUDE.md`
   first, `docs/LANES.md` within three reads, no edits, LANE-02 named.
5. **The journal's `trial` entries** so far (stderr determinism; RECFM=V; NC124A; DFHAID) each have a
   result or a "not yet run" line; none is left ambiguous.
6. Re-measure, record the pass in `docs/AGENT.md` with pass 1's numbers kept beside it, journal it.

## Trials (R-052: a question settled by running something; the implementing agent runs it)

- **The recalled DFHAID values** (R-055 C-2): compare DFHPF13–24 and DFHPA1–3 against IBM's CICS
  documentation when reachable, or a second independent copybook; until then `hypothesis`.

(run: RECFM=V J-062, stderr J-059, NC124A J-072)

## Waiting on Ghost

- **Appendix A of the Enterprise COBOL Programming Guide** ("Intermediate results and arithmetic
  precision"): the one IBM semantic still unverified (R-049, `docs/PROFILE.md`). Blocks phase 3.1.
  Closes when Ghost pastes the topic URL (path-style `ibm.com/docs/en/SS6SG3_6.4.0/pg/ref/<topic>.html`
  URLs are reachable when the id is known) or attaches the PDF (SC27-8714) for a full read.
- **Two more IBM texts to confirm when reachable**, neither blocking: IDCAMS reads columns 2–72
  (DFSMS Access Method Services Commands; the lane applies it, J-035, two secondary sources); the
  Enterprise COBOL rule for tab characters in source (R-016: fixture-declared width, default 8, as a
  finding); DFSORT's IBM-supplied PAD/TRUNC defaults and whether its padding rule applies to the
  OUTREC-reformatted length (R-051 assumes RC0 and yes).
- Counsel on R-002's license texts and R-015 (EPL-2.0 grammars), before any copy leaves the repo.
- Counsel on JUnit 5 (EPL-2.0, vendored for tests only, unmodified, never shipped): a lighter
  question than R-015's che4z grammars, which would be modified and shipped inside the compiler. The
  two go to counsel together; neither blocks phase 1 (R-045 notes the distinction).
- The provisional patent application (R-037), before any public disclosure; realistically after
  phase 4, when there is a CardDemo proof to describe.

## Waiting on a run

- **CI is out of GitHub Actions minutes until 2026-11-02** (R-050). Until then a step's gate is the CI
  image and commands on a fresh clone before the push: `scripts/prepush.sh` (the suites the change
  touches, and why; `--all` at a phase boundary), its result and suites quoted in the commit message. The first commit only checked that way is
  `6701f6f` (1.5a). When minutes return, run the workflow by hand on `main` once.
- NIST under `-std=ibm` (phase 1.2) is recorded (`oracle/nist/IBM-CHANGES.md`); three of its causes
  are what `rt/Decimal` and the profile must model before phase 3.1 freezes the numeric runtime:
  IBM's intermediate precision (`-farithmetic-osvs`, 61 failures), binary items not truncated
  (`-fno-binary-truncate`, 8) and segment numbers ignored (`-fsection-segments=ignore`, 31).
- che4z's CICS and DB2 grammars driven over CardDemo and BankDemo (phase 2.2): generated, never run;
  about 30 minutes.
