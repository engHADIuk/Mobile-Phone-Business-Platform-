package com.ecommerce.finance;

import com.ecommerce.finance.launchpad.LaunchpadView;
import com.ecommerce.finance.services.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainApp extends Application {

    private static final String LAUNCHPAD_TITLE = "Velmora Launchpad";
    private static final double LAUNCHPAD_WIDTH = 1240;
    private static final double LAUNCHPAD_HEIGHT = 820;

    /**
     * Marks a root node as one this class inserted, so a scene is never
     * wrapped in a second back bar.
     */
    private static final String BAR_HOST_ID = "velmora-launchpad-bar-host";
    private static final double BAR_HEIGHT = 46;

    /** Held so a guest window's back button knows which window to raise. */
    private Stage primaryStage;

    /**
     * Built once and kept, so returning from a component to the launchpad is
     * instant and does not rebuild the whole screen.
     */
    private Scene launchpadScene;

    /**
     * One window per guest component, created on first click and reused after
     * that. Each component gets its own Stage so it can drive its own scenes,
     * titles and sizes exactly as it does when run on its own -- none of their
     * source files had to be touched.
     */
    private final Map<String, Stage> componentWindows = new LinkedHashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        try {
            DatabaseManager.getInstance().initializeDatabase();
            DatabaseManager.getInstance().seedIfEmpty();
        } catch (Exception dbEx) {
            // Non-fatal: every screen already falls back to its own sample data if the DB is unavailable
            dbEx.printStackTrace();
        }

        Map<String, Runnable> tileActions = new LinkedHashMap<>();
        tileActions.put("finance", () -> enterFinanceReporting(primaryStage));
        tileActions.put("delivery", () -> enterDeliveryLogistics(primaryStage));
        tileActions.put("orders", () -> openComponentWindow(
                "orders", "VELMORA - Orders", velmora.Main::new));
        tileActions.put("customers", () -> openComponentWindow(
                "customers", "VELMORA - Customers", org.gwanhee.groupproject.LogInApplication::new));
        tileActions.put("warehouse", () -> openComponentWindow(
                "warehouse", "VELMORA - Warehouse", com.nc1605.inventorywarehouse.App::new));
        tileActions.put("reviews", () -> openComponentWindow(
                "reviews", "VELMORA - Reviews & Ratings", Ratings_Reviews.Ratings_Reviews.App::new));
        tileActions.put("products", () -> openComponentWindow(
                "products", "VELMORA - Products", Ecommerce.Products.App::new));

        LaunchpadView launchpad = new LaunchpadView(tileActions);
        launchpadScene = new Scene(launchpad.build(), LAUNCHPAD_WIDTH, LAUNCHPAD_HEIGHT);
        primaryStage.setTitle(LAUNCHPAD_TITLE);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(launchpadScene);
        primaryStage.show();
    }

    /**
     * Exactly the app's original startup behaviour (load Login.fxml, apply
     * styles.css, title "Login") -- now triggered by the Finance & Reporting
     * tile instead of running automatically. sizeToScene()+centerOnScreen()
     * are the only additions, so the login window shrinks back down to its
     * own compact size instead of staying stretched to the launchpad's
     * larger dimensions.
     */
    private void enterFinanceReporting(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            // Wrapped before the scene is handed over, so the sizeToScene()
            // below already accounts for the bar's height.
            applyBackBar(scene, () -> returnToLaunchpad(primaryStage), null);
            primaryStage.setTitle("Login");
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The Delivery & Logistics component's own startup sequence, lifted
     * straight out of its DeliveryApplication.start(...) and pointed at this
     * stage. The component's launchpad screen is skipped, because we are
     * already on a launchpad -- the tile goes directly to its login screen.
     */
    private void enterDeliveryLogistics(Stage primaryStage) {
        try {
            AppServices.initialize();
            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.initialize(primaryStage);
            sceneManager.setLaunchpadReturn(() -> returnToLaunchpad(primaryStage));
            sceneManager.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Puts the launchpad back on screen at its own title and size. */
    private void returnToLaunchpad(Stage primaryStage) {
        if (launchpadScene == null) {
            return;
        }
        primaryStage.setScene(launchpadScene);
        primaryStage.setTitle(LAUNCHPAD_TITLE);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(640);
        primaryStage.setWidth(LAUNCHPAD_WIDTH);
        primaryStage.setHeight(LAUNCHPAD_HEIGHT);
        primaryStage.centerOnScreen();
    }

    /**
     * Opens a guest component in its own window by handing a fresh Stage to
     * that component's own Application class and calling its start(...)
     * method -- the same method JavaFX would call if the component were run
     * standalone. Nothing inside those components is modified or wrapped:
     * they set their own title, scenes and window size, and they show the
     * window themselves.
     *
     * <p>The window is created once and reused, so clicking the same tile
     * again brings the existing window back to the front instead of starting
     * the component a second time. Closing a component window leaves the
     * launchpad untouched.</p>
     */
    private void openComponentWindow(String id, String fallbackTitle, ComponentFactory factory) {
        Stage existing = componentWindows.get(id);
        if (existing != null) {
            existing.show();
            existing.toFront();
            existing.requestFocus();
            return;
        }

        Stage stage = new Stage();
        stage.setTitle(fallbackTitle);
        try {
            // Installed first: the component sets its own scene inside start(),
            // and the listener below wraps whatever it sets.
            installBackBar(stage);
            factory.create().start(stage);
            componentWindows.put(id, stage);
            if (!stage.isShowing()) {
                stage.show();
            }
            stage.toFront();
        } catch (Exception e) {
            // A failure inside one component must never take the launchpad down.
            e.printStackTrace();
            stage.close();
        }
    }

    /**
     * Watches a guest component's window and keeps a "back to launchpad" bar
     * above whatever that component chooses to display.
     *
     * <p>The five guest components are not modified to make this work. Each one
     * builds and sets its own scene inside {@code start(...)}, and several swap
     * the scene or its root again later (on login, for instance). So rather
     * than editing their code, this listens for those swaps and re-wraps the
     * new content. The component itself never sees the wrapper: its own root is
     * simply the centre of a BorderPane, so its layout, sizing and stylesheet
     * all behave exactly as they do when it runs standalone.</p>
     */
    private void installBackBar(Stage stage) {
        Runnable onBack = () -> {
            stage.hide();
            if (primaryStage != null) {
                primaryStage.show();
                primaryStage.toFront();
                primaryStage.requestFocus();
            }
        };
        if (stage.getScene() != null) {
            watchScene(stage.getScene(), onBack, stage);
        }
        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                watchScene(newScene, onBack, stage);
            }
        });
    }

    /** Wraps a scene now, and again if the component later replaces its root. */
    private void watchScene(Scene scene, Runnable onBack, Stage stage) {
        if (!Boolean.TRUE.equals(scene.getProperties().get(BAR_HOST_ID))) {
            scene.getProperties().put(BAR_HOST_ID, Boolean.TRUE);
            scene.rootProperty().addListener((obs, oldRoot, newRoot) -> {
                if (newRoot != null && !BAR_HOST_ID.equals(newRoot.getId())) {
                    // Deferred: mutating the root from inside its own change
                    // notification is asking for trouble.
                    Platform.runLater(() -> applyBackBar(scene, onBack, null));
                }
            });
        }
        applyBackBar(scene, onBack, stage);
    }

    /**
     * Puts the component's root into the centre of a BorderPane whose top is
     * the back bar. Does nothing if this scene is already wrapped.
     *
     * @param growStage if given, the window grows by the bar's height once, so
     *                  the bar does not eat into the component's own content.
     *                  Pass null when the window is already the right size.
     */
    private void applyBackBar(Scene scene, Runnable onBack, Stage growStage) {
        Parent root = scene.getRoot();
        if (root == null || BAR_HOST_ID.equals(root.getId())) {
            return;
        }
        BorderPane host = new BorderPane();
        host.setId(BAR_HOST_ID);
        host.setTop(buildBackBar(onBack));
        host.setCenter(root);
        scene.setRoot(host);

        if (growStage != null) {
            // After show(), so the height being read is the real one.
            Platform.runLater(() -> {
                if (growStage.isShowing()) {
                    growStage.setHeight(growStage.getHeight() + BAR_HEIGHT);
                }
            });
        }
    }

    /**
     * The same control Delivery & Logistics uses on its login screen: same
     * label, same height, same Theme.styleSecondary() call. Inline styles win
     * over a stylesheet in JavaFX, so it looks identical in every component
     * regardless of what CSS that component loads.
     */
    private HBox buildBackBar(Runnable onBack) {
        Button back = new Button("\u2190  Launchpad");
        back.setPrefHeight(30);
        Theme.styleSecondary(back);
        back.setOnAction(event -> onBack.run());

        HBox bar = new HBox(back);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.setStyle("-fx-background-color: #0C0D0D; "
                + "-fx-border-color: transparent transparent #242626 transparent;");
        return bar;
    }

    /** Supplies a component's own Application instance without importing it at the top. */
    @FunctionalInterface
    private interface ComponentFactory {
        Application create();
    }

    @Override
    public void stop() {
        // Stops the delivery component's background simulation threads if it was opened.
        AppServices.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
