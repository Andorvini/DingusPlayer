FROM docker.io/library/amazoncorretto:19-alpine-jdk

WORKDIR /usr/src/app

COPY ./target/*.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
