#!/usr/bin/env python3
import collections, pathlib, re, sys
n=int(sys.argv[1]); d=pathlib.Path('target/independent-executions')
files=list(d.glob('*.done')) if d.exists() else []
ids=[re.match(r'^(case-\d{5})__',p.name).group(1) for p in files if re.match(r'^(case-\d{5})__',p.name)]
c=collections.Counter(ids); expected={f'case-{i:05d}' for i in range(1,n+1)}
missing=expected-set(ids); dup={k:v for k,v in c.items() if v!=1}
print(f'expected={n} executed={len(ids)} unique={len(set(ids))} missing={len(missing)} duplicates={len(dup)}')
if len(ids)!=n or missing or dup: raise SystemExit(1)
