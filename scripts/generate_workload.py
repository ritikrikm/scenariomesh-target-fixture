#!/usr/bin/env python3
import pathlib, sys
count=int(sys.argv[1])
root=pathlib.Path('src/test/resources/features')
for p in root.rglob('*.feature'): p.unlink()
(root/'accounts').mkdir(parents=True, exist_ok=True)
(root/'payments').mkdir(parents=True, exist_ok=True)
parts=[('accounts','account_search',count//2),('payments','payment_review',count-count//2)]
start=1
for folder,name,n in parts:
    lines=[f'@regression @{folder}',f'Feature: {name}', '', f'  Scenario Outline: {name} <id>', '    Given an account workflow "<id>"', '    When the workflow is submitted', '    Then workflow "<id>" completes', '', '    Examples:', '      | id |']
    for i in range(start,start+n): lines.append(f'      | case-{i:05d} |')
    (root/folder/f'{name}.feature').write_text('\n'.join(lines)+'\n')
    start += n
print(count)
