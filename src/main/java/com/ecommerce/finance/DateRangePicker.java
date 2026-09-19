package com.ecommerce.finance;
import java.sql.SQLException;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateRangePicker {

    private Window owner;
    private Stage popupStage;
    private LocalDate startDate;
    private LocalDate endDate;
    private YearMonth currentMonth;
    private YearMonth nextMonth;

    private Label startLabel;
    private Label endLabel;
    private Label monthYearLabel1;
    private Label monthYearLabel2;

    private GridPane monthGrid1;
    private GridPane monthGrid2;

    private Runnable onDateRangeSelected;
    private String lastSelectedRange = "Select Date Range";

    public DateRangePicker() {
        this.startDate = null;
        this.endDate = null;
        this.currentMonth = YearMonth.now();
        this.nextMonth = currentMonth.plusMonths(1);
    }

    public void setOnDateRangeSelected(Runnable callback) {
        this.onDateRangeSelected = callback;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public void setStartDate(LocalDate start) {
        this.startDate = start;
        updateLabels();
    }

    public void setEndDate(LocalDate end) {
        this.endDate = end;
        updateLabels();
    }

    public void showPicker(double x, double y, Window ownerWindow) {
        this.owner = ownerWindow;
        VBox root = buildContent();
        Scene scene = new Scene(root);
        popupStage = new Stage();
        popupStage.initOwner(owner);
        popupStage.initStyle(StageStyle.UNDECORATED);
        popupStage.setScene(scene);
        popupStage.setX(x);
        popupStage.setY(y);
        popupStage.show();
    }

    public void hide() {
        if (popupStage != null) {
            popupStage.hide();
        }
    }

    private VBox buildContent() {
        VBox root = new VBox(10);
        root.setStyle("-fx-background-color: #1a1e26; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 4);");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        startLabel = new Label(startDate != null ? startDate.toString() : "Start");
        startLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
        endLabel = new Label(endDate != null ? endDate.toString() : "End");
        endLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label arrow = new Label("→");
        arrow.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 16px;");
        HBox rangeBox = new HBox(5, startLabel, arrow, endLabel);
        rangeBox.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(rangeBox);

        // Navigation
        HBox navBox = new HBox(20);
        navBox.setAlignment(Pos.CENTER);
        Button prevBtn = new Button("◀");
        prevBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-font-size: 16px;");
        prevBtn.setOnAction(e -> navigate(-1));
        Button nextBtn = new Button("▶");
        nextBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-font-size: 16px;");
        nextBtn.setOnAction(e -> navigate(1));
        monthYearLabel1 = new Label(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthYearLabel1.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 15px;");
        monthYearLabel2 = new Label(nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthYearLabel2.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 15px;");
        HBox monthLabels = new HBox(30, monthYearLabel1, monthYearLabel2);
        monthLabels.setAlignment(Pos.CENTER);
        navBox.getChildren().addAll(prevBtn, monthLabels, nextBtn);

        // Grids
        HBox grids = new HBox(20);
        grids.setAlignment(Pos.CENTER);
        monthGrid1 = buildMonthGrid(currentMonth);
        monthGrid2 = buildMonthGrid(nextMonth);
        grids.getChildren().addAll(monthGrid1, monthGrid2);

        // Footer
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6a8aaa; -fx-border-color: #4a6a8a; -fx-border-radius: 4; -fx-padding: 5 15;");
        clearBtn.setOnAction(e -> clearSelection());
        Button doneBtn = new Button("Done");
        doneBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 4;");
        doneBtn.setOnAction(e -> {
            if (onDateRangeSelected != null) onDateRangeSelected.run();
            hide();
        });
        footer.getChildren().addAll(clearBtn, doneBtn);

        root.getChildren().addAll(header, navBox, grids, footer);
        return root;
    }

    private void navigate(int step) {
        currentMonth = currentMonth.plusMonths(step);
        nextMonth = currentMonth.plusMonths(1);
        updateGrids();
        monthYearLabel1.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthYearLabel2.setText(nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }

    private void updateGrids() {
        monthGrid1.getChildren().clear();
        monthGrid2.getChildren().clear();
        buildMonthGrid(currentMonth, monthGrid1);
        buildMonthGrid(nextMonth, monthGrid2);
    }

    private GridPane buildMonthGrid(YearMonth ym) {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);
        buildMonthGrid(ym, grid);
        return grid;
    }

    private void buildMonthGrid(YearMonth ym, GridPane grid) {
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label day = new Label(dayNames[i]);
            day.setStyle("-fx-text-fill: #4a6a8a; -fx-font-weight: bold; -fx-font-size: 11px;");
            grid.add(day, i, 0);
        }

        LocalDate first = ym.atDay(1);
        int startOffset = first.getDayOfWeek().getValue() - 1;
        int daysInMonth = ym.lengthOfMonth();

        int row = 1;
        int col = startOffset;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = ym.atDay(day);
            Button btn = new Button(String.valueOf(day));
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-min-width: 30; -fx-min-height: 30;");
            btn.setOnAction(e -> handleDateClick(date));
            if (isDateInRange(date)) {
                btn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%;");
            } else if (date.equals(LocalDate.now())) {
                btn.setStyle("-fx-background-color: #0d1b2a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%;");
            }
            btn.setOnMouseEntered(e -> {
                if (!isDateInRange(date) && !date.equals(LocalDate.now())) {
                    btn.setStyle("-fx-background-color: #1a3a5a; -fx-background-radius: 50%;");
                }
            });
            btn.setOnMouseExited(e -> {
                if (!isDateInRange(date) && !date.equals(LocalDate.now())) {
                    btn.setStyle("-fx-background-color: transparent;");
                } else if (date.equals(LocalDate.now())) {
                    btn.setStyle("-fx-background-color: #0d1b2a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%;");
                }
            });
            grid.add(btn, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private void handleDateClick(LocalDate date) {
        if (startDate == null || (startDate != null && endDate != null)) {
            startDate = date;
            endDate = null;
        } else if (startDate != null && endDate == null) {
            if (date.isBefore(startDate)) {
                endDate = startDate;
                startDate = date;
            } else {
                endDate = date;
            }
        }
        updateLabels();
        updateGrids();
    }

    private boolean isDateInRange(LocalDate date) {
        if (startDate == null) return false;
        if (endDate == null) return date.equals(startDate);
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    private void updateLabels() {
        startLabel.setText(startDate != null ? startDate.toString() : "Start");
        endLabel.setText(endDate != null ? endDate.toString() : "End");
    }

    private void clearSelection() {
        startDate = null;
        endDate = null;
        updateLabels();
        updateGrids();
    }
}