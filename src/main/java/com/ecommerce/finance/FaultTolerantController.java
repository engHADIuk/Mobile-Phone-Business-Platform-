package com.ecommerce.finance;

import java.sql.SQLException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FaultTolerantController {

    private static List<FaultTolerantOrder> orderData = generateDummyOrders();
    private static TableView<FaultTolerantOrder> orderTable;
    private static Label readOnlyLabel;

    public static Scene createFaultTolerantScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "FaultTolerance");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Fault-Tolerant Data Resolution");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Graceful handling of incomplete data – deleted customers, discontinued products.");
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

        long totalOrders = orderData.size();
        double totalRevenue = orderData.stream().mapToDouble(FaultTolerantOrder::getPrice).sum();
        double avgPrice = (totalOrders == 0) ? 0.0 : totalRevenue / totalOrders;
        long missingCustomerCount = orderData.stream().filter(o -> o.getCustomerIdSafe().equals("Deleted Customer")).count();
        long missingProductCount = orderData.stream().filter(o -> o.getProductNameSafe().equals("Discontinued Product")).count();

        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(15);
        cardsGrid.setStyle("-fx-padding: 0 0 5 0;");

        VBox card1 = createKpiCard("Total Orders", String.valueOf(totalOrders));
        VBox card2 = createKpiCard("Total Revenue", String.format("$%,.2f", totalRevenue));
        VBox card3 = createKpiCard("Avg Order Value", String.format("$%,.2f", avgPrice));
        VBox card4 = createKpiCard("Missing Customer/Product", missingCustomerCount + "/" + missingProductCount);
        cardsGrid.add(card1, 0, 0); cardsGrid.add(card2, 1, 0);
        cardsGrid.add(card3, 2, 0); cardsGrid.add(card4, 3, 0);

        Label infoNote = new Label("Placeholder strings: 'Deleted Customer', 'Discontinued Product' – data integrity maintained.");
        infoNote.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 10 0;");

        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Orders with Missing Data (Fault-Tolerant View)");
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

        orderTable = createOrderTable();
        orderTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        orderTable.setFocusTraversable(true);
        orderTable.getItems().setAll(orderData);

        editBtn.setOnAction(e -> DialogHelper.editSelected(orderTable, "Order"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(orderTable, "Order"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(orderTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(orderTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, orderTable);

        contentWrapper.getChildren().addAll(cardsGrid, infoNote, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(FaultTolerantController.class.getResource("/styles.css").toExternalForm());

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

    private static List<FaultTolerantOrder> generateDummyOrders() {
        List<FaultTolerantOrder> orders = new ArrayList<>();
        Random rand = new Random();
        String[] customers = {"C001", "C002", null, "C004", null, "C006", "C007", null, "C009", "C010"};
        String[] products = {"iPhone 15 Pro", null, "AirPods Pro 2", null, "Apple Watch Ultra", "Mac Mini M2", null, "AirTag 4-Pack", null, "Apple Pencil 2"};
        String[] statuses = {"Completed", "Pending", "Refunded", "Completed", "Completed"};
        for (int i = 0; i < 20; i++) {
            String orderId = "TXN-" + (1000 + i);
            String customer = customers[rand.nextInt(customers.length)];
            String product = products[rand.nextInt(products.length)];
            double price = Math.round((50 + rand.nextDouble() * 950) * 100.0) / 100.0;
            String status = statuses[rand.nextInt(statuses.length)];
            orders.add(new FaultTolerantOrder(orderId, customer, product, price, status));
        }
        return orders;
    }

    private static TableView<FaultTolerantOrder> createOrderTable() {
        TableView<FaultTolerantOrder> table = new TableView<>();
        table.setPrefHeight(300);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<FaultTolerantOrder>() {
            @Override
            protected void updateItem(FaultTolerantOrder item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: #1a1e26;" : "-fx-background-color: #0C0F16;");
            }
        });

        TableColumn<FaultTolerantOrder, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<FaultTolerantOrder, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerIdSafe"));
        customerCol.setPrefWidth(150);
        customerCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<FaultTolerantOrder, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productNameSafe"));
        productCol.setPrefWidth(180);
        productCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<FaultTolerantOrder, Double> priceCol = new TableColumn<>("Price ($)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(120);
        priceCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<FaultTolerantOrder, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, customerCol, productCol, priceCol, statusCol);
        return table;
    }

    public static class FaultTolerantOrder {
        private String orderId;
        private String customerId;
        private String productName;
        private double price;
        private String status;

        public FaultTolerantOrder(String orderId, String customerId, String productName, double price, String status) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.productName = productName;
            this.price = price;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
        public String getCustomerIdSafe() {
            return (customerId == null || customerId.trim().isEmpty()) ? "Deleted Customer" : customerId;
        }
        public String getProductNameSafe() {
            return (productName == null || productName.trim().isEmpty()) ? "Discontinued Product" : productName;
        }

        public void setOrderId(String orderId) { this.orderId = orderId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setPrice(double price) { this.price = Math.round(price * 100.0) / 100.0; }
        public void setStatus(String status) { this.status = status; }
    }
}