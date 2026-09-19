package com.ecommerce.finance;

import java.sql.SQLException;
import com.ecommerce.finance.services.DatabaseManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExportController {

    private static List<ExportTransaction> exportData;
    private static Stage primaryStage;
    private static TableView<ExportTransaction> previewTable;
    private static Label readOnlyLabel;

    public static Scene createExportScene(Stage stage) {
        primaryStage = stage;
        VBox sidebar = SidebarHelper.buildSidebar(stage, "ExportData");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Automated Export Daemon");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Export current data view as CSV or PDF.");
        subText.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        headerText.getChildren().addAll(welcomeLabel, subText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusLabel = new Label("● Live");
        statusLabel.getStyleClass().add("app-status-live");

        readOnlyLabel = new Label("READ-ONLY MODE");
        readOnlyLabel.getStyleClass().add("app-readonly-badge");
        readOnlyLabel.setVisible(false);
        readOnlyLabel.setManaged(false);
        headerBar.getChildren().addAll(headerText, spacer, statusLabel, readOnlyLabel);

        VBox contentWrapper = new VBox(20);
        contentWrapper.setStyle("-fx-padding: 25 30 30 30;");

        try {
            exportData = DatabaseManager.getInstance().loadExportTransactions();
        } catch (SQLException e) {
            e.printStackTrace();
            exportData = generateDummyExportData();
        }

        // Export Panel (reduced for brevity – keep your existing UI)
        VBox exportPanel = new VBox(15);
        exportPanel.getStyleClass().add("app-glass-panel");
        Label panelTitle = new Label("Export Options");
        panelTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Region panelUnderline = new Region();
        panelUnderline.setStyle("-fx-background-color: #2563EB; -fx-pref-height: 2; -fx-pref-width: 50;");
        VBox panelTitleBox = new VBox(5);
        panelTitleBox.getChildren().addAll(panelTitle, panelUnderline);

        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(30);
        optionsGrid.setVgap(15);
        optionsGrid.setStyle("-fx-padding: 15 0 0 0;");

        Label formatLabel = new Label("Export Format");
        formatLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton csvRadio = new RadioButton("CSV");
        csvRadio.setToggleGroup(formatGroup);
        csvRadio.setSelected(true);
        csvRadio.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        RadioButton pdfRadio = new RadioButton("PDF");
        pdfRadio.setToggleGroup(formatGroup);
        pdfRadio.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        HBox formatBox = new HBox(20, csvRadio, pdfRadio);

        Label nameLabel = new Label("File Name");
        nameLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        TextField fileNameField = new TextField("export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        fileNameField.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 8; -fx-background-radius: 4; -fx-pref-width: 300; -fx-font-family: 'Segoe UI';");

        Label locationLabel = new Label("Save Location");
        locationLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        TextField locationField = new TextField(System.getProperty("user.home") + "/Desktop");
        locationField.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 8; -fx-background-radius: 4; -fx-pref-width: 300; -fx-font-family: 'Segoe UI';");
        locationField.setEditable(false);

        Button browseBtn = new Button("Browse");
        browseBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15; -fx-background-radius: 4; -fx-font-family: 'Segoe UI';");
        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Export Folder");
            chooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            File selected = chooser.showSaveDialog(primaryStage);
            if (selected != null) {
                locationField.setText(selected.getParent());
                fileNameField.setText(selected.getName().replaceFirst("\\.[^.]+$", ""));
            }
        });

        Label statusMessage = new Label();
        statusMessage.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        statusMessage.setVisible(false);

        optionsGrid.add(formatLabel, 0, 0);
        optionsGrid.add(formatBox, 1, 0);
        optionsGrid.add(nameLabel, 0, 1);
        optionsGrid.add(fileNameField, 1, 1);
        optionsGrid.add(locationLabel, 0, 2);
        optionsGrid.add(locationField, 1, 2);
        optionsGrid.add(browseBtn, 2, 2);
        optionsGrid.add(statusMessage, 0, 3, 2, 1);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setStyle("-fx-padding: 15 0 0 0;");
        Button exportBtnAction = new Button("Export Now");
        exportBtnAction.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 6; -fx-font-family: 'Segoe UI';");
        exportBtnAction.setOnAction(e -> {
            String format = csvRadio.isSelected() ? "CSV" : "PDF";
            String fileName = fileNameField.getText().trim();
            String location = locationField.getText().trim();
            if (fileName.isEmpty()) {
                if (format.equals("PDF") && !SessionManager.getInstance().isAdmin()) {
                    showStatus(statusMessage, "PDF export is only available for administrators.", "#EF4444");
                    return;
                }
                showStatus(statusMessage, "Please enter a file name.", "#EF4444");
                return;
            }
            if (location.isEmpty()) {
                showStatus(statusMessage, "Please select a save location.", "#EF4444");
                return;
            }
            String fullPath = location + File.separator + fileName + (format.equals("CSV") ? ".csv" : ".pdf");
            try {
                boolean success = exportData(fullPath, format);
                if (success) {
                    showStatus(statusMessage, "Export successful! File saved to: " + fullPath, "#10B981");
                }
            } catch (IOException ex) {
                showStatus(statusMessage, "Error: " + ex.getMessage(), "#EF4444");
            }
        });
        Button resetBtn = new Button("Reset");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6a8aaa; -fx-border-color: #4a6a8a; -fx-border-radius: 6; -fx-padding: 8 20; -fx-font-family: 'Segoe UI';");
        resetBtn.setOnAction(e -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileNameField.setText("export_" + timestamp);
            locationField.setText(System.getProperty("user.home") + "/Desktop");
            statusMessage.setVisible(false);
        });
        buttonBox.getChildren().addAll(exportBtnAction, resetBtn);
        exportPanel.getChildren().addAll(panelTitleBox, optionsGrid, buttonBox);

        // Preview Table
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Data Preview (Exportable Records)");
        tableTitle.getStyleClass().add("app-section-title");
        Region tableUnderline = new Region();
        tableUnderline.getStyleClass().add("app-underline");
        VBox tableTitleBox = new VBox(5);
        tableTitleBox.getChildren().addAll(tableTitle, tableUnderline);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-padding: 10 0 5 0;");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button");
        Button modifyBtn = new Button("Modify");
        modifyBtn.getStyleClass().add("action-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-danger");
        Button printBtn = new Button("Print");
        printBtn.getStyleClass().add("action-button");

        previewTable = createPreviewTable();
        previewTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        previewTable.setFocusTraversable(true);
        previewTable.getItems().setAll(exportData);

        editBtn.setOnAction(e -> DialogHelper.editSelected(previewTable, "Export Record"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(previewTable, "Export Record"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(previewTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(previewTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, previewTable);

        contentWrapper.getChildren().addAll(exportPanel, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(ExportController.class.getResource("/styles.css").toExternalForm());

        applyPermissions(editBtn, modifyBtn, deleteBtn, printBtn, exportBtnAction, resetBtn, browseBtn);
        AnimationHelper.fadeSlideIn(exportPanel, 100);
        AnimationHelper.fadeSlideIn(tableContainer, 260);

        return scene;
    }

    private static void applyPermissions(Button... buttons) {
        UserSession session = UserSession.getInstance();
        boolean isEmployee = session.isEmployee();
        if (readOnlyLabel != null) {
            readOnlyLabel.setVisible(isEmployee);
            readOnlyLabel.setManaged(isEmployee);
        }
        for (int i = 0; i < 3 && i < buttons.length; i++) {
            buttons[i].setDisable(isEmployee);
        }
        if (buttons.length > 3) buttons[3].setDisable(false);
    }

    private static void showStatus(Label statusLabel, String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        statusLabel.setVisible(true);
    }

    private static boolean exportData(String filePath, String format) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file, true)) {
                // file is writable
            } catch (IOException e) {
                throw new IOException("File is locked or in use by another program.");
            }
        }
        if (format.equals("CSV")) {
            exportCSV(filePath);
        } else {
            exportPDF(filePath);
        }
        return true;
    }

    private static void exportCSV(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Transaction ID,Client ID,Product,Category,Date,Amount,Status");
            for (ExportTransaction t : exportData) {
                writer.printf("%s,%s,%s,%s,%s,%.2f,%s%n", t.getId(), t.getClientId(), t.getProduct(), t.getCategory(), t.getDate(), t.getAmount(), t.getStatus());
            }
        } catch (IOException e) {
            throw new IOException("Could not write CSV file: " + e.getMessage());
        }
    }

    private static void exportPDF(String filePath) throws IOException {
        throw new IOException("PDF export not implemented in this demo. Use CSV instead.");
    }

    private static TableView<ExportTransaction> createPreviewTable() {
        TableView<ExportTransaction> table = new TableView<>();
        table.setPrefHeight(200);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<ExportTransaction>() {
            @Override
            protected void updateItem(ExportTransaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: #1a1e26;" : "-fx-background-color: #0C0F16;");
            }
        });

        TableColumn<ExportTransaction, String> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ExportTransaction, String> clientCol = new TableColumn<>("Client ID");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        clientCol.setPrefWidth(100);
        clientCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ExportTransaction, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        productCol.setPrefWidth(150);
        productCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ExportTransaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);
        categoryCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ExportTransaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);
        dateCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ExportTransaction, Double> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ExportTransaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, clientCol, productCol, categoryCol, dateCol, amountCol, statusCol);
        return table;
    }

    private static List<ExportTransaction> generateDummyExportData() {
        List<ExportTransaction> list = new ArrayList<>();
        Random rand = new Random();
        String[] products = {"iPhone 15 Pro", "MacBook Pro M3", "AirPods Pro 2", "iPad Air 5", "Apple Watch Ultra",
                             "Mac Mini M2", "Apple TV 4K", "AirTag 4-Pack", "MagSafe Charger", "Apple Pencil 2"};
        String[] categories = {"Electronics", "Software", "Accessories", "Services"};
        String[] statuses = {"Completed", "Pending", "Refunded", "Failed"};
        String[] clients = {"C001", "C002", "C003", "C004", "C005", "C006", "C007", "C008", "C009", "C010"};

        LocalDateTime startDate = LocalDateTime.of(2025, 6, 1, 0, 0);
        for (int i = 0; i < 30; i++) {
            String id = "TXN-" + (1000 + i);
            String client = clients[rand.nextInt(clients.length)];
            String product = products[rand.nextInt(products.length)];
            String category = categories[rand.nextInt(categories.length)];
            String date = startDate.plusDays(rand.nextInt(50)).format(DateTimeFormatter.ISO_LOCAL_DATE);
            double amount = Math.round((50 + rand.nextDouble() * 950) * 100.0) / 100.0;
            String status = statuses[rand.nextInt(statuses.length)];
            list.add(new ExportTransaction(id, client, product, category, date, amount, status));
        }
        return list;
    }

    public static class ExportTransaction {
        private String id;
        private String clientId;
        private String product;
        private String category;
        private String date;
        private double amount;
        private String status;

        public ExportTransaction(String id, String clientId, String product, String category, String date, double amount, String status) {
            this.id = id;
            this.clientId = clientId;
            this.product = product;
            this.category = category;
            this.date = date;
            this.amount = amount;
            this.status = status;
        }

        public String getId() { return id; }
        public String getClientId() { return clientId; }
        public String getProduct() { return product; }
        public String getCategory() { return category; }
        public String getDate() { return date; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }

        public void setId(String id) { this.id = id; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public void setProduct(String product) { this.product = product; }
        public void setCategory(String category) { this.category = category; }
        public void setDate(String date) { this.date = date; }
        public void setAmount(double amount) { this.amount = Math.round(amount * 100.0) / 100.0; }
        public void setStatus(String status) { this.status = status; }
    }
}