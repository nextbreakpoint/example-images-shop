package com.nextbreakpoint.shop.designs.model;

import java.util.List;
import java.util.Objects;

public class ListDesignsResponse {
    private final List<String> uuids;

    public ListDesignsResponse(List<String> uuids) {
        this.uuids = Objects.requireNonNull(uuids);
    }

    public List<String> getUuids() {
        return uuids;
    }
}
