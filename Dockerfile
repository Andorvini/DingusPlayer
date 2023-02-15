FROM debian:11-slim

WORKDIR /usr/src/app

RUN apt update
RUN apt install openjdk-17-jre -y
COPY ./target/*.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
