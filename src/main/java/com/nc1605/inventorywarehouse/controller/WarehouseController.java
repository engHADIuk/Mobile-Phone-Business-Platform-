package com.nc1605.inventorywarehouse.controller;

import com.nc1605.inventorywarehouse.App;
import com.nc1605.inventorywarehouse.data.Database;
import com.nc1605.inventorywarehouse.model.Warehouse;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Manages warehouse details and provides quick access to each location's stock. */
public class WarehouseController extends BaseController {
    @FXML private FlowPane warehouseCards;
    @FXML private GridPane warehouseForm;
    @FXML private Label formTitle;
    @FXML private Label messageLabel;
    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextField capacityField;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    private Warehouse selectedWarehouse;
    private String returnScreen;

    @FXML public void initialize() {
        refreshCards();
        setFormVisible(false);
        returnScreen = App.takeReturnScreen();
        backButton.setVisible(!returnScreen.isBlank());
        backButton.setManaged(!returnScreen.isBlank());
    }

    @FXML private void newWarehouse() {
        selectedWarehouse = null;
        formTitle.setText("Add warehouse");
        saveButton.setText("Save warehouse");
        nameField.clear();
        locationField.clear();
        capacityField.clear();
        clearMessage();
        setFormVisible(true);
        nameField.requestFocus();
    }

    private void editWarehouse(Warehouse warehouse) {
        selectedWarehouse = warehouse;
        formTitle.setText("Edit " + warehouse.getName());
        saveButton.setText("Update warehouse");
        nameField.setText(warehouse.getName());
        locationField.setText(warehouse.getLocation());
        capacityField.setText(String.valueOf(warehouse.getCapacity()));
        clearMessage();
        setFormVisible(true);
    }

    @FXML private void saveWarehouse() {
        try {
            String name = validateText(nameField.getText(), "Warehouse name");
            String location = validateText(locationField.getText(), "Location");
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) throw new IllegalArgumentException("Capacity must be greater than zero.");

            if (selectedWarehouse == null) Database.addWarehouse(name, location, capacity);
            else Database.updateWarehouse(selectedWarehouse.getId(), name, location, capacity);

            messageLabel.setText(selectedWarehouse == null ? "Warehouse added successfully." : "Warehouse updated successfully.");
            messageLabel.getStyleClass().setAll("success");
            selectedWarehouse = null;
            refreshCards();
            nameField.clear();
            locationField.clear();
            capacityField.clear();
            formTitle.setText("Add warehouse");
            saveButton.setText("Save warehouse");
        } catch (NumberFormatException e) {
            showError("Capacity must be a whole number.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private String validateText(String value, String fieldName) {
        String text = value == null ? "" : value.trim();
        if (text.isBlank()) throw new IllegalArgumentException(fieldName + " is required.");
        if (text.length() > 50) throw new IllegalArgumentException(fieldName + " must be 50 characters or fewer.");
        if (!text.matches("[A-Za-z0-9 .'-]+")) throw new IllegalArgumentException(fieldName + " contains unsupported characters.");
        return text;
    }

    @FXML private void cancelEdit() {
        selectedWarehouse = null;
        setFormVisible(false);
        clearMessage();
    }

    private void refreshCards() {
        warehouseCards.getChildren().clear();
        for (Warehouse warehouse : Database.warehouses()) warehouseCards.getChildren().add(createCard(warehouse));
    }

    private VBox createCard(Warehouse warehouse) {
        VBox card = new VBox();
        card.getStyleClass().add("warehouse-card");
        Label name = new Label(warehouse.getName()); name.getStyleClass().add("warehouse-name");
        Label location = new Label("Location: " + warehouse.getLocation()); location.getStyleClass().add("warehouse-location");
        HBox capacityLine = new HBox(8);
        Label capacity = new Label("Capacity"); capacity.getStyleClass().add("warehouse-detail");
        Label count = new Label(warehouse.getUsed() + " / " + warehouse.getCapacity()); count.getStyleClass().add("warehouse-detail");
        HBox.setHgrow(capacity, Priority.ALWAYS); capacityLine.getChildren().addAll(capacity, count);
        ProgressBar bar = new ProgressBar(warehouse.getCapacity() == 0 ? 0 : (double) warehouse.getUsed() / warehouse.getCapacity());
        bar.setMaxWidth(Double.MAX_VALUE);
        Label utilisation = new Label(warehouse.getUtilisation() + " utilised"); utilisation.getStyleClass().add("warehouse-location");
        Button view = new Button("View stock"); view.getStyleClass().add("small-button");
        view.setOnAction(event -> viewStock(warehouse));
        Button edit = new Button("Edit details"); edit.getStyleClass().addAll("small-button", "secondary-button");
        edit.setOnAction(event -> editWarehouse(warehouse));
        HBox actions = new HBox(8, view, edit);
        card.getChildren().addAll(name, location, new Label(""), capacityLine, bar, utilisation, actions);
        return card;
    }

    private void viewStock(Warehouse warehouse) {
        try { App.showInventoryForWarehouse(warehouse.getName()); }
        catch (IOException e) { showError("Could not open this warehouse's stock."); }
    }

    @FXML private void backToSource() throws IOException {
        if (!returnScreen.isBlank()) App.show(returnScreen);
    }

    private void setFormVisible(boolean visible) {
        warehouseForm.setVisible(visible);
        warehouseForm.setManaged(visible);
    }

    private void clearMessage() { messageLabel.setText(""); messageLabel.getStyleClass().clear(); }
    private void showError(String text) { messageLabel.setText(text); messageLabel.getStyleClass().setAll("error"); }
}
