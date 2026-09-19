package uk.ac.brunel.delivery;

import org.junit.jupiter.api.Test;
import uk.ac.brunel.delivery.model.DeliveryStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryStatusTest {
    @Test
    void followsRequiredAssessmentSequence() {
        assertEquals(DeliveryStatus.IN_TRANSIT, DeliveryStatus.PENDING.next(true));
        assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.IN_TRANSIT.next(true));
        assertEquals(DeliveryStatus.DELIVERED, DeliveryStatus.OUT_FOR_DELIVERY.next(true));
        assertEquals(DeliveryStatus.FAILED, DeliveryStatus.OUT_FOR_DELIVERY.next(false));
        assertTrue(DeliveryStatus.DELIVERED.isTerminal());
        assertTrue(DeliveryStatus.FAILED.isTerminal());
    }
}
