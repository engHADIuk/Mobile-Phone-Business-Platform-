package com.ecommerce.finance.services;

import com.ecommerce.finance.DashboardController;
import com.ecommerce.finance.MarginAnalysisController;
import com.ecommerce.finance.KPIController;
import com.ecommerce.finance.VATController;
import com.ecommerce.finance.FilterController;
import com.ecommerce.finance.ExportController;
import com.ecommerce.finance.ClientInsightController;
import com.ecommerce.finance.AnomalyDetectionController;
import com.ecommerce.finance.StatementController;
import com.ecommerce.finance.SpendingOverviewController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Singleton database manager for the finance reporting application.
 * Uses SQLite (or MySQL) – default URL points to a local SQLite file.
 * For testing, call setDbUrl("jdbc:sqlite::memory:") before initializing.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private static String dbUrl = "jdbc:sqlite:ecommerce.db"; // can be overridden for testing

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Override the database URL – useful for testing with an in‑memory database.
     * Must be called BEFORE initializeDatabase() and getConnection().
     */
    public void setDbUrl(String url) {
        dbUrl = url;
        closeConnection(); // force reconnection with new URL
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl);
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database: " + e.getMessage());
            }
        }
        connection = null;
    }

    // ---------- Database Initialization ----------
    public void initializeDatabase() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT DEFAULT 'employee'," +
                "created_date DATE)");

        stmt.execute("INSERT OR IGNORE INTO users (username, password, role, created_date) " +
                     "VALUES ('hadi7@email.com', 'hadi7', 'admin', date('now'))");
        stmt.execute("INSERT OR IGNORE INTO users (username, password, role, created_date) " +
                     "VALUES ('leo@email.com', 'leo10', 'employee', date('now'))");

        stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "cost REAL NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id TEXT UNIQUE," +
                "name TEXT," +
                "email TEXT)");

        stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id TEXT UNIQUE NOT NULL," +
                "customer_id TEXT," +
                "product_id INTEGER," +
                "quantity INTEGER NOT NULL," +
                "price REAL NOT NULL," +
                "status TEXT NOT NULL," +
                "order_date DATE NOT NULL)");

        stmt.close();
        System.out.println("Database initialized successfully.");
    }

    /**
     * Populates products/customers/orders with realistic sample data the first
     * time the app runs (skips silently if data already exists). This is what
     * lets Filter/Export/KPI/Margin/VAT/Anomaly/Client Insights screens query a
     * real SQLite database instead of always falling back to per-screen dummy data.
     */
    public void seedIfEmpty() throws SQLException {
        Connection conn = getConnection();
        Statement check = conn.createStatement();
        ResultSet rs = check.executeQuery("SELECT COUNT(*) AS c FROM products");
        boolean empty = !rs.next() || rs.getInt("c") == 0;
        rs.close();
        check.close();
        if (!empty) return;

        String[][] productData = {
            {"iPhone 15 Pro", "Electronics", "1299.00", "1100.00"},
            {"MacBook Pro M3", "Electronics", "2499.00", "2100.00"},
            {"AirPods Pro 2", "Accessories", "249.00", "180.00"},
            {"iPad Air 5", "Electronics", "799.00", "650.00"},
            {"Apple Watch Ultra", "Electronics", "899.00", "700.00"},
            {"Mac Mini M2", "Electronics", "799.00", "650.00"},
            {"Apple TV 4K", "Electronics", "199.00", "150.00"},
            {"AirTag 4-Pack", "Accessories", "99.00", "70.00"},
            {"MagSafe Charger", "Accessories", "39.00", "25.00"},
            {"Apple Pencil 2", "Accessories", "129.00", "90.00"},
            {"Studio Display", "Electronics", "1599.00", "1300.00"},
            {"Mac Studio", "Electronics", "1999.00", "1600.00"},
            {"HomePod Mini", "Software", "99.00", "70.00"},
            {"Software License", "Software", "299.00", "50.00"},
            {"Cloud Services", "Services", "499.00", "120.00"}
        };
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO products (name, category, price, cost) VALUES (?, ?, ?, ?)");
        for (String[] p : productData) {
            pstmt.setString(1, p[0]);
            pstmt.setString(2, p[1]);
            pstmt.setDouble(3, Double.parseDouble(p[2]));
            pstmt.setDouble(4, Double.parseDouble(p[3]));
            pstmt.executeUpdate();
        }
        pstmt.close();

        String[] customerIds = {"C001", "C002", "C003", "C004", "C005", "C006", "C007", "C008", "C009", "C010"};
        String[] customerNames = {"Alice Johnson", "Bob Smith", "Carol White", "David Brown", "Eva Green",
                                   "Frank Black", "Grace Lee", "Henry King", "Ivy Hall", "Jack Rose"};
        pstmt = conn.prepareStatement("INSERT INTO customers (customer_id, name, email) VALUES (?, ?, ?)");
        for (int i = 0; i < customerIds.length; i++) {
            pstmt.setString(1, customerIds[i]);
            pstmt.setString(2, customerNames[i]);
            pstmt.setString(3, customerIds[i].toLowerCase() + "@email.com");
            pstmt.executeUpdate();
        }
        pstmt.close();

        Random rand = new Random(42);
        String[] statuses = {"Completed", "Pending", "Shipped", "Delivered", "Refunded"};
        // Anchored to "today" (not a fixed year) and shifted so the spread covers a
        // little into next month too — the date-range picker always opens on the
        // current + next month, so both need real data by default.
        LocalDate start = LocalDate.now().minusDays(300);
        pstmt = conn.prepareStatement(
                "INSERT INTO orders (order_id, customer_id, product_id, quantity, price, status, order_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");
        for (int i = 0; i < 150; i++) {
            String orderId = "TXN-" + (1000 + i);
            String customerId = customerIds[rand.nextInt(customerIds.length)];
            int productId = 1 + rand.nextInt(productData.length);
            int quantity = 1 + rand.nextInt(3);
            double basePrice = Double.parseDouble(productData[productId - 1][2]);
            double price = Math.round(basePrice * quantity * 100.0) / 100.0;
            String status = statuses[rand.nextInt(statuses.length)];
            LocalDate date = start.plusDays(rand.nextInt(365));
            pstmt.setString(1, orderId);
            pstmt.setString(2, customerId);
            pstmt.setInt(3, productId);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, price);
            pstmt.setString(6, status);
            pstmt.setString(7, date.toString());
            pstmt.executeUpdate();
        }
        pstmt.close();

        System.out.println("Database seeded with sample products/customers/orders.");
    }

    // ---------- User Authentication ----------
    public String getUserRole(String username) throws SQLException {
        String query = "SELECT role FROM users WHERE username = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        String role = rs.next() ? rs.getString("role") : "employee";
        rs.close();
        pstmt.close();
        return role;
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();
        boolean found = rs.next();
        rs.close();
        pstmt.close();
        return found;
    }

    // ---------- DASHBOARD ----------
    public List<DashboardController.Transaction> loadDashboardTransactions() throws SQLException {
        List<DashboardController.Transaction> list = new ArrayList<>();
        String query = "SELECT o.order_id, p.name as product, o.price, o.status, o.order_date " +
                       "FROM orders o JOIN products p ON o.product_id = p.id " +
                       "ORDER BY o.order_date DESC LIMIT 5";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String name = rs.getString("product") + " Purchase";
            String category = "Revenue";
            String amount = String.format("$%,.2f", rs.getDouble("price"));
            String status = rs.getString("status");
            // date is not used in Transaction constructor – we omit it
            list.add(new DashboardController.Transaction(name, category, amount, status));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- MARGIN ANALYSIS ----------
    public List<MarginAnalysisController.TransactionMargin> loadMarginTransactions() throws SQLException {
        List<MarginAnalysisController.TransactionMargin> list = new ArrayList<>();
        String query = "SELECT o.order_id, p.name as product, p.cost, o.price as sale_price " +
                       "FROM orders o JOIN products p ON o.product_id = p.id " +
                       "ORDER BY o.order_date DESC LIMIT 20";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String id = rs.getString("order_id");
            String product = rs.getString("product");
            double cost = rs.getDouble("cost");
            double salePrice = rs.getDouble("sale_price");
            list.add(new MarginAnalysisController.TransactionMargin(id, product, cost, salePrice));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- KPI METRICS ----------
    public KPIController.KPIData loadKPIData() throws SQLException {
        String query = "SELECT SUM(o.price) as total_revenue, " +
                       "COUNT(DISTINCT o.order_id) as total_orders, " +
                       "SUM(o.quantity) as total_units FROM orders o";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        double totalRevenue = rs.getDouble("total_revenue");
        int totalOrders = rs.getInt("total_orders");
        int totalUnits = rs.getInt("total_units");
        double avgOrderValue = (totalOrders == 0) ? 0.0 : totalRevenue / totalOrders;

        String productQuery = "SELECT p.name, SUM(o.quantity) as units, SUM(o.price) as revenue " +
                              "FROM orders o JOIN products p ON o.product_id = p.id " +
                              "GROUP BY p.id ORDER BY revenue DESC LIMIT 10";

        PreparedStatement pstmt2 = getConnection().prepareStatement(productQuery);
        ResultSet rs2 = pstmt2.executeQuery();
        List<KPIController.ProductKPI> topProducts = new ArrayList<>();

        while (rs2.next()) {
            String name = rs2.getString("name");
            int units = rs2.getInt("units");
            double revenue = rs2.getDouble("revenue");
            topProducts.add(new KPIController.ProductKPI(name, units, revenue));
        }
        rs2.close();
        pstmt2.close();

        for (KPIController.ProductKPI p : topProducts) {
            double percent = (totalRevenue == 0) ? 0.0 : (p.getRevenue() / totalRevenue) * 100;
            p.setPercentageOfTotal(String.format("%.2f%%", percent));
        }

        rs.close();
        pstmt.close();
        return new KPIController.KPIData(totalRevenue, avgOrderValue, totalOrders, totalUnits, topProducts);
    }

    // ---------- FILTER ----------
    public List<FilterController.Transaction> loadFilteredTransactions(
            String startDate, String endDate, String clientId, String category) throws SQLException {

        List<FilterController.Transaction> list = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT o.order_id, o.customer_id, p.name as product, p.category, o.order_date, o.price, o.status " +
                "FROM orders o JOIN products p ON o.product_id = p.id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            query.append("AND o.order_date BETWEEN ? AND ? ");
            params.add(startDate);
            params.add(endDate);
        }
        if (clientId != null && !clientId.isEmpty()) {
            query.append("AND o.customer_id LIKE ? ");
            params.add("%" + clientId + "%");
        }
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            query.append("AND p.category = ? ");
            params.add(category);
        }
        query.append("ORDER BY o.order_date DESC LIMIT 100");

        PreparedStatement pstmt = getConnection().prepareStatement(query.toString());
        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            String id = rs.getString("order_id");
            String client = rs.getString("customer_id");
            String product = rs.getString("product");
            String cat = rs.getString("category");
            LocalDate date = LocalDate.parse(rs.getString("order_date"));
            double amount = rs.getDouble("price");
            String status = rs.getString("status");
            list.add(new FilterController.Transaction(
                    id, client, product, cat, date, amount, status));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- EXPORT ----------
    public List<ExportController.ExportTransaction> loadExportTransactions() throws SQLException {
        List<ExportController.ExportTransaction> list = new ArrayList<>();
        String query = "SELECT o.order_id, o.customer_id, p.name as product, p.category, o.order_date, o.price, o.status " +
                       "FROM orders o JOIN products p ON o.product_id = p.id " +
                       "ORDER BY o.order_date DESC";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String id = rs.getString("order_id");
            String client = rs.getString("customer_id");
            String product = rs.getString("product");
            String category = rs.getString("category");
            String date = rs.getString("order_date");
            double amount = rs.getDouble("price");
            String status = rs.getString("status");
            list.add(new ExportController.ExportTransaction(id, client, product, category, date, amount, status));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- CLIENT INSIGHTS ----------
    public List<ClientInsightController.CustomerProfile> loadCustomerProfiles() throws SQLException {
        List<ClientInsightController.CustomerProfile> list = new ArrayList<>();
        String query = "SELECT o.customer_id, COUNT(o.order_id) as order_count, " +
                       "SUM(o.price) as total_spent, AVG(o.price) as avg_order, " +
                       "MAX(o.order_date) as last_order " +
                       "FROM orders o GROUP BY o.customer_id";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String customerId = rs.getString("customer_id");
            int orderCount = rs.getInt("order_count");
            double totalSpent = rs.getDouble("total_spent");
            double avgOrder = rs.getDouble("avg_order");
            LocalDate lastOrder = LocalDate.parse(rs.getString("last_order"));
            list.add(new ClientInsightController.CustomerProfile(customerId, orderCount, totalSpent, avgOrder, lastOrder));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- ANOMALY DETECTION ----------
    public List<AnomalyDetectionController.ProductAnomaly> loadProductAnomalies(int minSampleSize) throws SQLException {
        List<AnomalyDetectionController.ProductAnomaly> list = new ArrayList<>();
        String query = "SELECT p.name, COUNT(o.order_id) as sales, " +
                       "SUM(CASE WHEN o.status = 'Refunded' THEN 1 ELSE 0 END) as returns " +
                       "FROM products p LEFT JOIN orders o ON p.id = o.product_id " +
                       "GROUP BY p.id";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            String name = rs.getString("name");
            int sales = rs.getInt("sales");
            int returns = rs.getInt("returns");
            double refundRate = (sales > 0) ? ((double) returns / sales) * 100 : 0;

            boolean isAnomaly = false;
            String anomalyType = "Normal";
            if (sales >= minSampleSize) {
                if (refundRate > 20.0) {
                    isAnomaly = true;
                    anomalyType = "High Refund Rate";
                } else if (sales < 10 && refundRate > 10.0) {
                    isAnomaly = true;
                    anomalyType = "Underperforming Stock";
                }
            } else {
                anomalyType = "Insufficient Data";
            }
            list.add(new AnomalyDetectionController.ProductAnomaly(name, sales, returns, refundRate, isAnomaly, anomalyType));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- VAT ----------
    public List<VATController.VATTransaction> loadVATTransactions(double standardRate, double reducedRate, double zeroRate) throws SQLException {
        List<VATController.VATTransaction> list = new ArrayList<>();
        String query = "SELECT o.order_id, p.name as description, o.price as gross, " +
                       "CASE WHEN p.category = 'Electronics' THEN ? " +
                       "     WHEN p.category = 'Software' THEN ? " +
                       "     ELSE ? END as vat_rate, " +
                       "CASE WHEN p.category = 'Electronics' THEN 'Standard' " +
                       "     WHEN p.category = 'Software' THEN 'Reduced' " +
                       "     ELSE 'Zero' END as rate_label " +
                       "FROM orders o JOIN products p ON o.product_id = p.id " +
                       "ORDER BY o.order_date DESC LIMIT 50";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setDouble(1, standardRate);
        pstmt.setDouble(2, reducedRate);
        pstmt.setDouble(3, zeroRate);

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            String id = rs.getString("order_id");
            String desc = rs.getString("description");
            double gross = rs.getDouble("gross");
            double rate = rs.getDouble("vat_rate");
            String rateLabel = rs.getString("rate_label");
            double vat = Math.round((gross * rate) * 100.0) / 100.0;
            list.add(new VATController.VATTransaction(id, desc, gross, rate, rateLabel, vat));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- ACCOUNT STATEMENT ----------
    public List<StatementController.StatementTransaction> loadStatementTransactions(String startDate, String endDate) throws SQLException {
        List<StatementController.StatementTransaction> list = new ArrayList<>();
        String query = "SELECT o.order_id, o.order_date, o.customer_id, p.name as product, o.quantity, o.price, o.status " +
                       "FROM orders o JOIN products p ON o.product_id = p.id " +
                       "WHERE o.order_date BETWEEN ? AND ? ORDER BY o.order_date DESC";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate);
        pstmt.setString(2, endDate);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            list.add(new StatementController.StatementTransaction(
                    rs.getString("order_id"),
                    rs.getString("order_date"),
                    rs.getString("customer_id"),
                    rs.getString("product"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getString("status")
            ));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    // ---------- SPENDING OVERVIEW ----------
    public List<SpendingOverviewController.CategorySpend> loadSpendingByCategory() throws SQLException {
        List<SpendingOverviewController.CategorySpend> list = new ArrayList<>();
        String query = "SELECT p.category, SUM(p.cost * o.quantity) as total_cost, COUNT(*) as order_count " +
                       "FROM orders o JOIN products p ON o.product_id = p.id " +
                       "GROUP BY p.category ORDER BY total_cost DESC";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            list.add(new SpendingOverviewController.CategorySpend(
                    rs.getString("category"),
                    rs.getDouble("total_cost"),
                    rs.getInt("order_count")
            ));
        }
        rs.close();
        pstmt.close();
        return list;
    }

    public LinkedHashMap<String, Double> loadMonthlySpend() throws SQLException {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        String query = "SELECT strftime('%Y-%m', o.order_date) as month, SUM(p.cost * o.quantity) as total_cost " +
                       "FROM orders o JOIN products p ON o.product_id = p.id " +
                       "GROUP BY month ORDER BY month";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            map.put(rs.getString("month"), rs.getDouble("total_cost"));
        }
        rs.close();
        pstmt.close();
        return map;
    }
}