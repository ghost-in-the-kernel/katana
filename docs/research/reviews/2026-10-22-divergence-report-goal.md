# The divergence report for a foreign Java port: Fable's advice on the roadmap (2026-10-22)

Ghost's goal (2026-10-22): "We want to be able to take in a finished java implementation, and a cobol source code.
Then, we want to generate a report about potential diversions between the java logic and the cobol logic. We want
the report to say why the diversions occured and to map the steps of the logic through the conversion process. We
offer a potential fix in logic if possible."

A Fable advisor wrote this, read-only, on Ghost's word ("lets spin up a fable advisor to look at state of the
roadmap, and the goal"). It is a model's reading (R-029, R-031). Nothing in it changes the roadmap until Ghost
chooses.

**Opus checked these claims against the code (2026-10-22):**
- `oracle/lane/JavaPrograms.java` runs a named command per PGM as a process under the DD contract.
- `lower/prototypes/oc4j-carddemo-reference/` holds opensourcecobol4j's Java for the CardDemo batch programs.
- R-004 and R-058 are as cited.
- `docs/research/semantic_traps.md` has its sections of causes.
- The ten `rt/` differential tests exist under `test/katana/rt/`.

## 1. The goal, and what it leaves open

**Input:** a COBOL batch program, with its copybooks and profile, and a Java port of it that Katana did not write.

**Output:** each place the Java may behave otherwise than the COBOL; the COBOL semantic it misses; a step-by-step
trace from the COBOL statement to the Java; and a fix where one is possible.

