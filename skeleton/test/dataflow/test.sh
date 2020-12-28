#!/bin/bash

runassembler() {
  $(git rev-parse --show-toplevel)/skeleton/run.sh -target lowir -opt cse cp dce -o $2 -debug $1
}

fail=0

for file in `dirname $0`/input/*.dcf; do
  asm='tmp.s'
  bin='tmp'
  lir='tmp.lir'

  echo "running " $file;
  # compile to assembly
  if runassembler $file $asm 2>&1 > $lir; then
    
    # assemble code and run
    if gcc -no-pie -o $bin $asm 2>&1 > /dev/null; then
      output='out';
      ./$bin > $output;
      exitcode=$?

      # check return value for error
      if [ -f "$(dirname $0)"/error/"$(basename $file .dcf)".err ]; then
	errfile="$(dirname $0)"/error/"$(basename $file .dcf)".err;
        val=$(<$errfile)
	if [ $val != $exitcode ]; then
	  echo "Program $file is expected to return runtime error $val";
          fail=1;
        fi
      else 
        if [ $exitcode != 0 ]; then
          echo "Program $file returns an unexpected runtime error";
          fail=1;
	fi
      fi

      # check program output 
      if  ! diff $output "$(dirname $0)"/output/"$(basename $file .dcf)".out > /dev/null; then
        echo "Program $file output does not match expected result"
	fail=1;
      fi

      # check intermediate code output
      if  ! diff $lir "$(dirname $0)"/output_lir/"$(basename $file .dcf)".lir> /dev/null; then
	echo "Program $file intermediate representation output does not mach expected results"
	fail=1;
      fi

    else 
      echo "Program $file failed to assemble";
      fail=1;
    fi

  else 
    echo "Program $file failed to compile";
    fail=1;    
  fi

  rm -f $asm $bin $output $lir;

done

exit $fail;
