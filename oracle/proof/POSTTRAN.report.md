# Proof report v0: POSTTRAN

- Fixture: `oracle/fixture/carddemo/POSTTRAN.fixture`, sha256:a1f1395c632a730634013eb0394b8a58b223f0cd103c4a2948ae5f4e2e259906
- Profile: ibm sha256:feca25654120f84bcd60468e51f9dfe083ab409a44bac92c65771ab6e525fcfc
- Expected: the cobc lane. Actual: the study's frozen datasets (F-02, oracle/prototypes/carddemo/post1).

## Gate

Passed: the cobc lane's two runs are identical.

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.DALYREJS.G0001V00 | none | the same |

Every dataset compared is the same.

## Coverage (from a cobc trace)

| Program | Paragraphs entered | Not entered |
| --- | --- | --- |
| CBTRN02C | 24 of 26 | 9999-ABEND-PROGRAM, 9910-DISPLAY-IO-STATUS |

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
