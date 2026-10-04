# Open-source COBOL parsers and COBOL-to-Java translators: inventory, builds, and observed failures

Scope note: everything marked **[measured]** below was produced by building the tool in this session and running it over the corpus under `/home/claude/cobol-research/corpus/` (CardDemo, Open Mainframe Project COBOL course, Micro Focus BankDemo, NIST CCVS85). Raw per-file results are in `/home/claude/cobol-research/tools/results/`, generated artifacts in `/home/claude/cobol-research/tools/runs/`, built tools under `/home/claude/cobol-research/tools/<project>/`. Facts taken from project pages are cited inline. Session date: 2026-10-03.

Environment findings that shaped the work (all **[measured]**):
- Toolchain present: gcc, make, bison, flex, autoconf, OpenJDK 21, Maven 3.9.11 (`/opt/maven`), Gradle (`/opt/gradle`), node 22, python3, **and GnuCOBOL 3.1.2 (`cobc`) was already installed** at `/usr/bin/cobc`.
- Egress proxy blocked (403 on CONNECT) every artifact host: `repo.maven.apache.org`, `repo1.maven.org`, `services.gradle.org`, `plugins.gradle.org`, `jitpack.io`, `sourceforge.net`, `www.antlr.org`, `central.sonatype.com`, `download.docker.com`, `www.ibm.com`, and `github.com/<org>/<repo>/releases/download/...` (GitHub release assets 404 through the proxy). **Every `mvn`/`gradlew` build was therefore impossible**, and the GitHub REST API (`api.github.com/search`, `repos/<x>`) answered "GitHub access to this repository is not enabled for this session" — star counts below come from fetching the HTML pages, not the API.
- What *did* work: `git clone` from github.com (proxy rewrites git), `registry.npmjs.org`, `pypi.org`. The ANTLR 4.13.2 complete jar was obtained from the npm package `antlr4ng-cli@2.0.0` (it bundles `antlr4-4.13.2-SNAPSHOT-complete.jar`); the tree-sitter runtime from PyPI (`tree_sitter==0.26.0`); slf4j/guava/commons-cli jars from `/opt/maven/lib`; Ant from `/opt/gradle/lib/ant-1.10.15.jar`. Everything was then compiled with plain `javac`/`gcc`.
- The corpus folder was being filled by another agent concurrently; by the end it held 23 repos (CardDemo 44 programs/62 copybooks, OMP course 30, BankDemo 82/83, NIST 459 programs + 51 copybooks, plus gnucobol-contrib, cobol-check, opensourcecobol4j's own tests, TypeCobol's 911 test files, etc.). This study used CardDemo, the OMP course, BankDemo and NIST.

## Key question 1: Inventory of open-source COBOL parsers, translators and Java runtimes

### Takeaway
Only two open-source projects actually translate real COBOL to Java end-to-end: **opensourcecobol4j** (a GnuCOBOL/OpenCOBOL 1.1 fork whose `codegen.c` emits Java instead of C; GPLv3 + LGPLv3 runtime) and the long-dead **RES** (SourceForge, 2010). Of the parsers, **ProLeap** (ANTLR, MIT), **Koopa** (own island-parser generator, BSD), **che4z COBOL Language Support** (ANTLR 4.13.2, EPL-2.0, IBM/Broadcom lineage) and **tree-sitter-cobol** (Sakamoto, grammar derived from opensource-cobol) are the serious candidates; cb2xml/JRecord parse copybooks (data division) only.

### Cited Findings

