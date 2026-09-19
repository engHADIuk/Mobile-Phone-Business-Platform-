package com.ecommerce.finance.services;

import com.ecommerce.finance.DashboardController;
import com.ecommerce.finance.MarginAnalysisController;
import com.ecommerce.finance.KPIController;
import com.ecommerce.finance.VATController;
import com.ecommerce.finance.services.DatabaseManager;
import com.ecommerce.finance.FilterController;
import com.ecommerce.finance.ExportController;
import com.ecommerce.finance.ClientInsightController;
import com.ecommerce.finance.AnomalyDetectionController;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = DatabaseManager.getInstance();
        dbManager.setDbUrl("jdbc:sqlite::memory:");
        dbManager.initializeDatabase();
    }

    @AfterEach
    void tearDown() {
        dbManager.closeConnection();
    }

    @Test
    void testAuthenticateUser_validAdmin() throws SQLException {
        assertTrue(dbManager.authenticateUser("hadi7@email.com", "hadi7"));
    }

    @Test
    void testAuthenticateUser_validEmployee() throws SQLException {
        assertTrue(dbManager.authenticateUser("leo@email.com", "leo10"));
    }

    @Test
    void testAuthenticateUser_invalidPassword() throws SQLException {
        assertFalse(dbManager.authenticateUser("hadi7@email.com", "wrong"));
    }

    @Test
    void testAuthenticateUser_unknownUser() throws SQLException {
        assertFalse(dbManager.authenticateUser("unknown", "pass"));
    }

    @Test
    void testGetUserRole_admin() throws SQLException {
        assertEquals("admin", dbManager.getUserRole("hadi7@email.com"));
    }

    @Test
    void testGetUserRole_employee() throws SQLException {
        assertEquals("employee", dbManager.getUserRole("leo@email.com"));
    }

    @Test
    void testGetUserRole_unknown() throws SQLException {
        assertEquals("employee", dbManager.getUserRole("unknown"));
    }

    @Test
    void testLoadDashboardTransactions() throws SQLException {
        insertSampleOrders();
        List<DashboardController.Transaction> transactions = dbManager.loadDashboardTransactions();
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertEquals(5, transactions.size());
    }

    @Test
    void testLoadMarginTransactions() throws SQLException {
        insertSampleOrders();
        List<MarginAnalysisController.TransactionMargin> margins = dbManager.loadMarginTransactions();
        assertNotNull(margins);
        assertFalse(margins.isEmpty());
    }

    @Test
    void testLoadKPIData() throws SQLException {
        insertSampleOrders();
        KPIController.KPIData data = dbManager.loadKPIData();
        assertNotNull(data);
        assertTrue(data.totalRevenue > 0);
        assertTrue(data.totalOrders > 0);
        assertTrue(data.totalUnitsSold > 0);
        assertNotNull(data.topProducts);
        assertFalse(data.topProducts.isEmpty());
    }

    @Test
    void testLoadVATTransactions() throws SQLException {
        insertSampleOrders();
        List<VATController.VATTransaction> vats = dbManager.loadVATTransactions(0.20, 0.05, 0.00);
        assertNotNull(vats);
        assertFalse(vats.isEmpty());
    }

    @Test
    void testLoadFilteredTransactions_all() throws SQLException {
        insertSampleOrders();
        List<FilterController.Transaction> filtered = dbManager.loadFilteredTransactions(null, null, null, null);
        assertNotNull(filtered);
        assertFalse(filtered.isEmpty());
    }

    @Test
    void testLoadExportTransactions() throws SQLException {
        insertSampleOrders();
        List<ExportController.ExportTransaction> exports = dbManager.loadExportTransactions();
        assertNotNull(exports);
        assertFalse(exports.isEmpty());
    }

    @Test
    void testLoadCustomerProfiles() throws SQLException {
        insertSampleOrders();
        List<ClientInsightController.CustomerProfile> profiles = dbManager.loadCustomerProfiles();
        assertNotNull(profiles);
        assertFalse(profiles.isEmpty());
    }

    @Test
    void testLoadProductAnomalies() throws SQLException {
        insertSampleOrders();
        List<AnomalyDetectionController.ProductAnomaly> anomalies = dbManager.loadProductAnomalies(5);
        assertNotNull(anomalies);
        assertFalse(anomalies.isEmpty());
    }

    private void insertSampleOrders() throws SQLException {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute("INSERT OR IGNORE INTO products (id, name, category, price, cost) VALUES " +
                "(1, 'iPhone 15 Pro', 'Electronics', 1299.99, 900.00), " +
                "(2, 'MacBook Pro M3', 'Electronics', 2499.99, 1800.00), " +
                "(3, 'AirPods Pro 2', 'Accessories', 249.99, 150.00)");

        stmt.execute("INSERT OR IGNORE INTO customers (id, customer_id, name, email) VALUES " +
                "(1, 'C001', 'John Doe', 'john@example.com'), " +
                "(2, 'C002', 'Jane Smith', 'jane@example.com')");

        stmt.execute("INSERT OR IGNORE INTO orders (id, order_id, customer_id, product_id, quantity, price, status, order_date) VALUES " +
                "(1, 'ORD-001', 'C001', 1, 2, 2599.98, 'Delivered', '2025-01-15'), " +
                "(2, 'ORD-002', 'C002', 2, 1, 2499.99, 'Delivered', '2025-02-10'), " +
                "(3, 'ORD-003', 'C001', 3, 5, 1249.95, 'Pending', '2025-03-05'), " +
                "(4, 'ORD-004', 'C002', 1, 1, 1299.99, 'Refunded', '2025-04-20'), " +
                "(5, 'ORD-005', 'C001', 2, 2, 4999.98, 'Delivered', '2025-05-12')");

        stmt.close();
    }
}