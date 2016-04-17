FROM develar/java:8u45

RUN mkdir /reader
RUN mkdir /reader/data
COPY build/docker/ /reader
WORKDIR /reader
ENTRYPOINT []
CMD java -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -server -classpath "web.jar:dependencies/*" com.richo.reader.web.dropwizard.ReaderApplication server config.yaml
EXPOSE 8080 8081

