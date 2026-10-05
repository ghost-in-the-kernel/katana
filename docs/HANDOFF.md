# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0 and 1 are done
(1.5 with one part of its check deferred, below). Phase 2: 2.1 and 2.3-2.5 done, 2.2 deferred to 7.2 (R-015).

## For Fable (R-053: answered at the phase boundary; the default taken is named)

(the "check is worth what it can refute" proposal of J-078 is recorded as R-056, with the
consequence clause added: a verdict must stop an action; the meta-test is tithe item A7)

(2.8's tithe evaluation and plan: written 2026-10-05, below under "For Opus: tithe pass 2")

## For Opus: tithe pass 2, the phase-2 boundary (2.8's check is this list)

**The evaluation.** Every metric in `docs/AGENT.md` is inside its target and the pre-push check fell
from 6.2 to 1.8 minutes for a one-module change: the environment is sound. What the metrics cannot
see, a reviewer who had not seen the plan found in an afternoon (J-073): the Semantic IR's
*statement* side is not complete. `sir/build/Paragraphs.walk` skips every terminal node, so a keyword
that carries semantics is dropped: `ROUNDED` appears nowhere in `sir/`; `EVALUATE ... ALSO ... WHEN`
collapses its WHEN groups and loses ANY; `INSPECT FOR ALL` and `FOR LEADING` produce one role;
`DELIMITED BY SIZE`, `WITH TEST AFTER`, `STOP RUN` against `STOP literal` lose their word; arithmetic
and conditions are stored in ProLeap's rule shapes (`MultDivs`, `Condition`), not operator trees.
The round-trip and contract checks did not catch it because they compare the IR with itself: a check
that is always green (the family of J-006 and the always-red CI). The *data map* side is in phase-3
shape (one `Attributes` tuple, 12,881 of 12,893 NIST fields placed as `cobc` places them). R-017
("the IR is complete") was reserved and would have been recorded false.

So this tithe is not environment repair; it is making the IR the thing phase 3 can be built on,
and putting the seams phase 3 will stand on in place first. The reviewer's estimate: done first,
phase 3 is ~8 weeks; skipped, ~14, because the lowering would consult the parse tree or decide
semantics itself, which MOD-03 and SRC-10's equivalent forbid.

Each item its own commit; the gated pre-push check before each push; `ir.golden` re-recorded where
an item changes it and the diff reviewed. Order: A first (it changes the IR), then B in any order,
then C.

**A. The IR made complete (R-017 becomes a decision held by a test)**

A1. **No token left behind.** For every statement subtree, every terminal outside a noise list
    (IS, ARE, THEN, END-x, punctuation) must appear in the IR as verb, role, keyword, operator or
    literal. Keyword alternatives become `Operand(role, Name("keyword", X))` or a `flags` list on
    `Statement`; `Branch.condition` filled, one branch per WHEN group with its condition list;
    arithmetic and conditions normalized to binary trees over a fixed operator vocabulary, abbreviated
    relations (`A > 1 AND < 5`) expanded in `sir/build`. The test runs on COPYREC and a probe program
    in the fast loop and on NIST in integration. (`Paragraphs.java` walk; `Values.java` expressions;
    37 `(KEYWORD | KEYWORD)` alternatives in `front/grammar/Cobol.g4` procedure rules.) ~16–24 h.
    *Check:* the probe (`COMPUTE A ROUNDED`, `EVALUATE ALSO/ANY/OTHER`, `INSPECT ALL/LEADING`,
    `STRING DELIMITED BY SIZE`, `PERFORM WITH TEST AFTER`) shows every keyword in its JSON; the
    no-token test passes on all 437 NIST programs.
A2. **`Origin` into `sir/types`.** Every IR record imports `katana.front.Mapped.Origin`; `lower/` may
    import `sir` only and `ModulesTest` refuses `katana.front`. A four-field `sir.types.Origin`,
    converted in `sir/build`. ~2 h. *Check:* `grep katana.front sir/types` is empty; `ModulesTest`.
A3. **Structured literals and VALUEs.** `Item.value` and `Condition.values` are text with quotes;
    `Value.Literal` does not distinguish `X'..'`, `Z'..'`, `N'..'`, floating literals. A literal kind
    and decoded payload (bytes for hex; unscaled+scale for numeric). ~6 h.
A4. **PICTURE as data; the missing clauses.** `Pictures` is package-private, so `rt/` (no imports)
    would re-parse PICTURE strings. An edit-mask array per position on `Attributes` for edited items;
    `JUSTIFIED RIGHT` (not read), `BLANK WHEN ZERO` (folded into category, lost as a flag),
    `EXTERNAL`/`GLOBAL`, `RELATIVE KEY` on `FileDescriptor`. ~8 h. *Check:* `datamap.golden` unchanged
    for offsets; a new column for the flags.
A5. **Per-program IR goldens.** `sir/ir.golden` is counts; `Contract.lines` are discarded
    (`IrIntegrationTest.java:81`). Commit the contract lines for COPYREC and one NIST program per
    module as text goldens in the fast loop, so A1–A4 land as reviewable diffs; define the lowered-Java
    golden format beside them (`lower/golden/<prog>.java`, byte-identical, P-02). ~3 h.
A7. **The check meta-test (R-056).** A registry where each golden, integration and rule check
    declares its oracle (a path or tool outside the module under test), one killer mutation, and the
    task it gates; the meta-test applies each mutation and fails if the check stays green, refuses an
    oracle inside the module, and makes a suite that ran zero tests red. First run over the existing
    checks: a check the meta-test cannot kill is a finding, listed. ~8 h. *Check:* the IR round trip
    and the contract count each have a mutation that kills them (A1's token test is the oracle); the
    zero-run case from J-006 is red.
A6. **Pin the role vocabulary.** Roles and verbs are grammar rule names; the lowering's switch must
    be exhaustive. `sir/roles.golden` generated from the walker, held by a test; phase 3's `lower/`
    test checks its table covers it. ~3 h.

**B. The seams phase 3 stands on**

B1. **Delete the two temporary readers against the agreement test 2.5 promised.**
    `oracle/compare/Layout.java` and `oracle/lane/Paragraphs.java` still carry `// Unsure: R-053
    default` and are the live readers; three PICTURE expanders and two zoned decoders exist (the
    duplicate-helpers metric sees same-body only). Agreement test: `Layout.read` == `sir` items on
    every fixture copybook; then delete; one PICTURE expander, one zoned decoder. ~6 h.
B2. **The generated program's contract, into LANES.md now.** `JavaPrograms` launches a process from a
    command map; nothing compiles Java; NIST does not go through the lanes (`ModuleRun.run` takes the
    concrete `Cobc`), so 3.4 "NIST through both lanes" has no seam; LANE-01 binds `DD_` names while
    NIST programs ASSIGN to `XXXXX001` filenames. Specify: `static int run(Map<String,String> dd,
    String parm, InputStream sysin, PrintStream sysout)` plus a `main` adapter; ASSIGN resolves
    `DD_<assign>` then cwd; abend as a thrown `Abend(code)` printed `CEE3ABD abend Unnnn`; return
    code = exit; `ModuleRun` behind a `ProgramRunner`-style interface. In-process entry, because
    437 × 2 profiles as JVMs is ~5 minutes the pre-push budget does not have. ~6 h.
B3. **Fetch and extract the GnuCOBOL autotests now.** `fetch.sh` documents the sparse checkout and
    does nothing; no `.at` parser exists (1,447 cases, ~900 with expected stdout). An
    `AT_SETUP/AT_DATA/AT_CHECK` extractor into `oracle/fixture/autotest/*.fixture` (program, stdin,
    expected stdout, rc), count held by a test; sources under `corpus/`, never in git (R-045). ~8 h.
B4. **`rt/` and `lower/` in `modules.json` before their first file.** `rt/` imports nothing and ships
    (its own LICENSE per R-002); `lower/` imports `sir` only; `passes/` writes Java only through
    `lower/`'s emitter. ~1 h. *Check:* `ModulesTest` on the empty modules.

**C. Ordinary tithe**

C1. **`docs/KERNEL-MAP.md`:** 600 generated rows whose "stands on" is `called by X`; keep the
    superseded/removed ledger, drop the body to a `-diff` generated file or replace it with the cited
    `// Decision:` per function. ~2 h.
C2. **A quick-NIST subset** (NC+IF, ~140 programs) tagged for `prepush.sh` on `sir/`, `lower/`, `rt/`
    changes; full NIST at boundaries. Keeps the check under 3 min once javac+run are added. ~2 h.
C3. **Split `DataMap.java`** (344 lines) along its seams — reading clauses, placement, the tuple —
    before A4 adds to it. ~2 h, goldens unchanged.
C4. Re-measure; record pass 2 in `docs/AGENT.md`; journal it; the R-053 count for phase 2.

*2.8 closes when A1–A6 and B1–B4 are on `main` and R-017 is recorded in force with A1's test as what
holds it.*

## For Ghost (R-053: answered at the phase boundary; a default is named, or "stopped")

(phase 2's two answered 2026-10-05: the ANTLR tool jar accepted under R-001; che4z parked for good,
R-015)

- **The GnuCOBOL autotests against R-045 (tithe pass 2, B3).** Fable's B3 asked to fetch the
  autotests now and extract them into `oracle/fixture/autotest/*.fixture`. R-045 says `fetch.sh`
  fetches them "only in phase 3" and that GnuCOBOL's expected-result files are "never in this
  repository"; the extracted cases are its programs and expected stdout, so committing them in
  `oracle/fixture/` would be what R-045 forbids, whatever the file is called. *Default taken
  (reversible):* the extractor is written and tested on a sample written here from the Autotest format;
  `fetch.sh` fetches the autotests only when named, and phase 3's first step runs it; the cases are
  written under `corpus/gnucobol-autotests/cases/`, gitignored. To reverse: amend R-045 to allow the
  extracted cases in the tree (counsel's question as much as yours, since they are GPL test input).
- **rt/LICENSE's text (R-002; tithe pass 2, B4).** `rt/` now compiles alone against the JDK, and
  `ModulesTest` refuses it code without an `rt/LICENSE` beside it: the per-customer license R-002
  describes. The text is counsel's and yours. *Default:* none is written; phase 3's first `rt/` file
  waits on it (the test is red until it is there), so nothing ships unlicensed.

## For Opus

(tithe pass 2a done, J-071 to J-073; next: TODO.md from its first item, 2.6)

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
