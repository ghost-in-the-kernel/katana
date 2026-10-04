# Front-end prototypes

- `ProLeapDriver.java`: the driver that ran ProLeap's ANTLR grammars over the corpus (results in
  `docs/research/parser-results/proleap-*.txt`): 31/31 CardDemo, 79/82 BankDemo, 26/30 OMP, 279/459
  NIST; zero NIST failures that GnuCOBOL accepts.
- `run_ts.py`: the tree-sitter-cobol driver (disqualified: error recovery hides failures).
- `cics-stubs/`: DFHAID and DFHBMSCA copybooks IBM supplies and the sample repos omit; the front end
  synthesizes these (phase 2.1).
