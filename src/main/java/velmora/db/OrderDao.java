package velmora.db;

import velmora.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    private Order mapRow(ResultSet rs) throws SQLException {
        String updatedAtText = rs.getString("updated_at");
        LocalDateTime updatedAt = (updatedAtText == null || updatedAtText.isEmpty())
                ? LocalDateTime.now()
                : LocalDateTime.parse(updatedAtText);

        return new Order(
                rs.getString("id"),
                rs.getString("summary"),
                LocalDate.parse(rs.getString("order_date")),
                rs.getDouble("total"),
                Order.Status.valueOf(rs.getString("status")),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                updatedAt,
                rs.getString("shipping_address"),
                rs.getString("card_last4")
        );
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC, id DESC";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load orders", e);
        }
        return orders;
    }

    public List<Order> findByDateRange(LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE 1=1");
        if (from != null) sql.append(" AND order_date >= ?");
        if (to != null) sql.append(" AND order_date <= ?");
        sql.append(" ORDER BY order_date DESC, id DESC");

        List<Order> orders = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (from != null) stmt.setString(paramIndex++, from.toString());
            if (to != null) stmt.setString(paramIndex, to.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load orders by date range", e);
        }
        return orders;
    }

    public void insert(Order order) {
        String sql = "INSERT INTO orders (id, summary, order_date, total, status, customer_name, customer_email, updated_at, shipping_address, card_last4) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setString(1, order.getId());
            stmt.setString(2, order.getSummary());
            stmt.setString(3, order.getDate().toString());
            stmt.setDouble(4, order.getTotal());
            stmt.setString(5, order.getStatus().name());
            stmt.setString(6, order.getCustomerName());
            stmt.setString(7, order.getCustomerEmail());
            stmt.setString(8, order.getUpdatedAt().toString());
            stmt.setString(9, order.getShippingAddress());
            stmt.setString(10, order.getCardLast4());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order " + order.getId(), e);
        }
    }

    public void updateStatus(String orderId, Order.Status status) {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, LocalDateTime.now().toString());
            stmt.setString(3, orderId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order " + orderId, e);
        }
    }

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) AS count FROM orders";
        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            rs.next();
            return rs.getInt("count") == 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count orders", e);
        }
    }
}
