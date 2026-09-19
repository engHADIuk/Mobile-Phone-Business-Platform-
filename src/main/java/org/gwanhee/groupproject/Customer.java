package org.gwanhee.groupproject;

public class Customer {
    private String name;
    private String logInID;
    private String password;
    private String address;
    private String email;

    public Customer(String name, String email, String loginId, String password, String address) {
        this.name = name.trim();
        this.logInID = loginId;
        this.password = password;
        this.address = address.trim();
        this.email = email.trim();
    }

    public String getName() {
        return name;
    }

    public String getLogInID() {
        return logInID;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail(){ return email; }

    @Override
    public String toString() {
        return name + " (" + logInID + ")";
    }
}
