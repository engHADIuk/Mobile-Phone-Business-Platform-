package uk.ac.brunel.delivery.util;

import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import uk.ac.brunel.delivery.model.DeliveryStatus;

public final class Theme {
    public static final String BG = "#080909";
    public static final String PANEL = "#141515";
    public static final String PANEL_LIGHT = "#1B1C1C";
    public static final String LINE = "#292B2B";
    public static final String TEXT = "#F1F1EF";
    public static final String MUTED = "#797C7B";
    public static final String GOLD = "#D5C38B";
    public static final String GREEN = "#3BD5A0";
    public static final String BLUE = "#4CB7F6";
    public static final String AMBER = "#ECA91C";
    public static final String RED = "#EF5B4E";

    private Theme() {
    }

    public static void stylePrimary(ButtonBase button) {
        button.setStyle("-fx-background-color: " + GOLD + "; -fx-text-fill: #151515; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18;");
    }

    public static void styleLight(ButtonBase button) {
        button.setStyle("-fx-background-color: #F0F0EB; -fx-text-fill: #151515; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18;");
    }

    public static void styleSecondary(ButtonBase button) {
        button.setStyle("-fx-background-color: #111212; -fx-text-fill: " + TEXT + "; "
                + "-fx-font-weight: bold; -fx-border-color: #343636; -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-padding: 9 16;");
    }

    public static void styleDanger(ButtonBase button) {
        button.setStyle("-fx-background-color: rgba(239,91,78,0.16); -fx-text-fill: " + RED + "; "
                + "-fx-font-weight: bold; -fx-border-color: rgba(239,91,78,0.35); -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-padding: 9 16;");
    }

    public static void styleInput(TextInputControl input) {
        input.setStyle("-fx-background-color: #181919; -fx-text-fill: " + TEXT + "; "
                + "-fx-prompt-text-fill: #656868; -fx-border-color: #2B2D2D; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 12;");
    }

    public static void styleCombo(ComboBoxBase<?> combo) {
        combo.setStyle("-fx-background-color: #181919; -fx-mark-color: " + GOLD + "; "
                + "-fx-border-color: #2B2D2D; -fx-border-radius: 8; -fx-background-radius: 8;");
    }

    public static void styleTable(TableView<?> table) {
        table.setStyle("-fx-background-color: #0C0D0D; -fx-control-inner-background: #0C0D0D; "
                + "-fx-control-inner-background-alt: #111212; -fx-table-cell-border-color: #202222; "
                + "-fx-table-header-border-color: #202222; -fx-selection-bar: #202222; "
                + "-fx-selection-bar-non-focused: #1B1D1D; -fx-text-background-color: " + TEXT + "; "
                + "-fx-border-color: #202222; -fx-border-radius: 10; -fx-background-radius: 10;");
        table.setFixedCellSize(54);
        table.skinProperty().addListener((observable, oldSkin, newSkin) -> Platform.runLater(() -> styleTableSkin(table)));
        Platform.runLater(() -> styleTableSkin(table));
    }

    public static void styleTableSkins(Parent root) {
        root.lookupAll(".table-view").stream()
                .filter(TableView.class::isInstance)
                .map(TableView.class::cast)
                .forEach(Theme::styleTableSkin);
    }

    private static void styleTableSkin(TableView<?> table) {
        setNodeStyle(table.lookupAll(".column-header-background"),
                "-fx-background-color: " + PANEL_LIGHT + ";");
        setNodeStyle(table.lookupAll(".column-header"),
                "-fx-background-color: " + PANEL_LIGHT + "; -fx-border-color: transparent #292B2B #292B2B transparent;");
        setNodeStyle(table.lookupAll(".column-header .label"),
                "-fx-text-fill: #9A9D9B; -fx-font-size: 9; -fx-font-weight: bold;");
        setNodeStyle(table.lookupAll(".filler"),
                "-fx-background-color: " + PANEL_LIGHT + "; -fx-border-color: transparent transparent #292B2B transparent;");
        setNodeStyle(table.lookupAll(".scroll-bar"), "-fx-background-color: #101111;");
        setNodeStyle(table.lookupAll(".scroll-bar .thumb"), "-fx-background-color: #353737; -fx-background-radius: 8;");
    }

    private static void setNodeStyle(java.util.Set<Node> nodes, String style) {
        nodes.forEach(node -> node.setStyle(style));
    }

    public static <T> void styleTextCell(TableCell<T, String> cell, String value) {
        cell.setText(value);
        cell.setTextFill(Color.web(value == null || value.isBlank() ? MUTED : TEXT));
        cell.setStyle("-fx-border-color: transparent transparent #202222 transparent; -fx-padding: 0 10;");
    }

    public static Label statusBadge(DeliveryStatus status) {
        Label label = new Label(status.getDisplayName());
        String color = statusColor(status);
        label.setTextFill(Color.web(color));
        label.setStyle("-fx-background-color: " + color + "20; -fx-border-color: " + color + "55; "
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11; "
                + "-fx-font-weight: bold; -fx-padding: 5 9;");
        return label;
    }

    public static String statusColor(DeliveryStatus status) {
        return switch (status) {
            case PENDING -> "#A1A3A2";
            case IN_TRANSIT -> BLUE;
            case OUT_FOR_DELIVERY -> AMBER;
            case DELIVERED -> GREEN;
            case FAILED -> RED;
        };
    }

    public static Background panelBackground(double radius) {
        return new Background(new BackgroundFill(Color.web(PANEL), new CornerRadii(radius), Insets.EMPTY));
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
