package ru.tinkoff.kora.kotlin.example.kafka.listener

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.kafka.*
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import ru.tinkoff.kora.application.graph.Lifecycle
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.kotlin.example.kafka.Application
import ru.tinkoff.kora.kotlin.example.kafka.kafkaConfig
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Duration
import java.util.concurrent.Executors

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = Topics("my-topic-consumer"))
@KoraAppTest(Application::class)
class AutoCommitValueKeyListenerTests : KoraAppTestConfigModifier {
    @ConnectionKafka
    lateinit var connection: KafkaConnection

    @Tag(AutoCommitValueKeyListenerModule.AutoCommitValueKeyListenerProcessTag::class)
    @TestComponent
    lateinit var consumerLifecycle: Lifecycle

    @TestComponent
    lateinit var consumer: AutoCommitValueKeyListener
    override fun config(): KoraConfigModification = kafkaConfig(connection)

    @Test
    fun processed() {
        connection.send("my-topic-consumer", Event.ofValueAndRandomKey("Ivan"))
        Awaitility.await().atMost(Duration.ofSeconds(15)).pollExecutorService(Executors.newSingleThreadExecutor())
            .until { consumer.received().size == 1 }
    }
}
