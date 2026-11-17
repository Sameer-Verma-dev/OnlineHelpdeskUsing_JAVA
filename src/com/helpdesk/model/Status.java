package com.helpdesk.model;

public enum Status {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public boolean isResolvedOrClosed() {
        return this == RESOLVED || this == CLOSED;
    }
}
