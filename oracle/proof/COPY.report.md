# Proof report v0: COPY

- Fixture: `oracle/fixture/trivial/COPY.fixture`, sha256:6c331777f8d6c00b2cf811d092b85fa2012d855b0877494801d09449d1722ada
- Profile: ibm sha256:a5298bfd77c97fd3f26b37103ba2e6ed51a164dbd90b0435ff2d88f27939e77b
- Expected: the cobc lane. Actual: the java lane (a hand-written Java copy).

## Gate

Passed: the cobc lane's two runs are identical.

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| KATANA.TRIVIAL.OUT | none | the same |

Every dataset compared is the same.

## Coverage (from a cobc trace)

| Program | Paragraphs entered | Not entered |
| --- | --- | --- |
| COPYREC | 0 of 0 | (no named paragraphs) |

## What it never covers (docs/PROOF.md, verbatim)

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
