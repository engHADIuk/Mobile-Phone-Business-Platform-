package uk.ac.brunel.delivery.model;

import java.time.LocalDateTime;

public record StatusLog(
        long logId,
        String deliveryId,
        DeliveryStatus fromStatus,
        DeliveryStatus toStatus,
        LocalDateTime changedAt,
        String changedBy,
        String note
) {
    public String fromDisplay() {
        return fromStatus == null ? "-" : fromStatus.getDisplayName();
    }
}
