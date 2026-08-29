# ScenarioMesh Target Fixture

This repository is a controlled external consumer used to verify ScenarioMesh against a real Maven + Cucumber + Selenium project.

The target project intentionally stays small in source code while scaling the number of executable Cucumber examples dynamically. This lets CI exercise ScenarioMesh discovery, Maven takeover, isolated workers, scheduling, reporting, exact-once execution, worker recovery, and browser-driver behavior at sizes from a handful of scenarios to 10,000.

## Fixture test matrix

The `main` branch is the single source of truth. Its workflows exercise several real-world shapes instead of maintaining divergent long-lived fixture branches: low-count browser smoke, browserless scale, exact-once accounting, worker recovery, controlled takeover, and hostile compatibility cases. Workflow inputs vary scenario count, browser mode, worker count, and failure behavior.

## Default ScenarioMesh source

GitHub Actions builds ScenarioMesh from:

```text
ritikrikm/ScenarioMesh
main
```

The ref can be overridden for manual workflow runs so feature branches or exact SHAs can be tested before merge.

## Consumer experience under test

ScenarioMesh is activated through `.mvn/extensions.xml`, while the target repository continues to use its normal command:

```bash
mvn test
```

The target POM deliberately stays inside ScenarioMesh's documented takeover-safe Surefire subset. ScenarioMesh-specific runtime settings live in `scenariomesh.yml` or Maven user properties rather than unsupported Surefire configuration.

CI requires all of the following for a ScenarioMesh-owned lane:

- the Maven lifecycle takeover message is present;
- the selected adapter is `junit-platform`;
- the discovered scenario count equals the requested count;
- every expected scenario passes with zero skipped/failed terminal results;
- every scenario ID produces exactly one completion marker;
- no unexpected or duplicate scenario ID is observed;
- ScenarioMesh `report.html` and `summary.json` are produced.

A green Maven build without those conditions is not considered an E2E success.

## Cucumber discovery model

ScenarioMesh's JUnit Platform adapter discovers Cucumber directly from classpath roots, matching the supported ScenarioMesh example. The repository therefore does **not** keep a permanent `@Suite` runner, because doing so could expose the same Cucumber scenarios through both the Suite and Cucumber engines.

The native Maven baseline job temporarily creates a JUnit Platform suite runner only inside that isolated baseline workspace. ScenarioMesh jobs start from a clean checkout without the temporary runner.

## Fixture workload

The committed feature contains four Scenario Outline rows. CI can regenerate it to any positive scenario count:

```bash
python3 scripts/generate_scenarios.py 100
python3 scripts/generate_scenarios.py 1000
python3 scripts/generate_scenarios.py 10000
```

For small compatibility runs, each scenario launches headless Chrome and interacts with an isolated Base64 in-memory HTML page. Large scale runs disable the browser workload so they stress ScenarioMesh discovery, scheduling, IPC, worker lifecycle, and reporting rather than Chrome startup cost.

### Browser driver modes

The fixture supports multiple browser-driver shapes through `fixture.browser.mode`:

- `chrome-headless` - local headless Chrome, the default CI path
- `chrome-headed` - local visible Chrome for interactive debugging
- `remote` - Selenium RemoteWebDriver, useful for Selenium Grid or hosted browser lanes
- `none` - browserless execution for scale and contract lanes

`fixture.browser.enabled=false` is still accepted as a backward-compatible alias for `fixture.browser.mode=none`.

The core worker test intentionally avoids a shared Cucumber JSON output file because several isolated JVMs writing the same file would test Cucumber reporter contention instead of ScenarioMesh execution correctness. ScenarioMesh's own reports are the authoritative aggregate artifacts for these lanes.

## Exact-once validation

A completed scenario writes a unique marker under `target/fixture-executions/`. Validation fails when a scenario is missing, duplicated, unexpected, or produces an invalid marker.

```bash
python3 scripts/validate_execution.py 100
```

When `target/scenariomesh-maven.log` exists, that validator also invokes `validate_scenariomesh_run.py`, so the same command validates takeover, adapter selection, discovered/pass counts, and ScenarioMesh report publication.

## Worker crash recovery

The E2E workflow intentionally terminates one worker JVM before the first scenario (`scenario-00001`) completes. A filesystem sentinel ensures only the first attempt crashes. With `execution.infrastructureRetries: 1`, ScenarioMesh is expected to detect the worker loss, requeue the unfinished work, start a replacement worker, and finish with exactly one completion marker per scenario.

This lane proves recovery from worker loss before observable scenario completion. It does not claim transactional exactly-once side effects after an arbitrary mid-container crash: Cucumber scenario outlines can be dispatched as one container, and infrastructure retry is consequently at-least-once for side effects already produced inside that container. Applications that require such guarantees must make external side effects idempotent or transactional.

## Workflows

- `ScenarioMesh External Target E2E`: native Maven baseline, ScenarioMesh smoke matrix, real Selenium runs, 100-scenario run, crash/requeue validation, and configurable manual scale.
- `ScenarioMesh Scale Suite`: manually runs 1,000 / 5,000 / 10,000 scenario jobs with configurable ScenarioMesh ref and worker count.

## Manual ScenarioMesh run

Build the ScenarioMesh branch into your local Maven repository first, then from this repository run:

```bash
python3 scripts/generate_scenarios.py 100
python3 scripts/set_workers.py 4
rm -rf target/fixture-executions target/scenariomesh
mvn test -Dfixture.browser.enabled=false | tee target/scenariomesh-maven.log
python3 scripts/validate_execution.py 100
```

For a real Selenium smoke run, use `-Dfixture.browser.enabled=true` and ensure Chrome/Chromium is available on the machine.
