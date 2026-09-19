package velmora.model;

public class Session {

    private static String name;
    private static String email;

    public static void login(String loggedInName, String loggedInEmail) {
        name = loggedInName;
        email = loggedInEmail;
    }

    public static boolean isLoggedIn() {
        return email != null;
    }

    public static String getName() {
        return name == null ? "" : name;
    }

    public static String getEmail() {
        return email == null ? "" : email;
    }

    public static void logout() {
        name = null;
        email = null;
    }
}
