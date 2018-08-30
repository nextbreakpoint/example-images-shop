package com.nextbreakpoint.shop.designs.controllers.update;

import com.nextbreakpoint.shop.common.model.Controller;
import com.nextbreakpoint.shop.common.model.Mapper;
import com.nextbreakpoint.shop.common.model.Message;
import com.nextbreakpoint.shop.common.model.commands.UpdateDesignCommand;
import com.nextbreakpoint.shop.common.model.events.DesignChangedEvent;
import com.nextbreakpoint.shop.designs.Store;
import com.nextbreakpoint.shop.designs.model.CommandResult;
import com.nextbreakpoint.shop.designs.model.CommandStatus;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.kafka.client.producer.KafkaProducer;
import io.vertx.rxjava.kafka.client.producer.KafkaProducerRecord;
import rx.Single;

import java.util.Objects;

public class UpdateDesignController implements Controller<UpdateDesignCommand, CommandResult> {
    private Logger logger = LoggerFactory.getLogger(UpdateDesignController.class);

    private final Store store;
    private final String topic;
    private final KafkaProducer<String, String> producer;
    private final Mapper<DesignChangedEvent, Message> mapper;

    public UpdateDesignController(Store store, String topic, KafkaProducer<String, String> producer, Mapper<DesignChangedEvent, Message> mapper) {
        this.store = Objects.requireNonNull(store);
        this.topic = Objects.requireNonNull(topic);
        this.producer = Objects.requireNonNull(producer);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public Single<CommandResult> onNext(UpdateDesignCommand command) {
        return store.updateDesign(command)
                .map(result -> new DesignChangedEvent(result.getUuid(), System.currentTimeMillis()))
                .flatMap(this::createRecord)
                .flatMap(record -> producer.rxWrite(record))
                .map(metadata -> new CommandResult(command.getUuid(), CommandStatus.SUCCESS))
                .doOnError(err -> logger.error("Failed to write record into Kafka", err))
                .onErrorReturn(err -> new CommandResult(command.getUuid(), CommandStatus.FAILURE));
    }

    protected Single<KafkaProducerRecord<String, String>> createRecord(DesignChangedEvent event) {
        return Single.fromCallable(() -> KafkaProducerRecord.create(topic, event.getUuid().toString(), Json.encode(mapper.transform(event))));
    }
}