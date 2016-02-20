from rpi-java8:8u71

run mkdir /reader
run mkdir /reader/data
copy content /reader
workdir /reader
cmd ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005","-XX:+UseConcMarkSweepGC","-XX:+CMSParallelRemarkEnabled","-server","-jar","web-1.0-SNAPSHOT.jar","server","config.yaml"]
expose 8080 8081 5005

