package com.ecommerce.finance;

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

public class VATController {

    private static final double STANDARD_RATE = 0.20;
    private static final double REDUCED_RATE = 0.05;
    private static final double ZERO_RATE = 0.00;

    private static TableView<VATTransaction> vatTable;
    private static Label readOnlyLabel;

    public static Scene createVATScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "VAT");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Dynamic VAT Liability Processor");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Segregated tax calculations independent of net profit margins.");
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

        List<VATTransaction> vatTransactions;
        try {
            vatTransactions = DatabaseManager.getInstance().loadVATTransactions(STANDARD_RATE, REDUCED_RATE, ZERO_RATE);
        } catch (SQLException e) {
            e.printStackTrace();
            vatTransactions = generateDummyTransactions();
        }

        double totalGross = vatTransactions.stream().mapToDouble(VATTransaction::getGrossAmount).sum();
        double totalVAT = vatTransactions.stream().mapToDouble(VATTransaction::getVatAmount).sum();
        double totalNet = totalGross - totalVAT;
        double averageRate = (totalGross > 0) ? (totalVAT / totalGross) * 100 : 0;

        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(15);
        cardsGrid.setStyle("-fx-padding: 0 0 5 0;");

        VBox card1 = createKpiCard("Total Gross Sales", String.format("$%,.2f", totalGross));
        VBox card2 = createKpiCard("Total VAT Liability", String.format("$%,.2f", totalVAT));
        VBox card3 = createKpiCard("Net Sales (excl. VAT)", String.format("$%,.2f", totalNet));
        VBox card4 = createKpiCard("Average VAT Rate", String.format("%.2f%%", averageRate));
        cardsGrid.add(card1, 0, 0); cardsGrid.add(card2, 1, 0);
        cardsGrid.add(card3, 2, 0); cardsGrid.add(card4, 3, 0);

        // VAT Breakdown
        double standardGross = vatTransactions.stream().filter(t -> t.getVatRate() == STANDARD_RATE).mapToDouble(VATTransaction::getGrossAmount).sum();
        double standardVAT = vatTransactions.stream().filter(t -> t.getVatRate() == STANDARD_RATE).mapToDouble(VATTransaction::getVatAmount).sum();
        double reducedGross = vatTransactions.stream().filter(t -> t.getVatRate() == REDUCED_RATE).mapToDouble(VATTransaction::getGrossAmount).sum();
        double reducedVAT = vatTransactions.stream().filter(t -> t.getVatRate() == REDUCED_RATE).mapToDouble(VATTransaction::getVatAmount).sum();
        double zeroGross = vatTransactions.stream().filter(t -> t.getVatRate() == ZERO_RATE).mapToDouble(VATTransaction::getGrossAmount).sum();
        double zeroVAT = vatTransactions.stream().filter(t -> t.getVatRate() == ZERO_RATE).mapToDouble(VATTransaction::getVatAmount).sum();

        VBox rateBox = new VBox(10);
        rateBox.getStyleClass().add("app-glass-panel");
        Label rateTitle = new Label("VAT Breakdown by Rate");
        rateTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Region rateUnderline = new Region();
        rateUnderline.setStyle("-fx-background-color: #2563EB; -fx-pref-height: 2; -fx-pref-width: 40;");
        VBox rateTitleBox = new VBox(5);
        rateTitleBox.getChildren().addAll(rateTitle, rateUnderline);

        GridPane rateGrid = new GridPane();
        rateGrid.setHgap(30);
        rateGrid.setVgap(10);
        rateGrid.setStyle("-fx-padding: 10 0 0 0;");

        rateGrid.add(createWhiteLabel("Standard (20%)"), 0, 0);
        rateGrid.add(createWhiteLabel(String.format("Gross: $%,.2f", standardGross)), 1, 0);
        rateGrid.add(createWhiteLabel(String.format("VAT: $%,.2f", standardVAT)), 2, 0);

        rateGrid.add(createWhiteLabel("Reduced (5%)"), 0, 1);
        rateGrid.add(createWhiteLabel(String.format("Gross: $%,.2f", reducedGross)), 1, 1);
        rateGrid.add(createWhiteLabel(String.format("VAT: $%,.2f", reducedVAT)), 2, 1);

        rateGrid.add(createWhiteLabel("Zero (0%)"), 0, 2);
        rateGrid.add(createWhiteLabel(String.format("Gross: $%,.2f", zeroGross)), 1, 2);
        rateGrid.add(createWhiteLabel(String.format("VAT: $%,.2f", zeroVAT)), 2, 2);

        rateBox.getChildren().addAll(rateTitleBox, rateGrid);

        // Table
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Transaction VAT Details");
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

        vatTable = createVATTable();
        vatTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        vatTable.setFocusTraversable(true);
        vatTable.getItems().setAll(vatTransactions);

        editBtn.setOnAction(e -> DialogHelper.editSelected(vatTable, "VAT Transaction"));
        modifyBtn.setOnAction(e -> DialogHelper.modifySelected(vatTable, "VAT Transaction"));
        deleteBtn.setOnAction(e -> DialogHelper.deleteSelected(vatTable));
        printBtn.setOnAction(e -> DialogHelper.printTable(vatTable));

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, vatTable);

        contentWrapper.getChildren().addAll(cardsGrid, rateBox, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(VATController.class.getResource("/styles.css").toExternalForm());
        AnimationHelper.staggerIn(card1, card2, card3, card4);
        AnimationHelper.fadeSlideIn(rateBox, 260);
        AnimationHelper.fadeSlideIn(tableContainer, 340);

        return scene;
    }

    private static Label createWhiteLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        return label;
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
    private static List<VATTransaction> generateDummyTransactions() {
        List<VATTransaction> list = new ArrayList<>();
        Random rand = new Random();
        String[] descriptions = {"iPhone 15 Pro", "MacBook Pro M3", "AirPods Pro 2", "iPad Air 5", "Apple Watch Ultra",
                                 "Mac Mini M2", "Apple TV 4K", "AirTag 4-Pack", "MagSafe Charger", "Apple Pencil 2",
                                 "Software License", "Cloud Services", "Consulting Fee"};
        double[] rates = {0.20, 0.05, 0.00};
        String[] rateLabels = {"Standard", "Reduced", "Zero"};
        for (int i = 0; i < 30; i++) {
            String desc = descriptions[rand.nextInt(descriptions.length)];
            int rateIndex = rand.nextInt(3);
            double rate = rates[rateIndex];
            double gross = Math.round((50 + rand.nextDouble() * 4950) * 100.0) / 100.0;
            double vat = Math.round((gross * rate) * 100.0) / 100.0;
            String transactionId = "TXN-" + (1000 + i);
            list.add(new VATTransaction(transactionId, desc, gross, rate, rateLabels[rateIndex], vat));
        }
        return list;
    }

    private static TableView<VATTransaction> createVATTable() {
        TableView<VATTransaction> table = new TableView<>();
        table.setPrefHeight(280);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<VATTransaction>() {
            @Override
            protected void updateItem(VATTransaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) return;
                getStyleClass().removeAll("even-row", "odd-row");
                getStyleClass().add(getIndex() % 2 == 0 ? "even-row" : "odd-row");
            }
        });

        TableColumn<VATTransaction, String> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<VATTransaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(160);
        descCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<VATTransaction, Double> grossCol = new TableColumn<>("Gross Amount ($)");
        grossCol.setCellValueFactory(new PropertyValueFactory<>("grossAmount"));
        grossCol.setPrefWidth(140);
        grossCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<VATTransaction, Double> rateCol = new TableColumn<>("VAT Rate (%)");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("vatRatePercentage"));
        rateCol.setPrefWidth(120);
        rateCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<VATTransaction, String> rateLabelCol = new TableColumn<>("Rate Type");
        rateLabelCol.setCellValueFactory(new PropertyValueFactory<>("rateLabel"));
        rateLabelCol.setPrefWidth(100);
        rateLabelCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<VATTransaction, Double> vatCol = new TableColumn<>("VAT Amount ($)");
        vatCol.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));
        vatCol.setPrefWidth(140);
        vatCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<VATTransaction, Double> netCol = new TableColumn<>("Net Amount ($)");
        netCol.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        netCol.setPrefWidth(140);
        netCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, descCol, grossCol, rateCol, rateLabelCol, vatCol, netCol);
        return table;
    }

    public static class VATTransaction {
        private String transactionId;
        private String description;
        private double grossAmount;
        private double vatRate;
        private String rateLabel;
        private double vatAmount;
        private double netAmount;

        public VATTransaction(String transactionId, String description, double grossAmount, double vatRate, String rateLabel, double vatAmount) {
            this.transactionId = transactionId;
            this.description = description;
            this.grossAmount = grossAmount;
            this.vatRate = vatRate;
            this.rateLabel = rateLabel;
            this.vatAmount = vatAmount;
            this.netAmount = Math.round((grossAmount - vatAmount) * 100.0) / 100.0;
        }

        public String getTransactionId() { return transactionId; }
        public String getDescription() { return description; }
        public double getGrossAmount() { return grossAmount; }
        public double getVatRate() { return vatRate; }
        public String getRateLabel() { return rateLabel; }
        public double getVatAmount() { return vatAmount; }
        public double getNetAmount() { return netAmount; }
        public double getVatRatePercentage() { return Math.round(vatRate * 100 * 100.0) / 100.0; }

        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public void setDescription(String description) { this.description = description; }
        public void setGrossAmount(double grossAmount) {
            this.grossAmount = Math.round(grossAmount * 100.0) / 100.0;
            recalcVAT();
        }
        public void setVatRate(double vatRate) {
            this.vatRate = vatRate;
            recalcVAT();
        }
        public void setRateLabel(String rateLabel) { this.rateLabel = rateLabel; }
        private void recalcVAT() {
            this.vatAmount = Math.round((this.grossAmount * this.vatRate) * 100.0) / 100.0;
            this.netAmount = Math.round((this.grossAmount - this.vatAmount) * 100.0) / 100.0;
        }
    }
}