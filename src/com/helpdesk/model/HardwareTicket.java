package com.helpdesk.model;

import com.helpdesk.exception.InvalidTicketException;

/**
 * Specialized Hardware Incident Ticket
 * Handles hardware breakdown, equipment repair, device malfunction.
 */
public class HardwareTicket extends Ticket {
    private String deviceType;   // e.g., Laptop, Printer, Monitor, Server
    private String serialNumber; // Device S/N
    private boolean underWarranty;

    public HardwareTicket(int id, String ticketCode, int userId, String customerName, 
                          String title, String description, Priority priority,
                          String deviceType, String serialNumber, boolean underWarranty) 
                          throws InvalidTicketException {
        super(id, ticketCode, userId, customerName, title, description, priority);
        if (deviceType == null || deviceType.trim().isEmpty()) {
            throw new InvalidTicketException("Hardware ticket requires a valid Device Type.");
        }
        this.deviceType = deviceType;
        this.serialNumber = serialNumber != null ? serialNumber : "N/A";
        this.underWarranty = underWarranty;
    }

    @Override
    public String getTicketCategory() {
        return "HARDWARE";
    }

    @Override
    public int calculateSlaHours() {
        // Hardware usually requires 24 - 48 hours, expedited if Critical
        switch (priority) {
            case CRITICAL: return 4;
            case HIGH: return 12;
            case MEDIUM: return 24;
            case LOW:
            default: return 48;
        }
    }

    @Override
    public String getSpecificDetails() {
        return "Device: " + deviceType + " | S/N: " + serialNumber + " | Warranty: " + (underWarranty ? "Yes" : "No");
    }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public boolean isUnderWarranty() { return underWarranty; }
    public void setUnderWarranty(boolean underWarranty) { this.underWarranty = underWarranty; }
}
