package velmora;

import javafx.application.Application;
import javafx.stage.Stage;

import velmora.db.AdminDao;
import velmora.db.CatalogDao;
import velmora.db.Database;
import velmora.db.ProductDao;
import velmora.db.UserDao;
import velmora.model.OrderStatusScheduler;
import velmora.model.StorageOption;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("=== UNCAUGHT EXCEPTION ===");
            throwable.printStackTrace();
        });

        Database.initialize();
        seedProductsIfEmpty();
        seedCatalogIfEmpty();
        seedAdminIfEmpty();
        seedDemoUserIfEmpty();
        OrderStatusScheduler.start();

        SceneManager.init(primaryStage);
        primaryStage.setTitle("VELMORA — Luxury Tech");
        SceneManager.show("login");
        primaryStage.show();
    }

    private void seedProductsIfEmpty() {
        ProductDao dao = new ProductDao();
        if (dao.isEmpty()) {
            dao.insert(new StorageOption("128GB", 899, 1099, 16));
            dao.insert(new StorageOption("256GB", 999, 1199, 9));
            dao.insert(new StorageOption("512GB", 1199, 1399, 3));
        }
    }

    private void seedCatalogIfEmpty() {
        CatalogDao dao = new CatalogDao();
        if (dao.isEmpty()) {
            dao.insert("Velmora X1",
                    "A premium smartphone with a titanium frame, all-day battery life, "
                    + "and a pro-grade camera system. Available in three storage tiers.");
        }
    }

    private void seedAdminIfEmpty() {
        AdminDao dao = new AdminDao();
        if (dao.isEmpty()) {
            dao.insert("admin", "admin123");
        }
    }

    private void seedDemoUserIfEmpty() {
        UserDao dao = new UserDao();
        if (dao.isEmpty()) {
            dao.insert("demo@velmora.com", "demo1234", "Demo Customer");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
