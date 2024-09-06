FROM ghcr.io/graalvm/native-image-community:21 as builder

ARG TARGET_DIR=/opt/app
ARG SOURCE_DIR=build/distributions
WORKDIR $TARGET_DIR

COPY $SOURCE_DIR/*.zip application.zip

RUN native-image --no-fallback -classpath $TARGET_DIR/application.zip

FROM ubuntu:noble-20240212 as runner

ARG TARGET_DIR=/opt/app
WORKDIR $TARGET_DIR

COPY --from=builder $TARGET_DIR/application $TARGET_DIR/application

ARG DOCKER_USER=app
RUN groupadd -r $DOCKER_USER && useradd -rg $DOCKER_USER $DOCKER_USER
RUN chmod +x application
USER $DOCKER_USER

EXPOSE 8080/tcp
EXPOSE 8085/tcp
CMD "/opt/application/application"