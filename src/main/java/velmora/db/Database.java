package velmora.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_FILE = "velmora.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        System.out.println("Using SQLite database at: " + new java.io.File(DB_FILE).getAbsolutePath());

        String createOrders =
                "CREATE TABLE IF NOT EXISTS orders (" +
                "  id TEXT PRIMARY KEY," +
                "  summary TEXT NOT NULL," +
                "  order_date TEXT NOT NULL," +
                "  total REAL NOT NULL," +
                "  status TEXT NOT NULL," +
                "  customer_name TEXT NOT NULL DEFAULT ''," +
                "  customer_email TEXT NOT NULL DEFAULT ''," +
                "  updated_at TEXT NOT NULL DEFAULT ''," +
                "  shipping_address TEXT NOT NULL DEFAULT ''," +
                "  card_last4 TEXT NOT NULL DEFAULT ''" +
                ")";

        String createUsers =
                "CREATE TABLE IF NOT EXISTS users (" +
                "  email TEXT PRIMARY KEY," +
                "  password TEXT NOT NULL," +
                "  name TEXT NOT NULL" +
                ")";

        String createProducts =
                "CREATE TABLE IF NOT EXISTS products (" +
                "  storage TEXT PRIMARY KEY," +
                "  price REAL NOT NULL," +
                "  old_price REAL NOT NULL," +
                "  stock INTEGER NOT NULL" +
                ")";

        String createCatalog =
                "CREATE TABLE IF NOT EXISTS product_catalog (" +
                "  id INTEGER PRIMARY KEY CHECK (id = 1)," +
                "  name TEXT NOT NULL," +
                "  description TEXT NOT NULL" +
                ")";

        String createCartItems =
                "CREATE TABLE IF NOT EXISTS cart_items (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL," +
                "  color TEXT NOT NULL," +
                "  storage TEXT NOT NULL," +
                "  price REAL NOT NULL," +
                "  old_price REAL NOT NULL," +
                "  stock INTEGER NOT NULL," +
                "  qty INTEGER NOT NULL" +
                ")";

        String createOrderItems =
                "CREATE TABLE IF NOT EXISTS order_items (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  order_id TEXT NOT NULL," +
                "  product_name TEXT NOT NULL," +
                "  color TEXT NOT NULL," +
                "  storage TEXT NOT NULL," +
                "  qty INTEGER NOT NULL," +
                "  unit_price REAL NOT NULL," +
                "  line_total REAL NOT NULL" +
                ")";

        String createReturns =
                "CREATE TABLE IF NOT EXISTS returns (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  order_id TEXT NOT NULL," +
                "  return_date TEXT NOT NULL," +
                "  reason TEXT NOT NULL DEFAULT ''" +
                ")";

        String createReturnedItems =
                "CREATE TABLE IF NOT EXISTS returned_items (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  return_id INTEGER NOT NULL," +
                "  product_name TEXT NOT NULL," +
                "  color TEXT NOT NULL," +
                "  storage TEXT NOT NULL," +
                "  qty INTEGER NOT NULL" +
                ")";

        String createAdmins =
                "CREATE TABLE IF NOT EXISTS admins (" +
                "  username TEXT PRIMARY KEY," +
                "  password TEXT NOT NULL" +
                ")";

        try (Connection c = connect(); Statement stmt = c.createStatement()) {
            stmt.execute(createOrders);
            stmt.execute(createUsers);
            stmt.execute(createProducts);
            stmt.execute(createCatalog);
            stmt.execute(createCartItems);
            stmt.execute(createOrderItems);
            stmt.execute(createReturns);
            stmt.execute(createReturnedItems);
            stmt.execute(createAdmins);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }

        migrateExistingTables();
    }

    private static void migrateExistingTables() {
        tryAddColumn("orders", "customer_name", "TEXT NOT NULL DEFAULT ''");
        tryAddColumn("orders", "customer_email", "TEXT NOT NULL DEFAULT ''");
        tryAddColumn("orders", "updated_at", "TEXT NOT NULL DEFAULT ''");
        tryAddColumn("orders", "shipping_address", "TEXT NOT NULL DEFAULT ''");
        tryAddColumn("orders", "card_last4", "TEXT NOT NULL DEFAULT ''");
        tryAddColumn("returns", "reason", "TEXT NOT NULL DEFAULT ''");
    }

    private static void tryAddColumn(String table, String column, String definition) {
        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
        try (Connection c = connect(); Statement stmt = c.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {

            if (e.getMessage() == null || !e.getMessage().toLowerCase().contains("duplicate column")) {
                throw new RuntimeException("Failed to migrate " + table + "." + column, e);
            }
        }
    }
}
