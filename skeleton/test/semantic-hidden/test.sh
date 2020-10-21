#!/bin/bash

runsemantic() {
  $(git rev-parse --show-toplevel)/skeleton/run.sh -target inter $1
}

fail=0

for file in `dirname $0`/illegal/*; do
  if runsemantic > /dev/null 2>&1 $file; then
    echo "Illegal file $file passed semantic check.";
    fail=1
  fi
done

for file in `dirname $0`/legal/*; do
  if ! runsemantic $file; then
    echo "Legal file $file failed semantic check.";
    fail=1
  fi
done

exit $fail;
