package com.nc1605.inventorywarehouse;

import com.nc1605.inventorywarehouse.data.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/** Starts the program and provides one place for screen navigation. */
public class App extends Application {
    private static Stage stage;
    private static Scene scene;
    private static String requestedInventoryFilter = "All";
    private static String requestedWarehouseFilter = "";
    private static String requestedReturnScreen = "";

    @Override public void start(Stage primaryStage) throws IOException {
        Database.initialise();
        stage = primaryStage;
        stage.setTitle("VELMORA Inventory System");
        show("Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void show(String screen) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource("/com/nc1605/inventorywarehouse/" + screen + ".fxml"));
        if (scene == null) {
            scene = new Scene(root, 1150, 720);
            scene.getStylesheets().add(App.class.getResource("/com/nc1605/inventorywarehouse/app.css").toExternalForm());
        } else {
            scene.setRoot(root);
        }
    }

    public static void showInventory(String filter) throws IOException {
        requestedInventoryFilter = filter;
        requestedWarehouseFilter = "";
        requestedReturnScreen = "";
        show("Inventory");
    }

    public static void showInventoryFrom(String filter, String returnScreen) throws IOException {
        requestedInventoryFilter = filter;
        requestedWarehouseFilter = "";
        requestedReturnScreen = returnScreen;
        show("Inventory");
    }

    public static void showScreenFrom(String screen, String returnScreen) throws IOException {
        requestedReturnScreen = returnScreen;
        show(screen);
    }

    public static void showInventoryForWarehouse(String warehouseName) throws IOException {
        requestedInventoryFilter = "All";
        requestedWarehouseFilter = warehouseName;
        requestedReturnScreen = "Warehouses";
        show("Inventory");
    }

    public static String takeInventoryFilter() { String filter = requestedInventoryFilter; requestedInventoryFilter = "All"; return filter; }
    public static String takeWarehouseFilter() { String filter = requestedWarehouseFilter; requestedWarehouseFilter = ""; return filter; }
    public static String takeReturnScreen() { String screen = requestedReturnScreen; requestedReturnScreen = ""; return screen; }

    public static void main(String[] args) { launch(args); }
}
