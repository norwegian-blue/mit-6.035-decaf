#!/bin/sh
gitroot=$(git rev-parse --show-toplevel)
if [ -n "$JAVA_TOOL_OPTIONS" ]; then
	java_too_options="$JAVA_TOOL_OPTIONS"
	unset JAVA_TOOL_OPTIONS
fi
java $java_tool_options -jar $gitroot/skeleton/dist/Compiler.jar "$@"
