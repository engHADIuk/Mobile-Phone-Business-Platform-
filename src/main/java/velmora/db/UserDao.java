package velmora.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) AS count FROM users";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt("count") == 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count users", e);
        }
    }

    public void insert(String email, String password, String name) {
        String sql = "INSERT INTO users (email, password, name) VALUES (?, ?, ?)";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user " + email, e);
        }
    }

    public boolean checkCredentials(String email, String password) {
        String sql = "SELECT password FROM users WHERE email = ?";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getString("password").equals(password);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user credentials", e);
        }
    }

    public String findName(String email) {
        String sql = "SELECT name FROM users WHERE email = ?";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("name") : "";
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up user name for " + email, e);
        }
    }
}
