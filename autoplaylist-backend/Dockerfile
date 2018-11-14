FROM openjdk:10-jdk as builder
RUN mkdir src
WORKDIR src

COPY gradlew /src/
COPY gradle /src/gradle
RUN ./gradlew --no-daemon #just so that we download gradle in its own layer

COPY . /src/
RUN ./gradlew --no-daemon
RUN mkdir jars
RUN cp server/spring/build/libs/spring.jar jars

FROM openjdk:10-jre-slim
RUN mkdir app
WORKDIR app
COPY --from=builder /src/jars .

# the urandom thing reduces tomcat startup time, no idea if it's smart or not
CMD java \
-Djava.security.egd=file:/dev/./urandom \
-Djava.util.concurrent.ForkJoinPool.common.parallelism=100 \
-Dspring.profiles.active=prod \
-Dspotify.clientId=$CLIENT_ID \
-Dspotify.clientSecret=$CLIENT_SECRET \
-Dspotify.redirectUrl=$REDIRECT_URL \
-Dgcs.project=$GCS_PROJECT \
-Dgcs.bucket=$GCS_BUCKET \
-jar spring.jar \

EXPOSE 8080
