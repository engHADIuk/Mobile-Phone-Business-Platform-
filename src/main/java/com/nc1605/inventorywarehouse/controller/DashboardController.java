package com.nc1605.inventorywarehouse.controller;
import com.nc1605.inventorywarehouse.App; import com.nc1605.inventorywarehouse.data.Database;
import com.nc1605.inventorywarehouse.model.InventoryItem;
import javafx.collections.FXCollections; import javafx.fxml.FXML; import javafx.scene.control.Label; import javafx.scene.control.ListView; import java.io.IOException;
public class DashboardController extends BaseController {
    @FXML private Label totalUnitsLabel, lowStockLabel, criticalLabel, warehouseLabel;
    @FXML private ListView<String> alertsList;
    @FXML public void initialize() { var items=Database.inventory("","All"); totalUnitsLabel.setText(String.valueOf(items.stream().mapToInt(InventoryItem::getQuantity).sum())); lowStockLabel.setText(String.valueOf(items.stream().filter(i->i.getStatus().equals("Low Stock")).count())); criticalLabel.setText(String.valueOf(items.stream().filter(i->i.getStatus().equals("Critical")).count())); warehouseLabel.setText(String.valueOf(Database.warehouses().size()));
        var alerts=items.stream().filter(i->!i.getStatus().equals("In Stock")).map(i->i.getStatus()+" — "+i.getProductName()+" at "+i.getWarehouseName()+": "+i.getQuantity()+" units (minimum "+i.getThreshold()+")").toList();
        alertsList.setItems(FXCollections.observableArrayList(alerts.isEmpty() ? java.util.List.of("No low-stock alerts. All products are above their minimum levels.") : alerts));
        alertsList.setFixedCellSize(32); double listHeight=alertsList.getItems().size()*32+4; alertsList.setPrefHeight(listHeight); alertsList.setMinHeight(listHeight); alertsList.setMaxHeight(listHeight); }
    @FXML private void showAllInventory() throws IOException { App.showInventoryFrom("All", "Dashboard"); }
    @FXML private void showLowStock() throws IOException { App.showInventoryFrom("Low Stock", "Dashboard"); }
    @FXML private void showCriticalStock() throws IOException { App.showInventoryFrom("Critical", "Dashboard"); }
    @FXML private void openWarehouses() throws IOException { App.showScreenFrom("Warehouses", "Dashboard"); }
    @FXML private void openSelectedAlert() throws IOException { String selected = alertsList.getSelectionModel().getSelectedItem(); if (selected != null && !selected.startsWith("No low")) App.showInventoryFrom(selected.startsWith("Critical") ? "Critical" : "Low Stock", "Dashboard"); }
}
