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

public class MarginAnalysisController {

    private static TableView<TransactionMargin> marginTable;
    private static Label readOnlyLabel;

    public static Scene createMarginAnalysisScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "MarginAnalysis");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Algorithmic Margin Analysis");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Automatically calculated cost, sale price, and profit margin per transaction.");
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

        List<TransactionMargin> transactions;
        try {
            transactions = DatabaseManager.getInstance().loadMarginTransactions();
        } catch (SQLException e) {
            e.printStackTrace();
            transactions = generateDummyTransactions();
        }

        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(15);
        cardsGrid.setStyle("-fx-padding: 0 0 5 0;");

        VBox card1 = createKpiCard("Total Transactions", String.valueOf(transactions.size()));
        double avgMargin = transactions.stream().mapToDouble(TransactionMargin::getMarginPercentage).average().orElse(0.0);
        VBox card2 = createKpiCard("Average Margin", String.format("%.2f%%", avgMargin));
        double totalProfit = transactions.stream().mapToDouble(t -> t.getSalePrice() - t.getCost()).sum();
        VBox card3 = createKpiCard("Total Profit", String.format("$%,.2f", totalProfit));
        cardsGrid.add(card1, 0, 0); cardsGrid.add(card2, 1, 0); cardsGrid.add(card3, 2, 0);

        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Transaction Margin Details");
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

        marginTable = createMarginTable(transactions);
        marginTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        marginTable.setFocusTraversable(true);

        editBtn.setOnAction(e -> DialogHelper.editSelected(marginTable, "Margin Transaction"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(marginTable, "Margin Transaction"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(marginTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(marginTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, marginTable);

        contentWrapper.getChildren().addAll(cardsGrid, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(MarginAnalysisController.class.getResource("/styles.css").toExternalForm());
        AnimationHelper.staggerIn(card1, card2, card3);
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
    private static List<TransactionMargin> generateDummyTransactions() {
        List<TransactionMargin> list = new ArrayList<>();
        Random rand = new Random();
        String[] products = {"iPhone 15 Pro", "MacBook Pro M3", "AirPods Pro 2", "iPad Air 5", "Apple Watch Ultra",
                             "Mac Mini M2", "Apple TV 4K", "AirTag 4-Pack", "MagSafe Charger", "Apple Pencil 2"};
        for (int i = 0; i < 12; i++) {
            double cost = 50 + rand.nextDouble() * 900;
            double salePrice = cost + 20 + rand.nextDouble() * 200;
            if (i == 5) salePrice = 0;
            String product = products[rand.nextInt(products.length)];
            list.add(new TransactionMargin("TXN-" + (1001 + i), product, cost, salePrice));
        }
        return list;
    }

    private static TableView<TransactionMargin> createMarginTable(List<TransactionMargin> data) {
        TableView<TransactionMargin> table = new TableView<>();
        table.setPrefHeight(300);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<TransactionMargin>() {
            @Override
            protected void updateItem(TransactionMargin item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) return;
                getStyleClass().removeAll("even-row", "odd-row");
                getStyleClass().add(getIndex() % 2 == 0 ? "even-row" : "odd-row");
            }
        });

        TableColumn<TransactionMargin, String> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        idCol.setPrefWidth(130);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<TransactionMargin, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        productCol.setPrefWidth(160);
        productCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<TransactionMargin, Double> costCol = new TableColumn<>("Cost ($)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setPrefWidth(110);
        costCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<TransactionMargin, Double> saleCol = new TableColumn<>("Sale Price ($)");
        saleCol.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        saleCol.setPrefWidth(130);
        saleCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<TransactionMargin, String> marginCol = new TableColumn<>("Profit Margin (%)");
        marginCol.setCellValueFactory(new PropertyValueFactory<>("marginPercentageFormatted"));
        marginCol.setPrefWidth(160);
        marginCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, productCol, costCol, saleCol, marginCol);
        table.getItems().setAll(data);
        return table;
    }

    public static class TransactionMargin {
        private String transactionId;
        private String product;
        private double cost;
        private double salePrice;
        private double marginPercentage;

        public TransactionMargin(String transactionId, String product, double cost, double salePrice) {
            this.transactionId = transactionId;
            this.product = product;
            this.cost = Math.round(cost * 100.0) / 100.0;
            if (salePrice <= 0) {
                this.salePrice = 0;
                this.marginPercentage = 0;
            } else {
                this.salePrice = Math.round(salePrice * 100.0) / 100.0;
                recalcMargin();
            }
        }

        public String getTransactionId() { return transactionId; }
        public String getProduct() { return product; }
        public double getCost() { return cost; }
        public double getSalePrice() { return salePrice; }
        public double getMarginPercentage() { return marginPercentage; }
        public String getMarginPercentageFormatted() {
            if (salePrice <= 0) return "N/A (Invalid)";
            return String.format("%.2f%%", marginPercentage);
        }

        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public void setProduct(String product) { this.product = product; }
        public void setCost(double cost) {
            this.cost = Math.round(cost * 100.0) / 100.0;
            recalcMargin();
        }
        public void setSalePrice(double salePrice) {
            if (salePrice <= 0) {
                this.salePrice = 0;
                this.marginPercentage = 0;
            } else {
                this.salePrice = Math.round(salePrice * 100.0) / 100.0;
                recalcMargin();
            }
        }
        private void recalcMargin() {
            if (salePrice > 0) {
                double margin = ((salePrice - cost) / salePrice) * 100;
                this.marginPercentage = Math.round(margin * 100.0) / 100.0;
            } else {
                this.marginPercentage = 0;
            }
        }
    }
}