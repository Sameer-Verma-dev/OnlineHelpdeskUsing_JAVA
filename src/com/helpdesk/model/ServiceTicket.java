package com.helpdesk.model;

import com.helpdesk.exception.InvalidTicketException;

/**
 * Specialized Service Request Ticket
 * Handles network access, VPN, Wi-Fi, email, lab resources.
 */
public class ServiceTicket extends Ticket {
    private String serviceType;       // e.g., Network, VPN, Account Access, Email
    private String departmentRequested;

    public ServiceTicket(int id, String ticketCode, int userId, String customerName, 
                         String title, String description, Priority priority,
                         String serviceType, String departmentRequested) 
                         throws InvalidTicketException {
        super(id, ticketCode, userId, customerName, title, description, priority);
        if (serviceType == null || serviceType.trim().isEmpty()) {
            throw new InvalidTicketException("Service ticket requires a specific Service Type.");
        }
        this.serviceType = serviceType.trim();
        this.departmentRequested = departmentRequested != null ? departmentRequested.trim() : "IT Support";
    }

    @Override
    public String getTicketCategory() {
        return "SERVICE";
    }

    @Override
    public int calculateSlaHours() {
        switch (priority) {
            case CRITICAL: return 3;
            case HIGH: return 6;
            case MEDIUM: return 12;
            case LOW:
            default: return 24;
        }
    }

    @Override
    public String getSpecificDetails() {
        return "Service: " + serviceType + " | Target Dept: " + departmentRequested;
    }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getDepartmentRequested() { return departmentRequested; }
    public void setDepartmentRequested(String departmentRequested) { this.departmentRequested = departmentRequested; }
}
