package com.nextbreakpoint.shop.designs.handlers;

import com.nextbreakpoint.shop.common.Controller;
import com.nextbreakpoint.shop.common.Mapper;
import com.nextbreakpoint.shop.common.Message;
import com.nextbreakpoint.shop.common.DeleteDesignsEvent;
import com.nextbreakpoint.shop.designs.model.DeleteDesignsResult;
import io.vertx.core.json.Json;
import io.vertx.rxjava.kafka.client.producer.KafkaProducer;
import io.vertx.rxjava.kafka.client.producer.KafkaProducerRecord;
import rx.Single;

import java.util.Objects;

public class DeleteDesignsController implements Controller<DeleteDesignsEvent, DeleteDesignsResult> {
    private final String topic;
    private final KafkaProducer<String, String> producer;
    private final Mapper<DeleteDesignsEvent, Message> messageMapper;

    public DeleteDesignsController(String topic, KafkaProducer<String, String> producer, Mapper<DeleteDesignsEvent, Message> messageMapper) {
        this.topic = Objects.requireNonNull(topic);
        this.producer = Objects.requireNonNull(producer);
        this.messageMapper = Objects.requireNonNull(messageMapper);
    }

    @Override
    public Single<DeleteDesignsResult> onNext(DeleteDesignsEvent event) {
        return createRecord(event)
                .flatMap(record -> producer.rxWrite(record))
                .map(record -> new DeleteDesignsResult(1))
                .onErrorReturn(record -> new DeleteDesignsResult(0));
    }

    protected Single<KafkaProducerRecord<String, String>> createRecord(DeleteDesignsEvent request) {
        return Single.fromCallable(() -> KafkaProducerRecord.create(topic, "designs", Json.encode(messageMapper.transform(request))));
    }
}
