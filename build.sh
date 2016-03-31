#!/bin/bash

#from http://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

CPU_ARCHITECTURE="$( uname -m )"

case $CPU_ARCHITECTURE in
x86_64)
  echo "you have a 64bit processor"
  ;;
armv7l)
  echo "you have an arm processor"
  ;;
*)
  echo "Unsupported architecture: " $CPU_ARCHITECTURE
  exit 1
esac

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


case $CPU_ARCHITECTURE in
x86_64)
  docker build -t richodemus/reader:latest -f ${DIR}/docker/Dockerfile ${DIR}/docker/
  ;;
armv7l)
  docker build -t richodemus/reader:latest -f ${DIR}/docker/Dockerfile_arm ${DIR}/docker/
  ;;
esac

rm $CONTENT_DIR/*
rmdir $CONTENT_DIR
echo "Done!"

