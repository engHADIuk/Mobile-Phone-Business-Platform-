package org.gwanhee.groupproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    public static Customer findByLoginId(String loginID){
        String sql = "SELECT * FROM Accounts WHERE LOGINID = ?";

        try(Connection conn = DataBaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, loginID);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                String name = rs.getString("NAME");
                String email = rs.getString("EMAIL");
                String id = rs.getString("LOGINID");
                String password = rs.getString("PASSWORD");
                String address = rs.getString("ADDRESS");

                return new Customer(name, email, id, password, address);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", "Could not check login ID.");
        }

        return null;
    }

    public static boolean login(String loginId, String password) {
        String sql = "SELECT * FROM Accounts WHERE LOGINID = ? AND PASSWORD = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loginId);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", "Could not check login.");
            return false;
        }
    }

    public static boolean addCustomer(Customer customer) {
        String sql = """
            INSERT INTO Accounts (EMAIL, PASSWORD, ADDRESS, NAME, LOGINID, Active, ContactNo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getEmail());
            stmt.setString(2, customer.getPassword());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getName());
            stmt.setString(5, customer.getLogInID());
            stmt.setInt(6, 1);
            stmt.setString(7, "");

            System.out.println("Customer Listed");

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
           e.printStackTrace();
           AlertUtil.errorAlert("Database Error", e.getMessage());
            return false;
        }
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT * FROM Accounts WHERE EMAIL = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", "Could not check email.");
            return false;
        }
    }

    public static boolean deactivateCustomer(String loginId) {
        String sql = "UPDATE Accounts SET Active = 0 WHERE LOGINID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loginId);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
            return false;
        }
    }

    public static boolean deleteCustomer(String loginId) {
        String sql = "DELETE FROM Accounts WHERE LOGINID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loginId);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
            return false;
        }
    }

    public static boolean activateCustomer(String loginId) {
        String sql = "UPDATE Accounts SET Active = 1 WHERE LOGINID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loginId);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
            return false;
        }
    }

    public static boolean activate(String loginId, String password) {
        String sql = "SELECT * FROM Accounts WHERE LOGINID = ? AND PASSWORD = ? AND Active = 0";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loginId);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", "Could not check login.");
            return false;
        }
    }

    public static boolean isAccountActive(String loginId) {
        String sql = "SELECT Active FROM Accounts WHERE LOGINID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loginId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("Active") == 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", "Could not check account status.");
        }

        return false;
    }

    public static boolean updateAddress(String loginId, String address) {
        String sql = "UPDATE Accounts SET ADDRESS = ? WHERE LOGINID = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, address);
            stmt.setString(2, loginId);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.errorAlert("Database Error", e.getMessage());
            return false;
        }
    }
}
