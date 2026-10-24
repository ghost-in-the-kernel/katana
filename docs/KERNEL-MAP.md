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
| rt | `rt/katana/rt/Inspect.java:Inspect.view` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.back` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.Dispatcher` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.perform` | R-021 | 2026-10-06 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.end` | R-021 | 2026-10-06 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.depth` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.alter` | R-021 | 2026-10-06 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.goTo` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.Arith` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.decimals` | R-020 | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.result` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.term` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.term#2` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.operand` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.integerPlaces` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.dmax` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.ArithVerbs` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.statement` | R-020 | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.receivers` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.items` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.store` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.sum` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.op` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.add` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.sumExpression` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.sumExpression` | moved in 6.4, one helper for the passes and the lowering | 2026-10-21 | superseded-by: `lower/Forms.java:Forms.sum` |
| lower | `lower/ArithVerbs.java:ArithVerbs.subtract` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.multiply` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.divide` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.compute` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.Conditions` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.of` | R-019 | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.join` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.relation` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.compare` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.sign` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.alphabetic` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.conditionName` |  | 2026-10-06 |  |
| lower | `lower/Emitter.java:Emitter.line` |  | 2026-10-06 |  |
| lower | `lower/Emitter.java:Emitter.open` |  | 2026-10-06 |  |
| lower | `lower/Emitter.java:Emitter.close` |  | 2026-10-06 |  |
| lower | `lower/Emitter.java:Emitter.provenance` | R-024 | 2026-10-06 |  |
| lower | `lower/Emitter.java:Emitter.statements` |  | 2026-10-06 |  |
| lower | `lower/Emitter.java:Emitter.text` |  | 2026-10-06 |  |
| lower | `lower/Emitter.java:Emitter.quote` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.Exprs` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.item` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.unsupported` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.unsupported#2` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.dimensions` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.occurrences` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.ints` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.field` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.index` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.sending` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.pattern` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.singular` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.moveInto` | R-019 | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.numeric` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.Flow` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.procedure` | R-021 | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.open` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.split` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.label` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.counter` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.end` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.step` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.indentEnd` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.statements` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.splits` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.splits#2` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.target` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.branch` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.jump` |  | 2026-10-06 |  |
| lower | `lower/Initialize.java:Initialize.Initialize` |  | 2026-10-06 |  |
| lower | `lower/Initialize.java:Initialize.statement` | R-019 | 2026-10-06 |  |
| lower | `lower/Initialize.java:Initialize.receives` |  | 2026-10-06 |  |
| lower | `lower/Initialize.java:Initialize.move` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.Lower` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.java` | R-024 | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.declareItems` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.defineItems` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.recordLength` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.attr` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.index` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.stored` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.edit` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.Performs` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.statement` | R-021 | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.count` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.once` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.loop` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.perform` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.varying` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.TextVerbs` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.isKeyword` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.word` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.bytes` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.inspect` | R-019 | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.read` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.Reader.Reader` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.Reader.take` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.Reader.value` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.add` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.phrases` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.converting` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.sent` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.string` | R-019 | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.overflow` |  | 2026-10-06 |  |
| lower | `lower/TextVerbs.java:TextVerbs.unstring` | R-019 | 2026-10-06 |  |
| lower | `lower/Values.java:Values.Values` |  | 2026-10-06 |  |
| lower | `lower/Values.java:Values.initial` | R-019 | 2026-10-06 |  |
| lower | `lower/Values.java:Values.governed` |  | 2026-10-06 |  |
| lower | `lower/Values.java:Values.isText` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.Verbs` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.statement` | R-021 | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.operand` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.operands` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.keyword` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.branch` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.move` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.display` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.shown` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.goTo` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.altered` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.alters` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.alter` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.paragraphIndex` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.exit` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.lastOfSection` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.Classes` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.numeric` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.allDigits` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.zoned` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.packed` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.alphabetic` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Compare.java:Compare.alphanumeric` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Compare.java:Compare.figurative` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.negate` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.add` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Inspect.java:Inspect.repeat` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Literal.java:Literal.Literal` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Literal.java:Literal.number` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Literal.java:Literal.text` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Literal.java:Literal.hex` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Literal.java:Literal.bytes` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Literal.java:Literal.figurative` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.fill` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.group#2` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Sysout.java:Sysout.Sysout` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Sysout.java:Sysout.display` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.Table` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.each` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.walk` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.at` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.ref` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.index` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Unsupported.java:Unsupported.Unsupported` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Unsupported.java:Unsupported.fail` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.Evaluate` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.isKeyword` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.statement` | R-021 | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.chain` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.phrase` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.when` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.pair` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.condition` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.through` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.plain` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.bool` |  | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.FileVerbs` |  | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.name` |  | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.declare` |  | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.open` | R-019 | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.close` |  | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.write` |  | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.read` |  | 2026-10-06 |  |
| lower | `lower/FileVerbs.java:FileVerbs.owner` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.main` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.className` |  | 2026-10-06 |  |
| lower | `lower/Lower.java:Lower.area` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.SeqFile` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.path` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.open` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.write` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.read` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.close` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.remainder` | R-020 | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.read` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.node` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.varies` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.repeat` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.start` |  | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.start` | folded into the VARYING loops, which set every identifier FROM first (J-134) | 2026-10-06 | superseded-by: `lower/Performs.java:Performs.varying` |
| lower | `lower/Performs.java:Performs.step` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.set` | R-019 | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.setTrue` |  | 2026-10-06 |  |
| lower | `lower/Evaluate.java:Evaluate.condition` | folded into `Evaluate.read` and `Evaluate.pair` (J-116) | 2026-10-06 | superseded-by: `lower/Evaluate.java:Evaluate.pair` |
| lower | `lower/Verbs.java:Verbs.control` |  | 2026-10-06 |  |
| sir | `sir/check/NonExact.java:NonExact.leaving` | R-021 | 2026-10-06 |  |
| findings | `findings/Finding.java:Finding.Kind.hypothesis` |  | 2026-10-06 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.findings` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.sentences` | R-021 | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.holdsNextSentence` |  | 2026-10-06 |  |
| lower | `lower/Flow.java:Flow.nextSentence` |  | 2026-10-06 |  |
| lower | `lower/Search.java:Search.Search` |  | 2026-10-06 |  |
| lower | `lower/Search.java:Search.statement` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.set` | R-019 | 2026-10-06 |  |
| sir | `sir/build/Origins.java:Origins.at#2` |  | 2026-10-06 |  |
| sir | `sir/build/Origins.java:Origins.of` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.accept` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Accept.java:Accept.Accept` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Accept.java:Accept.from` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Accept.java:Accept.from#2` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Sysin.java:Sysin.Sysin` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Sysin.java:Sysin.accept` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Sysin.java:Sysin.record` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.ifStatement` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.switchStatus` |  | 2026-10-06 |  |
| lower | `lower/Corresponding.java:Corresponding.Corresponding` |  | 2026-10-06 |  |
| lower | `lower/Corresponding.java:Corresponding.pairs` | R-019 | 2026-10-06 |  |
| lower | `lower/Corresponding.java:Corresponding.walk` |  | 2026-10-06 |  |
| lower | `lower/Corresponding.java:Corresponding.children` |  | 2026-10-06 |  |
| lower | `lower/Corresponding.java:Corresponding.eligible` |  | 2026-10-06 |  |
| lower | `lower/Corresponding.java:Corresponding.group` |  | 2026-10-06 |  |
| lower | `lower/Corresponding.java:Corresponding.number` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Upsi.java:Upsi.Upsi` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Upsi.java:Upsi.of` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Upsi.java:Upsi.on` |  | 2026-10-06 |  |
| sir | `sir/build/Build.java:Build.switches` |  | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.power` | R-020 | 2026-10-06 |  |
| lower | `lower/Arith.java:Arith.holds` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/ArithProfile.java:ArithProfile.power` | R-049 | 2026-10-06 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.power` | R-020 | 2026-10-06 |  |
| sir | `sir/build/Expressions.java:Expressions.top` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.corresponding` | R-020 | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.charClass` | R-019 | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.codes` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.member` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Classes.java:Classes.code` |  | 2026-10-06 |  |
| sir | `sir/build/Build.java:Build.classes` |  | 2026-10-06 |  |
| sir | `sir/build/Build.java:Build.bound` |  | 2026-10-06 |  |
| sir | `sir/types/Header.java:Header.CharClass.CharClass` |  | 2026-10-06 |  |
| lower | `lower/Verbs.java:Verbs.setSwitches` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Upsi.java:Upsi.set` |  | 2026-10-06 |  |
| sir | `sir/build/Build.java:Build.mnemonics` |  | 2026-10-06 |  |
| lower | `lower/Initialize.java:Initialize.replacing` |  | 2026-10-06 |  |
| lower | `lower/Initialize.java:Initialize.replace` |  | 2026-10-06 |  |
| lower | `lower/Values.java:Values.edited` |  | 2026-10-06 |  |
| lower | `lower/Values.java:Values.placed` | R-057 | 2026-10-06 |  |
| rt | `rt/katana/rt/Move.java:Move.place#2` |  | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.storeOnce` | R-020 | 2026-10-06 |  |
| sir | `sir/check/NonExact.java:NonExact.overlap` | R-020 | 2026-10-06 |  |
| sir | `sir/check/NonExact.java:NonExact.fields` |  | 2026-10-06 |  |
| sir | `sir/check/NonExact.java:NonExact.meet` |  | 2026-10-06 |  |
| sir | `sir/check/NonExact.java:NonExact.storage` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.written` |  | 2026-10-06 |  |
| lower | `lower/Collating.java:Collating.Collating` |  | 2026-10-06 |  |
| lower | `lower/Collating.java:Collating.of` | R-019 | 2026-10-06 |  |
| lower | `lower/Collating.java:Collating.literal` |  | 2026-10-06 |  |
| lower | `lower/Collating.java:Collating.code` |  | 2026-10-06 |  |
| lower | `lower/Collating.java:Collating.charCode` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Collation.java:Collation.literal` | R-019 | 2026-10-06 |  |
| rt | `rt/katana/rt/Collation.java:Collation.thru` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Collation.java:Collation.low` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Collation.java:Collation.high` |  | 2026-10-06 |  |
| sir | `sir/build/Build.java:Build.alphabets` |  | 2026-10-06 |  |
| sir | `sir/build/Build.java:Build.collating` |  | 2026-10-06 |  |
| sir | `sir/types/Header.java:Header.Alphabet.Alphabet` |  | 2026-10-06 |  |
| lower | `lower/Conditions.java:Conditions.text` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.receiving` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.field#2` |  | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.variable` | R-019 | 2026-10-06 |  |
| lower | `lower/Exprs.java:Exprs.under` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Table.java:Table.current` | R-019 | 2026-10-06 |  |
| lower | `lower/Performs.java:Performs.varyingAfter` | R-021 | 2026-10-06 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.shared` | R-020 | 2026-10-06 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.Outcome.abnormal` |  | 2026-10-06 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.reason` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Abend.java:Abend.Abend` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Abend.java:Abend.code` |  | 2026-10-06 |  |
| rt | `rt/katana/rt/Abend.java:Abend.line` |  | 2026-10-06 |  |
| sir | `sir/build/Paragraphs.java:Paragraphs.use` |  | 2026-10-08 |  |
| sir | `sir/contract/Contract.java:Contract.declarative` |  | 2026-10-08 |  |
| sir | `sir/types/Procedure.java:Procedure.Use.Use` |  | 2026-10-08 |  |
| sir | `sir/build/Files.java:Files.item` |  | 2026-10-08 |  |
| sir | `sir/build/Names.java:Names.unresolved` |  | 2026-10-08 |  |
| lower | `lower/FileVerbs.java:FileVerbs.closer` | R-019 | 2026-10-08 |  |
| sir | `sir/build/DataMap.java:DataMap.ref` |  | 2026-10-08 |  |
| sir | `sir/build/DataMap.java:DataMap.first` | kept a qualified name's first word only (J-148) | 2026-10-08 | superseded-by: `sir/build/DataMap.java:DataMap.ref` |
| sir | `sir/build/Placement.java:Placement.matching` |  | 2026-10-08 |  |
| sir | `sir/build/Placement.java:Placement.unresolved` |  | 2026-10-08 |  |
| sir | `sir/build/Names.java:Names.resolve` |  | 2026-10-08 |  |
| sir | `sir/build/Names.java:Names.unresolved` | folded into resolve (J-148) | 2026-10-08 | superseded-by: `sir/build/Names.java:Names.resolve` |
| sir | `sir/build/Names.java:Names.item` | split in two by the name sweep (J-148) | 2026-10-08 | superseded-by: `sir/build/Names.java:Names.find` |
| sir | `sir/build/Names.java:Names.find` |  | 2026-10-08 |  |
| sir | `sir/build/Files.java:Files.fds` |  | 2026-10-08 |  |
| sir | `sir/build/Files.java:Files.fd` | R-017 | 2026-10-08 |  |
| sir | `sir/build/Files.java:Files.linage` |  | 2026-10-08 |  |
| sir | `sir/build/Files.java:Files.value` |  | 2026-10-08 |  |
| sir | `sir/build/Files.java:Files.all` |  | 2026-10-08 |  |
| sir | `sir/build/Names.java:Names.note` |  | 2026-10-08 |  |
| sir | `sir/contract/Contract.java:Contract.fd` |  | 2026-10-08 |  |
| sir | `sir/types/FileDescriptor.java:FileDescriptor.Fd.Fd` |  | 2026-10-08 |  |
| sir | `sir/types/FileDescriptor.java:FileDescriptor.Linage.Linage` |  | 2026-10-08 |  |
| lower | `lower/Exprs.java:Exprs.counter` |  | 2026-10-08 |  |
| lower | `lower/Exprs.java:Exprs.counterDigits` |  | 2026-10-08 |  |
| lower | `lower/FileVerbs.java:FileVerbs.linages` | R-019 | 2026-10-08 |  |
| lower | `lower/FileVerbs.java:FileVerbs.supplier` |  | 2026-10-08 |  |
| lower | `lower/FileVerbs.java:FileVerbs.paged` |  | 2026-10-08 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.linage` | R-019 | 2026-10-08 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.counter` |  | 2026-10-08 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.page` | R-019 | 2026-10-08 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.page#2` |  | 2026-10-08 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.count` |  | 2026-10-08 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.blank` |  | 2026-10-08 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.line` |  | 2026-10-08 |  |
| lower | `lower/FileVerbs.java:FileVerbs.noRewind` |  | 2026-10-09 |  |
| lower | `lower/FileVerbs.java:FileVerbs.after` | R-019 | 2026-10-09 |  |
| lower | `lower/FileVerbs.java:FileVerbs.range` |  | 2026-10-09 |  |
| lower | `lower/FileVerbs.java:FileVerbs.fileOf` |  | 2026-10-09 |  |
| lower | `lower/Flow.java:Flow.entry` | R-021 | 2026-10-09 |  |
| lower | `lower/Flow.java:Flow.declaratives` | R-021 | 2026-10-09 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.reason#2` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.status` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.successful` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.failed` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.mode` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.hasStatus` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.abend` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.writable` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.noRewind` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.close#2` |  | 2026-10-09 |  |
| lower | `lower/FileVerbs.java:FileVerbs.rewrite` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.rewrite` | R-019 | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.Linage` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.counter` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.open` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.write` | R-019 | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.page` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.count` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.blank` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Linage.java:Linage.line` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.page#2` | moved to Linage by the split (tithe 3a.5, J-159) | 2026-10-09 | superseded-by: `rt/katana/rt/Linage.java:Linage.page` |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.count` | moved to Linage by the split (tithe 3a.5, J-159) | 2026-10-09 | superseded-by: `rt/katana/rt/Linage.java:Linage.count` |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.blank` | moved to Linage by the split (tithe 3a.5, J-159) | 2026-10-09 | superseded-by: `rt/katana/rt/Linage.java:Linage.blank` |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.line` | moved to Linage by the split (tithe 3a.5, J-159) | 2026-10-09 | superseded-by: `rt/katana/rt/Linage.java:Linage.line` |
| lower | `lower/FileVerbs.java:FileVerbs.varyings` |  | 2026-10-09 |  |
| lower | `lower/FileVerbs.java:FileVerbs.sizes` | R-019 | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.varying` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.current` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Varying.java:Varying.Varying` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Varying.java:Varying.length` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Varying.java:Varying.outside` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Varying.java:Varying.write` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Varying.java:Varying.read` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/Varying.java:Varying.status` |  | 2026-10-09 |  |
| rt | `rt/katana/rt/SeqFile.java:SeqFile.readVarying` |  | 2026-10-09 |  |
| lower | `lower/Places.java:Places.Places` |  | 2026-10-15 |  |
| lower | `lower/Places.java:Places.Size.of` |  | 2026-10-15 |  |
| lower | `lower/Places.java:Places.Size.refused` |  | 2026-10-15 |  |
| lower | `lower/Places.java:Places.of` | R-020 | 2026-10-15 |  |
| lower | `lower/Places.java:Places.item` |  | 2026-10-15 |  |
| lower | `lower/Places.java:Places.counter` |  | 2026-10-15 |  |
| lower | `lower/Exprs.java:Exprs.counterDigits` | moved to Places, where every operand's places are stated (J-170) | 2026-10-15 | superseded-by: `lower/Places.java:Places.counter` |
| lower | `lower/FileVerbs.java:FileVerbs.channel` | R-019 | 2026-10-15 |  |
| front | `front/Mapped.java:Mapped.replaceAll` | REPLACING made one pass of IBM's comparison rules (J-173) | 2026-10-16 | superseded-by: `front/Mapped.java:Mapped.replacement` |
| front | `front/Mapped.java:Mapped.replacement` |  | 2026-10-16 |  |
| lower | `lower/Exprs.java:Exprs.arith` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.Funcs` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.name` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.numeric` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.floating` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.dbl` | R-023 | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.fixed` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.alphanumeric` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.point` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.camel` |  | 2026-10-16 |  |
| lower | `lower/Lower.java:Lower.compiled` |  | 2026-10-16 |  |
| lower | `lower/Places.java:Places.function` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.Floats` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.of` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.term` | R-023 | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.acos` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.asin` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.atan` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.cos` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.sin` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.tan` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.log` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.log10` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.sqrt` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.power` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.annuity` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.presentValue` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.mean` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.median` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.midrange` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.variance` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.standardDeviation` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.random` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Floats.java:Floats.numval` | R-023 | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.Functions` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.term` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.of` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.of#2` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.integer` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.integerPart` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.mod` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.rem` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.factorial` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.max` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.min` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.range` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.sum` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.ordMax` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.ordMin` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.ord` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.chr` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.length` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.upperCase` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.lowerCase` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.reverse` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.text` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.integerOfDate` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.integerOfDay` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.dateOfInteger` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.dayOfInteger` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.currentDate` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.operation` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.call` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.arguments` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.texts` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.textAt` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.maxText` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.minText` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.ordMaxText` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Functions.java:Functions.ordMinText` |  | 2026-10-16 |  |
| lower | `lower/Funcs.java:Funcs.expand` | R-019 | 2026-10-16 |  |
| lower | `lower/Flow.java:Flow.segmented` |  | 2026-10-16 |  |
| lower | `lower/Flow.java:Flow.segments` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.Dispatcher#2` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.enter` | R-021 | 2026-10-16 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.resume` |  | 2026-10-16 |  |
| lower | `lower/Verbs.java:Verbs.alteredOnly` |  | 2026-10-16 |  |
| front | `front/Mapped.java:Mapped.blank` |  | 2026-10-16 |  |
| front | `front/Preprocessor.java:Preprocessor.debuggingMode` | R-019 | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.Debugging` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.on` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.held` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.start` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.perform` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.goTo` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.end` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.entry` | R-021 | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.alter` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.register` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.procedure` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.declarative` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.inDeclaratives` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.header` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.range` |  | 2026-10-16 |  |
| lower | `lower/Debugging.java:Debugging.line` |  | 2026-10-16 |  |
| lower | `lower/Flow.java:Flow.end#2` |  | 2026-10-16 |  |
| lower | `lower/Flow.java:Flow.perform` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.Debug` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.on` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.start` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.perform` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.goTo` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.fell` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.entered` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.fill` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.altered` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.fill#2` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.put` |  | 2026-10-16 |  |
| sir | `sir/build/Build.java:Build.debuggingMode` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Debug.java:Debug.section` |  | 2026-10-16 |  |
| front | `front/Mapped.java:Mapped.debugging` |  | 2026-10-16 |  |
| front | `front/Mapped.java:Mapped.debugging#2` |  | 2026-10-16 |  |
| front | `front/Lines.java:Lines.marked` |  | 2026-10-16 |  |
| lower | `lower/Verbs.java:Verbs.stop` | R-019 | 2026-10-16 |  |
| sir | `sir/contract/Contract.java:Contract.stopLiteral` |  | 2026-10-16 |  |
| lower | `lower/Flow.java:Flow.paragraphIndex` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.relative` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.descriptor` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.key` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.phrase` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.invalidKey` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.start` | R-019 | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.delete` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.RelFile` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.status` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.successful` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.invalid` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.atEnd` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.failed` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.mode` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.hasStatus` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.abend` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.varying` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.current` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.bytes` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.path` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.open` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.load` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.noRewind` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.refused` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.readNext` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.read` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.take` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.write` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.rewrite` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.delete` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.target` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.start` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.close` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.close#2` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.number` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.digits` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Varying.java:Varying.took` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Varying.java:Varying.bytes` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.indexed` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.keyed` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.keys` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.keyItem` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.Entry.compareTo` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.IdxFile` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.keys` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.value` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.value#2` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.clear` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.hold` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.drop` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.load` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.save` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.opened` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.forget` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.readNext` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.read` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.take` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.key` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.write` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.clash` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.repeats` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.rewrite` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.delete` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.target` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/IdxFile.java:IdxFile.start` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.KeyedFile` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.status` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.successful` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.invalid` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.atEnd` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.failed` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.mode` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.hasStatus` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.abend` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.varying` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.current` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.bytes` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.take` | R-060 | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.path` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.open` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.noRewind` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.refused` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.close` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/KeyedFile.java:KeyedFile.close#2` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.clear` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.save` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.opened` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.forget` |  | 2026-10-16 |  |
| sir | `sir/build/Files.java:Files.shared` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RelFile.java:RelFile.status` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.status` |
| rt | `rt/katana/rt/RelFile.java:RelFile.successful` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.successful` |
| rt | `rt/katana/rt/RelFile.java:RelFile.invalid` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.invalid` |
| rt | `rt/katana/rt/RelFile.java:RelFile.atEnd` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.atEnd` |
| rt | `rt/katana/rt/RelFile.java:RelFile.failed` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.failed` |
| rt | `rt/katana/rt/RelFile.java:RelFile.mode` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.mode` |
| rt | `rt/katana/rt/RelFile.java:RelFile.hasStatus` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.hasStatus` |
| rt | `rt/katana/rt/RelFile.java:RelFile.abend` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.abend` |
| rt | `rt/katana/rt/RelFile.java:RelFile.varying` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.varying` |
| rt | `rt/katana/rt/RelFile.java:RelFile.current` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.current` |
| rt | `rt/katana/rt/RelFile.java:RelFile.bytes` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.bytes` |
| rt | `rt/katana/rt/RelFile.java:RelFile.path` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.path` |
| rt | `rt/katana/rt/RelFile.java:RelFile.open` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.open` |
| rt | `rt/katana/rt/RelFile.java:RelFile.noRewind` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.noRewind` |
| rt | `rt/katana/rt/RelFile.java:RelFile.refused` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.refused` |
| rt | `rt/katana/rt/RelFile.java:RelFile.close` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.close` |
| rt | `rt/katana/rt/RelFile.java:RelFile.close#2` | moved into the base RelFile and IdxFile share (J-182) | 2026-10-16 | superseded-by: `rt/katana/rt/KeyedFile.java:KeyedFile.close#2` |
| lower | `lower/Exprs.java:Exprs.returnCode` |  | 2026-10-16 |  |
| lower | `lower/Verbs.java:Verbs.call` | R-027 | 2026-10-16 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.Datasets#2` |  | 2026-10-16 |  |
| oracle | `oracle/lane/Datasets.java:Datasets.ksds` |  | 2026-10-16 |  |
| oracle | `oracle/lane/Start.java:Start.signs` | R-061 | 2026-10-16 |  |
| rt | `rt/katana/rt/Ksds.java:Ksds.Ksds` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Ksds.java:Ksds.main` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Ksds.java:Ksds.run` | R-025 | 2026-10-16 |  |
| rt | `rt/katana/rt/Ksds.java:Ksds.key` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Ksds.java:Ksds.load` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Ksds.java:Ksds.unload` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Now.java:Now.Now` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Now.java:Now.now` | R-009 | 2026-10-16 |  |
| lower | `lower/Lower.java:Lower.parm` | R-019 | 2026-10-16 |  |
| lower | `lower/Collating.java:Collating.named` | R-019 | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.SortVerbs` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.sd` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.descriptor` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.area` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.length` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.statement` | R-026 | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.files` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.target` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.read` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.write` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.release` |  | 2026-10-16 |  |
| lower | `lower/SortVerbs.java:SortVerbs.returnStatement` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.begin` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.release` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.weights` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.sort` | R-026 | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.compare` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.compareDecimal` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.current` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.rewind` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/SortFile.java:SortFile.returnNext` | R-060 | 2026-10-16 |  |
| lower | `lower/Verbs.java:Verbs.io` |  | 2026-10-16 |  |
| lower | `lower/CallVerbs.java:CallVerbs.CallVerbs` |  | 2026-10-16 |  |
| lower | `lower/CallVerbs.java:CallVerbs.call` | R-062 | 2026-10-16 |  |
| lower | `lower/CallVerbs.java:CallVerbs.cancel` | R-062 | 2026-10-16 |  |
| lower | `lower/CallVerbs.java:CallVerbs.arguments` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.externals` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.external` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.type` |  | 2026-10-16 |  |
| lower | `lower/FileVerbs.java:FileVerbs.made` |  | 2026-10-16 |  |
| lower | `lower/Lower.java:Lower.program` | R-024 | 2026-10-16 |  |
| lower | `lower/Lower.java:Lower.className#2` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.Programs` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.global` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.external` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.externals` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.name` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.entry` | R-019 | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.link` | R-019 | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.nested` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.visible` | R-062 | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.byName` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Dispatcher.java:Dispatcher.exited` | R-021 | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.RunUnit` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.call` | R-062 | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.found` | R-062 | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.cancel` | R-062 | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.name` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.external` | R-019 | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.first` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.externalFile` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.close` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.content` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.load` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.className` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/StopRun.java:StopRun.StopRun` |  | 2026-10-16 |  |
| sir | `sir/build/Globals.java:Globals.Globals` |  | 2026-10-16 |  |
| sir | `sir/build/Globals.java:Globals.copied` | R-062 | 2026-10-16 |  |
| sir | `sir/build/Globals.java:Globals.copy` |  | 2026-10-16 |  |
| sir | `sir/build/Names.java:Names.own` |  | 2026-10-16 |  |
| lower | `lower/Verbs.java:Verbs.call` | moved in phase 4.3 (J-186) | 2026-10-16 | superseded-by: `lower/CallVerbs.java:CallVerbs.call` |
| sir | `sir/types/Program.java:Program.globals` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.linkage` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.bind` | R-019 | 2026-10-16 |  |
| lower | `lower/Verbs.java:Verbs.address` |  | 2026-10-16 |  |
| lower | `lower/Verbs.java:Verbs.setAddress` | R-063 | 2026-10-16 |  |
| oracle | `oracle/lane/Steps.java:Steps.tiot` | R-063 | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.storage` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/RunUnit.java:RunUnit.storage#2` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.Storage` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.of` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.of#2` | R-063 | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.add` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.at` | R-063 | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.addressOf` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.pointer` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.set` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.word` |  | 2026-10-16 |  |
| rt | `rt/katana/rt/Storage.java:Storage.text` |  | 2026-10-16 |  |
| lower | `lower/Programs.java:Programs.link` | renamed in phase 4.4 (J-187) | 2026-10-16 | superseded-by: `lower/Programs.java:Programs.bind` |
| lower | `lower/Flow.java:Flow.trace` |  | 2026-10-17 |  |
| oracle | `oracle/lane/JavaPrograms.java:JavaPrograms.ran` |  | 2026-10-17 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.v1` |  | 2026-10-17 |  |
| oracle | `oracle/proof/ProofReport.java:ProofReport.coverage#2` |  | 2026-10-17 |  |
| rt | `rt/katana/rt/Trace.java:Trace.Trace` |  | 2026-10-17 |  |
| rt | `rt/katana/rt/Trace.java:Trace.section` |  | 2026-10-17 |  |
| rt | `rt/katana/rt/Trace.java:Trace.paragraph` |  | 2026-10-17 |  |
| rt | `rt/katana/rt/Trace.java:Trace.line` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.FileDecls` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.declare` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.externals` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.external` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.type` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.made` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.closer` | R-019 | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.keys` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.linages` | R-019 | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.varyings` |  | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.sizes` | R-019 | 2026-10-17 |  |
| lower | `lower/FileDecls.java:FileDecls.supplier` |  | 2026-10-17 |  |
| lower | `lower/Verbs.java:Verbs.moveCorresponding` |  | 2026-10-17 |  |
| oracle | `oracle/lane/JavaPrograms.java:JavaPrograms.traced` |  | 2026-10-17 |  |
| lower | `lower/FileVerbs.java:FileVerbs.declare` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.declare` |
| lower | `lower/FileVerbs.java:FileVerbs.externals` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.externals` |
| lower | `lower/FileVerbs.java:FileVerbs.external` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.external` |
| lower | `lower/FileVerbs.java:FileVerbs.type` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.type` |
| lower | `lower/FileVerbs.java:FileVerbs.made` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.made` |
| lower | `lower/FileVerbs.java:FileVerbs.closer` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.closer` |
| lower | `lower/FileVerbs.java:FileVerbs.keys` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.keys` |
| lower | `lower/FileVerbs.java:FileVerbs.linages` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.linages` |
| lower | `lower/FileVerbs.java:FileVerbs.varyings` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.varyings` |
| lower | `lower/FileVerbs.java:FileVerbs.sizes` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.sizes` |
| lower | `lower/FileVerbs.java:FileVerbs.supplier` | moved in tithe pass 4 (4.7) | 2026-10-17 | superseded-by: `lower/FileDecls.java:FileDecls.supplier` |
| intent | `intent/types/Evidence.java:Evidence.Evidence` |  | 2026-10-17 |  |
| intent | `intent/types/Fact.java:Fact.Fact` |  | 2026-10-17 |  |
| intent | `intent/types/Fact.java:Fact.id` |  | 2026-10-17 |  |
| intent | `intent/types/Fact.java:Fact.unconfirmed` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.Intent` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.actionable` | R-029 | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.against` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.subject` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.node` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.item` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.paragraph` |  | 2026-10-17 |  |
| intent | `intent/types/Invalid.java:Invalid.Invalid` |  | 2026-10-17 |  |
| intent | `intent/types/Kind.java:Kind.Kind` |  | 2026-10-17 |  |
| intent | `intent/types/Kind.java:Kind.shape` |  | 2026-10-17 |  |
| intent | `intent/types/Node.java:Node.ItemNode.ItemNode` |  | 2026-10-17 |  |
| intent | `intent/types/Node.java:Node.ParagraphNode.ParagraphNode` |  | 2026-10-17 |  |
| intent | `intent/types/Node.java:Node.StatementNode.StatementNode` |  | 2026-10-17 |  |
| intent | `intent/types/Node.java:Node.EdgeNode.EdgeNode` |  | 2026-10-17 |  |
| intent | `intent/types/State.java:State.State` |  | 2026-10-17 |  |
| intent | `intent/types/State.java:State.date` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Datum.Datum` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Datum.text` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Choice.Choice` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Choice.text` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Union.Union` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Union.text` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Range.Range` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Range.text` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Paragraphs.Paragraphs` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Paragraphs.text` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Whole.Whole` |  | 2026-10-17 |  |
| intent | `intent/types/Subject.java:Subject.Whole.text` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.conditions` |  | 2026-10-17 |  |
| intent | `intent/types/Intent.java:Intent.alternatives` |  | 2026-10-17 |  |
| intent | `intent/rules/EnumRule.java:EnumRule.name` |  | 2026-10-17 |  |
| intent | `intent/rules/EnumRule.java:EnumRule.version` |  | 2026-10-17 |  |
| intent | `intent/rules/EnumRule.java:EnumRule.confidence` |  | 2026-10-17 |  |
| intent | `intent/rules/EnumRule.java:EnumRule.infer` |  | 2026-10-17 |  |
| intent | `intent/rules/EnumRule.java:EnumRule.disjoint` |  | 2026-10-17 |  |
| intent | `intent/rules/EnumRule.java:EnumRule.key` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.name` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.version` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.confidence` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.Range.holds` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.Range.overlaps` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.infer` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.enteredOnlyByPerform` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.leavesAtItsEnd` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.recursive` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.mainLine` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.falls` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.ends` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.declarative` |  | 2026-10-17 |  |
| intent | `intent/rules/FunctionRule.java:FunctionRule.paragraph` |  | 2026-10-17 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.name` |  | 2026-10-17 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.version` |  | 2026-10-17 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.confidence` |  | 2026-10-17 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.infer` |  | 2026-10-17 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.stored` |  | 2026-10-17 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.squared` |  | 2026-10-17 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.squared#2` |  | 2026-10-17 |  |
| intent | `intent/rules/Rules.java:Rules.Rules` |  | 2026-10-17 |  |
| intent | `intent/rules/Rules.java:Rules.infer` |  | 2026-10-17 |  |
| intent | `intent/rules/Walk.java:Walk.Walk` |  | 2026-10-17 |  |
| intent | `intent/rules/Walk.java:Walk.statements` |  | 2026-10-17 |  |
| intent | `intent/rules/Walk.java:Walk.add` |  | 2026-10-17 |  |
| intent | `intent/rules/Walk.java:Walk.refers` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.write` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.confidence` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.read` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.problem` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.subject` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.join` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.subject#2` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.union` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.list` |  | 2026-10-17 |  |
| intent | `intent/review/Line.java:Line.nodes` |  | 2026-10-17 |  |
| intent | `intent/review/Names.java:Names.Names` |  | 2026-10-17 |  |
| intent | `intent/review/Names.java:Names.item` |  | 2026-10-17 |  |
| intent | `intent/review/Names.java:Names.item#2` |  | 2026-10-17 |  |
| intent | `intent/review/Names.java:Names.matches` |  | 2026-10-17 |  |
| intent | `intent/review/Names.java:Names.ancestors` |  | 2026-10-17 |  |
| intent | `intent/review/Names.java:Names.paragraph` |  | 2026-10-17 |  |
| intent | `intent/review/Names.java:Names.paragraph#2` |  | 2026-10-17 |  |
| intent | `intent/review/Review.java:Review.Review` |  | 2026-10-17 |  |
| intent | `intent/review/Review.java:Review.merge` |  | 2026-10-17 |  |
| intent | `intent/review/Review.java:Review.merge#2` | R-064 | 2026-10-17 |  |
| intent | `intent/review/Review.java:Review.kept` |  | 2026-10-17 |  |
| intent | `intent/review/Review.java:Review.withState` |  | 2026-10-17 |  |
| intent | `intent/review/Review.java:Review.read` |  | 2026-10-17 |  |
| intent | `intent/review/Review.java:Review.write` |  | 2026-10-17 |  |
| ledger | `ledger/Entry.java:Entry.Entry` |  | 2026-10-17 |  |
| ledger | `ledger/Entry.java:Entry.text` |  | 2026-10-17 |  |
| ledger | `ledger/Entry.java:Entry.read` |  | 2026-10-17 |  |
| ledger | `ledger/Entry.java:Entry.hash` |  | 2026-10-17 |  |
| ledger | `ledger/Invalid.java:Invalid.Invalid` |  | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.Ledger` | R-030 | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.read` |  | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.last` |  | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.text` |  | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.append` | R-030 | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.appendedTo` |  | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.live` |  | 2026-10-17 |  |
| ledger | `ledger/Ledger.java:Ledger.against` |  | 2026-10-17 |  |
| ledger | `ledger/LedgerReport.java:LedgerReport.LedgerReport` |  | 2026-10-17 |  |
| ledger | `ledger/LedgerReport.java:LedgerReport.write` |  | 2026-10-17 |  |
| ledger | `ledger/ProvenanceMap.java:ProvenanceMap.ProvenanceMap` |  | 2026-10-17 |  |
| ledger | `ledger/ProvenanceMap.java:ProvenanceMap.write` |  | 2026-10-17 |  |
| lower | `lower/Emitter.java:Emitter.enter` |  | 2026-10-17 |  |
| lower | `lower/Emitter.java:Emitter.leave` |  | 2026-10-17 |  |
| lower | `lower/Emitter.java:Emitter.symbol` |  | 2026-10-17 |  |
| lower | `lower/Emitter.java:Emitter.symbols` |  | 2026-10-17 |  |
| sir | `sir/contract/Contract.java:Contract.exitProgram` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.name` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.version` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.confidence` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.infer` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.flag` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.keys` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.nothingUsed` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.compared` |  | 2026-10-18 |  |
| intent | `intent/rules/FlagRule.java:FlagRule.nothing` |  | 2026-10-18 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.twoPlaces` |  | 2026-10-18 |  |
| intent | `intent/rules/MoneyRule.java:MoneyRule.edited` |  | 2026-10-18 |  |
| intent | `intent/review/Review.java:Review.sections` |  | 2026-10-18 |  |
| sir | `sir/contract/Contract.java:Contract.leaves` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.PerformTrace` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.entered` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.perform` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.returned` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.fell` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.exited` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.caller` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.program` |  | 2026-10-18 |  |
| rt | `rt/katana/rt/PerformTrace.java:PerformTrace.line` |  | 2026-10-18 |  |
| oracle | `oracle/nist/ModuleRun.java:ModuleRun.run#2` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.methods` | R-021 | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.method` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.call` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.falls` |  | 2026-10-18 |  |
| lower | `lower/Lower.java:Lower.Plan.Plan` |  | 2026-10-18 |  |
| lower | `lower/Lower.java:Lower.java#2` | R-024 | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.Structuring` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.plan` | R-032 | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.program` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.falls` | R-032 | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.toNext` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.name` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.Obligations.Obligations` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.Obligations.refusals` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.Obligations.edges` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.Obligations.in` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.Obligations.mainLine` |  | 2026-10-18 |  |
| passes | `passes/Structuring.java:Structuring.Obligations.recursive` |  | 2026-10-18 |  |
| passes | `passes/StructuringLedger.java:StructuringLedger.StructuringLedger` |  | 2026-10-18 |  |
| passes | `passes/StructuringLedger.java:StructuringLedger.lines` | R-030 | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.loop` |  | 2026-10-18 |  |
| lower | `lower/Lower.java:Lower.Plan.loop` |  | 2026-10-18 |  |
| lower | `lower/Lower.java:Lower.varying` |  | 2026-10-18 |  |
| lower | `lower/Performs.java:Performs.phrases` |  | 2026-10-18 |  |
| lower | `lower/Performs.java:Performs.stepped` |  | 2026-10-18 |  |
| lower | `lower/Performs.java:Performs.counted` | R-021 | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.Loops` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.loops` | R-032 | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.visit` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.refusals` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.unstructured` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.scope` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.flatten` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.stores` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.holds` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.target` |  | 2026-10-18 |  |
| passes | `passes/Loops.java:Loops.body` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.inlined` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.inline` | R-021 | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.targets` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.methods` | moved to Regions in 6.2b | 2026-10-18 | superseded-by: `lower/Regions.java:Regions.methods` |
| lower | `lower/Flow.java:Flow.inlined` | moved to Regions in 6.2b | 2026-10-18 | superseded-by: `lower/Regions.java:Regions.inlined` |
| lower | `lower/Flow.java:Flow.inline` | moved to Regions in 6.2b | 2026-10-18 | superseded-by: `lower/Regions.java:Regions.region` |
| lower | `lower/Flow.java:Flow.targets` | moved to Regions in 6.2b | 2026-10-18 | superseded-by: `lower/Regions.java:Regions.targets` |
| lower | `lower/Flow.java:Flow.leave` |  | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.Inline` |  | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.inline` | R-032 | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.refusals` |  | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.local` |  | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.entries` |  | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.run` |  | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.procedures` |  | 2026-10-18 |  |
| passes | `passes/Inline.java:Inline.dispatched` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.paragraphCode` |  | 2026-10-18 |  |
| lower | `lower/Flow.java:Flow.called` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.Regions` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.active` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.inlined` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.jump` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.methods` | R-021 | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.dispatch` | R-021 | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.region` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.level` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.targets` |  | 2026-10-18 |  |
| lower | `lower/Regions.java:Regions.gotos` |  | 2026-10-18 |  |
| passes | `passes/Cycles.java:Cycles.Cycles` |  | 2026-10-18 |  |
| passes | `passes/Cycles.java:Cycles.cycles` | R-033 | 2026-10-18 |  |
| passes | `passes/Cycles.java:Cycles.gotos` |  | 2026-10-18 |  |
| passes | `passes/Cycles.java:Cycles.name` |  | 2026-10-18 |  |
| passes | `passes/MainLine.java:MainLine.MainLine` |  | 2026-10-18 |  |
| passes | `passes/MainLine.java:MainLine.main` | R-032 | 2026-10-18 |  |
| passes | `passes/MainLine.java:MainLine.entries` |  | 2026-10-18 |  |
| lower | `lower/Exprs.java:Exprs.store` |  | 2026-10-19 |  |
| lower | `lower/Lower.java:Lower.Plan.structured` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.Promoted` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.has` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.name` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.name#2` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.item` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.big` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.declaration` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.attr` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.decimal` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.field` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.assign` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.receive` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.constant` | R-068 | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.store` | R-068 | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.Scalars` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.promote` | R-032 | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.item` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.aliases` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.own` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.named` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.fields` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.Uses.statement` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.Uses.varying` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.Uses.value` |  | 2026-10-19 |  |
| passes | `passes/StructuringLedger.java:StructuringLedger.pass` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.Scalars` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.of` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.of#2` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.longOf` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.bigOf` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.field` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.received` |  | 2026-10-19 |  |
| rt | `rt/katana/rt/Scalars.java:Scalars.store` | R-068 | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.group` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.fieldName` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.in` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.items` |  | 2026-10-19 |  |
| lower | `lower/Promoted.java:Promoted.through` | R-068 | 2026-10-19 |  |
| lower | `lower/Verbs.java:Verbs.verb` |  | 2026-10-19 |  |
| passes | `passes/Scalars.java:Scalars.Uses.through` |  | 2026-10-19 |  |
| lower | `lower/Arith.java:Arith.exact` | R-020 | 2026-10-21 |  |
| lower | `lower/Arith.java:Arith.integers` |  | 2026-10-21 |  |
| lower | `lower/Arith.java:Arith.whole` |  | 2026-10-21 |  |
| lower | `lower/Arith.java:Arith.big` |  | 2026-10-21 |  |
| lower | `lower/Forms.java:Forms.Forms` |  | 2026-10-21 |  |
| lower | `lower/Forms.java:Forms.of` |  | 2026-10-21 |  |
| lower | `lower/Forms.java:Forms.add` |  | 2026-10-21 |  |
| lower | `lower/Forms.java:Forms.subtract` |  | 2026-10-21 |  |
| lower | `lower/Forms.java:Forms.multiply` |  | 2026-10-21 |  |
| lower | `lower/Forms.java:Forms.sum` |  | 2026-10-21 |  |
| lower | `lower/Forms.java:Forms.fields` |  | 2026-10-21 |  |
| passes | `passes/Exact.java:Exact.Exact` |  | 2026-10-21 |  |
| passes | `passes/Exact.java:Exact.exact` | R-032 | 2026-10-21 |  |
| passes | `passes/Exact.java:Exact.statement` |  | 2026-10-21 |  |
| passes | `passes/Exact.java:Exact.places` |  | 2026-10-21 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.exact` | R-020 | 2026-10-21 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.exact#2` |  | 2026-10-21 |  |
| lower | `lower/Conditions.java:Conditions.test` |  | 2026-10-22 |  |
| lower | `lower/Promoted.java:Promoted.Promoted#2` |  | 2026-10-22 |  |
| lower | `lower/Promoted.java:Promoted.predicate` |  | 2026-10-22 |  |
| lower | `lower/Promoted.java:Promoted.constants` |  | 2026-10-22 |  |
| lower | `lower/Promoted.java:Promoted.predicates` | R-069 | 2026-10-22 |  |
| lower | `lower/Promoted.java:Promoted.setTrue` |  | 2026-10-22 |  |
| lower | `lower/Promoted.java:Promoted.money` |  | 2026-10-22 |  |
| passes | `passes/StructuringLedger.java:StructuringLedger.typed` |  | 2026-10-22 |  |
| passes | `passes/Types.java:Types.Types` |  | 2026-10-22 |  |
| passes | `passes/Types.java:Types.types` | R-069 | 2026-10-22 |  |
| passes | `passes/Types.java:Types.table` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Money.java:Money.zero` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Money.java:Money.of` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Money.java:Money.toString` |  | 2026-10-22 |  |
| lower | `lower/Arith.java:Arith.guards` |  | 2026-10-22 |  |
| lower | `lower/ArithVerbs.java:ArithVerbs.stepByStep` |  | 2026-10-22 |  |
| lower | `lower/Lower.java:Lower.guard` | R-069 | 2026-10-22 |  |
| passes | `passes/Exact.java:Exact.guardable` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Classes.java:Classes.exact` | R-069 | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.Dataflow` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.bytes` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.collect` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.receives` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.target` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.initializes` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.initialized` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.table` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.fields` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.varied` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.solve` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.meet` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.flow` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.perform` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.loops` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.definite` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.plainInitialize` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.divides` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.holds` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.reads` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.check` |  | 2026-10-22 |  |
| passes | `passes/Stateless.java:Stateless.Stateless` |  | 2026-10-22 |  |
| passes | `passes/Stateless.java:Stateless.refusals` | R-032 | 2026-10-22 |  |
| passes | `passes/Stateless.java:Stateless.unmodelled` |  | 2026-10-22 |  |
| passes | `passes/Stateless.java:Stateless.address` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.entering` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.writes` |  | 2026-10-22 |  |
| passes | `passes/Dataflow.java:Dataflow.transfer` |  | 2026-10-22 |  |
| lower | `lower/Arith.java:Arith.assign` | R-068 | 2026-10-22 |  |
| passes | `passes/Fits.java:Fits.Fits` |  | 2026-10-22 |  |
| passes | `passes/Fits.java:Fits.fits` | R-068 | 2026-10-22 |  |
| passes | `passes/Fits.java:Fits.statement` |  | 2026-10-22 |  |
| passes | `passes/Fits.java:Fits.refusals` |  | 2026-10-22 |  |
| passes | `passes/Fits.java:Fits.signed` |  | 2026-10-22 |  |
| lower | `lower/Arith.java:Arith.getter` |  | 2026-10-22 |  |
| lower | `lower/Promoted.java:Promoted.reserve` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.Views` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.javaLang` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.record` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.path` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.at` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.getter` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.big` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.typed` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.declare` | R-024 | 2026-10-22 |  |
| lower | `lower/Views.java:Views.accessors` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.create` |  | 2026-10-22 |  |
| passes | `passes/Records.java:Records.Records` |  | 2026-10-22 |  |
| passes | `passes/Records.java:Records.records` | R-032 | 2026-10-22 |  |
| passes | `passes/Records.java:Records.refusals` |  | 2026-10-22 |  |
| passes | `passes/Records.java:Records.refmod` |  | 2026-10-22 |  |
| passes | `passes/Records.java:Records.refmod#2` |  | 2026-10-22 |  |
| lower | `lower/Views.java:Views.bind` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.Result.same` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.Prove` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.run` | R-072 | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.outputs` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.dataset` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.copybook` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.read` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.lane` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.text` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Prove.java:Prove.main` |  | 2026-10-22 |  |
| findings | `findings/Divergence.java:Divergence.Divergence` |  | 2026-10-22 |  |
| findings | `findings/Divergence.java:Divergence.Kind.Kind` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.Generators` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.generate` | R-072 | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.write` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.program` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.copybook` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.id` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.digits` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.bytes` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Generators.java:Generators.sha256` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Boundary.java:Boundary.Boundary` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Boundary.java:Boundary.names` | R-072 | 2026-10-22 |  |
| oracle | `oracle/prove/Boundary.java:Boundary.statement` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Boundary.java:Boundary.fields` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Boundary.java:Boundary.targets` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Boundary.java:Boundary.perturb` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Events.java:Events.Events` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Events.java:Events.note` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Events.java:Events.stored` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.Line.text` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.Align` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.nodes` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.paragraph` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.item` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.count` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.qualifiedMatches` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.flat` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.infer` | R-072 | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.referenced` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.items` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.statement` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.literalsOf` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.literals` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.digits` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.byName` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.spellings` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.paragraphRanges` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.line` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.write` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Align.java:Align.read` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.Result.first` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.Attribute` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.attribute` | R-072 | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.run` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.unload` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.differing` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.cell` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.text` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Attribute.java:Attribute.text#2` |  | 2026-10-22 |  |
| rt | `rt/katana/rt/Decimal.java:Decimal.leading` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.Divergences` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.entries` | R-072 | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.line` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.proven` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.cause` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.storing` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.receiver` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.reads` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.fields` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.unexercised` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.divides` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.paragraph` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.aligned` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.origin` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.record` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.trace` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.fix` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Divergences.java:Divergences.leaf` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Report.java:Report.Report` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Report.java:Report.write` | R-072 | 2026-10-22 |  |
| oracle | `oracle/prove/Report.java:Report.entry` |  | 2026-10-22 |  |
| oracle | `oracle/prove/Report.java:Report.refusals` |  | 2026-10-22 |  |
| lower | `lower/Plan.java:Plan.Plan` |  | 2026-10-23 |  |
| lower | `lower/Plan.java:Plan.structured` |  | 2026-10-23 |  |
| lower | `lower/Plan.java:Plan.loop` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.Storage` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.declareItems` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.defineItems` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.area` | R-019 | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.recordLength` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.attr` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.index` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.stored` |  | 2026-10-23 |  |
| lower | `lower/Storage.java:Storage.edit` |  | 2026-10-23 |  |
| lower | `lower/Lower.java:Lower.declareItems` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.declareItems` |
| lower | `lower/Lower.java:Lower.defineItems` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.defineItems` |
| lower | `lower/Lower.java:Lower.recordLength` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.recordLength` |
| lower | `lower/Lower.java:Lower.attr` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.attr` |
| lower | `lower/Lower.java:Lower.index` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.index` |
| lower | `lower/Lower.java:Lower.stored` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.stored` |
| lower | `lower/Lower.java:Lower.edit` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.edit` |
| lower | `lower/Lower.java:Lower.area` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Storage.java:Storage.area` |
| lower | `lower/Lower.java:Lower.Plan.Plan` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Plan.java:Plan.Plan` |
| lower | `lower/Lower.java:Lower.Plan.loop` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Plan.java:Plan.loop` |
| lower | `lower/Lower.java:Lower.Plan.structured` | moved in the super tithe (J-241) | 2026-10-23 | superseded-by: `lower/Plan.java:Plan.structured` |
| lower | `lower/Exprs.java:Exprs.Exprs#2` |  | 2026-10-23 |  |
| lower | `lower/Promoted.java:Promoted.views` |  | 2026-10-23 |  |
| lower | `lower/Promoted.java:Promoted.intercept` |  | 2026-10-23 |  |
| passes | `passes/Passes.java:Passes.Passes` |  | 2026-10-23 |  |
| passes | `passes/Passes.java:Passes.plan` | R-032 | 2026-10-23 |  |
| passes | `passes/Structuring.java:Structuring.methods` | R-032 | 2026-10-23 |  |
| passes | `passes/Structuring.java:Structuring.plan` | moved in the super tithe (J-242) | 2026-10-23 | superseded-by: `passes/Passes.java:Passes.plan` |
| front | `front/Directives.java:Directives.Frame.Frame` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.Directives` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.apply` | R-081 | 2026-10-23 |  |
| front | `front/Directives.java:Directives.directive` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.condition` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.unquote` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.blank` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.debugging` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.opened` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.next` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.otherwise` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.closed` |  | 2026-10-23 |  |
| findings | `findings/Finding.java:Finding.Kind.compiledOut` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.branch` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.leave` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.where` |  | 2026-10-23 |  |
| front | `front/Lines.java:Lines.normalize#3` |  | 2026-10-23 |  |
| front | `front/Parse.java:Parse.file#2` |  | 2026-10-23 |  |
| front | `front/Parse.java:Parse.text#3` |  | 2026-10-23 |  |
| front | `front/Preprocessor.java:Preprocessor.run#3` |  | 2026-10-23 |  |
| front | `front/Settings.java:Settings.Settings` |  | 2026-10-23 |  |
| front | `front/Settings.java:Settings.declared` |  | 2026-10-23 |  |
| front | `front/Settings.java:Settings.isDeclared` |  | 2026-10-23 |  |
| oracle | `oracle/fixture/FixtureCheck.java:FixtureCheck.defines` |  | 2026-10-23 |  |
| oracle | `oracle/lane/Definitions.java:Definitions.Definitions` |  | 2026-10-23 |  |
| oracle | `oracle/lane/Definitions.java:Definitions.of` |  | 2026-10-23 |  |
| oracle | `oracle/lane/Definitions.java:Definitions.settings` | R-081 | 2026-10-23 |  |
| oracle | `oracle/lane/Definitions.java:Definitions.cobcArguments` |  | 2026-10-23 |  |
| sir | `sir/check/CompiledOut.java:CompiledOut.CompiledOut` |  | 2026-10-23 |  |
| sir | `sir/check/CompiledOut.java:CompiledOut.findings` | R-081 | 2026-10-23 |  |
| sir | `sir/check/CompiledOut.java:CompiledOut.finding` |  | 2026-10-23 |  |
| front | `front/Settings.java:Settings.Settings#2` |  | 2026-10-23 |  |
| front | `front/Settings.java:Settings.withDebugging` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.compileFlags` | R-045 | 2026-10-23 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.words` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.CompileFlags` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.map` | R-045 | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.noEffect` |  | 2026-10-23 |  |
| front | `front/Directives.java:Directives.valueless` | R-084 | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.reason` | R-083 | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.why` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.listing` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.grammarLeak` | R-089 | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.ibmsOwn` | R-088 | 2026-10-23 |  |
| front | `front/Lines.java:Lines.placed` |  | 2026-10-23 |  |
| lower | `lower/Exprs.java:Exprs.occurrences#2` |  | 2026-10-23 |  |
| lower | `lower/Exprs.java:Exprs.occurrences#3` |  | 2026-10-23 |  |
| lower | `lower/Slide.java:Slide.Slide` |  | 2026-10-23 |  |
| lower | `lower/Slide.java:Slide.tables` |  | 2026-10-23 |  |
| lower | `lower/Slide.java:Slide.shortfall` |  | 2026-10-23 |  |
| lower | `lower/Slide.java:Slide.before` |  | 2026-10-23 |  |
| lower | `lower/Slide.java:Slide.where` |  | 2026-10-23 |  |
| rt | `rt/katana/rt/Table.java:Table.slide` | R-088 | 2026-10-23 |  |
| lower | `lower/Exprs.java:Exprs.length` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.reasonKey` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/CompileFlags.java:CompileFlags.reasonOfKey` |  | 2026-10-23 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.finish` |  | 2026-10-24 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.written` |  | 2026-10-24 |  |
| oracle | `oracle/autotest/Autotests.java:Autotests.unquoteWhole` | R-045 | 2026-10-24 |  |
| lower | `lower/Exits.java:Exits.Exits` |  | 2026-10-24 |  |
| lower | `lower/Exits.java:Exits.plain` |  | 2026-10-24 |  |
| lower | `lower/Exits.java:Exits.stretch` |  | 2026-10-24 |  |
| lower | `lower/Exits.java:Exits.within` |  | 2026-10-24 |  |
| lower | `lower/Exits.java:Exits.exit` | R-090 | 2026-10-24 |  |
| lower | `lower/Exits.java:Exits.holds` |  | 2026-10-24 |  |
| lower | `lower/Performs.java:Performs.inline` |  | 2026-10-24 |  |
| lower | `lower/Performs.java:Performs.performed` |  | 2026-10-24 |  |
| front | `front/Lines.java:Lines.dollar` | R-090 | 2026-10-24 |  |
| lower | `lower/Verbs.java:Verbs.foreignName` | R-090 | 2026-10-24 |  |
| lower | `lower/FileDecls.java:FileDecls.foreignAssign` | R-090 | 2026-10-24 |  |
