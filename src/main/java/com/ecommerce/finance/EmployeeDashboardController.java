package com.ecommerce.finance;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDashboardController {

    private static TableView<Transaction> transactionsTable;

    private static Label revenueValue, revenueChange;
    private static Label expensesValue, expensesChange;
    private static Label profitValue, profitChange;
    private static Label marginValue, marginChange;

    private static final double[] MONTHLY_REVENUE = {4.8, 5.2, 6.1, 5.8, 7.2, 6.9, 8.1, 7.8, 9.2, 8.8, 10.5, 9.8};
    private static final double[] MONTHLY_EXPENSES = {3.2, 3.5, 3.8, 3.4, 4.2, 3.9, 4.6, 4.3, 5.1, 4.8, 5.8, 5.2};

    public static Scene createEmployeeDashboardScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "Dashboard");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: transparent; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        String userName = SessionManager.getInstance().getCurrentUser();
        if (userName == null) userName = "User";

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Good morning, Mr. " + userName.toUpperCase());
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI', Arial, sans-serif;");
        Label subText = new Label("View-only access – you can view data but cannot modify it.");
        subText.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px;");
        headerText.getChildren().addAll(welcomeLabel, subText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label readOnlyBadge = new Label("READ-ONLY");
        readOnlyBadge.getStyleClass().add("app-readonly-badge");
        headerBar.getChildren().addAll(headerText, spacer, readOnlyBadge);

        VBox contentWrapper = new VBox(20);
        contentWrapper.setStyle("-fx-padding: 0 30 30 30;");
        contentWrapper.setFillWidth(true);

        // ---------- KPI SECTION WITH TIME PERIOD SWITCHER ----------
        HBox kpiRow = new HBox(20);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        kpiRow.setStyle("-fx-padding: 0 0 10 0;");

        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(20);
        kpiGrid.setVgap(15);
        VBox card1 = createKpiCard("Total Revenue", "$84.1M", "+12.5%", "#10B981");
        VBox card2 = createKpiCard("Total Expenses", "$48.3M", "-3.2%", "#EF4444");
        VBox card3 = createKpiCard("Net Profit", "$35.8M", "+18.7%", "#10B981");
        VBox card4 = createKpiCard("Profit Margin", "27.6%", "+4.2%", "#10B981");
        kpiGrid.add(card1, 0, 0); kpiGrid.add(card2, 1, 0);
        kpiGrid.add(card3, 2, 0); kpiGrid.add(card4, 3, 0);

        Region kpiSpacer = new Region();
        HBox.setHgrow(kpiSpacer, Priority.ALWAYS);

        VBox buttonBox = new VBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-padding: 0 0 0 20;");

        Label periodTitle = new Label("Time Period");
        periodTitle.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        Button lastWeekBtn = createPeriodButton("1 Week", false);
        Button monthlyBtn = createPeriodButton("1 Month", true);
        Button sixMonthsBtn = createPeriodButton("6 Months", false);
        Button yearlyBtn = createPeriodButton("1 Year", false);

        lastWeekBtn.setOnAction(e -> updateKPIs("1 Week", lastWeekBtn, monthlyBtn, sixMonthsBtn, yearlyBtn));
        monthlyBtn.setOnAction(e -> updateKPIs("1 Month", lastWeekBtn, monthlyBtn, sixMonthsBtn, yearlyBtn));
        sixMonthsBtn.setOnAction(e -> updateKPIs("6 Months", lastWeekBtn, monthlyBtn, sixMonthsBtn, yearlyBtn));
        yearlyBtn.setOnAction(e -> updateKPIs("1 Year", lastWeekBtn, monthlyBtn, sixMonthsBtn, yearlyBtn));

        buttonBox.getChildren().addAll(periodTitle, lastWeekBtn, monthlyBtn, sixMonthsBtn, yearlyBtn);

        kpiRow.getChildren().addAll(kpiGrid, kpiSpacer, buttonBox);

        // Chart Row
        HBox chartRow = new HBox(20);
        chartRow.setStyle("-fx-padding: 5 0 0 0;");
        chartRow.setAlignment(Pos.CENTER_LEFT);

        VBox chartContainer = new VBox(10);
        chartContainer.getStyleClass().add("app-glass-panel");
        chartContainer.setPrefWidth(750);
        VBox chartTitleBox = new VBox(5);
        Label chartTitle = new Label("Revenue vs. Expenses");
        chartTitle.getStyleClass().add("app-section-title");
        Label chartSub = new Label("Monthly breakdown · FY 2024");
        chartSub.getStyleClass().add("app-section-sub");
        Region chartUnderline = new Region();
        chartUnderline.getStyleClass().add("app-underline");
        chartTitleBox.getChildren().addAll(chartTitle, chartSub, chartUnderline);
        BarChart<String, Number> chart = createBarChart();
        chartContainer.getChildren().addAll(chartTitleBox, chart);

        VBox pieContainer = new VBox(10);
        pieContainer.getStyleClass().add("app-glass-panel");
        pieContainer.setPrefWidth(380);
        VBox pieTitleBox = new VBox(5);
        Label pieTitle = new Label("Expense Breakdown");
        pieTitle.getStyleClass().add("app-section-title");
        Label pieSub = new Label("FY 2024 · Total $50.5M");
        pieSub.getStyleClass().add("app-section-sub");
        Region pieUnderline = new Region();
        pieUnderline.getStyleClass().add("app-underline");
        pieTitleBox.getChildren().addAll(pieTitle, pieSub, pieUnderline);
        PieChart pieChart = createPieChart();
        pieContainer.getChildren().addAll(pieTitleBox, pieChart);

        chartRow.getChildren().addAll(chartContainer, pieContainer);

        // Transactions Table – Print only
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        VBox tableTitleBox = new VBox(5);
        Label tableTitle = new Label("Recent Transactions");
        tableTitle.getStyleClass().add("app-section-title");
        Label tableSub2 = new Label("December 2024");
        tableSub2.getStyleClass().add("app-section-sub");
        Region tableUnderline = new Region();
        tableUnderline.getStyleClass().add("app-underline");
        tableTitleBox.getChildren().addAll(tableTitle, tableSub2, tableUnderline);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-padding: 10 0 5 0;");
        Button editBtn = new Button("Edit");
        editBtn.setDisable(true);
        editBtn.getStyleClass().add("action-button");
        Button modifyBtn = new Button("Modify");
        modifyBtn.setDisable(true);
        modifyBtn.getStyleClass().add("action-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.setDisable(true);
        deleteBtn.getStyleClass().add("action-button-danger");
        Button printBtn = new Button("Print");
        printBtn.getStyleClass().add("action-button");

        transactionsTable = createTransactionsTable();
        transactionsTable.getItems().setAll(generateRealisticTransactions());
        transactionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        transactionsTable.setFocusTraversable(true);

        printBtn.setOnAction(e -> DialogHelper.printTable(transactionsTable));
        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);

        tableContainer.getChildren().addAll(tableTitleBox, toolbar, transactionsTable);

        contentWrapper.getChildren().addAll(kpiRow, chartRow, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(EmployeeDashboardController.class.getResource("/styles.css").toExternalForm());

        updateKPIs("1 Month", lastWeekBtn, monthlyBtn, sixMonthsBtn, yearlyBtn);

        AnimationHelper.staggerIn(card1, card2, card3, card4);
        AnimationHelper.fadeSlideIn(chartContainer, 260);
        AnimationHelper.fadeSlideIn(pieContainer, 320);
        AnimationHelper.fadeSlideIn(tableContainer, 400);

        return scene;
    }

    private static VBox createKpiCard(String label, String value, String change, String color) {
        VBox card = new VBox(10);
        card.getStyleClass().add("app-kpi-glass");

        StackPane icon = new StackPane();
        icon.getStyleClass().add("app-kpi-icon");
        Label iconLabel = new Label(kpiIcon(label));
        iconLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #93c5fd; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI';");
        icon.getChildren().add(iconLabel);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #8aa3c4; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        HBox valueRow = new HBox(12);
        valueRow.setAlignment(Pos.CENTER_LEFT);
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI';");
        Label changeText = new Label(change);
        changeText.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        valueRow.getChildren().addAll(valueText, changeText);
        Label vsLabel = new Label("vs. FY 2023");
        vsLabel.setStyle("-fx-text-fill: #4a6a8a; -fx-font-size: 11px;");
        card.getChildren().addAll(icon, labelText, valueRow, vsLabel);

        if (label.equals("Total Revenue")) {
            revenueValue = valueText;
            revenueChange = changeText;
        } else if (label.equals("Total Expenses")) {
            expensesValue = valueText;
            expensesChange = changeText;
        } else if (label.equals("Net Profit")) {
            profitValue = valueText;
            profitChange = changeText;
        } else if (label.equals("Profit Margin")) {
            marginValue = valueText;
            marginChange = changeText;
        }

        return card;
    }

    private static Button createPeriodButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(110);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add("period-button");
        if (active) {
            btn.getStyleClass().add("period-button-active");
        }
        return btn;
    }

    private static void updateKPIs(String period, Button... periodButtons) {
        for (Button btn : periodButtons) {
            btn.getStyleClass().remove("period-button-active");
            btn.getStyleClass().add("period-button");
        }
        for (Button btn : periodButtons) {
            if (btn.getText().equals(period)) {
                btn.getStyleClass().remove("period-button");
                btn.getStyleClass().add("period-button-active");
                break;
            }
        }

        int monthsToInclude = 0;
        double revenue = 0, expenses = 0;
        switch (period) {
            case "1 Week":
                revenue = MONTHLY_REVENUE[MONTHLY_REVENUE.length - 1] / 4.0;
                expenses = MONTHLY_EXPENSES[MONTHLY_EXPENSES.length - 1] / 4.0;
                break;
            case "1 Month":
                monthsToInclude = 1;
                break;
            case "6 Months":
                monthsToInclude = 6;
                break;
            case "1 Year":
                monthsToInclude = 12;
                break;
        }

        if (monthsToInclude > 0) {
            for (int i = MONTHLY_REVENUE.length - monthsToInclude; i < MONTHLY_REVENUE.length; i++) {
                if (i >= 0) {
                    revenue += MONTHLY_REVENUE[i];
                    expenses += MONTHLY_EXPENSES[i];
                }
            }
        }

        double profit = revenue - expenses;
        double margin = (revenue > 0) ? (profit / revenue) * 100 : 0;

        revenueValue.setText(String.format("$%.1fM", revenue));
        expensesValue.setText(String.format("$%.1fM", expenses));
        profitValue.setText(String.format("$%.1fM", profit));
        marginValue.setText(String.format("%.1f%%", margin));

        revenueChange.setText("+12.5%");
        revenueChange.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-font-weight: bold;");
        expensesChange.setText("-3.2%");
        expensesChange.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-font-weight: bold;");
        profitChange.setText("+18.7%");
        profitChange.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-font-weight: bold;");
        marginChange.setText("+4.2%");
        marginChange.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-font-weight: bold;");
    }

    private static String kpiIcon(String label) {
        switch (label) {
            case "Total Revenue": return "$";
            case "Total Expenses": return "$";
            case "Net Profit": return "▲";
            case "Profit Margin": return "%";
            default: return "•";
        }
    }

    private static BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("");
        xAxis.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("");
        yAxis.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");
        yAxis.setUpperBound(12000000);
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefHeight(250);
        chart.setPrefWidth(700);
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setStyle("-fx-background-color: transparent;");

        chart.getStylesheets().add("data:text/css," +
                ".default-color0.chart-bar { -fx-bar-fill: #3b82f6; } " +
                ".default-color1.chart-bar { -fx-bar-fill: #64748b; } " +
                ".chart-legend { -fx-background-color: transparent; } " +
                ".chart-legend-item { -fx-text-fill: #ffffff; }");

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        double[] revenue = {4800000, 5200000, 6100000, 5800000, 7200000, 6900000, 8100000, 7800000, 9200000, 8800000, 10500000, 9800000};
        double[] expenses = {3200000, 3500000, 3800000, 3400000, 4200000, 3900000, 4600000, 4300000, 5100000, 4800000, 5800000, 5200000};
        for (int i = 0; i < months.length; i++) {
            revenueSeries.getData().add(new XYChart.Data<>(months[i], revenue[i]));
        }
        chart.getData().add(revenueSeries);

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        for (int i = 0; i < months.length; i++) {
            expenseSeries.getData().add(new XYChart.Data<>(months[i], expenses[i]));
        }
        chart.getData().add(expenseSeries);
        return chart;
    }

    private static PieChart createPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setPrefHeight(200);
        pieChart.setPrefWidth(320);
        pieChart.setLegendVisible(true);
        pieChart.setAnimated(true);
        pieChart.setStyle("-fx-background-color: transparent;");
        pieChart.getData().addAll(
            new PieChart.Data("Payroll (38%)", 38),
            new PieChart.Data("Operations (22%)", 22),
            new PieChart.Data("Marketing (15%)", 15),
            new PieChart.Data("R&D (14%)", 14),
            new PieChart.Data("Infrastructure (11%)", 11)
        );
        pieChart.getStylesheets().add("data:text/css," +
                ".default-color0.chart-pie { -fx-pie-color: #1d4ed8; } " +
                ".default-color1.chart-pie { -fx-pie-color: #2563EB; } " +
                ".default-color2.chart-pie { -fx-pie-color: #3b82f6; } " +
                ".default-color3.chart-pie { -fx-pie-color: #60a5fa; } " +
                ".default-color4.chart-pie { -fx-pie-color: #93c5fd; } " +
                ".chart-pie-label { -fx-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold; } " +
                ".chart-legend { -fx-background-color: transparent; } " +
                ".chart-legend-item { -fx-text-fill: #ffffff; }");
        return pieChart;
    }

    private static TableView<Transaction> createTransactionsTable() {
        TableView<Transaction> table = new TableView<>();
        table.setPrefHeight(250);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> nameCol = new TableColumn<>("Transaction");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(280);
        nameCol.setStyle("-fx-text-fill: #ffffff;");
        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        categoryCol.setStyle("-fx-text-fill: #6a8aaa;");
        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equalsIgnoreCase("cleared")) setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                else if (item.equalsIgnoreCase("pending")) setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: #6a8aaa;");
            }
        });
        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(150);
        amountCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.startsWith("+")) setStyle("-fx-text-fill: #34D399; -fx-font-weight: bold;");
                else if (item.startsWith("-")) setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
            }
        });
        table.getColumns().addAll(nameCol, categoryCol, statusCol, amountCol);

        table.setRowFactory(tv -> new TableRow<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("even-row", "odd-row");
                if (item == null || empty) return;
                getStyleClass().add(getIndex() % 2 == 0 ? "even-row" : "odd-row");
            }
        });
        return table;
    }

    private static List<Transaction> generateRealisticTransactions() {
        List<Transaction> list = new ArrayList<>();
        list.add(new Transaction("AWS Infrastructure — Q4 Invoice", "Infrastructure", "cleared", "-$284,500"));
        list.add(new Transaction("Enterprise License — Salesforce", "Operations", "cleared", "-$142,000"));
        list.add(new Transaction("Client Payment — Meridian Corp", "Revenue", "cleared", "+$1,840,000"));
        list.add(new Transaction("Contractor Payroll — December", "Payroll", "cleared", "-$618,300"));
        list.add(new Transaction("Client Payment — Vantage Holdings", "Revenue", "pending", "+$920,000"));
        list.add(new Transaction("Office Lease — HQ Q1 2025", "Operations", "cleared", "-$385,000"));
        list.add(new Transaction("Digital Ad Spend — December", "Marketing", "cleared", "-$224,800"));
        list.add(new Transaction("Client Payment — Blackstone Partners", "Revenue", "cleared", "+$2,340,000"));
        return list;
    }

    public static class Transaction {
        private String name;
        private String category;
        private String status;
        private String amount;
        public Transaction(String name, String category, String status, String amount) {
            this.name = name;
            this.category = category;
            this.status = status;
            this.amount = amount;
        }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getAmount() { return amount; }
    }
}
