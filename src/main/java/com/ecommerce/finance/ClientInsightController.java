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
import java.util.Comparator;

public class ClientInsightController {

    private static List<CustomerProfile> customerProfiles;
    private static TableView<CustomerProfile> customerTable;
    private static Label readOnlyLabel;

    public static Scene createClientInsightScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "ClientInsights");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Client Insight & CLV Profiling");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Customer lifetime value, top buyers, purchase frequency, and average spend.");
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
            customerProfiles = DatabaseManager.getInstance().loadCustomerProfiles();
        } catch (SQLException e) {
            e.printStackTrace();
            customerProfiles = generateDummyCustomers();
        }

        long totalCustomers = customerProfiles.stream().filter(c -> c.getCustomerId() != null && !c.getCustomerId().contains("Deleted")).count();
        double totalRevenue = customerProfiles.stream().mapToDouble(CustomerProfile::getTotalSpent).sum();
        double avgOrderValue = customerProfiles.stream().mapToDouble(CustomerProfile::getAverageOrderValue).average().orElse(0.0);
        CustomerProfile topBuyer = customerProfiles.stream()
                .filter(c -> c.getCustomerId() != null && !c.getCustomerId().contains("Deleted"))
                .max(Comparator.comparingDouble(CustomerProfile::getTotalSpent))
                .orElse(null);

        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(15);
        cardsGrid.setStyle("-fx-padding: 0 0 5 0;");

        VBox card1 = createKpiCard("Total Customers", String.valueOf(totalCustomers));
        VBox card2 = createKpiCard("Total Revenue", String.format("$%,.2f", totalRevenue));
        VBox card3 = createKpiCard("Avg Order Value", String.format("$%,.2f", avgOrderValue));
        String topBuyerText = (topBuyer != null) ? topBuyer.getCustomerId() + " ($" + String.format("%,.2f", topBuyer.getTotalSpent()) + ")" : "N/A";
        VBox card4 = createKpiCard("Top Buyer", topBuyerText);
        cardsGrid.add(card1, 0, 0); cardsGrid.add(card2, 1, 0);
        cardsGrid.add(card3, 2, 0); cardsGrid.add(card4, 3, 0);

        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Customer Profiles");
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

        customerTable = createCustomerTable();
        customerTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        customerTable.setFocusTraversable(true);
        customerTable.getItems().setAll(customerProfiles);

        editBtn.setOnAction(e -> DialogHelper.editSelected(customerTable, "Customer Profile"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(customerTable, "Customer Profile"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(customerTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(customerTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);

        Label noteLabel = new Label("Note: Accounts with 'null' ID represent deleted customers; their historical contributions are retained.");
        noteLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-padding: 5 0 0 0;");

        tableContainer.getChildren().addAll(tableTitleBox, toolbar, customerTable, noteLabel);

        contentWrapper.getChildren().addAll(cardsGrid, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(ClientInsightController.class.getResource("/styles.css").toExternalForm());

        applyPermissions(editBtn, modifyBtn, deleteBtn, printBtn);
        AnimationHelper.staggerIn(card1, card2, card3, card4);
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
        for (int i = 0; i < buttons.length - 1; i++) {
            buttons[i].setDisable(isEmployee);
        }
        buttons[buttons.length - 1].setDisable(false);
    }

    private static VBox createKpiCard(String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("app-kpi-glass");
        card.setPrefWidth(220);
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        card.getChildren().addAll(labelText, valueText);
        return card;
    }

    private static List<CustomerProfile> generateDummyCustomers() {
        List<CustomerProfile> list = new ArrayList<>();
        Random rand = new Random();
        String[] names = {"Alice Johnson", "Bob Smith", "Carol White", "David Brown", "Eva Green",
                          "Frank Black", "Grace Lee", "Henry King", "Ivy Hall", "Jack Rose"};
        for (int i = 0; i < 10; i++) {
            String id = "C" + String.format("%03d", i+1);
            int orderCount = 2 + rand.nextInt(8);
            double totalSpent = 0;
            LocalDate lastOrder = LocalDate.now().minusDays(rand.nextInt(60));
            for (int j = 0; j < orderCount; j++) {
                totalSpent += 50 + rand.nextDouble() * 950;
            }
            totalSpent = Math.round(totalSpent * 100.0) / 100.0;
            double avgOrder = totalSpent / orderCount;
            list.add(new CustomerProfile(id, orderCount, totalSpent, avgOrder, lastOrder));
        }
        double deletedTotal = 0;
        int deletedOrders = 3 + rand.nextInt(5);
        for (int j = 0; j < deletedOrders; j++) {
            deletedTotal += 50 + rand.nextDouble() * 950;
        }
        deletedTotal = Math.round(deletedTotal * 100.0) / 100.0;
        double deletedAvg = deletedTotal / deletedOrders;
        list.add(new CustomerProfile(null, deletedOrders, deletedTotal, deletedAvg, LocalDate.now().minusDays(rand.nextInt(30))));
        return list;
    }

    private static TableView<CustomerProfile> createCustomerTable() {
        TableView<CustomerProfile> table = new TableView<>();
        table.setPrefHeight(300);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<CustomerProfile>() {
            @Override
            protected void updateItem(CustomerProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: #1a1e26;" : "-fx-background-color: #0C0F16;");
            }
        });

        TableColumn<CustomerProfile, String> idCol = new TableColumn<>("Customer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<CustomerProfile, Integer> ordersCol = new TableColumn<>("Order Count");
        ordersCol.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        ordersCol.setPrefWidth(100);
        ordersCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<CustomerProfile, Double> spentCol = new TableColumn<>("Total Spent ($)");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        spentCol.setPrefWidth(140);
        spentCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<CustomerProfile, Double> avgCol = new TableColumn<>("Avg Order Value ($)");
        avgCol.setCellValueFactory(new PropertyValueFactory<>("averageOrderValue"));
        avgCol.setPrefWidth(140);
        avgCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<CustomerProfile, String> lastOrderCol = new TableColumn<>("Last Order Date");
        lastOrderCol.setCellValueFactory(new PropertyValueFactory<>("lastOrderDateFormatted"));
        lastOrderCol.setPrefWidth(140);
        lastOrderCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<CustomerProfile, Double> freqCol = new TableColumn<>("Frequency (Orders/Month)");
        freqCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        freqCol.setPrefWidth(120);
        freqCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, ordersCol, spentCol, avgCol, lastOrderCol, freqCol);
        return table;
    }

    public static class CustomerProfile {
        private String customerId;
        private int orderCount;
        private double totalSpent;
        private double averageOrderValue;
        private LocalDate lastOrderDate;
        private double frequency;

        public CustomerProfile(String customerId, int orderCount, double totalSpent, double averageOrderValue, LocalDate lastOrderDate) {
            this.customerId = customerId;
            this.orderCount = orderCount;
            this.totalSpent = totalSpent;
            this.averageOrderValue = averageOrderValue;
            this.lastOrderDate = lastOrderDate;
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastOrderDate, LocalDate.now());
            double months = Math.max(1, daysBetween / 30.0);
            this.frequency = Math.round((orderCount / months) * 100.0) / 100.0;
        }

        public String getCustomerId() { return (customerId == null) ? "null (Deleted)" : customerId; }
        public int getOrderCount() { return orderCount; }
        public double getTotalSpent() { return totalSpent; }
        public double getAverageOrderValue() { return averageOrderValue; }
        public String getLastOrderDateFormatted() { return lastOrderDate.format(DateTimeFormatter.ISO_LOCAL_DATE); }
        public double getFrequency() { return frequency; }

        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
        public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
        public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
        public void setLastOrderDate(LocalDate lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    }
}