package ru.tinkoff.kora.kotlin.example.telemetry

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.net.URI

class TelemetryContainer :
    GenericContainer<TelemetryContainer>(DockerImageName.parse("otel/opentelemetry-collector:0.67.0")) {

    fun getPrometheusURI(): URI = URI.create("http://$host:${getMappedPort(8888)}/metrics")

    fun getCollectorURI(): URI = URI.create("http://$host:${getMappedPort(4317)}")

    fun getCollectorURIInNetwork(): URI = URI.create("http://${networkAliases[0]}:4317")

    override fun configure() {
        super.configure()
        addExposedPort(4317)
        addExposedPort(8888)
        addExposedPort(13133)
        waitingFor(Wait.forLogMessage(".*Everything is ready.*", 1))
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(javaClass)))
    }
}
