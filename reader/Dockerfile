FROM adoptopenjdk/openjdk11:debian-slim as builder
RUN mkdir src
WORKDIR src

COPY mvnw /src/
COPY .mvn /src/.mvn/
RUN ./mvnw -v #just so that we download mvn in its own layer

COPY . /src/
RUN ./mvnw package
#RUN mkdir jars
#RUN cp server/spring/build/libs/spring.jar jars

FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir /reader
RUN mkdir /reader/data
COPY --from=builder src/server/core/spring/target/spring-*.jar /reader/jars/reader.jar
WORKDIR /reader
ENTRYPOINT []
CMD java \
-Xmx2G \
-XX:+UseConcMarkSweepGC \
-XX:+CMSParallelRemarkEnabled \
-jar "jars/reader.jar" \
--spring.profiles.active=prod
EXPOSE 8080 8081 9010
