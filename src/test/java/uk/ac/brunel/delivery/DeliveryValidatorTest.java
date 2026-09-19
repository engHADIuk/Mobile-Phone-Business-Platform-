package uk.ac.brunel.delivery;

import org.junit.jupiter.api.Test;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.model.Driver;
import uk.ac.brunel.delivery.service.DeliveryValidator;
import uk.ac.brunel.delivery.service.ValidationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeliveryValidatorTest {
    private final Delivery delivery = new Delivery(
            "DEL-TEST", "ORD-TEST", null, null,
            "7 Oxford Street", "West London Hub",
            null, null, 45, DeliveryStatus.PENDING, 0,
            LocalDateTime.now(), null, null
    );

    @Test
    void rejectsPastScheduledStart() {
        Driver driver = new Driver("DRV-TEST", "Test Driver", "test", true, 0.5, 0.5);
        assertThrows(ValidationException.class, () -> DeliveryValidator.validateAssignment(
                delivery, driver, LocalDateTime.now().minusMinutes(10), 45));
    }

    @Test
    void rejectsEstimatedTimeOutsideAllowedRange() {
        Driver driver = new Driver("DRV-TEST", "Test Driver", "test", true, 0.5, 0.5);
        assertThrows(ValidationException.class, () -> DeliveryValidator.validateAssignment(
                delivery, driver, LocalDateTime.now().plusMinutes(10), 10));
        assertThrows(ValidationException.class, () -> DeliveryValidator.validateAssignment(
                delivery, driver, LocalDateTime.now().plusMinutes(10), 600));
    }

    @Test
    void rejectsUnavailableDriver() {
        Driver driver = new Driver("DRV-TEST", "Test Driver", "test", false, 0.5, 0.5);
        assertThrows(ValidationException.class, () -> DeliveryValidator.validateAssignment(
                delivery, driver, LocalDateTime.now().plusMinutes(10), 45));
    }
}
