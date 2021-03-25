package com.nextbreakpoint.blueprint.designs.model;

public class ControllerResult {
    private final Throwable error;

    public ControllerResult() {
        this(null);
    }

    public ControllerResult(Throwable error) {
        this.error = error;
    }

    public boolean successful() {
        return error == null;
    }

    public Throwable getError() {
        return error;
    }
}
