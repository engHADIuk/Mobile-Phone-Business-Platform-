package velmora;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import velmora.model.Session;

public class SceneManager {

    private static Stage stage;
    private static BorderPane rootLayout;
    private static HBox globalTopBar;
    private static Label userLabel;
    private static Button logoutButton;
    private static final String STYLESHEET = "/css/style.css";

    public static void init(Stage primaryStage) {
        stage = primaryStage;

        Label brand = new Label("VELMORA");
        brand.getStyleClass().add("app-brand");

        userLabel = new Label();
        userLabel.getStyleClass().add("app-user");

        logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("btn-back");
        logoutButton.setOnAction(e -> {
            Session.logout();
            show("login");
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox userGroup = new HBox(12, userLabel, logoutButton);
        userGroup.setAlignment(Pos.CENTER_RIGHT);

        globalTopBar = new HBox(brand, spacer, userGroup);
        globalTopBar.getStyleClass().add("app-topbar");
        globalTopBar.setAlignment(Pos.CENTER_LEFT);
        globalTopBar.setPadding(new Insets(12, 24, 12, 24));

        rootLayout = new BorderPane();
        rootLayout.setTop(globalTopBar);

        Scene scene = new Scene(rootLayout, 1280, 800);
        scene.getStylesheets().add(SceneManager.class.getResource(STYLESHEET).toExternalForm());
        stage.setScene(scene);
    }

    public static void show(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/" + fxmlName + ".fxml"));
            Parent content = loader.load();

            rootLayout.setCenter(content);

            boolean showTopBar = !fxmlName.equals("login");
            globalTopBar.setVisible(showTopBar);
            globalTopBar.setManaged(showTopBar);
            if (showTopBar) {
                userLabel.setText(Session.isLoggedIn() ? Session.getName() : "");
            }

        } catch (Exception e) {
            System.err.println("=== FAILED TO LOAD SCREEN: " + fxmlName + " ===");
            e.printStackTrace();
        }
    }
}
