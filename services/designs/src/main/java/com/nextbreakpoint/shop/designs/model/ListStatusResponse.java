package com.nextbreakpoint.shop.designs.model;

import java.util.List;
import java.util.Objects;

public class ListStatusResponse {
    private final List<Status> values;

    public ListStatusResponse(List<Status> values) {
        this.values = Objects.requireNonNull(values);
    }

    public List<Status> getValues() {
        return values;
    }
}