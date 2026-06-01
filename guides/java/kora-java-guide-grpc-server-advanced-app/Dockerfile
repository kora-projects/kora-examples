FROM eclipse-temurin:25-jre-jammy

ARG TARGET_DIR=/opt/app

COPY build/distributions/application.tar /application.tar
RUN mkdir -p ${TARGET_DIR}
RUN tar -xf /application.tar -C ${TARGET_DIR}
RUN rm /application.tar

ARG DOCKER_USER=app
RUN groupadd -r ${DOCKER_USER} && useradd -rg ${DOCKER_USER} ${DOCKER_USER}
USER ${DOCKER_USER}

EXPOSE 8092/tcp
CMD ["/opt/app/application/bin/application"]
