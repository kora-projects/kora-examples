package ru.tinkoff.kora.kotlin.example.grpc.server

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName
import java.nio.file.Paths
import java.time.Duration

class AppContainer : GenericContainer<AppContainer> {

    private constructor() : super(
        ImageFromDockerfile("kora-kotlin-grpc-server")
            .withDockerfile(Paths.get("Dockerfile").toAbsolutePath())
    )

    private constructor(image: DockerImageName) : super(image)

    override fun configure() {
        super.configure()
        withEnv("GRPC_PORT", "8090")
        withExposedPorts(8090)
        waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(50)))
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer::class.java)))
    }

    fun getPort(): Int = getMappedPort(8090)

    companion object {
        fun build(): AppContainer {
            val appImage = System.getenv("IMAGE_KORA_KOTLIN_GRPC_SERVER")
            return if (!appImage.isNullOrBlank()) AppContainer(DockerImageName.parse(appImage)) else AppContainer()
        }
    }
}
