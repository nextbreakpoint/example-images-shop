package com.nextbreakpoint.blueprint.designs;

import com.nextbreakpoint.blueprint.common.vertx.TemplateHandler;
import com.nextbreakpoint.blueprint.designs.common.TileCompletedMessageMapper;
import com.nextbreakpoint.blueprint.designs.common.MessaggeFailureConsumer;
import com.nextbreakpoint.blueprint.designs.common.MessaggeSuccessConsumer;
import com.nextbreakpoint.blueprint.designs.model.ControllerResult;
import com.nextbreakpoint.blueprint.designs.model.TileCreated;
import com.nextbreakpoint.blueprint.designs.operations.design.TileCreatedInputMapper;
import com.nextbreakpoint.blueprint.designs.operations.design.TileCreatedController;
import com.nextbreakpoint.blueprint.designs.model.RecordAndMessage;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.WorkerExecutor;
import io.vertx.rxjava.kafka.client.producer.KafkaProducer;

public class Factory {
    private Factory() {}

    public static Handler<RecordAndMessage> createTileCreatedHandler(Store store, WorkerExecutor executor, String topic, KafkaProducer<String, String> producer, String messageSource) {
        return TemplateHandler.<RecordAndMessage, TileCreated, ControllerResult, JsonObject>builder()
                .withInputMapper(new TileCreatedInputMapper())
                .withController(new TileCreatedController(store, executor, topic, producer, new TileCompletedMessageMapper(messageSource)))
                .withOutputMapper(event -> new JsonObject())
                .onSuccess(new MessaggeSuccessConsumer())
                .onFailure(new MessaggeFailureConsumer())
                .build();
    }
}
