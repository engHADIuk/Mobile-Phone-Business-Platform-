package Ratings_Reviews.Ratings_Reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Verifies the database is created, seeded, and enforces the key review input rules. */
class DatabaseSmokeTest {
    private static Database database;

    @BeforeAll
    static void setUpDatabase() {
        database = new Database();
        database.initialise();
    }

    @Test
    void seededCustomerCanAuthenticateAndSeeVerifiedPurchases() throws Exception {
        User user = database.authenticate("customer@example.com", "customer123");
        assertEquals("CUSTOMER", user.getRole());
        assertFalse(database.getProductsForCustomer(user.getId()).isEmpty());
    }

    @Test
    void invalidCharactersAndOutOfRangeRatingsAreRejected() {
        assertThrows(ValidationException.class, () -> ReviewValidator.validate(6, "Valid title", "Valid comment"));
        assertThrows(ValidationException.class, () -> ReviewValidator.validate(5, "Bad@title", "Valid comment"));
    }
}
