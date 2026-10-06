# The dialect profile

The compile options a proof is valid for (docs/ROADMAP.md P-12): compiler and version, TRUNC, ARITH,
NUMPROC, code page, program collating sequence, and each one's mapping to GnuCOBOL's dialect
configuration. Content-addressed; its hash appears in the Semantic IR header, the Java manifest,
every fixture and every ledger line (R-007). `test/katana/profile/ProfileTest.java` holds this
document to `profile/`.

Built in phase 1.3, where a fixture first carries the hash, with the fields that can be stated from
what was measured: the compiler, its version, its dialect, and GnuCOBOL flags set beyond the dialect.
The IBM compile options are fields from R-049 (below), each carrying what is known of its meaning
and how well it is known; `docs/research/ibm-options.md` holds the sourced facts. Adding a field is a
new profile, with a new hash, never a change to an old one.

Invariants are numbered PROFILE-01 onward, in this document only, and a number is never reused.

## The fields

A profile is a file of `field value` lines (`#` starts a comment): `profile/ibm.profile`,
`profile/cobol85.profile`.

| Field | Value | Example |
| --- | --- | --- |
| `compiler` | the compiler the cobc lane runs | `GnuCOBOL` |
| `version` | its version, as `cobc --version` prints it | `3.1.2` |
| `dialect` | the `-std` dialect | `ibm` |
| `flag` | a GnuCOBOL dialect flag set beyond the dialect, `name=value`; repeatable | `binary-truncate=yes` |
| `ibm.trunc` | IBM TRUNC: `STD`, `OPT` or `BIN` (default `STD`) | `STD` |
| `ibm.numproc` | IBM NUMPROC: `PFD` or `NOPFD` (default `NOPFD`; `MIG` is Enterprise COBOL 4 only and is recorded as `MIG` with a finding) | `NOPFD` |
| `ibm.arith` | IBM ARITH: `COMPAT` (18 digits) or `EXTEND` (31) (default `COMPAT`) | `COMPAT` |
| `ibm.codepage` | IBM CODEPAGE: the EBCDIC CCSID (default `1140`) | `1140` |
| `ibm.invdata` | IBM INVDATA / ZONEDATA (6.1 to 6.4): `PFD`, `NOPFD`, `MIG`, or an INVDATA pair such as `FORCENUMCMP,CLEANSIGN`; `NO` for NOINVDATA (default `NO`) | `NO` |
| `ibm.version` | the Enterprise COBOL release the options are read as (defaults and option sets differ by release) | `6.4` |
| `ibm.dispsign` | IBM DISPSIGN: `COMPAT` or `SEP` (default `COMPAT`); how DISPLAY shows a signed number | `COMPAT` |
| `ibm.wsclear` | whether the customer compiles WORKING-STORAGE cleared: `NO` or `YES` (default `NO`); recorded for R-057's finding, not read by the lowering | `NO` |

The IBM fields describe the *source* program's compilation on z/OS; the cobc lane cannot set them,
and the literal lowering reads them. The program collating sequence is not a field: it comes from
the program's own `PROGRAM COLLATING SEQUENCE` clause, which the Semantic IR carries.

## What each IBM field means, and how well it is known (R-049)

Every semantic the lowering takes from an IBM field has a status: **verified** (quoted from a primary
IBM source in `docs/research/ibm-options.md`) or **hypothesis** (recollection of the Programming
Guide, not yet read). A lowering that depends on a hypothesis says so in its findings and in the proof
report ("valid under hypothesis H-n"), until the manual text is read and the status changes.

