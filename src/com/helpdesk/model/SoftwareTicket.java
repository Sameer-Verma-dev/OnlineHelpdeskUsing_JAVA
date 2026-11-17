package com.helpdesk.model;

import com.helpdesk.exception.InvalidTicketException;

/**
 * Specialized Software Incident Ticket
 * Handles application bugs, OS issues, installation, crash reports.
 */
public class SoftwareTicket extends Ticket {
    private String softwareName;
    private String softwareVersion;
    private String operatingSystem;

    public SoftwareTicket(int id, String ticketCode, int userId, String customerName, 
                          String title, String description, Priority priority,
                          String softwareName, String softwareVersion, String operatingSystem) 
                          throws InvalidTicketException {
        super(id, ticketCode, userId, customerName, title, description, priority);
        if (softwareName == null || softwareName.trim().isEmpty()) {
            throw new InvalidTicketException("Software ticket requires software name.");
        }
        this.softwareName = softwareName.trim();
        this.softwareVersion = softwareVersion != null ? softwareVersion.trim() : "v1.0";
        this.operatingSystem = operatingSystem != null ? operatingSystem.trim() : "Windows";
    }

    @Override
    public String getTicketCategory() {
        return "SOFTWARE";
    }

    @Override
    public int calculateSlaHours() {
        // Software SLA hours based on priority
        switch (priority) {
            case CRITICAL: return 2;
            case HIGH: return 8;
            case MEDIUM: return 16;
            case LOW:
            default: return 24;
        }
    }

    @Override
    public String getSpecificDetails() {
        return "Software: " + softwareName + " (" + softwareVersion + ") | OS: " + operatingSystem;
    }

    public String getSoftwareName() { return softwareName; }
    public void setSoftwareName(String softwareName) { this.softwareName = softwareName; }

    public String getSoftwareVersion() { return softwareVersion; }
    public void setSoftwareVersion(String softwareVersion) { this.softwareVersion = softwareVersion; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }
}
