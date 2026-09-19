package com.ecommerce.finance;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String role;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isEmployee() { return "EMPLOYEE".equalsIgnoreCase(role); }
}