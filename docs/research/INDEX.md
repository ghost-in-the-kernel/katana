# Research: the contents

Written by `gradle researchIndex`; do not edit by hand.

## The notes

- `docs/research/FEASIBILITY.md`: Deterministic COBOL-to-Java compiler clears the feasibility bar
- `docs/research/corpus.md`: Public COBOL corpus for a deterministic COBOL-to-Java migration compiler
- `docs/research/ibm-options.md`: IBM Enterprise COBOL dialect options: sourced facts
- `docs/research/incumbents.md`: Commercial COBOL Modernization Landscape (as of October 2026) — Incumbents, Claims, Pricing, Market, Acquisitions, Regulatory Evidence
- `docs/research/literature_and_failures.md`: COBOL-to-Java Migration: Literature, Failures, JOBOL, Equivalence Testing, and Proof Artifacts
- `docs/research/open_source_tools.md`: Open-source COBOL parsers and COBOL-to-Java translators: inventory, builds, and observed failures
- `docs/research/oracle.md`: Byte-for-byte output-equivalence oracle for COBOL batch: empirical feasibility
- `docs/research/reviews/2026-10-22-agent-maintainability.md`: Readability, maintainability and reentrancy for an AI agent: Fable's advice (2026-10-22)
- `docs/research/reviews/2026-10-22-divergence-report-goal.md`: The divergence report for a foreign Java port: Fable's advice on the roadmap (2026-10-22)
- `docs/research/reviews/2026-10-22-navigation-trial.md`: A navigation trial with a Haiku agent (2026-10-22)
- `docs/research/reviews/2026-10-22-test-methodology.md`: A hostile review of the testing methodology (2026-10-22)
- `docs/research/reviews/2026-10-23-super-tithe.md`: The super tithe's plan: Fable's advice (2026-10-23)
- `docs/research/semantic_traps.md`: COBOL semantics that break naive COBOL-to-Java translation (spec for the literal-lowering runtime)

## The sections of `docs/research/ibm-options.md`

