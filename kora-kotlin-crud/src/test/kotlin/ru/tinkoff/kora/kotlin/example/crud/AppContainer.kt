package ru.tinkoff.kora.kotlin.example.crud

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.Future

class AppContainer : GenericContainer<AppContainer> {

    constructor(dockerImageName: DockerImageName) : super(dockerImageName)

    constructor(image: Future<String>) : super(image)

    companion object {

        fun build(): AppContainer {
            val appImage = System.getenv("IMAGE_KORA_KOTLIN_CRUD")
            return if (appImage != null && appImage.isNotBlank())
                AppContainer(DockerImageName.parse(appImage))
            else AppContainer(
                ImageFromDockerfile("kora-kotlin-crud", true)
                    .withDockerfile(Paths.get("Dockerfile").toAbsolutePath())
            )
        }
    }

    override fun configure() {
        super.configure()
        withExposedPorts(8080, 8085)
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer::class.java)))
        waitingFor(Wait.forHttp("/system/readiness").forPort(8085).forStatusCode(200))
    }

    val port: Int get() = getMappedPort(8080)

    val uri: URI get() = URI.create("http://$host:$port")
}

