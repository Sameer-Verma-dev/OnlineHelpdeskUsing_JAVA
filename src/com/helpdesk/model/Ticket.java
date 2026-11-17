package com.helpdesk.model;

import com.helpdesk.exception.InvalidTicketException;
import java.time.LocalDateTime;

/**
 * Abstract base class Ticket implementing Assignable interface.
 * Demonstrates Abstraction, Polymorphism, and Interface implementation.
 */
public abstract class Ticket implements Assignable {
    protected int id;
    protected String ticketCode; // e.g. "TCK-1001"
    protected int userId;
    protected String customerName;
    protected String title;
    protected String description;
    protected Priority priority;
    protected Status status;
    protected Integer assignedAgentId;
    protected String assignedAgentName;
    protected Agent assignedAgent;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected boolean escalated;
    protected String resolutionNotes;

    public Ticket(int id, String ticketCode, int userId, String customerName, String title, 
                  String description, Priority priority) throws InvalidTicketException {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidTicketException("Ticket title cannot be empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidTicketException("Ticket description cannot be empty.");
        }
        if (priority == null) {
            throw new InvalidTicketException("Ticket priority must be specified.");
        }

        this.id = id;
        this.ticketCode = ticketCode;
        this.userId = userId;
        this.customerName = customerName;
        this.title = title.trim();
        this.description = description.trim();
        this.priority = priority;
        this.status = Status.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.escalated = false;
        this.resolutionNotes = "";
    }

    // Polymorphic ticket methods
    public abstract String getTicketCategory();
    public abstract int calculateSlaHours();
    public abstract String getSpecificDetails();

    // Assignable interface implementation
    @Override
    public void assignTo(Agent agent) {
        if (agent != null) {
            this.assignedAgent = agent;
            this.assignedAgentId = agent.getId();
            this.assignedAgentName = agent.getFullName();
            if (this.status == Status.OPEN) {
                this.status = Status.ASSIGNED;
            }
            this.updatedAt = LocalDateTime.now();
        }
    }

    @Override
    public Agent getAssignedAgent() {
        return this.assignedAgent;
    }

    @Override
    public boolean isAssigned() {
        return this.assignedAgentId != null && this.assignedAgentId > 0;
    }

    @Override
    public void unassign() {
        this.assignedAgent = null;
        this.assignedAgentId = null;
        this.assignedAgentName = null;
        if (this.status == Status.ASSIGNED) {
            this.status = Status.OPEN;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Status transition with validation
    public void updateStatus(Status newStatus) throws InvalidTicketException {
        if (newStatus == null) {
            throw new InvalidTicketException("New status cannot be null.");
        }
        if (this.status == Status.CLOSED && newStatus != Status.CLOSED) {
            throw new InvalidTicketException("Closed tickets cannot be re-opened directly without admin approval.");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { 
        this.priority = priority; 
        this.updatedAt = LocalDateTime.now();
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Integer assignedAgentId) { this.assignedAgentId = assignedAgentId; }

    public String getAssignedAgentName() { return assignedAgentName; }
    public void setAssignedAgentName(String assignedAgentName) { this.assignedAgentName = assignedAgentName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isEscalated() { return escalated; }
    public void setEscalated(boolean escalated) { 
        this.escalated = escalated; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { 
        this.resolutionNotes = resolutionNotes; 
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "[" + ticketCode + "] " + title + " (" + priority + " | " + status + ")";
    }
}
