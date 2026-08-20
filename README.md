# Independent Enterprise UI Regression Fixture

This branch is intentionally designed without ScenarioMesh integration in source. It represents an ordinary Java 17 + Maven + Cucumber + Selenium regression repository with hooks, page objects, multiple feature folders, tags, properties, and scenario outlines.

Native contract: `mvn test` must work before any ScenarioMesh activation is introduced.

The CI compatibility test builds ScenarioMesh separately, injects only `.mvn/extensions.xml` and `scenariomesh.yml` at runtime, and runs the same `mvn test` command again.

`test/small-scale` uses 100 scenarios. `test/medium-scale` uses 500 scenarios.
