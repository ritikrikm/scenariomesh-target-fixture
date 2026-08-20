#!/usr/bin/env python3
import pathlib
import re
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: set_workers.py <count>")
count = int(sys.argv[1])
if count < 1:
    raise SystemExit("worker count must be >= 1")
path = pathlib.Path("scenariomesh.yml")
text = path.read_text()
updated, replacements = re.subn(r"(?m)^(\s*count:\s*)\d+\s*$", rf"\g<1>{count}", text, count=1)
if replacements != 1:
    raise SystemExit("could not locate scenariomesh.workers.count")
path.write_text(updated)
print(f"Configured ScenarioMesh workers: {count}")
