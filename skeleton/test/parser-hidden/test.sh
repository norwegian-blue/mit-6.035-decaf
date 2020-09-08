#!/bin/bash

runparser() {
  $(git rev-parse --show-toplevel)/skeleton/run.sh -target parse $1
}

fail=0

for file in `dirname $0`/illegal/*; do
  runparser > tmp 2>&1 $file
  if ! [ -s tmp ]; then
    echo "Illegal file $file parsed successfully.";
    fail=1
  fi
  rm tmp
done

for file in `dirname $0`/legal/*; do
  runparser 2>&1 $file | tee -a tmp
  if [ -s tmp ]; then
    echo "Legal file $file failed to parse.";
    fail=1
  fi
  rm tmp
done

exit $fail;
