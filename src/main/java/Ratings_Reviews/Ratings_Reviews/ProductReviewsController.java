package Ratings_Reviews.Ratings_Reviews;

import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ProductReviewsController {
    @FXML private Label productNameLabel;
    @FXML private Label productDescriptionLabel;
    @FXML private Label averageLabel;
    @FXML private Label topReviewsLabel;
    @FXML private Label actionLabel;
    @FXML private ComboBox<String> ratingFilterBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private TextField searchField;
    @FXML private ListView<Review> reviewsList;

    @FXML
    private void initialize() {
        Product product = App.getSelectedProduct();
        if (product == null) return;
        productNameLabel.setText(product.getName() + " (" + product.getProductCode() + ")");
        productDescriptionLabel.setText(product.getDescription());
        ratingFilterBox.setItems(FXCollections.observableArrayList("All ratings", "5 stars", "4 stars", "3 stars", "2 stars", "1 star"));
        sortBox.setItems(FXCollections.observableArrayList("Most recent", "Highest rating", "Most helpful"));
        ratingFilterBox.getSelectionModel().selectFirst();
        sortBox.getSelectionModel().selectFirst();
        UiUtil.configureReviewList(reviewsList);
        refreshReviews();
    }

    @FXML
    private void refreshReviews() {
        Product product = App.getSelectedProduct();
        if (product == null) return;
        averageLabel.setText(App.database().getProductSummary(product.getId()));
        List<Review> reviews = App.database().getReviews(product.getId(), ratingFilterBox.getValue(), sortBox.getValue(), searchField.getText(), false);
        reviewsList.setItems(FXCollections.observableArrayList(reviews));
        List<Review> top = App.database().getTopReviews(product.getId());
        topReviewsLabel.setText(top.isEmpty() ? "No top reviews yet." : "Top reviews: " + top.get(0).getTitle()
                + (top.size() > 1 ? "  •  " + top.get(1).getTitle() : "") + (top.size() > 2 ? "  •  " + top.get(2).getTitle() : ""));
        actionLabel.setText(reviews.isEmpty() ? "No reviews match your selection." : "Select a review to vote, report, edit, or delete.");
    }

    @FXML
    private void writeReview() {
        try { App.showReviewForm(null); }
        catch (IOException exception) { UiUtil.error("Unable to open the review form."); }
    }

    @FXML
    private void editSelected() {
        Review review = selectedReview();
        if (review == null) return;
        if (!review.canBeEditedBy(Session.getCurrentUser())) {
            UiUtil.error("Only your active review can be edited within its time window.");
            return;
        }
        try { App.showReviewForm(review); }
        catch (IOException exception) { UiUtil.error("Unable to open the editor."); }
    }

    @FXML
    private void deleteSelected() {
        Review review = selectedReview();
        if (review == null) return;
        try {
            App.database().deleteCustomerReview(Session.getCurrentUser(), review);
            UiUtil.information("Your review was deleted and the product average was recalculated.");
            refreshReviews();
        } catch (ValidationException exception) { UiUtil.error(exception.getMessage()); }
    }

    @FXML
    private void markHelpful() { vote(true); }

    @FXML
    private void markUnhelpful() { vote(false); }

    private void vote(boolean helpful) {
        Review review = selectedReview();
        if (review == null) return;
        try {
            App.database().vote(Session.getCurrentUser(), review, helpful);
            actionLabel.setText("Thank you — your vote was recorded.");
            refreshReviews();
        } catch (ValidationException exception) { UiUtil.error(exception.getMessage()); }
    }

    @FXML
    private void reportSelected() {
        Review review = selectedReview();
        if (review == null) return;
        try {
            App.database().report(Session.getCurrentUser(), review, "Customer report");
            UiUtil.information("The review has been reported. It will be automatically flagged after three reports.");
            refreshReviews();
        } catch (ValidationException exception) { UiUtil.error(exception.getMessage()); }
    }

    private Review selectedReview() {
        Review review = reviewsList.getSelectionModel().getSelectedItem();
        if (review == null) UiUtil.error("Select a review first.");
        return review;
    }

    @FXML
    private void back() {
        try { App.showCustomerDashboard(); }
        catch (IOException exception) { UiUtil.error("Unable to return to your dashboard."); }
    }

    @FXML
    private void logout() {
        try { App.showLogin(); }
        catch (IOException exception) { UiUtil.error("Unable to log out."); }
    }
}
