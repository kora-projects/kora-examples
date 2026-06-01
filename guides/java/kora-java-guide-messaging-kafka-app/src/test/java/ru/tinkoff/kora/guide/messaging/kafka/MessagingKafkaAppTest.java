package ru.tinkoff.kora.guide.messaging.kafka;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka;
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection;
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka;
import io.goodforgod.testcontainers.extensions.kafka.Topics;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.tinkoff.kora.guide.messaging.kafka.controller.UserController;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserRequest;
import ru.tinkoff.kora.guide.messaging.kafka.kafka.$UserCreatedPublisher_PublisherModule;
import ru.tinkoff.kora.guide.messaging.kafka.kafka.UserCreatedConsumerModule;
import ru.tinkoff.kora.guide.messaging.kafka.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = @Topics({ "user-created-events" }))
@KoraAppTest(value = Application.class, modules = UserCreatedConsumerModule.class)
class MessagingKafkaAppTest implements KoraAppTestConfigModifier {

    @ConnectionKafka
    private KafkaConnection connection;

    @TestComponent
    private UserController userController;

    @TestComponent
    private UserService userService;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("KAFKA_BOOTSTRAP", connection.params().bootstrapServers());
    }

    @Test
    void createUserPublishesUserCreatedEvent() {
        var consumer = this.connection.subscribe("user-created-events");

        var accepted = this.userController.createUser(new UserRequest("Kafka User", "kafka-user@example.com"));

        assertEquals(202, accepted.code());
        assertNotNull(accepted.body());
        assertNotNull(accepted.body().id());

        var receivedEvent = consumer.assertReceivedAtLeast(1).get(0);
        assertEquals(accepted.body().id(), receivedEvent.value().asJson().getString("id"));
        assertEquals("Kafka User", receivedEvent.value().asJson().getString("name"));
        assertEquals("kafka-user@example.com", receivedEvent.value().asJson().getString("email"));
    }

    @Test
    void consumerStoresUserAfterKafkaMessageIsProcessed() {
        var accepted = this.userController.createUser(new UserRequest("Async User", "async-user@example.com"));
        var userId = accepted.body().id();

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollExecutorService(Executors.newSingleThreadExecutor())
                .until(() -> this.userService.getUser(userId).isPresent());

        var storedUser = this.userService.getUser(userId).orElseThrow();
        assertEquals(userId, storedUser.id());
        assertEquals("Async User", storedUser.name());
        assertEquals("async-user@example.com", storedUser.email());

        var users = this.userService.getUsers(0, 20, "name");
        assertTrue(users.stream().anyMatch(user -> user.id().equals(userId)));
    }
}
