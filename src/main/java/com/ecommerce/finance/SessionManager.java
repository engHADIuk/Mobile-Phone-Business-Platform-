package com.ecommerce.finance;

public class SessionManager {
    private static SessionManager instance;
    private String sessionToken;
    private String currentUser;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String token) { this.sessionToken = token; }
    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String user) { this.currentUser = user; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() { return "admin".equals(role); }
    public boolean isEmployee() { return "employee".equals(role); }

    public void clearSession() {
        sessionToken = null;
        currentUser = null;
        role = null;
    }
}