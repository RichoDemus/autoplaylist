#!/bin/bash
./gradlew copyDependencies && java -classpath "server/docker/build/dependencies/*" com.richo.reader.web.dropwizard.ReaderApplication server server/docker/config.yaml
