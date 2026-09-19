package uk.ac.brunel.delivery.db;

import uk.ac.brunel.delivery.model.DashboardStats;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.model.Driver;
import uk.ac.brunel.delivery.model.StatusLog;
import uk.ac.brunel.delivery.service.DeliveryValidator;
import uk.ac.brunel.delivery.service.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class DeliveryRepository {
    private static final String DELIVERY_SELECT = """
            SELECT d.delivery_id, d.order_reference, d.assigned_driver_id, dr.full_name AS driver_name,
                   d.delivery_address, d.warehouse_location, d.scheduled_start, d.scheduled_end,
                   d.estimated_minutes, d.status, d.progress, d.created_at, d.started_at, d.completed_at
            FROM deliveries d
            LEFT JOIN drivers dr ON dr.driver_id = d.assigned_driver_id
            """;

    private final DatabaseManager database;

    public DeliveryRepository(DatabaseManager database) {
        this.database = database;
    }

    public List<Delivery> findAll(String query, DeliveryStatus filter) {
        String search = query == null ? "" : query.trim().toLowerCase();
        String sql = DELIVERY_SELECT + """
                WHERE (? = '' OR lower(d.delivery_id) LIKE ? OR lower(d.order_reference) LIKE ?
                    OR lower(d.delivery_address) LIKE ? OR lower(COALESCE(dr.full_name, '')) LIKE ?)
                  AND (? IS NULL OR d.status = ?)
                ORDER BY CASE d.status
                    WHEN 'IN_TRANSIT' THEN 1 WHEN 'OUT_FOR_DELIVERY' THEN 2 WHEN 'PENDING' THEN 3
                    WHEN 'FAILED' THEN 4 ELSE 5 END,
                    COALESCE(d.scheduled_start, d.created_at), d.delivery_id
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement prepared = connection.prepareStatement(sql)) {
            String like = "%" + search + "%";
            prepared.setString(1, search);
            prepared.setString(2, like);
            prepared.setString(3, like);
            prepared.setString(4, like);
            prepared.setString(5, like);
            if (filter == null) {
                prepared.setNull(6, Types.VARCHAR);
                prepared.setNull(7, Types.VARCHAR);
            } else {
                prepared.setString(6, filter.name());
                prepared.setString(7, filter.name());
            }
            return readDeliveries(prepared.executeQuery());
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to load deliveries", exception);
        }
    }

    public List<Delivery> findForDriver(String driverId) {
        String sql = DELIVERY_SELECT + """
                WHERE d.assigned_driver_id = ?
                ORDER BY CASE d.status
                    WHEN 'IN_TRANSIT' THEN 1 WHEN 'OUT_FOR_DELIVERY' THEN 1 WHEN 'PENDING' THEN 2 ELSE 3 END,
                    COALESCE(d.scheduled_start, d.created_at)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setString(1, driverId);
            return readDeliveries(prepared.executeQuery());
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to load the driver's delivery queue", exception);
        }
    }

    public Optional<Delivery> findById(String deliveryId) {
        try (Connection connection = database.getConnection()) {
            return findById(connection, deliveryId);
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to load delivery " + deliveryId, exception);
        }
    }

    public DashboardStats getDashboardStats() {
        String sql = """
                SELECT
                    SUM(CASE WHEN status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY') THEN 1 ELSE 0 END) AS active_count,
                    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_count,
                    SUM(CASE WHEN status = 'DELIVERED' AND date(completed_at) = date('now', 'localtime') THEN 1 ELSE 0 END) AS delivered_today,
                    SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) AS failed_count
                FROM deliveries
                """;
        try (Connection connection = database.getConnection();
             var statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next()
                    ? new DashboardStats(result.getLong(1), result.getLong(2), result.getLong(3), result.getLong(4))
                    : new DashboardStats(0, 0, 0, 0);
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to calculate dashboard statistics", exception);
        }
    }

    public List<Driver> findDrivers() {
        String sql = "SELECT driver_id, full_name, username, available, latitude, longitude FROM drivers ORDER BY full_name";
        List<Driver> drivers = new ArrayList<>();
        try (Connection connection = database.getConnection();
             var statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                drivers.add(mapDriver(result));
            }
            return drivers;
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to load drivers", exception);
        }
    }

    public List<StatusLog> findStatusLogs(String deliveryId) {
        String sql = """
                SELECT log_id, delivery_id, from_status, to_status, changed_at, changed_by, note
                FROM delivery_status_log WHERE delivery_id = ? ORDER BY changed_at DESC, log_id DESC
                """;
        List<StatusLog> logs = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setString(1, deliveryId);
            try (ResultSet result = prepared.executeQuery()) {
                while (result.next()) {
                    String from = result.getString("from_status");
                    logs.add(new StatusLog(
                            result.getLong("log_id"),
                            result.getString("delivery_id"),
                            from == null ? null : DeliveryStatus.fromDatabase(from),
                            DeliveryStatus.fromDatabase(result.getString("to_status")),
                            DbTime.parse(result.getString("changed_at")),
                            result.getString("changed_by"),
                            result.getString("note")
                    ));
                }
            }
            return logs;
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to load the delivery audit log", exception);
        }
    }

    public Optional<Delivery> findActiveForDriver(String driverId) {
        return findSingleByDriverAndStatuses(driverId, "d.status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY')", LocalDateTime.now());
    }

    public Optional<Delivery> findNextEligibleForDriver(String driverId, LocalDateTime now) {
        return findSingleByDriverAndStatuses(driverId,
                "d.status = 'PENDING' AND d.scheduled_start IS NOT NULL AND d.scheduled_start <= ?", now);
    }

    private Optional<Delivery> findSingleByDriverAndStatuses(String driverId, String condition, LocalDateTime time) {
        String sql = DELIVERY_SELECT + " WHERE d.assigned_driver_id = ? AND " + condition
                + " ORDER BY d.scheduled_start LIMIT 1";
        try (Connection connection = database.getConnection();
             PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setString(1, driverId);
            if (condition.contains("<= ?")) {
                prepared.setString(2, DbTime.format(time));
            }
            try (ResultSet result = prepared.executeQuery()) {
                return result.next() ? Optional.of(mapDelivery(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to inspect active driver schedules", exception);
        }
    }

    public synchronized Delivery generateSampleOrder(String actor) {
        String[] addresses = {
                "41 Carnaby Street", "14 Kensington High Street", "82 Camden Road",
                "5 Queen's Gate", "27 Fleet Street", "66 Shoreditch High Street"
        };
        String[] warehouses = {"West London Hub", "Central London Hub", "Docklands Hub"};
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int sequence = nextDeliverySequence(connection);
                String deliveryId = "DEL-%04d".formatted(sequence);
                String orderRef = "ORD-%04d".formatted(sequence + 1000);
                String address = addresses[ThreadLocalRandom.current().nextInt(addresses.length)];
                String warehouse = warehouses[ThreadLocalRandom.current().nextInt(warehouses.length)];
                int eta = ThreadLocalRandom.current().nextInt(30, 76);
                String now = DbTime.format(LocalDateTime.now());
                try (PreparedStatement prepared = connection.prepareStatement("""
                        INSERT INTO deliveries(delivery_id, order_reference, delivery_address, warehouse_location,
                            estimated_minutes, status, progress, created_at)
                        VALUES(?, ?, ?, ?, ?, 'PENDING', 0, ?)
                        """)) {
                    prepared.setString(1, deliveryId);
                    prepared.setString(2, orderRef);
                    prepared.setString(3, address);
                    prepared.setString(4, warehouse);
                    prepared.setInt(5, eta);
                    prepared.setString(6, now);
                    prepared.executeUpdate();
                }
                insertLog(connection, deliveryId, null, DeliveryStatus.PENDING, actor,
                        "Sample order generated for testing and driver assignment");
                Delivery created = findById(connection, deliveryId).orElseThrow();
                connection.commit();
                return created;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw translate("Unable to generate a sample order", exception);
        }
    }

    public synchronized Delivery createFromShippedOrder(String orderReference, String deliveryAddress,
                                                        String warehouseLocation, int estimatedMinutes,
                                                        String actor) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<Delivery> existing = findByOrderReference(connection, orderReference);
                if (existing.isPresent()) {
                    connection.commit();
                    return existing.get();
                }

                String deliveryId = "DEL-%04d".formatted(nextDeliverySequence(connection));
                try (PreparedStatement prepared = connection.prepareStatement("""
                        INSERT INTO deliveries(delivery_id, order_reference, delivery_address, warehouse_location,
                            estimated_minutes, status, progress, created_at)
                        VALUES(?, ?, ?, ?, ?, 'PENDING', 0, ?)
                        """)) {
                    prepared.setString(1, deliveryId);
                    prepared.setString(2, orderReference);
                    prepared.setString(3, deliveryAddress);
                    prepared.setString(4, warehouseLocation);
                    prepared.setInt(5, estimatedMinutes);
                    prepared.setString(6, DbTime.format(LocalDateTime.now()));
                    prepared.executeUpdate();
                }
                insertLog(connection, deliveryId, null, DeliveryStatus.PENDING, actor,
                        "Automatically created after the Orders component marked the order Shipped");
                Delivery created = findById(connection, deliveryId).orElseThrow();
                connection.commit();
                return created;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw translate("Unable to create a delivery from the Shipped order", exception);
        }
    }

    public synchronized Delivery assignDriver(Delivery delivery, Driver driver, LocalDateTime start,
                                              int estimatedMinutes, String actor) {
        DeliveryValidator.validateAssignment(delivery, driver, start, estimatedMinutes);
        LocalDateTime end = start.plusMinutes(estimatedMinutes);
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Delivery current = findById(connection, delivery.deliveryId())
                        .orElseThrow(() -> new ValidationException("The selected delivery no longer exists."));
                if (current.status() != DeliveryStatus.PENDING) {
                    throw new ValidationException("Only pending deliveries can be assigned or reassigned.");
                }
                ensureDriverAvailable(connection, driver.driverId());
                ensureNoOverlap(connection, current.deliveryId(), driver.driverId(), start, end);

                try (PreparedStatement prepared = connection.prepareStatement("""
                        UPDATE deliveries
                        SET assigned_driver_id = ?, scheduled_start = ?, scheduled_end = ?, estimated_minutes = ?
                        WHERE delivery_id = ? AND status = 'PENDING'
                        """)) {
                    prepared.setString(1, driver.driverId());
                    prepared.setString(2, DbTime.format(start));
                    prepared.setString(3, DbTime.format(end));
                    prepared.setInt(4, estimatedMinutes);
                    prepared.setString(5, current.deliveryId());
                    if (prepared.executeUpdate() != 1) {
                        throw new ValidationException("The delivery changed while it was being assigned. Please refresh and try again.");
                    }
                }
                String action = current.assignedDriverId() == null ? "Assigned" : "Reassigned";
                insertLog(connection, current.deliveryId(), current.status(), current.status(), actor,
                        action + " to " + driver.fullName() + " for " + DbTime.format(start));
                Delivery updated = findById(connection, current.deliveryId()).orElseThrow();
                connection.commit();
                return updated;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw translate("Unable to assign the driver", exception);
        }
    }

    public synchronized Delivery updateStatus(String deliveryId, DeliveryStatus target,
                                              String actor, String note) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Delivery current = findById(connection, deliveryId)
                        .orElseThrow(() -> new ValidationException("Delivery " + deliveryId + " was not found."));
                validateStatusTransition(current, target);

                String started = current.startedAt() == null && target == DeliveryStatus.IN_TRANSIT
                        ? DbTime.format(LocalDateTime.now()) : DbTime.format(current.startedAt());
                String completed = target.isTerminal()
                        ? DbTime.format(LocalDateTime.now()) : DbTime.format(current.completedAt());
                try (PreparedStatement prepared = connection.prepareStatement("""
                        UPDATE deliveries SET status = ?, progress = ?, started_at = ?, completed_at = ?
                        WHERE delivery_id = ? AND status = ?
                        """)) {
                    prepared.setString(1, target.name());
                    prepared.setInt(2, target.getProgress());
                    prepared.setString(3, started);
                    prepared.setString(4, completed);
                    prepared.setString(5, deliveryId);
                    prepared.setString(6, current.status().name());
                    if (prepared.executeUpdate() != 1) {
                        throw new ValidationException("The delivery status changed concurrently. Please refresh.");
                    }
                }
                insertLog(connection, deliveryId, current.status(), target, actor, note);
                Delivery updated = findById(connection, deliveryId).orElseThrow();
                connection.commit();
                return updated;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw translate("Unable to update delivery status", exception);
        }
    }

    private void validateStatusTransition(Delivery current, DeliveryStatus target) {
        if (current.status().isTerminal()) {
            throw new ValidationException("Completed or failed deliveries cannot be advanced.");
        }
        boolean normalTransition = current.status().next(true) == target || current.status().next(false) == target;
        boolean activeFailure = current.status().isActive() && target == DeliveryStatus.FAILED;
        if (!normalTransition && !activeFailure) {
            throw new ValidationException("Invalid status transition from " + current.status() + " to " + target + ".");
        }
        if (target == DeliveryStatus.IN_TRANSIT) {
            if (current.assignedDriverId() == null || current.scheduledStart() == null) {
                throw new ValidationException("Assign and schedule the delivery before starting it.");
            }
            if (current.scheduledStart().isAfter(LocalDateTime.now())) {
                throw new ValidationException("This delivery cannot start before " + current.scheduledDisplay() + ".");
            }
        }
    }

    private void ensureDriverAvailable(Connection connection, String driverId) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(
                "SELECT available FROM drivers WHERE driver_id = ?")) {
            prepared.setString(1, driverId);
            try (ResultSet result = prepared.executeQuery()) {
                if (!result.next()) {
                    throw new ValidationException("The selected driver does not exist.");
                }
                if (!result.getBoolean(1)) {
                    throw new ValidationException("The selected driver is currently unavailable.");
                }
            }
        }
    }

    private void ensureNoOverlap(Connection connection, String deliveryId, String driverId,
                                 LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = """
                SELECT delivery_id FROM deliveries
                WHERE delivery_id <> ? AND assigned_driver_id = ?
                  AND status NOT IN ('DELIVERED', 'FAILED')
                  AND scheduled_start IS NOT NULL AND scheduled_end IS NOT NULL
                  AND ? < scheduled_end AND ? > scheduled_start
                LIMIT 1
                """;
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setString(1, deliveryId);
            prepared.setString(2, driverId);
            prepared.setString(3, DbTime.format(start));
            prepared.setString(4, DbTime.format(end));
            try (ResultSet result = prepared.executeQuery()) {
                if (result.next()) {
                    throw new ValidationException("Driver schedule overlaps delivery " + result.getString(1) + ".");
                }
            }
        }
    }

    private Optional<Delivery> findById(Connection connection, String deliveryId) throws SQLException {
        String sql = DELIVERY_SELECT + " WHERE d.delivery_id = ?";
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setString(1, deliveryId);
            try (ResultSet result = prepared.executeQuery()) {
                return result.next() ? Optional.of(mapDelivery(result)) : Optional.empty();
            }
        }
    }

    private Optional<Delivery> findByOrderReference(Connection connection, String orderReference) throws SQLException {
        String sql = DELIVERY_SELECT + " WHERE d.order_reference = ?";
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setString(1, orderReference);
            try (ResultSet result = prepared.executeQuery()) {
                return result.next() ? Optional.of(mapDelivery(result)) : Optional.empty();
            }
        }
    }

    private int nextDeliverySequence(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COALESCE(MAX(CAST(substr(delivery_id, 5) AS INTEGER)), 0) + 1 FROM deliveries")) {
            return result.next() ? result.getInt(1) : 1;
        }
    }

    private void insertLog(Connection connection, String deliveryId, DeliveryStatus from, DeliveryStatus to,
                           String actor, String note) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement("""
                INSERT INTO delivery_status_log(delivery_id, from_status, to_status, changed_at, changed_by, note)
                VALUES(?, ?, ?, ?, ?, ?)
                """)) {
            prepared.setString(1, deliveryId);
            if (from == null) {
                prepared.setNull(2, Types.VARCHAR);
            } else {
                prepared.setString(2, from.name());
            }
            prepared.setString(3, to.name());
            prepared.setString(4, DbTime.format(LocalDateTime.now()));
            prepared.setString(5, actor == null || actor.isBlank() ? "system" : actor);
            prepared.setString(6, note == null ? "" : note);
            prepared.executeUpdate();
        }
    }

    private List<Delivery> readDeliveries(ResultSet result) throws SQLException {
        List<Delivery> deliveries = new ArrayList<>();
        try (result) {
            while (result.next()) {
                deliveries.add(mapDelivery(result));
            }
        }
        return deliveries;
    }

    private Delivery mapDelivery(ResultSet result) throws SQLException {
        return new Delivery(
                result.getString("delivery_id"),
                result.getString("order_reference"),
                result.getString("assigned_driver_id"),
                result.getString("driver_name"),
                result.getString("delivery_address"),
                result.getString("warehouse_location"),
                DbTime.parse(result.getString("scheduled_start")),
                DbTime.parse(result.getString("scheduled_end")),
                result.getInt("estimated_minutes"),
                DeliveryStatus.fromDatabase(result.getString("status")),
                result.getInt("progress"),
                DbTime.parse(result.getString("created_at")),
                DbTime.parse(result.getString("started_at")),
                DbTime.parse(result.getString("completed_at"))
        );
    }

    private Driver mapDriver(ResultSet result) throws SQLException {
        return new Driver(
                result.getString("driver_id"),
                result.getString("full_name"),
                result.getString("username"),
                result.getBoolean("available"),
                result.getDouble("latitude"),
                result.getDouble("longitude")
        );
    }

    private RuntimeException translate(String message, SQLException exception) {
        String detail = exception.getMessage() == null ? "" : exception.getMessage();
        if (detail.contains("overlaps") || detail.contains("active delivery")) {
            return new ValidationException(detail);
        }
        return new DatabaseException(message, exception);
    }
}
