package uk.ac.brunel.delivery;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.util.SceneManager;

public final class DeliveryApplication extends Application {
    @Override
    public void start(Stage stage) {
        AppServices.initialize();
        SceneManager.getInstance().initialize(stage);
        SceneManager.getInstance().showLaunchpad();
    }

    @Override
    public void stop() {
        AppServices.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
