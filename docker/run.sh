#!/bin/bash
docker run --name reader -d --restart always -v /home/pi/applications/reader/data:/reader/data rpi-reader

