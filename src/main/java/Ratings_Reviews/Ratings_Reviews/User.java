package Ratings_Reviews.Ratings_Reviews;

public class User {
    private final int id;
    private final String username;
    private final String displayName;
    private final String role;

    public User(int id, String username, String displayName, String role) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
}
