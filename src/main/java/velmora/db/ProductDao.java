package velmora.db;

import velmora.model.StorageOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public List<StorageOption> findAll() {
        List<StorageOption> options = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                options.add(new StorageOption(
                        rs.getString("storage"),
                        rs.getDouble("price"),
                        rs.getDouble("old_price"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load products", e);
        }
        return options;
    }

    public void insert(StorageOption option) {
        String sql = "INSERT INTO products (storage, price, old_price, stock) VALUES (?, ?, ?, ?)";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setString(1, option.getStorage());
            stmt.setDouble(2, option.getPrice());
            stmt.setDouble(3, option.getOldPrice());
            stmt.setInt(4, option.getStock());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save product " + option.getStorage(), e);
        }
    }

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) AS count FROM products";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            rs.next();
            return rs.getInt("count") == 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count products", e);
        }
    }

    public void adjustStock(String storage, int delta) {
        String sql = "UPDATE products SET stock = stock + ? WHERE storage = ?";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setInt(1, delta);
            stmt.setString(2, storage);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to adjust stock for " + storage, e);
        }
    }

    public int getStock(String storage) {
        String sql = "SELECT stock FROM products WHERE storage = ?";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setString(1, storage);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("stock") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read stock for " + storage, e);
        }
    }
}
