FROM debian:11-slim

WORKDIR /usr/src/app

COPY ./target/*.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
