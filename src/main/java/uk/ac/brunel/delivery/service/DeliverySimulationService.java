package uk.ac.brunel.delivery.service;

import uk.ac.brunel.delivery.db.DeliveryRepository;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.model.Driver;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class DeliverySimulationService {
    private final DeliveryRepository repository;
    private final DeliveryEventBus eventBus;
    private final Map<String, LocalDateTime> transitionDue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "velmora-delivery-simulator");
        thread.setDaemon(true);
        return thread;
    });
    private final int minimumStageSeconds = Integer.getInteger("velmora.simulation.minSeconds", 8);
    private final int maximumStageSeconds = Integer.getInteger("velmora.simulation.maxSeconds", 15);
    private volatile boolean started;

    public DeliverySimulationService(DeliveryRepository repository, DeliveryEventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        scheduler.scheduleWithFixedDelay(this::safeTick, 2, 2, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void safeTick() {
        try {
            tick();
        } catch (RuntimeException exception) {
            System.err.println("Delivery simulation tick skipped: " + exception.getMessage());
        }
    }

    private void tick() {
        LocalDateTime now = LocalDateTime.now();
        for (Driver driver : repository.findDrivers()) {
            Optional<Delivery> active = repository.findActiveForDriver(driver.driverId());
            if (active.isPresent()) {
                advanceWhenDue(active.get(), now);
            } else {
                startNextEligible(driver, now);
            }
        }
    }

    private void startNextEligible(Driver driver, LocalDateTime now) {
        repository.findNextEligibleForDriver(driver.driverId(), now).ifPresent(delivery -> {
            Delivery startedDelivery = repository.updateStatus(
                    delivery.deliveryId(), DeliveryStatus.IN_TRANSIT, "simulation-timer",
                    "Scheduled start reached; delivery started automatically"
            );
            transitionDue.put(startedDelivery.deliveryId(), nextDueTime());
            eventBus.publish();
        });
    }

    private void advanceWhenDue(Delivery delivery, LocalDateTime now) {
        LocalDateTime due = transitionDue.computeIfAbsent(delivery.deliveryId(), ignored -> nextDueTime());
        if (now.isBefore(due)) {
            return;
        }

        DeliveryStatus target;
        String note;
        if (delivery.status() == DeliveryStatus.IN_TRANSIT) {
            target = DeliveryStatus.OUT_FOR_DELIVERY;
            note = "Driver reached the final local delivery route";
        } else {
            boolean successful = ThreadLocalRandom.current().nextDouble() < 0.88;
            target = successful ? DeliveryStatus.DELIVERED : DeliveryStatus.FAILED;
            note = successful ? "Recipient confirmed delivery" : "Delivery attempt failed; review required";
        }

        Delivery updated = repository.updateStatus(delivery.deliveryId(), target, "simulation-timer", note);
        if (updated.status().isTerminal()) {
            transitionDue.remove(updated.deliveryId());
        } else {
            transitionDue.put(updated.deliveryId(), nextDueTime());
        }
        eventBus.publish();
    }

    private LocalDateTime nextDueTime() {
        int max = Math.max(minimumStageSeconds + 1, maximumStageSeconds + 1);
        return LocalDateTime.now().plusSeconds(ThreadLocalRandom.current().nextInt(minimumStageSeconds, max));
    }
}
