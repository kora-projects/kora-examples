FROM ghcr.io/graalvm/native-image-community:21 as builder

ARG APP_DIR=/opt/application
ARG JAR_DIR=build/libs
WORKDIR $APP_DIR

ADD $JAR_DIR/*.jar $APP_DIR/application.jar

RUN native-image --no-fallback -classpath $APP_DIR/application.jar --trace-class-initialization=ch.qos.logback.core.subst.Parser$1,ch.qos.logback.core.util.Loader,ch.qos.logback.core.model.processor.ImplicitModelHandler$1,ch.qos.logback.classic.Logger,ch.qos.logback.core.CoreConstants,ch.qos.logback.core.pattern.parser.Parser,ch.qos.logback.core.subst.NodeToStringTransformer$1,ch.qos.logback.core.subst.Token,ch.qos.logback.classic.Level,ch.qos.logback.core.status.InfoStatus,org.slf4j.LoggerFactory,ch.qos.logback.core.model.processor.DefaultProcessor$1,ch.qos.logback.core.status.StatusBase,ch.qos.logback.core.util.Duration,ch.qos.logback.classic.model.processor.LogbackClassicDefaultNestedComponentRules,ch.qos.logback.classic.PatternLayout,ch.qos.logback.core.model.processor.ChainedModelFilter$1

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