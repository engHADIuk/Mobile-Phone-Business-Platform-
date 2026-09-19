package uk.ac.brunel.delivery.db;

import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.util.PasswordUtil;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public final class DatabaseManager {
    private final String databaseUrl;

    public DatabaseManager(Path databasePath) {
        this.databaseUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath().normalize();
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("PRAGMA journal_mode = WAL");
        }
        return connection;
    }

    public void initialize() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS drivers (
                        driver_id TEXT PRIMARY KEY,
                        full_name TEXT NOT NULL,
                        username TEXT NOT NULL UNIQUE,
                        available INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0, 1)),
                        latitude REAL NOT NULL DEFAULT 0,
                        longitude REAL NOT NULL DEFAULT 0
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        display_name TEXT NOT NULL,
                        role TEXT NOT NULL CHECK (role IN ('ADMIN', 'LOGISTICS_MANAGER', 'DRIVER')),
                        driver_id TEXT UNIQUE,
                        active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
                        FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS deliveries (
                        delivery_id TEXT PRIMARY KEY,
                        order_reference TEXT NOT NULL UNIQUE,
                        assigned_driver_id TEXT,
                        delivery_address TEXT NOT NULL CHECK (length(trim(delivery_address)) >= 5),
                        warehouse_location TEXT NOT NULL CHECK (length(trim(warehouse_location)) >= 3),
                        scheduled_start TEXT,
                        scheduled_end TEXT,
                        estimated_minutes INTEGER NOT NULL CHECK (estimated_minutes BETWEEN 15 AND 480),
                        status TEXT NOT NULL CHECK (status IN ('PENDING', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED')),
                        progress INTEGER NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
                        created_at TEXT NOT NULL,
                        started_at TEXT,
                        completed_at TEXT,
                        FOREIGN KEY (assigned_driver_id) REFERENCES drivers(driver_id),
                        CHECK ((assigned_driver_id IS NULL AND scheduled_start IS NULL AND scheduled_end IS NULL)
                            OR (assigned_driver_id IS NOT NULL AND scheduled_start IS NOT NULL AND scheduled_end IS NOT NULL))
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS delivery_status_log (
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        delivery_id TEXT NOT NULL,
                        from_status TEXT,
                        to_status TEXT NOT NULL,
                        changed_at TEXT NOT NULL,
                        changed_by TEXT NOT NULL,
                        note TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY (delivery_id) REFERENCES deliveries(delivery_id) ON DELETE CASCADE
                    )
                    """);
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_deliveries_driver ON deliveries(assigned_driver_id)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_logs_delivery ON delivery_status_log(delivery_id, changed_at)");
            createIntegrityTriggers(statement);
            seedIfEmpty(connection);
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to initialise the Velmora database", exception);
        }
    }

    private void createIntegrityTriggers(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TRIGGER IF NOT EXISTS deliveries_no_overlap_insert
                BEFORE INSERT ON deliveries
                WHEN NEW.assigned_driver_id IS NOT NULL
                BEGIN
                    SELECT CASE WHEN EXISTS (
                        SELECT 1 FROM deliveries existing
                        WHERE existing.assigned_driver_id = NEW.assigned_driver_id
                          AND existing.status NOT IN ('DELIVERED', 'FAILED')
                          AND NEW.scheduled_start < existing.scheduled_end
                          AND NEW.scheduled_end > existing.scheduled_start
                    ) THEN RAISE(ABORT, 'Driver schedule overlaps an existing delivery') END;
                END
                """);
        statement.executeUpdate("""
                CREATE TRIGGER IF NOT EXISTS deliveries_no_overlap_update
                BEFORE UPDATE OF assigned_driver_id, scheduled_start, scheduled_end ON deliveries
                WHEN NEW.assigned_driver_id IS NOT NULL
                BEGIN
                    SELECT CASE WHEN EXISTS (
                        SELECT 1 FROM deliveries existing
                        WHERE existing.delivery_id <> NEW.delivery_id
                          AND existing.assigned_driver_id = NEW.assigned_driver_id
                          AND existing.status NOT IN ('DELIVERED', 'FAILED')
                          AND NEW.scheduled_start < existing.scheduled_end
                          AND NEW.scheduled_end > existing.scheduled_start
                    ) THEN RAISE(ABORT, 'Driver schedule overlaps an existing delivery') END;
                END
                """);
        statement.executeUpdate("""
                CREATE TRIGGER IF NOT EXISTS deliveries_one_active_per_driver
                BEFORE UPDATE OF status, assigned_driver_id ON deliveries
                WHEN NEW.assigned_driver_id IS NOT NULL AND NEW.status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY')
                BEGIN
                    SELECT CASE WHEN EXISTS (
                        SELECT 1 FROM deliveries existing
                        WHERE existing.delivery_id <> NEW.delivery_id
                          AND existing.assigned_driver_id = NEW.assigned_driver_id
                          AND existing.status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY')
                    ) THEN RAISE(ABORT, 'Driver already has an active delivery') END;
                END
                """);
    }

    private void seedIfEmpty(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            if (result.next() && result.getInt(1) > 0) {
                return;
            }
        }

        connection.setAutoCommit(false);
        try {
            seedDrivers(connection);
            seedUsers(connection);
            seedDeliveries(connection);
            connection.commit();
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void seedDrivers(Connection connection) throws SQLException {
        String sql = "INSERT INTO drivers(driver_id, full_name, username, available, latitude, longitude) VALUES(?, ?, ?, 1, ?, ?)";
        List<Object[]> drivers = List.of(
                new Object[]{"DRV-001", "Alex Morgan", "alex", 0.18, 0.20},
                new Object[]{"DRV-002", "Priya Sharma", "priya", 0.35, 0.38},
                new Object[]{"DRV-003", "James Carter", "james", 0.52, 0.58},
                new Object[]{"DRV-004", "Layla Hassan", "layla", 0.70, 0.44},
                new Object[]{"DRV-005", "Tom Nguyen", "tom", 0.82, 0.72}
        );
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            for (Object[] driver : drivers) {
                prepared.setString(1, (String) driver[0]);
                prepared.setString(2, (String) driver[1]);
                prepared.setString(3, (String) driver[2]);
                prepared.setDouble(4, (Double) driver[3]);
                prepared.setDouble(5, (Double) driver[4]);
                prepared.addBatch();
            }
            prepared.executeBatch();
        }
    }

    private void seedUsers(Connection connection) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, display_name, role, driver_id) VALUES(?, ?, ?, ?, ?)";
        List<Object[]> users = List.of(
                new Object[]{"admin", "Admin123!", "System Administrator", "ADMIN", null},
                new Object[]{"logistics", "Logistics123!", "Logistics Manager", "LOGISTICS_MANAGER", null},
                new Object[]{"alex", "Driver123!", "Alex Morgan", "DRIVER", "DRV-001"},
                new Object[]{"priya", "Driver123!", "Priya Sharma", "DRIVER", "DRV-002"},
                new Object[]{"james", "Driver123!", "James Carter", "DRIVER", "DRV-003"},
                new Object[]{"layla", "Driver123!", "Layla Hassan", "DRIVER", "DRV-004"},
                new Object[]{"tom", "Driver123!", "Tom Nguyen", "DRIVER", "DRV-005"}
        );
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            for (Object[] user : users) {
                prepared.setString(1, (String) user[0]);
                prepared.setString(2, PasswordUtil.hash((String) user[1]));
                prepared.setString(3, (String) user[2]);
                prepared.setString(4, (String) user[3]);
                prepared.setString(5, (String) user[4]);
                prepared.addBatch();
            }
            prepared.executeBatch();
        }
    }

    private void seedDeliveries(Connection connection) throws SQLException {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<SeedDelivery> deliveries = List.of(
                new SeedDelivery("DEL-0001", "ORD-1001", null, "7 Oxford Street", "West London Hub", null, 45, DeliveryStatus.PENDING),
                new SeedDelivery("DEL-0002", "ORD-1002", "DRV-001", "55 Baker Street", "West London Hub", now.plusMinutes(2), 50, DeliveryStatus.PENDING),
                new SeedDelivery("DEL-0003", "ORD-1003", "DRV-002", "23 Canary Wharf", "Docklands Hub", now.minusMinutes(10), 45, DeliveryStatus.IN_TRANSIT),
                new SeedDelivery("DEL-0004", "ORD-1004", "DRV-003", "10 King's Road", "Central London Hub", now.minusMinutes(25), 45, DeliveryStatus.OUT_FOR_DELIVERY),
                new SeedDelivery("DEL-0005", "ORD-1005", "DRV-004", "88 Regent Street", "Central London Hub", now.minusHours(2), 40, DeliveryStatus.DELIVERED),
                new SeedDelivery("DEL-0006", "ORD-1006", "DRV-005", "3 Portobello Road", "West London Hub", now.minusHours(2), 35, DeliveryStatus.FAILED),
                new SeedDelivery("DEL-0007", "ORD-1007", "DRV-002", "99 Greenwich High Road", "Docklands Hub", now.plusMinutes(60), 45, DeliveryStatus.PENDING),
                new SeedDelivery("DEL-0008", "ORD-1008", "DRV-001", "12 Victoria Street", "Central London Hub", now.minusHours(4), 35, DeliveryStatus.DELIVERED)
        );

        String sql = """
                INSERT INTO deliveries(delivery_id, order_reference, assigned_driver_id, delivery_address,
                  warehouse_location, scheduled_start, scheduled_end, estimated_minutes, status, progress,
                  created_at, started_at, completed_at)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            for (SeedDelivery delivery : deliveries) {
                LocalDateTime end = delivery.start == null ? null : delivery.start.plusMinutes(delivery.etaMinutes);
                prepared.setString(1, delivery.id);
                prepared.setString(2, delivery.orderRef);
                prepared.setString(3, delivery.driverId);
                prepared.setString(4, delivery.address);
                prepared.setString(5, delivery.warehouse);
                prepared.setString(6, DbTime.format(delivery.start));
                prepared.setString(7, DbTime.format(end));
                prepared.setInt(8, delivery.etaMinutes);
                prepared.setString(9, delivery.status.name());
                prepared.setInt(10, delivery.status.getProgress());
                prepared.setString(11, DbTime.format(now.minusHours(6)));
                prepared.setString(12, delivery.status == DeliveryStatus.PENDING ? null : DbTime.format(delivery.start));
                prepared.setString(13, delivery.status.isTerminal() ? DbTime.format(end) : null);
                prepared.addBatch();
            }
            prepared.executeBatch();
        }

        try (PreparedStatement prepared = connection.prepareStatement("""
                INSERT INTO delivery_status_log(delivery_id, from_status, to_status, changed_at, changed_by, note)
                VALUES(?, NULL, ?, ?, 'seed-data', 'Initial demonstration record')
                """)) {
            for (SeedDelivery delivery : deliveries) {
                prepared.setString(1, delivery.id);
                prepared.setString(2, delivery.status.name());
                prepared.setString(3, DbTime.format(now.minusHours(6)));
                prepared.addBatch();
            }
            prepared.executeBatch();
        }
    }

    private record SeedDelivery(String id, String orderRef, String driverId, String address,
                                String warehouse, LocalDateTime start, int etaMinutes, DeliveryStatus status) {
    }
}
