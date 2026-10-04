# Kernel map: Katana's own functions, append-only

Generated from the source by the kernel-map script (phase 0.6) and committed; review its diff, never
edit a line by hand (docs/ROADMAP.md 4.3). One line per function: its module, its name, the decision
it stands on or the caller it serves, and the commit it was first seen in. A line is never changed;
a function that goes away gets a later line with `superseded-by:` or `removed:` and the commit.
`test/KernelMapTest.java` fails on a function removed or renamed without one.

| Module | Function | Stands on | First seen | Superseded |
| --- | --- | --- | --- | --- |
| oracle | `oracle/nist/Population.java:Population.Population` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Population.java:Population.read` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Population.java:Population.card` | called by `oracle/nist/Population.java:Population.read` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Result.ok` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Cobc` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.compile` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.execute` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.run` | called by `oracle/nist/Cobc.java:Cobc.compile` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.Exec85` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.cards` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.program` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.records` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.lines` | called by `oracle/nist/Exec85.java:Exec85.cards` and 3 more | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.undefined` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.run` | called by `oracle/nist/Cobc.java:Cobc.compile` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.Report` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.read` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.footer` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.last` | called by `oracle/nist/Report.java:Report.read` | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.count` | called by `oracle/nist/Report.java:Report.read` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.compileModule` | called by `oracle/nist/ModuleRun.java:ModuleRun.runMain` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.cobc` | called by `oracle/nist/Cobc.java:Cobc.compile` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.Outcome.compileError` | called by `oracle/nist/ModuleRun.java:ModuleRun.runMain` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.Outcome.compiled` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.ModuleRun` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.module` | called by `oracle/nist/ModuleRun.java:ModuleRun.run` | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.run` | called by `oracle/nist/Cobc.java:Cobc.cobc` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.runMain` | called by `oracle/nist/ModuleRun.java:ModuleRun.run` | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.runProgram` | called by `oracle/nist/ModuleRun.java:ModuleRun.runMain` | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.isSubroutine` | called by `oracle/nist/ModuleRun.java:ModuleRun.runMain` | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.write` | called by `oracle/nist/Exec85.java:Exec85.run` and 3 more | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.writeCopy` | called by `oracle/nist/ModuleRun.java:ModuleRun.run` | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.firstLine` | called by `oracle/nist/ModuleRun.java:ModuleRun.Outcome.compileError` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.execute#2` | called by `oracle/nist/Cobc.java:Cobc.execute` and 2 more | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.run#2` | called by `oracle/nist/Cobc.java:Cobc.cobc` and 2 more | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.removeFile` | called by `oracle/nist/ModuleRun.java:ModuleRun.runProgram` | 2026-10-04 |  |
| oracle | `oracle/nist/RunNotes.java:RunNotes.RunNotes` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/RunNotes.java:RunNotes.read` | called by `oracle/nist/Exec85.java:Exec85.run` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/RunNotes.java:RunNotes.of` | called by `oracle/nist/Cobc.java:Cobc.cobc` and 6 more | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.Run.with` | called by `oracle/nist/Reconciliation.java:Reconciliation.apply` | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.Reconciliation` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.run` | called by `oracle/nist/Cobc.java:Cobc.cobc` and 4 more | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.golden` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.lines` | called by `oracle/nist/Exec85.java:Exec85.cards` and 10 more | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.check` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.apply` | called by `oracle/nist/Reconciliation.java:Reconciliation.check` | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.rows` | called by `oracle/nist/Reconciliation.java:Reconciliation.check` | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.visual` | called by `oracle/nist/Report.java:Report.read` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Cobc#2` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.dialect` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.DialectChanges` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.read` | called by `oracle/nist/Exec85.java:Exec85.run` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.check` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.index` | called by `oracle/nist/DialectChanges.java:DialectChanges.check` | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.same` | called by `oracle/nist/DialectChanges.java:DialectChanges.check` | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.cards#2` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.switches` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.causes` | called by `oracle/nist/DialectChanges.java:DialectChanges.check` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.measured` | called by `oracle/nist/DialectChanges.java:DialectChanges.causes` | 2026-10-04 |  |
