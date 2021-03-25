package com.nextbreakpoint.blueprint.designs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TileDocument {
    private final String uuid;
    private final String version;
    private final Short level;
    private final Short x;
    private final Short y;
    private final String created;
    private final String updated;

    @JsonCreator
    public TileDocument(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("version") String version,
            @JsonProperty("level") Short level,
            @JsonProperty("x") Short x,
            @JsonProperty("y") Short y,
            @JsonProperty("created") String created,
            @JsonProperty("updated") String updated
    ) {
        this.uuid = Objects.requireNonNull(uuid);
        this.version = Objects.requireNonNull(version);
        this.level = Objects.requireNonNull(level);
        this.x = Objects.requireNonNull(x);
        this.y = Objects.requireNonNull(y);
        this.created = Objects.requireNonNull(created);
        this.updated = updated;
    }

    public String getUuid() {
        return uuid;
    }

    public String getVersion() {
        return version;
    }

    public Short getLevel() {
        return level;
    }

    public Short getX() {
        return x;
    }

    public Short getY() {
        return y;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileDocument that = (TileDocument) o;
        return Objects.equals(getUuid(), that.getUuid()) && Objects.equals(getVersion(), that.getVersion()) && Objects.equals(getLevel(), that.getLevel()) && Objects.equals(getX(), that.getX()) && Objects.equals(getY(), that.getY()) && Objects.equals(getCreated(), that.getCreated()) && Objects.equals(getUpdated(), that.getUpdated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getVersion(), getLevel(), getX(), getY(), getCreated(), getUpdated());
    }

    @Override
    public String toString() {
        return "TileDocument{" +
                "uuid='" + uuid + '\'' +
                ", version='" + version + '\'' +
                ", level=" + level +
                ", x=" + x +
                ", y=" + y +
                ", created='" + created + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}
