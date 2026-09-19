package uk.ac.brunel.delivery.model;

public record SessionUser(
        long userId,
        String username,
        String displayName,
        UserRole role,
        String driverId
) {
    public boolean isDriver() {
        return role == UserRole.DRIVER;
    }
}
