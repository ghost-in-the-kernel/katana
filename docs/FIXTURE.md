# The job fixture

A fixture is a content-addressed description of a job as the lanes run it (docs/ROADMAP.md 1.3):
its steps (PGM, PARM, COND), their DDs (dataset, disposition, record format, copybook, declared
encoding), the datasets that exist when it starts and what is in them, its environment (profile
hash, `COB_CURRENT_DATE` with hundredths and zone, `TZ`, the modules on `COB_LIBRARY_PATH`, `SYSIN`
bytes as inline cards), the stubs it needs, and optional mainframe gold. `oracle/fixture/` reads and
checks it; `test/katana/oracle/fixture/FixtureDocTest.java` holds this document to the schema and
`FixtureTest` holds the invariants. The CardDemo jobs are in `oracle/fixture/carddemo/`.

A fixture is written from the JCL, already resolved: procedures expanded (a procedure's step is
`STEP.PROCSTEP`), symbols substituted, `DCB=(*.ddname)` referbacks written out, and STEPLIB left
to the library path. What the JCL says that a fixture leaves out or changes is said in a comment
beside it. `oracle/lane/` runs one under GnuCOBOL (phase 1.4): DD binding, dispositions,
generations, IDCAMS, IEFBR14, IEBGENER, the PARM driver, the CEE3ABD stub; SORT is phase 1.5 (R-048).

Invariants are numbered FIXTURE-01 onward, in this document only, and a number is never reused.

## The lines

One line each, a keyword first; `#` at the start of a line is a comment. Values are separated by
spaces; a value with spaces is quoted with `'` (`''` inside is one quote). A line `| ` and then text
is one card of the inline DD above it, kept as written (a lane pads it to 80 bytes).

| Keyword | Values | Attributes | Words | What it says |
| --- | --- | --- | --- | --- |
| `job` | 1 | | | the job's name; once |
| `source` | 1 | | | where its JCL is (`carddemo:app/jcl/POSTTRAN.jcl`) |
| `profile` | 2 | | | the profile's name and its `sha256:` hash (docs/PROFILE.md); once |
| `clock` | rest | | | `COB_CURRENT_DATE`: `YYYY-MM-DD HH:MM:SS.hh +HH:MM`; once |
| `tz` | 1 | | | `TZ`; once |
| `module` | 1 | | | a called program the lane puts on `COB_LIBRARY_PATH` |
| `stub` | 1 | | | a callee the lane stands in for (`CEE3ABD`) |
| `dataset` | 1 | `org`, `lrecl`, `keys`, `over`, `unique`, `from`, `framing`, `sha256`, `defined-by` | `empty` | a dataset in the catalog and how it starts |
| `gold` | 1 | `sha256` | | a dataset's records as the mainframe wrote them |
| `step` | 1 | `pgm`, `parm`, `cond` | | a step, in order; its DDs follow it |
| `dd` | 1 | `dsn`, `disp`, `recfm`, `lrecl`, `copybook`, `encoding` | `sysout`, `dummy`, `inline`, `concat` | a DD of the step above; `concat` adds a dataset to the DD of the same name just above it |

| Attribute | Value |
| --- | --- |
| `org` | `PS`, `KSDS`, `PATH` (an alternate index's path), `GDG` (a generation base) |
| `lrecl` | the record length |
| `keys` | a key as IDCAMS writes it: `length,offset` |
| `over` | the KSDS a `PATH` reads |
| `unique` | `yes` or `no`: whether a `PATH`'s keys are unique |
| `from` | where the records come from: `carddemo:<path>`, `repo:<path>`, or `job:<fixture>` (what that fixture's job leaves of this dataset) or `job:<fixture>:<dsn>` (of another dataset) |
| `framing` | how a file source holds records: `lines` (one a line, padded to lrecl) or `fixed` |
| `sha256` | the hex SHA-256 of the records |
| `defined-by` | the step that defines the dataset (IDCAMS DEFINE); it does not exist before |
| `pgm` | the program or utility |
| `parm` | the PARM string |
| `cond` | the step's COND: `code,operator` |
| `dsn` | the dataset; a generation is `BASE(+n)` |
| `disp` | the disposition, as JCL writes it |
| `recfm` | the record format |
| `copybook` | the copybook that lays out the records, or `-` when the program lays them out itself |
| `encoding` | the declared encoding: `ascii`, `cp037`, `cp1140` |

## The address

A fixture's address is `sha256:` and the hex SHA-256 of its canonical text: comments and blank
lines dropped, each line single-spaced with its attributes and words in the order of the table
above, each card with its trailing spaces dropped (FIXTURE-02).

## The records

A dataset's `sha256=` is over its logical records: each `lrecl` bytes, in file order, or in key
order for a KSDS (the byte order of the declared encoding). A line's newline and a carriage return
before it are not part of the record; a line shorter than `lrecl` is padded with spaces, a longer
one refused. So a source's line endings and a KSDS's load order do not change the hash, and an
index file's pages are never hashed (F-02: BDB pages differ between identical runs). A
`job:` source's hash is that job's dataset unloaded in key order.

## Invariants

- **FIXTURE-01 What the schema does not know is refused, never dropped.** An unknown keyword,
  attribute or word, a malformed value, or a card with no inline DD above it.
- **FIXTURE-02 The address is the canonical text's.** Two fixtures that say the same job have the
  same address; spacing, comments and attribute order do not change it.
- **FIXTURE-03 The clock is pinned to hundredths and zone, and `TZ` is set.** No clock line is an
  unpinned clock; so is one with seconds alone, since the live hundredths leak through (F-02:
  TRANFILE column 326).
- **FIXTURE-04 A fixture is valid for one profile.** The profile is named with its hash, and the
  hash must be the named profile's (P-12).
- **FIXTURE-05 A DD is one of four forms.** `sysout`, `dummy`, `inline` with its cards, or a
  dataset with all six attributes (`copybook=-` says there is none); a dataset DD marked `concat`
  follows a DD of the same name, as JCL's unnamed DD does.
- **FIXTURE-06 Every dataset says how it starts.** A GDG base (no records), a `PATH` over a KSDS,
  defined by a step, `empty`, or `from=` a source with the `sha256=` of its records; a KSDS has
  `keys=` that fit its `lrecl`.
- **FIXTURE-07 A step reads only what exists when it starts.** `SHR` and `OLD` need the dataset;
  `NEW` refuses one that exists; `MOD` makes one; a normal disposition of `DELETE` removes it after
  the step. A generation is `(+n)` of a declared base, new in this job, or `(0)` or `(-n)`, which the
  fixture declares as a sequential dataset that exists when the job starts.
- **FIXTURE-08 A dataset has one record length.** A DD's `lrecl` is the dataset's.
