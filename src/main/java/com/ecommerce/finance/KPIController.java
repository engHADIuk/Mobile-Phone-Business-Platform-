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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KPIController {

    private static TableView<ProductKPI> productTable;
    private static Label readOnlyLabel;

    public static Scene createKPIScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "KPIMetrics");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Aggregate KPI Engine");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("System-wide financial health metrics.");
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

        VBox contentWrapper = new VBox(20);
        contentWrapper.setStyle("-fx-padding: 25 30 30 30;");

        KPIData data;
        try {
            data = DatabaseManager.getInstance().loadKPIData();
        } catch (SQLException e) {
            e.printStackTrace();
            data = generateKPIData();
        }

        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(15);
        cardsGrid.setStyle("-fx-padding: 0 0 5 0;");

        VBox card1 = createKpiCard("Cumulative Income", String.format("$%,.2f", data.totalRevenue));
        VBox card2 = createKpiCard("Average Order Value", String.format("$%,.2f", data.averageOrderValue));
        VBox card3 = createKpiCard("Total Orders", String.valueOf(data.totalOrders));
        VBox card4 = createKpiCard("Products Sold", String.valueOf(data.totalUnitsSold));
        cardsGrid.add(card1, 0, 0); cardsGrid.add(card2, 1, 0);
        cardsGrid.add(card3, 2, 0); cardsGrid.add(card4, 3, 0);

        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Top Products by Revenue");
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

        if (SessionManager.getInstance().isEmployee()) {
            editBtn.setDisable(true);
            modifyBtn.setDisable(true);
            deleteBtn.setDisable(true);
        }

        productTable = createProductTable(data.topProducts);
        productTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        productTable.setFocusTraversable(true);

        editBtn.setOnAction(e -> DialogHelper.editSelected(productTable, "Product KPI"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(productTable, "Product KPI"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(productTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(productTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, productTable);

        contentWrapper.getChildren().addAll(cardsGrid, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(KPIController.class.getResource("/styles.css").toExternalForm());
        AnimationHelper.staggerIn(card1, card2, card3, card4);
        AnimationHelper.fadeSlideIn(tableContainer, 260);

        return scene;
    }

    private static VBox createKpiCard(String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("app-kpi-glass");
        Label labelText = new Label(label);
        labelText.getStyleClass().add("label");
        Label valueText = new Label(value);
        valueText.getStyleClass().add("value");
        card.getChildren().addAll(labelText, valueText);
        return card;
    }

    // ... (rest of the class unchanged – keep your existing methods)
    private static KPIData generateKPIData() {
        Random rand = new Random();
        int totalOrders = 150 + rand.nextInt(350);
        double totalRevenue = 0;
        int totalUnits = 0;
        String[] productNames = {"iPhone 15 Pro", "MacBook Pro M3", "AirPods Pro 2", "iPad Air 5", "Apple Watch Ultra",
                                 "Mac Mini M2", "Apple TV 4K", "AirTag 4-Pack", "MagSafe Charger", "Apple Pencil 2"};
        List<ProductKPI> products = new ArrayList<>();
        for (String name : productNames) {
            int units = 10 + rand.nextInt(90);
            double revenue = units * (100 + rand.nextDouble() * 1900);
            totalUnits += units;
            totalRevenue += revenue;
            products.add(new ProductKPI(name, units, revenue));
        }
        products.sort((p1, p2) -> Double.compare(p2.revenue, p1.revenue));
        for (ProductKPI p : products) {
            double percent = (totalRevenue == 0) ? 0.0 : (p.revenue / totalRevenue) * 100;
            p.setPercentageOfTotal(String.format("%.2f%%", percent));
        }
        double avgOrderValue = (totalOrders == 0) ? 0.0 : totalRevenue / totalOrders;
        return new KPIData(totalRevenue, avgOrderValue, totalOrders, totalUnits, products);
    }

    private static TableView<ProductKPI> createProductTable(List<ProductKPI> products) {
        TableView<ProductKPI> table = new TableView<>();
        table.setPrefHeight(250);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<ProductKPI>() {
            @Override
            protected void updateItem(ProductKPI item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) return;
                getStyleClass().removeAll("even-row", "odd-row");
                getStyleClass().add(getIndex() % 2 == 0 ? "even-row" : "odd-row");
            }
        });

        TableColumn<ProductKPI, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        nameCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductKPI, Integer> unitsCol = new TableColumn<>("Units Sold");
        unitsCol.setCellValueFactory(new PropertyValueFactory<>("unitsSold"));
        unitsCol.setPrefWidth(100);
        unitsCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductKPI, Double> revenueCol = new TableColumn<>("Revenue ($)");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        revenueCol.setPrefWidth(150);
        revenueCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductKPI, String> percentCol = new TableColumn<>("% of Total");
        percentCol.setCellValueFactory(new PropertyValueFactory<>("percentageOfTotal"));
        percentCol.setPrefWidth(120);
        percentCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(nameCol, unitsCol, revenueCol, percentCol);
        table.getItems().setAll(products);
        return table;
    }

    public static class KPIData {
        public final double totalRevenue;
        public final double averageOrderValue;
        public final int totalOrders;
        public final int totalUnitsSold;
        public final List<ProductKPI> topProducts;
        public KPIData(double totalRevenue, double averageOrderValue, int totalOrders, int totalUnitsSold, List<ProductKPI> topProducts) {
            this.totalRevenue = totalRevenue;
            this.averageOrderValue = averageOrderValue;
            this.totalOrders = totalOrders;
            this.totalUnitsSold = totalUnitsSold;
            this.topProducts = topProducts;
        }
    }

    public static class ProductKPI {
        private String name;
        private int unitsSold;
        private double revenue;
        private String percentageOfTotal;

        public ProductKPI(String name, int unitsSold, double revenue) {
            this.name = name;
            this.unitsSold = unitsSold;
            this.revenue = Math.round(revenue * 100.0) / 100.0;
            this.percentageOfTotal = "";
        }

        public String getName() { return name; }
        public int getUnitsSold() { return unitsSold; }
        public double getRevenue() { return revenue; }
        public String getPercentageOfTotal() { return percentageOfTotal; }

        public void setName(String name) { this.name = name; }
        public void setUnitsSold(int unitsSold) { this.unitsSold = unitsSold; }
        public void setRevenue(double revenue) { this.revenue = Math.round(revenue * 100.0) / 100.0; }
        public void setPercentageOfTotal(String percentage) { this.percentageOfTotal = percentage; }
    }
}