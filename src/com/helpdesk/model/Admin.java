package com.helpdesk.model;

/**
 * System Administrator class
 */
public class Admin extends User {
    public Admin(int id, String username, String password, String fullName, String email) {
        super(id, username, password, fullName, email, "ADMIN");
    }

    public Admin(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, "ADMIN");
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator - Full oversight, ticket reassignment, and reports";
    }
}
