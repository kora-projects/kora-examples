package ru.tinkoff.kora.kotlin.example.telemetry

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName
import java.net.URI
import java.nio.file.Paths
import java.time.Duration

class AppContainer : GenericContainer<AppContainer> {

    private constructor() : super(
        ImageFromDockerfile("kora-kotlin-telemetry")
            .withDockerfile(Paths.get("Dockerfile").toAbsolutePath())
    )

    private constructor(image: DockerImageName) : super(image)

    override fun configure() {
        super.configure()
        withExposedPorts(8080, 8085)
        withStartupTimeout(Duration.ofSeconds(50))
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(javaClass)))
        waitingFor(
            Wait.forHttp("/readiness")
                .forPort(8085)
                .forStatusCode(503)
        )
    }

    fun getPort(): Int = getMappedPort(8080)

    fun getSystemPort(): Int = getMappedPort(8085)

    fun getURI(): URI = URI.create("http://$host:${getPort()}")

    fun getSystemURI(): URI = URI.create("http://$host:${getSystemPort()}")

    companion object {
        fun build(): AppContainer {
            val appImage = System.getenv("IMAGE_KORA_KOTLIN_TELEMETRY")
            return if (!appImage.isNullOrBlank()) AppContainer(DockerImageName.parse(appImage)) else AppContainer()
        }
    }
}
