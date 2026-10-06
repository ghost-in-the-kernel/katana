# Discoveries: what Katana's checks have proven about other tools (R-058)

A discovery is a defect Katana's checks prove in an existing implementation: output that disagrees
with the authority the tool claims to follow, with the authority quoted, the test that shows it, and
a COBOL reproducer. It is not a finding (a finding is about the input program) and not a claim (a
claim is about Katana). Numbers are never reused. A status is `open`, `reported`, `fixed in
<version>`, or `withdrawn` (with why). Reporting upstream is Ghost's decision each time (R-037).
`DiscoveriesDocTest` holds every row to a test and a journal entry that exist.

The rows below were seeded by Fable from the phase-3 reports on 2026-10-06; Opus fills the test, the
journal entry and the counts from the entries J-097 to J-120 and adds the doc test. Every differential
test that excludes cases "where cobc departs" names its D-number.

| D | Tool, version | What it does | What the authority says | Proof (test; mutation) | Journal | Cases | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| D-001 | GnuCOBOL 3.1.2 `-std=ibm` | does not apply the 30-digit cap on intermediate results | Enterprise COBOL 6.4 PG Appendix A, pp. 793–801 (`docs/research/ibm-options.md` §4) | `ArithDifferentialIntegrationTest` | J-101 | 7 of 40 past the cap | open |
| D-002 | GnuCOBOL 3.1.2 `-std=ibm` | folds an all-literal arithmetic expression at compile time with more places than Appendix A gives | Appendix A gives no literal rule (§4) | `ArithTrialIntegrationTest` | J-097 | the trial's first fixture | open |
| D-003 | GnuCOBOL 3.1.2 `-std=ibm` | DISPLAY of a packed item shows a separate sign; IBM's COMPAT overpunches | PG 6.4 Table 48 (DISPSIGN) | `CodecDifferentialIntegrationTest` (Opus: confirm) | J-109 | 150 | open |
| D-004 | GnuCOBOL 3.1.2 `-std=ibm` | DISPLAY of a binary item shows the binary field's digits, not the PICTURE's | PG 6.4 Table 48 | (Opus) | J-109 | 5 | open |
| D-005 | GnuCOBOL 3.1.2 `-std=ibm` | DISPLAY of an overpunched zoned item shows the sign separate in the overpunch's place | PG 6.4 Table 48 | (Opus) | J-109 | (Opus) | open |
| D-006 | GnuCOBOL 3.1.2 `-std=ibm` | editing: five departures, including IBM's own `-$999.99` example | LR 6.4, PICTURE editing examples (Opus: page) | `EditDifferentialIntegrationTest` | (Opus) | (Opus) | open |
| D-007 | GnuCOBOL 3.1.2 `-std=ibm` | leaves insertion characters out of a floating editing string | LR 6.4, floating insertion (Opus: page) | `EditDifferentialIntegrationTest` | (Opus) | (Opus) | open |
| D-008 | GnuCOBOL 3.1.2 `-std=ibm` | runs INSPECT's phrases one at a time instead of IBM's comparison cycles | LR 6.4, INSPECT comparison cycle (Opus: page) | (Opus) | (Opus) | (Opus) | open |
| D-009 | GnuCOBOL 3.1.2 `-std=ibm` | judges a COMP-5 size error by the binary field's capacity, not its PICTURE | PG 6.4, TRUNC and COMP-5 (`docs/research/ibm-options.md` §1) | `SizeErrorDifferentialIntegrationTest` | (Opus) | (Opus) | open |
| D-010 | GnuCOBOL 3.1.2 testsuite | five autotest expectations hold the default dialect's output, not IBM's (DISPLAY of a decimal item shows a point) | LR 6.4, DISPLAY of numeric items (Opus: page) | `AutotestRtIntegrationTest` | (Opus) | 5 of 24 hand-lowered | open |
| D-011 | GnuCOBOL 3.1.2 lexer (`cobc`) | (withdrawn: the comma-as-decimal reading was Katana's own front-end bug, J-119, not cobc's) | — | — | J-119 | — | withdrawn |
