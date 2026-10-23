# Decisions: the index

Written from `theGhost.md` by `gradle decisions`; do not edit by hand. A decision's reasons, source and what holds it are in `theGhost.md` under its number.

| Decision | Title | Status |
| --- | --- | --- |
| R-001 | Java 21 for everything | in force |
| R-002 | Licensing is protective | in force |
| R-003 | GnuCOBOL 3.1.2, pinned by image digest, is the cobc lane | in force |
| R-004 | No model runs at conversion time | in force |
| R-005 | Batch only, for v1 | in force |
| R-006 | One target language; no C++ | refused (C++); in force (one target) |
| R-007 | Three IRs and a ledger, kept apart | in force |
| R-008 | The navigation tithe | in force |
| R-009 | The fixture schema | in force |
| R-010 | DFSORT steps run on Katana's own Java sort | in force |
| R-011 | What the comparator normalizes, and what it refuses to | in force |
| R-012 | The double-run gate | in force |
| R-013 | What a proof covers, and what it never covers | in force |
| R-015 | che4z's EPL-2.0 grammars stay out; CICS and SQL command syntax is Katana's own when needed | in force |
| R-016 | The indicator column is read permissively; a tab is expanded to the next 8-column stop, as a finding | in force |
| R-017 | The IR is complete: a lowering consults nothing but it, held by "no token left behind" | in force |
| R-019 | The literal Java is a byte-image machine; no COBOL item is ever a Java scalar in it | in force |
| R-020 | Decimal is BigInteger plus scale, driven by an ArithProfile; MathContext is forbidden | in force |
| R-021 | Control flow in the literal is a dispatcher with a PERFORM continuation table | in force |
| R-022 | TRUNC(OPT) is lowered as TRUNC(STD), with a finding | in force |
| R-023 | COMP-1 and COMP-2 are lowered as IEEE binary float, with a finding | in force |
| R-024 | One emitter writes every line of Java; passes never write Java directly | in force |
| R-025 | Indexed files are compared on their logical unload | in force |
| R-026 | Katana's sort is stable; an unordered tie in the original is a finding | in force |
| R-027 | The facades: LE and assembler stubs, and a z/OS storage facade in the java lane only | in force |
| R-029 | Intent is never acted on when refuted, and is flagged when unconfirmed | in force |
| R-030 | The proof ledger is append-only, each line chained to the one before | in force |
| R-031 | A model may only propose intent facts, for a person to confirm, and not in v1 | in force |
| R-037 | Patent protection is intended; nothing public before a provisional is filed | in force |
| R-038 | Doc tests are plain Java 21, no dependencies, until Gradle arrives | in force |
| R-039 | Glossary aliases are enforced only when marked | in force |
| R-040 | Each finished step lands on main | in force |
| R-041 | Gradle comes from the pinned image; no wrapper | in force |
| R-042 | Who writes theGhost.md; a metric arms when first met; tools live outside the modules | in force |
| R-043 | A new module or import is a decision; an agent proposes it and stops | in force |
| R-044 | NIST in CI is Katana's own harness, written from NIST's public-domain material | in force |
| R-045 | GnuCOBOL and other GPL code stay off the screen while Katana's equivalent is written | in force |
| R-046 | The journal: every event recorded with fixed fields, by whoever was there | in force |
| R-047 | Katana's NIST golden means what NIST says, not what GnuCOBOL's harness counted | in force |
| R-048 | SORT steps run in phase 1.5, not 1.4 | in force |
| R-049 | The IBM compile options are profile fields now, each with a verification status | in force |
| R-050 | One CI run per change | in force |
| R-051 | A reformatted record that does not match SORTOUT's LRECL is padded or truncated as DFSORT does | in force |
| R-052 | Claims carry a status; an uncertain claim is settled by a trial, not an argument | in force |
| R-053 | The implementing agent runs a phase to its end; decisions are classed, and only the irreversible stop it | in force |
| R-054 | The fast loop is measured warm; a metric also arms at the first boundary after real code | in force |
| R-055 | Phase 2's six deferred questions, answered mid-phase; 2.2 is deferred, not stopped | in force |
| R-056 | A check names its oracle, carries the mutation that turns it red, and its verdict stops an action | in force |
| R-057 | An item without VALUE starts as INITIALIZE would set it, and a program that reads it first is not yet proven | in force |
| R-058 | Every defect Katana's checks prove in an existing tool is a discovery, recorded with its proof | in force |
| R-059 | An arithmetic statement with several receivers takes its sending value once, as IBM's multiple-results rule says | in force |
| R-060 | Past a short variable-length record, the record area keeps what it held | in force |
| R-061 | A file source's EBCDIC signs translated to ASCII are loaded in the lane's ASCII form | in force |
| R-062 | A CALL reaches the copy IBM's DYNAM option says, the java lane's run unit keeping each | in force |
| R-063 | The java lane's z/OS storage is the blocks a program reads, laid out as it declares them | in force |
| R-064 | A review file names each fact by the program's names, and a person's word on it outlives a rebuild | in force |
| R-065 | A fact rule's evidence is what a program does with an item, not how the item is stored | in force |
| R-066 | A review has three readings before a person's word, and a model's reading is one of them, never the word | in force |
| R-067 | The supply-chain baseline holds here, and tests hold it | in force |
| R-068 | A promoted item keeps COBOL's truncation on every store; a proof only simplifies a store | in force |
| R-069 | Exact arithmetic over a record field is guarded by the bytes being a valid number, never assumed | in force |
| R-070 | An enum or flag fact names an item's values; a money fact types a promoted item; neither changes what it holds | in force |
| R-071 | An open item waits where what it needs is built; an item that can be done now stays in the chain | in force |
| R-072 | The divergence report: a foreign Java port held to its COBOL, each divergence proven by an input or marked possible | in force |
| R-073 | A Sonnet agent tries a code fix once, then escalates a hard failure to Opus | in force |
| R-074 | CLAUDE.md is a pointer file; a routing row goes in the instructions file its workpath points to | in force |
| R-075 | An escalation replays the instructions; a gap three times of one kind goes to Ghost | in force |
| R-076 | A long run's output goes to a file; an agent reads its verdict and its failures | in force |
| R-077 | A conflict between agents stops the work and goes to Ghost at once; an agent never touches what it did not start | in force |
