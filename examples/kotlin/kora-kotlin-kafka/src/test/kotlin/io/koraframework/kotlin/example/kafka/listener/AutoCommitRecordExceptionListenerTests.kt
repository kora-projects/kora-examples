package io.koraframework.kotlin.example.kafka.listener

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.kafka.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import io.koraframework.application.graph.Lifecycle
import io.koraframework.common.annotation.Tag
import io.koraframework.kotlin.example.kafka.Application
import io.koraframework.kotlin.example.kafka.kafkaConfig
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = Topics("my-topic-consumer"))
@KoraAppTest(Application::class)
class AutoCommitRecordExceptionListenerTests : KoraAppTestConfigModifier {
    @ConnectionKafka
    lateinit var connection: KafkaConnection

    @Tag(AutoCommitRecordExceptionListenerModule.AutoCommitRecordExceptionListenerProcessTag::class)
    @TestComponent
    lateinit var consumerLifecycle: Lifecycle

    @TestComponent
    lateinit var consumer: AutoCommitRecordExceptionListener

    override fun config(): KoraConfigModification = kafkaConfig(connection)

    @Test
    fun processed() {
        connection.send("my-topic-consumer", Event.ofValueAndRandomKey("""{"username":"Ivan","code":1}"""))

        awaitReceived(consumer)
        assertEquals("Ivan", consumer.received()[0].username)
    }

    @Test
    fun failed() {
        connection.send("my-topic-consumer", Event.ofValueAndRandomKey("incorrect"))

        awaitFailed(consumer)
        assertEquals(0, consumer.received().size)
    }
}
