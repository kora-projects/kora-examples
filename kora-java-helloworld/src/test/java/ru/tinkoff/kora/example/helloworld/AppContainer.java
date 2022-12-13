package ru.tinkoff.kora.example.helloworld;

import java.net.URI;
import java.nio.file.Paths;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

public final class AppContainer extends GenericContainer<AppContainer> {

    private AppContainer() {
        super(new ImageFromDockerfile("kora-java-helloworld")
                .withDockerfile(Paths.get("Dockerfile").toAbsolutePath()));
    }

    private AppContainer(DockerImageName image) {
        super(image);
    }

    public static AppContainer build() {
        final String appImage = System.getenv("IMAGE_KORA_JAVA_HELLOWORLD");
        return (appImage != null && !appImage.isBlank())
                ? new AppContainer(DockerImageName.parse(appImage))
                : new AppContainer();
    }

    @Override
    protected void configure() {
        super.configure();
        withExposedPorts(8080, 8085);
        withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer.class)));
        waitingFor(Wait.forHttp("/system/readiness").forPort(8085).forStatusCode(200));
    }

    public int getPort() {
        return getMappedPort(8080);
    }

    public URI getURI() {
        return URI.create(String.format("http://%s:%s", getHost(), getPort()));
    }
}
