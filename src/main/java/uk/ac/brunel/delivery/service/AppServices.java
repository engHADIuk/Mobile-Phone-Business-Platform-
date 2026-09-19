package uk.ac.brunel.delivery.service;

import uk.ac.brunel.delivery.db.AuthRepository;
import uk.ac.brunel.delivery.db.DatabaseManager;
import uk.ac.brunel.delivery.db.DeliveryRepository;

import java.nio.file.Path;

public final class AppServices {
    private static DatabaseManager database;
    private static AuthRepository authRepository;
    private static DeliveryRepository deliveryRepository;
    private static DeliveryEventBus eventBus;
    private static OrderShipmentIntegrationService orderShipmentIntegrationService;
    private static DeliverySimulationService simulationService;

    private AppServices() {
    }

    public static synchronized void initialize() {
        if (database != null) {
            return;
        }
        Path databasePath = Path.of(System.getProperty("velmora.db.path", "velmora-delivery.db"));
        database = new DatabaseManager(databasePath);
        database.initialize();
        authRepository = new AuthRepository(database);
        deliveryRepository = new DeliveryRepository(database);
        eventBus = new DeliveryEventBus();
        orderShipmentIntegrationService = new OrderShipmentIntegrationService(deliveryRepository, eventBus);
        simulationService = new DeliverySimulationService(deliveryRepository, eventBus);
        simulationService.start();
    }

    public static AuthRepository auth() {
        requireInitialised();
        return authRepository;
    }

    public static DeliveryRepository deliveries() {
        requireInitialised();
        return deliveryRepository;
    }

    public static DeliveryEventBus events() {
        requireInitialised();
        return eventBus;
    }

    public static OrderShipmentIntegrationService orderShipments() {
        requireInitialised();
        return orderShipmentIntegrationService;
    }

    public static synchronized void shutdown() {
        if (simulationService != null) {
            simulationService.shutdown();
        }
        simulationService = null;
        orderShipmentIntegrationService = null;
        eventBus = null;
        deliveryRepository = null;
        authRepository = null;
        database = null;
        SessionContext.logout();
    }

    private static void requireInitialised() {
        if (database == null) {
            throw new IllegalStateException("Application services have not been initialised");
        }
    }
}
