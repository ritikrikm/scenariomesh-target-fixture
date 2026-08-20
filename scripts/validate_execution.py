#!/usr/bin/env python3
import collections
import pathlib
import re
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: validate_execution.py <expected-count>")

expected_count = int(sys.argv[1])
if expected_count < 1:
    raise SystemExit("expected-count must be >= 1")

execution_dir = pathlib.Path("target/fixture-executions")
if not execution_dir.exists():
    raise SystemExit(f"execution directory not found: {execution_dir}")

pattern = re.compile(r"^(scenario-\d{5})__.+\.done$")
observed = []
unexpected_files = []
for path in execution_dir.glob("*.done"):
    match = pattern.match(path.name)
    if match:
        observed.append(match.group(1))
    else:
        unexpected_files.append(path.name)

expected = [f"scenario-{i:05d}" for i in range(1, expected_count + 1)]
expected_set = set(expected)
counts = collections.Counter(observed)
observed_set = set(observed)
missing = sorted(expected_set - observed_set)
unexpected_ids = sorted(observed_set - expected_set)
duplicates = sorted((scenario_id, count) for scenario_id, count in counts.items() if count != 1)

print("ScenarioMesh target exact-once validation")
print(f"expected       : {expected_count}")
print(f"executions     : {len(observed)}")
print(f"unique ids     : {len(observed_set)}")
print(f"missing        : {len(missing)}")
print(f"unexpected ids : {len(unexpected_ids)}")
print(f"duplicates     : {len(duplicates)}")
print(f"bad markers    : {len(unexpected_files)}")

errors = []
if len(observed) != expected_count:
    errors.append(f"execution count {len(observed)} != expected {expected_count}")
if missing:
    errors.append("missing: " + ", ".join(missing[:20]))
if unexpected_ids:
    errors.append("unexpected ids: " + ", ".join(unexpected_ids[:20]))
if duplicates:
    errors.append("duplicates: " + ", ".join(f"{sid}={count}" for sid, count in duplicates[:20]))
if unexpected_files:
    errors.append("unexpected marker names: " + ", ".join(unexpected_files[:20]))

if errors:
    print("VALIDATION: FAIL")
    for error in errors:
        print(" - " + error)
    raise SystemExit(1)

print("VALIDATION: PASS")
