package uk.ac.brunel.delivery.model;

import java.util.Locale;

public enum DeliveryStatus {
    PENDING("Pending", 0),
    IN_TRANSIT("In Transit", 45),
    OUT_FOR_DELIVERY("Out for Delivery", 78),
    DELIVERED("Delivered", 100),
    FAILED("Failed", 60);

    private final String displayName;
    private final int progress;

    DeliveryStatus(String displayName, int progress) {
        this.displayName = displayName;
        this.progress = progress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isActive() {
        return this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED;
    }

    public DeliveryStatus next(boolean successful) {
        return switch (this) {
            case PENDING -> IN_TRANSIT;
            case IN_TRANSIT -> OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> successful ? DELIVERED : FAILED;
            case DELIVERED, FAILED -> this;
        };
    }

    public static DeliveryStatus fromDatabase(String value) {
        return DeliveryStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
