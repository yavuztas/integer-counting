#!/usr/bin/env bash
# Usage: ./generate.sh <count>

count=$1

# Safety check
if [[ -z "$count" || "$count" -le 0 ]]; then
  echo "Usage: $0 <count>"
  exit 1
fi

# Generate numbers in [0-999] efficiently
awk -v n="$count" 'BEGIN {
    srand();
    for (i = 0; i < n; i++) {
        print int(rand() * 1000);
    }
}'
