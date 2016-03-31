#!/bin/bash

mvn clean install failsafe:integration-test failsafe:verify -pl :reader-test
