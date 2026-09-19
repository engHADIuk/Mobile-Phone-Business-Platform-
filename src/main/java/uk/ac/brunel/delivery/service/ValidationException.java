package uk.ac.brunel.delivery.service;

public final class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
