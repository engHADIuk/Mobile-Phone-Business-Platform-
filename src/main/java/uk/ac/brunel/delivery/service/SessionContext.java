package uk.ac.brunel.delivery.service;

import uk.ac.brunel.delivery.model.SessionUser;

import java.util.Optional;

public final class SessionContext {
    private static SessionUser currentUser;

    private SessionContext() {
    }

    public static void login(SessionUser user) {
        currentUser = user;
    }

    public static Optional<SessionUser> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static SessionUser requireUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is logged in");
        }
        return currentUser;
    }

    public static String actorName() {
        return currentUser == null ? "system" : currentUser.username();
    }

    public static void logout() {
        currentUser = null;
    }
}
