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

JAR_FILE=${DIR}/web/build/libs/web.jar
CONFIG_FILE=${DIR}/config.yaml
DEPENDENCIES=${DIR}/web/build/dependencies
CONTENT_DIR=${DIR}/build/docker/

${DIR}/gradlew clean build
status=$?
if [ $status -ne 0 ]; then
  echo "mvn build failed with status $status" >&2
  exit
fi

mkdir -p $CONTENT_DIR
cp $JAR_FILE $CONTENT_DIR
cp $CONFIG_FILE $CONTENT_DIR
cp -r $DEPENDENCIES $CONTENT_DIR


case $CPU_ARCHITECTURE in
x86_64)
  docker build -t richodemus/reader:latest -f ${DIR}/Dockerfile ${DIR}/
  ;;
armv7l)
  docker build -t richodemus/reader:latest -f ${DIR}/Dockerfile_arm ${DIR}/
  ;;
esac

rm $CONTENT_DIR/dependencies/*
rmdir $CONTENT_DIR/dependencies
rm $CONTENT_DIR/*
rmdir $CONTENT_DIR
echo "Done!"

