package uk.ac.brunel.delivery.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uk.ac.brunel.delivery.DeliveryApplication;
import uk.ac.brunel.delivery.controller.AssignDeliveryController;
import uk.ac.brunel.delivery.controller.DeliveryDetailController;
import uk.ac.brunel.delivery.controller.LifecycleController;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.service.SessionContext;

import java.io.IOException;
import java.util.function.Consumer;

public final class SceneManager {
    private static final SceneManager INSTANCE = new SceneManager();

    private Stage stage;
    private Scene scene;
    private Object activeController;

    /**
     * Set by the host application (the Velmora launchpad) so this component can
     * hand the window back. Null when the component is run standalone, in which
     * case the "back" control simply does not appear.
     */
    private Runnable launchpadReturn;

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        return INSTANCE;
    }

    public void initialize(Stage primaryStage) {
        this.stage = primaryStage;
        this.scene = new Scene(new javafx.scene.layout.StackPane(), 1440, 900);
        this.stage.setScene(scene);
        this.stage.setTitle("VELMORA - Delivery & Logistics");
        this.stage.setMinWidth(1120);
        this.stage.setMinHeight(720);
    }

    /** Registers how this component returns to the launchpad it was opened from. */
    public void setLaunchpadReturn(Runnable launchpadReturn) {
        this.launchpadReturn = launchpadReturn;
    }

    /** True when this component was opened from the launchpad. */
    public boolean canReturnToLaunchpad() {
        return launchpadReturn != null;
    }

    /** Logs the current user out and hands the window back to the launchpad. */
    public void returnToLaunchpad() {
        if (launchpadReturn == null) {
            return;
        }
        if (activeController instanceof LifecycleController lifecycle) {
            lifecycle.onHide();
        }
        activeController = null;
        SessionContext.logout();
        launchpadReturn.run();
    }

    public void showLaunchpad() {
        switchTo("LaunchpadView.fxml", controller -> { });
    }

    public void showLogin() {
        switchTo("LoginView.fxml", controller -> { });
    }

    public void showAdminDashboard() {
        switchTo("AdminDashboardView.fxml", controller -> { });
    }

    public void showDeliveries() {
        switchTo("DeliveriesView.fxml", controller -> { });
    }

    public void showDriverDashboard() {
        switchTo("DriverDashboardView.fxml", controller -> { });
    }

    public void showLiveMap() {
        switchTo("LiveMapView.fxml", controller -> { });
    }

    public void showAssignDelivery(Delivery delivery) {
        switchTo("AssignDeliveryView.fxml", controller -> ((AssignDeliveryController) controller).setDelivery(delivery));
    }

    public void showDeliveryDetail(Delivery delivery) {
        switchTo("DeliveryDetailView.fxml", controller -> ((DeliveryDetailController) controller).setDelivery(delivery));
    }

    private <T> void switchTo(String fxml, Consumer<T> initializer) {
        try {
            if (activeController instanceof LifecycleController lifecycle) {
                lifecycle.onHide();
            }

            FXMLLoader loader = new FXMLLoader(DeliveryApplication.class.getResource("view/" + fxml));
            Parent root = loader.load();
            T controller = loader.getController();
            initializer.accept(controller);
            activeController = controller;
            scene.setRoot(root);

            if (controller instanceof LifecycleController lifecycle) {
                lifecycle.onShow();
            }
            if (!stage.isShowing()) {
                stage.show();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load JavaFX view: " + fxml, exception);
        }
    }
}
