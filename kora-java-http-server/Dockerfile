ARG RUN_IMAGE=eclipse-temurin:17-jre-jammy
FROM ${RUN_IMAGE}

ARG APP_DIR=/opt/application
ARG JAR_DIR=build/libs

ADD $JAR_DIR/*.jar $APP_DIR/application.jar

ARG DOCKER_USER=app
RUN groupadd -r $DOCKER_USER && useradd -rg $DOCKER_USER $DOCKER_USER
USER $DOCKER_USER

EXPOSE 8080/tcp
EXPOSE 8085/tcp
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/opt/application/application.jar" ]