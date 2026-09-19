package velmora.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class ReturnDao {

    public int insert(String orderId, String reason) {
        String sql = "INSERT INTO returns (order_id, return_date, reason) VALUES (?, ?, ?)";

        try (Connection c = Database.connect();
             PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, orderId);
            stmt.setString(2, LocalDate.now().toString());
            stmt.setString(3, reason);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new RuntimeException("Insert succeeded but no generated id was returned");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to log return for order " + orderId, e);
        }
    }
}
