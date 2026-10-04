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
| oracle | `oracle/fixture/Content.java:Content.Content` | no caller | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.records` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.start` and 1 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.sha256` | called by `oracle/fixture/Fixture.java:Fixture.hash` | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.lines` | called by `oracle/fixture/Content.java:Content.records` and 15 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.padded` | called by `oracle/fixture/Content.java:Content.lines` | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.fixed` | called by `oracle/fixture/Content.java:Content.records` | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.Step.name` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` and 9 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.Dd.name` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` and 9 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.Dd.attributes` | called by `oracle/fixture/Fixture.java:Fixture.canonical` and 7 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.read` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 2 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.place` | called by `oracle/fixture/Fixture.java:Fixture.read` | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.card` | called by `oracle/fixture/Fixture.java:Fixture.read` and 1 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.canonical` | called by `oracle/fixture/Fixture.java:Fixture.read` and 1 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.quoted` | called by `oracle/fixture/Fixture.java:Fixture.canonical` | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.first` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.hash` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.sha256` | called by `oracle/fixture/Content.java:Content.sha256` and 1 more | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.FixtureCheck` | no caller | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` | no caller | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` | no caller | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` and 1 more | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.clock` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.form` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.start` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` and 1 more | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.path` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.start` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.records` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.start` and 1 more | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.keysFit` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.start` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.walk` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.step` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.walk` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.generation` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.step` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.use` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.step` | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.Schema` | no caller | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.specs` | no caller | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.values` | called by `oracle/fixture/Fixture.java:Fixture.Step.name` and 8 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.ordered` | called by `oracle/fixture/Schema.java:Schema.specs` | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.line` | called by `oracle/fixture/Fixture.java:Fixture.read` and 2 more | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.attribute` | called by `oracle/fixture/Schema.java:Schema.line` | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.tokens` | called by `oracle/fixture/Schema.java:Schema.line` | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.flush` | called by `oracle/fixture/Schema.java:Schema.tokens` | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.Profile` | no caller | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.read` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 2 more | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.canonical` | called by `oracle/fixture/Fixture.java:Fixture.read` and 1 more | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.hash` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.cobcArguments` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.CobcLane` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.run` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` and 12 more | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.run#2` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 12 more | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.prepare` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.libraries` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.end` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.Compiler` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.build` | called by `oracle/lane/Datasets.java:Datasets.generated` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.compile` | called by `oracle/fixture/Schema.java:Schema.values` and 5 more | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.firstError` | called by `oracle/lane/Compiler.java:Compiler.build` | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.hash` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.Datasets` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.absolute` | called by `oracle/lane/Datasets.java:Datasets.get` and 4 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.get` | called by `oracle/fixture/Content.java:Content.records` and 44 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.all` | called by `oracle/lane/CobcLane.java:CobcLane.end` | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.sequential` | called by `oracle/lane/Idcams.java:Idcams.repro` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.indexed` | called by `oracle/lane/Idcams.java:Idcams.define` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.load` | called by `oracle/lane/Datasets.java:Datasets.indexed` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.unload` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.delete` | called by `oracle/lane/CobcLane.java:CobcLane.prepare` and 5 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.generated` | called by `oracle/lane/Datasets.java:Datasets.load` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.write` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 11 more | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.removeFiles` | called by `oracle/lane/Datasets.java:Datasets.load` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Exec.java:Exec.Exec` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Exec.java:Exec.run` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 13 more | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.Shape.tag` | called by `oracle/lane/Datasets.java:Datasets.generated` | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.Key.of` | called by `oracle/fixture/Fixture.java:Fixture.card` and 33 more | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.Generated` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.loader` | called by `oracle/lane/Datasets.java:Datasets.load` | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.unloader` | called by `oracle/lane/Datasets.java:Datasets.unload` | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.program` | called by `oracle/lane/Generated.java:Generated.loader` and 6 more | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.keyRecord` | called by `oracle/lane/Generated.java:Generated.program` | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.parmDriver` | called by `oracle/lane/Programs.java:Programs.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.cee3abd` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.fixed` | called by `oracle/fixture/Content.java:Content.records` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.Node.value` | called by `oracle/lane/Idcams.java:Idcams.command` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.Idcams` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.run` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 13 more | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.command` | called by `oracle/lane/Idcams.java:Idcams.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.delete` | called by `oracle/lane/CobcLane.java:CobcLane.prepare` and 4 more | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.set` | called by `oracle/lane/Idcams.java:Idcams.command` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.define` | called by `oracle/lane/Idcams.java:Idcams.command` | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.repro` | called by `oracle/lane/Idcams.java:Idcams.command` | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.commands` | called by `oracle/lane/Idcams.java:Idcams.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.nodes` | called by `oracle/lane/Idcams.java:Idcams.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.list` | called by `oracle/lane/Idcams.java:Idcams.command` and 4 more | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.Programs` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.source` | called by `oracle/lane/Programs.java:Programs.module` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.sourceHashes` | called by `oracle/lane/CobcLane.java:CobcLane.end` | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.module` | called by `oracle/lane/CobcLane.java:CobcLane.libraries` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.stub` | called by `oracle/lane/CobcLane.java:CobcLane.libraries` | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.run` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 12 more | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.read` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 5 more | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.text` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.line` | called by `oracle/fixture/Fixture.java:Fixture.read` and 10 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.step` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.walk` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.dataset` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.finding` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` and 4 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.steps` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.datasets` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` and 4 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.findings` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.text` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.Start` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.catalog` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.dataset` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.source` | called by `oracle/lane/Programs.java:Programs.module` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.Steps` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.run` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 13 more | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.bypassed` | called by `oracle/lane/Steps.java:Steps.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.execute` | called by `oracle/lane/Steps.java:Steps.run` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.idcams` | called by `oracle/lane/Steps.java:Steps.execute` | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.program` | called by `oracle/lane/Generated.java:Generated.loader` and 6 more | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.bind` | called by `oracle/lane/Steps.java:Steps.execute` | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.allocate` | called by `oracle/lane/Steps.java:Steps.bind` | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.dispose` | called by `oracle/lane/Steps.java:Steps.execute` | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.write` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 11 more | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.Utilities` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.cards` | called by `oracle/fixture/Fixture.java:Fixture.card` and 4 more | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.sysin` | called by `oracle/lane/Steps.java:Steps.idcams` | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.iebgener` | called by `oracle/lane/Steps.java:Steps.execute` | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.cluster` | called by `oracle/lane/Idcams.java:Idcams.define` | 2026-10-04 |  |
