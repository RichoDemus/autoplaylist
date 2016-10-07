FROM openjdk:8u92-jre-alpine

RUN mkdir /youtube
COPY build/dependencies /youtube/dependencies
COPY build/libs/youtube-mock.jar /youtube/
WORKDIR /youtube
ENTRYPOINT []
CMD java -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -server -classpath "youtube-mock.jar:dependencies/*" com.richodemus.reader.mock.youtube.YoutubemockApplication server
EXPOSE 8080 8081

