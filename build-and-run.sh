#!/bin/bash
./gradlew copyDependencies && java -classpath "web/build/libs/web.jar:web/build/dependencies/*" com.richo.reader.web.dropwizard.ReaderApplication server config.yaml
