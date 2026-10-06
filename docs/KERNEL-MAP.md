# Kernel map: Katana's own functions, append-only

Generated from the source by the kernel-map script (phase 0.6) and committed; review its diff, never
edit a line by hand (docs/ROADMAP.md 4.3). One line per function: its module, its name, the decision
it cites (`// Decision:` above it; empty when none), and the date it was first seen. A line is never
changed (but once: tithe pass 2, C1, emptied the stale "called by" column, J-089); a function that
goes away gets a later line with `superseded-by:` or `removed:`.
`test/KernelMapTest.java` fails on a function removed or renamed without one.

| Module | Function | Stands on | First seen | Superseded |
| --- | --- | --- | --- | --- |
| oracle | `oracle/nist/Population.java:Population.Population` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Population.java:Population.read` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Population.java:Population.card` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Result.ok` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Cobc` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.compile` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.execute` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.run` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.Exec85` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.cards` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.program` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.records` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.lines` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.undefined` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.run` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.Report` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.read` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.footer` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.last` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.count` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.compileModule` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.cobc` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.Outcome.compileError` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.Outcome.compiled` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.ModuleRun` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.module` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.run` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.runMain` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.runProgram` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.isSubroutine` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.write` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.writeCopy` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.firstLine` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.execute#2` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.run#2` |  | 2026-10-04 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.removeFile` |  | 2026-10-04 |  |
| oracle | `oracle/nist/RunNotes.java:RunNotes.RunNotes` |  | 2026-10-04 |  |
| oracle | `oracle/nist/RunNotes.java:RunNotes.read` |  | 2026-10-04 |  |
| oracle | `oracle/nist/RunNotes.java:RunNotes.of` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.Run.with` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.Reconciliation` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.run` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.golden` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.lines` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.check` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.apply` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Reconciliation.java:Reconciliation.rows` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Report.java:Report.visual` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Cobc#2` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.dialect` |  | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.DialectChanges` |  | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.read` |  | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.check` |  | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.index` |  | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.same` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.cards#2` |  | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.switches` |  | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.causes` |  | 2026-10-04 |  |
| oracle | `oracle/nist/DialectChanges.java:DialectChanges.measured` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.Content` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.records` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.sha256` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.lines` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.padded` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Content.java:Content.fixed` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.Step.name` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.Dd.name` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.Dd.attributes` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.read` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.place` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.card` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.canonical` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.quoted` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.first` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.hash` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Fixture.java:Fixture.sha256` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.FixtureCheck` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.refusals` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.profiles` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.environment` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.clock` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.form` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.start` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.path` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.records` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.keysFit` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.walk` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.step` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.generation` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.Catalog.use` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.Schema` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.specs` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.values` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.ordered` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.line` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.attribute` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.tokens` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/Schema.java:Schema.flush` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.Profile` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.read` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.canonical` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.hash` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.cobcArguments` |  | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.CobcLane` |  | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.run` |  | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.run#2` |  | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.prepare` |  | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.libraries` |  | 2026-10-04 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.end` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.Compiler` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.build` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.compile` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.firstError` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Compiler.java:Compiler.hash` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.Datasets` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.absolute` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.get` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.all` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.sequential` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.indexed` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.load` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.unload` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.delete` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.generated` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.write` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.removeFiles` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Exec.java:Exec.Exec` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Exec.java:Exec.run` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.Shape.tag` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.Key.of` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.Generated` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.loader` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.unloader` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.program` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.keyRecord` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.parmDriver` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.cee3abd` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Generated.java:Generated.fixed` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.Node.value` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.Idcams` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.run` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.command` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.delete` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.set` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.define` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.repro` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.commands` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.nodes` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.list` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.Programs` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.source` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.sourceHashes` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.module` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.stub` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.run` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.read` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.text` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.line` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.step` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.dataset` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.finding` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.steps` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.datasets` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.findings` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.text` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.Start` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.catalog` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.dataset` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Start.java:Start.source` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.Steps` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.run` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.bypassed` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.execute` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.idcams` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.program` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.bind` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.allocate` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.dispose` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Steps.java:Steps.write` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.Utilities` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.cards` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.sysin` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.iebgener` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Idcams.java:Idcams.cluster` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.ibmValues` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.truncation` |  | 2026-10-04 |  |
| profile | `profile/Profile.java:Profile.findings` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.concatenates` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.existingGeneration` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.existing` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.tabbed` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.note` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Report.java:Report.notes` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.Unsupported.Unsupported` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.SortCards` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.read` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.symbols` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.keys` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.field` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.constant` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.isConstant` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.operand` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.split` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.statements` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCards.java:SortCards.firstWord` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.SortConditions` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.parse` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.comparison` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.field` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.character` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.zoned` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.text` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.holds` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.SortOutrec` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.parse` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.item` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.column` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.edit` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortOutrec.java:SortOutrec.build` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.Sorts` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.run` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.input` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.order` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.output` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.Tabs` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.expand` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.has` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.shadow` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Utilities.java:Utilities.cards#2` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Zoned.java:Zoned.Zoned` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Zoned.java:Zoned.value` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.copied` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Programs.java:Programs.runGenerated` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.SortCobol` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.source` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.record` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.collect` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortCobol.java:SortCobol.condition` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.Compare.test` |  | 2026-10-04 |  |
| oracle | `oracle/lane/SortConditions.java:SortConditions.Group.test` |  | 2026-10-04 |  |
| oracle | `oracle/lane/Sorts.java:Sorts.generated` |  | 2026-10-04 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.options` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Steps.java:Steps.rules` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Steps.java:Steps.header` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Tabs.java:Tabs.widths` |  | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.Dfsort` |  | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.run` |  | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.order` |  | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.ties` |  | 2026-10-05 |  |
| oracle | `oracle/sort/Dfsort.java:Dfsort.output` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.Unsupported.Unsupported` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.SortCards` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.read` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.symbols` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.keys` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.field` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.constant` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.isConstant` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.operand` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.split` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.statements` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortCards.java:SortCards.firstWord` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.Compare.test` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.Group.test` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.SortConditions` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.parse` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.comparison` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.field` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.character` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.zoned` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.text` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortConditions.java:SortConditions.holds` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.SortOutrec` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.parse` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.item` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.column` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.edit` |  | 2026-10-05 |  |
| oracle | `oracle/sort/SortOutrec.java:SortOutrec.build` |  | 2026-10-05 |  |
| oracle | `oracle/sort/Zoned.java:Zoned.Zoned` |  | 2026-10-05 |  |
| oracle | `oracle/sort/Zoned.java:Zoned.value` |  | 2026-10-05 |  |
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
| oracle | `oracle/lane/Gate.java:Gate.Gate` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.compare` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.require` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.dataset` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.dds` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.stdouts` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.ranges` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.read` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Report.java:Report.lrecls` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.fixed` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.lines` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.rdw` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Side.split` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Compare` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.compare` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.record` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.same` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.note` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.Decode` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.charset` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.value` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.packed` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.zonedEbcdic` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.zonedAscii` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Decode.java:Decode.hex` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.Unsupported.Unsupported` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.Layout` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.fields` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.length` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.read` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.items` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.item` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.place` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.elementary` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.expand` |  | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.Setup.Setup` |  | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.javaLimits` |  | 2026-10-05 |  |
| oracle | `oracle/lane/JavaPrograms.java:JavaPrograms.JavaPrograms` |  | 2026-10-05 |  |
| oracle | `oracle/lane/JavaPrograms.java:JavaPrograms.run` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.raw` |  | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.Setup.Setup#2` |  | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.Setup.traced` |  | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.coverage` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.Paragraphs` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.of` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.entered` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Report.java:Report.coverage` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Report.java:Report.coverage#2` |  | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.ProofReport` |  | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.write` |  | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.profileLine` |  | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.compared` |  | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.coverage` |  | 2026-10-05 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.neverCovers` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.sysouts` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.names` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.stdouts` | moved or retired by R-012 (SYSOUT DDs and stderr held) | 2026-10-05 | superseded-by: `oracle/lane/Gate.java:Gate.sysouts` |
| oracle | `oracle/lane/Gate.java:Gate.sysoutLines` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Gate.java:Gate.normalize` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.Numproc.of` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.signVariant` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Compare.java:Compare.negative` |  | 2026-10-05 |  |
| oracle | `oracle/lane/CobcLane.java:CobcLane.variable` |  | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.Copybooks` |  | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.index` |  | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.read` |  | 2026-10-05 |  |
| front | `front/Copybooks.java:Copybooks.unquote` |  | 2026-10-05 |  |
| front | `front/Errors.java:Errors.Errors` |  | 2026-10-05 |  |
| front | `front/Errors.java:Errors.listen` |  | 2026-10-05 |  |
| front | `front/Errors.java:Errors..syntaxError` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.Lines` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.Line.of` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.normalize` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.join` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.open` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.trailing` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Result.clean` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Parse` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.file` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.text` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.parse` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.errorNodes` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.Preprocessor` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.run` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.text` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.enterEveryRule` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitCompilerOptions` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitEjectStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitSkipStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitTitleStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitReplaceByStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitReplaceOffStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitCopyStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitExecCicsStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitExecSqlStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitExecSqlImsStatement` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exec` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.exitReplaceArea` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.visitTerminal` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.replace` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.operand` |  | 2026-10-05 |  |
| front | `front/Tabs.java:Tabs.Tabs` |  | 2026-10-05 |  |
| front | `front/Tabs.java:Tabs.expand` |  | 2026-10-05 |  |
| front | `front/Tabs.java:Tabs.has` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.normalize#2` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.strip` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.source` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.add` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.added` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.append` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.append#2` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.slice` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.truncate` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.length` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.charAt` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.origin` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.toString` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.replaceAll` |  | 2026-10-05 |  |
| front | `front/Mapped.java:Mapped.grow` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Result.origin` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.Result.origin#2` |  | 2026-10-05 |  |
| front | `front/Parse.java:Parse.text#2` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.Result.text` |  | 2026-10-05 |  |
| front | `front/Preprocessor.java:Preprocessor.run#2` |  | 2026-10-05 |  |
| front | `front/Lines.java:Lines.Line.of` | moved or retired in phase 2.3 (lines read as Mapped text) | 2026-10-05 | superseded-by: `front/Lines.java:Lines.normalize#2` |
| sir | `sir/types/Attributes.java:Attributes.Attributes` |  | 2026-10-05 |  |
| sir | `sir/types/Call.java:Call.Call` |  | 2026-10-05 |  |
| sir | `sir/types/FileDescriptor.java:FileDescriptor.FileDescriptor` |  | 2026-10-05 |  |
| sir | `sir/types/Header.java:Header.Header` |  | 2026-10-05 |  |
| sir | `sir/types/Invalid.java:Invalid.Invalid` |  | 2026-10-05 |  |
| sir | `sir/types/Invalid.java:Invalid.origin` |  | 2026-10-05 |  |
| sir | `sir/types/Item.java:Item.Occurs.Occurs` |  | 2026-10-05 |  |
| sir | `sir/types/Item.java:Item.Condition.Condition` |  | 2026-10-05 |  |
| sir | `sir/types/Item.java:Item.Item` |  | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Section.Section` |  | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Paragraph.Paragraph` |  | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Edge.Edge` |  | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.Procedure` |  | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.index` |  | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.Program` |  | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.dataMap` |  | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.references` |  | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.statement` |  | 2026-10-05 |  |
| sir | `sir/types/Program.java:Program.value` |  | 2026-10-05 |  |
| sir | `sir/types/Statement.java:Statement.Branch.Branch` |  | 2026-10-05 |  |
| sir | `sir/types/Statement.java:Statement.Statement` |  | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Field.Field` |  | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.ConditionName.ConditionName` |  | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Expression.Expression` |  | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.inside` |  | 2026-10-05 |  |
| sir | `sir/types/Procedure.java:Procedure.index` | moved or retired in phase 2.4 (a compact constructor cannot read its fields) | 2026-10-05 | superseded-by: `sir/types/Procedure.java:Procedure.inside` |
| oracle | `oracle/nist/Cobc.java:Cobc.translate` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.Build` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.data` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.all` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.data#2` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.first` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.units` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.nested` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.DataMap` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.section` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.format1` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.add` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.index` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.clauses` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.sign` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.occurs` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.condition` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.renames` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.usage` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.text` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.resolve` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.sibling` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.named` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.layout` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.place` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.extent` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.renamed` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.alignment` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.size` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.items` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.keys` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.indexes` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.attributes` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.read` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.read#2` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.digits` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.scale` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.floating` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.firstOf` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.lastOf` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.expand` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.Symbols.scan` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.Symbols.category` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.program` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.program#2` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.using` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.returning` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.all#2` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.data#3` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.first` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.inRecord` |  | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.Files` |  | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.build` |  | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.entries` |  | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.organization` |  | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.access` |  | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.Names` |  | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.item` |  | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.condition` |  | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.qualified` |  | 2026-10-05 |  |
| sir | `sir/build/Names.java:Names.inFile` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.Paragraphs` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.build` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.section` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.paragraphs` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.statement` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.walk` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.edges` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.target` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.range` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.call` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.verbName` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.role` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.Values` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.isValue` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.of` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.file` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.identifier` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.argument` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.table` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.field` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.named` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.conditionName` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.literal` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.figurative` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.expression` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.upper` |  | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.Check` |  | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.findings` |  | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.check` |  | 2026-10-05 |  |
| sir | `sir/check/Check.java:Check.targets` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.IrJson` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.write` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.write#2` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.record` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.string` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.read` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.convert` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.enumValue` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.Parser` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.value` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.object` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.array` |  | 2026-10-05 |  |
| sir | `sir/json/IrJson.java:IrJson.Parser.text` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.Contract` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.lines` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.diff` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.each` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.item` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.occurs` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.file` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.falls` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.statements` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.statement` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.call` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.value` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.quoted` |  | 2026-10-05 |  |
| findings | `findings/Finding.java:Finding.Kind.Kind` |  | 2026-10-05 |  |
| findings | `findings/Finding.java:Finding.report` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.NonExact` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.findings` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.check` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.ranges` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.division` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.divides` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.range` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.name` |  | 2026-10-05 |  |
| sir | `sir/check/NonExact.java:NonExact.where` |  | 2026-10-05 |  |
| sir | `sir/build/Origins.java:Origins.Origins` |  | 2026-10-05 |  |
| sir | `sir/build/Origins.java:Origins.at` |  | 2026-10-05 |  |
| sir | `sir/types/Origin.java:Origin.toString` |  | 2026-10-05 |  |
| sir | `sir/build/Build.java:Build.programs` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.Expressions` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.arithmetic` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.multDivs` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.powers` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.basis` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.condition` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.combinable` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.simple` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.relation` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.abbreviation` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.classCondition` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.operator` |  | 2026-10-05 |  |
| sir | `sir/build/Expressions.java:Expressions.binary` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.branch` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.group` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.holdsStatements` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.noise` |  | 2026-10-05 |  |
| sir | `sir/build/Values.java:Values.subscript` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.holdsStatements#2` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.verbName#2` |  | 2026-10-05 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.role#2` |  | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.Roles` |  | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.vocabulary` |  | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.walk` |  | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.called` |  | 2026-10-05 |  |
| sir | `sir/build/Roles.java:Roles.context` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.value` |  | 2026-10-05 |  |
| sir | `sir/build/DataMap.java:DataMap.interval` |  | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.Literals` |  | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.of` |  | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.quote` |  | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.unquote` |  | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.number` |  | 2026-10-05 |  |
| sir | `sir/contract/Contract.java:Contract.literal` |  | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Literal.Literal` |  | 2026-10-05 |  |
| sir | `sir/types/Value.java:Value.Literal.numeric` |  | 2026-10-05 |  |
| sir | `sir/build/Literals.java:Literals.unscaled` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.Placement` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.resolve` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.sibling` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.named` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.layout` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.place` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.extent` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.inRecord` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.renamed` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.alignment` |  | 2026-10-05 |  |
| sir | `sir/build/Placement.java:Placement.size` |  | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.Tuples` |  | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.items` |  | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.keys` |  | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.indexes` |  | 2026-10-05 |  |
| sir | `sir/build/Tuples.java:Tuples.attributes` |  | 2026-10-05 |  |
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
| sir | `sir/build/Build.java:Build.currency` |  | 2026-10-05 |  |
| sir | `sir/build/Pictures.java:Pictures.mask` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.flatten` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.field` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Overpunch.java:Overpunch.Overpunch` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Overpunch.java:Overpunch.value` |  | 2026-10-05 |  |
| oracle | `oracle/lane/Paragraphs.java:Paragraphs.names` |  | 2026-10-05 |  |
| oracle | `oracle/compare/Layout.java:Layout.items` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.item` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.place` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.elementary` | the comparator's own copybook reader, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `oracle/compare/Layout.java:Layout.flatten` |
| oracle | `oracle/compare/Layout.java:Layout.expand` | a second PICTURE expander, retired in tithe pass 2, B1 | 2026-10-05 | superseded-by: `sir/build/Pictures.java:Pictures.expand` |
| oracle | `oracle/nist/Cobc.java:Cobc.module` |  | 2026-10-05 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.program` |  | 2026-10-05 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.run#3` |  | 2026-10-05 |  |
| oracle | `oracle/nist/Runner.java:Runner.Result.ok` |  | 2026-10-05 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Result.ok` | moved to the runner seam in tithe pass 2, B2 | 2026-10-05 | superseded-by: `oracle/nist/Runner.java:Runner.Result.ok` |
| oracle | `oracle/autotest/Autotests.java:Autotests.Malformed.Malformed` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.Autotests` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.read` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.cases` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.macro` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.Builder.Builder` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.arguments` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.quoted` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.quadrigraphs` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.arg` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.name` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.word` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.wordChar` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.skipLine` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.line` |  | 2026-10-05 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.write` |  | 2026-10-05 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.ArithProfile` |  | 2026-10-05 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.add` | R-049 | 2026-10-05 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.multiply` | R-049 | 2026-10-05 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.divide` | R-049 | 2026-10-05 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.carried` | R-049 | 2026-10-05 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.dmax` | R-049 | 2026-10-05 |  |
| sir | `sir/build/Files.java:Files.assign` |  | 2026-10-05 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.last` | R-049 | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.of` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.toBigDecimal` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.toString` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.operand` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.add` | R-020 | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.subtract` | R-020 | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.multiply` | R-020 | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.divide` | R-020 | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.cut` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.store` | R-020 | 2026-10-06 |  |
| rt | `rt/katana/rt/Attr.java:Attr.zoned` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Attr.java:Attr.numeric` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Attr.java:Attr.length` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Codepage.java:Codepage.Codepage` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Codepage.java:Codepage.character` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Codepage.java:Codepage.digit` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Codepage.java:Codepage.digitOf` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Codepage.java:Codepage.negative` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.get` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.put` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.digitsAt` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.zonedGet` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.zonedPut` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.packedGet` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.packedPut` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.binaryGet` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Field.java:Field.binaryPut` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Record.java:Record.of` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Attr.java:Attr.text` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.Move` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.move` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.group` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.number` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.text` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.place` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.bytes` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.digits` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.unsignedInteger` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Attr.java:Attr.numericEdited` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Attr.java:Attr.alphanumericEdited` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.Editor` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.layout` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.count` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.number` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.significance` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.suppressed` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.suppressor` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.symbol` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.point` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.floatingSymbol` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.text` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Editor.java:Editor.deEdit` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.write` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.deEdited` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.Inspect` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.Phrase.width` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.region` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.find` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.matches` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.tally` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.replace` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.convert` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.cycles` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.Strings` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.string` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.unstring` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.examined` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.nextDelimiter` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.text` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.startsWith` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Strings.java:Strings.indexOf` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Collation.java:Collation.nativeOrder` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Collation.java:Collation.of` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Collation.java:Collation.weight` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Compare.java:Compare.Compare` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Compare.java:Compare.numbers` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Compare.java:Compare.text` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Compare.java:Compare.all` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Compare.java:Compare.operand` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Arithmetic.java:Arithmetic.Arithmetic` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Arithmetic.java:Arithmetic.store` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Display.java:Display.Display` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Display.java:Display.of` | R-019 | 2026-10-06 |  |
