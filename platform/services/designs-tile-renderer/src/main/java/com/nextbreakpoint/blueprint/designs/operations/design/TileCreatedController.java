package com.nextbreakpoint.blueprint.designs.operations.design;

import com.nextbreakpoint.blueprint.common.core.Mapper;
import com.nextbreakpoint.blueprint.common.core.Message;
import com.nextbreakpoint.blueprint.common.vertx.Controller;
import com.nextbreakpoint.blueprint.designs.Store;
import com.nextbreakpoint.blueprint.designs.model.*;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.TileGenerator;
import com.nextbreakpoint.nextfractal.core.common.TileUtils;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Promise;
import io.vertx.rxjava.core.WorkerExecutor;
import io.vertx.rxjava.kafka.client.producer.KafkaProducer;
import io.vertx.rxjava.kafka.client.producer.KafkaProducerRecord;
import rx.Single;

import java.util.*;

public class TileCreatedController implements Controller<TileCreated, ControllerResult> {
    private final Logger logger = LoggerFactory.getLogger(TileCreatedController.class.getName());

    private final WorkerExecutor executor;
    private final int retries;
    private final Store store;
    private final String topic;
    private final KafkaProducer<String, String> producer;
    private final Mapper<TileCompleted, Message> mapper;

    public TileCreatedController(Store store, WorkerExecutor executor, String topic, KafkaProducer<String, String> producer, Mapper<TileCompleted, Message> mapper) {
        this.retries = 3;
        this.store = Objects.requireNonNull(store);
        this.executor = Objects.requireNonNull(executor);
        this.topic = Objects.requireNonNull(topic);
        this.producer = Objects.requireNonNull(producer);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public Single<ControllerResult> onNext(TileCreated event) {
        return computeTile(event)
                .flatMap(image -> uploadImage(event, image))
                .map(image -> createEvent(event))
                .flatMap(completedEvent -> Single.just(completedEvent)
                        .flatMap(this::createRecord)
                        .flatMap(this::publishRecord)
                        .flatMap(record -> onRecordSent(completedEvent))
                        .doOnError(err -> logger.error("Can't send message. The operation will be retried later", err))
                )
                .map(record -> new ControllerResult());
    }

    private Single<?> uploadImage(TileCreated event, byte[] image) {
        logger.info("Uploading image for version " + event.getUuid() + ", level " + event.getLevel() + ", x " + event.getX() + ", y " + event.getY());
        return Single.just(image);
    }

    private Single<byte[]> computeTile(TileCreated event) {
        return executor.rxExecuteBlocking(promise -> computeTileBlocking(promise, event), false);
    }

    private void computeTileBlocking(Promise<byte[]> promise, TileCreated event) throws RuntimeException {
        try {
            final TileParams params = makeTileParams(event);
            final JsonObject json = new JsonObject(event.getData());
            final Bundle bundle = convertToBundle(json);
            final byte[] image = renderImage(bundle, params);
            promise.complete(image);
        } catch (Exception e) {
            promise.fail("Can't render image for version " + event.getUuid() + ", level " + event.getLevel() + ", x " + event.getX() + ", y " + event.getY());
        }
    }

    private Single<KafkaProducerRecord<String, String>> createRecord(TileCompleted event) {
        return Single.fromCallable(() -> KafkaProducerRecord.create(topic, createKey(event), createValue(event)));
    }

    private String createKey(TileCompleted event) {
        return event.getUuid().toString();
    }

    private String createValue(TileCompleted event) {
        return Json.encode(mapper.transform(event));
    }

    private Single<KafkaProducerRecord<String, String>> publishRecord(KafkaProducerRecord<String, String> record) {
        return producer.rxWrite(record).retry(retries).map(ignore -> record);
    }

    private Single<PersistenceResult<Void>> onRecordSent(TileCompleted event) {
        return store.publishTile(event.getUuid(), event.getLevel(), event.getX(), event.getY());
    }

    private TileCompleted createEvent(TileCreated event) {
        return new TileCompleted(event.getUuid(), event.getLevel(), event.getX(), event.getY());
    }

    private static byte[] renderImage(Bundle bundle, TileParams params) throws Exception {
        int side = 1 << params.getZoom();
        return TileGenerator.generateImage(TileGenerator.createTileRequest(params.getSize(), side, side, params.getY() % side, params.getX() % side, bundle));
    }

    private static TileParams makeTileParams(TileCreated event) {
        final UUID uuid = event.getUuid();
        final int zoom = event.getLevel();
        final int x = event.getX();
        final int y = event.getY();
        final int size = 256;
        return new TileParams(uuid, zoom, x, y, size);
    }

    private static Bundle convertToBundle(JsonObject jsonObject) throws Exception {
        final String manifest = jsonObject.getString("manifest");
        final String metadata = jsonObject.getString("metadata");
        final String script = jsonObject.getString("script");
        return TileUtils.parseData(manifest, metadata, script);
    }

}
