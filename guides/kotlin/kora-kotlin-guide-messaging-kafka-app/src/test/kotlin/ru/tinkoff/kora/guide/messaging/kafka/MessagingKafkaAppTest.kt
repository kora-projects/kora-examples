package ru.tinkoff.kora.guide.messaging.kafka

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka
import io.goodforgod.testcontainers.extensions.kafka.Topics
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import ru.tinkoff.kora.guide.messaging.kafka.controller.UserController
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserRequest
import ru.tinkoff.kora.guide.messaging.kafka.kafka.UserCreatedConsumerModule
import ru.tinkoff.kora.guide.messaging.kafka.service.UserService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Duration
import java.util.concurrent.Executors

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = Topics("user-created-events"))
@KoraAppTest(value = Application::class, modules = [UserCreatedConsumerModule::class])
class MessagingKafkaAppTest : KoraAppTestConfigModifier {
    @ConnectionKafka
    lateinit var connection: KafkaConnection

    @TestComponent
    lateinit var userController: UserController

    @TestComponent
    lateinit var userService: UserService

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("KAFKA_BOOTSTRAP", connection.params().bootstrapServers())

    @Test
    fun createUserPublishesUserCreatedEvent() {
        val consumer = connection.subscribe("user-created-events")

        val accepted = userController.createUser(UserRequest("Kafka User", "kafka-user@example.com"))

        assertEquals(202, accepted.code())
        assertNotNull(accepted.body())
        assertNotNull(accepted.body()!!.id)

        val receivedEvent = consumer.assertReceivedAtLeast(1)[0]
        assertEquals(accepted.body()!!.id, receivedEvent.value().asJson().getString("id"))
        assertEquals("Kafka User", receivedEvent.value().asJson().getString("name"))
        assertEquals("kafka-user@example.com", receivedEvent.value().asJson().getString("email"))
    }

    @Test
    fun consumerStoresUserAfterKafkaMessageIsProcessed() {
        val accepted = userController.createUser(UserRequest("Async User", "async-user@example.com"))
        val userId = accepted.body()!!.id

        Awaitility.await()
            .atMost(Duration.ofSeconds(15))
            .pollExecutorService(Executors.newSingleThreadExecutor())
            .until { userService.getUser(userId) != null }

        val storedUser = userService.getUser(userId) ?: error("User was not stored")
        assertEquals(userId, storedUser.id)
        assertEquals("Async User", storedUser.name)
        assertEquals("async-user@example.com", storedUser.email)

        val users = userService.getUsers(0, 20, "name")
        assertTrue(users.any { it.id == userId })
    }
}
