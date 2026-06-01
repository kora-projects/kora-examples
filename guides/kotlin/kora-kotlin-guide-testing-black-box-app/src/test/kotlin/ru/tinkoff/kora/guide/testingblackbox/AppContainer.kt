package ru.tinkoff.kora.guide.testingblackbox

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import java.net.URI
import java.nio.file.Path
import java.time.Duration

class AppContainer : GenericContainer<AppContainer>(
    ImageFromDockerfile("guide-database-jdbc-black-box")
        .withDockerfile(Path.of("../kora-kotlin-guide-database-jdbc-app/Dockerfile"))
) {

    init {
        withExposedPorts(8080, 8085)
        withStartupTimeout(Duration.ofSeconds(30))
        waitingFor(Wait.forHttp("/system/readiness").forPort(8085).forStatusCode(200))
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer::class.java)))
    }

    fun getURI(): URI = URI.create("http://$host:${getMappedPort(8080)}")

    fun getSystemURI(): URI = URI.create("http://$host:${getMappedPort(8085)}")
}
