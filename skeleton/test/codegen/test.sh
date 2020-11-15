#!/bin/bash

runsemantic() {
  $(git rev-parse --show-toplevel)/skeleton/run.sh -target inter $1
}

fail=0

for file in `dirname $0`/illegal/*; do
  if runsemantic > /dev/null 2>tmp_err $file; then
    echo "Illegal file $file passed semantic check.";
    fail=1
  else
    grep "ERROR.*" tmp_err -o > tmp_errAct;
    grep "ERROR.*" $file -o > tmp_errTgt;
    if ! diff tmp_errAct tmp_errTgt > /dev/null; then
      echo "Detected semantic error in $file does not match expectation.";
    fi
  fi
  rm tmp_*;
done

for file in `dirname $0`/legal/*; do
  if ! runsemantic $file; then
    echo "Legal file $file failed semantic check.";
    fail=1
  fi
done

exit $fail;
