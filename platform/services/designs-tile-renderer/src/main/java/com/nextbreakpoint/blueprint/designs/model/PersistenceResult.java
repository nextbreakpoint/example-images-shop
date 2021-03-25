package com.nextbreakpoint.blueprint.designs.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PersistenceResult<T> {
    private final UUID uuid;
    private final T value;

    public PersistenceResult(UUID uuid, T value) {
        this.uuid = Objects.requireNonNull(uuid);
        this.value = value;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }
}
