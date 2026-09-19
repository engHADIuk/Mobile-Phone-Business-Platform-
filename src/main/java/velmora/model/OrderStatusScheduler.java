package velmora.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class OrderStatusScheduler {

    private static final int PROCESSING_SECONDS = 20;
    private static final int SHIPPED_SECONDS = 20;
    private static final int TICK_SECONDS = 20;

    private static Timeline timeline;

    public static void start() {
        if (timeline != null) return;

        timeline = new Timeline(new KeyFrame(Duration.seconds(TICK_SECONDS), event -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private static void tick() {
        for (Order order : OrderHistory.getInstance().getOrders()) {
            if (order.getStatus() == Order.Status.PROCESSING && order.secondsSinceUpdate() >= PROCESSING_SECONDS) {
                OrderHistory.getInstance().updateStatus(order, Order.Status.SHIPPED);
            } else if (order.getStatus() == Order.Status.SHIPPED && order.secondsSinceUpdate() >= SHIPPED_SECONDS) {
                OrderHistory.getInstance().updateStatus(order, Order.Status.DELIVERED);
            }
        }
    }
}
