package velmora.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CatalogDao {

    public static class Info {
        public final String name, description;
        public Info(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) AS count FROM product_catalog";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt("count") == 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count product_catalog", e);
        }
    }

    public void insert(String name, String description) {
        String sql = "INSERT INTO product_catalog (id, name, description) VALUES (1, ?, ?)";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save product_catalog", e);
        }
    }

    public Info find() {
        String sql = "SELECT * FROM product_catalog WHERE id = 1";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Info(rs.getString("name"), rs.getString("description"));
            }
            return new Info("Velmora X1", "");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load product_catalog", e);
        }
    }
}