| § | Heading |
| --- | --- |
| 1 | TRUNC |
| 2 | NUMPROC |
| 3 | ARITH |
| 4 | Appendix A, "Intermediate results and arithmetic precision" (read 2026-10-05, primary) |
| 5 | CODEPAGE / COLLSEQ |
| 6 | ZONEDATA / INVDATA (6.1-6.4) |
| 7 | Division by zero / SIZE ERROR |
| 8 | DFSORT padding and truncation (read 2026-10-04, primary) |
| 9 | Initial values without VALUE, and WSCLEAR (read 2026-10-06, primary; R-057) |
| 10 | The authority behind each discovery (read 2026-10-06, primary; R-058) |
| 11 | LINAGE (read 2026-10-08, primary; for the SQ module's 39 LINAGE programs) |
| 12 | FILE STATUS and EXCEPTION/ERROR declaratives (read 2026-10-09, primary; the SQ module) |
| 13 | Variable-length records (read 2026-10-09, primary; SQ106A, SQ107A, SQ134A, SQ212A, SQ218A-SQ224A, SQ227A, SQ228A) |
| 14 | COPY REPLACING and REPLACE (read 2026-10-16, primary; NIST SM) |
| 15 | Intrinsic functions (read 2026-10-16, primary; NIST IF) |
| 16 | Segmentation (read 2026-10-16, primary; NIST SG) |
| 17 | Debugging (read 2026-10-16, primary; NIST DB) |
| 18 | Obsolete elements (read 2026-10-16, primary; NIST OB) |
| 19 | Report Writer (read 2026-10-16, primary; NIST RW) |
| 20 | Relative files (read 2026-10-16, primary; NIST RL) |
| 21 | Indexed files (read 2026-10-16, primary; NIST IX) |
| 22 | CardDemo's KSDS jobs through the Java lane, and CEE3ABD (read 2026-10-16, primary; phase 4.1) |
| 23 | SORT and MERGE (read 2026-10-16, primary; NIST ST) |
| 24 | CALL, CANCEL and nested programs (read 2026-10-16, primary; NIST IC, phase 4.3) |
| 25 | The z/OS storage facade: POINTER, SET ADDRESS OF, and the PSA, TCB and TIOT (read 2026-10-16; phase 4.4, R-027) |
| 26 | The four jobs through both lanes (phase 4.5; no new IBM text) |
| 27 | Proof report v1: the four jobs, the java lane against the cobc lane (phase 4.6; no new IBM text) |
| 28 | The Intent IR's types (phase 5.1; no IBM text: the facts are Katana's own) |
| 29 | The first inference rules, measured against a hand-written key (phase 5.2; no IBM text) |
| 30 | The review file: a person's word on each fact, kept across a rebuild (phase 5.3; no IBM text) |
| 31 | The proof ledger: append-only lines over the Java a build makes (phase 5.4; no IBM text) |
| 32 | The rules' version 2: use as evidence (R-065; phase 5.2's answers, J-199) |
| 33 | The first review file's second reading: tests and documents for what is under certainty (R-066; J-204) |
| 34 | The remaining review tests, built (R-066; J-205) |
| 35 | 6.1, pattern structuring: a Function range becomes a method (R-029, R-032; J-210) |
| 36 | 6.1's third pattern: a PERFORM VARYING with a monotone counter becomes a `for` (R-032; J-211) |
| 37 | 6.2, method extraction under the dispatcher: a range's paragraphs as the method's own code (R-032 to R-034; J-212) |
| 38 | 6.2b, the main line as `run`'s own code and the loops as labeled `while`s (R-032 to R-034; J-213) |
| 39 | 6.3, scalar promotion: an item no bytes alias becomes a typed Java variable (R-032, R-068; J-215) |
| 40 | 6.3's remaining verbs: a statement that writes a promoted item's bytes runs on a field of its own (R-068; J-216) |
| 41 | 6.4, exact-arithmetic substitution: an expression IBM carries without a cut is plain arithmetic (R-020, R-032; J-217) |
| 42 | 6.5, enum, flag and money types from the intent facts, behaviour kept whatever a fact says (R-029, R-070; J-218) |
| 43 | 6.4b, a record field in exact arithmetic, under a guard (R-069; J-219) |
| 44 | 6.6, the dataflow, and stateless subprograms (J-221) |
| 45 | 6.6b, the PICTURE rule: a store whose value always fits, written as plain arithmetic (R-068; J-222) |
| 46 | 6.7, record classes as typed views over a record's bytes, with fixed tables (J-223) |
| 47 | 6.7b, LINKAGE records as record classes (J-224) |
| 48 | 8A.1's trial: a foreign Java port run through the lanes, and refused when it ignores the DD contract (J-229) |
| 49 | 8A.2, the divergence catalogue as data: the first three kinds, each a generator and a mutation (J-230) |
| 50 | 8A.3, boundary inputs from the IR, with the events each drives counted where it happens (J-233) |
| 51 | 8A.4, the alignment file: a foreign port's Java mapped onto the COBOL by the program's own names (J-234) |
| 52 | 8A.5, attribution: which divergence kind reproduces a field's difference, by probe (J-236) |
| 53 | 8A.6, the divergence report: each divergence proven or possible, its statement, cause, trace and fix (J-237) |
| 54 | 3.1, the runnable autotests through the lowering: a first sweep of all 378, outcomes counted by stage (J-246) |
| 55 | 3.1, a reason for each autotest that does not parse, and the causes by count (J-248) |
| 56 | 3.1, the nine autotests whose lowered Java does not compile: three causes, one fixed (J-248) |
| 57 | 3.1, the census gives the parser the case's own directory as its copybook path (J-249) |
| 58 | 3.1, the `>>` directives: conditional compilation and `>>D`, the rest left as errors (J-251) |
| 59 | 3.1, the `>>` directives amended: a name from outside the source, compiled-out code seen, IBM's own directives (J-252) |
| 60 | 3.1, variably located items: what follows an OCCURS DEPENDING ON table is located after its current length (R-088; J-265) |
| 61 | 3.1, `ROUNDED MODE`: ten autotests that do not parse, and what accepting the phrase would need (J-268) |
