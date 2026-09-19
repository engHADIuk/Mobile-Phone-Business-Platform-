package velmora.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDao {

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) AS count FROM admins";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt("count") == 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count admins", e);
        }
    }

    public void insert(String username, String password) {
        String sql = "INSERT INTO admins (username, password) VALUES (?, ?)";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save admin " + username, e);
        }
    }

    public boolean checkCredentials(String username, String password) {
        String sql = "SELECT password FROM admins WHERE username = ?";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getString("password").equals(password);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check admin credentials", e);
        }
    }
}
