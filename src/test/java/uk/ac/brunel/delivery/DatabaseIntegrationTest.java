package uk.ac.brunel.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.brunel.delivery.db.AuthRepository;
import uk.ac.brunel.delivery.db.DatabaseManager;
import uk.ac.brunel.delivery.db.DeliveryRepository;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.model.Driver;
import uk.ac.brunel.delivery.model.OrderShipmentEvent;
import uk.ac.brunel.delivery.service.DeliveryEventBus;
import uk.ac.brunel.delivery.service.OrderShipmentIntegrationService;
import uk.ac.brunel.delivery.service.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIntegrationTest {
    private DeliveryRepository deliveries;
    private AuthRepository auth;

    @BeforeEach
    void setUp() throws IOException {
        Path testDirectory = Path.of("target", "junit-data");
        Files.createDirectories(testDirectory);
        Path databasePath = testDirectory.resolve("test-" + UUID.randomUUID() + ".db");
        DatabaseManager database = new DatabaseManager(databasePath);
        database.initialize();
        deliveries = new DeliveryRepository(database);
        auth = new AuthRepository(database);
    }

    @Test
    void authenticatesAdminAndDriverCredentials() {
        assertTrue(auth.authenticate("admin", "Admin123!").isPresent());
        assertTrue(auth.authenticate("priya", "Driver123!").orElseThrow().isDriver());
        assertTrue(auth.authenticate("admin", "wrong-password").isEmpty());
    }

    @Test
    void createsDeliveryFromShippedOrderExactlyOnceAndSupportsSampleFallback() {
        OrderShipmentIntegrationService orders =
                new OrderShipmentIntegrationService(deliveries, DeliveryEventBus.headless());
        OrderShipmentEvent event = new OrderShipmentEvent(
                "ORD-SHIPPED-9001", "22 Market Street", "Central London Hub", 45);

        Delivery created = orders.onOrderMarkedShipped(event);
        Delivery duplicateEventResult = orders.onOrderMarkedShipped(event);

        assertEquals(created.deliveryId(), duplicateEventResult.deliveryId());
        assertEquals(1, deliveries.findAll("ORD-SHIPPED-9001", null).size());
        assertEquals(DeliveryStatus.PENDING, created.status());
        assertTrue(deliveries.findStatusLogs(created.deliveryId()).getFirst().note().contains("Orders component"));

        Delivery sample = deliveries.generateSampleOrder("unit-test");
        assertNotNull(sample.deliveryId());
        assertEquals("Unassigned", sample.driverDisplayName());
        assertTrue(sample.deliveryId().startsWith("DEL-"));
        assertFalse(deliveries.findStatusLogs(sample.deliveryId()).isEmpty());
    }

    @Test
    void rejectsOverlappingDriverSchedule() {
        Delivery unassigned = deliveries.findById("DEL-0001").orElseThrow();
        Delivery existing = deliveries.findById("DEL-0002").orElseThrow();
        Driver alex = deliveries.findDrivers().stream()
                .filter(driver -> driver.driverId().equals("DRV-001"))
                .findFirst().orElseThrow();

        ValidationException error = assertThrows(ValidationException.class, () ->
                deliveries.assignDriver(unassigned, alex, existing.scheduledStart().plusMinutes(5),
                        existing.estimatedMinutes(), "unit-test"));
        assertTrue(error.getMessage().toLowerCase().contains("overlap"));
    }

    @Test
    void preventsDeliveryStartingBeforeScheduledTime() {
        ValidationException error = assertThrows(ValidationException.class, () ->
                deliveries.updateStatus("DEL-0002", DeliveryStatus.IN_TRANSIT, "unit-test", "early start"));
        assertTrue(error.getMessage().toLowerCase().contains("cannot start before"));
    }

    @Test
    void preventsTerminalDeliveryBeingAdvancedAgain() {
        ValidationException error = assertThrows(ValidationException.class, () ->
                deliveries.updateStatus("DEL-0005", DeliveryStatus.FAILED, "unit-test", "invalid retry"));
        assertTrue(error.getMessage().toLowerCase().contains("cannot be advanced"));
    }

    @Test
    void searchesAndFiltersPendingDeliveries() {
        var result = deliveries.findAll("Oxford", DeliveryStatus.PENDING);
        assertEquals(1, result.size());
        assertEquals("DEL-0001", result.getFirst().deliveryId());
    }

    @Test
    void returnsDriverQueueWithActiveDeliveryBeforePendingWork() {
        var queue = deliveries.findForDriver("DRV-002");
        assertFalse(queue.isEmpty());
        assertTrue(queue.getFirst().status().isActive());
    }
}
