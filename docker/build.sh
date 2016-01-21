#!/bin/bash
mkdir content
cp ../web/target/web-1.0-SNAPSHOT.jar content/
cp ../config.yaml content/

docker build -t rpi-reader .
rm content/*
rmdir content/

