package ru.tinkoff.kora.guide.observability;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

final class AppContainer extends GenericContainer<AppContainer> {

    AppContainer() {
        super(new ImageFromDockerfile("guide-observability-black-box")
                .withDockerfile(Path.of("Dockerfile").toAbsolutePath()));

        withExposedPorts(8080, 8085);
        withStartupTimeout(Duration.ofSeconds(30));
        waitingFor(Wait.forHttp("/system/readiness").forPort(8085).forStatusCode(200));
        withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer.class)));
    }

    URI getURI() {
        return URI.create("http://" + getHost() + ":" + getMappedPort(8080));
    }

    URI getSystemURI() {
        return URI.create("http://" + getHost() + ":" + getMappedPort(8085));
    }
}
