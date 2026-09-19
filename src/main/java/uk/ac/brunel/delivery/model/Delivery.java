package uk.ac.brunel.delivery.model;

import java.time.LocalDateTime;

public record Delivery(
        String deliveryId,
        String orderReference,
        String assignedDriverId,
        String driverName,
        String deliveryAddress,
        String warehouseLocation,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        int estimatedMinutes,
        DeliveryStatus status,
        int progress,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public String driverDisplayName() {
        return driverName == null || driverName.isBlank() ? "Unassigned" : driverName;
    }

    public String scheduledDisplay() {
        return scheduledStart == null ? "Not scheduled" : scheduledStart.toLocalDate() + " " + scheduledStart.toLocalTime();
    }
}
