package com.helpdesk.model;

/**
 * Customer / Student class extending User
 */
public class Customer extends User {
    private String contactNumber;

    public Customer(int id, String username, String password, String fullName, String email, String contactNumber) {
        super(id, username, password, fullName, email, "USER");
        this.contactNumber = contactNumber;
    }

    public Customer(String username, String password, String fullName, String email, String contactNumber) {
        super(username, password, fullName, email, "USER");
        this.contactNumber = contactNumber;
    }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    @Override
    public String getRoleDescription() {
        return "Customer / User lodging service complaints";
    }
}
