#!/bin/sh
gitroot=$(git rev-parse --show-toplevel)

java -jar $gitroot/skeleton/dist/Compiler.jar "$@"
