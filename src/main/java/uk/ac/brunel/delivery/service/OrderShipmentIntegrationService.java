package uk.ac.brunel.delivery.service;

import uk.ac.brunel.delivery.db.DeliveryRepository;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.OrderShipmentEvent;

import java.util.Objects;

/**
 * Public integration boundary used by the Orders component.
 */
public final class OrderShipmentIntegrationService {
    private final DeliveryRepository deliveries;
    private final DeliveryEventBus events;

    public OrderShipmentIntegrationService(DeliveryRepository deliveries, DeliveryEventBus events) {
        this.deliveries = Objects.requireNonNull(deliveries);
        this.events = Objects.requireNonNull(events);
    }

    /**
     * Automatically creates the Pending delivery for a Shipped order. Repeated events for
     * the same order are idempotent and return the delivery that was already created.
     */
    public Delivery onOrderMarkedShipped(OrderShipmentEvent event) {
        Objects.requireNonNull(event, "Order shipment event is required");
        Delivery delivery = deliveries.createFromShippedOrder(
                event.orderReference(),
                event.deliveryAddress(),
                event.warehouseLocation(),
                event.estimatedMinutes(),
                "orders-component"
        );
        events.publish();
        return delivery;
    }
}
