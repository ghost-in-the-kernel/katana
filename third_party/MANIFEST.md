# Third-party code, vendored so a clean clone builds with the network off (R-001)

Each jar is the Maven Central artifact named, checked against Central's SHA-1 when it was fetched
(2026-10-04) and recorded here by SHA-256. Its license text is beside it. "Ships" says whether the
jar reaches a customer: only what `rt/` links against ever could, and `rt/` links against nothing
(docs/modules.json MOD-02). GPL code never enters this folder (R-002).

| Jar | License | Used by | Ships | SHA-256 |
| --- | --- | --- | --- | --- |
| `antlr/antlr4-runtime-4.13.2.jar` | BSD-3-Clause (`antlr/LICENSE.txt`) | front/ (phase 2) | no | `dd3e8a13a2d669bf84fb8d834de35ce4875f27157698d206241ec8488aadcaf7` |
| `gson/gson-2.11.0.jar` | Apache-2.0 (`gson/LICENSE`) | the JSON the tool reads and writes | no | `57928d6e5a6edeb2abd3770a8f95ba44dce45f3b23b7a9dc2b309c581552a78b` |
| `junit/junit-jupiter-api-5.11.4.jar` | EPL-2.0 (`junit/LICENSE-junit.md`, `junit/NOTICE-junit.md`) | tests only | no | `ab83ef9e51ac4597d59d26b4b58812129550e2f579a404c8af7d09f5ce5b4293` |
| `junit/junit-jupiter-engine-5.11.4.jar` | EPL-2.0 (`junit/LICENSE-junit.md`, `junit/NOTICE-junit.md`) | tests only | no | `cdf8ac59f3fad774ca738ad03890950eeb91833ef0e8908753177edd26f1581c` |
| `junit/junit-platform-commons-1.11.4.jar` | EPL-2.0 (`junit/LICENSE-junit.md`, `junit/NOTICE-junit.md`) | tests only | no | `9edd969b0d0670c54105bc91ae79bd1c6f503e12115faba82073b84c86bbc334` |
| `junit/junit-platform-engine-1.11.4.jar` | EPL-2.0 (`junit/LICENSE-junit.md`, `junit/NOTICE-junit.md`) | tests only | no | `b1dd998f64f9acadc15966d9cd3d08074662677b3e390f0a38fcbf0bb4c72330` |
| `junit/junit-platform-launcher-1.11.4.jar` | EPL-2.0 (`junit/LICENSE-junit.md`, `junit/NOTICE-junit.md`) | tests only | no | `d7430bd029e7fcced53ee445e4d2d1a8a1e043ea4c4df43b6335a857f79761ae` |
| `junit/opentest4j-1.3.0.jar` | Apache-2.0 (`junit/LICENSE-opentest4j`) | tests only | no | `48e2df636cab6563ced64dcdff8abb2355627cb236ef0bf37598682ddf742f1b` |
| `junit/apiguardian-api-1.1.2.jar` | Apache-2.0 (`junit/LICENSE-apiguardian`) | tests only | no | `b509448ac506d607319f182537f0b35d71007582ec741832a1f111e5b5b70b38` |

JUnit 5 is EPL-2.0, the license R-015 holds for counsel before che4z's grammars enter the compiler.
JUnit is a test dependency: it is never compiled into the tool or the runtime and never ships. Whether
that needs counsel too is Ghost's question, recorded in docs/HANDOFF.md.
