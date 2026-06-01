package ru.tinkoff.kora.guide.messaging.kafka.kafka;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.messaging.kafka.service.UserService;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class UserCreatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserCreatedConsumer.class);

    private final UserService userService;

    public UserCreatedConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener("kafka.consumer.user-created")
    public void process(@Json @Nullable UserCreatedEvent event, @Nullable Exception exception) {
        if (exception != null) {
            logger.warn("Failed to consume user creation event", exception);
            return;
        }
        if (event == null) {
            logger.warn("Received null user creation event without exception");
            return;
        }
        logger.info("Consuming user creation event for user {}", event.id());
        this.userService.createUser(event);
    }
}
