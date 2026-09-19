package Ratings_Reviews.Ratings_Reviews;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Review {
    private final int id;
    private final int productId;
    private final int customerId;
    private final String reviewCode;
    private final String customerName;
    private final int rating;
    private final String title;
    private final String comment;
    private final LocalDateTime postedAt;
    private final LocalDateTime editableUntil;
    private final String status;
    private final int helpfulVotes;
    private final int unhelpfulVotes;
    private final int reportCount;
    private final String sentiment;

    public Review(int id, int productId, int customerId, String reviewCode, String customerName, int rating,
                  String title, String comment, LocalDateTime postedAt, LocalDateTime editableUntil,
                  String status, int helpfulVotes, int unhelpfulVotes, int reportCount, String sentiment) {
        this.id = id; this.productId = productId; this.customerId = customerId; this.reviewCode = reviewCode;
        this.customerName = customerName; this.rating = rating; this.title = title; this.comment = comment;
        this.postedAt = postedAt; this.editableUntil = editableUntil; this.status = status;
        this.helpfulVotes = helpfulVotes; this.unhelpfulVotes = unhelpfulVotes; this.reportCount = reportCount;
        this.sentiment = sentiment;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public int getCustomerId() { return customerId; }
    public String getReviewCode() { return reviewCode; }
    public String getCustomerName() { return customerName; }
    public int getRating() { return rating; }
    public String getTitle() { return title; }
    public String getComment() { return comment; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public LocalDateTime getEditableUntil() { return editableUntil; }
    public String getStatus() { return status; }
    public int getHelpfulVotes() { return helpfulVotes; }
    public int getUnhelpfulVotes() { return unhelpfulVotes; }
    public int getReportCount() { return reportCount; }
    public String getSentiment() { return sentiment; }

    public boolean canBeEditedBy(User user) {
        return user != null && user.getId() == customerId && LocalDateTime.now().isBefore(editableUntil)
                && "ACTIVE".equals(status);
    }

    @Override
    public String toString() {
        String stars = "★★★★★".substring(0, rating) + "☆☆☆☆☆".substring(0, 5 - rating);
        return stars + "  " + title + "\n" + comment + "\n" + customerName + " • "
                + postedAt.format(DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm"))
                + " • Helpful: " + helpfulVotes + " • " + sentiment;
    }
}
