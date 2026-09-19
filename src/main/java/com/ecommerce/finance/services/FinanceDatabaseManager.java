package com.ecommerce.finance.services;
import com.ecommerce.finance.models.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecommerce.finance.models.*;

public class FinanceDatabaseManager {

    private static FinanceDatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:ecommerce.db";

    private FinanceDatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    public static synchronized FinanceDatabaseManager getInstance() {
        if (instance == null) {
            instance = new FinanceDatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
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
    }

    // ================================================================
    // DATABASE INITIALIZATION
    // ================================================================
    public void initializeDatabase() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        // Users table
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT DEFAULT 'admin'," +
                "created_date DATE NOT NULL)");

        // Products table
        stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "cost REAL NOT NULL)");

        // Customers table
        stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_code TEXT UNIQUE," +
                "name TEXT NOT NULL," +
                "email TEXT," +
                "phone TEXT)");

        // Orders table
        stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_code TEXT UNIQUE NOT NULL," +
                "customer_id INTEGER," +
                "order_date DATE NOT NULL," +
                "status TEXT NOT NULL," +
                "net_revenue REAL DEFAULT 0," +
                "total_amount REAL DEFAULT 0," +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

        // Order Items table
        stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "unit_price REAL NOT NULL," +
                "cost_price REAL NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
                "FOREIGN KEY (product_id) REFERENCES products(product_id))");

        // Tax Records table
        stmt.execute("CREATE TABLE IF NOT EXISTS tax_records (" +
                "tax_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL UNIQUE," +
                "gross_amount REAL NOT NULL," +
                "tax_rate REAL NOT NULL," +
                "tax_amount REAL NOT NULL," +
                "net_amount REAL NOT NULL," +
                "record_date DATE NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id))");

        // Tax Settings table
        stmt.execute("CREATE TABLE IF NOT EXISTS tax_settings (" +
                "setting_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "vat_rate REAL NOT NULL DEFAULT 20.0," +
                "last_updated DATE NOT NULL)");

        // Invoices table
        stmt.execute("CREATE TABLE IF NOT EXISTS invoices (" +
                "invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL UNIQUE," +
                "customer_id INTEGER NOT NULL," +
                "invoice_number TEXT UNIQUE NOT NULL," +
                "file_name TEXT NOT NULL," +
                "generate_date DATE NOT NULL," +
                "status TEXT NOT NULL," +
                "file_path TEXT," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

        // Expenses table
        stmt.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                "expense_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "expense_date DATE NOT NULL," +
                "description TEXT)");

        // Refund Records table
        stmt.execute("CREATE TABLE IF NOT EXISTS refund_records (" +
                "refund_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "customer_id INTEGER NOT NULL," +
                "amount REAL NOT NULL," +
                "reason TEXT NOT NULL," +
                "refund_date DATE NOT NULL," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

        // Financial Adjustments table
        stmt.execute("CREATE TABLE IF NOT EXISTS financial_adjustments (" +
                "adj_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "adj_type TEXT NOT NULL," +
                "value REAL NOT NULL," +
                "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id))");

        // Transactions table (Payment)
        stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "tx_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "payment_method TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "tx_date DATE NOT NULL," +
                "reference TEXT," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id))");

        // Budget Entries table
        stmt.execute("CREATE TABLE IF NOT EXISTS budget_entries (" +
                "entry_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT NOT NULL," +
                "planned_amount REAL NOT NULL," +
                "period TEXT NOT NULL," +
                "created_date DATE NOT NULL," +
                "UNIQUE(category, period))");

        // Instalment Plans table
        stmt.execute("CREATE TABLE IF NOT EXISTS instalment_plans (" +
                "plan_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id INTEGER NOT NULL," +
                "order_id INTEGER NOT NULL," +
                "total_value REAL NOT NULL," +
                "instalments_count INTEGER NOT NULL," +
                "instalment_amount REAL NOT NULL," +
                "start_date DATE NOT NULL," +
                "end_date DATE NOT NULL," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id))");

        // Payment Receipts table
        stmt.execute("CREATE TABLE IF NOT EXISTS payment_receipts (" +
                "receipt_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "plan_id INTEGER NOT NULL," +
                "amount REAL NOT NULL," +
                "payment_date DATE NOT NULL," +
                "billing_month TEXT NOT NULL," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY (plan_id) REFERENCES instalment_plans(plan_id))");

        // Financial Alerts table
        stmt.execute("CREATE TABLE IF NOT EXISTS financial_alerts (" +
                "alert_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id INTEGER NOT NULL," +
                "plan_id INTEGER NOT NULL," +
                "alert_type TEXT NOT NULL," +
                "days_overdue INTEGER DEFAULT 0," +
                "outstanding REAL DEFAULT 0," +
                "alert_date DATE NOT NULL," +
                "resolved BOOLEAN DEFAULT 0," +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
                "FOREIGN KEY (plan_id) REFERENCES instalment_plans(plan_id))");

        // Trade-In Valuations table
        stmt.execute("CREATE TABLE IF NOT EXISTS trade_in_valuations (" +
                "valuation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "device_model TEXT NOT NULL," +
                "condition_grade TEXT NOT NULL," +
                "assessed_value REAL NOT NULL," +
                "UNIQUE(device_model, condition_grade))");

        // Trade-In Records table
        stmt.execute("CREATE TABLE IF NOT EXISTS trade_in_records (" +
                "trade_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "customer_id INTEGER NOT NULL," +
                "device_model TEXT NOT NULL," +
                "condition_grade TEXT NOT NULL," +
                "assessed_value REAL NOT NULL," +
                "trade_date DATE NOT NULL," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

        // Product Add-Ons table
        stmt.execute("CREATE TABLE IF NOT EXISTS product_addons (" +
                "addon_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "product_id INTEGER NOT NULL," +
                "addon_type TEXT NOT NULL," +
                "premium_amount REAL NOT NULL," +
                "duration_months INTEGER NOT NULL," +
                "start_date DATE NOT NULL," +
                "end_date DATE NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
                "FOREIGN KEY (product_id) REFERENCES products(product_id))");

        // Campaigns table
        stmt.execute("CREATE TABLE IF NOT EXISTS campaigns (" +
                "campaign_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "cost REAL NOT NULL," +
                "start_date DATE NOT NULL," +
                "end_date DATE NOT NULL," +
                "status TEXT NOT NULL)");

        // Campaign Orders table
        stmt.execute("CREATE TABLE IF NOT EXISTS campaign_orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "campaign_id INTEGER NOT NULL," +
                "order_id INTEGER NOT NULL," +
                "discount_applied REAL NOT NULL," +
                "UNIQUE(campaign_id, order_id)," +
                "FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id)," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id))");

        // Audit Logs table
        stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "event_type TEXT NOT NULL," +
                "entity_id INTEGER NOT NULL," +
                "admin_id INTEGER," +
                "details TEXT," +
                "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");

        stmt.close();
        System.out.println("Finance database schema created.");
    }

    // ================================================================
    // FR1: Sales Revenue Report
    // ================================================================
    public List<RevenueRow> getRevenueRows(LocalDate startDate, LocalDate endDate, String category) throws SQLException {
        List<RevenueRow> rows = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT p.category, SUM(oi.unit_price * oi.quantity) as revenue, COUNT(DISTINCT o.order_id) as order_count " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "WHERE o.status = 'Delivered' AND o.order_date BETWEEN ? AND ? "
        );

        if (category != null && !category.isEmpty() && !category.equals("All")) {
            query.append("AND p.category = ? ");
        }
        query.append("GROUP BY p.category");

        PreparedStatement pstmt = getConnection().prepareStatement(query.toString());
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        int paramIndex = 3;
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            pstmt.setString(paramIndex, category);
        }

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            rows.add(new RevenueRow(rs.getString("category"), rs.getDouble("revenue"), rs.getInt("order_count")));
        }
        rs.close();
        pstmt.close();
        return rows;
    }

    // ================================================================
    // FR2: Tax Calculation & Compliance Report
    // ================================================================
    public List<TaxRecord> getTaxRecords(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<TaxRecord> records = new ArrayList<>();
        String query = "SELECT tax_id, order_id, gross_amount, tax_rate, tax_amount, record_date " +
                       "FROM tax_records WHERE record_date BETWEEN ? AND ?";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            records.add(new TaxRecord(
                rs.getInt("tax_id"),
                rs.getInt("order_id"),
                rs.getDouble("gross_amount"),
                rs.getDouble("tax_rate"),
                rs.getDouble("tax_amount"),
                LocalDate.parse(rs.getString("record_date"))
            ));
        }
        rs.close();
        pstmt.close();
        return records;
    }

    public double getVATRate() throws SQLException {
        String query = "SELECT vat_rate FROM tax_settings ORDER BY setting_id DESC LIMIT 1";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        double rate = rs.next() ? rs.getDouble("vat_rate") : 20.0;
        rs.close();
        pstmt.close();
        return rate;
    }

    public void insertTaxRecord(int orderId, double grossAmount, double taxRate, double taxAmount,
                                double netAmount, LocalDate recordDate) throws SQLException {
        String query = "INSERT INTO tax_records (order_id, gross_amount, tax_rate, tax_amount, net_amount, record_date) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, orderId);
        pstmt.setDouble(2, grossAmount);
        pstmt.setDouble(3, taxRate);
        pstmt.setDouble(4, taxAmount);
        pstmt.setDouble(5, netAmount);
        pstmt.setString(6, recordDate.toString());
        pstmt.executeUpdate();
        pstmt.close();
    }

    // ================================================================
    // FR3: Automated Invoice Generation
    // ================================================================
    public Invoice findInvoiceByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM invoices WHERE order_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, orderId);
        ResultSet rs = pstmt.executeQuery();

        Invoice invoice = null;
        if (rs.next()) {
            invoice = new Invoice();
            invoice.setInvoiceId(rs.getInt("invoice_id"));
            invoice.setOrderId(rs.getInt("order_id"));
            invoice.setCustomerId(rs.getInt("customer_id"));
            invoice.setInvoiceNumber(rs.getString("invoice_number"));
            invoice.setFileName(rs.getString("file_name"));
            invoice.setGenerateDate(LocalDate.parse(rs.getString("generate_date")));
            invoice.setStatus(rs.getString("status"));
            invoice.setFilePath(rs.getString("file_path"));
        }
        rs.close();
        pstmt.close();
        return invoice;
    }

    public void insertInvoice(Invoice invoice) throws SQLException {
        String query = "INSERT INTO invoices (order_id, customer_id, invoice_number, file_name, generate_date, status, file_path) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, invoice.getOrderId());
        pstmt.setInt(2, invoice.getCustomerId());
        pstmt.setString(3, invoice.getInvoiceNumber());
        pstmt.setString(4, invoice.getFileName());
        pstmt.setString(5, invoice.getGenerateDate().toString());
        pstmt.setString(6, invoice.getStatus());
        pstmt.setString(7, invoice.getFilePath());
        pstmt.executeUpdate();
        pstmt.close();
    }

    public Order getOrderWithItems(int orderId) throws SQLException {
        // Get order details
        String orderQuery = "SELECT * FROM orders WHERE order_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(orderQuery);
        pstmt.setInt(1, orderId);
        ResultSet rs = pstmt.executeQuery();

        Order order = null;
        if (rs.next()) {
            order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setOrderCode(rs.getString("order_code"));
            order.setCustomerId(rs.getInt("customer_id"));
            order.setOrderDate(LocalDate.parse(rs.getString("order_date")));
            order.setStatus(rs.getString("status"));
            order.setNetRevenue(rs.getDouble("net_revenue"));
            order.setTotalAmount(rs.getDouble("total_amount"));
        }
        rs.close();
        pstmt.close();

        if (order != null) {
            // Get order items
            String itemsQuery = "SELECT * FROM order_items WHERE order_id = ?";
            pstmt = getConnection().prepareStatement(itemsQuery);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();

            List<OrderItem> items = new ArrayList<>();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setItemId(rs.getInt("item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setCostPrice(rs.getDouble("cost_price"));
                items.add(item);
            }
            rs.close();
            pstmt.close();
            order.setItems(items);
        }

        return order;
    }

    // ================================================================
    // FR4: Profit & Loss Statement
    // ================================================================
    public double sumNetRevenueDelivered(LocalDate startDate, LocalDate endDate) throws SQLException {
        String query = "SELECT SUM(net_revenue) as total FROM orders WHERE status = 'Delivered' AND order_date BETWEEN ? AND ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    public double sumCOGSDelivered(LocalDate startDate, LocalDate endDate) throws SQLException {
        String query = "SELECT SUM(oi.quantity * oi.cost_price) as total " +
                       "FROM orders o JOIN order_items oi ON o.order_id = oi.order_id " +
                       "WHERE o.status = 'Delivered' AND o.order_date BETWEEN ? AND ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    public double sumExpensesInPeriod(LocalDate startDate, LocalDate endDate) throws SQLException {
        String query = "SELECT SUM(amount) as total FROM expenses WHERE expense_date BETWEEN ? AND ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    // ================================================================
    // FR5: Refund & Return Financial Adjustment
    // ================================================================
    public Order findOrderById(int orderId) throws SQLException {
        String query = "SELECT * FROM orders WHERE order_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, orderId);
        ResultSet rs = pstmt.executeQuery();

        Order order = null;
        if (rs.next()) {
            order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setOrderCode(rs.getString("order_code"));
            order.setCustomerId(rs.getInt("customer_id"));
            order.setOrderDate(LocalDate.parse(rs.getString("order_date")));
            order.setStatus(rs.getString("status"));
            order.setNetRevenue(rs.getDouble("net_revenue"));
            order.setTotalAmount(rs.getDouble("total_amount"));
        }
        rs.close();
        pstmt.close();
        return order;
    }

    public void insertRefundRecord(RefundRecord record) throws SQLException {
        String query = "INSERT INTO refund_records (order_id, customer_id, amount, reason, refund_date, status) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, record.getOrderId());
        pstmt.setInt(2, record.getCustomerId());
        pstmt.setDouble(3, record.getAmount());
        pstmt.setString(4, record.getReason());
        pstmt.setString(5, record.getRefundDate().toString());
        pstmt.setString(6, record.getStatus());
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void insertFinancialAdjustment(FinancialAdjustment adj) throws SQLException {
        String query = "INSERT INTO financial_adjustments (order_id, adj_type, value, timestamp) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, adj.getOrderId());
        pstmt.setString(2, adj.getAdjType());
        pstmt.setDouble(3, adj.getValue());
        pstmt.setString(4, adj.getTimestamp().toString());
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void updateOrderNetRevenue(int orderId, double adjustment) throws SQLException {
        String query = "UPDATE orders SET net_revenue = net_revenue + ? WHERE order_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setDouble(1, adjustment);
        pstmt.setInt(2, orderId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void updateTaxRecord(int orderId) throws SQLException {
        // Recalculate tax based on updated net revenue
        String query = "SELECT net_revenue FROM orders WHERE order_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, orderId);
        ResultSet rs = pstmt.executeQuery();
        double netRevenue = rs.next() ? rs.getDouble("net_revenue") : 0;
        rs.close();
        pstmt.close();

        double taxRate = getVATRate();
        double grossAmount = netRevenue / (1 - taxRate / 100);
        double taxAmount = grossAmount * taxRate / 100;

        String updateQuery = "UPDATE tax_records SET gross_amount = ?, tax_amount = ?, net_amount = ? WHERE order_id = ?";
        pstmt = getConnection().prepareStatement(updateQuery);
        pstmt.setDouble(1, grossAmount);
        pstmt.setDouble(2, taxAmount);
        pstmt.setDouble(3, netRevenue);
        pstmt.setInt(4, orderId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void insertAuditLog(AuditLog log) throws SQLException {
        String query = "INSERT INTO audit_logs (event_type, entity_id, admin_id, details, timestamp) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, log.getEventType());
        pstmt.setInt(2, log.getEntityId());
        pstmt.setInt(3, log.getAdminId());
        pstmt.setString(4, log.getDetails());
        pstmt.setString(5, log.getTimestamp().toString());
        pstmt.executeUpdate();
        pstmt.close();
    }

    // ================================================================
    // FR6: Payment Method Analytics
    // ================================================================
    public List<Map<String, Object>> getTransactionsGroupedByMethod(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String query = "SELECT payment_method, SUM(amount) as total_amount, COUNT(*) as tx_count " +
                       "FROM transactions WHERE tx_date BETWEEN ? AND ? GROUP BY payment_method";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            row.put("payment_method", rs.getString("payment_method"));
            row.put("total_amount", rs.getDouble("total_amount"));
            row.put("tx_count", rs.getInt("tx_count"));
            result.add(row);
        }
        rs.close();
        pstmt.close();
        return result;
    }

    public double getTotalRevenueInPeriod(LocalDate startDate, LocalDate endDate) throws SQLException {
        String query = "SELECT SUM(amount) as total FROM transactions WHERE tx_date BETWEEN ? AND ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    // ================================================================
    // FR7: Budget vs Actual Expenditure Tracker
    // ================================================================
    public List<BudgetEntry> getBudgetEntriesByPeriod(String period) throws SQLException {
        List<BudgetEntry> entries = new ArrayList<>();
        String query = "SELECT * FROM budget_entries WHERE period = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, period);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            BudgetEntry entry = new BudgetEntry();
            entry.setEntryId(rs.getInt("entry_id"));
            entry.setCategory(rs.getString("category"));
            entry.setPlannedAmount(rs.getDouble("planned_amount"));
            entry.setPeriod(rs.getString("period"));
            entry.setCreatedDate(LocalDate.parse(rs.getString("created_date")));
            entries.add(entry);
        }
        rs.close();
        pstmt.close();
        return entries;
    }

    public void insertBudgetEntry(BudgetEntry entry) throws SQLException {
        String query = "INSERT INTO budget_entries (category, planned_amount, period, created_date) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, entry.getCategory());
        pstmt.setDouble(2, entry.getPlannedAmount());
        pstmt.setString(3, entry.getPeriod());
        pstmt.setString(4, entry.getCreatedDate().toString());
        pstmt.executeUpdate();
        pstmt.close();
    }

    public double sumExpensesByCategory(String category, String period) throws SQLException {
        // Parse period (e.g., "2024-01" to start and end of month)
        String[] parts = period.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        String query = "SELECT SUM(amount) as total FROM expenses WHERE category = ? AND expense_date BETWEEN ? AND ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, category);
        pstmt.setString(2, startDate.toString());
        pstmt.setString(3, endDate.toString());
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    // ================================================================
    // FR8: Instalment Payment Plan Tracker
    // ================================================================
    public List<InstalmentPlan> getActivePlans() throws SQLException {
        List<InstalmentPlan> plans = new ArrayList<>();
        String query = "SELECT * FROM instalment_plans WHERE status IN ('ACTIVE', 'OVERDUE')";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            InstalmentPlan plan = new InstalmentPlan();
            plan.setPlanId(rs.getInt("plan_id"));
            plan.setCustomerId(rs.getInt("customer_id"));
            plan.setOrderId(rs.getInt("order_id"));
            plan.setTotalValue(rs.getDouble("total_value"));
            plan.setInstalmentsCount(rs.getInt("instalments_count"));
            plan.setInstalmentAmount(rs.getDouble("instalment_amount"));
            plan.setStartDate(LocalDate.parse(rs.getString("start_date")));
            plan.setEndDate(LocalDate.parse(rs.getString("end_date")));
            plan.setStatus(rs.getString("status"));
            plans.add(plan);
        }
        rs.close();
        pstmt.close();
        return plans;
    }

    public void updatePlanStatus(int planId, String status) throws SQLException {
        String query = "UPDATE instalment_plans SET status = ? WHERE plan_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, status);
        pstmt.setInt(2, planId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public List<PaymentReceipt> getPaymentReceipts(int planId) throws SQLException {
        List<PaymentReceipt> receipts = new ArrayList<>();
        String query = "SELECT * FROM payment_receipts WHERE plan_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, planId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            PaymentReceipt receipt = new PaymentReceipt();
            receipt.setReceiptId(rs.getInt("receipt_id"));
            receipt.setPlanId(rs.getInt("plan_id"));
            receipt.setAmount(rs.getDouble("amount"));
            receipt.setPaymentDate(LocalDate.parse(rs.getString("payment_date")));
            receipt.setBillingMonth(rs.getString("billing_month"));
            receipt.setStatus(rs.getString("status"));
            receipts.add(receipt);
        }
        rs.close();
        pstmt.close();
        return receipts;
    }

    public double sumReceiptsForPlan(int planId) throws SQLException {
        String query = "SELECT SUM(amount) as total FROM payment_receipts WHERE plan_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, planId);
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    public void insertFinancialAlert(FinancialAlert alert) throws SQLException {
        String query = "INSERT INTO financial_alerts (customer_id, plan_id, alert_type, days_overdue, outstanding, alert_date) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, alert.getCustomerId());
        pstmt.setInt(2, alert.getPlanId());
        pstmt.setString(3, alert.getAlertType());
        pstmt.setInt(4, alert.getDaysOverdue());
        pstmt.setDouble(5, alert.getOutstanding());
        pstmt.setString(6, alert.getAlertDate().toString());
        pstmt.executeUpdate();
        pstmt.close();
    }

    // ================================================================
    // FR9: Trade-In Device Valuation & Revenue Adjustment
    // ================================================================
    public double lookupTradeInValue(String deviceModel, String conditionGrade) throws SQLException {
        String query = "SELECT assessed_value FROM trade_in_valuations WHERE device_model = ? AND condition_grade = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, deviceModel);
        pstmt.setString(2, conditionGrade);
        ResultSet rs = pstmt.executeQuery();
        double value = rs.next() ? rs.getDouble("assessed_value") : 0.0;
        rs.close();
        pstmt.close();
        return value;
    }

    public void insertTradeInRecord(TradeInRecord record) throws SQLException {
        String query = "INSERT INTO trade_in_records (order_id, customer_id, device_model, condition_grade, assessed_value, trade_date, status) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, record.getOrderId());
        pstmt.setInt(2, record.getCustomerId());
        pstmt.setString(3, record.getDeviceModel());
        pstmt.setString(4, record.getConditionGrade());
        pstmt.setDouble(5, record.getAssessedValue());
        pstmt.setString(6, record.getTradeDate().toString());
        pstmt.setString(7, record.getStatus());
        pstmt.executeUpdate();
        pstmt.close();
    }

    // ================================================================
    // FR10: Warranty & Insurance Add-On Revenue Report
    // ================================================================
    public List<Map<String, Object>> getAddOnJoinedData(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String query = "SELECT pa.addon_id, pa.addon_type, p.name as product_name, pa.premium_amount, o.status " +
                       "FROM product_addons pa " +
                       "JOIN orders o ON pa.order_id = o.order_id " +
                       "JOIN products p ON pa.product_id = p.product_id " +
                       "WHERE o.order_date BETWEEN ? AND ?";

        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setString(1, startDate.toString());
        pstmt.setString(2, endDate.toString());
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            row.put("addon_id", rs.getInt("addon_id"));
            row.put("addon_type", rs.getString("addon_type"));
            row.put("product_name", rs.getString("product_name"));
            row.put("premium_amount", rs.getDouble("premium_amount"));
            row.put("status", rs.getString("status"));
            result.add(row);
        }
        rs.close();
        pstmt.close();
        return result;
    }

    // ================================================================
    // FR11: Promotional Discount & Campaign ROI Report
    // ================================================================
    public Campaign findCampaignById(int campaignId) throws SQLException {
        String query = "SELECT * FROM campaigns WHERE campaign_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, campaignId);
        ResultSet rs = pstmt.executeQuery();

        Campaign campaign = null;
        if (rs.next()) {
            campaign = new Campaign();
            campaign.setCampaignId(rs.getInt("campaign_id"));
            campaign.setName(rs.getString("name"));
            campaign.setDescription(rs.getString("description"));
            campaign.setCost(rs.getDouble("cost"));
            campaign.setStartDate(LocalDate.parse(rs.getString("start_date")));
            campaign.setEndDate(LocalDate.parse(rs.getString("end_date")));
            campaign.setStatus(rs.getString("status"));
        }
        rs.close();
        pstmt.close();
        return campaign;
    }

    public List<CampaignOrder> getCampaignOrders(int campaignId) throws SQLException {
        List<CampaignOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM campaign_orders WHERE campaign_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, campaignId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            CampaignOrder order = new CampaignOrder();
            order.setId(rs.getInt("id"));
            order.setCampaignId(rs.getInt("campaign_id"));
            order.setOrderId(rs.getInt("order_id"));
            order.setDiscountApplied(rs.getDouble("discount_applied"));
            orders.add(order);
        }
        rs.close();
        pstmt.close();
        return orders;
    }

    public double sumDiscountByCampaign(int campaignId) throws SQLException {
        String query = "SELECT SUM(discount_applied) as total FROM campaign_orders WHERE campaign_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, campaignId);
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    public double sumRevenueByCampaign(int campaignId) throws SQLException {
        String query = "SELECT SUM(o.total_amount) as total " +
                       "FROM campaign_orders co JOIN orders o ON co.order_id = o.order_id " +
                       "WHERE co.campaign_id = ?";
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        pstmt.setInt(1, campaignId);
        ResultSet rs = pstmt.executeQuery();
        double total = rs.next() ? rs.getDouble("total") : 0.0;
        rs.close();
        pstmt.close();
        return total;
    }

    // ================================================================
    // User Authentication
    // ================================================================
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
}