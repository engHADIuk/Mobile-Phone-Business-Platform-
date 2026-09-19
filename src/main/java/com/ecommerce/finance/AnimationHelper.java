package com.ecommerce.finance;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Small reusable entrance-animation helper shared across screens.
 * Purely presentational — never touches data or business logic.
 */
public class AnimationHelper {

    public static void fadeSlideIn(Node node, double delayMillis) {
        node.setOpacity(0);
        node.setTranslateY(16);

        FadeTransition fade = new FadeTransition(Duration.millis(420), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(Duration.millis(420), node);
        slide.setFromY(16);
        slide.setToY(0);

        ParallelTransition anim = new ParallelTransition(fade, slide);
        anim.setDelay(Duration.millis(delayMillis));
        anim.play();
    }

    /** Fades/slides a row of nodes in with an increasing stagger delay. */
    public static void staggerIn(Node... nodes) {
        double delay = 0;
        for (Node node : nodes) {
            fadeSlideIn(node, delay);
            delay += 70;
        }
    }
}
