package velmora.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReturnedItemDao {

    public static class Row {
        public final String productName, color, storage;
        public final int qty;

        public Row(String productName, String color, String storage, int qty) {
            this.productName = productName;
            this.color = color;
            this.storage = storage;
            this.qty = qty;
        }
    }

    public void insert(int returnId, Row row) {
        String sql = "INSERT INTO returned_items (return_id, product_name, color, storage, qty) VALUES (?, ?, ?, ?, ?)";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setInt(1, returnId);
            stmt.setString(2, row.productName);
            stmt.setString(3, row.color);
            stmt.setString(4, row.storage);
            stmt.setInt(5, row.qty);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save returned item for return " + returnId, e);
        }
    }

    public List<Row> findByReturnId(int returnId) {
        List<Row> rows = new ArrayList<>();
        String sql = "SELECT * FROM returned_items WHERE return_id = ?";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setInt(1, returnId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Row(
                            rs.getString("product_name"),
                            rs.getString("color"),
                            rs.getString("storage"),
                            rs.getInt("qty")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load returned items for return " + returnId, e);
        }
        return rows;
    }
}
