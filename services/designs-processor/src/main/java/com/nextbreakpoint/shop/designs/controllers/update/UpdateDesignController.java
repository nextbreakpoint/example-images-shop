package com.nextbreakpoint.shop.designs.controllers.update;

import com.nextbreakpoint.shop.common.model.Controller;
import com.nextbreakpoint.shop.common.model.Mapper;
import com.nextbreakpoint.shop.common.model.Message;
import com.nextbreakpoint.shop.common.model.MessageReceipt;
import com.nextbreakpoint.shop.common.model.events.DesignChangedEvent;
import com.nextbreakpoint.shop.common.model.events.UpdateDesignEvent;
import com.nextbreakpoint.shop.designs.Store;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.kafka.client.producer.KafkaProducer;
import io.vertx.rxjava.kafka.client.producer.KafkaProducerRecord;
import rx.Single;

import java.util.Objects;

public class UpdateDesignController implements Controller<UpdateDesignEvent, MessageReceipt> {
    private Logger LOG = LoggerFactory.getLogger(UpdateDesignController.class);

    private final Store store;
    private final String topic;
    private final KafkaProducer<String, String> producer;
    private final Mapper<DesignChangedEvent, Message> messageMapper;

    public UpdateDesignController(Store store, String topic, KafkaProducer<String, String> producer, Mapper<DesignChangedEvent, Message> messageMapper) {
        this.store = Objects.requireNonNull(store);
        this.topic = Objects.requireNonNull(topic);
        this.producer = Objects.requireNonNull(producer);
        this.messageMapper = Objects.requireNonNull(messageMapper);
    }

    @Override
    public Single<MessageReceipt> onNext(UpdateDesignEvent event) {
        return store.updateDesign(event)
                .map(result -> new DesignChangedEvent(result.getUuid(), System.currentTimeMillis()))
                .flatMap(this::createRecord)
                .flatMap(record -> producer.rxWrite(record))
                .map(metadata -> new MessageReceipt(1, metadata.getTimestamp()))
                .doOnError(err -> LOG.error("Failed to write record into Kafka", err))
                .onErrorReturn(err -> new MessageReceipt(0, 0L));
    }

    protected Single<KafkaProducerRecord<String, String>> createRecord(DesignChangedEvent event) {
        return Single.fromCallable(() -> KafkaProducerRecord.create(topic, event.getUuid().toString(), Json.encode(messageMapper.transform(event))));
    }
}
