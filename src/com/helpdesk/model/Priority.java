package com.helpdesk.model;

public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public Priority nextEscalationLevel() {
        switch (this) {
            case LOW: return MEDIUM;
            case MEDIUM: return HIGH;
            case HIGH:
            case CRITICAL:
            default:
                return CRITICAL;
        }
    }
}
