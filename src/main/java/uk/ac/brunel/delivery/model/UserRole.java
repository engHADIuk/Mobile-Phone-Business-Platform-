package uk.ac.brunel.delivery.model;

public enum UserRole {
    ADMIN,
    LOGISTICS_MANAGER,
    DRIVER;

    public boolean isAdministrator() {
        return this == ADMIN || this == LOGISTICS_MANAGER;
    }
}
