# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0, 1 and 2 are
done (1.5 with one part of its check deferred, below; 2.2 deferred to 7.2, R-015), and tithe pass 2
with them (J-091). Phase 3 starts after the boundary: Fable answers "For Fable" and Ghost "For Ghost"
below; 3.1's first step is the autotest fetch TODO names.

## For Fable (R-053: answered at the phase boundary; the default taken is named)

(the "check is worth what it can refute" proposal of J-078 is recorded as R-056, with the
consequence clause added: a verdict must stop an action; the meta-test is tithe item A7)

(2.8's tithe evaluation and plan: written 2026-10-05, below under "For Opus: tithe pass 2")

(R-056's first trial worded into its Claims 2026-10-05: C-3 refuted as stated and restated as
"independence is declared, not detected"; the path rule is a prompt, not a refusal; C-2 stands)

- **The intermediate-results trial (J-097), against the defaults you set.** Run under cobc `-std=ibm`,
  operands as data items. Truncation: held (1.98, not 2.01). ROUNDED's place in dmax: refuted (1.8,
  not 2.0), so `ArithProfile.dmax` adds no place, with an `// Unsure` that IBM's own "might be
  carried" (p. 794) is unsettled on IBM. The Language Reference (p. 296) verifies the final store.
  One more: with all-literal operands cobc folds the expression at compile time with more places
  (1.99, 1.9, the control failing as you said it would if the fixture were wrong); IBM's text has no
  such rule, so the lanes may differ on literal-only COMPUTEs. *Lean:* a finding against cobc's
  dialect, and the lowering keeps IBM's table for literals too.
  (Answered, Fable 2026-10-05: both leans hold and are in R-020's claims. The folding is C-5, a
  finding against cobc, and the lowering applies the table to literals; a mainframe may yet show IBM
  folds too, so the comparator excludes all-literal COMPUTEs from the 3.3–3.4 lane comparison by
  finding, not by silence. Hand-lowering: the autotest programs are input and may be read (R-045
  keeps GnuCOBOL's expected results and its code off screen, not the programs it tests); a failing
  comparison reports pass/fail and Katana's own output, never the expected side; 378 is the count.
  Fable's ROUNDED default was refuted by the trial; recorded as such.)

- **J-097 was wrong on ROUNDED, and the chain that would have caught it (J-099).** Ghost's hunch led
  to an audit: `COMPUTE R ROUNDED = A / B` into 9V9 (2 and 3) is 0.7 on cobc `-std=ibm`, not the 0.6
  "ROUNDED adds no place" predicts. The rule is now ROUNDED's place on the last operation
  (`ArithProfile.last`), with an `// Unsure` for IBM itself. Ghost proposed, and the audit supports,
  an evidence chain for every semantic claim, each arrow justified: IBM's text, then a formal rule
  (a parameter a mutation can change), then an executable model independent of `rt/`, then the
  oracle (cobc), then a differential test whose cases separate every competing reading, then a
  mutation per reading, then the regression test, then the implementation. J-097 skipped the
  independent model and chose cases that could not separate two readings; R-056 held, and still the
  claim was wrong. *Lean:* an amendment to R-056 (or a decision of its own) for semantic claims, and
  an independent model of Appendix A before `rt/Decimal`, which Opus builds next. R-020's C-2 (recorded
  from J-097 in the same hour) says "ROUNDED adds no place to dmax": still true, but it needs J-099's
  half, the place on the last operation (`ArithProfile.last`), which changes `COMPUTE R ROUNDED = A / B`.

- **cobc's ibm dialect does not apply the 30-digit cap (J-101).** `rt/Decimal` landed with an
  independent model of Appendix A (exact fractions, the table as data rows, no code shared with
  `rt/`) and a differential test: 400 generated COMPUTEs, three ways. Model and `rt` agree on all 400;
  cobc agrees on every case no intermediate of which passes the cap, and on 33 of the 40 that do. In
  the other 7 cobc gives exactly what the model gives with no cap at all. Per your rule, a finding
  against cobc, and Katana follows IBM's table. The consequence: the cap rows have no executable
  oracle, only IBM's text and a model written from it by the same agent that wrote `rt/` (the
  judgment half of R-056). *Lean:* name them in R-020 as text-verified, oracle-absent, and add them
  to what a mainframe run settles, with ROUNDED's place.
  (Answered, Fable 2026-10-05: the lean holds, recorded as C-6 in R-020 with status `hypothesis`, not
  `verified`: a model written from the same text by the same agent is a second reading, not a second
  source. One addition: it must reach the proof report. A program whose theoretical places can pass
  30 gets a finding, "arithmetic past the intermediate cap: proven on IBM's text, not on an IBM run";
  the lowering can tell from the PICTUREs alone, so it costs nothing at run time and fires on few
  real programs. That is what the hypothesis status was built for in R-049.)

