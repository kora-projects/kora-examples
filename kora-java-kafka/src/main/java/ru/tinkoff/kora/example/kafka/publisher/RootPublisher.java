package ru.tinkoff.kora.example.kafka.publisher;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

/**
 * Is required to access publisher in tests
 */
@Root
@Component
public final class RootPublisher {

    private final ProducerJsonPublisher producerJsonPublisher;
    private final ProducerMapperPublisher producerMapperPublisher;
    private final ProducerPublisher producerPublisher;
    private final TopicJsonPublisher topicJsonPublisher;
    private final TopicMapperPublisher topicMapperPublisher;
    private final TopicKeyHeadersPublisher topicKeyHeadersPublisher;
    private final TopicKeyPublisher topicKeyPublisher;
    private final TopicPublisher topicPublisher;
    private final MyTransactionalPublisher transactionalPublisher;

    public RootPublisher(ProducerJsonPublisher producerJsonPublisher,
                         ProducerMapperPublisher producerMapperPublisher,
                         ProducerPublisher producerPublisher,
                         TopicJsonPublisher topicJsonPublisher,
                         TopicMapperPublisher topicMapperPublisher,
                         TopicKeyHeadersPublisher topicKeyHeadersPublisher,
                         TopicKeyPublisher topicKeyPublisher,
                         TopicPublisher topicPublisher,
                         MyTransactionalPublisher transactionalPublisher) {
        this.producerJsonPublisher = producerJsonPublisher;
        this.producerMapperPublisher = producerMapperPublisher;
        this.producerPublisher = producerPublisher;
        this.topicJsonPublisher = topicJsonPublisher;
        this.topicMapperPublisher = topicMapperPublisher;
        this.topicKeyHeadersPublisher = topicKeyHeadersPublisher;
        this.topicKeyPublisher = topicKeyPublisher;
        this.topicPublisher = topicPublisher;
        this.transactionalPublisher = transactionalPublisher;
    }
}
