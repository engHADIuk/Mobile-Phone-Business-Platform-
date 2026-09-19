package Ratings_Reviews.Ratings_Reviews;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Small SQLite repository for the standalone assessment component. The database file is created
 * beside pom.xml on first run and contains safe demo accounts and purchase records.
 */
public class Database {
    private static final int AUTO_FLAG_THRESHOLD = 3;
    private final String url;

    public Database() {
        Path databasePath = Paths.get(System.getProperty("user.dir"), "ratings-reviews.db");
        url = "jdbc:sqlite:" + databasePath.toAbsolutePath();
    }

    public void initialise() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, "
                    + "display_name TEXT NOT NULL, role TEXT NOT NULL CHECK(role IN ('CUSTOMER','ADMIN'))) ");
            statement.execute("CREATE TABLE IF NOT EXISTS products ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, product_code TEXT UNIQUE NOT NULL, "
                    + "name TEXT NOT NULL, description TEXT NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS purchases ("
                    + "customer_id INTEGER NOT NULL, product_id INTEGER NOT NULL, purchased_at TEXT NOT NULL, "
                    + "PRIMARY KEY(customer_id, product_id), FOREIGN KEY(customer_id) REFERENCES users(id), "
                    + "FOREIGN KEY(product_id) REFERENCES products(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS reviews ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, review_code TEXT UNIQUE NOT NULL, product_id INTEGER NOT NULL, "
                    + "customer_id INTEGER NOT NULL, rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5), "
                    + "title TEXT NOT NULL, comment TEXT NOT NULL, posted_at TEXT NOT NULL, editable_until TEXT NOT NULL, "
                    + "last_edited TEXT, status TEXT NOT NULL DEFAULT 'ACTIVE', helpful_votes INTEGER NOT NULL DEFAULT 0, "
                    + "unhelpful_votes INTEGER NOT NULL DEFAULT 0, report_count INTEGER NOT NULL DEFAULT 0, sentiment TEXT NOT NULL, "
                    + "FOREIGN KEY(product_id) REFERENCES products(id), FOREIGN KEY(customer_id) REFERENCES users(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS review_votes ("
                    + "review_id INTEGER NOT NULL, customer_id INTEGER NOT NULL, vote_type TEXT NOT NULL, "
                    + "PRIMARY KEY(review_id, customer_id), FOREIGN KEY(review_id) REFERENCES reviews(id), "
                    + "FOREIGN KEY(customer_id) REFERENCES users(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS reports ("
                    + "review_id INTEGER NOT NULL, customer_id INTEGER NOT NULL, reason TEXT NOT NULL, reported_at TEXT NOT NULL, "
                    + "PRIMARY KEY(review_id, customer_id), FOREIGN KEY(review_id) REFERENCES reviews(id), "
                    + "FOREIGN KEY(customer_id) REFERENCES users(id))");
            statement.execute("CREATE TABLE IF NOT EXISTS audit_log ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, review_id INTEGER NOT NULL, admin_id INTEGER NOT NULL, "
                    + "action TEXT NOT NULL, reason TEXT, created_at TEXT NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS settings (setting_key TEXT PRIMARY KEY, setting_value TEXT NOT NULL)");
            seed(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialise the local SQLite database: " + exception.getMessage(), exception);
        }
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void seed(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT OR IGNORE INTO users(username,password,display_name,role) VALUES "
                    + "('customer@example.com','customer123','Jordan Customer','CUSTOMER'),"
                    + "('sophia@example.com','customer123','Sophia Customer','CUSTOMER'),"
                    + "('admin@valmora.com','admin123','Avery Admin','ADMIN')");
            statement.execute("INSERT OR IGNORE INTO products(product_code,name,description) VALUES "
                    + "('P100','VALMORA X1 Pro','Flagship smartphone with an immersive display and all-day battery'),"
                    + "('P200','VALMORA Edge','Premium camera phone with a refined edge-to-edge display'),"
                    + "('P300','VALMORA Lite','Compact everyday smartphone with essential premium features')");
            // The project ships with a pre-created demonstration database. These updates keep that
            // database visually consistent with the VALMORA product catalogue on first launch.
            statement.execute("UPDATE products SET name='VALMORA X1 Pro', description='Flagship smartphone with an immersive display and all-day battery' WHERE product_code='P100'");
            statement.execute("UPDATE products SET name='VALMORA Edge', description='Premium camera phone with a refined edge-to-edge display' WHERE product_code='P200'");
            statement.execute("UPDATE products SET name='VALMORA Lite', description='Compact everyday smartphone with essential premium features' WHERE product_code='P300'");
            statement.execute("INSERT OR IGNORE INTO purchases(customer_id,product_id,purchased_at) VALUES "
                    + "(1,1,'2026-01-11T10:00:00'),(1,2,'2026-01-12T10:00:00'),(2,1,'2026-01-10T10:00:00'),(2,3,'2026-01-10T10:00:00')");
            statement.execute("INSERT OR IGNORE INTO settings(setting_key,setting_value) VALUES ('edit_window_minutes','5')");
            statement.execute("INSERT OR IGNORE INTO reviews(review_code,product_id,customer_id,rating,title,comment,posted_at,editable_until,status,helpful_votes,unhelpful_votes,report_count,sentiment) VALUES "
                    + "('REV-DEMO-001',1,2,5,'Excellent sound','Clear audio and comfortable for long listening sessions','2026-06-10T10:15:00','2026-06-10T10:20:00','ACTIVE',8,0,0,'Positive'),"
                    + "('REV-DEMO-002',3,2,3,'Useful bottle','Keeps drinks cool but the lid could be improved','2026-06-12T09:30:00','2026-06-12T09:35:00','ACTIVE',2,1,0,'Neutral')");
        }
    }

    public User authenticate(String username, String password) throws ValidationException {
        String sql = "SELECT id, username, display_name, role FROM users WHERE lower(username)=lower(?) AND password=?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, password);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new User(result.getInt("id"), result.getString("username"),
                            result.getString("display_name"), result.getString("role"));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Login could not be checked: " + exception.getMessage(), exception);
        }
        throw new ValidationException("Incorrect email or password.");
    }

    public List<Product> getProductsForCustomer(int customerId) {
        String sql = "SELECT p.* FROM products p JOIN purchases pu ON pu.product_id=p.id "
                + "WHERE pu.customer_id=? ORDER BY p.name";
        return products(sql, customerId);
    }

    public List<Product> getAllProducts() {
        return products("SELECT * FROM products ORDER BY name", null);
    }

    private List<Product> products(String sql, Integer parameter) {
        List<Product> products = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) statement.setInt(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    products.add(new Product(result.getInt("id"), result.getString("product_code"),
                            result.getString("name"), result.getString("description")));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Products could not be loaded: " + exception.getMessage(), exception);
        }
        return products;
    }

    public String getProductSummary(int productId) {
        String sql = "SELECT COUNT(*) AS count, COALESCE(ROUND(AVG(rating),1),0) AS average FROM reviews "
                + "WHERE product_id=? AND status<>'REMOVED'";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return String.format("%.1f ★ based on %d review%s", result.getDouble("average"), result.getInt("count"),
                        result.getInt("count") == 1 ? "" : "s");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Rating summary could not be calculated.", exception);
        }
    }

    public List<Review> getReviews(int productId, String ratingFilter, String sort, String search, boolean includeRemoved) {
        StringBuilder sql = new StringBuilder("SELECT r.*, u.display_name FROM reviews r JOIN users u ON r.customer_id=u.id WHERE r.product_id=? ");
        if (!includeRemoved) sql.append("AND r.status<>'REMOVED' ");
        if (ratingFilter != null && !"All ratings".equals(ratingFilter)) sql.append("AND r.rating=? ");
        if (search != null && !search.trim().isEmpty()) sql.append("AND (lower(r.title) LIKE ? OR lower(r.comment) LIKE ?) ");
        if ("Highest rating".equals(sort)) sql.append("ORDER BY r.rating DESC, r.posted_at DESC");
        else if ("Most helpful".equals(sort)) sql.append("ORDER BY r.helpful_votes DESC, r.posted_at DESC");
        else sql.append("ORDER BY r.posted_at DESC");
        List<Review> reviews = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setInt(index++, productId);
            if (ratingFilter != null && !"All ratings".equals(ratingFilter)) statement.setInt(index++, Integer.parseInt(ratingFilter.substring(0, 1)));
            if (search != null && !search.trim().isEmpty()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                statement.setString(index++, term);
                statement.setString(index, term);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) reviews.add(reviewFrom(result));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Reviews could not be loaded: " + exception.getMessage(), exception);
        }
        return reviews;
    }

    public List<Review> getTopReviews(int productId) {
        List<Review> sortedReviews = getReviews(productId, "All ratings", "Most helpful", "", false);
        return sortedReviews.subList(0, Math.min(3, sortedReviews.size()));
    }

    public void createReview(User user, Product product, int rating, String title, String comment) throws ValidationException {
        ReviewValidator.validate(rating, title, comment);
        if (!hasPurchased(user.getId(), product.getId())) {
            throw new ValidationException("Only verified customers who purchased this product may review it.");
        }
        if (hasActiveReview(user.getId(), product.getId())) {
            throw new ValidationException("You already have an active review for this product. Edit it instead.");
        }
        LocalDateTime now = LocalDateTime.now();
        int windowMinutes = getEditWindowMinutes();
        String sql = "INSERT INTO reviews(review_code,product_id,customer_id,rating,title,comment,posted_at,editable_until,status,sentiment) "
                + "VALUES(?,?,?,?,?,?,?,?,'ACTIVE',?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "REV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            statement.setInt(2, product.getId()); statement.setInt(3, user.getId()); statement.setInt(4, rating);
            statement.setString(5, title.trim()); statement.setString(6, comment.trim());
            statement.setString(7, now.toString()); statement.setString(8, now.plusMinutes(windowMinutes).toString());
            statement.setString(9, sentimentFor(comment));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Review could not be saved: " + exception.getMessage(), exception);
        }
    }

    public void updateCustomerReview(User user, Review review, int rating, String title, String comment) throws ValidationException {
        ReviewValidator.validate(rating, title, comment);
        if (!review.canBeEditedBy(user)) throw new ValidationException("The five-minute edit/delete window has closed.");
        updateReview(review.getId(), rating, title, comment, user.getId(), "CUSTOMER_EDIT", false);
    }

    public void updateAdminReview(User admin, Review review, int rating, String title, String comment) throws ValidationException {
        ReviewValidator.validate(rating, title, comment);
        if (!"ADMIN".equals(admin.getRole())) throw new ValidationException("Administrator access is required.");
        updateReview(review.getId(), rating, title, comment, admin.getId(), "ADMIN_EDIT", true);
    }

    private void updateReview(int reviewId, int rating, String title, String comment, int actorId, String action, boolean audit) {
        String sql = "UPDATE reviews SET rating=?, title=?, comment=?, sentiment=?, last_edited=? WHERE id=?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, rating); statement.setString(2, title.trim()); statement.setString(3, comment.trim());
            statement.setString(4, sentimentFor(comment)); statement.setString(5, LocalDateTime.now().toString()); statement.setInt(6, reviewId);
            statement.executeUpdate();
            if (audit) writeAudit(connection, reviewId, actorId, action, "Content updated by administrator");
        } catch (SQLException exception) { throw new IllegalStateException("Review could not be updated.", exception); }
    }

    public void deleteCustomerReview(User user, Review review) throws ValidationException {
        if (!review.canBeEditedBy(user)) throw new ValidationException("The five-minute edit/delete window has closed.");
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("UPDATE reviews SET status='REMOVED' WHERE id=?")) {
            statement.setInt(1, review.getId()); statement.executeUpdate();
        } catch (SQLException exception) { throw new IllegalStateException("Review could not be deleted.", exception); }
    }

    public void vote(User user, Review review, boolean helpful) throws ValidationException {
        if (user.getId() == review.getCustomerId()) throw new ValidationException("You cannot vote on your own review.");
        try (Connection connection = connect()) {
            try (PreparedStatement check = connection.prepareStatement("SELECT 1 FROM review_votes WHERE review_id=? AND customer_id=?")) {
                check.setInt(1, review.getId()); check.setInt(2, user.getId());
                if (check.executeQuery().next()) throw new ValidationException("You have already voted on this review.");
            }
            try (PreparedStatement vote = connection.prepareStatement("INSERT INTO review_votes(review_id,customer_id,vote_type) VALUES(?,?,?)");
                 PreparedStatement update = connection.prepareStatement("UPDATE reviews SET " + (helpful ? "helpful_votes=helpful_votes+1" : "unhelpful_votes=unhelpful_votes+1") + " WHERE id=?")) {
                vote.setInt(1, review.getId()); vote.setInt(2, user.getId()); vote.setString(3, helpful ? "HELPFUL" : "UNHELPFUL"); vote.executeUpdate();
                update.setInt(1, review.getId()); update.executeUpdate();
            }
        } catch (SQLException exception) { throw new IllegalStateException("Vote could not be recorded.", exception); }
    }

    public void report(User user, Review review, String reason) throws ValidationException {
        if (user.getId() == review.getCustomerId()) throw new ValidationException("You cannot report your own review.");
        try (Connection connection = connect()) {
            try (PreparedStatement check = connection.prepareStatement("SELECT 1 FROM reports WHERE review_id=? AND customer_id=?")) {
                check.setInt(1, review.getId()); check.setInt(2, user.getId());
                if (check.executeQuery().next()) throw new ValidationException("You have already reported this review.");
            }
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO reports(review_id,customer_id,reason,reported_at) VALUES(?,?,?,?)")) {
                insert.setInt(1, review.getId()); insert.setInt(2, user.getId()); insert.setString(3, reason); insert.setString(4, LocalDateTime.now().toString()); insert.executeUpdate();
            }
            try (PreparedStatement update = connection.prepareStatement("UPDATE reviews SET report_count=report_count+1, status=CASE WHEN report_count+1>=? THEN 'FLAGGED' ELSE status END WHERE id=?")) {
                update.setInt(1, AUTO_FLAG_THRESHOLD); update.setInt(2, review.getId()); update.executeUpdate();
            }
        } catch (SQLException exception) { throw new IllegalStateException("Report could not be recorded.", exception); }
    }

    public void moderate(User admin, Review review, String action, String reason) throws ValidationException {
        if (!"ADMIN".equals(admin.getRole())) throw new ValidationException("Administrator access is required.");
        String status = "FLAG".equals(action) ? "FLAGGED" : ("REMOVE".equals(action) ? "REMOVED" : "ACTIVE");
        try (Connection connection = connect(); PreparedStatement update = connection.prepareStatement("UPDATE reviews SET status=? WHERE id=?")) {
            update.setString(1, status); update.setInt(2, review.getId()); update.executeUpdate();
            writeAudit(connection, review.getId(), admin.getId(), action, reason);
        } catch (SQLException exception) { throw new IllegalStateException("Moderation action could not be completed.", exception); }
    }

    public int getEditWindowMinutes() {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("SELECT setting_value FROM settings WHERE setting_key='edit_window_minutes'")) {
            try (ResultSet result = statement.executeQuery()) { return result.next() ? Integer.parseInt(result.getString(1)) : 5; }
        } catch (SQLException exception) { throw new IllegalStateException("Settings could not be loaded.", exception); }
    }

    public void setEditWindowMinutes(User admin, int minutes) throws ValidationException {
        if (!"ADMIN".equals(admin.getRole())) throw new ValidationException("Administrator access is required.");
        if (minutes < 1 || minutes > 120) throw new ValidationException("The edit window must be between 1 and 120 minutes.");
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("UPDATE settings SET setting_value=? WHERE setting_key='edit_window_minutes'")) {
            statement.setString(1, String.valueOf(minutes)); statement.executeUpdate();
        } catch (SQLException exception) { throw new IllegalStateException("Settings could not be saved.", exception); }
    }

    public String getAdminStatistics() {
        String sql = "SELECT COUNT(*) AS count, COALESCE(ROUND(AVG(rating),1),0) AS average, "
                + "SUM(CASE WHEN status='FLAGGED' THEN 1 ELSE 0 END) AS flagged FROM reviews WHERE status<>'REMOVED'";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            result.next();
            return "Total active reviews: " + result.getInt("count") + "   |   Average rating: " + result.getDouble("average")
                    + " ★   |   Flagged: " + result.getInt("flagged") + "   |   Edit window: " + getEditWindowMinutes() + " minutes";
        } catch (SQLException exception) { throw new IllegalStateException("Dashboard statistics could not be loaded.", exception); }
    }

    private boolean hasPurchased(int userId, int productId) {
        return exists("SELECT 1 FROM purchases WHERE customer_id=? AND product_id=?", userId, productId);
    }

    private boolean hasActiveReview(int userId, int productId) {
        return exists("SELECT 1 FROM reviews WHERE customer_id=? AND product_id=? AND status<>'REMOVED'", userId, productId);
    }

    private boolean exists(String sql, int first, int second) {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, first); statement.setInt(2, second);
            return statement.executeQuery().next();
        } catch (SQLException exception) { throw new IllegalStateException("Database check failed.", exception); }
    }

    private void writeAudit(Connection connection, int reviewId, int adminId, String action, String reason) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO audit_log(review_id,admin_id,action,reason,created_at) VALUES(?,?,?,?,?)")) {
            statement.setInt(1, reviewId); statement.setInt(2, adminId); statement.setString(3, action);
            statement.setString(4, reason); statement.setString(5, LocalDateTime.now().toString()); statement.executeUpdate();
        }
    }

    private String sentimentFor(String comment) {
        String text = comment.toLowerCase();
        int positive = words(text, "excellent", "great", "clear", "comfortable", "love", "good", "amazing", "perfect");
        int negative = words(text, "bad", "poor", "broken", "hate", "slow", "worse", "problem", "disappointed");
        return positive > negative ? "Positive" : negative > positive ? "Negative" : "Neutral";
    }

    private int words(String text, String... values) {
        int count = 0;
        for (String value : values) if (text.contains(value)) count++;
        return count;
    }

    private Review reviewFrom(ResultSet result) throws SQLException {
        return new Review(result.getInt("id"), result.getInt("product_id"), result.getInt("customer_id"),
                result.getString("review_code"), result.getString("display_name"), result.getInt("rating"),
                result.getString("title"), result.getString("comment"), LocalDateTime.parse(result.getString("posted_at")),
                LocalDateTime.parse(result.getString("editable_until")), result.getString("status"), result.getInt("helpful_votes"),
                result.getInt("unhelpful_votes"), result.getInt("report_count"), result.getString("sentiment"));
    }
}
