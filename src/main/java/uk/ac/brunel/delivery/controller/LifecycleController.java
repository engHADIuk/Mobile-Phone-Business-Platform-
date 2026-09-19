package uk.ac.brunel.delivery.controller;

public interface LifecycleController {
    default void onShow() {
    }

    default void onHide() {
    }
}