| Question | Recommended default | Why |
| --- | --- | --- |
| Does their Java run in Katana's lanes? | Yes, first: the java lane already runs any command under the DD contract; a thin adapter maps their entry point | Dynamic differential testing is the only thing the project can call proven |
| Static or dynamic? | Both, dynamic first; static analysis only proposes where to look, and a lane run with a crafted input confirms | Nothing unconfirmed is called a divergence (the hostile review's §1) |
| Test data, supplied or generated? | Both: the customer's fixture for coverage, generated boundary inputs for each trap class | One clean dataset never drives truncation, SIZE ERROR, sign or dmax |
| Granularity of the mapping | The statement for the cause, the paragraph for the trace, the field for the symptom | The IR numbers statements by origin, the comparator reports by field, and both lanes trace paragraphs |
| What a fix is | Katana's own literal Java for that statement, with a description; never a patch to their code | Exact by construction, with its provenance; a patch to foreign code cannot be proven |
| Does R-004 cover the "why" and the "fix"? | Yes, extended: the report is deterministic; the "why" is a catalogue entry keyed by trap class, and the fix is Katana's emitted Java. A model may draft a catalogue entry offline for a person to review, never at report time | A report two runs write differently cannot be audited |

## 2. What exists that serves it

**Direct:**
- Two executable oracles for the COBOL side: the cobc lane and Katana's literal Java. The foreign Java is a third
  lane.
- The java lane already runs a foreign process (`JavaPrograms`, `ProgramRunner`).
- The comparator gives field-level differences, with bytes and decoded values. This is the symptom.
- The proof report is the divergence report's sibling.
- The paragraph and PERFORM traces give the COBOL half of the trace.
- The Semantic IR with origins, and its contract, list what a port must honour.
- NonExact findings (NE-, HY-) mark the places no port can be exact; the report lists them apart.

**Indirect:**
- `semantic_traps.md` is the seed of a catalogue of causes, but no test holds it.
- The `rt/` differential tests are seeded boundary generators per trap class, pointed at `rt/` rather than at a
  customer program.
- The ledger and provenance map give the shape of an alignment, but only for Katana's own Java.
- The oc4j CardDemo reference is a ready foreign port to develop against (SUSPECTED usable; licence noted in §5).
- DISCOVERIES.md (R-058) is the form for a proven defect in someone else's tool.

## 3. The gaps, ranked

1. **Aligning foreign Java to COBOL statements and fields.** Nothing on the roadmap builds it, and it is the hard,
   unsolved part. Names and literals will match a fraction. SUSPECTED: it needs the customer to confirm an
   alignment file (R-064's review-file pattern).
2. **Attributing a difference to a cause.** This needs the trap catalogue as data, held by a test with a generator
   per class, and a probe per class to see which one reproduces the difference.
3. **Inputs that drive the divergence-prone paths,** generated from the IR so they cross each receiver's boundaries:
   truncation, sign, scale, dmax and collation.
4. **An adapter that runs foreign Java under the DD contract,** and a refusal with a finding when a port cannot be
   wrapped.
5. **Tracing foreign Java.** In v1 the trace is COBOL-side only, and the report says so.
6. **The fix.** Cheap once 1 and 2 exist.
7. **Abends and hangs,** a divergence of their own kind.

8.5 as written ("anyone's Java against the COBOL under the same fixture") is gap 4 only: a SAME or DIFFER verdict,
with no cause and no trace.

## 4. Proposed roadmap change: a phase 8A, "the divergence report"

Phase 8A sits between phases 7 and 8 and absorbs 8.5:
- **It comes after 7.1**, because a customer program is CNAF-sized.
- **It may come before 7.2 to 7.5** for batch ports.
- **It makes the hostile review's §1 and §2 more urgent**, since the same generators serve both.

Each of its items: a research section with a prediction first, findings never silent, and no model at report time.

| Item | What it builds | Check |
| --- | --- | --- |
| 8A.1 The DD adapter and refusal | `katana prove --java <cmd>` runs a foreign command as the java lane's step; a port that ignores LANE-01 to 04 is refused with a finding | An oc4j CardDemo program, or a hand port of COPYREC, runs a fixture through the lane and the comparator; a port that ignores `DD_` is refused |
| 8A.2 The trap catalogue as data | `DV-01` onward from `semantic_traps.md`, each with IBM's text, the COBOL rule, the wrong Java shape, Katana's exact Java and a generator; a doc test holds it | Every kind's generator gives a program and inputs on which its wrong shape differs from cobc |
| 8A.3 Boundary inputs from the IR | For each statement, inputs crossing its receivers' boundaries, as a fixture | On CardDemo it enters every paragraph the sample data does, and drives a SIZE ERROR, a high-order truncation, a negative into an unsigned item and a dmax division the sample data never does, counted by `rt/` execution counters |
| 8A.4 The alignment file | `<PROGRAM>.align`, a COBOL node to a Java symbol or line range, inferred, `unconfirmed` until a person confirms | Coverage on the oc4j reference, predicted first; a node the IR lacks is refused |
| 8A.5 Attribution | For each differing field, the catalogue kinds whose probe reproduces it, ranked; "unattributed" otherwise | A mutation harness: Katana's literal Java with one rule broken, run as the foreign port, is named by its kind first, one mutation per kind |
| 8A.6 The report | Per divergence: `proven`, by a cited record, or `possible`, static only; the COBOL statement and origin; the aligned Java; the cause; the trace; the fix. NE- and HY- findings apart | PROOF-01's quote is present; a `proven` line without its fixture is refused; two runs write identical reports |
| 8A.7 Tithe, and the first external letter | One report read outside the project (8.6's rule) | |

## 5. Risks, and keeping the report honest

- **"Why" is a hypothesis about someone else's code.** The claim is "this input differs, and the difference matches
  DV-03's signature", never "your Java forgot truncation". `proven` and `possible` are kept apart and counted in the
  header. There is no divergence without a reproducer, and no "same" for a path no input entered (PROOF-04).
- **The oracle is cobc, not IBM** (R-013, the review's §5). Where cobc and `rt/` disagree, the report says which it
  believed and why.
- **Legal.**
  - Customer Java never enters the repository (R-037).
  - The oc4j reference is GPL tool output and its runtime is LGPL: run it as a process only. Ghost's call.
  - A fix shipped as Katana's lowering discloses the lowering's shape before the provisional patent is filed, so
    under NDA only (R-037).
- **Technical.**
  - Alignment may need the customer's hand.
  - A port may reject generated inputs; that is a divergence too, which the report must say.
  - Frameworks such as Spring Batch and JDBC need an adapter per shop.
- **Self-grading.** The generators and the mutation harness must be red before each kind is built.

## 6. Three first steps

1. Run a foreign port through the lane today: one oc4j CardDemo program, or a hand-written COPYREC port, wrapped to
   the DD contract, through `JavaPrograms` and `Compare`. Record what had to be adapted.
2. Turn three traps into data, with a mutation each: high-order truncation, scale truncation against HALF_UP, and
   dmax division. Predict the catch rate before running.
3. Write the roadmap section and TODO items, with 8A.3's check the hostile review's §1 list, so the same work pays
   the project's own debt first.
