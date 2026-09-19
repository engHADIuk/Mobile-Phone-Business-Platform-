package com.ecommerce.finance;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SidebarHelper {

    public static VBox buildSidebar(Stage stage, String activePage) {
        VBox sidebar = new VBox(20);
        sidebar.setStyle("-fx-background-color: #0C0F16; -fx-pref-width: 260; -fx-padding: 25 15 20 15;");
        sidebar.setAlignment(Pos.TOP_CENTER);

        // Logo section — same shield-check glass badge as the login page
        VBox logoSection = new VBox(10);
        logoSection.setAlignment(Pos.CENTER);
        logoSection.setStyle("-fx-padding: 0 0 15 0;");
        HBox logo = createLogo();
        Region logoDivider = new Region();
        logoDivider.getStyleClass().add("app-underline");
        logoDivider.setStyle("-fx-pref-width: 60; -fx-max-width: 60;");
        logoSection.getChildren().addAll(logo, logoDivider);

        // Navigation wrapper
        VBox navWrapper = new VBox(8);
        navWrapper.setStyle("-fx-padding: 10 0 0 0;");

        Label navLabel = new Label("DASHBOARD");
        navLabel.setStyle("-fx-text-fill: #4a6a8a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 10 5; -fx-font-family: 'Segoe UI';");

        boolean isAdmin = SessionManager.getInstance().isAdmin();

        // ---- All buttons ----
        Button dashBtn = createNavButton("Dashboard", activePage.equals("Dashboard"));
        dashBtn.setOnAction(e -> {
            if (isAdmin) {
                Scene dashScene = DashboardController.createDashboardScene(stage);
                stage.setScene(dashScene);
            } else {
                Scene dashScene = EmployeeDashboardController.createEmployeeDashboardScene(stage);
                stage.setScene(dashScene);
            }
            stage.setTitle(isAdmin ? "Admin Dashboard" : "Employee Dashboard");
            stage.setMaximized(true);
        });

        Button marginBtn = createNavButton("Margin Analysis", activePage.equals("MarginAnalysis"));
        marginBtn.setOnAction(e -> {
            Scene marginScene = MarginAnalysisController.createMarginAnalysisScene(stage);
            stage.setScene(marginScene);
            stage.setTitle("Margin Analysis");
            stage.setMaximized(true);
        });

        Button kpiBtn = createNavButton("KPI Metrics", activePage.equals("KPIMetrics"));
        kpiBtn.setOnAction(e -> {
            Scene kpiScene = KPIController.createKPIScene(stage);
            stage.setScene(kpiScene);
            stage.setTitle("KPI Metrics");
            stage.setMaximized(true);
        });

        Button periodBtn = createNavButton("Period Analytics", activePage.equals("PeriodAnalytics"));
        periodBtn.setOnAction(e -> {
            Scene periodScene = PeriodOverPeriodController.createPeriodScene(stage);
            stage.setScene(periodScene);
            stage.setTitle("Period Analytics");
            stage.setMaximized(true);
        });

        Button vatBtn = createNavButton("VAT Liability", activePage.equals("VAT"));
        vatBtn.setOnAction(e -> {
            Scene vatScene = VATController.createVATScene(stage);
            stage.setScene(vatScene);
            stage.setTitle("VAT Liability");
            stage.setMaximized(true);
        });

        Button statementBtn = createNavButton("Account Statement", activePage.equals("Statements"));
        statementBtn.setOnAction(e -> {
            Scene statementScene = StatementController.createStatementScene(stage);
            stage.setScene(statementScene);
            stage.setTitle("Account Statement");
            stage.setMaximized(true);
        });

        Button spendingBtn = createNavButton("Spending Overview", activePage.equals("SpendingOverview"));
        spendingBtn.setOnAction(e -> {
            Scene spendingScene = SpendingOverviewController.createSpendingScene(stage);
            stage.setScene(spendingScene);
            stage.setTitle("Spending Overview");
            stage.setMaximized(true);
        });

        // ---- Admin-only buttons ----
        Button filterBtn = createNavButton("Filter Data", activePage.equals("FilterData"));
        filterBtn.setOnAction(e -> {
            Scene filterScene = FilterController.createFilterScene(stage);
            stage.setScene(filterScene);
            stage.setTitle("Data Filters");
            stage.setMaximized(true);
        });
        filterBtn.setVisible(isAdmin);

        Button exportBtn = createNavButton("Export Data", activePage.equals("ExportData"));
        exportBtn.setOnAction(e -> {
            Scene exportScene = ExportController.createExportScene(stage);
            stage.setScene(exportScene);
            stage.setTitle("Export Data");
            stage.setMaximized(true);
        });
        exportBtn.setVisible(isAdmin);

        Button clientInsightBtn = createNavButton("Client Insights", activePage.equals("ClientInsights"));
        clientInsightBtn.setOnAction(e -> {
            Scene insightScene = ClientInsightController.createClientInsightScene(stage);
            stage.setScene(insightScene);
            stage.setTitle("Client Insights");
            stage.setMaximized(true);
        });
        clientInsightBtn.setVisible(isAdmin);

        Button anomalyBtn = createNavButton("Anomaly Detection", activePage.equals("AnomalyDetection"));
        anomalyBtn.setOnAction(e -> {
            Scene anomalyScene = AnomalyDetectionController.createAnomalyScene(stage);
            stage.setScene(anomalyScene);
            stage.setTitle("Anomaly Detection");
            stage.setMaximized(true);
        });
        anomalyBtn.setVisible(isAdmin);

        Button schedulerBtn = createNavButton("Report Scheduler", activePage.equals("Scheduler"));
        schedulerBtn.setOnAction(e -> {
            Scene schedulerScene = SchedulerController.createSchedulerScene(stage);
            stage.setScene(schedulerScene);
            stage.setTitle("Report Scheduler");
            stage.setMaximized(true);
        });
        schedulerBtn.setVisible(isAdmin);

        Button faultBtn = createNavButton("Fault Tolerance", activePage.equals("FaultTolerance"));
        faultBtn.setOnAction(e -> {
            Scene faultScene = FaultTolerantController.createFaultTolerantScene(stage);
            stage.setScene(faultScene);
            stage.setTitle("Fault Tolerance");
            stage.setMaximized(true);
        });
        faultBtn.setVisible(isAdmin);

        // Admin section
        Label adminLabel = new Label("ADMIN");
        adminLabel.setStyle("-fx-text-fill: #4a6a8a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 20 0 10 5; -fx-font-family: 'Segoe UI';");
        adminLabel.setVisible(isAdmin);

        Button profileBtn = createNavButton("Mr. " + SessionManager.getInstance().getCurrentUser().toUpperCase(), false);
        profileBtn.setStyle("-fx-background-color: #1a1e26; -fx-text-fill: #ffffff; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-background-radius: 8; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");

        Label tierLabel = new Label(isAdmin ? "Private Tier" : "View-Only Access");
        tierLabel.setStyle("-fx-text-fill: #4a6a8a; -fx-font-size: 11px; -fx-padding: 0 0 0 15; -fx-font-family: 'Segoe UI';");

        Button logoutBtn = createNavButton("Logout", false);
        logoutBtn.setOnAction(e -> {
            try {
                SessionManager.getInstance().clearSession();
                Parent loginRoot = FXMLLoader.load(SidebarHelper.class.getResource("/Login.fxml"));
                Scene loginScene = new Scene(loginRoot);
                loginScene.getStylesheets().add(SidebarHelper.class.getResource("/styles.css").toExternalForm());
                stage.setScene(loginScene);
                stage.setTitle("Login");
                stage.setMaximized(false);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Build navigation menu with conditional visibility
        navWrapper.getChildren().addAll(
                navLabel,
                dashBtn,
                marginBtn,
                kpiBtn,
                periodBtn,
                vatBtn,
                statementBtn,
                spendingBtn,
                filterBtn,
                exportBtn,
                clientInsightBtn,
                anomalyBtn,
                schedulerBtn,
                faultBtn,
                adminLabel,
                profileBtn,
                tierLabel,
                logoutBtn
        );

        // ScrollPane wrapper
        ScrollPane scrollPane = new ScrollPane(navWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("sidebar-scroll-pane");
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        sidebar.getChildren().addAll(logoSection, scrollPane);
        return sidebar;
    }

    // ---------- Logo Creation (shield-check glass badge — matches the login page) ----------
    private static HBox createLogo() {
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.getStyleClass().add("login-stat-card");
        logoBox.setStyle("-fx-padding: 12 16;");

        StackPane badge = new StackPane();
        badge.getStyleClass().add("sidebar-badge");

        SVGPath shield = new SVGPath();
        shield.setContent("M0,-16 L14,-11 L14,-1 C14,9 7,16 0,19 C-7,16 -14,9 -14,-1 L-14,-11 Z");
        shield.setStyle("-fx-fill: rgba(255,255,255,0.95);");

        SVGPath check = new SVGPath();
        check.setContent("M-7,0 L-2,6 L9,-8");
        check.setStyle("-fx-fill: transparent; -fx-stroke: #1d3f8f; -fx-stroke-width: 2.4; -fx-stroke-line-cap: round; -fx-stroke-line-join: round;");

        badge.getChildren().addAll(shield, check);

        VBox wordmark = new VBox(1);
        Label brandName = new Label("VELMORA");
        brandName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 19px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', 'Segoe UI';");
        Label tagline = new Label("Premium Intelligence");
        tagline.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 10px; -fx-font-family: 'Segoe UI';");
        wordmark.getChildren().addAll(brandName, tagline);

        logoBox.getChildren().addAll(badge, wordmark);
        return logoBox;
    }

    private static Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefWidth(180);
        btn.getStyleClass().add("sidebar-button");
        if (active) {
            btn.getStyleClass().add("sidebar-button-active");
        }
        return btn;
    }
}