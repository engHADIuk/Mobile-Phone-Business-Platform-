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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Random;

public class PeriodOverPeriodController {

    private static Map<String, MonthlyData> monthlyData;
    private static String selectedMonthKey = null;
    private static TableView<MonthlyTransaction> detailTable;
    private static Label readOnlyLabel;
    private static GridPane detailCardsRef;
    private static Label currentRevenueLabelRef;
    private static Label previousRevenueLabelRef;
    private static Label growthLabelRef;
    private static Label transactionCountLabelRef;
    private static VBox detailsBoxRef;

    public static Scene createPeriodScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "PeriodAnalytics");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // HEADER
        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Period-over-Period Analytics");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Month-over-month growth visualizations and historical tracking.");
        subText.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        headerText.getChildren().addAll(welcomeLabel, subText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusLabel = new Label("● Live");
        statusLabel.getStyleClass().add("app-status-live");

        readOnlyLabel = new Label("READ-ONLY MODE");
        readOnlyLabel.getStyleClass().add("app-readonly-badge");
        readOnlyLabel.setVisible(SessionManager.getInstance().isEmployee());
        readOnlyLabel.setManaged(SessionManager.getInstance().isEmployee());
        headerBar.getChildren().addAll(headerText, spacer, statusLabel, readOnlyLabel);

        // ---- Content ----
        VBox contentWrapper = new VBox(20);
        contentWrapper.setStyle("-fx-padding: 25 30 30 30;");

        try {
            // fallback to dummy
            monthlyData = generateMonthlyData();
        } catch (Exception e) {
            e.printStackTrace();
            monthlyData = generateMonthlyData();
        }

        // Calendar Section
        VBox calendarBox = new VBox(10);
        calendarBox.getStyleClass().add("app-glass-panel");

        Label calendarTitle = new Label("Select Month");
        calendarTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Region calendarUnderline = new Region();
        calendarUnderline.setStyle("-fx-background-color: #2563EB; -fx-pref-height: 2; -fx-pref-width: 50;");
        VBox calendarTitleBox = new VBox(5);
        calendarTitleBox.getChildren().addAll(calendarTitle, calendarUnderline);

        GridPane monthGrid = new GridPane();
        monthGrid.setHgap(12);
        monthGrid.setVgap(12);
        monthGrid.setStyle("-fx-padding: 10 0 0 0;");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] keys = {"2025-01", "2025-02", "2025-03", "2025-04", "2025-05", "2025-06",
                         "2025-07", "2025-08", "2025-09", "2025-10", "2025-11", "2025-12"};

        for (int i = 0; i < 12; i++) {
            String monthKey = keys[i];
            MonthlyData data = monthlyData.get(monthKey);
            String monthName = months[i];
            boolean hasData = data != null;

            VBox monthCard = new VBox(5);
            monthCard.setAlignment(Pos.CENTER);
            monthCard.setStyle("-fx-background-color: " + (hasData ? "#0C0F16" : "#1a1e26") + "; -fx-background-radius: 8; -fx-padding: 10 15; -fx-pref-width: 80; -fx-pref-height: 70;");
            if (hasData) {
                monthCard.setStyle(monthCard.getStyle() + "; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
                final String key = monthKey;
                monthCard.setOnMouseClicked(e -> {
                    selectedMonthKey = key;
                    updateDetailView();
                });
            }

            Label monthLabel = new Label(monthName);
            monthLabel.setStyle("-fx-text-fill: " + (hasData ? "#ffffff" : "#4a6a8a") + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            if (hasData && data.growth != null) {
                String growthText = data.growth;
                String color = growthText.startsWith("-") ? "#EF4444" : (growthText.equals("N/A") ? "#6a8aaa" : "#10B981");
                Label growthLabel = new Label(growthText);
                growthLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
                monthCard.getChildren().addAll(monthLabel, growthLabel);
            } else {
                monthCard.getChildren().add(monthLabel);
            }
            monthGrid.add(monthCard, i % 3, i / 3);
        }

        calendarBox.getChildren().addAll(calendarTitleBox, monthGrid);

        // Details Section
        VBox detailsBox = new VBox(15);
        detailsBox.getStyleClass().add("app-glass-panel");

        Label detailsTitle = new Label("Month Details");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Region detailsUnderline = new Region();
        detailsUnderline.setStyle("-fx-background-color: #2563EB; -fx-pref-height: 2; -fx-pref-width: 50;");
        VBox detailsTitleBox = new VBox(5);
        detailsTitleBox.getChildren().addAll(detailsTitle, detailsUnderline);

        GridPane detailCards = new GridPane();
        detailCards.setHgap(20);
        detailCards.setVgap(10);
        detailCards.setStyle("-fx-padding: 10 0 5 0;");

        Label currentRevenueLabel = new Label("--");
        Label previousRevenueLabel = new Label("--");
        Label growthLabel = new Label("--");
        Label transactionCountLabel = new Label("--");

        VBox cardA = createSmallSummaryCard("Current Month Revenue", currentRevenueLabel);
        VBox cardB = createSmallSummaryCard("Previous Month Revenue", previousRevenueLabel);
        VBox cardC = createSmallSummaryCard("Growth", growthLabel);
        VBox cardD = createSmallSummaryCard("Transactions", transactionCountLabel);
        detailCards.add(cardA, 0, 0); detailCards.add(cardB, 1, 0);
        detailCards.add(cardC, 2, 0); detailCards.add(cardD, 3, 0);

        detailCardsRef = detailCards;
        currentRevenueLabelRef = currentRevenueLabel;
        previousRevenueLabelRef = previousRevenueLabel;
        growthLabelRef = growthLabel;
        transactionCountLabelRef = transactionCountLabel;
        detailsBoxRef = detailsBox;

        // Toolbar
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

        if (SessionManager.getInstance().isEmployee()) {
            editBtn.setDisable(true);
            modifyBtn.setDisable(true);
            deleteBtn.setDisable(true);
        }

        detailTable = createDetailTable();
        detailTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        detailTable.setFocusTraversable(true);

        editBtn.setOnAction(e -> DialogHelper.editSelected(detailTable, "Monthly Transaction"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(detailTable, "Monthly Transaction"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(detailTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(detailTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);

        VBox tableWrapper = new VBox(10);
        tableWrapper.setStyle("-fx-padding: 10 0 0 0;");
        Label tableLabel = new Label("Transactions");
        tableLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        tableWrapper.getChildren().addAll(tableLabel, toolbar, detailTable);

        detailsBox.getChildren().addAll(detailsTitleBox, detailCards, tableWrapper);

        contentWrapper.getChildren().addAll(calendarBox, detailsBox);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(PeriodOverPeriodController.class.getResource("/styles.css").toExternalForm());

        String latestKey = getLatestMonthKey();
        if (latestKey != null) {
            selectedMonthKey = latestKey;
            updateDetailView();
        }

        AnimationHelper.fadeSlideIn(calendarBox, 100);
        AnimationHelper.fadeSlideIn(detailsBox, 260);

        return scene;
    }

    private static void updateDetailView() {
        if (selectedMonthKey == null || monthlyData.get(selectedMonthKey) == null) return;
        MonthlyData data = monthlyData.get(selectedMonthKey);

        currentRevenueLabelRef.setText(String.format("$%,.2f", data.revenue));
        previousRevenueLabelRef.setText(String.format("$%,.2f", data.previousRevenue));
        transactionCountLabelRef.setText(String.valueOf(data.transactionCount));

        if (data.growth != null) {
            String color = data.growth.startsWith("-") ? "#EF4444" : (data.growth.equals("N/A") ? "#6a8aaa" : "#10B981");
            growthLabelRef.setText(data.growth);
            growthLabelRef.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        } else {
            growthLabelRef.setText("N/A");
            growthLabelRef.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        }

        detailTable.getItems().setAll(data.transactions);
    }

    private static String getLatestMonthKey() {
        String[] keys = {"2025-01", "2025-02", "2025-03", "2025-04", "2025-05", "2025-06",
                         "2025-07", "2025-08", "2025-09", "2025-10", "2025-11", "2025-12"};
        for (int i = keys.length - 1; i >= 0; i--) {
            if (monthlyData.containsKey(keys[i])) return keys[i];
        }
        return null;
    }

    private static VBox createSmallSummaryCard(String labelText, Label valueLabel) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #0C0F16; -fx-background-radius: 8; -fx-padding: 12 18; -fx-pref-width: 180;");
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");
        valueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        card.getChildren().addAll(label, valueLabel);
        return card;
    }

    // ---------- Dummy Data ----------
    private static Map<String, MonthlyData> generateMonthlyData() {
        Map<String, MonthlyData> data = new HashMap<>();
        Random rand = new Random();
        String[] months = {"2025-01", "2025-02", "2025-03", "2025-04", "2025-05", "2025-06",
                           "2025-07", "2025-08", "2025-09", "2025-10", "2025-11", "2025-12"};
        double previousRevenue = 0;
        for (int i = 0; i < months.length; i++) {
            String key = months[i];
            double revenue = Math.round((5000 + rand.nextDouble() * 40000) * 100.0) / 100.0;
            int txCount = 20 + rand.nextInt(60);
            List<MonthlyTransaction> transactions = new ArrayList<>();
            String[] products = {"iPhone 15 Pro", "MacBook Pro M3", "AirPods Pro 2", "iPad Air 5", "Apple Watch Ultra",
                                 "Mac Mini M2", "Apple TV 4K", "AirTag 4-Pack", "MagSafe Charger", "Apple Pencil 2"};
            for (int j = 0; j < txCount; j++) {
                String product = products[rand.nextInt(products.length)];
                double amount = Math.round((50 + rand.nextDouble() * 1950) * 100.0) / 100.0;
                String status = rand.nextBoolean() ? "Completed" : "Pending";
                transactions.add(new MonthlyTransaction("TXN-" + key + "-" + (100 + j), product, amount, status));
            }
            String growth = null;
            if (i == 0) {
                growth = "N/A";
            } else {
                double prev = previousRevenue;
                if (prev > 0) {
                    double pct = ((revenue - prev) / prev) * 100;
                    growth = String.format("%+.2f%%", pct);
                } else {
                    growth = "N/A";
                }
            }
            MonthlyData md = new MonthlyData(revenue, previousRevenue, growth, txCount, transactions);
            data.put(key, md);
            previousRevenue = revenue;
        }
        return data;
    }

    // ---------- Table Creation ----------
    private static TableView<MonthlyTransaction> createDetailTable() {
        TableView<MonthlyTransaction> table = new TableView<>();
        table.setPrefHeight(180);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<MonthlyTransaction>() {
            @Override
            protected void updateItem(MonthlyTransaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) return;
                getStyleClass().removeAll("even-row", "odd-row");
                getStyleClass().add(getIndex() % 2 == 0 ? "even-row" : "odd-row");
            }
        });

        TableColumn<MonthlyTransaction, String> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(150);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<MonthlyTransaction, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        productCol.setPrefWidth(200);
        productCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<MonthlyTransaction, Double> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(150);
        amountCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<MonthlyTransaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, productCol, amountCol, statusCol);
        return table;
    }

    // ---------- Inner classes ----------
    public static class MonthlyData {
        public final double revenue;
        public final double previousRevenue;
        public final String growth;
        public final int transactionCount;
        public final List<MonthlyTransaction> transactions;

        public MonthlyData(double revenue, double previousRevenue, String growth, int transactionCount, List<MonthlyTransaction> transactions) {
            this.revenue = revenue;
            this.previousRevenue = previousRevenue;
            this.growth = growth;
            this.transactionCount = transactionCount;
            this.transactions = transactions;
        }
    }

    public static class MonthlyTransaction {
        private String id;
        private String product;
        private double amount;
        private String status;

        public MonthlyTransaction(String id, String product, double amount, String status) {
            this.id = id;
            this.product = product;
            this.amount = amount;
            this.status = status;
        }

        public String getId() { return id; }
        public String getProduct() { return product; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }

        public void setId(String id) { this.id = id; }
        public void setProduct(String product) { this.product = product; }
        public void setAmount(double amount) { this.amount = Math.round(amount * 100.0) / 100.0; }
        public void setStatus(String status) { this.status = status; }
    }
}