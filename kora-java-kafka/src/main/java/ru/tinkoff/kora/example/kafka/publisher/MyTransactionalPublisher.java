package ru.tinkoff.kora.example.kafka.publisher;

import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic;
import ru.tinkoff.kora.kafka.common.producer.TransactionalPublisher;

@KafkaPublisher("kafka.producer.my-transactional")
public interface MyTransactionalPublisher extends TransactionalPublisher<MyTransactionalPublisher.TopicPublisher> {

    @KafkaPublisher("kafka.producer.my-publisher")
    interface TopicPublisher {

        @Topic("kafka.producer.my-topic")
        void send(String value);
    }
}
