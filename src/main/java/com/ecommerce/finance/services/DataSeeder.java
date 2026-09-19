package com.ecommerce.finance.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

public class DataSeeder {

    public static void seedDatabase() {
        try {
            FinanceDatabaseManager db = FinanceDatabaseManager.getInstance();
            Connection conn = db.getConnection();

            // Check if data already exists
            java.sql.Statement checkStmt = conn.createStatement();
            java.sql.ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.getInt(1) > 0) {
                System.out.println("Finance data already seeded. Skipping.");
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();

            // ---------- Insert Users ----------
            String insertUser = "INSERT INTO users (username, password, role, created_date) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertUser);

            // Admin user
            pstmt.setString(1, "admin");
            pstmt.setString(2, "admin123");
            pstmt.setString(3, "admin");
            pstmt.setString(4, LocalDate.now().toString());
            pstmt.executeUpdate();

            // Finance user (also admin role – keep as admin)
            pstmt.setString(1, "finance");
            pstmt.setString(2, "finance123");
            pstmt.setString(3, "admin");   // finance user has admin privileges
            pstmt.setString(4, LocalDate.now().toString());
            pstmt.executeUpdate();

            // --- NEW: Employee user (view‑only) ---
            pstmt.setString(1, "employee");
            pstmt.setString(2, "employee123");
            pstmt.setString(3, "employee");
            pstmt.setString(4, LocalDate.now().toString());
            pstmt.executeUpdate();

            pstmt.close();

            // ---------- Insert Products ----------
            String[] productData = {
                "iPhone 15 Pro", "Electronics", "1299.00", "1100.00",
                "iPhone 15", "Electronics", "999.00", "850.00",
                "MacBook Pro M3", "Electronics", "2499.00", "2100.00",
                "MacBook Air M3", "Electronics", "1599.00", "1300.00",
                "AirPods Pro 2", "Accessories", "249.00", "180.00",
                "AirPods 4", "Accessories", "179.00", "130.00",
                "iPad Air 5", "Electronics", "799.00", "650.00",
                "iPad Pro M4", "Electronics", "1099.00", "900.00",
                "Apple Watch Ultra", "Electronics", "899.00", "700.00",
                "Apple Watch Series 9", "Electronics", "499.00", "380.00",
                "Mac Mini M2", "Electronics", "799.00", "650.00",
                "Apple TV 4K", "Electronics", "199.00", "150.00",
                "AirTag 4-Pack", "Accessories", "99.00", "70.00",
                "MagSafe Charger", "Accessories", "39.00", "25.00",
                "Apple Pencil 2", "Accessories", "129.00", "90.00",
                "Studio Display", "Electronics", "1599.00", "1300.00",
                "Mac Studio", "Electronics", "1999.00", "1600.00",
                "HomePod Mini", "Electronics", "99.00", "70.00",
                "iMac 24\"", "Electronics", "1299.00", "1050.00",
                "Pro Display XDR", "Electronics", "4999.00", "4000.00"
            };

            String insertProduct = "INSERT INTO products (name, category, price, cost) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertProduct);
            for (int i = 0; i < productData.length; i += 4) {
                pstmt.setString(1, productData[i]);
                pstmt.setString(2, productData[i + 1]);
                pstmt.setDouble(3, Double.parseDouble(productData[i + 2]));
                pstmt.setDouble(4, Double.parseDouble(productData[i + 3]));
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Customers ----------
            String[] customers = {"C001", "C002", "C003", "C004", "C005", "C006", "C007", "C008", "C009", "C010"};
            String[] customerNames = {"Alice Johnson", "Bob Smith", "Carol White", "David Brown", "Eva Green",
                                      "Frank Black", "Grace Lee", "Henry King", "Ivy Hall", "Jack Rose"};
            String insertCustomer = "INSERT INTO customers (customer_code, name, email, phone) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertCustomer);
            for (int i = 0; i < customers.length; i++) {
                pstmt.setString(1, customers[i]);
                pstmt.setString(2, customerNames[i]);
                pstmt.setString(3, customers[i] + "@email.com");
                pstmt.setString(4, "+1-555-" + String.format("%04d", i + 1));
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Orders ----------
            Random rand = new Random();
            String[] statuses = {"Pending", "Processing", "Shipped", "Delivered", "Refunded"};
            String[] paymentMethods = {"Credit Card", "PayPal", "Apple Pay", "Bank Transfer", "Instalment"};
            LocalDate startDate = LocalDate.of(2025, 1, 1);

            String insertOrder = "INSERT INTO orders (order_code, customer_id, order_date, status, net_revenue, total_amount) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertOrder);

            for (int i = 0; i < 150; i++) {
                String orderCode = "ORD-" + String.format("%06d", (100000 + i));
                int customerId = 1 + rand.nextInt(10);
                String status = statuses[rand.nextInt(statuses.length)];
                LocalDate date = startDate.plusDays(rand.nextInt(365));
                double total = Math.round((50 + rand.nextDouble() * 4000) * 100.0) / 100.0;
                double netRevenue = Math.round(total * (0.8 + rand.nextDouble() * 0.15) * 100.0) / 100.0;

                pstmt.setString(1, orderCode);
                pstmt.setInt(2, customerId);
                pstmt.setString(3, date.toString());
                pstmt.setString(4, status);
                pstmt.setDouble(5, netRevenue);
                pstmt.setDouble(6, total);
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Order Items ----------
            String insertOrderItem = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, cost_price) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertOrderItem);

            for (int orderId = 1; orderId <= 150; orderId++) {
                int itemCount = 1 + rand.nextInt(4);
                for (int j = 0; j < itemCount; j++) {
                    int productId = 1 + rand.nextInt(20);
                    int quantity = 1 + rand.nextInt(3);
                    double unitPrice = Math.round((100 + rand.nextDouble() * 1900) * 100.0) / 100.0;
                    double costPrice = Math.round(unitPrice * (0.6 + rand.nextDouble() * 0.3) * 100.0) / 100.0;

                    pstmt.setInt(1, orderId);
                    pstmt.setInt(2, productId);
                    pstmt.setInt(3, quantity);
                    pstmt.setDouble(4, unitPrice);
                    pstmt.setDouble(5, costPrice);
                    pstmt.executeUpdate();
                }
            }
            pstmt.close();

            // ---------- Insert Transactions (FR6) ----------
            String insertTransaction = "INSERT INTO transactions (order_id, payment_method, amount, tx_date) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertTransaction);
            for (int orderId = 1; orderId <= 150; orderId++) {
                String method = paymentMethods[rand.nextInt(paymentMethods.length)];
                double amount = Math.round((50 + rand.nextDouble() * 4000) * 100.0) / 100.0;
                LocalDate date = startDate.plusDays(rand.nextInt(365));
                pstmt.setInt(1, orderId);
                pstmt.setString(2, method);
                pstmt.setDouble(3, amount);
                pstmt.setString(4, date.toString());
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Expenses (FR4) ----------
            String[] expenseCategories = {"Payroll", "Marketing", "Rent", "Utilities", "Software Licenses", "Office Supplies", "Travel", "Training"};
            String insertExpense = "INSERT INTO expenses (category, amount, expense_date, description) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertExpense);
            for (int i = 0; i < 100; i++) {
                String category = expenseCategories[rand.nextInt(expenseCategories.length)];
                double amount = Math.round((500 + rand.nextDouble() * 9500) * 100.0) / 100.0;
                LocalDate date = startDate.plusDays(rand.nextInt(365));
                pstmt.setString(1, category);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, date.toString());
                pstmt.setString(4, "Expense for " + category + " - " + date);
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Tax Records (FR2) ----------
            String insertTaxRecord = "INSERT INTO tax_records (order_id, gross_amount, tax_rate, tax_amount, net_amount, record_date) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertTaxRecord);
            for (int orderId = 1; orderId <= 150; orderId++) {
                double grossAmount = Math.round((50 + rand.nextDouble() * 4000) * 100.0) / 100.0;
                double taxRate = 20.0;
                double taxAmount = Math.round((grossAmount * taxRate / 100) * 100.0) / 100.0;
                double netAmount = grossAmount - taxAmount;
                LocalDate date = startDate.plusDays(rand.nextInt(365));
                pstmt.setInt(1, orderId);
                pstmt.setDouble(2, grossAmount);
                pstmt.setDouble(3, taxRate);
                pstmt.setDouble(4, taxAmount);
                pstmt.setDouble(5, netAmount);
                pstmt.setString(6, date.toString());
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Tax Settings (FR2) ----------
            String insertTaxSetting = "INSERT INTO tax_settings (vat_rate, last_updated) VALUES (?, ?)";
            pstmt = conn.prepareStatement(insertTaxSetting);
            pstmt.setDouble(1, 20.0);
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.executeUpdate();
            pstmt.close();

            // ---------- Insert Invoices (FR3) ----------
            String insertInvoice = "INSERT INTO invoices (order_id, customer_id, invoice_number, file_name, generate_date, status, file_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertInvoice);
            for (int orderId = 1; orderId <= 100; orderId++) {
                int customerId = 1 + rand.nextInt(10);
                String invoiceNumber = "INV-" + String.format("%08d", (100000 + orderId));
                String fileName = invoiceNumber + ".pdf";
                LocalDate date = startDate.plusDays(rand.nextInt(365));
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, customerId);
                pstmt.setString(3, invoiceNumber);
                pstmt.setString(4, fileName);
                pstmt.setString(5, date.toString());
                pstmt.setString(6, "Generated");
                pstmt.setString(7, "/invoices/" + fileName);
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Refund Records (FR5) ----------
            String[] refundReasons = {"Damaged Product", "Wrong Item", "Customer Changed Mind", "Late Delivery", "Defective Product"};
            String insertRefund = "INSERT INTO refund_records (order_id, customer_id, amount, reason, refund_date, status) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertRefund);
            for (int i = 0; i < 30; i++) {
                int orderId = 1 + rand.nextInt(150);
                int customerId = 1 + rand.nextInt(10);
                double amount = Math.round((50 + rand.nextDouble() * 1000) * 100.0) / 100.0;
                String reason = refundReasons[rand.nextInt(refundReasons.length)];
                LocalDate date = startDate.plusDays(rand.nextInt(365));
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, customerId);
                pstmt.setDouble(3, amount);
                pstmt.setString(4, reason);
                pstmt.setString(5, date.toString());
                pstmt.setString(6, "Completed");
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Budget Entries (FR7) ----------
            String[] budgetCategories = {"Marketing", "Payroll", "Rent", "Utilities", "Software", "Office Supplies", "Travel"};
            String insertBudget = "INSERT INTO budget_entries (category, planned_amount, period, created_date) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertBudget);
            for (String category : budgetCategories) {
                for (int month = 1; month <= 6; month++) {
                    String period = String.format("2025-%02d", month);
                    double amount = Math.round((500 + rand.nextDouble() * 9500) * 100.0) / 100.0;
                    pstmt.setString(1, category);
                    pstmt.setDouble(2, amount);
                    pstmt.setString(3, period);
                    pstmt.setString(4, LocalDate.now().toString());
                    pstmt.executeUpdate();
                }
            }
            pstmt.close();

            // ---------- Insert Instalment Plans (FR8) ----------
            String insertPlan = "INSERT INTO instalment_plans (customer_id, order_id, total_value, instalments_count, instalment_amount, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertPlan);
            for (int i = 0; i < 30; i++) {
                int customerId = 1 + rand.nextInt(10);
                int orderId = 1 + rand.nextInt(150);
                double totalValue = Math.round((500 + rand.nextDouble() * 3000) * 100.0) / 100.0;
                int count = 3 + rand.nextInt(10);
                double instalmentAmount = Math.round((totalValue / count) * 100.0) / 100.0;
                LocalDate start = LocalDate.now().minusMonths(rand.nextInt(12));
                LocalDate end = start.plusMonths(count);
                String status = rand.nextBoolean() ? "ACTIVE" : "PAID";
                pstmt.setInt(1, customerId);
                pstmt.setInt(2, orderId);
                pstmt.setDouble(3, totalValue);
                pstmt.setInt(4, count);
                pstmt.setDouble(5, instalmentAmount);
                pstmt.setString(6, start.toString());
                pstmt.setString(7, end.toString());
                pstmt.setString(8, status);
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Payment Receipts (FR8) ----------
            String insertReceipt = "INSERT INTO payment_receipts (plan_id, amount, payment_date, billing_month, status) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertReceipt);
            for (int planId = 1; planId <= 30; planId++) {
                int receiptCount = 3 + rand.nextInt(8);
                for (int j = 0; j < receiptCount; j++) {
                    double amount = Math.round((100 + rand.nextDouble() * 500) * 100.0) / 100.0;
                    LocalDate date = LocalDate.now().minusMonths(rand.nextInt(6));
                    String billingMonth = String.format("%04d-%02d", date.getYear(), date.getMonthValue());
                    pstmt.setInt(1, planId);
                    pstmt.setDouble(2, amount);
                    pstmt.setString(3, date.toString());
                    pstmt.setString(4, billingMonth);
                    pstmt.setString(5, "PAID");
                    pstmt.executeUpdate();
                }
            }
            pstmt.close();

            // ---------- Insert Trade-In Valuations (FR9) ----------
            String[] models = {"iPhone 14 Pro", "iPhone 14", "iPhone 13 Pro", "iPhone 13", "MacBook Pro 2022", "MacBook Air 2022", "iPad Pro 2022", "Apple Watch 8"};
            String[] grades = {"Excellent", "Good", "Fair", "Poor"};
            String insertValuation = "INSERT INTO trade_in_valuations (device_model, condition_grade, assessed_value) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(insertValuation);
            for (String model : models) {
                for (String grade : grades) {
                    double value = 50 + rand.nextDouble() * 500;
                    pstmt.setString(1, model);
                    pstmt.setString(2, grade);
                    pstmt.setDouble(3, Math.round(value * 100.0) / 100.0);
                    pstmt.executeUpdate();
                }
            }
            pstmt.close();

            // ---------- Insert Trade-In Records (FR9) ----------
            String insertTrade = "INSERT INTO trade_in_records (order_id, customer_id, device_model, condition_grade, assessed_value, trade_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertTrade);
            for (int i = 0; i < 20; i++) {
                int orderId = 1 + rand.nextInt(150);
                int customerId = 1 + rand.nextInt(10);
                String model = models[rand.nextInt(models.length)];
                String grade = grades[rand.nextInt(grades.length)];
                double value = 50 + rand.nextDouble() * 500;
                LocalDate date = startDate.plusDays(rand.nextInt(365));
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, customerId);
                pstmt.setString(3, model);
                pstmt.setString(4, grade);
                pstmt.setDouble(5, Math.round(value * 100.0) / 100.0);
                pstmt.setString(6, date.toString());
                pstmt.setString(7, "Completed");
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Product Add-Ons (FR10) ----------
            String[] addonTypes = {"Warranty", "Insurance", "Extended_Warranty"};
            String insertAddon = "INSERT INTO product_addons (order_id, product_id, addon_type, premium_amount, duration_months, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertAddon);
            for (int i = 0; i < 80; i++) {
                int orderId = 1 + rand.nextInt(150);
                int productId = 1 + rand.nextInt(20);
                String type = addonTypes[rand.nextInt(addonTypes.length)];
                double premium = Math.round((10 + rand.nextDouble() * 100) * 100.0) / 100.0;
                int duration = 12 + rand.nextInt(24);
                LocalDate start = LocalDate.now().minusMonths(rand.nextInt(6));
                LocalDate end = start.plusMonths(duration);
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, productId);
                pstmt.setString(3, type);
                pstmt.setDouble(4, premium);
                pstmt.setInt(5, duration);
                pstmt.setString(6, start.toString());
                pstmt.setString(7, end.toString());
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Campaigns (FR11) ----------
            String[] campaignNames = {"Summer Sale 2025", "Black Friday 2025", "Christmas Promo", "New Year Discount", "Spring Collection", "Student Discount"};
            String insertCampaign = "INSERT INTO campaigns (name, description, cost, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertCampaign);
            for (String name : campaignNames) {
                double cost = Math.round((500 + rand.nextDouble() * 5000) * 100.0) / 100.0;
                LocalDate start = LocalDate.of(2025, 1 + rand.nextInt(6), 1);
                LocalDate end = start.plusMonths(2 + rand.nextInt(4));
                pstmt.setString(1, name);
                pstmt.setString(2, "Campaign for " + name);
                pstmt.setDouble(3, cost);
                pstmt.setString(4, start.toString());
                pstmt.setString(5, end.toString());
                pstmt.setString(6, "Active");
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Campaign Orders (FR11) ----------
            String insertCampaignOrder = "INSERT INTO campaign_orders (campaign_id, order_id, discount_applied) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(insertCampaignOrder);
            for (int campaignId = 1; campaignId <= 6; campaignId++) {
                for (int i = 0; i < 10; i++) {
                    int orderId = 1 + rand.nextInt(150);
                    double discount = Math.round((10 + rand.nextDouble() * 200) * 100.0) / 100.0;
                    pstmt.setInt(1, campaignId);
                    pstmt.setInt(2, orderId);
                    pstmt.setDouble(3, discount);
                    pstmt.executeUpdate();
                }
            }
            pstmt.close();

            // ---------- Insert Audit Logs (FR5) ----------
            String[] eventTypes = {"REFUND", "TRADE_IN", "DISCOUNT", "TAX_CORRECTION", "INVOICE_GENERATED", "PAYMENT_RECEIVED"};
            String insertAudit = "INSERT INTO audit_logs (event_type, entity_id, admin_id, details, timestamp) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertAudit);
            for (int i = 0; i < 100; i++) {
                String event = eventTypes[rand.nextInt(eventTypes.length)];
                int entityId = 1 + rand.nextInt(100);
                int adminId = 1;
                String details = event + " processed for entity " + entityId;
                LocalDateTime ts = LocalDateTime.now().minusDays(rand.nextInt(365));
                pstmt.setString(1, event);
                pstmt.setInt(2, entityId);
                pstmt.setInt(3, adminId);
                pstmt.setString(4, details);
                pstmt.setString(5, ts.toString());
                pstmt.executeUpdate();
            }
            pstmt.close();

            // ---------- Insert Financial Adjustments (FR5) ----------
            String[] adjTypes = {"REFUND", "TRADE_IN", "DISCOUNT", "TAX_CORRECTION"};
            String insertAdj = "INSERT INTO financial_adjustments (order_id, adj_type, value, timestamp) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertAdj);
            for (int i = 0; i < 50; i++) {
                int orderId = 1 + rand.nextInt(150);
                String type = adjTypes[rand.nextInt(adjTypes.length)];
                double value = Math.round((-50 - rand.nextDouble() * 500) * 100.0) / 100.0;
                LocalDateTime ts = LocalDateTime.now().minusDays(rand.nextInt(365));
                pstmt.setInt(1, orderId);
                pstmt.setString(2, type);
                pstmt.setDouble(3, value);
                pstmt.setString(4, ts.toString());
                pstmt.executeUpdate();
            }
            pstmt.close();

            System.out.println("All finance sample data seeded successfully!");
        } catch (SQLException e) {
            System.err.println("Seeding error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}