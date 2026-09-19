package com.ecommerce.finance.services;

import com.ecommerce.finance.models.SalesData;
import com.ecommerce.finance.models.RefundData;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class DataLoader {
    
    // Simulates database connection status
    private static boolean isDatabaseAvailable = true;
    
    // Simulate loading sales data from SQL query
    public static List<SalesData> loadSalesData() throws Exception {
        // Simulate error: 10% chance of database failure
        if (!isDatabaseAvailable || new Random().nextInt(10) < 1) {
            isDatabaseAvailable = false;
            throw new Exception("Database connection lost");
        }
        
        // Simulate SQL: SELECT date, SUM(total), COUNT(id) FROM orders GROUP BY date
        List<SalesData> sales = new ArrayList<>();
        sales.add(new SalesData("2026-07-01", 4500.00, 12));
        sales.add(new SalesData("2026-07-02", 6200.00, 18));
        sales.add(new SalesData("2026-07-03", 3800.00, 9));
        sales.add(new SalesData("2026-07-04", 7100.00, 22));
        sales.add(new SalesData("2026-07-05", 5600.00, 15));
        sales.add(new SalesData("2026-07-06", 8900.00, 28));
        sales.add(new SalesData("2026-07-07", 4300.00, 11));
        
        return sales;
    }
    
    // Simulate loading refund data
    public static List<RefundData> loadRefundData() throws Exception {
        if (!isDatabaseAvailable || new Random().nextInt(10) < 1) {
            isDatabaseAvailable = false;
            throw new Exception("Database connection lost");
        }
        
        // Simulate SQL: SELECT reason, SUM(amount) FROM refunds GROUP BY reason
        List<RefundData> refunds = new ArrayList<>();
        refunds.add(new RefundData("Damaged Product", 1200.00));
        refunds.add(new RefundData("Wrong Item", 850.00));
        refunds.add(new RefundData("Customer Changed Mind", 2100.00));
        refunds.add(new RefundData("Late Delivery", 450.00));
        
        return refunds;
    }
    
    // Simulate loading pending balance
    public static double loadPendingBalance() throws Exception {
        if (!isDatabaseAvailable || new Random().nextInt(10) < 1) {
            isDatabaseAvailable = false;
            throw new Exception("Database connection lost");
        }
        
        // Simulate SQL: SELECT SUM(total) FROM orders WHERE status = 'PENDING'
        return 15420.50;
    }
    
    // Reset database status (call this to retry)
    public static void resetDatabaseConnection() {
        isDatabaseAvailable = true;
    }
}