# test/: the checks

- `test/*.java`: the doc and rule tests, each holding a document or a rule to the code (`DocTests` runs them in
  the fast loop). A failing one says what to do.
- `test/katana/<module>/`: each module's tests, in its package. `*IntegrationTest` needs cobc and the corpus
  (`gradle integration`); the rest is the fast loop.
- `test/katana/support/`: helpers for tests: `Small` (a small program from its storage and procedure) and
  `Javac` (lowered Java compiled, failing with javac's diagnostics).
- `test/katana/oracle/lane/CardDemoLane.java`: the CardDemo lanes' support.
