package com.nextbreakpoint.blueprint.designs.operations.update;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextbreakpoint.blueprint.designs.model.Command;

import java.util.Objects;
import java.util.UUID;

public class UpdateDesignCommand extends Command {
    private final String json;

    @JsonCreator
    public UpdateDesignCommand(@JsonProperty("uuid") UUID uuid,
                               @JsonProperty("json") String json,
                               @JsonProperty("timestamp") Long timestamp) {
        super(uuid, timestamp);
        this.json = Objects.requireNonNull(json);
    }

    public String getJson() {
        return json;
    }
}
