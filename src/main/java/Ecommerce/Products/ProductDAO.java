package Ecommerce.Products;

import java.sql.*;
import java.util.*;

public class ProductDAO {

    private static Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getString("ID"),
            rs.getString("NAME"),
            rs.getString("DESCRIPTION"),
            rs.getDouble("PRICE"),
            rs.getInt("STOCK"),
            rs.getString("IMAGE_URL"),
            rs.getString("STATUS"),
            rs.getString("CATEGORIES")
        );
    }

    private static final String BASE_SELECT =
        "SELECT p.product_id AS ID, p.name AS NAME, p.description AS DESCRIPTION, " +
        "p.price AS PRICE, p.stock_quantity AS STOCK, p.status AS STATUS, " +
        "img.image_url AS IMAGE_URL, " +
        "GROUP_CONCAT(DISTINCT c.category_name) AS CATEGORIES " +
        "FROM Product p " +
        "LEFT JOIN Product_Category pc ON p.product_id = pc.product_id " +
        "LEFT JOIN Category c ON pc.category_id = c.category_id " +
        "LEFT JOIN Product_Image img ON p.product_id = img.product_id AND img.is_main = 1 ";

    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = BASE_SELECT + "GROUP BY p.product_id ORDER BY p.product_id";
        try (Connection c = Controller.connect();
             PreparedStatement stmnt = c.prepareStatement(sql);
             ResultSet rs = stmnt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Product> search(String keyword, String category, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (p.name LIKE ? OR CAST(p.product_id AS TEXT) LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null && !status.equalsIgnoreCase("All")) {
            sql.append("AND p.status = ? ");
            params.add(status);
        }
        sql.append("GROUP BY p.product_id ");
        if (category != null && !category.equalsIgnoreCase("All Categories")) {
            sql.append("HAVING CATEGORIES LIKE ? ");
            params.add("%" + category + "%");
        }
        sql.append("ORDER BY p.product_id");

        List<Product> list = new ArrayList<>();
        try (Connection c = Controller.connect();
             PreparedStatement stmnt = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmnt.setObject(i + 1, params.get(i));
            ResultSet rs = stmnt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean idExists(String id) {
        String sql = "SELECT 1 FROM Product WHERE product_id = ?";
        try (Connection c = Controller.connect();
             PreparedStatement stmnt = c.prepareStatement(sql)) {
            stmnt.setInt(1, Integer.parseInt(id));
            return stmnt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addProduct(Product p, List<String> categoryNames) throws SQLException {
        try (Connection c = Controller.connect()) {
            c.setAutoCommit(false);
            try {
                String sql = "INSERT INTO Product (product_id, name, description, price, stock_quantity, status) VALUES (?,?,?,?,?,?)";
                PreparedStatement stmnt = c.prepareStatement(sql);
                stmnt.setInt(1, Integer.parseInt(p.getId()));
                stmnt.setString(2, p.getName());
                stmnt.setString(3, p.getDescription());
                stmnt.setDouble(4, p.getPrice());
                stmnt.setInt(5, p.getStock());
                stmnt.setString(6, p.getStatus());
                stmnt.executeUpdate();

                if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
                    PreparedStatement img = c.prepareStatement(
                        "INSERT INTO Product_Image (product_id, image_url, is_main) VALUES (?,?,1)");
                    img.setInt(1, Integer.parseInt(p.getId()));
                    img.setString(2, p.getImageUrl());
                    img.executeUpdate();
                }

                linkCategories(c, p.getId(), categoryNames);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static void updateProduct(Product p, List<String> categoryNames) throws SQLException {
        try (Connection c = Controller.connect()) {
            c.setAutoCommit(false);
            try {
                String sql = "UPDATE Product SET name=?, description=?, price=?, stock_quantity=?, status=? WHERE product_id=?";
                PreparedStatement stmnt = c.prepareStatement(sql);
                stmnt.setString(1, p.getName());
                stmnt.setString(2, p.getDescription());
                stmnt.setDouble(3, p.getPrice());
                stmnt.setInt(4, p.getStock());
                stmnt.setString(5, p.getStatus());
                stmnt.setInt(6, Integer.parseInt(p.getId()));
                stmnt.executeUpdate();

                PreparedStatement delCat = c.prepareStatement("DELETE FROM Product_Category WHERE product_id = ?");
                delCat.setInt(1, Integer.parseInt(p.getId()));
                delCat.executeUpdate();
                linkCategories(c, p.getId(), categoryNames);

                if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
                    PreparedStatement delImg = c.prepareStatement(
                        "DELETE FROM Product_Image WHERE product_id = ? AND is_main = 1");
                    delImg.setInt(1, Integer.parseInt(p.getId()));
                    delImg.executeUpdate();

                    PreparedStatement img = c.prepareStatement(
                        "INSERT INTO Product_Image (product_id, image_url, is_main) VALUES (?,?,1)");
                    img.setInt(1, Integer.parseInt(p.getId()));
                    img.setString(2, p.getImageUrl());
                    img.executeUpdate();
                }

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static void deleteProduct(String id) throws SQLException {
        try (Connection c = Controller.connect()) {
            c.setAutoCommit(false);
            try {
                int pid = Integer.parseInt(id);
                PreparedStatement delCat = c.prepareStatement("DELETE FROM Product_Category WHERE product_id = ?");
                delCat.setInt(1, pid);
                delCat.executeUpdate();

                PreparedStatement delImg = c.prepareStatement("DELETE FROM Product_Image WHERE product_id = ?");
                delImg.setInt(1, pid);
                delImg.executeUpdate();

                PreparedStatement delProd = c.prepareStatement("DELETE FROM Product WHERE product_id = ?");
                delProd.setInt(1, pid);
                delProd.executeUpdate();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static List<String> getAllCategoryNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT category_name FROM Category ORDER BY category_name";
        try (Connection c = Controller.connect();
             PreparedStatement stmnt = c.prepareStatement(sql);
             ResultSet rs = stmnt.executeQuery()) {
            while (rs.next()) list.add(rs.getString("category_name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void linkCategories(Connection c, String productId, List<String> categoryNames) throws SQLException {
        if (categoryNames == null) return;
        for (String catName : categoryNames) {
            Integer catId = getOrCreateCategoryId(c, catName);
            PreparedStatement link = c.prepareStatement(
                "INSERT INTO Product_Category (product_id, category_id) VALUES (?,?)");
            link.setInt(1, Integer.parseInt(productId));
            link.setInt(2, catId);
            link.executeUpdate();
        }
    }

    private static Integer getOrCreateCategoryId(Connection c, String name) throws SQLException {
        PreparedStatement find = c.prepareStatement("SELECT category_id FROM Category WHERE category_name = ?");
        find.setString(1, name);
        ResultSet rs = find.executeQuery();
        if (rs.next()) return rs.getInt("category_id");

        PreparedStatement insert = c.prepareStatement(
            "INSERT INTO Category (category_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        insert.setString(1, name);
        insert.executeUpdate();
        ResultSet keys = insert.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }
}