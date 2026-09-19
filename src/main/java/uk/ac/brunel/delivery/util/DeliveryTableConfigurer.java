package uk.ac.brunel.delivery.util;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;

import java.time.format.DateTimeFormatter;

public final class DeliveryTableConfigurer {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM HH:mm");

    private DeliveryTableConfigurer() {
    }

    public static void configure(
            TableView<Delivery> table,
            TableColumn<Delivery, String> idColumn,
            TableColumn<Delivery, String> orderColumn,
            TableColumn<Delivery, String> driverColumn,
            TableColumn<Delivery, String> addressColumn,
            TableColumn<Delivery, DeliveryStatus> statusColumn,
            TableColumn<Delivery, Number> progressColumn,
            TableColumn<Delivery, String> etaColumn,
            TableColumn<Delivery, String> scheduledColumn
    ) {
        Theme.styleTable(table);
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().deliveryId()));
        orderColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().orderReference()));
        driverColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().driverDisplayName()));
        addressColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().deliveryAddress()));
        statusColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().status()));
        progressColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().progress()));
        etaColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().estimatedMinutes() + " min"));
        if (scheduledColumn != null) {
            scheduledColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    data.getValue().scheduledStart() == null ? "Not scheduled" : DATE_TIME.format(data.getValue().scheduledStart())
            ));
            styleStringColumn(scheduledColumn, true);
        }

        styleStringColumn(idColumn, false);
        styleStringColumn(orderColumn, true);
        styleStringColumn(driverColumn, false);
        styleStringColumn(addressColumn, true);
        styleStringColumn(etaColumn, true);

        statusColumn.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(DeliveryStatus status, boolean empty) {
                super.updateItem(status, empty);
                setText(null);
                setGraphic(empty || status == null ? null : Theme.statusBadge(status));
                setStyle("-fx-border-color: transparent transparent #202222 transparent; -fx-padding: 0 10;");
            }
        });

        progressColumn.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(Number progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setGraphic(null);
                    return;
                }
                double fraction = progress.doubleValue() / 100.0;
                ProgressBar bar = new ProgressBar(fraction);
                bar.setPrefWidth(80);
                bar.setStyle("-fx-accent: " + progressColor(getTableRow().getItem()) + ";");
                Label percent = new Label(progress.intValue() + "%");
                percent.setTextFill(Color.web(Theme.MUTED));
                percent.setStyle("-fx-font-size: 10;");
                HBox content = new HBox(8, bar, percent);
                content.setAlignment(Pos.CENTER_LEFT);
                setGraphic(content);
                setStyle("-fx-border-color: transparent transparent #202222 transparent; -fx-padding: 0 10;");
            }
        });
    }

    private static void styleStringColumn(TableColumn<Delivery, String> column, boolean muted) {
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value);
                setTextFill(Color.web(muted ? Theme.MUTED : Theme.TEXT));
                setStyle("-fx-border-color: transparent transparent #202222 transparent; -fx-padding: 0 10;");
            }
        });
    }

    private static String progressColor(Delivery delivery) {
        return delivery == null ? Theme.BLUE : Theme.statusColor(delivery.status());
    }
}
