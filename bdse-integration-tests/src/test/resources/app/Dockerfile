# https://hub.docker.com/_/java/
FROM openjdk:8-jdk-alpine
MAINTAINER Artem Trofimov <trofimov9artem@gmail.com>
VOLUME /tmp
ADD target/bdse-app-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar", "ru.csc.bdse.app.Application"]