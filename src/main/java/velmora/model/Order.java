package velmora.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Order {

    public enum Status {
        PROCESSING("Processing"),
        SHIPPED("Shipped"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled"),
        RETURNED("Returned");

        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final String id;
    private final String summary;
    private final LocalDate date;
    private final double total;
    private Status status;
    private final String customerName;
    private final String customerEmail;
    private LocalDateTime updatedAt;
    private final String shippingAddress;
    private final String cardLast4;

    public Order(String id, String summary, LocalDate date, double total, Status status,
                 String customerName, String customerEmail, LocalDateTime updatedAt,
                 String shippingAddress, String cardLast4) {
        this.id = id;
        this.summary = summary;
        this.date = date;
        this.total = total;
        this.status = status;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.updatedAt = updatedAt;
        this.shippingAddress = shippingAddress;
        this.cardLast4 = cardLast4;
    }

    public String getId() { return id; }
    public String getSummary() { return summary; }
    public LocalDate getDate() { return date; }
    public String getFormattedDate() { return date.format(DATE_FORMAT); }
    public double getTotal() { return total; }
    public Status getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getShippingAddress() { return shippingAddress; }
    public String getCardLast4() { return cardLast4; }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public long secondsSinceUpdate() {
        return ChronoUnit.SECONDS.between(updatedAt, LocalDateTime.now());
    }

    public String getStatusStyleClass() {
        return "status-" + status.name().toLowerCase();
    }
}
