# Glossary: one word per thing

A term here is the only word for its concept in code, comments and documents; the aliases listed
beside it are what *not* to write. An alias in backticks is **enforced**: `test/GlossaryTest.java` fails
when it appears in a document outside this file and `docs/research/` (the study's notes keep their
own words). An alias without backticks is a review guide: a word that is wrong for this concept but
right for another ("run" is an execution, "source" is a source map), so a test cannot judge it. Add a
term before using a new word for a concept; put an alias in backticks the first time a test could
have caught a real slip (R-039).

| Term | Means | Not |
| --- | --- | --- |
| **program** | one COBOL compilation unit | source, unit |
| **module** | one of Katana's own source units, each an entry in `docs/modules.json` (the count lives only there); "NIST module" (NC, SQ, ...) is NIST's own word for its test groups and is not policed | package (that is Java's), component, layer |
| **job** | a JCL job of steps | batch, run (a run is an execution) |
| **step** | one program execution within a job, with its DDs | task |
| **profile** | the dialect profile: compiler, version, TRUNC, ARITH, NUMPROC, code page, collating sequence, and the GnuCOBOL flag mapping; content-addressed | options, flags, settings, dialect (alone) |
| **Semantic IR** | what the program does: data map, paragraph graph, statements over Fields, files, the profile header; exact, complete, mechanically derived | AST (that is the front end's tree), model, IR (alone) |
| **Intent IR** | what the program means: inferred facts with evidence, confidence and confirmation state | directive, annotations, hints, inference map |
| **fact** | one line of the Intent IR | annotation, tag, label |
| **rule** | an inference rule of `intent/rules`, named `kind.reading` and versioned, that makes facts from the Semantic IR | heuristic, detector |
| **review file** | `<PROGRAM>.review`: a person's confirmations and refutations of a program's facts, written by its names (R-064) | annotations file, overrides |
| **key** | the hand-written answers a rule's precision is measured against, written before the rule (`test/katana/intent/carddemo.key`) | ground truth, labels |
| **manifest** | the lowering's list of the Java symbols it wrote, each on the COBOL node it stands on | symbol table (that is a compiler's), index |
| **ledger** | the proof ledger: append-only record of pass, fact, run, symbol, COBOL node, profile | log, history, audit trail (use only when quoting a regulator) |
| **map** | the provenance map written from the ledger: per Java symbol, what it stands on | trace, lineage, kernel map (that is the tool's own: see below) |
| **kernel map** | the tool's own append-only function ledger, `docs/KERNEL-MAP.md` | map |
| **literal** | the pinned Java the lowering emits, a byte-image machine; never shipped | JOBOL (quote only), intermediate, lowered Java |
| **golden** | a recorded expected output a test holds the code to | baseline, snapshot, reference (use "reference log" only for eval-nist's files) |
| **port** | a Java program someone else wrote from a COBOL program, which a divergence report holds to it (R-072) | translation, migration, conversion (each names the act, not the program) |
| **divergence** | a place where a port's output differs from its COBOL's under a fixture; `proven` by a cited input, or `possible` | diff, mismatch, bug |
| **divergence kind** | one way a port departs from its COBOL, `DV-01` onward, a row of docs/DIVERGENCES.md with its rule, exact Java and wrong shape | pattern, category, trap (the study's word) |
| **probe** | Katana's literal Java run with one divergence kind's rule broken (LANE-23), to see whether it gives a port's output | mutant (that is the harness's port), simulation |
| **alignment** | `<PROGRAM>.align`: each COBOL node mapped to the place in a port's Java that does it, `unconfirmed` until a person confirms | mapping (alone), correspondence, trace (that is a run's paragraphs) |
| **fixture** | a content-addressed job description: steps, DDs with layouts and encodings, environment, stubs, optional mainframe gold | test case, scenario, harness (that is the whole oracle tool) |
| **lane** | one runner over a fixture: the cobc lane, the java lane, the Hercules lane | side, leg, run |
| **gate** | the double-run determinism check: the original lane twice, identical, before any oracle | precheck, sanity check |
| **oracle** | the comparison of two lanes' outputs under the copybooks, and its report | diff (that is the tool), verification, validation |
| **comparator** | the component that performs the oracle's comparison | differ |
| **finding** | what is wrong with the input, or what cannot be made exact; said first | warning, issue, error (an error is the tool failing) |
| **discovery** | a defect Katana's checks prove in an existing tool, recorded in `docs/DISCOVERIES.md` with its proof (R-058) | a finding (that is about the input), a claim (that is about Katana), a bug of Katana's own |
| **lowering** | the step from Semantic IR to literal | translation, transpilation, conversion, generation |
| **pass** | one refactoring with obligations, a proof and ledger lines | transform, rewrite, rule (a rule is in Intent IR or in the rule tests) |
| **obligation** | a property a pass must discharge before applying | precondition, guard |
| **proof** | the evidence a pass or a build was equivalent: oracle runs, traces, coverage, not-covered list | verification, certification |
| **distance** | how far the Java has moved from the literal: lines, nesting, dispatcher cases, scalars promoted | quality, idiomaticity |
| **dispatcher** | `for(;;) switch(pc)` with the PERFORM continuation table in the literal | trampoline, state machine, controller |
| **Field** | a view (offset, length, attribute) over a Record's bytes, in `rt/` | variable, data item (COBOL's word; fine in prose about COBOL) |
| **Record** | a `byte[]` for one 01-level, in `rt/` | buffer, struct |
| **attribute** | `{usage, digits, scale, signed, signSeparate, leading, picture}`: the one description of a Field | descriptor, type |
| **Decimal** | `BigInteger` unscaled plus scale, in `rt/`; arithmetic scratch | BigDecimal (the Java class; never the concept) |
| **tithe** | a commit or pass that changes no behaviour and improves the agent's environment | cleanup, chore, housekeeping |
| **routing table** | the task-to-files table in `CLAUDE.md` | index, guide |
| **Ghost** | the maintainer | Paul, the author, we |
| **Reviewer A, B, ...** | outside reviewers, by letter, in order of first appearance; Reviewer G wrote the feasibility study and roadmap | product or model names |