Parsers:
- **ProLeap COBOL parser** (uwol): 208 stars, 110 forks, MIT, Java/Maven, JDK 17, 36 open issues, 704 commits; "EXEC SQL, EXEC SQLIMS and EXEC CICS statements are extracted as texts"; preprocessor executes COPY, REPLACE, CBL, PROCESS; claims "Passes the NIST test suite" — [GitHub](https://github.com/uwol/proleap-cobol-parser). Last commit in clone: 2026-03-01 ("workflow"); grammar `Cobol.g4` 3,269 lines + `CobolPreprocessor.g4` 670 lines; pom pins ANTLR 4.7.2, 1,119 main Java files, 775 test files, 300 `.cbl` test fixtures; dialect enum is `ANSI85, MF, OSVS`; source formats `FIXED, TANDEM, VARIABLE` **[measured]**.
- **Koopa** (krisds): 57 stars, 17 forks, BSD, Java 11+, Ant, 2 open issues, 535 commits; "Accepts free, fixed and variable format COBOL"; "doesn't mind the presence of CICS/SQL fragments"; island parser generator; XML dumps and XPath querying; NIST test files bundled with "licensing is unclear" — [GitHub](https://github.com/krisds/koopa). Last commit in clone 2026-07-25; ships its 17 dependency jars in `lib/` so it builds offline **[measured]**.
- **che4z COBOL Language Support** (eclipse-che4z, IBM/Broadcom): EPL-2.0 (`LICENSE.md`), ANTLR 4.13.2, Maven multi-module (`parser`, `engine`, `dialect-idms`, `dialect-daco`, `common`); grammars total 20,637 lines, of which `CICSParser.g4` is 5,289 lines, `CobolParser.g4` 2,423, `Db2SqlExecParser.g4` 1,659, `IdmsParser.g4` 1,211; last commit 2026-10-02 **[measured from clone]**. Its `parser` module (15 Java files) depends only on ANTLR, lombok (`@Data` on one class), lsp4j `Range/Position`, guava, and an `MessageServiceProvider` from `common`; its ANTLR grammar generated cleanly with the 4.13.2 jar **[measured]**; not driven over the corpus for lack of time (see Gaps).
- **tree-sitter-cobol** (yutaro-sakamoto): "COBOL85 grammar of tree-sitter"; "syntax rules ... are based on opensource-cobol and are tested with NIST COBOL85 test suite" — [README](https://github.com/yutaro-sakamoto/tree-sitter-cobol). Last commit 2026-09-10; `grammar.js` 3,783 lines; ships pre-generated `src/parser.c` + `scanner.c` (LANGUAGE_VERSION 14), exported symbol `tree_sitter_COBOL` **[measured]**. A `run_nist_cobol85.sh` with a `skip_tests.txt` is in the repo **[measured]**.
- **tree-sitter-cobol-enterprise** (Spantree): MIT, derived from Sakamoto's grammar, "Tree-sitter grammar for IBM Enterprise COBOL — full EXEC CICS/SQL parsing with typed AST nodes"; self-reported coverage: AWS CardDemo 98.5% clean (65/66 files), NIST COBOL 85 6.3% clean, enterprise samples 62.9%, z Open Editor samples 66.7%; limitations: no free-form, "WRITE AFTER ADVANCING has a GLR ambiguity", intrinsic FUNCTION calls partial; 7 commits, 9 open issues — [GitHub](https://github.com/Spantree/tree-sitter-cobol-enterprise). Last commit 2026-04-24; `grammar.js` 1,253 lines; **no generated `parser.c` is committed**, so it needs the `tree-sitter` CLI, whose npm installer downloads the binary from GitHub releases (blocked) **[measured]**.
- Other tree-sitter COBOL grammars seen in search (not evaluated): Neppord, willswire, BloopAI, raresdolga — [search results](https://github.com/Neppord/tree-sitter-cobol), [willswire](https://github.com/willswire/tree-sitter-cobol), [BloopAI](https://github.com/BloopAI/tree-sitter-cobol), [raresdolga](https://github.com/raresdolga/tree-sitter-cobol).
- **GnuCOBOL 3.1.2** (`cobc`): bison/flex parser (`parser.y`, `scanner.l`, `pplex.l`, `ppparse.y`), GPLv3+ (`cobc --version`) — used here as the reference front end and as the C-output reference **[measured]**. opensource COBOL (OSS Consortium Japan) is an OpenCOBOL 1.1 fork; its parser is what both opensourcecobol4j and tree-sitter-cobol derive from — [opensourcecobol4j README](https://github.com/opensourcecobol/opensourcecobol4j).
- **cb2xml** (bmTas): LGPL 2.1, Java/Maven (`mvnw`), copybook-only ("reads a Cobol-Copybook and converts it to either a java Item tree ... or an Xml file") — [README in clone](https://github.com/bmTas/cb2xml); last commit 2026-07-07 **[measured]**. **JRecord** (bmTas): Maven, version 0.93.4 in pom, last commit 2026-01-30 **[measured]**; a record-layout/file-IO library over cb2xml, not a parser of procedure code.
- **cobrix** (AbsaOSS): Spark COBOL data source with its own copybook parser (`cobol-parser` module, sbt/Maven), Apache-2.0, last commit 2026-08-08 **[measured from clone]** — data division only.
- Not found as real projects: "pycobol" and "cobol2py" returned nothing substantive in search; "jcobol", "Zenith", "cobolToJava" did not surface any GitHub repo in the queries run (see Gaps).

Translators (COBOL -> Java / other):
- **opensourcecobol4j** (opensourcecobol): 125 stars, 40 forks, 85 open issues, latest release v2.0.0 (README in clone says 2.1.0 and the built binary prints "opensource COBOL 4J 2.1.0"); core GPLv3, runtime `libcobj` LGPLv3; languages C, Java, shell; claims NIST "8804 Pass, 0 Fail" across nucleus/file/sort/intrinsic modules, EXEC SQL for PostgreSQL, `cobj-idx` indexed-file utility and `cobj-api` Spring Boot integration; no CICS, no REPORT WRITER, no ALTER, no debugging module — [GitHub](https://github.com/opensourcecobol/opensourcecobol4j). Last commit 2026-09-18 **[measured]**. Open issues include "#882 ESQL: COMP-5 (and COMP-1/COMP-2) host variables receive corrupted values on FETCH / SELECT INTO", "#879 Performance issue", "#845 Performance analysis: prioritized list of performance issues in transpiled programs and libcobj", "#844 Optimize CobolDataStorage.memcpy", "#834/#833 cobj.exe segfaults on (variable) reference modification on Windows", "#835 PROGRAM-ID class/file name case mismatch on case-insensitive filesystems" — [issues](https://github.com/opensourcecobol/opensourcecobol4j/issues).
- **RES: open COBOL to Java** (SourceForge): GPL; "pre-Beta"; last documented update 2010-08-01; JavaCC/JTB parser; "100% translation but some re-factoring of the target Java may be recommended"; indexed/VSAM files must be migrated to relational tables; NIST exceptions noted for file status codes and START — [project page](https://opencobol2java.sourceforge.net/). sourceforge.net is blocked by the proxy so the source could not be fetched **[measured]**.
- **cobol-to-java-transpiler / "actojat"** (ArturHD): Kotlin + ANTLR, MIT, 4 stars, Maven; supports IF/THEN/ELSE, PERFORM TIMES/UNTIL, conditionals, partial PICTURE; **no MOVE, no PERFORM VARYING**; "work in progress", COBOL85 only — [GitHub](https://github.com/ArturHD/cobol-to-java-transpiler). Last commit 2021-12-22; its grammar is 905 lines and all Kotlin sources total 3,823 lines **[measured]** — a toy by size.
- **diegoasua/cobol-java-transpiler** surfaced in search — [GitHub](https://github.com/diegoasua/cobol-java-transpiler); not fetched (time).
- **legstar-core2** (legsem): "Mainframe COBOL and Java open world integration" (data binding / marshalling, not a translator) — [GitHub](https://github.com/legsem/legstar-core2).
- **GnuCOBOL's C output** (`cobc -C`) is the one production-quality exact lowering and is described in Key question 4.

### Inferences
- The three serious parser lineages are: (1) ANTLR grammars (ProLeap; che4z — the latter also has the only large open CICS/DB2 grammars, 5.3k + 1.7k lines); (2) the OpenCOBOL/GnuCOBOL bison grammar and its descendants (opensourcecobol4j, tree-sitter-cobol, tree-sitter-cobol-enterprise); (3) Koopa's hand-written island grammar. Everything else is copybook-only or abandoned.
- `awesome-cobol` lists were useless for this question: the two fetched ([Codilingo](https://github.com/Codilingo/awesome-cobol), [writ3it](https://github.com/writ3it/awesome-cobol)) list only GnuCOBOL under compilers and no parsers or translators.

### Gaps
- Star/fork/issue counts for Koopa, che4z, Sakamoto's tree-sitter-cobol, cb2xml, JRecord and cobrix were not collected (API blocked; only three project pages fetched for counts within the time budget).
- The che4z parser was generated but not compiled/run over the corpus (its `MessageServiceParser` base needs the `common` module; ~15 more minutes would have done it). It is the most promising untested candidate.
- Spantree's tree-sitter-cobol-enterprise could not be generated (no committed parser.c, CLI binary download blocked). Its coverage numbers above are self-reported, not reproduced.
- RES source and TypeCobol (C#, 911 test files present in corpus) were not examined.

## Key question 2: Build results and parse results on real programs

### Takeaway
Four tools were built from source without any package manager: ProLeap (javac + ANTLR jar from npm), Koopa (Ant from Gradle's lib), opensourcecobol4j's `cobj` translator (autoconf/gcc; its Java runtime hand-compiled with a 2-class `org.sqlite` stub), and tree-sitter-cobol (gcc of the committed parser.c, driven from Python). On the 31 CardDemo programs with CICS copybook stubs supplied, ProLeap parsed 31/31 and Koopa 31/31; cobj translated 11/31 (it has no CICS); tree-sitter-cobol 9/31. On NIST (459 programs, after normalizing the CCVS selection codes) ProLeap 279, Koopa 459 (but silently dropping tokens — see below), cobj 263, tree-sitter 269, versus GnuCOBOL 3.1.2's 264.

### Cited Findings (all **[measured]** unless cited)

Build log (what failed and why):
- `mvn` for ProLeap: not attempted after `repo.maven.apache.org` returned `curl: (56) CONNECT tunnel failed, response 403`. Workaround: `java -jar antlr-4.13.2-complete.jar -visitor` on the two `.g4` files (2.7 s), then `javac` of 1,131 sources with `slf4j-api-1.7.36.jar` on the classpath (11.3 s, 0 errors). Note ProLeap's pom pins ANTLR 4.7.2; it compiled and ran unchanged against 4.13.2.
- Koopa `ant jar` first failed with "`build does not exist`" (the `jar` target doesn't depend on compile) and then with "package koopa.sql.grammar does not exist" when `cobol` was built before `sql`/`cics`; running targets in the order `cics sql cobol apps jar` succeeded (~19 s). 
- opensourcecobol4j: `./configure` OK (8 s; it does not need libgmp despite the README); `make -C cobj` alone fails with `defaults.h: No such file or directory` (generated by the top-level Makefile); plain `make` builds `cobj/cobj` (722 KB) and then fails at `libcobj/`: `./gradlew shadowJar` -> `java.io.IOException: Server returned HTTP response code: 403 for URL: https://services.gradle.org/distributions/gradle-8.2.1-bin.zip`. Workaround: `javac` of the 87 runtime sources minus the `sql/` and `user_util/` packages, with guava/slf4j/commons-cli from `/opt/maven/lib` and a 2-class stub for `org.sqlite.SQLiteConfig`/`SQLiteErrorCode`; result in `/home/claude/cobol-research/tools/oc4j-rt/`. Generated programs then compile after deleting the `import jp.osscons.opensourcecobol.libcobj.sql.*;` line.
- tree-sitter-cobol: `gcc -shared -fPIC -O2 -o cobol.so -I src src/parser.c src/scanner.c` -> 13.4 MB shared object; loaded with `tree_sitter` 0.26.0 from PyPI (`Language(ctypes...tree_sitter_COBOL())`). tree-sitter-cobol-enterprise: `src/parser.c` absent; `npm pack tree-sitter-cli` gives only a JS installer whose `releaseURL` is `https://github.com/tree-sitter/tree-sitter/releases/download/...` (blocked) — not built.
- che4z: `antlr4` generation of `CobolLexer.g4` + `CobolParser.g4` succeeded (`superClass = MessageServiceParser`); Java compile not attempted (time).
- Corpus hygiene problems that are not tool failures: CardDemo's CICS programs `COPY DFHAID` and `COPY DFHBMSCA` (IBM-supplied, not in the repo) — stubs written to `/home/claude/cobol-research/tools/stubs/`; BankDemo ships `.settings/*.cbl.mfdirset` Eclipse files that Koopa's directory walker tried to parse; the OMP course's `.cobol` extension is unusual.

ProLeap results (pipeline = preprocessor + ANTLR parse + ASG build, `CobolDialect.MF`, formats tried FIXED then VARIABLE then TANDEM):
- CardDemo: first run 14/31; after CICS stubs 26/31; the last 5 failed with `CobolPreprocessorException: Could not find copy book 'CSUTLDWY'` although `app/cpy/CSUTLDWY.cpy` exists — **ProLeap's `LiteralCopyBookFinderImpl` matches a quoted `COPY 'NAME'` against the file *path* exactly and never applies `copyBookExtensions`** (`src/main/java/io/proleap/cobol/preprocessor/sub/copybook/impl/LiteralCopyBookFinderImpl.java`, `isMatchingCopyBookAbsolute` compares `Paths.get(dir, "CSUTLDWY")` to the candidate's full path). Symlinking extension-less names fixed it: 31/31. Per-program parse+ASG time 6 ms–2 s.
- OMP course: 26/30. Failures: `CBL0007.cobol` line 149 `mismatched input 'END-IF'` — the source is `IF STATE ADD 1 TO VIRGINIA-CLIENTS.` followed by a stray `END-IF.` (the lab is a deliberately broken program), so this is a correct rejection. `CBLDB21/22/23.cbl` line 133:44 `extraneous input '('` on `DISPLAY 'ERROR AT ' FUNCTION TRIM(UD-ERROR-MESSAGE, TRAILING)` — the grammar's `functionName` rule is `INTEGER | LENGTH | RANDOM | SUM | WHEN_COMPILED | cobolWord` and `argument` has no keyword alternative, so the `TRAILING` keyword argument to `FUNCTION TRIM` is not parsed (grammar gap).
- BankDemo: 79/82 with stubs. The 3 failures: `missing RECORD at 'KEY'` on `ALTERNATE KEY IS BAC-REC-PID WITH DUPLICATES` (ZBNKLOAD.cbl:69, DBANK51P/52P.cbl:43). The grammar has `alternateRecordKeyClause : ALTERNATE RECORD KEY? IS? ...` — RECORD is mandatory, but Micro Focus/IBM accept `ALTERNATE KEY` (grammar gap).
- NIST raw: 62/459 — 394 failures were `Is FIXED the correct line format ... Could not parse line 272: 027200S    EXIT PROGRAM.` ProLeap rejects any indicator-column character outside its set; the CCVS files carry selection codes S, Y, P, G, J, T (active-line variants) and C, A, X (optional lines) in column 7. After rewriting S/Y/P/G/J/T to space and C/A/X to `*` (`nist/src`): 317/459. After also quoting the `XXXXX0nn` ASSIGN placeholders the way the CCVS user guide intends (`nist/src2`): **279/459**; the remaining failures, by first offending token: 76× `"XXXXX069"` (a placeholder standing for an implementor clause inside FD, e.g. IC112A.CBL:67; a test-suite artefact, not a grammar gap), 31× `"XXXXX084"`, 25× `'RECORD-KEY'` (SQ101M.CBL:69 `RECORD-KEY IS RAW-DATA-KEY` — hyphenated spelling, also rejected by GnuCOBOL), 11× `"XXXXX044"`, 6× `'XXXXP044'`, 2× `'CLASS'` (NC174A.CBL:42, CLASS clause in SPECIAL-NAMES), 10× remaining `Is FIXED the correct line format` (indicator `U`, `H`, `E` codes). Of the 180 ProLeap failures, **0 are programs that GnuCOBOL 3.1.2 accepts** — i.e. every ProLeap NIST failure is also a cobc failure on the same files.
- Per NIST module (ProLeap pass / module total): NC 87/95, IF 45/45, IC 45/47, SQ 54/85, IX 16/42, RL 2/35, ST 5/40, SM 6/17, SG 3/13, DB 5/15, CM 3/9, RW 0/6, OB 7/9, EX 1/1. The low RL/ST/IX/RW numbers are dominated by the `XXXXX0nn` placeholders in FD clauses and by `RECORD-KEY`.

Koopa results (`koopa.app.cli.ToXml --preprocess -I ...`):
- CardDemo 31/31, BankDemo 82/82, OMP course 25/28 (the `.cobol` files are not picked up by default; with `-Dkoopa.cobolFileExtensions` set, 28 processed), NIST (src2) **459/459**, zero warnings printed. Failures: `CBLDB21/22/23.cbl` `Incomplete parse. Last successful match: displayStatement` at the `CALL` after the same `FUNCTION TRIM(..., TRAILING)` line — the same construct ProLeap fails on.
- **But Koopa's 100% is partly an artefact of its island grammar.** Its `Cobol.kg` contains rules of the form `| unknown: --> .` with a `# TODO Handle skipped.` comment (lines 1453, 1523, 1532, 1542, 2554). Counting in the emitted XML: 201,406 `<unknown>` nodes across all runs, 473 of 493 NIST XML files contain at least one, and every CardDemo (31/31), OMP (22) and BankDemo (69) file does too. 605 of those nodes carry non-empty text that the parser skipped rather than parsed: in BankDemo `DBANK10P` the skipped text is `BEGIN DECLARE SECTION`, `SP.BSP_TERM`, `:CD10I-TERM`, `(BSP_TERM, BSP_SPA_DATA) VALUES (:CD10I-TERM, ...` — i.e. the body of EXEC SQL statements — and in `SBANK70P` the Micro Focus screen-section syntax `IN <<SCRN>>`. The empty `<unknown />` nodes sit where an optional clause was expected (e.g. after `PIC X(120)` before the period in RL101A), so they are mostly harmless, but the parser gives no error for any of them.
- What Koopa does well: EXEC CICS is parsed structurally — `<execCICSStatement><cicsStatement><command>RETURN</command><option><name>TRANSID</name><value>WS-TRANID</value></option>...` for COSGN00C.cbl:98 — and the tree is a full concrete syntax tree with every whitespace and sequence-area token preserved as `<t>` nodes (COSGN00C.xml is 532 KB for a 270-line program), so exact provenance is trivially available.

opensourcecobol4j `cobj -C` (translate-only) results:
- CardDemo: 11/31. Failures and their causes: 15× `'EIBCALEN' undefined` plus `'EXEC'/'CICS'/'LINK' undefined` — **no CICS preprocessing at all** (the `parser.y` has only an `EXEC_SQL_STATEMENT` token); `CBSTM03A.CBL :: CUSTREC.cpy:6: Invalid picture string - 'X(2'` — the copybook has **tab characters** in the indicator/area-A region (`^I^I     05  CUST-FIRST-NAME ... PIC X(25).`), which cobj's column counting mis-expands (ProLeap and Koopa both accept it); `COADM01C/COMEN01C :: COADM02Y.cpy:55: Size of 'CDEMO-ADMIN-OPTIONS' larger than size of 'CDEMO-ADMIN-OPTIONS-DATA'` and `CORPT00C.cbl:126: Size of 'JOB-DATA-2' larger than size of 'JOB-DATA-1'` — a REDEFINES larger than the redefined item, which IBM allows as a warning for 01-levels and cobc/cobj reject as an error; `COCRDLIC.cbl:252: No previous data item of level 05` — a level-05 following 88-levels under an 01 with a comment block between (lexer/level-stack issue); `CBEXPORT/CBIMPORT: An indexed key 'EXPORT-SEQUENCE-NUM' is not found in a file record` (RECORD KEY names a field outside the FD record — accepted by IBM when the key is in WORKING-STORAGE? — rejected here).
- OMP course: 9/30. 19 are the same `line 10: syntax error` — `PROGRAM-ID.    CBL0001` **without a terminating period** before `AUTHOR.`; GnuCOBOL 3.1.2 rejects identically (`unexpected ENVIRONMENT, expecting COMMON or EXTERNAL or RECURSIVE or INITIAL`); IBM Enterprise COBOL accepts it, so this is a strictness gap in the whole OpenCOBOL lineage. `PAYROL0X.cobol:25: 'GROSS-PAY' is not numeric` is a deliberately broken lab program.
- BankDemo: 10/82. 46 are EXEC CICS (`'EXEC' undefined` / `Unknown statement 'EXEC'`), 12 are `MBANKnn.cpy: 'BANKnnAI' not the original definition` — BMS-generated map copybooks contain `01 BANK60AI REDEFINES MAPAREA.` and later `01 BANK60AO REDEFINES BANK60AI.` (a REDEFINES of a REDEFINES at level 01), which IBM allows and cobj rejects; 11 `'x' undefined` (DFHCOMMAREA/EIB fields), 3 `Unknown statement`.
- NIST (src2): 263/459 (GnuCOBOL 3.1.2: 264/459 on the same files — essentially identical, as expected from shared ancestry). 169 of the failures are `syntax error`, concentrated in the modules whose FD clauses contain placeholders (RL 2/35, ST 5/40, RW 0/6, SM 6/17, SG 3/13, CM 0/9, DB 3/15, IX 23/42, SQ 52/85) versus NC 85/95, IC 44/47, IF 33/45. Also: 4× `'x' reserved word, but not supported yet`, 2× `ALTER statement is obsolete and unsupported`, and **one segfault**: `IC235A.CBL` (nested programs `IC235A-1`, `IC235A-2` inside `IC235A` with `CALL "IC235A-2" USING ...`) — `cobj -C` exits 139 while `cobc -fsyntax-only` accepts the file.
- Project's own claim vs. measurement: the README's "8804 Pass, 0 Fail" on NIST refers to runtime test *results* after the suite's own driver resolves the `XXXXX0nn` placeholders and selection codes; our 263/459 is a translate-only pass rate over the raw CCVS files after a partial normalization and should not be read as contradicting it.

tree-sitter-cobol (Sakamoto) results: CardDemo 9/31, OMP 10/30, BankDemo 9/82, NIST raw 195/459, NIST src2 269/459. First-error lines: `IF TRANCAT-ACCT-ID NOT= WS-LAST-ACCT-NUM` (CBACT04C.cbl:194 — `NOT=` without a space), paragraph headers right after `PROCEDURE DIVISION.` (`0000-MAIN.`, `MAIN-PARA.` in 3 CardDemo and most BankDemo programs — the error is reported at the paragraph but the cause is the preceding EXEC CICS or `MOVE SPACES TO WS-ERROR-MSG` at col 11), `PROGRAM-ID.    CBL0001` without period (same as cobj), `MOVE WS-BANK-DATA TO DFHCOMMAREA (1:LENGTH OF WS-BANK-DATA)` (LENGTH OF inside a reference modifier), an 88-level VALUE with a long HTML literal (CBSTM03A.CBL:157). tree-sitter never fails a file; the count is of `ERROR`/`MISSING` nodes, so "fail" here means at least one such node.

GnuCOBOL 3.1.2 `cobc -fsyntax-only` baseline on NIST src2: 264/459; the failure classes are `unexpected Literal, expecting EXTERNAL or GLOBAL` (76 — the quoted placeholder standing in an FD clause), `unexpected Identifier, expecting SEQUENTIAL` (33 — `RECORD-KEY`), `expecting OMITTED or STANDARD` (31 — `LABEL RECORDS ARE "XXXXX0nn"`), `invalid indicator 'x' at column 7` (13 — codes U/H/E/G/J).

### Inferences
- On *mainframe-style* code (CardDemo, BankDemo) the ranking for "parses without error" is Koopa ≈ ProLeap (100% with copybook stubs) >> tree-sitter-cobol > cobj. On *standard-conformance* code (NIST) ProLeap and the OpenCOBOL lineage are within ~5% of each other and the residual failures are test-suite artefacts (placeholders, selection codes) more than grammar gaps; Koopa's 100% is not comparable because it skips silently.
- The CICS problem is the dividing line: ProLeap treats `EXEC CICS ... END-EXEC` as an opaque text token (`EXECCICSLINE : EXECCICSTAG WS ~('\n'|'\r'|'}')* ...` — note it is *line*-oriented, so a multi-line EXEC becomes several tokens), Koopa parses it into command/option/value, cobj does not accept it at all, and tree-sitter-cobol breaks on it. Only che4z (5,289-line CICS grammar) and Spantree's grammar claim typed CICS parsing, and neither was run here.
- The real-world failure classes that any new front end must handle, observed in this corpus: tabs in the indicator area; `PROGRAM-ID. X` without a period; `ALTERNATE KEY` without `RECORD`; `FUNCTION TRIM(x, TRAILING)` keyword arguments; `NOT=` written as one token; `LENGTH OF` inside a reference modifier; nested 01-level REDEFINES chains and REDEFINES larger than the target in BMS map copybooks; quoted `COPY 'NAME'`; missing IBM-supplied copybooks (DFHAID, DFHBMSCA, SQLCA, DFHEIBLK/EIB fields) that must be synthesized by the tool.

### Gaps
- No run with the IBM dialect switches on ProLeap (`CobolDialect` has only ANSI85/MF/OSVS; MF was used throughout).
- The Koopa `<unknown>` analysis counted nodes, not programs where the dropped text changes semantics; a per-statement diff against ProLeap's tree would be needed to quantify what Koopa really loses.
- cobj was only run translate-only (`-C`); runtime equivalence was checked on one 42-line program (below), not on CardDemo's batch programs (they need VSAM files and `cobj-idx`).
- The NIST normalization (`nist/src2`) is my approximation of what the CCVS driver does, verified only by the fact that GnuCOBOL's pass count rose from 195 to 264 with it; the remaining ~190 NIST "failures" shared by all tools are mostly placeholder artefacts, not measured grammar coverage.

## Key question 3: What Java the translators produce (PIC numerics, REDEFINES, PERFORM THRU, GO TO, EXEC CICS/SQL), does it compile, is it JOBOL, provenance

### Takeaway
opensourcecobol4j's Java is a faithful, byte-level, runtime-library-driven lowering — "JOBOL" in the strongest sense: every data item is a `CobolDataStorage` byte array with a `CobolField` attribute descriptor, every arithmetic goes through `CobolDecimal`, control flow is a trampoline over an array of `CobolControl` label objects — and it compiles and runs with results identical to GnuCOBOL on the test program. Every statement carries a comment `/* file:line: VERB */`, so line-level provenance is complete.

### Cited Findings (all **[measured]**, from `/home/claude/cobol-research/tools/runs/oc4j/CONSTR.{cbl,java,c}` and `runs/oc4j-carddemo/*.java`)

Test program `CONSTR.cbl` (42 lines; packed decimal, display numerics, edited picture, REDEFINES, OCCURS/INDEXED BY, 88-level, PERFORM THRU, PERFORM VARYING, COMPUTE ROUNDED, IF/GO TO loop, STOP RUN) -> `CONSTR.java` 435 lines; `javac` compiles it against the hand-built runtime; running it prints `TOTAL:      3.50` … `TOTAL:     24.50` (7 lines), **byte-identical to `cobc -x` of the same source**.

PIC numerics — storage is raw bytes, typing lives in a side attribute object:
```java
b_WS_AMOUNT = new CobolDataStorage(5);   /* WS-AMOUNT  PIC S9(7)V99 COMP-3 */
b_WS_COUNT  = new CobolDataStorage(4);   /* WS-COUNT   PIC 9(4) */
f_WS_AMOUNT = CobolFieldFactory.makeCobolField(5, b_WS_AMOUNT, a_1_NumericPacked_Digits9_Scale2_HaveSign);
f_WS_COUNT  = CobolFieldFactory.makeCobolField(4, b_WS_COUNT,  a_5_NumericDisplay_Digits4_Scale0);
f_WS_DISP   = CobolFieldFactory.makeCobolField(10, b_WS_DISP,  a_2_NumericEdited_Digits7_Scale2_HaveSign);
// initialization
f_WS_AMOUNT.moveFrom (c_1_123456);         // VALUE 1234.56 via a numeric constant field with scale 2
b_WS_COUNT.fillBytes (48, 4);              // VALUE 0 -> ASCII '0' x4
// COMPUTE WS-AMOUNT ROUNDED = WS-TOTAL * 1.5 / 3
d0.setField (f_WS_TOTAL); d2.setField (c_2_15); d0.mul (d2); d1.set (3); d0.div (d1); d0.getField (f_WS_AMOUNT, 5);
// IF WS-COUNT > 5
if (((long)b_WS_COUNT.cmpNumdisp (4, 5) >  0L))
// ADD 1 TO WS-COUNT
f_WS_COUNT.add (c_4_1, 4);
```
Arithmetic is done in a `CobolDecimal` scratch register set (d0, d1, d2) exactly as GnuCOBOL's `cob_decimal_*`; the trailing integer arguments are flag words (ROUNDED, ON SIZE ERROR) copied from the C API.

REDEFINES — pure storage aliasing, no Java type:
```java
b_WS_REC = new CobolDataStorage(20);
b_WS_KEY__WS_REC = b_WS_REC;                       // 05 WS-KEY -> same storage, offset 0
b_WS_E_VAL__WS_ENTRY__WS_TBL = b_WS_TBL;           // OCCURS element: base storage; subscripts become offsets
// OCCURS access: ADD WS-E-VAL(WS-IDX) TO WS-TOTAL
f_WS_TOTAL.add (CobolFieldFactory.makeCobolField(3, b_WS_E_VAL__WS_ENTRY__WS_TBL.getSubDataStorage(3 * (b_WS_IDX.intValue() - 1)), a_4_NumericDisplay_Digits3_Scale0), 4);
// MOVE 7 TO WS-E-VAL(3)  (constant subscript folded at translate time)
b_WS_E_VAL__WS_ENTRY__WS_TBL.getSubDataStorage(3 * 2).setBytes (str_1_007, 3);
```
In CardDemo CBACT04C (`01 TWO-BYTES-BINARY PIC 9(4) BINARY. 01 TWO-BYTES-ALPHA REDEFINES TWO-BYTES-BINARY. 05 TWO-BYTES-LEFT PIC X. 05 TWO-BYTES-RIGHT PIC X.`) the generated code is `b_TWO_BYTES_RIGHT__TWO_BYTES_ALPHA.setByte(b_IO_STAT2__IO_STATUS.getByte(0)); f_IO_STATUS_0403__IO_STATUS_04.moveFrom (f_TWO_BYTES_BINARY);` — the classic "convert a status byte via a binary redefinition" idiom works because the storage is shared bytes.

PERFORM THRU / GO TO / paragraphs — a label trampoline:
```java
public CobolControl[] contList = {
  /* MAIN-PARA */
  new CobolControl(l_MAIN_SECTION__MAIN_PARA, CobolControl.LabelType.label) {
    public Optional<CobolControl> run() throws CobolRuntimeException, CobolStopRunException {
      /* CONSTR.cbl:22: PERFORM */
      /* PERFORM INIT-PARA THRU INIT-EXIT */
      CobolControl.performThrough(contList, l_MAIN_SECTION__INIT_PARA, l_MAIN_SECTION__INIT_EXIT).run();
      ...
      /* CONSTR.cbl:30: GO TO */
      { if(true) return Optional.of(contList[l_MAIN_SECTION__END_PARA]); }
      ...
      /* CONSTR.cbl:33: GO TO */
      { if(true) return Optional.of(contList[l_MAIN_SECTION__MAIN_PARA]); }
      return Optional.of(contList[l_MAIN_SECTION__INIT_PARA]);   // fall-through to next paragraph
    }
  }, ...
};
public void execEntry(int start) { Optional<CobolControl> next = Optional.of(contList[start]); while (next.isPresent()) next = next.get().run(); }
```
GO TO is `return Optional.of(contList[target])` from the paragraph object; fall-through is an explicit return of the next paragraph; PERFORM THRU runs paragraphs `from..to` by index (so a GO TO out of a performed range behaves as in COBOL, and `ALTER` is rejected at translate time — "ALTER statement is obsolete and unsupported").

Files and status: `h_TCATBAL_FILE.open (1, 0, f_TCATBALF_STATUS); if (CobolRuntimeException.code != 0) { /* PERFORM Default Error Handler */ CobolControl.perform(contList, l_Default_Error_Handler).run(); }` then `if (((long)b_TCATBALF_STATUS.memcmp (str_0_00, 2) == 0L))` for `IF TCATBALF-STATUS = '00'`.

EXEC CICS: rejected at translate time (`'EXEC' undefined`, `'EIBCALEN' undefined`); there is no CICS lowering to show. EXEC SQL: accepted (grammar token `EXEC_SQL_STATEMENT`, `esql-parser.y`, runtime `libcobj/sql/CobolEsqlBackendPostgresql.java`) and lowered to calls on an ESQL backend; not exercised in this session (the ESQL runtime package was excluded from the hand build because it needs the PostgreSQL driver).

Compiles? Yes: `CONSTR.java` and all 11 translated CardDemo batch programs compile with `javac` against the runtime (0 errors, 211 class files) once the `libcobj.sql.*` import is dropped. Size: CBACT01C.cbl 430 lines -> CBACT01C.java 1,826 lines (4.2×); CONSTR 42 -> 435 (10×, dominated by fixed boilerplate).

Is it JOBOL? Yes, by design: a Java program that is a transliteration of GnuCOBOL's C output (same `b_`/`f_`/`a_`/`c_`/`d0..dn`/`h_` naming, same `cob_*` call shapes renamed to methods). There are no Java types for COBOL data; `int`/`long` appear only for indexes (`b_WS_IDX.set((int)1)`) and comparison results. Readability for a Java maintainer is low; semantic fidelity is high. The project's own issue tracker lists performance as the active concern (#879, #845, #844).

Provenance: every statement is preceded by `/* <abs path>/CBACT01C.cbl:141: DISPLAY */`, every paragraph by `/* NAME */`, every storage declaration by `/* COBOL-NAME */`; Java identifiers encode the qualified COBOL name (`b_ACCT_CURR_BAL__ACCOUNT_RECORD`). Nothing maps Java *runtime* exceptions back to COBOL lines beyond these comments. `cobj -g` passes debug to javac but no COBOL-line SMAP is emitted (not verified in depth).

RES / actojat: not run (RES source unreachable; actojat lacks MOVE and PERFORM VARYING per its README, so it cannot translate any corpus program) — [actojat README](https://github.com/ArturHD/cobol-to-java-transpiler).

### Inferences
- opensourcecobol4j proves that an exact literal lowering of COBOL to Java is achievable and already exists under GPL/LGPL; the open problems it leaves are (a) front-end coverage of IBM dialect and CICS, (b) output readability, (c) performance of the byte-array runtime — exactly the "intermediate literal Java map, then refactor with an intent/inference map" shape, with (a) being the actual blocker on real mainframe code.
- Because `cobj` is GnuCOBOL's `codegen.c` retargeted, its output is a direct empirical answer to "what does the literal intermediate form look like": it is the C output with `cob_field` -> `CobolField`, `goto` -> label trampoline, pointer arithmetic -> `getSubDataStorage(offset)`.

### Gaps
- Runtime equivalence on multi-program, file-driven code (CardDemo batch with VSAM via `cobj-idx`) not tested.
- EXEC SQL lowering not shown (runtime excluded from the offline build).
- Whether `cobj -g` produces line tables usable by a Java debugger was not checked.

## Key question 4: GnuCOBOL's C output for the same constructs (reference design)

### Takeaway
`cobc -C` on the same 42-line program yields three files (`CONSTR.c` 287 lines, `CONSTR.c.h` 74, `CONSTR.c.l.h` 52) in which every COBOL data item is a static byte buffer `b_N` plus a `cob_field {size, data, attr}` descriptor, every statement is a call into `libcob` under a `/* Line: N : VERB : file */` comment, and PERFORM/GO TO become `goto` labels with a hand-rolled perform stack using GCC computed gotos. It is the cleanest existing specification of an exact lowering and the Java translator above is a near-mechanical transliteration of it.

### Cited Findings (all **[measured]**, `/home/claude/cobol-research/tools/runs/oc4j/CONSTR.c*`; GnuCOBOL 3.1.2)

Data layout (`CONSTR.c.l.h`, `CONSTR.c.h`):
```c
static cob_u8_t b_8[5]  __attribute__((aligned));  /* WS-AMOUNT */
static cob_u8_t b_9[4]  __attribute__((aligned));  /* WS-COUNT */
static cob_u8_t b_12[20] __attribute__((aligned)); /* WS-REC */      -- WS-KEY/WS-DATA and the REDEFINES WS-REC-NUM get no storage of their own
static int      b_20;                              /* WS-IDX */      -- INDEXED BY index is a plain C int
static const cob_field_attr a_4 = {0x12, 9, 2, 0x0001, NULL};   /* type NUMERIC_PACKED, digits 9, scale 2, flags HAVE_SIGN */
static const cob_field_attr a_7 = {0x10, 4, 0, 0x0000, NULL};   /* NUMERIC_DISPLAY, 4 digits */
static const cob_field_attr a_5 = {0x24, 7, 2, 0x0001, p_1};    /* NUMERIC_EDITED with picture program p_1 for ZZ,ZZ9.99- */
static cob_field f_8  = {5, b_8, &a_4};   /* WS-AMOUNT */
static cob_field f_11 = {10, b_11, &a_5}; /* WS-DISP */
static const cob_field c_1 = {2, (cob_u8_ptr)"15", &a_1};         /* literal 1.5 as digits "15" scale 1 */
```
(type codes from `/usr/include/libcob/common.h`: `COB_TYPE_NUMERIC_DISPLAY 0x10`, `NUMERIC_PACKED 0x12`, `ALPHANUMERIC 0x21`, `NUMERIC_EDITED 0x24`.) Fields are only materialized for items used as whole operands; subordinate items accessed by MOVE of literals become raw `memcpy (b_12, "ABCDEFGH", 8)`.

Procedure body (`CONSTR.c`):
```c
  /* Line: 22        : PERFORM            : CONSTR.cbl */
  /* PERFORM INIT-PARA THRU INIT-EXIT */
  frame_ptr++;
  frame_ptr->perform_through = 6;
  frame_ptr->return_address_ptr = &&l_8;
  goto l_5;
  l_8:
  frame_ptr--;

  /* Line: 23        : PERFORM            : CONSTR.cbl */
  b_20 = 1;
  for (;;) {
    if (((int)(b_20 - 10) > 0)) break;
    /* Line: 24        : ADD                : CONSTR.cbl */
    cob_add_int (&f_10, cob_get_numdisp (b_18 + 3 * (b_20 - 1), 3), 0);
    b_20 = (b_20 + 1);
  }
  /* Line: 26        : COMPUTE            : CONSTR.cbl */
  cob_decimal_set_field (d0, &f_10);  cob_decimal_mul (d0, dc_1);  cob_decimal_div (d0, dc_2);  cob_decimal_get_field (d0, &f_8, 1);
  /* Line: 27        : MOVE               : CONSTR.cbl */
  cob_move (&f_8, &f_11);
  /* Line: 29        : IF                 : CONSTR.cbl */
  if (((int)cob_cmp_numdisp (b_9, 4, 5LL, 0) > 0)) {
    /* Line: 30        : GO TO              : CONSTR.cbl */
    goto l_7;
  }
  /* Line: 33        : GO TO              : CONSTR.cbl */
  goto l_4;
  /* Line: 34        : Paragraph INIT-PARA               : CONSTR.cbl */
  l_5:;
  memcpy (b_12, "ABCDEFGH", 8);
  memcpy (b_18 + 3 * 2, "007", 3);
  /* Line: 37        : Paragraph INIT-EXIT               : CONSTR.cbl */
  l_6:;
  /* Implicit PERFORM return */
  if (frame_ptr->perform_through == 6)
    goto *frame_ptr->return_address_ptr;
  /* Line: 39        : Paragraph END-PARA                : CONSTR.cbl */
  l_7:;
  *(b_22) = 89;                           /* SET DONE TO TRUE -> store 'Y' */
  cob_stop_run (b_2);
```
Design points worth copying: (1) the PERFORM frame stack (`struct cob_frame frame_stack[255]`) with the "implicit PERFORM return" check placed at the *end of every paragraph that is a THRU target*, which is what makes PERFORM THRU + GO TO interplay exact; (2) the `d0..dn` decimal scratch registers and `dc_n` pre-converted literal decimals; (3) specialized fast paths chosen at compile time by operand attributes (`cob_add_int`, `cob_cmp_numdisp`, `memcpy` for literal MOVEs) with the generic `cob_move`/`cob_decimal_*` as the fallback; (4) the module/program prologue (`P_initialize`, `cob_module` struct holding the program's decimal point, currency sign, collating sequence, etc.) that makes runtime behaviour depend only on data; (5) one comment per statement with line number and verb, which is the entire provenance mechanism.

### Inferences
- GnuCOBOL's `codegen.c` + `typeck.c` are the de-facto reference for what a "literal lowering" must preserve: storage layout (sizes, alignment, REDEFINES overlap, OCCURS strides), the attribute tuple `(type, digits, scale, flags, picture-program)`, decimal arithmetic semantics including ROUNDED/SIZE ERROR flags, and the perform-stack control-flow model. A Java target can (and in opensourcecobol4j, does) reproduce it 1:1.
- Because `cobc` is GPL, reusing `codegen.c` directly makes the tool GPL; reusing the *design* (as a specification) does not.

### Gaps
- Not examined: how GnuCOBOL lowers EXEC SQL (needs a precompiler such as ocesql/esqlOC; not installed), CALL/USING linkage, SORT, and REPORT WRITER.

## Key question 5: What is reusable for a new compiler — the parser and nothing else? Recommendation

### Takeaway
Reuse a *grammar*, not a pipeline: the best-tested open COBOL grammars are ProLeap's (MIT, ANTLR, 100% on CardDemo/BankDemo once copybooks resolve, ~ cobc parity on NIST, but EXEC CICS/SQL as opaque text and a copybook finder with a real bug) and che4z's (EPL-2.0, ANTLR 4.13.2, the only open 5k-line CICS and DB2 grammars, untested here). Nothing else is worth carrying: Koopa's tree is excellent but its island grammar silently drops text; the OpenCOBOL-lineage front ends (cobj, cobc, tree-sitter-cobol) reject common IBM idioms; cb2xml/JRecord cover the data division only. GnuCOBOL/opensourcecobol4j should be used as the *semantic oracle and reference lowering*, not as code.

### Cited Findings
- ProLeap grammar coverage **[measured]**: 31/31 CardDemo, 79/82 BankDemo (3 = `ALTERNATE KEY` gap), 26/30 OMP (3 = `FUNCTION TRIM(..., TRAILING)` gap, 1 correct rejection), 279/459 NIST with zero failures that cobc accepts. AST quality: it is an ASG (`io.proleap.cobol.asg.metamodel.Program` -> compilation units -> divisions -> data entries with resolved references), built by 1,119 Java files on top of the ANTLR tree; `Program.getCompilationUnits()` returned 1 unit per program throughout. Copybook handling: directory- or file-list based, with extension list; the quoted-literal finder ignores the extension list (`LiteralCopyBookFinderImpl.isMatchingCopyBookAbsolute`); REPLACE/REPLACING handled by the preprocessor grammar ("preprocessor executes COPY, REPLACE, CBL and PROCESS" — [README](https://github.com/uwol/proleap-cobol-parser)). Dialect switches: `CobolDialect {ANSI85, MF, OSVS}` only — no IBM Enterprise setting; indicator column accepts only space/`*`/`/`/`-`/`D`/`$` style characters and throws on anything else **[measured]**. EXEC blocks: `execCicsStatement : EXECCICSLINE+` — text lines, no structure **[measured]**; the project confirms "extracted as texts" — [README](https://github.com/uwol/proleap-cobol-parser). Open issues confirm grammar gaps for XML/JSON GENERATE (#106) and `performVaryingPhrase` accepting a literal for an index (#103) — [issues](https://github.com/uwol/proleap-cobol-parser/issues).
- che4z **[measured from clone]**: `CobolParser.g4` 2,423 + `CobolLexer.g4` 971 lines, `CICSParser.g4` 5,289, `Db2SqlExecParser.g4` 1,659, IDMS and DaCo dialect grammars as separate modules; built as an LSP so the pipeline (preprocessor, copybook resolution, `engine`) is entangled with lsp4j/guice; the `parser` module alone is 15 files. License EPL-2.0 (weak copyleft at file level; usable in a proprietary product if the EPL files are kept separate — this is a legal judgment, verify).
- Koopa **[measured]**: full CST with all trivia, structured EXEC CICS and EXEC SQL, 100% acceptance — and `<unknown>` island nodes in 473/493 NIST files and every CardDemo/BankDemo file, 605 of them wrapping skipped text (EXEC SQL bodies, MF screen syntax). The grammar is in Koopa's own `.kg` DSL with its own generator (not ANTLR), BSD.
- OpenCOBOL-lineage front ends **[measured]**: `cobj`/`cobc` reject `PROGRAM-ID. X` without period (19/30 OMP programs), tabs in area A (CUSTREC.cpy), 01-level REDEFINES-of-REDEFINES and oversize REDEFINES in BMS copybooks (12 BankDemo + 3 CardDemo), and have no CICS; `cobj` segfaults on IC235A (nested programs). tree-sitter-cobol inherits the grammar and adds its own breakages (`NOT=`, `LENGTH OF` in refmod).
- opensourcecobol4j back end **[measured]**: compiles and runs correctly; output is JOBOL; GPLv3 translator / LGPLv3 runtime; active performance issues (#879, #845, #844) — [issues](https://github.com/opensourcecobol/opensourcecobol4j/issues).
- Spantree's grammar claims 65/66 CardDemo files clean with typed CICS/SQL nodes but 6.3% on NIST — [README](https://github.com/Spantree/tree-sitter-cobol-enterprise); not reproducible here.

### Inferences (recommendation)
1. **Front end: fork ProLeap's two ANTLR grammars (MIT) as the base, and graft in che4z's `CICSParser.g4`/`Db2SqlExecParser.g4` (EPL-2.0) as sub-parsers invoked on ProLeap's EXEC text tokens.** This gets the measured 100% on CardDemo/BankDemo plus structured CICS/DB2 without writing a CICS grammar. Fix list from this study, all small: `ALTERNATE KEY` optional RECORD; keyword arguments in `FUNCTION` calls (`TRAILING`/`LEADING`); an IBM dialect enum; a permissive indicator-column policy; the quoted-COPY extension bug; synthesize DFHAID/DFHBMSCA/DFHEIBLK/SQLCA when missing. Budget to validate against che4z's full grammar as the alternative base if ProLeap's ASG layer (1,119 files) proves heavier than wanted — the ANTLR tree alone may be enough for a compiler that builds its own IR.
2. **Do not reuse ProLeap's ASG, Koopa, tree-sitter, cb2xml or any translator code in the new compiler.** Koopa's silent skipping is disqualifying for a deterministic compiler; tree-sitter's error-recovery is the wrong property for the same reason; cb2xml duplicates what the grammar already gives.
3. **Use GnuCOBOL (`cobc -C`) and opensourcecobol4j (`cobj -C`) as oracles**: run both on every program the new compiler accepts, diff the data layouts (`b_N` sizes/offsets, `cob_field_attr` tuples) and the runtime outputs against the new back end's. Their *design* (attribute tuple, decimal registers, perform-frame stack, per-statement line comments) is the specification for the literal "intermediate map"; their *code* is GPL and stays out.
4. **The corpus and harness built here are reusable as the regression set**: `nist/src2` (normalized CCVS), the CICS stubs, the three driver scripts (`proleap-cobol-parser/build/Driver.java`, `runs/cobj-run.sh`, `tree-sitter-cobol/run_ts.py`) and the per-file result tables in `tools/results/`.

### Gaps
- che4z's grammar was not run; the recommendation to graft its CICS grammar assumes it parses CardDemo/BankDemo CICS at least as well as Koopa did — to be verified (est. 30 min: compile `common` + `parser` + `engine` grammars with the 4.13.2 jar; `engine` pulls guice/lsp4j so only the ANTLR-generated classes should be used).
- Licensing compatibility of EPL-2.0 grammar files inside an MIT/proprietary compiler was not analysed.
- No measurement of parse *time* at scale beyond ProLeap's 6 ms–2 s per CardDemo program.
