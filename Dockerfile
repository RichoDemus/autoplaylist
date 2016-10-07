FROM openjdk:8u92-jre-alpine

RUN mkdir /reader
RUN mkdir /reader/data
COPY config.yaml /reader/
COPY web/build/dependencies /reader/dependencies
COPY web/build/libs/web.jar /reader/
WORKDIR /reader
ENTRYPOINT []
CMD java -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -server -classpath "web.jar:dependencies/*" com.richo.reader.web.dropwizard.ReaderApplication server config.yaml
EXPOSE 8080 8081

