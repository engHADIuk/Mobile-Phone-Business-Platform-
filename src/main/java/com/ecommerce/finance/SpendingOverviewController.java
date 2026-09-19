package com.ecommerce.finance;

import com.ecommerce.finance.services.DatabaseManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shows where company money actually goes — real cost-of-goods-sold data
 * pulled from the live orders/products tables, broken down by category and
 * by month. Fully read-only, visible to both admins and employees.
 */
public class SpendingOverviewController {

    private static final String[] PALETTE = {"#1d4ed8", "#2563EB", "#3b82f6", "#60a5fa", "#93c5fd", "#bfdbfe"};

    public static Scene createSpendingScene(Stage stage) {
        VBox sidebar = SidebarHelper.buildSidebar(stage, "SpendingOverview");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Spending Overview");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI';");
        Label subText = new Label("Real cost-of-goods-sold breakdown, computed live from your order history.");
        subText.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        headerText.getChildren().addAll(welcomeLabel, subText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusLabel = new Label("● Live");
        statusLabel.getStyleClass().add("app-status-live");
        headerBar.getChildren().addAll(headerText, spacer, statusLabel);

        VBox contentWrapper = new VBox(20);
        contentWrapper.setStyle("-fx-padding: 25 30 30 30;");

        // ---------- LOAD REAL DATA ----------
        List<CategorySpend> categorySpends;
        LinkedHashMap<String, Double> monthlySpend;
        try {
            categorySpends = DatabaseManager.getInstance().loadSpendingByCategory();
            monthlySpend = DatabaseManager.getInstance().loadMonthlySpend();
        } catch (SQLException e) {
            e.printStackTrace();
            categorySpends = new ArrayList<>();
            monthlySpend = new LinkedHashMap<>();
        }

        double totalSpend = categorySpends.stream().mapToDouble(CategorySpend::getTotalCost).sum();
        String topCategory = categorySpends.isEmpty() ? "N/A" : categorySpends.get(0).getCategory();
        double avgMonthly = monthlySpend.isEmpty() ? 0 : totalSpend / monthlySpend.size();
        int categoriesTracked = categorySpends.size();

        // ---------- KPI CARDS ----------
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(20);
        kpiGrid.setVgap(15);
        kpiGrid.add(createKpiCard("Total Spend (COGS)", String.format("$%,.2f", totalSpend)), 0, 0);
        kpiGrid.add(createKpiCard("Top Spending Category", topCategory), 1, 0);
        kpiGrid.add(createKpiCard("Avg Monthly Spend", String.format("$%,.2f", avgMonthly)), 2, 0);
        kpiGrid.add(createKpiCard("Categories Tracked", String.valueOf(categoriesTracked)), 3, 0);

        // ---------- CHARTS ----------
        HBox chartRow = new HBox(20);
        chartRow.setStyle("-fx-padding: 5 0 0 0;");
        chartRow.setAlignment(Pos.CENTER_LEFT);

        VBox trendContainer = new VBox(10);
        trendContainer.getStyleClass().add("app-glass-panel");
        trendContainer.setPrefWidth(750);
        Label trendTitle = new Label("Monthly Spend Trend");
        trendTitle.getStyleClass().add("app-section-title");
        Label trendSub = new Label("Cost of goods sold, by month");
        trendSub.getStyleClass().add("app-section-sub");
        Region trendUnderline = new Region();
        trendUnderline.getStyleClass().add("app-underline");
        VBox trendTitleBox = new VBox(5, trendTitle, trendSub, trendUnderline);
        trendContainer.getChildren().addAll(trendTitleBox, createTrendChart(monthlySpend));

        VBox pieContainer = new VBox(10);
        pieContainer.getStyleClass().add("app-glass-panel");
        pieContainer.setPrefWidth(380);
        Label pieTitle = new Label("Spend by Category");
        pieTitle.getStyleClass().add("app-section-title");
        Label pieSub = new Label("Share of total cost of goods sold");
        pieSub.getStyleClass().add("app-section-sub");
        Region pieUnderline = new Region();
        pieUnderline.getStyleClass().add("app-underline");
        VBox pieTitleBox = new VBox(5, pieTitle, pieSub, pieUnderline);
        pieContainer.getChildren().addAll(pieTitleBox, createCategoryPieChart(categorySpends, totalSpend));

        chartRow.getChildren().addAll(trendContainer, pieContainer);

        // ---------- TABLE ----------
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");
        Label tableTitle = new Label("Category Breakdown");
        tableTitle.getStyleClass().add("app-section-title");
        Region tableUnderline = new Region();
        tableUnderline.getStyleClass().add("app-underline");
        VBox tableTitleBox = new VBox(5, tableTitle, tableUnderline);

        TableView<CategorySpend> table = createCategoryTable();
        table.getItems().setAll(categorySpends);
        tableContainer.getChildren().addAll(tableTitleBox, table);

        contentWrapper.getChildren().addAll(kpiGrid, chartRow, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(SpendingOverviewController.class.getResource("/styles.css").toExternalForm());

        AnimationHelper.staggerIn(kpiGrid);
        AnimationHelper.fadeSlideIn(trendContainer, 200);
        AnimationHelper.fadeSlideIn(pieContainer, 260);
        AnimationHelper.fadeSlideIn(tableContainer, 340);

        return scene;
    }

    private static VBox createKpiCard(String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("app-kpi-glass");
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #8aa3c4; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI';");
        card.getChildren().addAll(labelText, valueText);
        return card;
    }

    private static BarChart<String, Number> createTrendChart(Map<String, Double> monthlySpend) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("");
        xAxis.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("");
        yAxis.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefHeight(250);
        chart.setPrefWidth(700);
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setStyle("-fx-background-color: transparent;");
        chart.getStylesheets().add("data:text/css," +
                ".default-color0.chart-bar { -fx-bar-fill: #3b82f6; }");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : monthlySpend.entrySet()) {
            series.getData().add(new XYChart.Data<>(formatMonth(entry.getKey()), entry.getValue()));
        }
        chart.getData().add(series);
        return chart;
    }

    private static String formatMonth(String yyyyMM) {
        try {
            YearMonth ym = YearMonth.parse(yyyyMM);
            return ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.US);
        } catch (Exception e) {
            return yyyyMM;
        }
    }

    private static PieChart createCategoryPieChart(List<CategorySpend> categorySpends, double totalSpend) {
        PieChart pieChart = new PieChart();
        pieChart.setPrefHeight(220);
        pieChart.setPrefWidth(320);
        pieChart.setLegendVisible(true);
        pieChart.setAnimated(true);
        pieChart.setStyle("-fx-background-color: transparent;");

        StringBuilder css = new StringBuilder(
                ".chart-pie-label { -fx-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold; } " +
                ".chart-legend { -fx-background-color: transparent; } " +
                ".chart-legend-item { -fx-text-fill: #ffffff; }");

        int i = 0;
        for (CategorySpend cs : categorySpends) {
            double pct = totalSpend > 0 ? (cs.getTotalCost() / totalSpend) * 100 : 0;
            pieChart.getData().add(new PieChart.Data(String.format("%s (%.0f%%)", cs.getCategory(), pct), cs.getTotalCost()));
            css.append(String.format(".default-color%d.chart-pie { -fx-pie-color: %s; } ", i, PALETTE[i % PALETTE.length]));
            i++;
        }
        pieChart.getStylesheets().add("data:text/css," + css);
        return pieChart;
    }

    private static TableView<CategorySpend> createCategoryTable() {
        TableView<CategorySpend> table = new TableView<>();
        table.setPrefHeight(220);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CategorySpend, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(220);
        catCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<CategorySpend, Double> costCol = new TableColumn<>("Total Cost ($)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        costCol.setPrefWidth(180);
        costCol.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        TableColumn<CategorySpend, Integer> countCol = new TableColumn<>("Orders");
        countCol.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        countCol.setPrefWidth(120);
        countCol.setStyle("-fx-text-fill: #6a8aaa; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(catCol, costCol, countCol);

        table.setRowFactory(tv -> new TableRow<CategorySpend>() {
            @Override
            protected void updateItem(CategorySpend item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("even-row", "odd-row");
                if (item == null || empty) return;
                getStyleClass().add(getIndex() % 2 == 0 ? "even-row" : "odd-row");
            }
        });

        return table;
    }

    public static class CategorySpend {
        private String category;
        private double totalCost;
        private int orderCount;

        public CategorySpend(String category, double totalCost, int orderCount) {
            this.category = category;
            this.totalCost = Math.round(totalCost * 100.0) / 100.0;
            this.orderCount = orderCount;
        }

        public String getCategory() { return category; }
        public double getTotalCost() { return totalCost; }
        public int getOrderCount() { return orderCount; }

        public void setCategory(String category) { this.category = category; }
        public void setTotalCost(double totalCost) { this.totalCost = Math.round(totalCost * 100.0) / 100.0; }
        public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    }
}
