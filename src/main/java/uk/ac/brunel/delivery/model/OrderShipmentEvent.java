package uk.ac.brunel.delivery.model;

/**
 * Integration message published by the Orders component when an order becomes Shipped.
 */
public record OrderShipmentEvent(
        String orderReference,
        String deliveryAddress,
        String warehouseLocation,
        int estimatedMinutes
) {
    public OrderShipmentEvent {
        orderReference = requireText(orderReference, "Order reference", 3);
        deliveryAddress = requireText(deliveryAddress, "Delivery address", 5);
        warehouseLocation = requireText(warehouseLocation, "Warehouse location", 3);
        if (estimatedMinutes < 15 || estimatedMinutes > 480) {
            throw new IllegalArgumentException("Estimated delivery time must be between 15 and 480 minutes.");
        }
    }

    private static String requireText(String value, String label, int minimumLength) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() < minimumLength) {
            throw new IllegalArgumentException(label + " must contain at least " + minimumLength + " characters.");
        }
        return cleaned;
    }
}
