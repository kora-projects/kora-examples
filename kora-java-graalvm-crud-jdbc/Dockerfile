FROM ghcr.io/graalvm/native-image-community:21 as builder

ARG APP_DIR=/opt/application
ARG JAR_DIR=build/libs
WORKDIR $APP_DIR

ADD $JAR_DIR/*.jar $APP_DIR/application.jar

RUN native-image --no-fallback -classpath $APP_DIR/application.jar

FROM ubuntu:noble-20240212 as runner

ARG APP_DIR=/opt/application
WORKDIR $APP_DIR

COPY --from=builder $APP_DIR/application $APP_DIR/application

ARG DOCKER_USER=app
RUN groupadd -r $DOCKER_USER && useradd -rg $DOCKER_USER $DOCKER_USER
RUN chmod +x application
USER $DOCKER_USER

EXPOSE 8080/tcp
EXPOSE 8085/tcp
CMD "/opt/application/application"