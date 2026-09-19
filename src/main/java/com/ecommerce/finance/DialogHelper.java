package com.ecommerce.finance;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DialogHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static <T> void editSelected(TableView<T> table, String title) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select a row to edit.", Alert.AlertType.WARNING);
            return;
        }
        showEditDialog(table, selected, title, "Edit");
    }

    public static <T> void modifySelected(TableView<T> table, String title) {
        editSelected(table, title);
    }

    public static <T> void deleteSelected(TableView<T> table) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select a row to delete.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this row?", ButtonType.YES, ButtonType.NO);
        styleAlert(confirm);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            table.getItems().remove(selected);
            showAlert("Deleted", "Row deleted successfully.", Alert.AlertType.INFORMATION);
        }
    }

    public static void printTable(Node tableNode) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tableNode.getScene().getWindow())) {
            if (job.printPage(tableNode)) {
                job.endJob();
                showAlert("Print", "Print job completed.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Print", "Print failed.", Alert.AlertType.ERROR);
            }
        }
    }

    // ---------- Generic Edit Dialog using JavaBean Introspection ----------
    @SuppressWarnings("unchecked")
    private static <T> void showEditDialog(TableView<T> table, T item, String title, String action) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(action + " " + title);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        List<PropertyDescriptor> properties = new ArrayList<>();
        List<Method> getters = new ArrayList<>();
        List<Method> setters = new ArrayList<>();
        List<Object> currentValues = new ArrayList<>();
        List<TextField> textFields = new ArrayList<>();

        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(item.getClass()).getPropertyDescriptors()) {
                if (pd.getName().equals("class")) continue;
                Method getter = pd.getReadMethod();
                Method setter = pd.getWriteMethod();
                if (getter != null && setter != null) {
                    Class<?> type = pd.getPropertyType();
                    if (type == String.class || type == Double.class || type == double.class ||
                        type == Integer.class || type == int.class || type == Long.class || type == long.class ||
                        type == Boolean.class || type == boolean.class || type == LocalDate.class) {
                        properties.add(pd);
                        getters.add(getter);
                        setters.add(setter);
                        Object val = getter.invoke(item);
                        currentValues.add(val != null ? val : "");
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to read properties: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        if (properties.isEmpty()) {
            Label fallback = new Label("No editable properties found for this object.");
            fallback.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px;");
            grid.add(fallback, 0, 0, 2, 1);
        } else {
            int row = 0;
            for (int i = 0; i < properties.size(); i++) {
                String propName = properties.get(i).getName();
                Label label = new Label(propName);
                label.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
                TextField textField = new TextField(currentValues.get(i).toString());
                textField.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 8; -fx-background-radius: 4; -fx-font-size: 14px;");
                grid.add(label, 0, row);
                grid.add(textField, 1, row);
                textFields.add(textField);
                row++;
            }
        }

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 25; -fx-background-radius: 6; -fx-cursor: hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6a8aaa; -fx-border-color: #4a6a8a; -fx-border-radius: 6; -fx-padding: 8 25; -fx-cursor: hand;");
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);

        VBox root = new VBox(15, grid, buttonBox);
        root.setStyle("-fx-background-color: #1a1e26; -fx-padding: 20; -fx-background-radius: 12;");
        Scene scene = new Scene(root, 550, 400);
        scene.getStylesheets().add(DialogHelper.class.getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.setResizable(true);

        saveBtn.setOnAction(e -> {
            try {
                for (int i = 0; i < properties.size(); i++) {
                    String newVal = textFields.get(i).getText().trim();
                    Method setter = setters.get(i);
                    Class<?> paramType = setter.getParameterTypes()[0];
                    Object converted = convertValue(newVal, paramType);
                    setter.invoke(item, converted);
                }
                ObservableList<T> items = table.getItems();
                int index = items.indexOf(item);
                if (index >= 0) {
                    items.set(index, item);
                }
                table.refresh();
                showAlert("Success", action + " completed successfully.", Alert.AlertType.INFORMATION);
                dialog.close();
            } catch (Exception ex) {
                showAlert("Error", "Failed to save: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private static Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        if (targetType == LocalDate.class) return LocalDate.parse(value, DATE_FORMATTER);
        return value;
    }

    private static void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Applies the app's dark glass theme to any Alert's body/buttons so nothing
     * ever shows up as a default unstyled white/gray native dialog. Call this on
     * every Alert right after creating it and before showAndWait().
     */
    public static void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(DialogHelper.class.getResource("/styles.css").toExternalForm());
        pane.getStyleClass().add("app-alert");
    }
}