package uk.ac.brunel.delivery.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.StatusLog;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

import java.time.format.DateTimeFormatter;

public final class DeliveryDetailController {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    @FXML private Label deliveryIdLabel;
    @FXML private Label orderReferenceLabel;
    @FXML private Label driverLabel;
    @FXML private Label addressLabel;
    @FXML private Label warehouseLabel;
    @FXML private Label scheduleLabel;
    @FXML private Label etaLabel;
    @FXML private Label statusContainer;
    @FXML private Button backButton;
    @FXML private Button assignButton;
    @FXML private TableView<StatusLog> auditTable;
    @FXML private TableColumn<StatusLog, String> timestampColumn;
    @FXML private TableColumn<StatusLog, String> transitionColumn;
    @FXML private TableColumn<StatusLog, String> actorColumn;
    @FXML private TableColumn<StatusLog, String> noteColumn;

    private Delivery delivery;

    @FXML
    private void initialize() {
        Theme.styleTable(auditTable);
        Theme.stylePrimary(backButton);
        Theme.styleSecondary(assignButton);
        timestampColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(DATE_TIME.format(data.getValue().changedAt())));
        transitionColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().fromDisplay() + " -> " + data.getValue().toStatus().getDisplayName()));
        actorColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().changedBy()));
        noteColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().note()));
        styleColumn(timestampColumn, true);
        styleColumn(transitionColumn, false);
        styleColumn(actorColumn, true);
        styleColumn(noteColumn, true);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = AppServices.deliveries().findById(delivery.deliveryId()).orElse(delivery);
        deliveryIdLabel.setText(this.delivery.deliveryId());
        orderReferenceLabel.setText(this.delivery.orderReference());
        driverLabel.setText(this.delivery.driverDisplayName());
        addressLabel.setText(this.delivery.deliveryAddress());
        warehouseLabel.setText(this.delivery.warehouseLocation());
        scheduleLabel.setText(this.delivery.scheduledDisplay());
        etaLabel.setText(this.delivery.estimatedMinutes() + " minutes");
        statusContainer.setGraphic(Theme.statusBadge(this.delivery.status()));
        statusContainer.setText("");
        assignButton.setVisible(SessionContext.requireUser().role().isAdministrator()
                && this.delivery.status() == uk.ac.brunel.delivery.model.DeliveryStatus.PENDING);
        assignButton.setManaged(assignButton.isVisible());
        auditTable.setItems(FXCollections.observableArrayList(
                AppServices.deliveries().findStatusLogs(this.delivery.deliveryId())
        ));
    }

    @FXML
    private void assignDelivery() {
        SceneManager.getInstance().showAssignDelivery(delivery);
    }

    @FXML
    private void goBack() {
        if (SessionContext.requireUser().isDriver()) {
            SceneManager.getInstance().showDriverDashboard();
        } else {
            SceneManager.getInstance().showDeliveries();
        }
    }

    private void styleColumn(TableColumn<StatusLog, String> column, boolean muted) {
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
}
