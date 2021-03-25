package com.nextbreakpoint.blueprint.designs.model;

import com.nextbreakpoint.blueprint.common.core.Message;
import io.vertx.rxjava.kafka.client.consumer.KafkaConsumerRecord;

import java.util.Objects;

public class RecordAndMessage {
    private final KafkaConsumerRecord<String, String> record;
    private final Message message;

    public RecordAndMessage(KafkaConsumerRecord<String, String> record, Message message) {
        this.record = Objects.requireNonNull(record);
        this.message = Objects.requireNonNull(message);
    }

    public KafkaConsumerRecord<String, String> getRecord() {
        return record;
    }

    public Message getMessage() {
        return message;
    }
}
