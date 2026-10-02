package io.koraframework.guide.messaging.kafka.kafka

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.guide.messaging.kafka.service.UserService
import io.koraframework.json.common.annotation.Json
import io.koraframework.kafka.common.annotation.KafkaListener

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
