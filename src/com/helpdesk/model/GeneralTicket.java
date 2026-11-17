package com.helpdesk.model;

import com.helpdesk.exception.InvalidTicketException;

/**
 * General Incident Ticket
 * Handles campus facility and general service inquiries.
 */
public class GeneralTicket extends Ticket {
    private String location; // e.g., Campus Block, Lab, Floor

    public GeneralTicket(int id, String ticketCode, int userId, String customerName, 
                         String title, String description, Priority priority,
                         String location) 
                         throws InvalidTicketException {
        super(id, ticketCode, userId, customerName, title, description, priority);
        this.location = location != null ? location.trim() : "General";
    }

    @Override
    public String getTicketCategory() {
        return "GENERAL";
    }

    @Override
    public int calculateSlaHours() {
        switch (priority) {
            case CRITICAL: return 6;
            case HIGH: return 12;
            case MEDIUM: return 24;
            case LOW:
            default: return 48;
        }
    }

    @Override
    public String getSpecificDetails() {
        return "Location/Area: " + location;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
