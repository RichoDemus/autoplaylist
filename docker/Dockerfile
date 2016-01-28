from rpi-java8:8u71

run mkdir /reader
run mkdir /reader/data
copy content /reader
workdir /reader
cmd ["java","-jar","web-1.0-SNAPSHOT.jar","server","config.yaml"]
expose 8080 8081

