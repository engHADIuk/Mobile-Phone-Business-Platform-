package Ratings_Reviews.Ratings_Reviews;

/** Holds the logged-in account for the current desktop session. */
public final class Session {
    private static User currentUser;

    private Session() { }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
}
