#!/bin/bash

#from http://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

${DIR}/gradlew componentTest
status=$?
if [ $status -ne 0 ]; then
  echo "gradle componentTest failed with status $status" >&2
  exit
fi