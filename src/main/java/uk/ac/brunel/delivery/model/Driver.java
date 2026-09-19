package uk.ac.brunel.delivery.model;

public record Driver(
        String driverId,
        String fullName,
        String username,
        boolean available,
        double latitude,
        double longitude
) {
    @Override
    public String toString() {
        return fullName + " (" + driverId + ")";
    }
}
