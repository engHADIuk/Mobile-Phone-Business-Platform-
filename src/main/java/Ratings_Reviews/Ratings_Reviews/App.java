package Ratings_Reviews.Ratings_Reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Stage stage;
    private static final Database DATABASE = new Database();
    private static Product selectedProduct;
    private static Review selectedReview;

    @Override
    public void start(Stage stage) throws IOException {
        DATABASE.initialise();
        App.stage = stage;
        scene = new Scene(loadFXML("login"), 1000, 700);
        scene.getStylesheets().add(App.class.getResource("app.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("VALMORA | Reviews & Ratings");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void showLogin() throws IOException {
        Session.logout();
        selectedProduct = null;
        selectedReview = null;
        setRoot("login");
    }

    public static void showCustomerDashboard() throws IOException {
        setRoot("customer-dashboard");
    }

    public static void showProductReviews(Product product) throws IOException {
        selectedProduct = product;
        selectedReview = null;
        setRoot("product-reviews");
    }

    public static void showReviewForm(Review review) throws IOException {
        selectedReview = review;
        setRoot("review-form");
    }

    public static void showAdminDashboard() throws IOException {
        selectedReview = null;
        setRoot("admin-dashboard");
    }

    public static Database database() {
        return DATABASE;
    }

    public static Product getSelectedProduct() {
        return selectedProduct;
    }

    public static Review getSelectedReview() {
        return selectedReview;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
