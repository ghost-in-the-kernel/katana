# What a proof is

What a proof covers and what it never covers. Every proof report quotes the section "What it never
covers" verbatim (`test/katana/oracle/proof/ProofReportTest.java`), so a buyer reads the same limits
the code is held to. Written in phase 1.9 from R-013's sentence in docs/ROADMAP.md; wording settled by Ghost at the
phase-1 boundary (R-013).

Invariants are numbered PROOF-01 onward, in this document only, and a number is never reused.

## What a proof is

A proof is a report that one lane's run of a fixture and another's (or a lane's and recorded gold)
wrote the same datasets, field by field under their copybooks, after the original lane ran the job
twice and the two runs were identical. It names the fixture and the profile by hash, the gate's
result, every dataset compared, every mask applied and every finding, and which of the program's
paragraphs the fixture entered.

## What it covers

The datasets the fixture's job writes, from the datasets the fixture states, under the profile and
the clock the fixture pins, on the paragraphs the fixture's data entered. Nothing else.

## What it never covers

- Environment configuration: the job's real JCL, catalog, security, region sizes and site options
  beyond what the fixture states.
- Data outside the fixtures: a record of a shape, a value or a volume no fixture held.
- Paragraphs the fixtures never entered: they are listed in the report, and proven by nothing.
- Concurrency: jobs and transactions that run at the same time, their locking and their ordering.
- I/O performance, before or after a change of storage: how long a job takes is not compared.
- Governance: who may run the job, change it, or read what it writes.
- The compiler's own semantics beyond GnuCOBOL's reading of them: until a customer's mainframe output
  reconciles the lanes on the same fixture, a proof says "equivalent under profile X as GnuCOBOL reads
  it", and says so (R-013).
- Intent: a proof says what the program does on the fixture's data, never what it was meant to do.

## Invariants

- **PROOF-01 Every proof report quotes "What it never covers" verbatim.** A test fails when the
  quote and this section differ.
- **PROOF-02 A proof is valid for one fixture and one profile.** Both are named by hash (P-12).
- **PROOF-03 Nothing is compared before the gate passes.** A report whose gate failed says so and
  compares nothing.
- **PROOF-04 Coverage says where it comes from.** v1 (4.6, research §27) proves the java lane against the
  cobc lane; each program's coverage comes from the cobc trace, and from the java lane's own trace only
  where the cobc lane cannot run the program (R-027), the report saying which. On every program both lanes
  run, the java lane's trace enters exactly the paragraphs cobc's does (`ProofV1IntegrationTest`). A dataset
  the cobc lane leaves none of is listed as not compared, never as the same. `oracle/proof/CARDDEMO.coverage.md`
  sums the coverage over the fixtures, listing every paragraph none entered (J-191).