| Field | Verified (primary source) | Hypothesis (to confirm from the Programming Guide) | GnuCOBOL flag |
| --- | --- | --- | --- |
| `ibm.trunc` | STD: result truncated to the PICTURE's digits (base 10). BIN: truncated to the halfword/fullword/doubleword (base 2). OPT: assumes the data fits; "results are dependent on the particular code sequence generated". Default STD. | BIN makes binary receivers behave as COMP-5; TRUNC has no effect on COMP-5; size error under BIN/COMP-5 is against binary capacity. | `binary-truncate` (yes = STD, no = BIN); OPT lowered as STD with finding (R-022) |
| `ibm.numproc` | PFD assumes preferred signs (C +, D −, F unsigned) and skips sign fix-up; NOPFD fixes up every reference; MIG fixes up receivers only (COBOL 4) and is unsupported in 6.x. NUMERIC test: PFD respects the PICTURE (C/D signed, F unsigned); NOPFD/MIG pass any valid sign. Default NOPFD. | Zoned preferred signs are the same nibbles in the low-order byte. | `hostsign` partly; the rest is in `rt/` sign codecs |
| `ibm.arith` | COMPAT 18 digits, EXTEND 31 for items, literals and intermediates; EXTEND rounds floating intermediates to 31 (APAR PH64935). Default COMPAT. | Intermediate cap 30 (COMPAT) / 31 (EXTEND). | `numeric-literal-length`; the cap in `rt/Decimal` |
| `ibm.codepage` | Default CCSID 1140. | Governs alphanumeric literal and data encoding and national conversions. | none (the cobc lane is ASCII; `rt/` carries EBCDIC) |
| `ibm.invdata` | ZONEDATA(NOPFD/MIG): packed-vs-display compare done alphanumerically; unsigned DISPLAY senders have the sign forced to F; INVDATA(FORCENUMCMP, CLEANSIGN) replaces ZONEDATA. Default NOINVDATA. | — | none; `rt/` |
| `ibm.dispsign` | **Verified** (Programming Guide 6.4, DISPSIGN, Table 48; J-109): COMPAT, the default, overpunches a negative value's last digit and shows a zoned item's bytes as they are; SEP shows a leading separate sign. `rt/Display` holds both. The cobc lane compares under SEP, cobc's binary and zoned departures being findings. | — | none; `rt/Display` |
| `ibm.wsclear` | **Verified** that IBM leaves an item without VALUE undefined (LR 6.4 pp. 103, 245; R-057 C-1). The literal starts it as INITIALIZE would (R-057), whatever this field says; the field is for the finding on reads before writes (phase 5). | WSCLEAR binary zeros (R-057 C-2): the z/OS Programming Guide 6.4 names WSCLEAR only as AIX (p. 864); open, HANDOFF. | none |
| Appendix A, intermediate results | **Verified** (PG 6.4 pp. 793-801, `docs/research/ibm-options.md` §4): the theoretical places (+ −: max(i1,i2)+1, max(d1,d2); ×: i1+i2, d1+d2; ÷: i2+d1, max(d2−d1, dmax), op2 the dividend), the cap table (30 COMPAT / 31 EXTEND: over the cap with d ≤ dmax, C−d integer places; d > dmax and i+dmax ≤ C, C−i decimals; else C−dmax and dmax), exponentiation, floating point's conditions and precisions, dmax in conditions. Intermediates are truncated; under ROUNDED the statement's last operation carries one more decimal place and the earlier ones none, as cobc `-std=ibm` computes them (the trial, J-097, corrected by J-099; on IBM itself a hypothesis: "might be carried", p. 794, and APAR PH41526 point at ROUNDED reaching intermediates). The final store: truncation unless ROUNDED, which adds 1 when the excess's first digit is 5 or more (LR 6.4 p. 296, verified). `1/3*300` into `999V99` = 99.00 (`ArithTrialIntegrationTest`). Past the 30-digit cap cobc's ibm dialect does not cut as the table does (7 of 400 generated cases, each matching the uncapped model; J-101): a finding against cobc, Katana following IBM's text, and the cap rows held only by the text and an independent model until a mainframe answers. | `arithmetic-osvs` |

**Appendix A was read on 2026-10-05** (J-095) and `rt/ArithProfile` holds its place tables; the two
questions it leaves open were put to cobc (J-097, J-099): intermediates truncate, and under ROUNDED the
last operation carries one more place, the earlier ones none, under cobc's ibm dialect.

## The GnuCOBOL mapping

The cobc arguments a profile means are `-std=<dialect>` and then, for each flag in name order,
`-f<name>` for `yes`, `-fno-<name>` for `no`, and `-f<name>=<value>` otherwise (PROFILE-03). What
GnuCOBOL's `ibm` dialect changes from `cobol85`, measured one flag at a time on NIST, is in
`oracle/nist/IBM-CHANGES.md`: intermediate precision (`arithmetic-osvs`), binary items not
truncated (`binary-truncate`), segment numbers ignored (`section-segments`), and the reserved words.

## The hash

`sha256:` and the hex SHA-256 of the canonical text: `compiler`, `version`, `dialect`, then each
`flag` sorted by name, then the `ibm.*` fields in the fields table's order, one `field value` line
each, ending in a newline. Comments, blank lines,
spacing and the order fields were written in do not change it (PROFILE-01).

## Invariants

- **PROFILE-01 The hash is over the canonical text alone.** Two files that say the same profile
  have the same hash.
- **PROFILE-02 A profile states every field.** A missing, repeated or unknown field, or an `ibm.*`
  value outside its set, is refused, never defaulted: a default is a compile option nobody chose.
- **PROFILE-03 The mapping is mechanical.** A profile's cobc arguments are its dialect and its flags
  and nothing else.
- **PROFILE-04 The lane truncates binary items as `ibm.trunc` says.** A stated `binary-truncate`
  flag agrees with it (`yes` for STD and OPT, `no` for BIN), and a profile on the `ibm` dialect,
  which does not truncate by itself (`oracle/nist/IBM-CHANGES.md`), states the flag.
