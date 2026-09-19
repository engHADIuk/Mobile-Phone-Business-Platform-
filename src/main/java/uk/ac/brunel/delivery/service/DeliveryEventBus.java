package uk.ac.brunel.delivery.service;

import javafx.application.Platform;

import java.util.concurrent.CopyOnWriteArrayList;

public final class DeliveryEventBus {
    private final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
    private final boolean dispatchOnJavaFxThread;

    public DeliveryEventBus() {
        this(true);
    }

    private DeliveryEventBus(boolean dispatchOnJavaFxThread) {
        this.dispatchOnJavaFxThread = dispatchOnJavaFxThread;
    }

    public static DeliveryEventBus headless() {
        return new DeliveryEventBus(false);
    }

    public Subscription subscribe(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void publish() {
        Runnable notification = () -> listeners.forEach(Runnable::run);
        if (!dispatchOnJavaFxThread) {
            notification.run();
            return;
        }
        try {
            if (Platform.isFxApplicationThread()) {
                notification.run();
            } else {
                Platform.runLater(notification);
            }
        } catch (RuntimeException ignored) {
            // Automated/headless integrations have no JavaFX toolkit; notify synchronously.
            notification.run();
        }
    }

    @FunctionalInterface
    public interface Subscription extends AutoCloseable {
        @Override
        void close();
    }
}
