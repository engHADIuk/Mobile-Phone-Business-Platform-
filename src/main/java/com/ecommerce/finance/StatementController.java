package com.ecommerce.finance;

import com.ecommerce.finance.services.DatabaseManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Lets any logged-in user (admin or employee) generate a real, database-backed
 * account statement for a chosen date range, with CSV export and printing.
 * Admins can also Edit/Modify/Delete individual statement rows; employees get
 * a read-only view (Print stays available to everyone).
 */
public class StatementController {

    private static TableView<StatementTransaction> statementTable;
    private static DateRangePicker dateRangePicker;
    private static Label readOnlyLabel;

    public static Scene createStatementScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "Statements");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // ---------- HEADER ----------
        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Account Statement Generator");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI';");
        Label subText = new Label("Generate a real transaction statement for any date range, straight from the live database.");
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

        // ---------- DATE RANGE PANEL ----------
        VBox rangePanel = new VBox(15);
        rangePanel.getStyleClass().add("app-glass-panel");

        Label rangeTitle = new Label("Statement Period");
        rangeTitle.getStyleClass().add("app-section-title");
        Region rangeUnderline = new Region();
        rangeUnderline.getStyleClass().add("app-underline");
        VBox rangeTitleBox = new VBox(5, rangeTitle, rangeUnderline);

        HBox rangeControls = new HBox(15);
        rangeControls.setAlignment(Pos.CENTER_LEFT);
        rangeControls.setStyle("-fx-padding: 10 0 0 0;");

        dateRangePicker = new DateRangePicker();
        TextField dateRangeField = new TextField("Select Date Range");
        dateRangeField.setEditable(false);
        dateRangeField.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 10; -fx-background-radius: 6; -fx-font-family: 'Segoe UI'; -fx-cursor: hand; -fx-pref-width: 260;");
        dateRangeField.setOnMouseClicked(e -> dateRangePicker.showPicker(
                dateRangeField.localToScreen(0, 0).getX(),
                dateRangeField.localToScreen(0, 0).getY() + dateRangeField.getHeight(),
                dateRangeField.getScene().getWindow()));

        Button generateBtn = new Button("Generate Statement");
        generateBtn.getStyleClass().add("app-primary-btn");
        Button exportBtn = new Button("Export CSV");
        exportBtn.getStyleClass().add("app-ghost-btn");

        rangeControls.getChildren().addAll(dateRangeField, generateBtn, exportBtn);

        Label statusMessage = new Label();
        statusMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        statusMessage.setVisible(false);

        dateRangePicker.setOnDateRangeSelected(() -> {
            if (dateRangePicker.getStartDate() != null && dateRangePicker.getEndDate() != null) {
                dateRangeField.setText(dateRangePicker.getStartDate() + " → " + dateRangePicker.getEndDate());
            } else if (dateRangePicker.getStartDate() != null) {
                dateRangeField.setText(dateRangePicker.getStartDate().toString());
            } else {
                dateRangeField.setText("Select Date Range");
            }
        });

        rangePanel.getChildren().addAll(rangeTitleBox, rangeControls, statusMessage);

        // ---------- SUMMARY CARDS ----------
        Label totalTxLabel = new Label("0");
        Label grossLabel = new Label("$0.00");
        Label refundedLabel = new Label("$0.00");
        Label netLabel = new Label("$0.00");

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(15);
        summaryGrid.add(createSummaryCard("Total Transactions", totalTxLabel, "$"), 0, 0);
        summaryGrid.add(createSummaryCard("Gross Revenue", grossLabel, "$"), 1, 0);
        summaryGrid.add(createSummaryCard("Refunded", refundedLabel, "$"), 2, 0);
        summaryGrid.add(createSummaryCard("Net Amount", netLabel, "$"), 3, 0);

        // ---------- TABLE ----------
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Statement Detail");
        tableTitle.getStyleClass().add("app-section-title");
        Label tableSub = new Label("Select a period above and click Generate Statement");
        tableSub.getStyleClass().add("app-section-sub");
        Region tableUnderline = new Region();
        tableUnderline.getStyleClass().add("app-underline");
        VBox tableTitleBox = new VBox(5, tableTitle, tableSub, tableUnderline);

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
        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);

        statementTable = createStatementTable();
        statementTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        statementTable.setFocusTraversable(true);

        editBtn.setOnAction(e -> DialogHelper.editSelected(statementTable, "Statement Transaction"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(statementTable, "Statement Transaction"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(statementTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(statementTable));

        tableContainer.getChildren().addAll(tableTitleBox, toolbar, statementTable);

        contentWrapper.getChildren().addAll(rangePanel, summaryGrid, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(StatementController.class.getResource("/styles.css").toExternalForm());

        // ---------- ACTIONS ----------
        generateBtn.setOnAction(e -> {
            LocalDate start = dateRangePicker.getStartDate();
            LocalDate end = dateRangePicker.getEndDate();
            if (start == null || end == null) {
                showStatus(statusMessage, "Please select a start and end date first.");
                return;
            }
            if (start.isAfter(end)) {
                showStatus(statusMessage, "Start date must be before or equal to the end date.");
                return;
            }
            statusMessage.setVisible(false);

            try {
                List<StatementTransaction> results = DatabaseManager.getInstance()
                        .loadStatementTransactions(start.toString(), end.toString());
                statementTable.getItems().setAll(results);

                double gross = results.stream()
                        .filter(t -> !t.getStatus().equalsIgnoreCase("Refunded"))
                        .mapToDouble(StatementTransaction::getAmount).sum();
                double refunded = results.stream()
                        .filter(t -> t.getStatus().equalsIgnoreCase("Refunded"))
                        .mapToDouble(StatementTransaction::getAmount).sum();

                totalTxLabel.setText(String.valueOf(results.size()));
                grossLabel.setText(String.format("$%,.2f", gross));
                refundedLabel.setText(String.format("$%,.2f", refunded));
                netLabel.setText(String.format("$%,.2f", gross - refunded));

                tableSub.setText(results.isEmpty()
                        ? "No transactions found in this period."
                        : results.size() + " transaction(s) from " + start + " to " + end);
            } catch (SQLException ex) {
                ex.printStackTrace();
                showStatus(statusMessage, "Database error: " + ex.getMessage());
            }
        });

        exportBtn.setOnAction(e -> {
            List<StatementTransaction> items = statementTable.getItems();
            if (items.isEmpty()) {
                showStatus(statusMessage, "Generate a statement before exporting.");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Statement CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("statement_" + LocalDate.now() + ".csv");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write("Order ID,Date,Customer,Product,Quantity,Amount,Status");
                    writer.newLine();
                    for (StatementTransaction t : items) {
                        writer.write(String.format("%s,%s,%s,%s,%d,%.2f,%s",
                                t.getOrderId(), t.getDate(), t.getCustomerId(), t.getProduct(),
                                t.getQuantity(), t.getAmount(), t.getStatus()));
                        writer.newLine();
                    }
                } catch (IOException ex) {
                    showStatus(statusMessage, "Error writing CSV: " + ex.getMessage());
                }
            }
        });

        // ---------- ANIMATIONS ----------
        AnimationHelper.fadeSlideIn(rangePanel, 0);
        AnimationHelper.fadeSlideIn(summaryGrid, 120);
        AnimationHelper.fadeSlideIn(tableContainer, 240);

        applyPermissions(editBtn, modifyBtn, deleteBtn, printBtn);

        return scene;
    }

    /** Admin gets full Edit/Modify/Delete; employees are read-only. Print always stays enabled. */
    private static void applyPermissions(Button editBtn, Button modifyBtn, Button deleteBtn, Button printBtn) {
        boolean isEmployee = SessionManager.getInstance().isEmployee();
        if (readOnlyLabel != null) {
            readOnlyLabel.setVisible(isEmployee);
            readOnlyLabel.setManaged(isEmployee);
        }
        editBtn.setDisable(isEmployee);
        modifyBtn.setDisable(isEmployee);
        deleteBtn.setDisable(isEmployee);
        printBtn.setDisable(false);
    }

    private static void showStatus(Label statusLabel, String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }

    private static VBox createSummaryCard(String label, Label valueLabel, String icon) {
        VBox card = new VBox(8);
        card.getStyleClass().add("app-kpi-glass");
        card.setPrefWidth(220);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #8aa3c4; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        valueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI';");

        card.getChildren().addAll(labelText, valueLabel);
        return card;
    }

    private static TableView<StatementTransaction> createStatementTable() {
        TableView<StatementTransaction> table = new TableView<>();
        table.setPrefHeight(320);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StatementTransaction, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        idCol.setPrefWidth(110);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<StatementTransaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);
        dateCol.setStyle("-fx-text-fill: #6a8aaa; -fx-font-family: 'Segoe UI';");

        TableColumn<StatementTransaction, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerCol.setPrefWidth(100);
        customerCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<StatementTransaction, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        productCol.setPrefWidth(180);
        productCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<StatementTransaction, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(60);
        qtyCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<StatementTransaction, Double> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(120);
        amountCol.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        TableColumn<StatementTransaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<StatementTransaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Delivered": setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;"); break;
                    case "Refunded": setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;"); break;
                    case "Pending":
                    case "Processing": setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;"); break;
                    default: setStyle("-fx-text-fill: #6a8aaa;");
                }
            }
        });

        table.getColumns().addAll(idCol, dateCol, customerCol, productCol, qtyCol, amountCol, statusCol);

        table.setRowFactory(tv -> new TableRow<StatementTransaction>() {
            @Override
            protected void updateItem(StatementTransaction item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("even-row", "odd-row");
                if (item == null || empty) return;
                getStyleClass().add(getIndex() % 2 == 0 ? "even-row" : "odd-row");
            }
        });

        return table;
    }

    public static class StatementTransaction {
        private String orderId;
        private String date;
        private String customerId;
        private String product;
        private int quantity;
        private double amount;
        private String status;

        public StatementTransaction(String orderId, String date, String customerId, String product,
                                     int quantity, double amount, String status) {
            this.orderId = orderId;
            this.date = date;
            this.customerId = customerId;
            this.product = product;
            this.quantity = quantity;
            this.amount = Math.round(amount * 100.0) / 100.0;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public String getDate() { return date; }
        public String getCustomerId() { return customerId; }
        public String getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }

        public void setOrderId(String orderId) { this.orderId = orderId; }
        public void setDate(String date) { this.date = date; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public void setProduct(String product) { this.product = product; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setAmount(double amount) { this.amount = Math.round(amount * 100.0) / 100.0; }
        public void setStatus(String status) { this.status = status; }
    }
}
