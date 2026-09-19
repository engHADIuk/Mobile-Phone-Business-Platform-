package org.gwanhee.groupproject;

public class Session {

    private static String currentLoginId;

    private static String selectedCustomerId;

    private static boolean admin = false;


    public static void setCurrentLoginId(String loginId) {
        currentLoginId = loginId;

        if (!admin) {
            selectedCustomerId = loginId;
        }
    }

    public static String getCurrentLoginId() {

        if (admin && selectedCustomerId != null) {
            return selectedCustomerId;
        }

        return currentLoginId;
    }

    public static boolean isLoggedIn() {
        return currentLoginId != null
                && !currentLoginId.isEmpty();
    }

    public static void setAdminLogin(String adminId) {
        currentLoginId = adminId;
        selectedCustomerId = null;
        admin = true;
    }

    public static void clear() {
        currentLoginId = null;
        selectedCustomerId = null;
        admin = false;
    }
}