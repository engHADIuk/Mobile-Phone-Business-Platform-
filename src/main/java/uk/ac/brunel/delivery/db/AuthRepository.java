package uk.ac.brunel.delivery.db;

import uk.ac.brunel.delivery.model.SessionUser;
import uk.ac.brunel.delivery.model.UserRole;
import uk.ac.brunel.delivery.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public final class AuthRepository {
    private final DatabaseManager database;

    public AuthRepository(DatabaseManager database) {
        this.database = database;
    }

    public Optional<SessionUser> authenticate(String username, String password) {
        String sql = """
                SELECT user_id, username, password_hash, display_name, role, driver_id
                FROM users
                WHERE lower(username) = lower(?) AND active = 1
                """;
        try (var connection = database.getConnection();
             var prepared = connection.prepareStatement(sql)) {
            prepared.setString(1, username == null ? "" : username.trim());
            try (var result = prepared.executeQuery()) {
                if (!result.next() || !PasswordUtil.matches(password, result.getString("password_hash"))) {
                    return Optional.empty();
                }
                return Optional.of(new SessionUser(
                        result.getLong("user_id"),
                        result.getString("username"),
                        result.getString("display_name"),
                        UserRole.valueOf(result.getString("role")),
                        result.getString("driver_id")
                ));
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to authenticate user", exception);
        }
    }
}
