package com.nextbreakpoint.blueprint.designs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

public class TileCreated {
    private final UUID uuid;
    private final String data;
    private final String checksum;
    private final short level;
    private final short x;
    private final short y;

    @JsonCreator
    public TileCreated(
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("data") String data,
            @JsonProperty("checksum") String checksum,
            @JsonProperty("level") short level,
            @JsonProperty("x") short x,
            @JsonProperty("y") short y
    ) {
        this.uuid = Objects.requireNonNull(uuid);
        this.data = Objects.requireNonNull(data);
        this.checksum = Objects.requireNonNull(checksum);
        this.level = level;
        this.x = x;
        this.y = y;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getData() {
        return data;
    }

    public String getChecksum() {
        return checksum;
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
