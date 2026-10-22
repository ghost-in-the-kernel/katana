# A hostile review of the testing methodology (2026-10-22)

An Opus agent wrote this review, read-only, on Ghost's word ("spin up an opus agent to do a hostile review of the
testing methodology"). It is a model's reading. No model reading confirms a fact (R-029, R-031). Its CONFIRMED marks
mean the reviewer saw the code it cites. Each item becomes work only when Ghost places it. Figures it took from
`build/` may be stale.

## 1. Pass proofs depend on a few clean inputs, and nobody measures whether a pass's sites ran (CONFIRMED)

**Claim it undermines:** each pass is proven because NIST and CardDemo run the same structured as literal.

**Evidence:**
- `JavaRunner.java:118` builds the structured build with `Structuring.plan`, and that one plan applies every pass
  at once (`passes/Structuring.java:69-76`). No pass ever runs alone, and none ever runs with an obligation
  removed.
- CardDemo runs one valid sample dataset per job.
- Coverage is per paragraph only. Nothing records whether any of these ran:
  - the plain assignments the PICTURE rule writes;
  - the `else` branch of the 6.4b guard;
  - a promoted store that truncates or raises SIZE ERROR;
  - a stateless subprogram called twice.
- One obligation has already been missed: J-212's SORT procedures. ST146A's crash caught it; a missed obligation
  that changed a value without crashing would not have been caught.

**Failure scenario:** drop the subtraction test from `Fits.signed`. If no fixture drives a negative result into
that receiver, every integration check stays green.

**Cheapest fix:**
- An obligation-mutation harness: disable each obligation in turn, rerun the structured comparison, and list each
  mutation that survives as a gap.
- Execution counters at each rewritten site, keyed by `Lower.Plan.loop`.
- Small COBOL programs that drive each obligation's boundary through both lanes and cobc.

## 2. NIST "equality" compares five counts, not the report (CONFIRMED)

**Evidence:**
- `NistJavaLaneIntegrationTest.java:241`: `rendered()` reduces a run to `total pass fail deleted inspect`.
- `structured()` compares only that string, plus the abend and its reason.
- 19 golden runs have inspect tests, which check printed output, and 17 already fail tests under cobc.

**Failure scenario:** a structured run prints a wrong amount in an inspect test, or fails test 12 where cobc fails
test 7. Both read SAME.

**Cheapest fix:** compare the full per-test report, between literal and structured and between the Java lane and
the cobc golden.

## 3. The gate's suite selection skips oracles that common changes need, and CI is off (CONFIRMED)

**Evidence:**
- `scripts/prepush.sh` maps:
  - `rt/*` to `rt quick javalane`, with no CardDemo lane;
  - `lower/*` to `passes`, which compares structured against literal but never runs
    `CardDemoJavaLaneIntegrationTest`;
  - `intent/*` to no NIST structured comparison;
  - `sir/*` to no CardDemo lane and no passes.
- HANDOFF: CI is out of GitHub Actions minutes until 2026-11-02.

**Cheapest fix:**
- `rt/`, `lower/`, `sir/` and `intent/` changes select both `lane` and `javalane`.
- ChecksTest holds that every module feeding the structured build selects every suite that proves it.

## 4. R-056's killer mutations are never applied (CONFIRMED)

**Evidence:**
- `test/ChecksTest.java:117-123` only checks that the named mutation is a test method that exists. R-056 says the
  meta-test applies each mutation.
- The structured CardDemo check's mutation is a unit test of the plan, not a red integration comparison.
- `docs/checks.json` has no entries for scalars, exact, fits, types, records or stateless.

**Cheapest fix:**
- Inject a known-unsound plan into the structured comparison and require it to go red.
- Assert, on the integration runs, that each pass applied at least once.

## 5. The oracle is not IBM, and the two lanes share their harness (CONFIRMED)

**Evidence:**
- Both lanes run through:
  - `oracle/lane/Steps.java`;
  - Katana's own DFSORT;
  - the IDCAMS and KSDS emulation;
  - the fixture's EBCDIC sign translation.
- The lane is ASCII (`PROFILE.md:54`), so EBCDIC collation is never exercised.
- Uninitialised storage follows cobc (R-057).
- The D-005 tolerance transforms cobc's output by Katana's own model.
- One compile-option set is proven.

**Failure scenario:** a mixed digit-and-letter key sorts differently on z/OS than in both lanes.

**Cheapest fix:**
- An EBCDIC-mode lane with a hand-computed expected order.
- Report claims labelled "≡ cobc -std=ibm + Katana harness".
- Differential tests of `Dfsort`.

## 6. Floors with slack, and no per-program golden for the Java lane (CONFIRMED)

**Evidence:**
- The NIST Java lane asserts only `same >= floor`. The last report against its floor:

  | Module | Last report | Floor |
  | --- | --- | --- |
  | SG | 9 | 5 |
  | SM | 16 | 14 |

- DB 10 of 16 and IC 18 of 25 differ with no findings listed for them.

**Failure scenario:** a regression in one program offset by a gain in another stays green.

**Cheapest fix:** a per-program SAME/DIFFER golden that may only improve, every DIFFER with a finding.

## 7. The pass unit tests check generated Java text, and the per-pass unit corpus does not exist (CONFIRMED)

**Evidence:**
- `test/katana/passes/*` asserts `java.contains(...)` and checks refusal strings. None compiles or runs the Java.
- The roadmap's per-pass check asks for the pass's own autotests, which wait on 3.1 (24 of 378 reached, by hand).

**Cheapest fix:** run each unit program through the literal and structured builds and cobc, and compare stdout.

## 8. The ledger says nothing about statement rewrites (CONFIRMED)

**Evidence:**
- Exact-arithmetic and PICTURE-rule rewrites change no symbol, so they get no line.
- The pass a line is credited to is guessed from the node's prefix.
- The integration test's run field is a constant string.

**Cheapest fix:**
- A ledger line per rewritten statement, keyed by `Lower.Plan.loop`.
- The run field the hash of the report that proved it.

## 9. Self-graded artefacts (CONFIRMED)

**Evidence:**
- `-Dkatana.record` rewrites goldens and then compares against them.
- CREASTMT from STEP040 on is held to the Java lane's own earlier output.
- The research predictions are written and judged by the same agents.
- The time ceilings live in `docs/AGENT.md`, and a docs-only change runs no suite.

**Cheapest fix:**
- A recorded golden refused without a reviewed, separately committed diff.
- The ceilings in a file that is not documentation.

## 10. Smaller items (SUSPECTED)

- A timeout in both builds renders the same, so a shared infinite loop reads as equal.
- The dataflow's handling of index-names and SET UP BY between calls is not visible.
- The CardDemo stdout tolerance accepts either form per line, so the Java lane may be inconsistent within one job.

**The reviewer's bottom line:** "proven" means structured matches literal on one set of clean inputs (five counts
for NIST), and literal matches cobc on the same inputs. The two fixes that change that most are obligation mutations
rerunning the structured comparison (§1, §4), and full per-test report comparison (§2).
