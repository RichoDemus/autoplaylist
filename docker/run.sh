#!/bin/bash
docker run --name reader -d --restart always --log-opt max-size=100m --log-opt max-file=1 -p 8081:8081 -p 5005:5005 -v /home/richo/applications/reader/data:/reader/data rpi-reader

