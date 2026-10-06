# Proof report v0: COPY

- Fixture: `oracle/fixture/trivial/COPY.fixture`, sha256:b5da97a51cea180aac0a96cca50b6bf3ebfc45ee71937883e1909a5a27fe9d3c
- Profile: ibm sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296
- Expected: the cobc lane. Actual: the java lane (a hand-written Java copy).
- Initial values: an item without VALUE is assumed to start as INITIALIZE would set it, not proven (R-057).

## Gate

Passed: the cobc lane's two runs are identical.

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| KATANA.TRIVIAL.OUT | none | the same |

Every dataset compared is the same.

## Findings

None.

## Valid under hypothesis

None.

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
