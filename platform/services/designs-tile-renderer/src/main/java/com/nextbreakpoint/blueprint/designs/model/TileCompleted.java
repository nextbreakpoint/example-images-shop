package com.nextbreakpoint.blueprint.designs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

public class TileCompleted {
    private final UUID uuid;
    private final short level;
    private final short x;
    private final short y;

    @JsonCreator
    public TileCompleted(
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("level") short level,
            @JsonProperty("x") short x,
            @JsonProperty("y") short y
    ) {
        this.uuid = Objects.requireNonNull(uuid);
        this.level = level;
        this.x = x;
        this.y = y;
    }

    public UUID getUuid() {
        return uuid;
    }

    public short getLevel() {
        return level;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }
}
