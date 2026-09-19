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

public class AnomalyDetectionController {

    private static final int MIN_SAMPLE_SIZE = 10;
    private static TableView<ProductAnomaly> productTable;
    private static Label readOnlyLabel;

    public static Scene createAnomalyScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "AnomalyDetection");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Predictive Anomaly Detection");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Automated visual flags for high refund rates or underperforming stock.");
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

        List<ProductAnomaly> productData;
        try {
            productData = DatabaseManager.getInstance().loadProductAnomalies(MIN_SAMPLE_SIZE);
        } catch (SQLException e) {
            e.printStackTrace();
            productData = generateDummyProducts();
        }

        long totalProducts = productData.size();
        long flaggedProducts = productData.stream().filter(ProductAnomaly::isAnomaly).count();
        double avgRefundRate = productData.stream()
                .filter(p -> p.getSalesCount() >= MIN_SAMPLE_SIZE)
                .mapToDouble(ProductAnomaly::getRefundRate)
                .average()
                .orElse(0.0);
        long underperformCount = productData.stream()
                .filter(p -> p.isAnomaly() && p.getAnomalyType().equals("Underperforming Stock"))
                .count();

        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(15);
        cardsGrid.setStyle("-fx-padding: 0 0 5 0;");

        VBox card1 = createKpiCard("Total Products", String.valueOf(totalProducts));
        VBox card2 = createKpiCard("Flagged Products", String.valueOf(flaggedProducts));
        VBox card3 = createKpiCard("Avg Refund Rate", String.format("%.2f%%", avgRefundRate));
        VBox card4 = createKpiCard("Underperforming Stock", String.valueOf(underperformCount));
        cardsGrid.add(card1, 0, 0); cardsGrid.add(card2, 1, 0);
        cardsGrid.add(card3, 2, 0); cardsGrid.add(card4, 3, 0);

        Label infoNote = new Label("Min Sample Size: " + MIN_SAMPLE_SIZE + " sales required for anomaly detection.");
        infoNote.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-padding: 0 0 10 0;");

        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Product Anomaly Report");
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

        productTable = createProductTable();
        productTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        productTable.setFocusTraversable(true);
        productTable.getItems().setAll(productData);

        editBtn.setOnAction(e -> DialogHelper.editSelected(productTable, "Product Anomaly"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(productTable, "Product Anomaly"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(productTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(productTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, productTable);

        contentWrapper.getChildren().addAll(cardsGrid, infoNote, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(AnomalyDetectionController.class.getResource("/styles.css").toExternalForm());

        applyPermissions(editBtn, modifyBtn, deleteBtn, printBtn);
     // Disable editing for employees
        if (SessionManager.getInstance().isEmployee()) {
            editBtn.setDisable(true);
            modifyBtn.setDisable(true);
            deleteBtn.setDisable(true);
            // printBtn stays enabled
        }
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

    private static List<ProductAnomaly> generateDummyProducts() {
        List<ProductAnomaly> list = new ArrayList<>();
        Random rand = new Random();
        String[] productNames = {"iPhone 15 Pro", "MacBook Pro M3", "AirPods Pro 2", "iPad Air 5", "Apple Watch Ultra",
                                 "Mac Mini M2", "Apple TV 4K", "AirTag 4-Pack", "MagSafe Charger", "Apple Pencil 2",
                                 "Studio Display", "Mac Studio", "HomePod Mini", "iMac 24\"", "Pro Display XDR"};
        for (String name : productNames) {
            int sales = 5 + rand.nextInt(45);
            int returns = 0;
            if (name.equals("AirPods Pro 2") || name.equals("MagSafe Charger")) {
                returns = (int)(sales * (0.25 + rand.nextDouble() * 0.15));
            } else if (name.equals("Apple Watch Ultra")) {
                returns = (int)(sales * 0.05);
            } else {
                returns = (int)(sales * (0.02 + rand.nextDouble() * 0.12));
            }
            returns = Math.min(returns, sales);
            double refundRate = (sales > 0) ? ((double) returns / sales) * 100 : 0;
            boolean isAnomaly = false;
            String anomalyType = "Normal";
            if (sales >= MIN_SAMPLE_SIZE) {
                if (refundRate > 20.0) { isAnomaly = true; anomalyType = "High Refund Rate"; }
                else if (sales < 10 && refundRate > 10.0) { isAnomaly = true; anomalyType = "Underperforming Stock"; }
                else if (sales < 5 && refundRate > 5.0) { isAnomaly = true; anomalyType = "Underperforming Stock"; }
            } else {
                anomalyType = "Insufficient Data";
            }
            list.add(new ProductAnomaly(name, sales, returns, refundRate, isAnomaly, anomalyType));
        }
        return list;
    }

    private static TableView<ProductAnomaly> createProductTable() {
        TableView<ProductAnomaly> table = new TableView<>();
        table.setPrefHeight(350);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<ProductAnomaly>() {
            @Override
            protected void updateItem(ProductAnomaly item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: #1a1e26;" : "-fx-background-color: #0C0F16;");
            }
        });

        TableColumn<ProductAnomaly, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        nameCol.setPrefWidth(160);
        nameCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductAnomaly, Integer> salesCol = new TableColumn<>("Sales Count");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("salesCount"));
        salesCol.setPrefWidth(100);
        salesCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductAnomaly, Integer> returnsCol = new TableColumn<>("Returns");
        returnsCol.setCellValueFactory(new PropertyValueFactory<>("returnsCount"));
        returnsCol.setPrefWidth(80);
        returnsCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductAnomaly, Double> refundCol = new TableColumn<>("Refund Rate (%)");
        refundCol.setCellValueFactory(new PropertyValueFactory<>("refundRate"));
        refundCol.setPrefWidth(120);
        refundCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductAnomaly, String> anomalyCol = new TableColumn<>("Anomaly Status");
        anomalyCol.setCellValueFactory(new PropertyValueFactory<>("anomalyStatus"));
        anomalyCol.setPrefWidth(160);
        anomalyCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ProductAnomaly, String> typeCol = new TableColumn<>("Anomaly Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("anomalyType"));
        typeCol.setPrefWidth(180);
        typeCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(nameCol, salesCol, returnsCol, refundCol, anomalyCol, typeCol);
        return table;
    }

    public static class ProductAnomaly {
        private String productName;
        private int salesCount;
        private int returnsCount;
        private double refundRate;
        private boolean isAnomaly;
        private String anomalyType;

        public ProductAnomaly(String productName, int salesCount, int returnsCount, double refundRate, boolean isAnomaly, String anomalyType) {
            this.productName = productName;
            this.salesCount = salesCount;
            this.returnsCount = returnsCount;
            this.refundRate = Math.round(refundRate * 100.0) / 100.0;
            this.isAnomaly = isAnomaly;
            this.anomalyType = anomalyType;
        }

        public String getProductName() { return productName; }
        public int getSalesCount() { return salesCount; }
        public int getReturnsCount() { return returnsCount; }
        public double getRefundRate() { return refundRate; }
        public boolean isAnomaly() { return isAnomaly; }
        public String getAnomalyType() { return anomalyType; }
        public String getAnomalyStatus() {
            if (salesCount < MIN_SAMPLE_SIZE) return "Insufficient Data";
            return isAnomaly ? "Flagged" : "Normal";
        }

        public void setProductName(String productName) { this.productName = productName; }
        public void setSalesCount(int salesCount) { this.salesCount = salesCount; }
        public void setReturnsCount(int returnsCount) { this.returnsCount = returnsCount; }
        public void setRefundRate(double refundRate) { this.refundRate = Math.round(refundRate * 100.0) / 100.0; }
        public void setAnomaly(boolean anomaly) { isAnomaly = anomaly; }
        public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }
    }
}