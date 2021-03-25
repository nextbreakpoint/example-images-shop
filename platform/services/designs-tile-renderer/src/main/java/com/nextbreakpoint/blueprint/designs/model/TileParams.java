package com.nextbreakpoint.blueprint.designs.model;

import java.util.UUID;

public class TileParams {
    private final UUID uuid;
    private final int zoom;
    private final int x;
    private final int y;
    private final int size;

    public TileParams(UUID uuid, int zoom, int x, int y, int size) {
        this.uuid = uuid;
        this.zoom = zoom;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getZoom() {
        return zoom;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }
}
