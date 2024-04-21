FROM docker.io/library/amazoncorretto:21

WORKDIR /usr/src/app

COPY ./target/*.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
