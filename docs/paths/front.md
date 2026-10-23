# Workpath: the front end and the Semantic IR (parse, the IR and its contract, findings, the profile)

Pointed to by CLAUDE.md (R-074). A new row for this path goes in this file's table, never in CLAUDE.md.

## Instructions

- Every branch on a COBOL property lives here or in `intent/`; `lower/` decides nothing (R-024).
- A construct that cannot be made exact is a finding, said first, never silent output (P-03).
- The IR over the corpus is held by goldens (`sir/ir.golden`, `sir/datamap.golden`, `front/corpus.golden`);
  a change to them is reviewed line by line.
- A change here selects the `sir` suite and the java lane in the gate: expect a long run, and read only its
  verdict and failures.

## Rows

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| The front end: COBOL to a parse tree, COPY and REPLACE, the source map | `front/Parse.java`, `front/Lines.java`, `front/Preprocessor.java`, `front/Mapped.java`; front/grammar (ProLeap's, forked, MIT) | docs/ROADMAP.md 2.1, 2.3 | `test/katana/front/ParseTest.java`, `test/katana/front/CorpusIntegrationTest.java` (`front/corpus.golden`) |
| The Semantic IR: what a program does, as data, and its contract | `sir/types/Program.java`, `sir/types/Item.java`, `sir/types/Statement.java`, `sir/contract/Contract.java` | `docs/SEMANTIC-IR.md` (SIR-01 onward), R-007 | `test/katana/sir/SirDocTest.java`, `test/katana/sir/SirTypesTest.java` |
| Tree to IR (`sir/build`), its findings (`sir/check`), JSON (`sir/json`) | `sir/build/Build.java`, `sir/build/DataMap.java`, `sir/build/Paragraphs.java`, `sir/build/Values.java` | `cobc -C`'s layout (`sir/datamap.golden`); SIR-01 onward | `test/katana/sir/BuildTest.java`, `test/katana/front/CorpusIntegrationTest.java` (`sir/ir.golden`, `sir/datamap.golden`) |
| Findings: what cannot be made exact, said first | `findings/Finding.java`, `sir/check/NonExact.java` | `docs/FINDINGS.md` | `test/katana/sir/NonExactTest.java` |
| The dialect profile | `profile/Profile.java`, `profile/ibm.profile` | `docs/PROFILE.md` | `test/katana/profile/ProfileTest.java` |
