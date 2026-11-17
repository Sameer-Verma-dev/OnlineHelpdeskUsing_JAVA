package com.helpdesk.exception;

/**
 * Custom Exception for Ticket validation and business rule violations.
 */
public class InvalidTicketException extends Exception {
    public InvalidTicketException(String message) {
        super(message);
    }

    public InvalidTicketException(String message, Throwable cause) {
        super(message, cause);
    }
}
