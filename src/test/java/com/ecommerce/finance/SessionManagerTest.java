package com.ecommerce.finance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    @Test
    void testSingleton() {
        SessionManager s1 = SessionManager.getInstance();
        SessionManager s2 = SessionManager.getInstance();
        assertSame(s1, s2, "SessionManager should be a singleton");
    }

    @Test
    void testSetAndGetRole() {
        SessionManager sm = SessionManager.getInstance();
        sm.setRole("admin");
        assertEquals("admin", sm.getRole());
        assertTrue(sm.isAdmin());
        assertFalse(sm.isEmployee());

        sm.setRole("employee");
        assertEquals("employee", sm.getRole());
        assertFalse(sm.isAdmin());
        assertTrue(sm.isEmployee());

        sm.setRole("guest");
        assertFalse(sm.isAdmin());
        assertFalse(sm.isEmployee());
    }

    @Test
    void testRoleTransition() {
        SessionManager sm = SessionManager.getInstance();
        sm.setRole("admin");
        assertTrue(sm.isAdmin());
        sm.setRole("employee");
        assertTrue(sm.isEmployee());
        assertFalse(sm.isAdmin());
    }

    @Test
    void testSetAndGetCurrentUser() {
        SessionManager sm = SessionManager.getInstance();
        sm.setCurrentUser("testuser");
        assertEquals("testuser", sm.getCurrentUser());
    }

    @Test
    void testSetAndGetSessionToken() {
        SessionManager sm = SessionManager.getInstance();
        sm.setSessionToken("token123");
        assertEquals("token123", sm.getSessionToken());
    }

    @Test
    void testClearSession() {
        SessionManager sm = SessionManager.getInstance();
        sm.setSessionToken("token123");
        sm.setCurrentUser("user");
        sm.setRole("admin");

        sm.clearSession();

        assertNull(sm.getSessionToken());
        assertNull(sm.getCurrentUser());
        assertNull(sm.getRole());
        assertFalse(sm.isAdmin());
        assertFalse(sm.isEmployee());
    }

    @Test
    void testClearSessionWhenAlreadyEmpty() {
        SessionManager sm = SessionManager.getInstance();
        sm.clearSession(); // should not throw
        assertNull(sm.getSessionToken());
        assertNull(sm.getCurrentUser());
        assertNull(sm.getRole());
    }
}