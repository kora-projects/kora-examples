package ru.tinkoff.kora.guide.messaging.kafka.kafka

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.messaging.kafka.service.UserService
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener

@Component
class UserCreatedConsumer(
    private val userService: UserService
) {

    private val logger = LoggerFactory.getLogger(UserCreatedConsumer::class.java)

    @KafkaListener("kafka.consumer.user-created")
    fun process(@Json event: UserCreatedEvent?, exception: Exception?) {
        if (exception != null) {
            logger.warn("Failed to consume user creation event", exception)
            return
        }
        if (event == null) {
            logger.warn("Received null user creation event without exception")
            return
        }
        logger.info("Consuming user creation event for user {}", event.id)
        userService.createUser(event)
    }
}
