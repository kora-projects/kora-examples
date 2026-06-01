package ru.tinkoff.kora.kotlin.example.openapi.http.server

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
        ImageFromDockerfile("kora-kotlin-openapi-generator-httpserver")
            .withDockerfile(Paths.get("Dockerfile").toAbsolutePath())
    )

    private constructor(image: DockerImageName) : super(image)

    override fun configure() {
        super.configure()
        withExposedPorts(8080)
        waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(20)))
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer::class.java)))
    }

    fun getPort(): Int = getMappedPort(8080)

    fun getURI(): URI = URI.create("http://$host:${getPort()}")

    companion object {
        fun build(): AppContainer {
            val appImage = System.getenv("IMAGE_KORA_KOTLIN_OPENAPI_GENERATOR_HTTP_SERVER")
            return if (!appImage.isNullOrBlank()) AppContainer(DockerImageName.parse(appImage)) else AppContainer()
        }
    }
}
