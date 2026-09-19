package uk.ac.brunel.delivery.model;

public record DashboardStats(long active, long pending, long deliveredToday, long failed) {
}
