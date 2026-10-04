# Oracle prototypes (from the 2026-10-04 feasibility study)

Working pieces of the equivalence oracle, written by hand during the study. Each is to be absorbed
into `oracle/` in phase 1 (docs/ROADMAP.md) and then deleted from here; nothing in this folder is a
dependency of anything.

- `nist/`: the study's run of NIST CCVS85 under GnuCOBOL 3.1.2 to
  `Total 426 426 0 0 9610 0 20 118 9748` (`summary.log`; `run-all.log`; per-module `report.txt`; the
  `*.diff.log` files, empty where the run matched GnuCOBOL's shipped expectations). `EXEC85.cob` is
  NIST's own driver, extracted from `newcob.val` (public domain). The GnuCOBOL harness scripts and
  its expected-result files that ran beside these were removed under R-045: Katana's harness is a
  clean-room rewrite from NIST's documentation (R-044), and GPL code stays off the screen while it
  is written. The suite itself (`newcob.val`) is fetched by `corpus/fetch.sh` at a pinned commit and
  SHA-256, never committed.
- `carddemo/`: the CardDemo batch runs. Hand-written: `load_*.cbl` (IDCAMS REPRO stand-ins, loading
  KSDS files in key order), `unload.cbl` (key-ordered logical unload for comparison), `CEE3ABD.cbl`
  (LE abend stub surfacing the abend code), `sortstep.cbl` and `dupsort.cbl` (DFSORT step
  emulation), `ovp*.cbl` and `ts.cbl` (sign-overpunch and timestamp probes). `post1/`, `post2/`,
  `rept1/`, `rept2/`, `run1/`, `run2/` are the paired runs whose logical unloads were byte-identical
  once `COB_CURRENT_DATE="2024-03-01 12:00:00.00 +00:00"` and `TZ=UTC` were set; they are the first
  frozen goldens. `data/` holds the ASCII inputs CardDemo ships (Apache-2.0) and the SORT probes.
- `omp/`: the Open Mainframe Project course programs run here, with the field-aware EBCDIC
  transcode that a byte-blind `iconv` gets wrong.
- `cobj-run.sh`: driver that ran opensourcecobol4j over the corpus (its results are in
  `docs/research/parser-results/`).
