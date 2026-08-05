package io.koraframework.example.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;
import io.koraframework.kafka.common.consumer.telemetry.KafkaConsumerTelemetry;

@Component
public final class AutoCommitRecordsTelemetryListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecords<String, String> records,
                 KafkaConsumerTelemetry.KafkaConsumerRecordsTelemetryContext<String, String> ctx) {
        for (ConsumerRecord<String, String> record : records) {
            var telemetryContext = ctx.get(record);
            success(record.value());
            telemetryContext.close(null);
        }
    }
}
