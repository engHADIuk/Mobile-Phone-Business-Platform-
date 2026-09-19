package Ratings_Reviews.Ratings_Reviews;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.time.format.DateTimeFormatter;

public final class UiUtil {
    private UiUtil() { }

    public static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("VALMORA Reviews");
        alert.setHeaderText("Please check your input");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void information(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("VALMORA Reviews");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void configureReviewList(ListView<Review> list) {
        list.setCellFactory(view -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setGraphic(null);
                } else {
                    Label author = new Label(review.getCustomerName());
                    author.getStyleClass().add("review-author");
                    Label status = new Label("VERIFIED • " + review.getStatus());
                    status.getStyleClass().add("status-pill");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    HBox heading = new HBox(10, author, status, spacer);
                    Label stars = new Label(stars(review.getRating()) + "   " + review.getPostedAt()
                            .format(DateTimeFormatter.ofPattern("dd MMM uuuu")));
                    stars.getStyleClass().add("review-stars");
                    Label metadata = new Label("Review ID: " + review.getReviewCode() + "   •   Customer ID: "
                            + review.getCustomerId() + "   •   " + review.getSentiment());
                    metadata.getStyleClass().add("review-meta");
                    Label title = new Label(review.getTitle());
                    title.getStyleClass().add("review-title");
                    Label comment = new Label(review.getComment());
                    comment.setWrapText(true);
                    comment.getStyleClass().add("review-comment");
                    Label footer = new Label("Helpful (" + review.getHelpfulVotes() + ")    Not helpful ("
                            + review.getUnhelpfulVotes() + ")    Reports (" + review.getReportCount() + ")");
                    footer.getStyleClass().add("review-footer");

                    VBox card = new VBox(8, heading, stars, metadata, title, comment, footer);
                    card.getStyleClass().add("review-card");
                    card.prefWidthProperty().bind(view.widthProperty().subtract(30));
                    setText(null);
                    setGraphic(card);
                }
            }
        });
    }

    private static String stars(int rating) {
        return "★★★★★".substring(0, rating) + "☆☆☆☆☆".substring(0, 5 - rating);
    }
}
