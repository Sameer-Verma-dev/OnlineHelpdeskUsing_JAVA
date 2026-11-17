package com.helpdesk.model;

/**
 * Helpdesk Agent support representative class
 */
public class Agent extends User {
    private String department;
    private int activeTicketCount;

    public Agent(int id, String username, String password, String fullName, String email, String department) {
        super(id, username, password, fullName, email, "AGENT");
        this.department = department;
        this.activeTicketCount = 0;
    }

    public Agent(String username, String password, String fullName, String email, String department) {
        super(username, password, fullName, email, "AGENT");
        this.department = department;
        this.activeTicketCount = 0;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getActiveTicketCount() { return activeTicketCount; }
    public void setActiveTicketCount(int activeTicketCount) { this.activeTicketCount = activeTicketCount; }

    @Override
    public String getRoleDescription() {
        return "Helpdesk Agent - Resolves technical and service requests in " + department;
    }

    @Override
    public String toString() {
        return fullName + " [" + (department != null ? department : "Support") + "]";
    }
}
