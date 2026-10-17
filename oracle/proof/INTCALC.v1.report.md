# Proof report v1: INTCALC

- Fixture: `oracle/fixture/carddemo/INTCALC.fixture`, sha256:31761bdb006ce653d6769bbc6d769ab73a8fce353faf87366449e3b17dd83449
- Profile: ibm sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1
- Expected: the cobc lane. Actual: the java lane (the program lowered by Katana, run on rt/).
- Initial values: an item without VALUE is assumed to start as INITIALIZE would set it, not proven (R-057).

## Gate

Passed: the cobc lane's two runs are identical.

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.SYSTRAN.G0001V00 | none | the same |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | none | the same |

Every dataset compared is the same.

## Findings

NE-04 division without ON SIZE ERROR: CBACT04C: COMPUTE with no ON SIZE ERROR (/katana/corpus/carddemo/app/cbl/CBACT04C.cbl:464:12)

## Valid under hypothesis

None.

## Coverage

| Program | Paragraphs entered | Not entered | From |
| --- | --- | --- | --- |
| CBACT04C | 20 of 22 | 9999-ABEND-PROGRAM, 9910-DISPLAY-IO-STATUS | the cobc trace; the java lane's the same |

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
