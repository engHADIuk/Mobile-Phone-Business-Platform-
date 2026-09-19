package org.gwanhee.groupproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PaymentMethodDAO {

    public static boolean saveCard(String customerId,
                                   String cardHolderName,
                                   String cardNumber,
                                   String expiryDate,
                                   String cvc) {

        String sql = """
                INSERT INTO PaymentMethod
                (paymentMethodId, customerId, cardHolderName, cardNumber, expiryDate, cvc)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String paymentMethodId = "PM" + System.currentTimeMillis();

            stmt.setString(1, paymentMethodId);
            stmt.setString(2, customerId);
            stmt.setString(3, cardHolderName);
            stmt.setString(4, cardNumber);
            stmt.setString(5, expiryDate);
            stmt.setString(6, cvc);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
            return false;
        }
    }

    public static boolean deleteCardById(String paymentMethodId) {
        String sql = "DELETE FROM PaymentMethod WHERE paymentMethodId = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paymentMethodId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
            return false;
        }
    }

    public static ArrayList<PaymentCard> getCardsByCustomerId(String customerId) {
        ArrayList<PaymentCard> cards = new ArrayList<>();

        String sql = """
                SELECT paymentMethodId, cardHolderName, cardNumber, expiryDate, cvc
                FROM PaymentMethod
                WHERE customerId = ?
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String paymentMethodId = rs.getString("paymentMethodId");
                String cardHolderName = rs.getString("cardHolderName");
                String cardNumber = rs.getString("cardNumber");
                String expiryDate = rs.getString("expiryDate");
                String cvc = rs.getString("cvc");

                PaymentCard card = new PaymentCard(
                        paymentMethodId,
                        cardHolderName,
                        cardNumber,
                        expiryDate,
                        cvc
                );

                cards.add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
        }

        return cards;
    }

    public static String getPaymentSummary(String customerId) {
        String sql = "SELECT cardNumber FROM PaymentMethod WHERE customerId = ? LIMIT 1";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String cardNumber = rs.getString("cardNumber");

                if (cardNumber != null && cardNumber.length() >= 4) {
                    String lastFour = cardNumber.substring(cardNumber.length() - 4);
                    return "Card ending in " + lastFour;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
        }

        return "No payment method added";
    }
}