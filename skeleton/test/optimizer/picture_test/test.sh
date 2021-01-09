#!/bin/bash

runassembler() {
  $(git rev-parse --show-toplevel)/skeleton/run.sh -target codegen -opt $3 -o $2 -debug $1
}

testFun() {
  # compile
  if runassembler $1 $2 $4 2>&1 > /dev/null; then
    # assemble 
    if gcc -pthread -no-pie -o $bin $asm ../lib/lib6035.a 2>&1 > /dev/null; then
      ./$bin
      exitcode=$?
      if [ $exitcode != 0 ]; then
	echo "!!! Failed to run !!!"
	exit
      fi
    else 
      echo "!!! Failed to assemble !!!"
      exit
    fi
  else 
      echo "!!! Failed to compile !!!"
  fi
}

rm *.pgm
git checkout saman.pgm
git checkout saman_noise.pgm
git checkout segovia.pgm

for file in `dirname $0`/*.dcf; do
  asm='tmp.s'
  bin='tmp'


  echo "____________________________"
  echo "testing:   " $file;
  echo ""
  echo "no optimization"
  testFun $file $asm $bin ""
  echo ""
  echo "dataflow optimization"
  testFun $file $asm $bin "cse cp dce"
  echo ""
  echo "register allocation"
  testFun $file $asm $bin "regalloc"
  echo ""
  echo "all optimizations"
  testFun $file $asm $bin "all"

done

rm -f $asm $bin $output;
echo "____________________________"
echo ""
exit