- **cobc sizes a COMP-5 of 1 or 2 digits as 1 byte; IBM as a halfword (J-102).** Seen while holding
  `rt/Field`'s byte images to cobc; `-fbinary-size=2-4-8` does not change it, and no corpus program
  has such an item, which is why the data map's cobc check never met it. `rt/` and the data map keep
  IBM's size; a record holding one differs in length between the lanes, so 3.4 compares such a
  program by a finding (TODO 3.4). *Lean:* a finding against cobc's dialect, like C-5 and the cap.

- **cobc truncates a numeric MOVE into a shorter JUSTIFIED receiver on the right (J-103).** IBM (LR
  p. 198): with JUSTIFIED "the leftmost character positions are truncated"; cobc keeps the leftmost
  digits when the sender is numeric (an alphanumeric sender it justifies as IBM says). `rt/` follows
  IBM; 5 of 400 generated moves are listed findings. *Lean:* a finding against cobc, compared by
  finding at 3.4 like C-5, the cap and COMP-5's size.

- **Editing: five ways cobc parts from IBM's text, and one reading to settle (J-104).** `rt/Editor`
  is held to every example IBM prints for editing (LR pp. 220-225, `EditorTest`) and to cobc over 400
  generated edited moves. 37 of them differ from cobc, in five families each checked by what cobc shows:
  a + or - next to the currency sign (cobc gets IBM's own `-$999.99` example wrong; 17 cases);
  suppression judged on the value before its digits were cut (3); DB not read as negative when
  de-editing (19); an inserted 0 read as a digit when de-editing (1); a fixed trailing - shown for a
  negative value cut to zero (2). The reading: a negative value whose digits are cut to zero (-0.006
  into `9.9CR`). IBM's sign table has a column "positive or zero"; p. 404 says "the sign of the sending
  item is placed in the receiving item". *Default taken:* zero shows as positive (the table, and what
  cobc does for +, CR, DB and floating signs), with an `// Unsure` in `Editor.number`. *Lean:* the five
  families findings against cobc, compared by finding at 3.4; the zero sign to the mainframe list.

- **INSPECT: one cobc finding, one reading taken from cobc (J-105).** `rt/Inspect` is held to IBM's
  four INSPECT examples (LR pp. 356-360) and to cobc over 402 statements. The finding: cobc runs a
  REPLACING's phrases one at a time, a position claimed by an earlier phrase barred to later ones;
  IBM's "Data flow" (p. 360) is comparison cycles, position by position. They part only when a later
  phrase matches where an earlier one is not yet eligible: `CHARACTERS BY "y" AFTER INITIAL "B"
  FIRST "BA" BY "zz"` on `BA-ABA` is `zzyyyy` by IBM's text and `Byyyyy` on cobc (3 cases; the
  phrase-at-a-time reading fits cobc on all 402). The reading: with both BEFORE and AFTER, IBM does
  not say where BEFORE's delimiter is searched from; cobc searches from the item's start, and `rt/`
  does the same (`// Unsure` in `Inspect.region`). *Lean:* the first a finding compared by finding at
  3.4; the second to the mainframe list.

- **SIZE ERROR: cobc judges a COMP-5 receiver by its binary capacity (J-108).** IBM (LR p. 296): for
  a resultant identifier of usage BINARY, COMP, COMP-4 or COMP-5, "the largest value that the resultant
  data item can contain is the value implied by the item's decimal PICTURE character-string, regardless
  of the TRUNC compiler option". cobc raises no size error on a COMP-5 receiver while the value fits its
  halfword, fullword or doubleword, and stores it (6 of 300 generated statements, each checked to be
  exactly that reading). `rt/` follows IBM. One doubt: a division by zero without ON SIZE ERROR leaves
  the receiver unaltered in `rt/` (an `// Unsure`); IBM's text gives the truncation rules no value to
  work on, and a mainframe may end the program. *Lean:* the first a finding compared by finding at 3.4;
  the second to the mainframe list, the generated tests keeping division by zero under the phrase.

- **DISPLAY of signed numbers: a profile field, and two cobc findings (J-109).** IBM displays a signed
  binary, packed or overpunched zoned item by DISPSIGN (Programming Guide 6.4, Table 48): COMPAT, the
  default, overpunches a negative value's last digit (`11J`) and shows a zoned item's bytes as they are
  (`33C`); SEP puts a leading separate sign (`-111`, `+333`). `rt/Display` does both, held to every row
  of the table. cobc -std=ibm is neither: packed items as SEP; binary items with the digits of their
  binary field, not their PICTURE (`00111` for `9(3) COMP`); overpunched zoned items with the sign
  separate in the overpunch's place (`333-`). Over 300 generated items, rt under SEP equals cobc but for
  those two families, each case checked against a model of cobc's display (150 and 5). *Proposal:* a
  profile field `ibm.dispsign` (`COMPAT` default, or `SEP`), a new profile per PROFILE's rule, which
  the lowering passes to `Display.of`; the cobc lane compares under SEP with the two families as
  findings. Not added: a profile field is yours to word (R-042).

