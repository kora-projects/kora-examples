package ru.tinkoff.kora.example.telemetry;

import java.net.URI;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public final class TelemetryContainer extends GenericContainer<TelemetryContainer> {

    public TelemetryContainer() {
        super(DockerImageName.parse("otel/opentelemetry-collector:0.67.0"));
    }

    public URI getPrometheusURI() {
        return URI.create(String.format("http://%s:%s/metrics", getHost(), getMappedPort(8888)));
    }

    public URI getCollectorURI() {
        return URI.create("http://%s:%s".formatted(getHost(), getMappedPort(4317)));
    }

    public URI getCollectorURIInNetwork() {
        return URI.create("http://%s:%s".formatted(getNetworkAliases().get(0), 4317));
    }

    @Override
    protected void configure() {
        super.configure();
        addExposedPort(4317);
        addExposedPort(8888);
        addExposedPort(13133);
        waitingFor(Wait.forLogMessage(".*Everything is ready.*", 1));
        withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(getClass())));
    }
}
