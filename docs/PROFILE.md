# The dialect profile

The compile options a proof is valid for (docs/ROADMAP.md P-12): compiler and version, TRUNC, ARITH,
NUMPROC, code page, program collating sequence, and each one's mapping to GnuCOBOL's dialect
configuration. Content-addressed; its hash appears in the Semantic IR header, the Java manifest,
every fixture and every ledger line (R-007). `test/katana/profile/ProfileTest.java` holds this
document to `profile/`.

Built in phase 1.3, where a fixture first carries the hash, with only the fields that can be stated
from what was measured: the compiler, its version, its dialect, and GnuCOBOL flags set beyond the
dialect. The IBM compile options (TRUNC, ARITH, NUMPROC, code page, collating sequence) are not
fields yet: their IBM meaning must be read from the Enterprise COBOL manuals first, which the study
could not reach (docs/HANDOFF.md), and phase 3.1 needs them before the numeric runtime is frozen.
Adding one is a new profile, with a new hash, never a change to an old one.

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

## The GnuCOBOL mapping

The cobc arguments a profile means are `-std=<dialect>` and then, for each flag in name order,
`-f<name>` for `yes`, `-fno-<name>` for `no`, and `-f<name>=<value>` otherwise (PROFILE-03). What
GnuCOBOL's `ibm` dialect changes from `cobol85`, measured one flag at a time on NIST, is in
`oracle/nist/IBM-CHANGES.md`: intermediate precision (`arithmetic-osvs`), binary items not
truncated (`binary-truncate`), segment numbers ignored (`section-segments`), and the reserved words.

## The hash

`sha256:` and the hex SHA-256 of the canonical text: `compiler`, `version`, `dialect`, then each
`flag` sorted by name, one `field value` line each, ending in a newline. Comments, blank lines,
spacing and the order fields were written in do not change it (PROFILE-01).

## Invariants

- **PROFILE-01 The hash is over the canonical text alone.** Two files that say the same profile
  have the same hash.
- **PROFILE-02 A profile states every field.** A missing, repeated or unknown field is refused, never
  defaulted: a default is a compile option nobody chose.
- **PROFILE-03 The mapping is mechanical.** A profile's cobc arguments are its dialect and its flags
  and nothing else.
