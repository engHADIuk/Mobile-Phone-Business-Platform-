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
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FilterController {

    private static TableView<Transaction> resultTable;
    private static Label errorLabelRef;
    private static DateRangePicker dateRangePicker;
    private static TextField clientIdFieldRef;
    private static ComboBox<String> categoryComboRef;
    private static Label readOnlyLabel;

    public static Scene createFilterScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "FilterData");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // HEADER (unchanged)
        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Parameterized Query Filtering");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Deep-dive analysis via user-defined filters.");
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

        // ---- Content ----
        VBox contentWrapper = new VBox(20);
        contentWrapper.setStyle("-fx-padding: 25 30 30 30;");

        // Filter Panel (unchanged – keep your existing UI)
        VBox filterPanel = new VBox(15);
        filterPanel.getStyleClass().add("app-glass-panel");
        Label filterTitle = new Label("Filter Criteria");
        filterTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Region filterUnderline = new Region();
        filterUnderline.setStyle("-fx-background-color: #2563EB; -fx-pref-height: 2; -fx-pref-width: 50;");
        VBox filterTitleBox = new VBox(5);
        filterTitleBox.getChildren().addAll(filterTitle, filterUnderline);

        GridPane filterGrid = new GridPane();
        filterGrid.setHgap(20);
        filterGrid.setVgap(12);
        filterGrid.setStyle("-fx-padding: 10 0 0 0;");

        dateRangePicker = new DateRangePicker();
        TextField dateRangeField = new TextField("Select Date Range");
        dateRangeField.setEditable(false);
        dateRangeField.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 8; -fx-background-radius: 4; -fx-font-family: 'Segoe UI'; -fx-cursor: hand;");
        dateRangeField.setOnMouseClicked(e -> {
            dateRangePicker.showPicker(dateRangeField.localToScreen(0, 0).getX(),
                                       dateRangeField.localToScreen(0, 0).getY() + dateRangeField.getHeight(),
                                       dateRangeField.getScene().getWindow());
        });
        dateRangePicker.setOnDateRangeSelected(() -> {
            if (dateRangePicker.getStartDate() != null && dateRangePicker.getEndDate() != null) {
                dateRangeField.setText(dateRangePicker.getStartDate() + " → " + dateRangePicker.getEndDate());
            } else if (dateRangePicker.getStartDate() != null) {
                dateRangeField.setText(dateRangePicker.getStartDate().toString());
            } else {
                dateRangeField.setText("Select Date Range");
            }
            applyFiltersFromUI();
        });

        TextField clientIdField = new TextField();
        clientIdField.setPromptText("Enter Client ID");
        clientIdField.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 8; -fx-background-radius: 4; -fx-font-family: 'Segoe UI';");
        clientIdFieldRef = clientIdField;

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("All", "Electronics", "Software", "Accessories", "Services");
        categoryCombo.setValue("All");
        categoryCombo.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 8; -fx-background-radius: 4; -fx-font-family: 'Segoe UI';");
        categoryComboRef = categoryCombo;

        Label dateRangeLabel = new Label("Date Range");
        dateRangeLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        Label clientLabel = new Label("Client ID");
        clientLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        Label categoryLabel = new Label("Product Category");
        categoryLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");

        filterGrid.add(dateRangeLabel, 0, 0);
        filterGrid.add(dateRangeField, 0, 1);
        filterGrid.add(clientLabel, 1, 0);
        filterGrid.add(clientIdField, 1, 1);
        filterGrid.add(categoryLabel, 2, 0);
        filterGrid.add(categoryCombo, 2, 1);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");
        Button applyBtn = new Button("Apply Filters");
        applyBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-font-family: 'Segoe UI';");
        applyBtn.setOnAction(e -> applyFiltersFromUI());

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6a8aaa; -fx-border-color: #4a6a8a; -fx-border-radius: 6; -fx-padding: 8 20; -fx-font-family: 'Segoe UI';");
        resetBtn.setOnAction(e -> resetFilters());

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        errorLabelRef = errorLabel;

        buttonBox.getChildren().addAll(applyBtn, resetBtn, errorLabel);
        filterPanel.getChildren().addAll(filterTitleBox, filterGrid, buttonBox);

        // ---- Results Table ----
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Filtered Transactions");
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

        resultTable = createResultTable();
        resultTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        resultTable.setFocusTraversable(true);

        editBtn.setOnAction(e -> DialogHelper.editSelected(resultTable, "Filtered Transaction"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(resultTable, "Filtered Transaction"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(resultTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(resultTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, resultTable);

        contentWrapper.getChildren().addAll(filterPanel, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(FilterController.class.getResource("/styles.css").toExternalForm());

        applyPermissions(editBtn, modifyBtn, deleteBtn, printBtn, applyBtn, resetBtn);
        applyFiltersFromUI();
        AnimationHelper.fadeSlideIn(filterPanel, 100);
        AnimationHelper.fadeSlideIn(tableContainer, 260);

        return scene;
    }

    // ---------- Permissions ----------
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

    // ---------- Filter Logic (unchanged) ----------
    private static void applyFiltersFromUI() {
        if (dateRangePicker == null) return;

        LocalDate startDate = dateRangePicker.getStartDate();
        LocalDate endDate = dateRangePicker.getEndDate();
        String startStr = (startDate != null) ? startDate.toString() : null;
        String endStr = (endDate != null) ? endDate.toString() : null;

        String clientText = (clientIdFieldRef != null) ? clientIdFieldRef.getText().trim() : "";
        String category = (categoryComboRef != null) ? categoryComboRef.getValue() : "All";

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            showError("Start date must be before or equal to end date.");
            return;
        }
        showError("");

        try {
            List<Transaction> filtered = DatabaseManager.getInstance().loadFilteredTransactions(
                startStr, endStr, clientText, category
            );
            resultTable.getItems().setAll(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
            resultTable.getItems().setAll(generateDummyTransactions());
        }
    }

    private static void resetFilters() {
        dateRangePicker.setStartDate(null);
        dateRangePicker.setEndDate(null);
        if (clientIdFieldRef != null) clientIdFieldRef.clear();
        if (categoryComboRef != null) categoryComboRef.setValue("All");
        showError("");
        applyFiltersFromUI();
    }

    private static void showError(String message) {
        if (errorLabelRef != null) errorLabelRef.setText(message);
    }

    // ---------- Table Creation ----------
    private static TableView<Transaction> createResultTable() {
        TableView<Transaction> table = new TableView<>();
        table.setPrefHeight(350);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: #1a1e26;" : "-fx-background-color: #0C0F16;");
            }
        });

        TableColumn<Transaction, String> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<Transaction, String> clientCol = new TableColumn<>("Client ID");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        clientCol.setPrefWidth(120);
        clientCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<Transaction, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        productCol.setPrefWidth(180);
        productCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        categoryCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateString"));
        dateCol.setPrefWidth(120);
        dateCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(120);
        amountCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, clientCol, productCol, categoryCol, dateCol, amountCol, statusCol);
        return table;
    }

    // ---------- Dummy Data ----------
    private static List<Transaction> generateDummyTransactions() {
        List<Transaction> list = new ArrayList<>();
        Random rand = new Random();
        String[] products = {"iPhone 15 Pro", "MacBook Pro M3", "AirPods Pro 2", "iPad Air 5", "Apple Watch Ultra",
                             "Mac Mini M2", "Apple TV 4K", "AirTag 4-Pack", "MagSafe Charger", "Apple Pencil 2"};
        String[] categories = {"Electronics", "Software", "Accessories", "Services"};
        String[] statuses = {"Completed", "Pending", "Refunded", "Failed"};
        String[] clients = {"C001", "C002", "C003", "C004", "C005", "C006", "C007", "C008", "C009", "C010"};
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        for (int i = 0; i < 50; i++) {
            String id = "TXN-" + (1000 + i);
            String client = clients[rand.nextInt(clients.length)];
            String product = products[rand.nextInt(products.length)];
            String category = categories[rand.nextInt(categories.length)];
            LocalDate date = startDate.plusDays(rand.nextInt(50));
            double amount = Math.round((50 + rand.nextDouble() * 950) * 100.0) / 100.0;
            String status = statuses[rand.nextInt(statuses.length)];
            list.add(new Transaction(id, client, product, category, date, amount, status));
        }
        return list;
    }

    // ---------- Inner class with getters/setters ----------
    public static class Transaction {
        private String id;
        private String clientId;
        private String product;
        private String category;
        private LocalDate date;
        private double amount;
        private String status;

        public Transaction(String id, String clientId, String product, String category, LocalDate date, double amount, String status) {
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
        public LocalDate getDate() { return date; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
        public String getDateString() { return date.format(DateTimeFormatter.ISO_LOCAL_DATE); }

        public void setId(String id) { this.id = id; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public void setProduct(String product) { this.product = product; }
        public void setCategory(String category) { this.category = category; }
        public void setDate(LocalDate date) { this.date = date; }
        public void setAmount(double amount) { this.amount = Math.round(amount * 100.0) / 100.0; }
        public void setStatus(String status) { this.status = status; }
    }
}