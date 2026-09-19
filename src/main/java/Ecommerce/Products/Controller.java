package Ecommerce.Products;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Controller {

    private static final String DRIVER = "jdbc:sqlite";
    // Was "E:/velmora_products.db/SQLite database files.db" - an absolute path on the
    // original machine. The same database file now ships in the project folder, so it is
    // resolved relative to wherever the project happens to sit. This is the only line of
    // the Products component that differs from the version supplied.
    private static final String DB = resolveDatabaseFile();
    private static final String URL = DRIVER + ":" + DB;

    /** Finds "SQLite database files.db" in the working directory, or the folder above it. */
    private static String resolveDatabaseFile() {
        String fileName = "SQLite database files.db";
        java.nio.file.Path here = java.nio.file.Paths.get(System.getProperty("user.dir"), fileName);
        if (java.nio.file.Files.exists(here)) {
            return here.toAbsolutePath().toString();
        }
        java.nio.file.Path above = java.nio.file.Paths.get(System.getProperty("user.dir"), "..", fileName);
        if (java.nio.file.Files.exists(above)) {
            return above.toAbsolutePath().normalize().toString();
        }
        return here.toAbsolutePath().toString();
    }

    public static Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        System.out.println("Made connection to db");
        return c;
    }

    /**
     * Returns null on successful login.
     * Returns a user-facing error message string if login fails.
     * adminPortal = true enforces the @velmora.com domain check (FR-01).
     */
    public static String checkLogin(String email, String password, boolean adminPortal) {

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return "Email and password are required.";
        }

        if (adminPortal && !email.toLowerCase().endsWith("@velmora.com")) {
            return "Admin access requires a @velmora.com email address.";
        }

        try (Connection c = connect()) {
            String sql = "SELECT ROLE FROM ACCOUNTS WHERE EMAIL = ? AND PASSWORD = ?";
            PreparedStatement stmnt = c.prepareStatement(sql);
            stmnt.setString(1, email);
            stmnt.setString(2, password);
            ResultSet rs = stmnt.executeQuery();

            if (!rs.next()) {
                return "Incorrect email or password.";
            }

            String role = rs.getString("ROLE");
            if (adminPortal && !"ADMIN".equalsIgnoreCase(role)) {
                return "This account is not an administrator account.";
            }
            if (!adminPortal && !"CUSTOMER".equalsIgnoreCase(role)) {
                return "Please use the Admin Login for this account.";
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }
}