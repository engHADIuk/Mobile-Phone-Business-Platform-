package velmora.db;

import velmora.model.CartItem;
import velmora.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CartDao {

    public List<CartItem> findAll() {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT * FROM cart_items";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getString("name"),
                        rs.getString("color"),
                        rs.getString("storage"),
                        rs.getDouble("price"),
                        rs.getDouble("old_price"),
                        rs.getInt("stock")
                );
                items.add(new CartItem(product, rs.getInt("qty")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load cart", e);
        }
        return items;
    }

    public void replaceAll(List<CartItem> items) {
        String deleteSql = "DELETE FROM cart_items";
        String insertSql = "INSERT INTO cart_items (name, color, storage, price, old_price, stock, qty) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Database.connect()) {
            try (Statement del = c.createStatement()) {
                del.execute(deleteSql);
            }
            try (PreparedStatement stmt = c.prepareStatement(insertSql)) {
                for (CartItem item : items) {
                    Product p = item.getProduct();
                    stmt.setString(1, p.getName());
                    stmt.setString(2, p.getColor());
                    stmt.setString(3, p.getStorage());
                    stmt.setDouble(4, p.getPrice());
                    stmt.setDouble(5, p.getOldPrice());
                    stmt.setInt(6, p.getStock());
                    stmt.setInt(7, item.getQty());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save cart", e);
        }
    }
}
