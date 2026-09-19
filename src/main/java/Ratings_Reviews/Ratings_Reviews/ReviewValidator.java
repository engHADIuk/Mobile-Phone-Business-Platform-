package Ratings_Reviews.Ratings_Reviews;

import java.util.Locale;

/** Input validation shared by review creation and editing. */
public final class ReviewValidator {
    private static final String FORBIDDEN = "@,.#/\\?\"'~`$";

    private ReviewValidator() { }

    public static void validate(int rating, String title, String comment) throws ValidationException {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Choose a rating between 1 and 5 stars.");
        }
        validateText("Review title", title, 100);
        validateText("Review comment", comment, 1000);
        String lowered = comment.toLowerCase(Locale.ROOT);
        if (lowered.contains("http") || lowered.contains("www.")) {
            throw new ValidationException("Links are not allowed in review comments.");
        }
    }

    private static void validateText(String field, String value, int maxLength) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
        if (value.trim().length() > maxLength) {
            throw new ValidationException(field + " must be no longer than " + maxLength + " characters.");
        }
        for (char character : FORBIDDEN.toCharArray()) {
            if (value.indexOf(character) >= 0) {
                throw new ValidationException(field + " contains a prohibited character: " + character);
            }
        }
    }
}
