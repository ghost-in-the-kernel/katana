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
| profile | `profile/Profile.java:Profile.ibmValues` | no caller | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.truncation` | called by `profile/Profile.java:Profile.read` | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.findings` | no caller | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.concatenates` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.form` | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.existingGeneration` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.start` | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.existing` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.tabbed` | called by `oracle/lane/CobcLane.java:CobcLane.end` | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.note` | called by `oracle/lane/CobcLane.java:CobcLane.prepare` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.notes` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.Unsupported.Unsupported` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.SortCards` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.read` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 6 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.symbols` | called by `oracle/lane/SortCards.java:SortCards.read` | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.keys` | called by `oracle/lane/SortCards.java:SortCards.read` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.field` | called by `oracle/lane/SortConditions.java:SortConditions.comparison` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.constant` | called by `oracle/lane/SortConditions.java:SortConditions.comparison` | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.isConstant` | called by `oracle/lane/SortCards.java:SortCards.symbols` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.operand` | called by `oracle/lane/SortCards.java:SortCards.read` | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.split` | called by `oracle/fixture/Content.java:Content.records` and 20 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.statements` | called by `oracle/lane/SortCards.java:SortCards.read` | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.firstWord` | called by `oracle/lane/SortCards.java:SortCards.symbols` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.SortConditions` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.parse` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.clock` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.comparison` | called by `oracle/lane/SortConditions.java:SortConditions.parse` | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.field` | called by `oracle/lane/SortConditions.java:SortConditions.comparison` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.character` | called by `oracle/lane/SortConditions.java:SortConditions.comparison` | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.zoned` | called by `oracle/lane/SortConditions.java:SortConditions.comparison` | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.text` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.holds` | called by `oracle/lane/SortConditions.java:SortConditions.character` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.SortOutrec` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.parse` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.clock` and 2 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.item` | called by `oracle/lane/SortOutrec.java:SortOutrec.parse` | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.column` | called by `oracle/lane/SortOutrec.java:SortOutrec.parse` | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.edit` | called by `oracle/lane/SortOutrec.java:SortOutrec.item` | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.build` | called by `oracle/lane/Datasets.java:Datasets.generated` and 4 more | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.Sorts` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.run` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 14 more | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.input` | called by `oracle/lane/Sorts.java:Sorts.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.order` | called by `oracle/lane/Sorts.java:Sorts.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.output` | called by `oracle/lane/Sorts.java:Sorts.run` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.Tabs` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.expand` | called by `oracle/lane/Programs.java:Programs.read` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.has` | called by `oracle/lane/Programs.java:Programs.read` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.shadow` | called by `oracle/lane/CobcLane.java:CobcLane.prepare` | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.cards#2` | called by `oracle/fixture/Fixture.java:Fixture.card` and 5 more | 2026-10-04 |  |
| oracle | `oracle/lane/Zoned.java:Zoned.Zoned` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/Zoned.java:Zoned.value` | called by `oracle/lane/Idcams.java:Idcams.command` and 5 more | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.copied` | called by `oracle/lane/CobcLane.java:CobcLane.end` | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.runGenerated` | called by `oracle/lane/Sorts.java:Sorts.generated` | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.SortCobol` | no caller | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.source` | called by `oracle/lane/Programs.java:Programs.module` and 3 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.record` | called by `oracle/lane/SortCobol.java:SortCobol.source` | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.collect` | called by `oracle/lane/SortCobol.java:SortCobol.source` | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.condition` | called by `oracle/lane/SortCobol.java:SortCobol.source` | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.Compare.test` | called by `oracle/lane/SortConditions.java:SortConditions.Group.test` and 1 more | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.Group.test` | called by `oracle/lane/Sorts.java:Sorts.run` | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.generated` | called by `oracle/lane/Datasets.java:Datasets.load` and 2 more | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.options` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` | 2026-10-05 |  |
| oracle | `oracle/lane/Steps.java:Steps.rules` | called by `oracle/lane/Steps.java:Steps.execute` | 2026-10-05 |  |
| oracle | `oracle/lane/Steps.java:Steps.header` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` and 4 more | 2026-10-05 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.widths` | called by `oracle/lane/CobcLane.java:CobcLane.CobcLane` | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.Dfsort` | no caller | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.run` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 15 more | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.order` | called by `oracle/sort/Dfsort.java:Dfsort.run` | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.ties` | called by `oracle/sort/Dfsort.java:Dfsort.run` | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.output` | called by `oracle/nist/Exec85.java:Exec85.run` and 3 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.Unsupported.Unsupported` | no caller | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.SortCards` | no caller | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.read` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 7 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.symbols` | called by `oracle/sort/SortCards.java:SortCards.read` | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.keys` | called by `oracle/sort/Dfsort.java:Dfsort.run` and 1 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.field` | called by `oracle/sort/Dfsort.java:Dfsort.order` and 3 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.constant` | called by `oracle/sort/SortConditions.java:SortConditions.comparison` and 2 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.isConstant` | called by `oracle/sort/SortCards.java:SortCards.symbols` and 1 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.operand` | called by `oracle/sort/SortCards.java:SortCards.read` | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.split` | called by `oracle/fixture/Content.java:Content.records` and 20 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.statements` | called by `oracle/sort/SortCards.java:SortCards.read` | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.firstWord` | called by `oracle/sort/SortCards.java:SortCards.symbols` and 1 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.Compare.test` | called by `oracle/sort/Dfsort.java:Dfsort.run` and 1 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.Group.test` | called by `oracle/sort/Dfsort.java:Dfsort.run` | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.SortConditions` | no caller | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.parse` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.clock` and 1 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.comparison` | called by `oracle/sort/SortConditions.java:SortConditions.parse` | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.field` | called by `oracle/sort/Dfsort.java:Dfsort.order` and 2 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.character` | called by `oracle/sort/SortConditions.java:SortConditions.Compare.test` | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.zoned` | called by `oracle/sort/SortConditions.java:SortConditions.Compare.test` | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.text` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` and 4 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.holds` | called by `oracle/sort/SortConditions.java:SortConditions.character` and 1 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.SortOutrec` | no caller | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.parse` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.clock` and 2 more | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.item` | called by `oracle/sort/SortOutrec.java:SortOutrec.parse` | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.column` | called by `oracle/sort/SortOutrec.java:SortOutrec.parse` | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.edit` | called by `oracle/sort/SortOutrec.java:SortOutrec.item` | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.build` | called by `oracle/lane/Datasets.java:Datasets.generated` and 4 more | 2026-10-05 |  |
| oracle | `oracle/sort/Zoned.java:Zoned.Zoned` | no caller | 2026-10-05 |  |
| oracle | `oracle/sort/Zoned.java:Zoned.value` | called by `oracle/lane/Idcams.java:Idcams.command` and 5 more | 2026-10-05 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.Unsupported.Unsupported` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.Unsupported.Unsupported` |
| oracle | `oracle/lane/SortCards.java:SortCards.SortCards` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.SortCards` |
| oracle | `oracle/lane/SortCards.java:SortCards.read` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.read` |
| oracle | `oracle/lane/SortCards.java:SortCards.symbols` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.symbols` |
| oracle | `oracle/lane/SortCards.java:SortCards.keys` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.keys` |
| oracle | `oracle/lane/SortCards.java:SortCards.field` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.field` |
| oracle | `oracle/lane/SortCards.java:SortCards.constant` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.constant` |
| oracle | `oracle/lane/SortCards.java:SortCards.isConstant` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.isConstant` |
| oracle | `oracle/lane/SortCards.java:SortCards.operand` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.operand` |
| oracle | `oracle/lane/SortCards.java:SortCards.split` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.split` |
| oracle | `oracle/lane/SortCards.java:SortCards.statements` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.statements` |
| oracle | `oracle/lane/SortCards.java:SortCards.firstWord` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortCards.java:SortCards.firstWord` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.SortConditions` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.SortConditions` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.parse` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.parse` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.comparison` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.comparison` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.field` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.field` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.character` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.character` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.zoned` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.zoned` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.text` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.text` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.holds` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.holds` |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.SortOutrec` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortOutrec.java:SortOutrec.SortOutrec` |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.parse` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortOutrec.java:SortOutrec.parse` |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.item` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortOutrec.java:SortOutrec.item` |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.column` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortOutrec.java:SortOutrec.column` |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.edit` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortOutrec.java:SortOutrec.edit` |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.build` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortOutrec.java:SortOutrec.build` |
| oracle | `oracle/lane/Sorts.java:Sorts.order` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/Dfsort.java:Dfsort.order` |
| oracle | `oracle/lane/Sorts.java:Sorts.output` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/Dfsort.java:Dfsort.output` |
| oracle | `oracle/lane/Zoned.java:Zoned.Zoned` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/Zoned.java:Zoned.Zoned` |
| oracle | `oracle/lane/Zoned.java:Zoned.value` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/Zoned.java:Zoned.value` |
| oracle | `oracle/lane/Programs.java:Programs.runGenerated` | moved or retired in phase 1.5b | 2026-10-05 | removed: the generated-COBOL sort retired by R-010 (phase 1.5b) |
| oracle | `oracle/lane/SortCobol.java:SortCobol.SortCobol` | moved or retired in phase 1.5b | 2026-10-05 | removed: the generated-COBOL sort retired by R-010 (phase 1.5b) |
| oracle | `oracle/lane/SortCobol.java:SortCobol.source` | moved or retired in phase 1.5b | 2026-10-05 | removed: the generated-COBOL sort retired by R-010 (phase 1.5b) |
| oracle | `oracle/lane/SortCobol.java:SortCobol.record` | moved or retired in phase 1.5b | 2026-10-05 | removed: the generated-COBOL sort retired by R-010 (phase 1.5b) |
| oracle | `oracle/lane/SortCobol.java:SortCobol.collect` | moved or retired in phase 1.5b | 2026-10-05 | removed: the generated-COBOL sort retired by R-010 (phase 1.5b) |
| oracle | `oracle/lane/SortCobol.java:SortCobol.condition` | moved or retired in phase 1.5b | 2026-10-05 | removed: the generated-COBOL sort retired by R-010 (phase 1.5b) |
| oracle | `oracle/lane/SortConditions.java:SortConditions.Compare.test` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.Compare.test` |
| oracle | `oracle/lane/SortConditions.java:SortConditions.Group.test` | moved or retired in phase 1.5b | 2026-10-05 | superseded-by: `oracle/sort/SortConditions.java:SortConditions.Group.test` |
| oracle | `oracle/lane/Sorts.java:Sorts.generated` | moved or retired in phase 1.5b | 2026-10-05 | removed: the generated-COBOL sort retired by R-010 (phase 1.5b) |
| oracle | `oracle/lane/Gate.java:Gate.Gate` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.compare` | called by `oracle/sort/Dfsort.java:Dfsort.ties` | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.require` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.dataset` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 2 more | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.dds` | called by `oracle/fixture/Fixture.java:Fixture.place` and 9 more | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.stdouts` | called by `oracle/lane/Gate.java:Gate.compare` | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.ranges` | called by `oracle/lane/Gate.java:Gate.dataset` | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.read` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 8 more | 2026-10-05 |  |
| oracle | `oracle/lane/Report.java:Report.lrecls` | called by `oracle/lane/Gate.java:Gate.compare` | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.fixed` | called by `oracle/fixture/Content.java:Content.records` and 3 more | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.lines` | called by `oracle/fixture/Content.java:Content.records` and 18 more | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.rdw` | no caller | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.split` | called by `oracle/compare/Compare.java:Compare.Side.fixed` and 23 more | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Compare` | no caller | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.compare` | called by `oracle/sort/Dfsort.java:Dfsort.ties` | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.record` | called by `oracle/compare/Compare.java:Compare.compare` | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.same` | called by `oracle/compare/Compare.java:Compare.record` and 1 more | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.note` | called by `oracle/compare/Compare.java:Compare.record` and 2 more | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.Decode` | no caller | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.charset` | called by `oracle/compare/Decode.java:Decode.value` | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.value` | called by `oracle/compare/Compare.java:Compare.record` and 7 more | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.packed` | called by `oracle/compare/Decode.java:Decode.value` | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.zonedEbcdic` | called by `oracle/compare/Decode.java:Decode.value` | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.zonedAscii` | called by `oracle/compare/Decode.java:Decode.value` | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.hex` | called by `oracle/compare/Compare.java:Compare.record` and 3 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.Unsupported.Unsupported` | no caller | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.Layout` | no caller | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.fields` | called by `oracle/compare/Compare.java:Compare.record` | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.length` | called by `oracle/compare/Compare.java:Compare.record` and 30 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.read` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 8 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.items` | called by `oracle/compare/Layout.java:Layout.read` | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.item` | called by `oracle/compare/Layout.java:Layout.items` and 1 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.place` | called by `oracle/compare/Layout.java:Layout.read` and 1 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.elementary` | called by `oracle/compare/Layout.java:Layout.place` | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.expand` | called by `oracle/compare/Layout.java:Layout.elementary` and 2 more | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.Setup.Setup` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.javaLimits` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` | 2026-10-05 |  |
| oracle | `oracle/lane/JavaPrograms.java:JavaPrograms.JavaPrograms` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/JavaPrograms.java:JavaPrograms.run` | called by `oracle/lane/CobcLane.java:CobcLane.run` and 15 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.raw` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.Setup.Setup#2` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.Setup.traced` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.coverage` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 2 more | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.Paragraphs` | no caller | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.of` | called by `oracle/compare/Layout.java:Layout.raw` and 42 more | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.entered` | called by `oracle/lane/CobcLane.java:CobcLane.coverage` and 1 more | 2026-10-05 |  |
| oracle | `oracle/lane/Report.java:Report.coverage` | called by `oracle/lane/CobcLane.java:CobcLane.coverage` and 3 more | 2026-10-05 |  |
| oracle | `oracle/lane/Report.java:Report.coverage#2` | called by `oracle/lane/CobcLane.java:CobcLane.coverage` and 3 more | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.ProofReport` | no caller | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.write` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 14 more | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.profileLine` | called by `oracle/proof/ProofReport.java:ProofReport.write` | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.compared` | called by `oracle/proof/ProofReport.java:ProofReport.write` | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.coverage` | called by `oracle/lane/CobcLane.java:CobcLane.coverage` and 2 more | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.neverCovers` | called by `oracle/proof/ProofReport.java:ProofReport.write` | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.sysouts` | called by `oracle/lane/Gate.java:Gate.compare` | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.names` | called by `oracle/lane/Gate.java:Gate.sysouts` | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.stdouts` | moved or retired by R-012 (SYSOUT DDs and stderr held) | 2026-10-05 | superseded-by: `oracle/lane/Gate.java:Gate.sysouts` |
| oracle | `oracle/lane/Gate.java:Gate.sysoutLines` | called by `oracle/lane/Gate.java:Gate.sysouts` | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.normalize` | called by `oracle/lane/Gate.java:Gate.sysoutLines` | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Numproc.of` | called by `oracle/compare/Layout.java:Layout.raw` and 43 more | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.signVariant` | called by `oracle/compare/Compare.java:Compare.record` | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.negative` | called by `oracle/compare/Compare.java:Compare.signVariant` | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.variable` | called by `oracle/lane/CobcLane.java:CobcLane.run#2` | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.Copybooks` | no caller | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.index` | called by `front/Copybooks.java:Copybooks.Copybooks` and 1 more | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.read` | called by `front/Preprocessor.java:Preprocessor.exitCopyStatement` and 10 more | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.unquote` | called by `front/Copybooks.java:Copybooks.read` | 2026-10-05 |  |
| front | `front/Errors.java:Errors.Errors` | no caller | 2026-10-05 |  |
| front | `front/Errors.java:Errors.listen` | called by `front/Parse.java:Parse.parse` and 1 more | 2026-10-05 |  |
| front | `front/Errors.java:Errors..syntaxError` | no caller | 2026-10-05 |  |
| front | `front/Lines.java:Lines.Lines` | no caller | 2026-10-05 |  |
| front | `front/Lines.java:Lines.Line.of` | called by `front/Lines.java:Lines.normalize` and 44 more | 2026-10-05 |  |
| front | `front/Lines.java:Lines.normalize` | called by `front/Preprocessor.java:Preprocessor.text` and 1 more | 2026-10-05 |  |
| front | `front/Lines.java:Lines.join` | called by `front/Lines.java:Lines.normalize` and 18 more | 2026-10-05 |  |
| front | `front/Lines.java:Lines.open` | called by `front/Lines.java:Lines.join` and 1 more | 2026-10-05 |  |
| front | `front/Lines.java:Lines.trailing` | no caller | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Result.clean` | no caller | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Parse` | no caller | 2026-10-05 |  |
| front | `front/Parse.java:Parse.file` | called by `oracle/lane/Datasets.java:Datasets.generated` and 1 more | 2026-10-05 |  |
| front | `front/Parse.java:Parse.text` | called by `front/Parse.java:Parse.file` and 12 more | 2026-10-05 |  |
| front | `front/Parse.java:Parse.parse` | called by `front/Parse.java:Parse.text` and 3 more | 2026-10-05 |  |
| front | `front/Parse.java:Parse.errorNodes` | called by `front/Parse.java:Parse.text` | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.Preprocessor` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.run` | called by `front/Parse.java:Parse.text` and 17 more | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.text` | called by `front/Parse.java:Parse.file` and 13 more | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.enterEveryRule` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitCompilerOptions` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitEjectStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitSkipStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitTitleStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitReplaceByStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitReplaceOffStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitCopyStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitExecCicsStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitExecSqlStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitExecSqlImsStatement` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exec` | called by `front/Preprocessor.java:Preprocessor.exitExecCicsStatement` and 2 more | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitReplaceArea` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.visitTerminal` | no caller | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.replace` | called by `front/Preprocessor.java:Preprocessor.exitCopyStatement` and 14 more | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.operand` | called by `front/Preprocessor.java:Preprocessor.replace` and 1 more | 2026-10-05 |  |
| front | `front/Tabs.java:Tabs.Tabs` | no caller | 2026-10-05 |  |
| front | `front/Tabs.java:Tabs.expand` | called by `front/Copybooks.java:Copybooks.read` and 5 more | 2026-10-05 |  |
| front | `front/Tabs.java:Tabs.has` | called by `oracle/lane/Programs.java:Programs.read` and 2 more | 2026-10-05 |  |
| front | `front/Lines.java:Lines.normalize#2` | called by `front/Lines.java:Lines.normalize` and 2 more | 2026-10-05 |  |
| front | `front/Lines.java:Lines.strip` | called by `front/Lines.java:Lines.normalize#2` and 4 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.source` | called by `front/Lines.java:Lines.normalize#2` and 4 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.add` | called by `front/Errors.java:Errors.listen` and 92 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.added` | called by `front/Lines.java:Lines.normalize#2` and 4 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.append` | called by `front/Lines.java:Lines.normalize#2` and 28 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.append#2` | called by `front/Lines.java:Lines.normalize#2` and 27 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.slice` | called by `front/Lines.java:Lines.normalize#2` and 2 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.truncate` | called by `front/Lines.java:Lines.join` and 1 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.length` | called by `front/Lines.java:Lines.normalize#2` and 45 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.charAt` | called by `front/Lines.java:Lines.normalize#2` and 11 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.origin` | called by `front/Mapped.java:Mapped.added` and 3 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.toString` | called by `front/Copybooks.java:Copybooks.Copybooks` and 40 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.replaceAll` | called by `front/Copybooks.java:Copybooks.unquote` and 10 more | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.grow` | called by `front/Mapped.java:Mapped.add` | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Result.origin` | called by `front/Mapped.java:Mapped.added` and 2 more | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Result.origin#2` | called by `front/Mapped.java:Mapped.added` and 2 more | 2026-10-05 |  |
| front | `front/Parse.java:Parse.text#2` | called by `front/Parse.java:Parse.file` and 13 more | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.Result.text` | called by `front/Parse.java:Parse.file` and 14 more | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.run#2` | called by `front/Parse.java:Parse.text#2` and 18 more | 2026-10-05 |  |
| front | `front/Lines.java:Lines.Line.of` | moved or retired in phase 2.3 (lines read as Mapped text) | 2026-10-05 | superseded-by: `front/Lines.java:Lines.normalize#2` |
| sir | `sir/types/Attributes.java:Attributes.Attributes` | no caller | 2026-10-05 |  |
| sir | `sir/types/Call.java:Call.Call` | no caller | 2026-10-05 |  |
| sir | `sir/types/FileDescriptor.java:FileDescriptor.FileDescriptor` | no caller | 2026-10-05 |  |
| sir | `sir/types/Header.java:Header.Header` | no caller | 2026-10-05 |  |
| sir | `sir/types/Invalid.java:Invalid.Invalid` | no caller | 2026-10-05 |  |
| sir | `sir/types/Invalid.java:Invalid.origin` | called by `front/Mapped.java:Mapped.added` and 12 more | 2026-10-05 |  |
| sir | `sir/types/Item.java:Item.Occurs.Occurs` | no caller | 2026-10-05 |  |
| sir | `sir/types/Item.java:Item.Condition.Condition` | no caller | 2026-10-05 |  |
| sir | `sir/types/Item.java:Item.Item` | no caller | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Section.Section` | no caller | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Paragraph.Paragraph` | no caller | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Edge.Edge` | no caller | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Procedure` | no caller | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.index` | called by `front/Copybooks.java:Copybooks.Copybooks` and 2 more | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.Program` | no caller | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.dataMap` | called by `sir/types/Program.java:Program.Program` | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.references` | called by `sir/types/Program.java:Program.Program` | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.statement` | called by `sir/types/Program.java:Program.references` | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.value` | called by `oracle/compare/Compare.java:Compare.record` and 9 more | 2026-10-05 |  |
| sir | `sir/types/Statement.java:Statement.Branch.Branch` | no caller | 2026-10-05 |  |
| sir | `sir/types/Statement.java:Statement.Statement` | no caller | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Field.Field` | no caller | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.ConditionName.ConditionName` | no caller | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Expression.Expression` | no caller | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.inside` | called by `sir/types/Procedure.java:Procedure.Procedure` | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.index` | moved or retired in phase 2.4 (a compact constructor cannot read its fields) | 2026-10-05 | superseded-by: `sir/types/Procedure.java:Procedure.inside` |
| oracle | `oracle/nist/Cobc.java:Cobc.translate` | no caller | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.Build` | no caller | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.data` | called by `sir/build/Build.java:Build.all` | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.all` | called by `oracle/lane/CobcLane.java:CobcLane.end` | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.data#2` | called by `sir/build/Build.java:Build.data` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.first` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` and 6 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.units` | called by `sir/build/Build.java:Build.data` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.nested` | called by `sir/build/Build.java:Build.units` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.DataMap` | no caller | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.section` | called by `sir/build/Build.java:Build.data#2` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.format1` | called by `sir/build/DataMap.java:DataMap.section` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.add` | called by `front/Errors.java:Errors.listen` and 106 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.index` | called by `front/Copybooks.java:Copybooks.Copybooks` and 3 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.clauses` | called by `sir/build/DataMap.java:DataMap.format1` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.sign` | called by `sir/build/DataMap.java:DataMap.clauses` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.occurs` | called by `oracle/compare/Layout.java:Layout.place` and 3 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.condition` | called by `sir/build/DataMap.java:DataMap.section` and 1 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.renames` | called by `sir/build/DataMap.java:DataMap.section` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.usage` | called by `oracle/compare/Layout.java:Layout.elementary` and 1 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.text` | called by `front/Parse.java:Parse.file` and 16 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.resolve` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 29 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.sibling` | called by `sir/build/DataMap.java:DataMap.resolve` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.named` | called by `sir/build/DataMap.java:DataMap.resolve` and 2 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.layout` | called by `sir/build/Build.java:Build.data#2` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.place` | called by `oracle/compare/Layout.java:Layout.read` and 3 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.extent` | called by `sir/build/DataMap.java:DataMap.place` and 1 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.renamed` | called by `sir/build/DataMap.java:DataMap.layout` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.alignment` | called by `sir/build/DataMap.java:DataMap.place` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.size` | called by `front/Lines.java:Lines.normalize#2` and 50 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.items` | called by `oracle/compare/Layout.java:Layout.read` and 1 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.keys` | called by `oracle/sort/Dfsort.java:Dfsort.run` and 3 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.indexes` | called by `sir/build/DataMap.java:DataMap.items` | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.attributes` | called by `oracle/fixture/Fixture.java:Fixture.Dd.attributes` and 19 more | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.read` | called by `front/Preprocessor.java:Preprocessor.exitCopyStatement` and 11 more | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.read#2` | called by `front/Preprocessor.java:Preprocessor.exitCopyStatement` and 12 more | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.digits` | called by `sir/build/DataMap.java:DataMap.size` and 2 more | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.scale` | called by `oracle/compare/Decode.java:Decode.value` and 2 more | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.floating` | called by `sir/build/Pictures.java:Pictures.scale` | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.firstOf` | called by `sir/build/Pictures.java:Pictures.scale` | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.lastOf` | called by `sir/build/Pictures.java:Pictures.scale` | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.expand` | called by `front/Copybooks.java:Copybooks.read` and 6 more | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.Symbols.scan` | called by `sir/build/Pictures.java:Pictures.read#2` | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.Symbols.category` | called by `sir/build/DataMap.java:DataMap.attributes` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.program` | called by `oracle/lane/Generated.java:Generated.loader` and 8 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.program#2` | called by `oracle/lane/Generated.java:Generated.loader` and 8 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.using` | called by `sir/build/Build.java:Build.program#2` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.returning` | called by `sir/build/Build.java:Build.program#2` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.all#2` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.data#3` | called by `sir/build/Build.java:Build.program#2` and 3 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.first` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` and 10 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.inRecord` | called by `sir/build/DataMap.java:DataMap.renamed` | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.Files` | no caller | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.build` | called by `oracle/lane/Datasets.java:Datasets.generated` and 5 more | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.entries` | called by `sir/build/Files.java:Files.build` | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.organization` | called by `sir/build/Files.java:Files.build` | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.access` | called by `sir/build/Files.java:Files.build` | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.Names` | no caller | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.item` | called by `oracle/compare/Layout.java:Layout.items` and 9 more | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.condition` | called by `sir/build/Build.java:Build.program#2` and 4 more | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.qualified` | called by `sir/build/Names.java:Names.item` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.inFile` | called by `sir/build/Names.java:Names.item` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.Paragraphs` | no caller | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.build` | called by `oracle/lane/Datasets.java:Datasets.generated` and 5 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.section` | called by `sir/build/Build.java:Build.data#3` and 2 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.paragraphs` | called by `sir/build/Paragraphs.java:Paragraphs.build` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.statement` | called by `sir/build/Paragraphs.java:Paragraphs.build` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.walk` | called by `front/Preprocessor.java:Preprocessor.text` and 4 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.edges` | called by `sir/build/Paragraphs.java:Paragraphs.statement` | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.target` | called by `sir/build/Paragraphs.java:Paragraphs.walk` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.range` | called by `sir/build/Paragraphs.java:Paragraphs.edges` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.call` | called by `sir/build/Paragraphs.java:Paragraphs.statement` | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.verbName` | called by `sir/build/Paragraphs.java:Paragraphs.statement` | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.role` | called by `oracle/nist/ModuleRun.java:ModuleRun.isSubroutine` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.Values` | no caller | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.isValue` | called by `sir/build/Paragraphs.java:Paragraphs.walk` | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.of` | called by `oracle/compare/Layout.java:Layout.raw` and 60 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.file` | called by `front/Mapped.java:Mapped.added` and 7 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.identifier` | called by `sir/build/Paragraphs.java:Paragraphs.call` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.argument` | called by `sir/build/Values.java:Values.identifier` | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.table` | called by `sir/build/Values.java:Values.of` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.field` | called by `oracle/compare/Compare.java:Compare.compare` and 7 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.named` | called by `sir/build/DataMap.java:DataMap.resolve` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.conditionName` | called by `sir/build/DataMap.java:DataMap.condition` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.literal` | called by `sir/build/Build.java:Build.data#3` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.figurative` | called by `sir/build/Values.java:Values.of` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.expression` | called by `sir/build/Values.java:Values.of` | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.upper` | called by `sir/build/Build.java:Build.program#2` and 17 more | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.Check` | no caller | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.findings` | called by `oracle/proof/ProofReport.java:ProofReport.compared` and 1 more | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.check` | called by `sir/check/Check.java:Check.findings` | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.targets` | called by `sir/check/Check.java:Check.check` | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.IrJson` | no caller | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.write` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 16 more | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.write#2` | called by `oracle/lane/CobcLane.java:CobcLane.end` and 16 more | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.record` | called by `oracle/compare/Compare.java:Compare.compare` and 3 more | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.string` | called by `sir/json/IrJson.java:IrJson.write#2` and 1 more | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.read` | called by `front/Preprocessor.java:Preprocessor.exitCopyStatement` and 12 more | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.convert` | called by `sir/json/IrJson.java:IrJson.read` | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.enumValue` | called by `sir/json/IrJson.java:IrJson.convert` | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.Parser` | no caller | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.value` | called by `oracle/compare/Compare.java:Compare.record` and 15 more | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.object` | called by `sir/json/IrJson.java:IrJson.Parser.value` | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.array` | called by `sir/json/IrJson.java:IrJson.Parser.value` | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.text` | called by `front/Parse.java:Parse.file` and 23 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.Contract` | no caller | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.lines` | called by `oracle/fixture/Content.java:Content.records` and 20 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.diff` | no caller | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.each` | no caller | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.item` | called by `oracle/compare/Layout.java:Layout.items` and 12 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.occurs` | called by `oracle/compare/Layout.java:Layout.place` and 4 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.file` | called by `front/Mapped.java:Mapped.added` and 7 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.falls` | no caller | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.statements` | called by `oracle/sort/SortCards.java:SortCards.read` and 6 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.statement` | called by `sir/build/Paragraphs.java:Paragraphs.build` and 4 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.call` | called by `sir/build/Paragraphs.java:Paragraphs.statement` | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.value` | called by `oracle/compare/Compare.java:Compare.record` and 17 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.quoted` | called by `oracle/fixture/Fixture.java:Fixture.canonical` and 2 more | 2026-10-05 |  |
| findings | `findings/Finding.java:Finding.Kind.Kind` | no caller | 2026-10-05 |  |
| findings | `findings/Finding.java:Finding.report` | no caller | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.NonExact` | no caller | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.findings` | called by `oracle/proof/ProofReport.java:ProofReport.compared` and 1 more | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.check` | called by `sir/check/Check.java:Check.findings` and 2 more | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.ranges` | called by `oracle/lane/Gate.java:Gate.dataset` and 1 more | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.division` | called by `sir/check/NonExact.java:NonExact.check` | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.divides` | called by `sir/check/NonExact.java:NonExact.division` | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.range` | called by `sir/build/Paragraphs.java:Paragraphs.edges` and 2 more | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.name` | called by `oracle/compare/Layout.java:Layout.place` and 40 more | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.where` | called by `findings/Finding.java:Finding.report` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Origins.java:Origins.Origins` | no caller | 2026-10-05 |  |
| sir | `sir/build/Origins.java:Origins.at` | called by `sir/build/Build.java:Build.program#2` and 12 more | 2026-10-05 |  |
| sir | `sir/types/Origin.java:Origin.toString` | called by `findings/Finding.java:Finding.report` and 50 more | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.programs` | called by `oracle/lane/CobcLane.java:CobcLane.prepare` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.Expressions` | no caller | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.arithmetic` | called by `sir/build/Expressions.java:Expressions.basis` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.multDivs` | called by `sir/build/Expressions.java:Expressions.arithmetic` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.powers` | called by `sir/build/Expressions.java:Expressions.multDivs` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.basis` | called by `sir/build/Expressions.java:Expressions.powers` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.condition` | called by `sir/build/Build.java:Build.program#2` and 7 more | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.combinable` | called by `sir/build/Expressions.java:Expressions.condition` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.simple` | called by `sir/build/Expressions.java:Expressions.combinable` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.relation` | called by `sir/build/Expressions.java:Expressions.simple` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.abbreviation` | called by `sir/build/Expressions.java:Expressions.condition` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.classCondition` | called by `sir/build/Expressions.java:Expressions.simple` | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.operator` | called by `sir/build/Expressions.java:Expressions.relation` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.binary` | called by `sir/build/Expressions.java:Expressions.arithmetic` and 5 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.branch` | called by `sir/build/Paragraphs.java:Paragraphs.walk` | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.group` | called by `oracle/compare/Layout.java:Layout.items` and 15 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.holdsStatements` | called by `sir/build/Paragraphs.java:Paragraphs.walk` | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.noise` | called by `sir/build/Paragraphs.java:Paragraphs.walk` and 2 more | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.subscript` | called by `sir/build/Values.java:Values.table` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.holdsStatements#2` | called by `sir/build/Paragraphs.java:Paragraphs.walk` and 2 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.verbName#2` | called by `sir/build/Paragraphs.java:Paragraphs.statement` and 2 more | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.role#2` | called by `oracle/nist/ModuleRun.java:ModuleRun.isSubroutine` and 6 more | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.Roles` | no caller | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.vocabulary` | no caller | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.walk` | called by `front/Preprocessor.java:Preprocessor.text` and 6 more | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.called` | called by `sir/build/Roles.java:Roles.vocabulary` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.context` | called by `sir/build/Roles.java:Roles.vocabulary` and 1 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.value` | called by `oracle/compare/Compare.java:Compare.record` and 20 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.interval` | called by `sir/build/DataMap.java:DataMap.condition` | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.Literals` | no caller | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.of` | called by `oracle/compare/Layout.java:Layout.raw` and 78 more | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.quote` | called by `front/Preprocessor.java:Preprocessor.replace` and 2 more | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.unquote` | called by `front/Copybooks.java:Copybooks.read` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.number` | called by `oracle/fixture/Fixture.java:Fixture.place` and 2 more | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.literal` | called by `sir/build/Build.java:Build.data#3` and 6 more | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Literal.Literal` | no caller | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Literal.numeric` | no caller | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.unscaled` | called by `sir/build/Literals.java:Literals.number` | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.Placement` | no caller | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.resolve` | called by `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` and 29 more | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.sibling` | called by `sir/build/Placement.java:Placement.resolve` | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.named` | called by `sir/build/Placement.java:Placement.resolve` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.layout` | called by `sir/build/Build.java:Build.data#3` | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.place` | called by `oracle/compare/Layout.java:Layout.read` and 3 more | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.extent` | called by `sir/build/Placement.java:Placement.place` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.inRecord` | called by `sir/build/Placement.java:Placement.renamed` | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.renamed` | called by `sir/build/Placement.java:Placement.layout` | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.alignment` | called by `sir/build/Placement.java:Placement.place` | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.size` | called by `front/Lines.java:Lines.normalize#2` and 66 more | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.Tuples` | no caller | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.items` | called by `oracle/compare/Layout.java:Layout.read` and 4 more | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.keys` | called by `oracle/sort/Dfsort.java:Dfsort.run` and 4 more | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.indexes` | called by `sir/build/Tuples.java:Tuples.items` | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.attributes` | called by `oracle/fixture/Fixture.java:Fixture.Dd.attributes` and 21 more | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.resolve` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.resolve` |
| sir | `sir/build/DataMap.java:DataMap.sibling` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.sibling` |
| sir | `sir/build/DataMap.java:DataMap.named` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.named` |
| sir | `sir/build/DataMap.java:DataMap.layout` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.layout` |
| sir | `sir/build/DataMap.java:DataMap.place` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.place` |
| sir | `sir/build/DataMap.java:DataMap.extent` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.extent` |
| sir | `sir/build/DataMap.java:DataMap.inRecord` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.inRecord` |
| sir | `sir/build/DataMap.java:DataMap.renamed` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.renamed` |
| sir | `sir/build/DataMap.java:DataMap.alignment` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.alignment` |
| sir | `sir/build/DataMap.java:DataMap.size` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Placement.java:Placement.size` |
| sir | `sir/build/DataMap.java:DataMap.items` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Tuples.java:Tuples.items` |
| sir | `sir/build/DataMap.java:DataMap.keys` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Tuples.java:Tuples.keys` |
| sir | `sir/build/DataMap.java:DataMap.indexes` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Tuples.java:Tuples.indexes` |
| sir | `sir/build/DataMap.java:DataMap.attributes` | moved in tithe pass 2, C3 | 2026-10-05 | superseded-by: `sir/build/Tuples.java:Tuples.attributes` |
| sir | `sir/build/Build.java:Build.currency` | called by `sir/build/Build.java:Build.program#2` and 1 more | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.mask` | called by `sir/build/Tuples.java:Tuples.attributes` and 1 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.flatten` | called by `oracle/compare/Layout.java:Layout.read` | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.field` | called by `oracle/compare/Compare.java:Compare.compare` and 8 more | 2026-10-05 |  |
| oracle | `oracle/compare/Overpunch.java:Overpunch.Overpunch` | no caller | 2026-10-05 |  |
| oracle | `oracle/compare/Overpunch.java:Overpunch.value` | called by `oracle/compare/Compare.java:Compare.record` and 22 more | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.names` | called by `oracle/lane/Gate.java:Gate.sysouts` and 1 more | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.items` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.item` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.place` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.elementary` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.expand` | a second PICTURE expander, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `sir/build/Pictures.java:Pictures.expand` |
| oracle | `oracle/nist/Cobc.java:Cobc.module` | called by `oracle/lane/CobcLane.java:CobcLane.libraries` and 4 more | 2026-10-05 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.program` | called by `oracle/lane/Generated.java:Generated.loader` and 14 more | 2026-10-05 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.run#3` | called by `front/Parse.java:Parse.text#2` and 19 more | 2026-10-05 |  |
| oracle | `oracle/nist/Runner.java:Runner.Result.ok` | called by `oracle/nist/Exec85.java:Exec85.run` and 2 more | 2026-10-05 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Result.ok` | moved to the runner seam in tithe pass 2, B2 | 2026-10-05 | superseded-by: `oracle/nist/Runner.java:Runner.Result.ok` |
