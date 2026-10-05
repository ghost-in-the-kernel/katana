# Front-end prototypes

- `ProLeapDriver.java`: the driver that ran ProLeap's ANTLR grammars over the corpus (results in
  `docs/research/parser-results/proleap-*.txt`): 31/31 CardDemo, 79/82 BankDemo, 26/30 OMP, 279/459
  NIST; zero NIST failures that GnuCOBOL accepts.
- `run_ts.py`: the tree-sitter-cobol driver (disqualified: error recovery hides failures).
- `cics-stubs/` (moved): DFHAID and DFHBMSCA are `front/ibm/` since phase 2.1; the CSUTLDWY stub went
  when the pinned CardDemo carried its own.
