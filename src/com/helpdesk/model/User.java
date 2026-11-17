package com.helpdesk.model;

import java.time.LocalDateTime;

/**
 * Abstract User base class (Encapsulation, Inheritance)
 */
public abstract class User {
    protected int id;
    protected String username;
    protected String password;
    protected String fullName;
    protected String email;
    protected String role; // "USER", "AGENT", "ADMIN"
    protected LocalDateTime createdAt;

    public User(int id, String username, String password, String fullName, String email, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String password, String fullName, String email, String role) {
        this(0, username, password, fullName, email, role);
    }

    // Abstract method to be overridden by subclasses (Polymorphism)
    public abstract String getRoleDescription();

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
