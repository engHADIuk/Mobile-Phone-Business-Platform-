package uk.ac.brunel.delivery.db;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class DbTime {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DbTime() {
    }

    static String format(LocalDateTime value) {
        return value == null ? null : value.withNano(0).format(FORMATTER);
    }

    static LocalDateTime parse(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value, FORMATTER);
    }
}
