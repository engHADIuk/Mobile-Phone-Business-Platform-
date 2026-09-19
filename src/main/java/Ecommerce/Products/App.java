package Ecommerce.Products;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App - Velmora Ecommerce (Products component)
 */
public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("login"), 900, 620);
        scene.getStylesheets().add(App.class.getResource("style.css").toExternalForm());
        stage.setTitle("VELMORA");
        stage.setScene(scene);
        stage.show();
    }

    /** Swaps the root of the current window (used after login, back button, etc). */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /** Loads an FXML and resizes the window to fit each screen's needs. */
    static void setRoot(String fxml, double width, double height) throws IOException {
        scene.setRoot(loadFXML(fxml));
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
    }

    static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        // Quick DB connectivity check on startup (same as your original project)
        try {
            Controller.connect().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch();
    }
}