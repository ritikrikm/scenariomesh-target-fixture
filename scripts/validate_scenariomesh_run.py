#!/usr/bin/env python3
from __future__ import annotations

import re
import sys
from pathlib import Path


def fail(message: str) -> None:
    print(f"VALIDATION: FAIL - {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    if len(sys.argv) != 3:
        fail("usage: validate_scenariomesh_run.py <maven-log> <expected-scenarios>")

    log_path = Path(sys.argv[1])
    expected = int(sys.argv[2])
    if expected < 1:
        fail("expected scenario count must be positive")
    if not log_path.is_file():
        fail(f"log file not found: {log_path}")

    text = log_path.read_text(encoding="utf-8", errors="replace")

    takeover_markers = (
        "ScenarioMesh: takeover enabled after runtime ownership preflight.",
        "ScenarioMesh: takeover enabled for scenariomesh-target-fixture",
    )
    if not any(marker in text for marker in takeover_markers):
        fail("ScenarioMesh takeover log was not found")

    pass_through_markers = (
        "ScenarioMesh: pass-through for scenariomesh-target-fixture",
        "runtime preflight selected native Maven pass-through",
    )
    if any(marker in text for marker in pass_through_markers):
        fail("target unexpectedly entered ScenarioMesh pass-through mode")

    adapter_matches = re.findall(r"ScenarioMesh selected adapter:\s*([^\r\n]+)", text)
    if not adapter_matches:
        fail("selected adapter log was not found")
    if not any("junit-platform" in value for value in adapter_matches):
        fail(f"expected junit-platform adapter, found: {adapter_matches}")

    result_matches = re.findall(
        r"ScenarioMesh results:\s*discovered=(\d+),\s*passed=(\d+),\s*skipped=(\d+),\s*failed=(\d+)",
        text,
    )
    if not result_matches:
        fail("ScenarioMesh results summary was not found")

    discovered, passed, skipped, failed = map(int, result_matches[-1])
    print(f"discovered : {discovered}")
    print(f"passed     : {passed}")
    print(f"skipped    : {skipped}")
    print(f"failed     : {failed}")

    if discovered != expected:
        fail(f"expected {expected} discovered scenarios but got {discovered}")
    if passed != expected:
        fail(f"expected {expected} passed scenarios but got {passed}")
    if skipped != 0:
        fail(f"expected 0 skipped scenarios but got {skipped}")
    if failed != 0:
        fail(f"expected 0 failed scenarios but got {failed}")

    report = Path("target/scenariomesh/report.html")
    summary = Path("target/scenariomesh/summary.json")
    if not report.is_file():
        fail(f"ScenarioMesh HTML report not found: {report}")
    if not summary.is_file():
        fail(f"ScenarioMesh summary not found: {summary}")

    print("VALIDATION: PASS - ScenarioMesh owned and completed the expected run")


if __name__ == "__main__":
    main()