- **3.1's check, the 378 autotests through `rt/`: what is hand-lowered (default taken, R-053).** Most of
  the 378 test what `rt/` will never hold by hand (CALL and the C API, files, profiling, debugging
  lines, dumps, screen I/O), and their expected outputs are cobc's default dialect, not IBM's (DISPLAY
  of a decimal item shows a point; IBM's does not). *Default:* hand-lower the cases whose statements
  `rt/` holds (moves, editing, INSPECT, STRING, UNSTRING, comparison, arithmetic, numeric data of
  every usage; about 50 by title), each compared with the testsuite's expected output (pass or fail
  only, R-045) and with cobc -std=ibm on the same program; the rest go through the lowering at 3.3.
  The count hand-lowered and passing is held by a golden, so the check says how far it reaches.

- **R-021's C-1, PERFORM return: what cobc says, and what IBM leaves open (J-113).** `rt/Dispatcher`
  returns from the innermost active PERFORM whose range ends at the paragraph reached, dropping those
  begun inside it. Three readings were probed by hand on one program (an inner PERFORM left by GO TO
  for the outer range's end, then reached again): the ANSI reading (only the innermost exit returns),
  this one, and a slot per exit paragraph (an abandoned inner PERFORM stays armed). cobc -std=ibm is
  this one; cobc's default dialect is ANSI's. IBM's text says such control "must not" pass through
  another active PERFORM's exit (LR p. 414), so it does not settle the reading; its examples there are
  figures. `DispatcherDifferentialIntegrationTest`: 81 programs, all equal to cobc -std=ibm; the ANSI
  reading differs on 21. *Lean:* C-1 recorded as verified against cobc -std=ibm, still a hypothesis on
  IBM, and the shape (control passing an active PERFORM's exit) a finding the lowering raises, as
  NE-02 and NE-03 are for recursion and overlap.

- **The lowering's first increment, and five readings it took (J-114).** `lower/` exists (its entry was
  already in `docs/modules.json`): data map, VALUEs, the dispatcher, MOVE, DISPLAY, the arithmetic verbs
  with ROUNDED and SIZE ERROR, IF, PERFORM in its forms, GO TO (DEPENDING, ALTER), INSPECT, STRING,
  UNSTRING, INITIALIZE. Each reading below is an `// Unsure` in the code, the default taken:
  (1) an item without VALUE starts as INITIALIZE would set it (zero or spaces), as cobc -std=ibm does;
  IBM leaves it unpredictable. (2) A numeric literal DISPLAYed shows its sign and digits, no point, as
  cobc -std=ibm shows it. (3) The program collating sequence is native until the IR carries the
  ALPHABET clause. (4) An index holds its occurrence number in a fullword (IBM: a displacement). (5)
  R-021's "one case per paragraph" is kept as one case per stretch of statements, each a method, so a
  NIST-sized program stays under the JVM's 64 KB method limit; the dispatcher is unchanged.
  *Lean:* (1) a decision of its own (it changes what a program prints when it reads before it writes);
  (2)-(4) the IR's to carry, then no reading is left; (5) R-021's wording amended.
- **A front-end bug, not yet fixed: the comma in numeric literals (J-114).** The lexer
  (`front/grammar/Cobol.g4`, NUMERICLITERAL) takes a comma as the decimal point in every program, so
  `DISPLAY 1,1,1` without DECIMAL-POINT IS COMMA reads as `1,1` and `,1`. IBM's text makes a separator
  comma one followed by a space, so the program is not conforming, but the reading is wrong under any
  rule. The fix needs the lexer to know the clause, and touches the NIST goldens; TODO 3.3 carries it.

## For Opus: tithe pass 2, the phase-2 boundary (done: J-076 to J-091; kept as the plan it was)

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

(phase 2's boundary, answered 2026-10-05: **B3** — Opus's default holds and R-045 stands; the plan
was wrong, not the decision: the extractor is in the tree, the cases live under
`corpus/gnucobol-autotests/cases/` gitignored, and 3.1's first step fetches them. **rt/LICENSE** —
drafted by Fable from R-002 as the per-customer template, status DRAFT awaiting Ghost's word in the
file's last paragraph; the first `rt/` file may land against it, and counsel reviews before the
first delivery.)

(phase 3.1's Appendix A: Ghost allowed the two IBM hosts, 2026-10-05; read in full and quoted, J-095.
Fable recorded R-019 to R-024 the same hour; R-020's condition for the IBM profiles, "a finding until
Appendix A is read", is met: `rt/ArithProfile` holds the IBM tables, and R-020's text is Fable's to
update; updated the same day, and again after J-097 settled truncation and ROUNDED's place.)

## For Opus: tithe pass 2, the phase-2 boundary (done: J-076 to J-091; kept as the plan it was)

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

(tithe pass 2a done, J-071 to J-073; tithe pass 2 done, J-076 to J-091; next: the boundary, then 3.1)

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
