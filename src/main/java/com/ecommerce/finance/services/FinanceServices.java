package com.ecommerce.finance.services;
import com.ecommerce.finance.models.*;
import java.sql.SQLException;
import com.ecommerce.finance.exceptions.*;
import com.ecommerce.finance.models.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FinanceServices {

    private FinanceDatabaseManager db = FinanceDatabaseManager.getInstance();

    // ================================================================
    // FR1: Sales Revenue Report
    // ================================================================
    public RevenueReport generateRevenueReport(LocalDate startDate, LocalDate endDate, String category)
            throws InvalidDateRangeException, Exception {

        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date must be before end date",
                    startDate.toString(), endDate.toString());
        }

        List<RevenueRow> rows = db.getRevenueRows(startDate, endDate, category);
        double grandTotal = rows.stream().mapToDouble(RevenueRow::getRevenue).sum();
        String period = startDate + " to " + endDate;

        return new RevenueReport(rows, grandTotal, period, category != null ? category : "All");
    }

    // ================================================================
    // FR2: Tax Calculation & Compliance Report
    // ================================================================
    public TaxReport generateComplianceReport(LocalDate startDate, LocalDate endDate)
            throws InvalidTaxRateException, Exception {

        double vatRate = db.getVATRate();
        if (vatRate < 0 || vatRate > 100) {
            throw new InvalidTaxRateException("VAT rate out of range", vatRate);
        }

        List<TaxRecord> records = db.getTaxRecords(startDate, endDate);
        double grossSales = records.stream().mapToDouble(TaxRecord::getGrossAmount).sum();
        double totalVAT = records.stream().mapToDouble(TaxRecord::getTaxAmount).sum();
        double netRevenue = records.stream().mapToDouble(TaxRecord::getGrossAmount)
                .map(g -> g - (g * vatRate / 100)).sum();

        return new TaxReport(startDate, endDate, records, grossSales, totalVAT, netRevenue);
    }

    public double computeTaxForOrder(Order order) {
        try {
            return order.getNetRevenue() * (db.getVATRate() / 100);
        } catch (SQLException e) {
            return order.getNetRevenue() * 0.20; // fallback
        }
    }

    // ================================================================
    // FR3: Automated Invoice Generation
    // ================================================================
    public Invoice generateInvoice(int orderId) throws DuplicateInvoiceException,
            MissingOrderFieldException, Exception {

        // Check for duplicate
        Invoice existing = db.findInvoiceByOrderId(orderId);
        if (existing != null) {
            throw new DuplicateInvoiceException("Invoice already exists for order", orderId);
        }

        Order order = db.getOrderWithItems(orderId);
        if (order == null) {
            throw new MissingOrderFieldException("Order not found", "order_id");
        }

        // Validate required fields
        if (order.getCustomerId() <= 0) {
            throw new MissingOrderFieldException("Customer ID missing", "customer_id");
        }
        if (order.getOrderDate() == null) {
            throw new MissingOrderFieldException("Order date missing", "order_date");
        }

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setOrderId(orderId);
        invoice.setCustomerId(order.getCustomerId());
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setFileName(invoice.getInvoiceNumber() + ".pdf");
        invoice.setGenerateDate(LocalDate.now());
        invoice.setStatus("Generated");
        invoice.setFilePath("/invoices/" + invoice.getFileName());

        db.insertInvoice(invoice);

        // Log the event
        AuditLog log = new AuditLog();
        log.setEventType("INVOICE_GENERATED");
        log.setEntityId(orderId);
        log.setAdminId(1);
        log.setDetails("Invoice generated for order " + orderId);
        log.setTimestamp(java.time.LocalDateTime.now());
        db.insertAuditLog(log);

        return invoice;
    }

    // ================================================================
    // FR4: Profit & Loss Statement
    // ================================================================
    public ProfitLossStatement generatePL(LocalDate startDate, LocalDate endDate) throws Exception {
        double totalRevenue = db.sumNetRevenueDelivered(startDate, endDate);
        double totalCOGS = db.sumCOGSDelivered(startDate, endDate);
        double totalExpenses = db.sumExpensesInPeriod(startDate, endDate);

        double grossProfit = totalRevenue - totalCOGS;
        double netProfit = grossProfit - totalExpenses;

        String profitMargin;
        if (totalRevenue == 0) {
            profitMargin = "N/A";
        } else {
            double margin = (netProfit / totalRevenue) * 100;
            profitMargin = String.format("%.2f%%", margin);
        }

        return new ProfitLossStatement(startDate, endDate, totalRevenue, totalCOGS,
                totalExpenses, grossProfit, netProfit, profitMargin);
    }

    // ================================================================
    // FR5: Refund & Return Financial Adjustment
    // ================================================================
    public RefundRecord processRefund(int orderId, int customerId, double amount, String reason)
            throws OrderNotFoundException, InvalidRefundAmountException, Exception {

        Order order = db.findOrderById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found", orderId);
        }

        if (!"Delivered".equals(order.getStatus())) {
            throw new OrderNotFoundException("Order not delivered", orderId);
        }

        if (amount > order.getTotalAmount()) {
            throw new InvalidRefundAmountException("Refund amount exceeds order total",
                    amount, order.getTotalAmount());
        }

        // Create refund record
        RefundRecord record = new RefundRecord();
        record.setOrderId(orderId);
        record.setCustomerId(customerId);
        record.setAmount(amount);
        record.setReason(reason);
        record.setRefundDate(LocalDate.now());
        record.setStatus("Completed");

        db.insertRefundRecord(record);

        // Create financial adjustment
        FinancialAdjustment adj = new FinancialAdjustment();
        adj.setOrderId(orderId);
        adj.setAdjType("REFUND");
        adj.setValue(-amount);
        adj.setTimestamp(java.time.LocalDateTime.now());
        db.insertFinancialAdjustment(adj);

        // Update order net revenue
        db.updateOrderNetRevenue(orderId, -amount);

        // Update tax record
        db.updateTaxRecord(orderId);

        // Audit log
        AuditLog log = new AuditLog();
        log.setEventType("REFUND");
        log.setEntityId(orderId);
        log.setAdminId(1);
        log.setDetails("Refund of $" + amount + " processed for order " + orderId);
        log.setTimestamp(java.time.LocalDateTime.now());
        db.insertAuditLog(log);

        return record;
    }

    // ================================================================
    // FR6: Payment Method Analytics
    // ================================================================
    public PaymentAnalyticsReport generatePaymentAnalytics(LocalDate startDate, LocalDate endDate)
            throws UnknownPaymentMethodAlert, Exception {

        List<Map<String, Object>> data = db.getTransactionsGroupedByMethod(startDate, endDate);
        double totalRevenue = db.getTotalRevenueInPeriod(startDate, endDate);

        Map<String, Double> methodRevenue = new HashMap<>();
        Map<String, Integer> txCounts = new HashMap<>();
        List<String> flaggedMethods = new ArrayList<>();

        for (Map<String, Object> row : data) {
            String method = (String) row.get("payment_method");
            double amount = (Double) row.get("total_amount");
            int count = (Integer) row.get("tx_count");

            // Check for unknown methods
            if (!isValidPaymentMethod(method)) {
                flaggedMethods.add(method);
                throw new UnknownPaymentMethodAlert("Unknown payment method detected", method);
            }

            methodRevenue.put(method, amount);
            txCounts.put(method, count);
        }

        // Calculate share percentages
        Map<String, Double> sharePercent = new HashMap<>();
        for (Map.Entry<String, Double> entry : methodRevenue.entrySet()) {
            double share = totalRevenue > 0 ? (entry.getValue() / totalRevenue) * 100 : 0;
            sharePercent.put(entry.getKey(), Math.round(share * 100.0) / 100.0);
        }

        return new PaymentAnalyticsReport(startDate, endDate, methodRevenue,
                txCounts, sharePercent, flaggedMethods);
    }

    private boolean isValidPaymentMethod(String method) {
        String[] valid = {"Credit Card", "PayPal", "Apple Pay", "Bank Transfer", "Instalment"};
        return Arrays.asList(valid).contains(method);
    }

    // ================================================================
    // FR7: Budget vs Actual Expenditure Tracker
    // ================================================================
    public BudgetVarianceReport generateVarianceReport(String period) throws InvalidBudgetEntryException, Exception {
        List<BudgetEntry> budgets = db.getBudgetEntriesByPeriod(period);
        List<BudgetVarianceRow> rows = new ArrayList<>();

        for (BudgetEntry budget : budgets) {
            double actual = db.sumExpensesByCategory(budget.getCategory(), period);
            double variance = actual - budget.getPlannedAmount();

            String percentVariance;
            if (budget.getPlannedAmount() == 0) {
                percentVariance = "N/A";
            } else {
                double pct = (variance / budget.getPlannedAmount()) * 100;
                percentVariance = String.format("%.2f%%", pct);
            }

            String colourCode = variance > 0 ? "RED" : "GREEN";

            rows.add(new BudgetVarianceRow(
                    budget.getCategory(),
                    budget.getPlannedAmount(),
                    actual,
                    variance,
                    percentVariance,
                    colourCode
            ));
        }

        return new BudgetVarianceReport(rows);
    }

    // ================================================================
    // FR8: Instalment Payment Plan Tracker
    // ================================================================
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startNightlyOverdueCheck() {
        scheduler.scheduleAtFixedRate(this::runNightlyOverdueCheck, 0, 1, TimeUnit.DAYS);
    }

    public void stopNightlyOverdueCheck() {
        scheduler.shutdown();
    }

    private void runNightlyOverdueCheck() {
        try {
            List<InstalmentPlan> plans = db.getActivePlans();
            for (InstalmentPlan plan : plans) {
                double paid = db.sumReceiptsForPlan(plan.getPlanId());
                double outstanding = plan.getTotalValue() - paid;

                if (outstanding > 0 && plan.getEndDate().isBefore(LocalDate.now())) {
                    // Mark as overdue
                    db.updatePlanStatus(plan.getPlanId(), "OVERDUE");

                    // Create alert
                    FinancialAlert alert = new FinancialAlert();
                    alert.setCustomerId(plan.getCustomerId());
                    alert.setPlanId(plan.getPlanId());
                    alert.setAlertType("OVERDUE");
                    alert.setDaysOverdue((int) java.time.temporal.ChronoUnit.DAYS.between(
                            plan.getEndDate(), LocalDate.now()));
                    alert.setOutstanding(outstanding);
                    alert.setAlertDate(LocalDate.now());
                    db.insertFinancialAlert(alert);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================================================
    // FR9: Trade-In Device Valuation & Revenue Adjustment
    // ================================================================
    public TradeInRecord processTradeIn(int orderId, int customerId, String deviceModel,
                                        String conditionGrade, boolean adminApproved)
            throws OrderNotFoundException, TradeInValuationNotFoundException, Exception {

        Order order = db.findOrderById(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found", orderId);
        }

        double assessedValue = db.lookupTradeInValue(deviceModel, conditionGrade);
        if (assessedValue <= 0) {
            throw new TradeInValuationNotFoundException("No valuation found for device",
                    deviceModel, conditionGrade);
        }

        // Check if admin approval needed (if value > order total)
        if (assessedValue > order.getTotalAmount() && !adminApproved) {
            throw new Exception("Admin approval required for this trade-in");
        }

        // Create trade-in record
        TradeInRecord record = new TradeInRecord();
        record.setOrderId(orderId);
        record.setCustomerId(customerId);
        record.setDeviceModel(deviceModel);
        record.setConditionGrade(conditionGrade);
        record.setAssessedValue(assessedValue);
        record.setTradeDate(LocalDate.now());
        record.setStatus("Completed");
        db.insertTradeInRecord(record);

        // Financial adjustment
        FinancialAdjustment adj = new FinancialAdjustment();
        adj.setOrderId(orderId);
        adj.setAdjType("TRADE_IN");
        adj.setValue(-assessedValue);
        adj.setTimestamp(java.time.LocalDateTime.now());
        db.insertFinancialAdjustment(adj);

        db.updateOrderNetRevenue(orderId, -assessedValue);

        // Audit log
        AuditLog log = new AuditLog();
        log.setEventType("TRADE_IN");
        log.setEntityId(orderId);
        log.setAdminId(1);
        log.setDetails("Trade-in of " + deviceModel + " valued at $" + assessedValue + " processed");
        log.setTimestamp(java.time.LocalDateTime.now());
        db.insertAuditLog(log);

        return record;
    }

    // ================================================================
    // FR10: Warranty & Insurance Add-On Revenue Report
    // ================================================================
    public WarrantyReport generateWarrantyReport(LocalDate startDate, LocalDate endDate)
            throws InvalidPremiumAmountException, Exception {

        List<Map<String, Object>> data = db.getAddOnJoinedData(startDate, endDate);
        List<WarrantyRow> rows = new ArrayList<>();
        double totalRevenue = 0;

        for (Map<String, Object> row : data) {
            double premium = (Double) row.get("premium_amount");
            String status = (String) row.get("status");

            // Skip refunded orders
            if ("Refunded".equals(status)) continue;

            if (premium <= 0) {
                throw new InvalidPremiumAmountException("Invalid premium amount",
                        (Integer) row.get("addon_id"), premium);
            }

            String addonType = (String) row.get("addon_type");
            String productName = (String) row.get("product_name");

            // Group by type and product
            rows.add(new WarrantyRow(addonType, productName, premium));
            totalRevenue += premium;
        }

        return new WarrantyReport(rows, totalRevenue);
    }

    // ================================================================
    // FR11: Promotional Discount & Campaign ROI Report
    // ================================================================
    public CampaignROIReport generateROIReport(int campaignId)
            throws CampaignNotFoundException, Exception {

        Campaign campaign = db.findCampaignById(campaignId);
        if (campaign == null) {
            throw new CampaignNotFoundException("Campaign not found", campaignId);
        }

        List<CampaignOrder> orders = db.getCampaignOrders(campaignId);
        double totalDiscounts = db.sumDiscountByCampaign(campaignId);
        double totalRevenue = db.sumRevenueByCampaign(campaignId);

        double roi;
        String roiLabel;
        if (campaign.getCost() == 0) {
            roi = 0;
            roiLabel = "N/A";
        } else {
            roi = ((totalRevenue - totalDiscounts - campaign.getCost()) / campaign.getCost()) * 100;
            roiLabel = String.format("%.2f%%", roi);
        }

        return new CampaignROIReport(
                campaignId,
                campaign.getName(),
                campaign.getCost(),
                totalRevenue,
                totalDiscounts,
                roi,
                roiLabel,
                orders
        );
    }
}