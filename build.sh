#!/bin/bash

#from http://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

JAR_FILE=${DIR}/web/target/web-1.0-SNAPSHOT.jar
CONFIG_FILE=${DIR}/config.yaml
CONTENT_DIR=${DIR}/docker/content/

mvn clean install -T1C
status=$?
if [ $status -ne 0 ]; then
  echo "mvn build failed with status $status" >&2
  exit
fi

mkdir -p $CONTENT_DIR
cp $JAR_FILE $CONTENT_DIR
cp $CONFIG_FILE $CONTENT_DIR

docker build -t rpi-reader ${DIR}/docker/

rm $CONTENT_DIR/*
rmdir $CONTENT_DIR
echo "Done!"

