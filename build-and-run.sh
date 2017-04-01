#!/bin/bash
./gradlew copyDependencies && java -classpath "docker/build/dependencies/*" com.richo.reader.web.dropwizard.ReaderApplication server docker/config.yaml
