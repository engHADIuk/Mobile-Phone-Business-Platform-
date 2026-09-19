package com.ecommerce.finance.models;

import java.time.LocalDate;

public class Invoice {
    private int invoiceId;
    private int orderId;
    private int customerId;
    private String invoiceNumber;
    private String fileName;
    private LocalDate generateDate;
    private String status;
    private String filePath;

    // Getters and Setters
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public LocalDate getGenerateDate() { return generateDate; }
    public void setGenerateDate(LocalDate generateDate) { this.generateDate = generateDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public boolean isGenerated() {
        return "Generated".equals(status);
    }
}