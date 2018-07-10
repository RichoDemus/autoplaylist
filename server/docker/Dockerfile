FROM openjdk:8u92-jre-alpine

RUN mkdir /reader
RUN mkdir /reader/data
COPY config.yaml /reader/
COPY build/dependencies /reader/jars
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
