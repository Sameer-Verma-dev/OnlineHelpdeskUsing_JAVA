package com.helpdesk.model;

/**
 * Assignable Interface
 * Represents entities that can be assigned to a support Agent.
 */
public interface Assignable {
    void assignTo(Agent agent);
    Agent getAssignedAgent();
    boolean isAssigned();
    void unassign();
}
