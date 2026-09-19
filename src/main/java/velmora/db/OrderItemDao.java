package velmora.db;

import velmora.model.CartItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDao {

    public static class Row {
        public final String productName, color, storage;
        public final int qty;
        public final double unitPrice, lineTotal;

        public Row(String productName, String color, String storage, int qty, double unitPrice, double lineTotal) {
            this.productName = productName;
            this.color = color;
            this.storage = storage;
            this.qty = qty;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }
    }

    public void insertAll(String orderId, List<CartItem> items) {
        String sql = "INSERT INTO order_items (order_id, product_name, color, storage, qty, unit_price, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            for (CartItem item : items) {
                stmt.setString(1, orderId);
                stmt.setString(2, item.getProduct().getName());
                stmt.setString(3, item.getProduct().getColor());
                stmt.setString(4, item.getProduct().getStorage());
                stmt.setInt(5, item.getQty());
                stmt.setDouble(6, item.getProduct().getPrice());
                stmt.setDouble(7, item.getLineTotal());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order items for " + orderId, e);
        }
    }

    public List<Row> findByOrderId(String orderId) {
        List<Row> rows = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Row(
                            rs.getString("product_name"),
                            rs.getString("color"),
                            rs.getString("storage"),
                            rs.getInt("qty"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("line_total")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load order items for " + orderId, e);
        }
        return rows;
    }
}
