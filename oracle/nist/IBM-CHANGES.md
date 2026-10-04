# NIST under `-std=ibm`: what changes from `-std=cobol85`

Phase 1.2 (docs/ROADMAP.md): the NIST run under GnuCOBOL's IBM dialect, its golden
`programs-ibm.golden` (the roadmap's `nist-ibm.txt` baseline) beside the cobol85 golden
`programs.golden`. Every run whose outcome differs between the two is a line below, with its cause.
`DialectChangesTest` fails on a change with no line, a line with no change, or a cause that does not
hold. The IBM profile's own X-cards (`exec85-ibm.cards`: the switches are UPSI-0 and UPSI-1) are
part of the profile and are not changes.

Causes, a fixed list (`DialectChanges.java`). The first three are behaviours the profile must model
(P-12): each is one GnuCOBOL flag the IBM dialect sets, and the number after it is the failures that
flag alone produces when added to the cobol85 run; on a line they must add up to the run's added
failures.

- `arithmetic-osvs` (`-farithmetic-osvs`): intermediate results limited to the precision of the
  result, IBM's rule (F-07); the profile's ARITH.
- `binary-truncation` (`-fno-binary-truncate`): binary items not truncated to their PICTURE; the
  profile's TRUNC.
- `section-segments` (`-fsection-segments=ignore`): segment numbers ignored, so an independent
  segment keeps its state.
- `reserved-word`: the program uses a word the IBM dialect reserves; a test that depends on the
  dialect's reserved-word list, not a behaviour to model.
- `follows`: the program depends on one the dialect no longer compiles.

| Program | Run | Causes | Follows | Why |
| --- | --- | --- | --- | --- |
| NC105A | 1 | `binary-truncation=2` | - | MOVE (DISPLAY/COMP): binary items no longer truncated to their PICTURE |
| NC106A | 1 | `arithmetic-osvs=39, binary-truncation=3` | - | SUBTRACT with SIZE ERROR, COMP against DISPLAY |
| NC118A | 1 | `arithmetic-osvs=3` | - | SIGN tests over a series of intermediate results |
| NC119A | 1 | `arithmetic-osvs=6` | - | SUBTRACT GIVING, ROUNDED and SIZE ERROR |
| NC175A | 1 | `arithmetic-osvs=8` | - | SUBTRACT GIVING (format 2) |
| NC176A | 1 | `binary-truncation=3` | - | ADD with SIZE ERROR, COMP against DISPLAY |
| NC177A | 1 | `arithmetic-osvs=3` | - | ADD GIVING |
| NC205A | 1 | `arithmetic-osvs=1` | - | a conditional over a COMPUTEd value |
| NC211A | 1 | `reserved-word` | - | declares `77 NOTHING`; NOTHING is reserved under the IBM dialect |
| NC250A | 1 | `arithmetic-osvs=1` | - | IF ... EQUAL over an arithmetic expression |
| SG102A | 1 | `section-segments=1` | - | segmentation: IBM treats segment numbers as comments |
| SG103A | 1 | `section-segments=3` | - | the initial state of an independent segment on re-entry |
| SG201A | 1 | `section-segments=23` | - | SEGMENT-LIMIT; one test fewer is reached (79 to 78) |
| SG203A | 1 | `section-segments=4` | - | the initial state of independent segments |
| ST115A | 1 | `reserved-word` | - | uses `CHAR` as a data name; CHAR is reserved under the IBM dialect |
| ST117A | 1 | `follows` | ST115A | sorts the file ST115A's chain writes; ST115A no longer compiles |
| ST137A | 1 | `reserved-word` | - | uses `CHAR` as a data name |
| ST139A | 1 | `reserved-word` | - | uses `CHAR` as a data name |
| ST140A | 1 | `reserved-word` | - | uses `CHAR` as a data name |
| ST144A | 1 | `reserved-word` | - | uses `CHAR` as a data name |
| ST147A | 1 | `reserved-word` | - | uses `CHAR` as a data name |
