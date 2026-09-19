package com.ecommerce.finance;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportController {

    public static Scene createReportScene(Stage stage, List<DashboardController.Transaction> transactions) {
        // ---------- Main container ----------
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // ---------- Header ----------
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 25 30 20 30; -fx-background-color: #1a1e26;");
        Label title = new Label("Financial Report");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            Scene dashScene = DashboardController.createDashboardScene(stage);
            stage.setScene(dashScene);
            stage.setTitle("Finance Dashboard");
            stage.setMaximized(true);
        });
        header.getChildren().addAll(title, dateLabel, spacer, backBtn);

        // ---------- Scrollable content ----------
        VBox content = new VBox(25);
        content.setStyle("-fx-padding: 25 30 30 30;");

        // ----- KPI Cards -----
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(20);
        kpiGrid.setVgap(15);

        double totalRevenue = 0;
        double totalExpenses = 0;
        int count = transactions.size();
        for (DashboardController.Transaction t : transactions) {
            String amtStr = t.getAmount().replaceAll("[$,]", "");
            if (amtStr.startsWith("+")) amtStr = amtStr.substring(1);
            else if (amtStr.startsWith("-")) amtStr = amtStr.substring(1);
            try {
                double value = Double.parseDouble(amtStr);
                if (t.getCategory().equals("Revenue")) {
                    totalRevenue += value;
                } else {
                    totalExpenses += value;
                }
            } catch (NumberFormatException ignored) {}
        }
        double netProfit = totalRevenue - totalExpenses;

        kpiGrid.add(createKpiCard("Total Revenue", String.format("$%,.2f", totalRevenue), "+12.5%", "#10B981"), 0, 0);
        kpiGrid.add(createKpiCard("Total Expenses", String.format("$%,.2f", totalExpenses), "-3.2%", "#EF4444"), 1, 0);
        kpiGrid.add(createKpiCard("Net Profit", String.format("$%,.2f", netProfit), "+18.7%", "#10B981"), 2, 0);
        kpiGrid.add(createKpiCard("Transactions", String.valueOf(count), "FY 2024", "#2563EB"), 3, 0);

        // ----- Charts Row -----
        HBox chartRow = new HBox(20);
        chartRow.setAlignment(Pos.CENTER);

        // Bar Chart
        VBox barContainer = new VBox(10);
        barContainer.getStyleClass().add("app-glass-panel");
        barContainer.setPrefWidth(700);
        Label barTitle = new Label("Revenue vs. Expenses (Monthly)");
        barTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        BarChart<String, Number> barChart = createMonthlyBarChart();
        barContainer.getChildren().addAll(barTitle, barChart);

        // Pie Chart – with fixed white labels
        VBox pieContainer = new VBox(10);
        pieContainer.getStyleClass().add("app-glass-panel");
        pieContainer.setPrefWidth(380);
        Label pieTitle = new Label("Expense Breakdown");
        pieTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        PieChart pieChart = createExpensePieChart();
        pieContainer.getChildren().addAll(pieTitle, pieChart);

        chartRow.getChildren().addAll(barContainer, pieContainer);

        // ----- Transactions Table -----
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");
        Label tableTitle = new Label("All Transactions");
        tableTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        TableView<DashboardController.Transaction> table = createTransactionTable();
        table.getItems().setAll(transactions);
        tableContainer.getChildren().addAll(tableTitle, table);

        content.getChildren().addAll(kpiGrid, chartRow, tableContainer);

        // Wrap content in ScrollPane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.getChildren().addAll(header, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(DashboardController.class.getResource("/styles.css").toExternalForm());
        AnimationHelper.fadeSlideIn(barContainer, 100);
        AnimationHelper.fadeSlideIn(pieContainer, 180);
        AnimationHelper.fadeSlideIn(tableContainer, 260);

        return scene;
    }

    // ---------- Helper: KPI Card ----------
    private static VBox createKpiCard(String label, String value, String change, String color) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #0C0F16; -fx-background-radius: 12; -fx-padding: 18 22; -fx-border-color: #1a1e26; -fx-border-radius: 12; -fx-border-width: 1;");
        card.setPrefWidth(220);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px;");

        HBox valueRow = new HBox(12);
        valueRow.setAlignment(Pos.CENTER_LEFT);
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
        Label changeText = new Label(change);
        changeText.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        valueRow.getChildren().addAll(valueText, changeText);

        card.getChildren().addAll(labelText, valueRow);
        return card;
    }

    // ---------- Bar Chart ----------
    private static BarChart<String, Number> createMonthlyBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("");
        xAxis.setStyle("-fx-text-fill: #6a8aaa;");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("");
        yAxis.setStyle("-fx-text-fill: #6a8aaa;");
        yAxis.setUpperBound(12000000);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefHeight(250);
        chart.setPrefWidth(660);
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setStyle("-fx-background-color: transparent;");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        double[] revenue = {4800000, 5200000, 6100000, 5800000, 7200000, 6900000, 8100000, 7800000, 9200000, 8800000, 10500000, 9800000};
        double[] expenses = {3200000, 3500000, 3800000, 3400000, 4200000, 3900000, 4600000, 4300000, 5100000, 4800000, 5800000, 5200000};

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
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

        chart.getStylesheets().add("data:text/css," +
            ".default-color0.chart-bar { -fx-bar-fill: #2563EB; } " +
            ".default-color1.chart-bar { -fx-bar-fill: #EF4444; }");

        return chart;
    }

    // ---------- Pie Chart with white labels ----------
    private static PieChart createExpensePieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setPrefHeight(220);
        pieChart.setPrefWidth(340);
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

        // Force pie chart labels and legend text to be white
        pieChart.getStylesheets().add("data:text/css," +
            ".default-color0.chart-pie { -fx-pie-color: #2563EB; } " +
            ".default-color1.chart-pie { -fx-pie-color: #10B981; } " +
            ".default-color2.chart-pie { -fx-pie-color: #F59E0B; } " +
            ".default-color3.chart-pie { -fx-pie-color: #8B5CF6; } " +
            ".default-color4.chart-pie { -fx-pie-color: #EF4444; } " +
            ".chart-pie-label { -fx-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold; } " +
            ".chart-legend { -fx-background-color: #1a1e26; } " +
            ".chart-legend-item { -fx-text-fill: #ffffff; -fx-font-size: 13px; }");

        return pieChart;
    }

    // ---------- Transaction Table ----------
    private static TableView<DashboardController.Transaction> createTransactionTable() {
        TableView<DashboardController.Transaction> table = new TableView<>();
        table.setPrefHeight(300);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DashboardController.Transaction, String> nameCol = new TableColumn<>("Transaction");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(280);
        nameCol.setStyle("-fx-text-fill: #ffffff;");

        TableColumn<DashboardController.Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        categoryCol.setStyle("-fx-text-fill: #6a8aaa;");

        TableColumn<DashboardController.Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-text-fill: #6a8aaa;");

        TableColumn<DashboardController.Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(150);
        amountCol.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        table.getColumns().addAll(nameCol, categoryCol, statusCol, amountCol);

        table.setRowFactory(tv -> new javafx.scene.control.TableRow<DashboardController.Transaction>() {
            @Override
            protected void updateItem(DashboardController.Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #1a1e26;");
                    } else {
                        setStyle("-fx-background-color: #0C0F16;");
                    }
                }
            }
        });

        return table;
    }
}