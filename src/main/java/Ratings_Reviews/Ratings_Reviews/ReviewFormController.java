package Ratings_Reviews.Ratings_Reviews;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ReviewFormController {
    @FXML private Label headingLabel;
    @FXML private Label productLabel;
    @FXML private Label messageLabel;
    @FXML private ComboBox<Integer> ratingBox;
    @FXML private TextField titleField;
    @FXML private TextArea commentField;

    @FXML
    private void initialize() {
        ratingBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        Review review = App.getSelectedReview();
        if (review == null) {
            headingLabel.setText("Write a review");
            Product product = App.getSelectedProduct();
            productLabel.setText(product == null ? "" : "For: " + product.getName());
        } else {
            headingLabel.setText(Session.isAdmin() ? "Administrator edit" : "Edit your review");
            productLabel.setText("Review ID: " + review.getReviewCode());
            ratingBox.setValue(review.getRating()); titleField.setText(review.getTitle()); commentField.setText(review.getComment());
        }
    }

    @FXML
    private void save() {
        Integer rating = ratingBox.getValue();
        if (rating == null) { messageLabel.setText("Choose a rating before saving."); return; }
        try {
            Review existing = App.getSelectedReview();
            if (existing == null) App.database().createReview(Session.getCurrentUser(), App.getSelectedProduct(), rating, titleField.getText(), commentField.getText());
            else if (Session.isAdmin()) App.database().updateAdminReview(Session.getCurrentUser(), existing, rating, titleField.getText(), commentField.getText());
            else App.database().updateCustomerReview(Session.getCurrentUser(), existing, rating, titleField.getText(), commentField.getText());
            UiUtil.information(existing == null ? "Review submitted successfully." : "Review updated successfully.");
            cancel();
        } catch (ValidationException exception) { messageLabel.setText(exception.getMessage()); }
        catch (IOException | IllegalStateException exception) { messageLabel.setText("Unable to save: " + exception.getMessage()); }
    }

    @FXML
    private void cancel() throws IOException {
        if (Session.isAdmin()) App.showAdminDashboard();
        else App.showProductReviews(App.getSelectedProduct());
    }
}
