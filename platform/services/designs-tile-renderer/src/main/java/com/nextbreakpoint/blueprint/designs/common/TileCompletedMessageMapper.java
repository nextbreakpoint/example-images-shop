package com.nextbreakpoint.blueprint.designs.common;

import com.nextbreakpoint.blueprint.common.core.Mapper;
import com.nextbreakpoint.blueprint.common.core.Message;
import com.nextbreakpoint.blueprint.common.core.MessageType;
import com.nextbreakpoint.blueprint.designs.model.TileCompleted;
import io.vertx.core.json.Json;

import java.util.Objects;
import java.util.UUID;

public class TileCompletedMessageMapper implements Mapper<TileCompleted, Message> {
    private final String messageSource;

    public TileCompletedMessageMapper(String messageSource) {
        this.messageSource = Objects.requireNonNull(messageSource);
    }

    @Override
    public Message transform(TileCompleted event) {
        return new Message(UUID.randomUUID().toString(), MessageType.DESIGN_TILE_COMPLETED, Json.encode(event), messageSource, event.getUuid().toString(), System.currentTimeMillis());
    }
}
