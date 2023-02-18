FROM docker.io/library/amazoncorretto:19

WORKDIR /usr/src/app

COPY ./target/*.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
