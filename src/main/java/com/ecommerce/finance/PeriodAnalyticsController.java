package com.ecommerce.finance;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

// CRITICAL: Add these imports for JavaFX properties
import javafx.beans.property.*;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class PeriodAnalyticsController implements Initializable {

    @FXML private ToggleGroup monthToggleGroup;
    @FXML private Label currentRevenueLabel;
    @FXML private Label previousRevenueLabel;
    @FXML private Label growthLabel;
    @FXML private Label growthArrow;
    @FXML private Label transactionsCountLabel;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> idColumn;
    @FXML private TableColumn<Transaction, String> productColumn;
    @FXML private TableColumn<Transaction, Number> amountColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Label readOnlyLabel;

    @FXML private Button editButton;
    @FXML private Button modifyButton;
    @FXML private Button printButton;

    private Map<Integer, MonthData> monthDataMap = new HashMap<>();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generateSampleData();

        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        productColumn.setCellValueFactory(cellData -> cellData.getValue().productProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty().map(date -> date.format(dateFormatter)));
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        statusColumn.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String styleClass = "status-" + item.toLowerCase();
                    getStyleClass().clear();
                    getStyleClass().add(styleClass);
                }
            }
        });

        transactionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        transactionsTable.setFocusTraversable(true);

        buildChart();

        int currentMonth = LocalDate.now().getMonthValue() - 1;
        Toggle selectedToggle = monthToggleGroup.getToggles().get(currentMonth);
        if (selectedToggle != null) selectedToggle.setSelected(true);

        monthToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int monthIndex = monthToggleGroup.getToggles().indexOf(newVal);
                updateMonthData(monthIndex + 1);
            }
        });

        updateMonthData(currentMonth + 1);

        editButton.setOnAction(e -> DialogHelper.editSelected(transactionsTable, "Transaction"));
        modifyButton.setOnAction(e -> DialogHelper.modifySelected(transactionsTable, "Transaction"));
        deleteButton.setOnAction(e -> DialogHelper.deleteSelected(transactionsTable));
        printButton.setOnAction(e -> DialogHelper.printTable(transactionsTable));

        applyPermissions();
    }

    private void generateSampleData() {
        Random rand = new Random(42);
        String[] products = {"Laptop Pro", "Wireless Mouse", "USB-C Hub", "Monitor 27\"", "Keyboard Deluxe",
                "External SSD", "Webcam HD", "Headphones ANC", "Docking Station", "Cable Kit"};
        String[] statuses = {"Completed", "Pending", "Failed"};

        for (int month = 1; month <= 12; month++) {
            double baseRevenue = 20000 + rand.nextDouble() * 30000;
            if (month == 11 || month == 12) baseRevenue *= 1.4;
            if (month == 1) baseRevenue *= 0.8;
            double revenue = Math.round(baseRevenue * 100.0) / 100.0;

            int transactionCount = 10 + rand.nextInt(30);
            List<Transaction> transactions = new ArrayList<>();
            for (int i = 0; i < transactionCount; i++) {
                String id = "TXN-" + String.format("%04d", (month * 100 + i + 1));
                String product = products[rand.nextInt(products.length)];
                double amount = 50 + rand.nextDouble() * 450;
                amount = Math.round(amount * 100.0) / 100.0;
                LocalDate date = LocalDate.of(2026, month, 1 + rand.nextInt(28));
                String status = statuses[rand.nextInt(statuses.length)];
                transactions.add(new Transaction(id, product, amount, date, status));
            }
            transactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            monthDataMap.put(month, new MonthData(month, revenue, transactions));
        }
    }

    private void buildChart() {
        CategoryAxis xAxis = (CategoryAxis) revenueChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) revenueChart.getYAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue ($)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        for (int month = 1; month <= 12; month++) {
            MonthData data = monthDataMap.get(month);
            series.getData().add(new XYChart.Data<>(getMonthName(month), data.getRevenue()));
        }
        revenueChart.getData().add(series);
        revenueChart.getStyleClass().add("area-chart");
    }

    private void updateMonthData(int month) {
        MonthData current = monthDataMap.get(month);
        if (current == null) return;

        double currentRevenue = current.getRevenue();
        currentRevenueLabel.setText(currencyFormat.format(currentRevenue));

        MonthData previous = monthDataMap.get(month - 1);
        double previousRevenue = (previous != null) ? previous.getRevenue() : 0;
        previousRevenueLabel.setText(currencyFormat.format(previousRevenue));

        double growth = previousRevenue != 0 ? ((currentRevenue - previousRevenue) / previousRevenue) * 100 : 0;
        growthLabel.setText(String.format("%.1f%%", growth));

        if (growth > 0) {
            growthArrow.setText("▲");
            growthArrow.setStyle("-fx-text-fill: #27ae60;");
        } else if (growth < 0) {
            growthArrow.setText("▼");
            growthArrow.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            growthArrow.setText("—");
            growthArrow.setStyle("-fx-text-fill: #6a8aaa;");
        }

        transactionsCountLabel.setText(String.valueOf(current.getTransactions().size()));
        transactionsTable.getItems().setAll(current.getTransactions());
    }

    private String getMonthName(int month) {
        return Month.of(month).getDisplayName(TextStyle.SHORT, Locale.US);
    }

    private void applyPermissions() {
        UserSession session = UserSession.getInstance();
        boolean isEmployee = session.isEmployee();

        addButton.setDisable(isEmployee);
        deleteButton.setDisable(isEmployee);
        editButton.setDisable(isEmployee);
        modifyButton.setDisable(isEmployee);
        printButton.setDisable(false);

        transactionsTable.setEditable(false);
        if (readOnlyLabel != null) {
            readOnlyLabel.setVisible(isEmployee);
            readOnlyLabel.setManaged(isEmployee);
        }
    }

    @FXML private void handleEdit() { DialogHelper.editSelected(transactionsTable, "Transaction"); }
    @FXML private void handleModify() { DialogHelper.modifySelected(transactionsTable, "Transaction"); }
    @FXML private void handleDelete() { DialogHelper.deleteSelected(transactionsTable); }
    @FXML private void handlePrint() { DialogHelper.printTable(transactionsTable); }
    @FXML private void handleAddTransaction() { /* admin only */ }
    @FXML private void handleDeleteTransaction() { /* admin only */ }

    // ---------- Inner classes ----------
    public static class MonthData {
        private int month;
        private double revenue;
        private List<Transaction> transactions;

        public MonthData(int month, double revenue, List<Transaction> transactions) {
            this.month = month;
            this.revenue = revenue;
            this.transactions = transactions;
        }

        public int getMonth() { return month; }
        public double getRevenue() { return revenue; }
        public List<Transaction> getTransactions() { return transactions; }

        public void setMonth(int month) { this.month = month; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    }

    public static class Transaction {
        // Using JavaFX properties – now with correct imports
        private final StringProperty id;
        private final StringProperty product;
        private final DoubleProperty amount;
        private final ObjectProperty<LocalDate> date;
        private final StringProperty status;

        public Transaction(String id, String product, double amount, LocalDate date, String status) {
            this.id = new SimpleStringProperty(id);
            this.product = new SimpleStringProperty(product);
            this.amount = new SimpleDoubleProperty(amount);
            this.date = new SimpleObjectProperty<>(date);
            this.status = new SimpleStringProperty(status);
        }

        public String getId() { return id.get(); }
        public String getProduct() { return product.get(); }
        public double getAmount() { return amount.get(); }
        public LocalDate getDate() { return date.get(); }
        public String getStatus() { return status.get(); }

        public StringProperty idProperty() { return id; }
        public StringProperty productProperty() { return product; }
        public DoubleProperty amountProperty() { return amount; }
        public ObjectProperty<LocalDate> dateProperty() { return date; }
        public StringProperty statusProperty() { return status; }

        public void setId(String id) { this.id.set(id); }
        public void setProduct(String product) { this.product.set(product); }
        public void setAmount(double amount) { this.amount.set(amount); }
        public void setDate(LocalDate date) { this.date.set(date); }
        public void setStatus(String status) { this.status.set(status); }

        // For DialogHelper's getter/setter introspection, we also expose the underlying values via getters/setters
        // already done above.
    }
}