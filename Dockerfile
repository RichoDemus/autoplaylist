FROM adoptopenjdk/openjdk11:debian-slim as builder
RUN mkdir src
WORKDIR src

COPY gradlew /src/
COPY gradle /src/gradle
RUN ./gradlew --no-daemon #just so that we download gradle in its own layer

COPY . /src/
RUN ./gradlew --no-daemon
RUN ./gradlew server:docker:copyDependencies --no-daemon
#RUN mkdir jars
#RUN cp server/spring/build/libs/spring.jar jars

FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir /reader
RUN mkdir /reader/data
COPY config.yaml /reader/
COPY --from=builder src/server/docker/build/dependencies /reader/jars
WORKDIR /reader
ENTRYPOINT []
CMD java \
-Dreader.gcs.project=$GCS_PROJECT \
-Dreader.gcs.bucket=$GCS_BUCKET \
-Xmx2G \
-XX:+UseConcMarkSweepGC \
-XX:+CMSParallelRemarkEnabled \
-server \
-classpath "jars/*" \
com.richo.reader.web.dropwizard.ReaderApplication \
server \
config.yaml
EXPOSE 8080 8081 9010
