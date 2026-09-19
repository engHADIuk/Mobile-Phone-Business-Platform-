package Ratings_Reviews.Ratings_Reviews;

import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class CustomerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label productDescriptionLabel;
    @FXML private Label productSummaryLabel;
    @FXML private ComboBox<Product> productBox;

    @FXML
    private void initialize() {
        User user = Session.getCurrentUser();
        if (user == null) return;
        welcomeLabel.setText("Welcome, " + user.getDisplayName() + " — your verified purchases");
        List<Product> products = App.database().getProductsForCustomer(user.getId());
        productBox.setItems(FXCollections.observableArrayList(products));
        if (!products.isEmpty()) {
            productBox.getSelectionModel().selectFirst();
            showProductDetails();
        }
    }

    @FXML
    private void showProductDetails() {
        Product product = productBox.getValue();
        if (product != null) {
            productDescriptionLabel.setText(product.getDescription());
            productSummaryLabel.setText(App.database().getProductSummary(product.getId()));
        }
    }

    @FXML
    private void openProductReviews() {
        if (productBox.getValue() == null) {
            UiUtil.error("Choose a purchased product first.");
            return;
        }
        try { App.showProductReviews(productBox.getValue()); }
        catch (IOException exception) { UiUtil.error("Unable to open reviews: " + exception.getMessage()); }
    }

    @FXML
    private void logout() {
        try { App.showLogin(); }
        catch (IOException exception) { UiUtil.error("Unable to log out."); }
    }
}
