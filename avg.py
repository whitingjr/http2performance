#!/usr/bin/env python3

import sys

total = 0
count = 0

for line in sys.stdin:
		total += float(line)
		count += 1
		
print(total/count)
