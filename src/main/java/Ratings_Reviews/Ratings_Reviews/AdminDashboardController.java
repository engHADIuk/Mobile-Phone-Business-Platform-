package Ratings_Reviews.Ratings_Reviews;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label statisticsLabel;
    @FXML private Label productSummaryLabel;
    @FXML private Label actionLabel;
    @FXML private ComboBox<Product> productBox;
    @FXML private ListView<Review> reviewsList;
    @FXML private TextField reasonField;
    @FXML private TextField editWindowField;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Administrator dashboard — " + Session.getCurrentUser().getDisplayName());
        productBox.setItems(FXCollections.observableArrayList(App.database().getAllProducts()));
        productBox.getSelectionModel().selectFirst();
        editWindowField.setText(String.valueOf(App.database().getEditWindowMinutes()));
        UiUtil.configureReviewList(reviewsList);
        refresh();
    }

    @FXML
    private void refresh() {
        statisticsLabel.setText(App.database().getAdminStatistics());
        Product product = productBox.getValue();
        if (product == null) return;
        productSummaryLabel.setText(product + " — " + App.database().getProductSummary(product.getId()));
        reviewsList.setItems(FXCollections.observableArrayList(App.database().getReviews(product.getId(), "All ratings", "Most recent", "", true)));
        actionLabel.setText("Select a review and choose a moderation action. All administrator actions are logged.");
    }

    @FXML
    private void editSelected() {
        Review review = selectedReview();
        if (review == null) return;
        try { App.showReviewForm(review); }
        catch (IOException exception) { UiUtil.error("Unable to open the review editor."); }
    }

    @FXML
    private void flagSelected() { moderate("FLAG", "Flagged by administrator"); }

    @FXML
    private void removeSelected() { moderate("REMOVE", "Removed for guideline violation"); }

    @FXML
    private void dismissFlag() { moderate("DISMISS", "Flag dismissed by administrator"); }

    private void moderate(String action, String fallbackReason) {
        Review review = selectedReview();
        if (review == null) return;
        try {
            String reason = reasonField.getText().trim().isEmpty() ? fallbackReason : reasonField.getText().trim();
            App.database().moderate(Session.getCurrentUser(), review, action, reason);
            actionLabel.setText("Action saved to audit log: " + action + ".");
            refresh();
        } catch (ValidationException exception) { UiUtil.error(exception.getMessage()); }
    }

    @FXML
    private void saveEditWindow() {
        try {
            App.database().setEditWindowMinutes(Session.getCurrentUser(), Integer.parseInt(editWindowField.getText().trim()));
            actionLabel.setText("New edit/delete window saved. It will apply to future reviews.");
            refresh();
        } catch (NumberFormatException exception) { UiUtil.error("Enter a whole number of minutes."); }
        catch (ValidationException exception) { UiUtil.error(exception.getMessage()); }
    }

    private Review selectedReview() {
        Review review = reviewsList.getSelectionModel().getSelectedItem();
        if (review == null) UiUtil.error("Select a review first.");
        return review;
    }

    @FXML
    private void logout() {
        try { App.showLogin(); }
        catch (IOException exception) { UiUtil.error("Unable to log out."); }
    }
}
