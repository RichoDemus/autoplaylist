FROM openjdk:8u92-jre-alpine

RUN mkdir /reader
RUN mkdir /reader/data
COPY config.yaml /reader/
COPY build/dependencies /reader/jars
WORKDIR /reader
ENTRYPOINT []
CMD ["java",\
"-Dreader.kakfa.host=$KAFKA_HOST", \
"-Xmx2G",\
"-Dcom.sun.management.jmxremote",\
"-Dcom.sun.management.jmxremote.port=9010",\
"-Dcom.sun.management.jmxremote.local.only=false",\
"-Dcom.sun.management.jmxremote.authenticate=false",\
"-Dcom.sun.management.jmxremote.ssl=false",\
"-XX:+UseConcMarkSweepGC",\
"-XX:+CMSParallelRemarkEnabled",\
"-server",\
"-classpath",\
"jars/*",\
"com.richo.reader.web.dropwizard.ReaderApplication",\
"server",\
"config.yaml"]
EXPOSE 8080 8081 9010
