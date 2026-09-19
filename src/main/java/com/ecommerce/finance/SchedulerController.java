package com.ecommerce.finance;

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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulerController {

    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static AtomicBoolean running = new AtomicBoolean(true);
    private static List<ScheduledReport> scheduledReports = new ArrayList<>();
    private static TableView<ScheduledReport> reportTable;
    private static Label readOnlyLabel;
    private static Button addBtn, editBtn, modifyBtn, deleteBtn, clearBtn;

    private static final String[] REPORT_TYPES = {"Sales Report", "Margin Analysis", "KPI Metrics", "VAT Summary", "Anomaly Report"};
    private static final String[] INTERVALS = {"Daily", "Weekly", "Monthly"};
    private static final String[] FORMATS = {"CSV", "PDF"};

    public static Scene createSchedulerScene(Stage stage) {
        // ---------- Determine admin status ----------
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        System.out.println("[Scheduler] isAdmin = " + isAdmin);  // DEBUG: check console

        // ---------- Build sidebar ----------
        VBox sidebar = SidebarHelper.buildSidebar(stage, "Scheduler");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color: #0C0F16; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // ---------- Header ----------
        HBox headerBar = new HBox(15);
        headerBar.setStyle("-fx-background-color: #0C0F16; -fx-padding: 20 30 15 30;");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label welcomeLabel = new Label("Cron-Based Report Scheduler");
        welcomeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label subText = new Label("Automated background generation of financial reports.");
        subText.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
        headerText.getChildren().addAll(welcomeLabel, subText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusLabel = new Label("● Live");
        statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        // Read-only label – shown only for employees
        readOnlyLabel = new Label("READ-ONLY MODE");
        readOnlyLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        readOnlyLabel.setVisible(!isAdmin);
        readOnlyLabel.setManaged(!isAdmin);

        headerBar.getChildren().addAll(headerText, spacer, statusLabel, readOnlyLabel);

        VBox contentWrapper = new VBox(20);
        contentWrapper.setStyle("-fx-padding: 25 30 30 30;");

        // ---------- Add Schedule Panel ----------
        VBox addPanel = new VBox(12);
        addPanel.getStyleClass().add("app-glass-panel");
        Label addTitle = new Label("Schedule New Report");
        addTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Region addUnderline = new Region();
        addUnderline.setStyle("-fx-background-color: #2563EB; -fx-pref-height: 2; -fx-pref-width: 50;");
        VBox addTitleBox = new VBox(5);
        addTitleBox.getChildren().addAll(addTitle, addUnderline);

        GridPane addGrid = new GridPane();
        addGrid.setHgap(20);
        addGrid.setVgap(12);
        addGrid.setStyle("-fx-padding: 10 0 0 0;");
        Label typeLabel = new Label("Report Type");
        typeLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll(REPORT_TYPES);
        reportTypeCombo.setValue(REPORT_TYPES[0]);
        reportTypeCombo.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 6; -fx-background-radius: 4;");
        Label intervalLabel = new Label("Interval");
        intervalLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        ComboBox<String> intervalCombo = new ComboBox<>();
        intervalCombo.getItems().addAll(INTERVALS);
        intervalCombo.setValue(INTERVALS[0]);
        intervalCombo.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 6; -fx-background-radius: 4;");
        Label timeLabel = new Label("Time (HH:mm)");
        timeLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        TextField timeField = new TextField("09:00");
        timeField.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 6; -fx-background-radius: 4;");
        Label formatLabel = new Label("Format");
        formatLabel.setStyle("-fx-text-fill: #6a8aaa; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';");
        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll(FORMATS);
        formatCombo.setValue(FORMATS[0]);
        formatCombo.setStyle("-fx-background-color: #0C0F16; -fx-text-fill: #ffffff; -fx-padding: 6; -fx-background-radius: 4;");

        addGrid.add(typeLabel, 0, 0); addGrid.add(reportTypeCombo, 0, 1);
        addGrid.add(intervalLabel, 1, 0); addGrid.add(intervalCombo, 1, 1);
        addGrid.add(timeLabel, 2, 0); addGrid.add(timeField, 2, 1);
        addGrid.add(formatLabel, 3, 0); addGrid.add(formatCombo, 3, 1);

        HBox addButtonBox = new HBox(15);
        addButtonBox.setAlignment(Pos.CENTER_LEFT);
        addButtonBox.setStyle("-fx-padding: 10 0 0 0;");
        addBtn = new Button("Add Schedule");
        addBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-font-family: 'Segoe UI';");
        addBtn.setOnAction(e -> {
            if (!SessionManager.getInstance().isAdmin()) {
                showAlert("Access Denied", "Only administrators can add schedules.");
                return;
            }
            String type = reportTypeCombo.getValue();
            String interval = intervalCombo.getValue();
            String time = timeField.getText().trim();
            String format = formatCombo.getValue();
            if (time.isEmpty() || !time.matches("\\d{2}:\\d{2}")) {
                showAlert("Invalid Time", "Please enter time in HH:mm format.");
                return;
            }
            ScheduledReport report = new ScheduledReport(
                UUID.randomUUID().toString(), type, interval, time, format,
                LocalDateTime.now(), null, true
            );
            scheduledReports.add(report);
            reportTable.getItems().add(report);
        });
        clearBtn = new Button("Clear All");
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6a8aaa; -fx-border-color: #4a6a8a; -fx-border-radius: 6; -fx-padding: 8 20; -fx-font-family: 'Segoe UI';");
        clearBtn.setOnAction(e -> {
            if (!SessionManager.getInstance().isAdmin()) {
                showAlert("Access Denied", "Only administrators can clear all schedules.");
                return;
            }
            scheduledReports.clear();
            reportTable.getItems().clear();
        });
        addButtonBox.getChildren().addAll(addBtn, clearBtn);
        addPanel.getChildren().addAll(addTitleBox, addGrid, addButtonBox);

        // ---------- Reports Table ----------
        VBox tableContainer = new VBox(10);
        tableContainer.getStyleClass().add("app-glass-panel");

        Label tableTitle = new Label("Scheduled Reports");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Region tableUnderline = new Region();
        tableUnderline.setStyle("-fx-background-color: #2563EB; -fx-pref-height: 2; -fx-pref-width: 50;");
        VBox tableTitleBox = new VBox(5);
        tableTitleBox.getChildren().addAll(tableTitle, tableUnderline);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-padding: 10 0 5 0;");
        editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button");
        modifyBtn = new Button("Modify");
        modifyBtn.getStyleClass().add("action-button");
        deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-danger");
        Button printBtn = new Button("Print");
        printBtn.getStyleClass().add("action-button");

        reportTable = createReportTable();
        reportTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        reportTable.setFocusTraversable(true);

        // Seed sample data
        if (scheduledReports.isEmpty()) {
            scheduledReports.add(new ScheduledReport("SCH-001", "Sales Report", "Daily", "09:00", "CSV", LocalDateTime.now(), null, true));
            scheduledReports.add(new ScheduledReport("SCH-002", "Margin Analysis", "Weekly", "17:00", "PDF", LocalDateTime.now(), null, true));
            scheduledReports.add(new ScheduledReport("SCH-003", "VAT Summary", "Monthly", "23:59", "CSV", LocalDateTime.now(), null, true));
        }
        reportTable.getItems().setAll(scheduledReports);

        // Action handlers (with admin checks)
        editBtn.setOnAction(e -> {
            if (!SessionManager.getInstance().isAdmin()) {
                showAlert("Access Denied", "Only administrators can edit schedules.");
                return;
            }
            ScheduledReport selected = reportTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("No selection", "Please select a report to edit.");
                return;
            }
            TextInputDialog dialog = new TextInputDialog(selected.getReportType());
            dialog.setTitle("Edit Report");
            dialog.setHeaderText("Edit report type for " + selected.getId());
            dialog.setContentText("New report type:");
            dialog.showAndWait().ifPresent(newType -> {
                selected.setReportType(newType);
                reportTable.refresh();
            });
        });

        modifyBtn.setOnAction(e -> {
            if (!SessionManager.getInstance().isAdmin()) {
                showAlert("Access Denied", "Only administrators can modify schedules.");
                return;
            }
            ScheduledReport selected = reportTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("No selection", "Please select a report to modify.");
                return;
            }
            selected.setActive(!selected.isActive());
            reportTable.refresh();
        });

        deleteBtn.setOnAction(e -> {
            if (!SessionManager.getInstance().isAdmin()) {
                showAlert("Access Denied", "Only administrators can delete schedules.");
                return;
            }
            ScheduledReport selected = reportTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("No selection", "Please select a report to delete.");
                return;
            }
            scheduledReports.remove(selected);
            reportTable.getItems().remove(selected);
        });

        printBtn.setOnAction(e -> {
            // Print is always allowed
            System.out.println("Printing report schedule...");
            for (ScheduledReport r : reportTable.getItems()) {
                System.out.println(r.getId() + " | " + r.getReportType() + " | " + r.getInterval());
            }
            showAlert("Print", "Table content printed to console.");
        });

        toolbar.getChildren().addAll(editBtn, modifyBtn, deleteBtn, printBtn);
        tableContainer.getChildren().addAll(tableTitleBox, toolbar, reportTable);

        contentWrapper.getChildren().addAll(addPanel, tableContainer);
        mainContent.getChildren().addAll(headerBar, contentWrapper);

        HBox root = new HBox(sidebar, mainContent);
        root.setStyle("-fx-background-color: #0C0F16;");
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(SchedulerController.class.getResource("/styles.css").toExternalForm());

        // ---------- Apply permissions (disable buttons for employees) ----------
        addBtn.setDisable(!isAdmin);
        clearBtn.setDisable(!isAdmin);
        editBtn.setDisable(!isAdmin);
        modifyBtn.setDisable(!isAdmin);
        deleteBtn.setDisable(!isAdmin);

        // ---------- Start scheduler background thread ----------
        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            running.set(true);
            scheduler.scheduleAtFixedRate(() -> {
                if (!running.get()) return;
                LocalDateTime now = LocalDateTime.now();
                for (ScheduledReport report : scheduledReports) {
                    if (!report.isActive()) continue;
                    if (report.getNextRunTime() == null || report.getNextRunTime().isBefore(now) || report.getNextRunTime().equals(now)) {
                        System.out.println("Generating " + report.getReportType() + " report in " + report.getFormat() + " format.");
                        LocalDateTime next = calculateNextRun(report);
                        report.setNextRunTime(next);
                        report.setLastRunTime(now);
                    }
                }
            }, 0, 1, TimeUnit.MINUTES);
        }

        AnimationHelper.fadeSlideIn(addPanel, 100);
        AnimationHelper.fadeSlideIn(tableContainer, 260);

        return scene;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogHelper.styleAlert(alert);
        alert.showAndWait();
    }

    private static LocalDateTime calculateNextRun(ScheduledReport report) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime time = LocalTime.parse(report.getTime());
        LocalDateTime next = LocalDateTime.of(now.toLocalDate(), time);
        if (next.isBefore(now)) next = next.plusDays(1);
        switch (report.getInterval()) {
            case "Daily":   return next.plusDays(1);
            case "Weekly":  return next.plusWeeks(1);
            case "Monthly": return next.plusMonths(1);
            default:        return next.plusDays(1);
        }
    }

    public static void shutdownScheduler() {
        running.set(false);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Scheduler did not terminate gracefully.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static TableView<ScheduledReport> createReportTable() {
        TableView<ScheduledReport> table = new TableView<>();
        table.setPrefHeight(250);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setRowFactory(tv -> new TableRow<ScheduledReport>() {
            @Override
            protected void updateItem(ScheduledReport item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: #1a1e26;" : "-fx-background-color: #0C0F16;");
            }
        });

        TableColumn<ScheduledReport, String> idCol = new TableColumn<>("Schedule ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ScheduledReport, String> typeCol = new TableColumn<>("Report Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        typeCol.setPrefWidth(150);
        typeCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ScheduledReport, String> intervalCol = new TableColumn<>("Interval");
        intervalCol.setCellValueFactory(new PropertyValueFactory<>("interval"));
        intervalCol.setPrefWidth(100);
        intervalCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ScheduledReport, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(80);
        timeCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ScheduledReport, String> formatCol = new TableColumn<>("Format");
        formatCol.setCellValueFactory(new PropertyValueFactory<>("format"));
        formatCol.setPrefWidth(80);
        formatCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ScheduledReport, String> nextCol = new TableColumn<>("Next Run");
        nextCol.setCellValueFactory(new PropertyValueFactory<>("nextRunTimeFormatted"));
        nextCol.setPrefWidth(150);
        nextCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ScheduledReport, String> lastCol = new TableColumn<>("Last Run");
        lastCol.setCellValueFactory(new PropertyValueFactory<>("lastRunTimeFormatted"));
        lastCol.setPrefWidth(150);
        lastCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        TableColumn<ScheduledReport, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(80);
        activeCol.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");

        table.getColumns().addAll(idCol, typeCol, intervalCol, timeCol, formatCol, nextCol, lastCol, activeCol);
        return table;
    }

    public static class ScheduledReport {
        private String id;
        private String reportType;
        private String interval;
        private String time;
        private String format;
        private LocalDateTime nextRunTime;
        private LocalDateTime lastRunTime;
        private boolean active;

        public ScheduledReport(String id, String reportType, String interval, String time, String format,
                               LocalDateTime nextRunTime, LocalDateTime lastRunTime, boolean active) {
            this.id = id;
            this.reportType = reportType;
            this.interval = interval;
            this.time = time;
            this.format = format;
            this.nextRunTime = nextRunTime;
            this.lastRunTime = lastRunTime;
            this.active = active;
        }

        public String getId() { return id; }
        public String getReportType() { return reportType; }
        public String getInterval() { return interval; }
        public String getTime() { return time; }
        public String getFormat() { return format; }
        public LocalDateTime getNextRunTime() { return nextRunTime; }
        public LocalDateTime getLastRunTime() { return lastRunTime; }
        public boolean isActive() { return active; }
        public String getNextRunTimeFormatted() {
            return nextRunTime != null ? nextRunTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
        }
        public String getLastRunTimeFormatted() {
            return lastRunTime != null ? lastRunTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Never";
        }

        public void setId(String id) { this.id = id; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        public void setInterval(String interval) { this.interval = interval; }
        public void setTime(String time) { this.time = time; }
        public void setFormat(String format) { this.format = format; }
        public void setNextRunTime(LocalDateTime nextRunTime) { this.nextRunTime = nextRunTime; }
        public void setLastRunTime(LocalDateTime lastRunTime) { this.lastRunTime = lastRunTime; }
        public void setActive(boolean active) { this.active = active; }
    }
}