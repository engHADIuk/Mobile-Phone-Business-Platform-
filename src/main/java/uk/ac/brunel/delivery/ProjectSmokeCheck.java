package uk.ac.brunel.delivery;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.Driver;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.service.ValidationException;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Build-time smoke check used to verify database rules and every FXML/controller pair.
 * It is not part of the normal application flow.
 */
public final class ProjectSmokeCheck extends Application {
    private static final List<String> ADMIN_VIEWS = List.of(
            "LoginView.fxml", "AdminDashboardView.fxml", "DeliveriesView.fxml",
            "AssignDeliveryView.fxml", "DeliveryDetailView.fxml", "LiveMapView.fxml"
    );

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Path database = Files.createTempFile("velmora-smoke-", ".db");
            System.setProperty("velmora.db.path", database.toString());
            AppServices.initialize();

            var admin = AppServices.auth().authenticate("admin", "Admin123!").orElseThrow();
            SessionContext.login(admin);
            for (String view : ADMIN_VIEWS) {
                load(view);
            }

            Delivery unassigned = AppServices.deliveries().findById("DEL-0001").orElseThrow();
            Delivery existing = AppServices.deliveries().findById("DEL-0002").orElseThrow();
            Driver alex = AppServices.deliveries().findDrivers().stream()
                    .filter(driver -> driver.driverId().equals("DRV-001"))
                    .findFirst().orElseThrow();
            boolean overlapRejected = false;
            try {
                AppServices.deliveries().assignDriver(unassigned, alex,
                        existing.scheduledStart().plusMinutes(5), existing.estimatedMinutes(), "smoke-check");
            } catch (ValidationException expected) {
                overlapRejected = expected.getMessage().toLowerCase().contains("overlap");
            }
            if (!overlapRejected) {
                throw new IllegalStateException("Overlap validation smoke check failed");
            }

            var driver = AppServices.auth().authenticate("priya", "Driver123!").orElseThrow();
            SessionContext.login(driver);
            load("DriverDashboardView.fxml");

            SessionContext.login(admin);
            SceneManager.getInstance().initialize(stage);
            SceneManager.getInstance().showAdminDashboard();
            capture(stage.getScene().getRoot(), "target/smoke-dashboard.png");
            SceneManager.getInstance().showLiveMap();
            capture(stage.getScene().getRoot(), "target/smoke-map.png");
            SceneManager.getInstance().showAssignDelivery(unassigned);
            capture(stage.getScene().getRoot(), "target/smoke-assignment.png");
            SessionContext.login(driver);
            SceneManager.getInstance().showDriverDashboard();
            capture(stage.getScene().getRoot(), "target/smoke-driver.png");
            SessionContext.login(admin);
            SceneManager.getInstance().showLogin();
            capture(stage.getScene().getRoot(), System.getProperty("smoke.screenshot", "target/smoke-login.png"));

            System.out.println("SMOKE CHECK PASSED: database rules, authentication, and 7 FXML screens loaded.");
        } finally {
            AppServices.shutdown();
            stage.close();
        }
    }

    private Parent load(String view) throws Exception {
        FXMLLoader loader = new FXMLLoader(DeliveryApplication.class.getResource("view/" + view));
        return loader.load();
    }

    private void capture(Parent root, String output) throws Exception {
        root.applyCss();
        root.layout();
        Theme.styleTableSkins(root);
        WritableImage image = new WritableImage(1440, 900);
        root.snapshot(null, image);
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", Path.of(output).toFile());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
