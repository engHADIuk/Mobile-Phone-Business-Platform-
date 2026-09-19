package com.ecommerce.finance.models;

import java.time.LocalDateTime;

public class AuditLog {
    private int logId;
    private String eventType;
    private int entityId;
    private int adminId;
    private String details;
    private LocalDateTime timestamp;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public int getEntityId() { return entityId; }
    public void setEntityId(int entityId) { this.entityId = entityId; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}