package uk.ac.brunel.delivery.service;

import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.Driver;

import java.time.LocalDateTime;

public final class DeliveryValidator {
    private DeliveryValidator() {
    }

    public static void validateAssignment(Delivery delivery, Driver driver, LocalDateTime start, int estimatedMinutes) {
        if (delivery == null) {
            throw new ValidationException("Select a delivery before assigning a driver.");
        }
        if (driver == null) {
            throw new ValidationException("Select an available driver.");
        }
        if (!driver.available()) {
            throw new ValidationException("The selected driver is unavailable.");
        }
        if (start == null) {
            throw new ValidationException("Choose a scheduled start date and time.");
        }
        if (start.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidationException("A delivery cannot start in the past.");
        }
        if (estimatedMinutes < 15 || estimatedMinutes > 480) {
            throw new ValidationException("Estimated delivery time must be between 15 and 480 minutes.");
        }
        if (delivery.deliveryAddress() == null || delivery.deliveryAddress().trim().length() < 5) {
            throw new ValidationException("Delivery address must contain at least 5 characters.");
        }
        if (delivery.warehouseLocation() == null || delivery.warehouseLocation().trim().length() < 3) {
            throw new ValidationException("Warehouse location must contain at least 3 characters.");
        }
    }
}
