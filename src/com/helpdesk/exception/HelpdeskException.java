package com.helpdesk.exception;

public class HelpdeskException extends Exception {
    public HelpdeskException(String message) {
        super(message);
    }

    public HelpdeskException(String message, Throwable cause) {
        super(message, cause);
    }
}
