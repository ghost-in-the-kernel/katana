# Proof report v1: TRANREPT

- Fixture: `oracle/fixture/carddemo/TRANREPT.fixture`, sha256:eefd4c89c44ec664007d2af97218508c9221dc09af972b13f12ffeb71c71ed8a
- Profile: ibm sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1
- Expected: the cobc lane. Actual: the java lane (the program lowered by Katana, run on rt/).
- Initial values: an item without VALUE is assumed to start as INITIALIZE would set it, not proven (R-057).

## Gate

Passed: the cobc lane's two runs are identical.

The job's own findings, in both lanes:
- step STEP05R.PRC001: IDCAMS: card 1 opens a comment in column 1, which IDCAMS does not read (columns 2 to 72), so its text is not a comment

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.CNTL(REPROCT) | none | the same |
| AWS.M2.CARDDEMO.DATEPARM | none | the same |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | none | the same |

Every dataset compared is the same.

## Findings

None.

## Valid under hypothesis

None.

## Coverage

| Program | Paragraphs entered | Not entered | From |
| --- | --- | --- | --- |

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
