#!/usr/bin/env python3
import argparse
from pathlib import Path

HEADER = '''Feature: ScenarioMesh isolated worker fixture
  A deterministic Selenium workload used to verify scenario discovery, execution, and isolation.

  Scenario Outline: Execute isolated fixture scenario <id>
    Given fixture scenario "<id>" opens an isolated page
    When the scenario writes its unique id
    Then only id "<id>" is visible

    Examples:
      | id             |
'''

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("count", type=int)
    parser.add_argument("--output", default="src/test/resources/features/scenario_mesh_fixture.feature")
    args = parser.parse_args()
    if args.count < 1:
        raise SystemExit("count must be >= 1")

    rows = ''.join(f"      | scenario-{i:05d} |\n" for i in range(1, args.count + 1))
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(HEADER + rows, encoding="utf-8")
    print(f"Generated {args.count} executable Cucumber examples in {output}")

if __name__ == "__main__":
    main()
