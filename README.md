# ScenarioMesh Target Fixture

This repository is a controlled external consumer used to verify ScenarioMesh against a real Maven + Cucumber + Selenium project.

The target project intentionally stays small in source code while scaling the number of executable Cucumber examples dynamically. This lets CI exercise ScenarioMesh discovery, Maven takeover, isolated workers, scheduling, reporting, exact-once execution, and worker recovery at sizes from a handful of scenarios to 10,000.

## Default ScenarioMesh source

GitHub Actions builds ScenarioMesh from:

```text
ritikrikm/ScenarioMesh
agent/worker-hardening-test
```

The ref can be overridden for manual workflow runs so feature branches or exact SHAs can be tested before merge.

## Consumer experience under test

ScenarioMesh is activated through `.mvn/extensions.xml`, while the target repository continues to use its normal command:

```bash
mvn test
```

CI requires the Maven log to contain the ScenarioMesh takeover message. A green Maven build without takeover is therefore not considered a ScenarioMesh E2E success.

## Fixture workload

The committed feature contains four Scenario Outline rows. CI can regenerate it to any positive scenario count:

```bash
python3 scripts/generate_scenarios.py 100
python3 scripts/generate_scenarios.py 1000
python3 scripts/generate_scenarios.py 10000
```

For small compatibility runs, each scenario launches headless Chrome and interacts with an isolated in-memory page. Large scale runs disable the browser workload so they stress ScenarioMesh discovery, scheduling, IPC, worker lifecycle, and reporting rather than Chrome startup cost.

## Exact-once validation

A completed scenario writes a unique marker under `target/fixture-executions/`. Validation fails when a scenario is missing, duplicated, unexpected, or produces an invalid marker.

```bash
python3 scripts/validate_execution.py 100
```

## Worker crash recovery

The E2E workflow can intentionally terminate one worker JVM while `scenario-00007` is running. A filesystem sentinel ensures only the first attempt crashes. ScenarioMesh is expected to detect the worker loss, requeue the unfinished scenario, start a replacement worker, and complete all scenarios exactly once.

## Workflows

- `ScenarioMesh External Target E2E`: baseline Maven, ScenarioMesh smoke matrix, real Selenium runs, 100-scenario run, crash/requeue validation, and configurable manual scale.
- `ScenarioMesh Scale Suite`: manually runs 1,000 / 5,000 / 10,000 scenario jobs with configurable ScenarioMesh ref and worker count.
