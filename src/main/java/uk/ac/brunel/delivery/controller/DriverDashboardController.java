package uk.ac.brunel.delivery.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.model.SessionUser;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.DeliveryEventBus;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.service.ValidationException;
import uk.ac.brunel.delivery.util.DeliveryTableConfigurer;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

public final class DriverDashboardController implements LifecycleController {
    @FXML private Label driverNameLabel;
    @FXML private Label activeDeliveryLabel;
    @FXML private Label queueCountLabel;
    @FXML private Label liveStatusLabel;
    @FXML private Button detailButton;
    @FXML private Button failButton;
    @FXML private Button logoutButton;
    @FXML private TableView<Delivery> deliveryTable;
    @FXML private TableColumn<Delivery, String> idColumn;
    @FXML private TableColumn<Delivery, String> orderColumn;
    @FXML private TableColumn<Delivery, String> driverColumn;
    @FXML private TableColumn<Delivery, String> addressColumn;
    @FXML private TableColumn<Delivery, DeliveryStatus> statusColumn;
    @FXML private TableColumn<Delivery, Number> progressColumn;
    @FXML private TableColumn<Delivery, String> etaColumn;
    @FXML private TableColumn<Delivery, String> scheduledColumn;

    private SessionUser user;
    private DeliveryEventBus.Subscription subscription;

    @FXML
    private void initialize() {
        user = SessionContext.requireUser();
        if (!user.isDriver() || user.driverId() == null) {
            throw new IllegalStateException("A driver account is required for this screen");
        }
        DeliveryTableConfigurer.configure(deliveryTable, idColumn, orderColumn, driverColumn, addressColumn,
                statusColumn, progressColumn, etaColumn, scheduledColumn);
        Theme.stylePrimary(detailButton);
        Theme.styleDanger(failButton);
        Theme.styleSecondary(logoutButton);
        driverNameLabel.setText(user.displayName());
        liveStatusLabel.setText("Automatic route simulation is running");
    }

    @Override
    public void onShow() {
        refresh();
        subscription = AppServices.events().subscribe(this::refresh);
    }

    @Override
    public void onHide() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    @FXML
    private void refresh() {
        var queue = AppServices.deliveries().findForDriver(user.driverId());
        deliveryTable.setItems(FXCollections.observableArrayList(queue));
        long pending = queue.stream().filter(delivery -> delivery.status() == DeliveryStatus.PENDING).count();
        queueCountLabel.setText(Long.toString(pending));
        String active = queue.stream().filter(delivery -> delivery.status().isActive())
                .map(delivery -> delivery.deliveryId() + " - " + delivery.status())
                .findFirst().orElse("No active delivery");
        activeDeliveryLabel.setText(active);
    }

    @FXML
    private void showDetails() {
        Delivery selected = selectedDelivery();
        if (selected != null) {
            SceneManager.getInstance().showDeliveryDetail(selected);
        }
    }

    @FXML
    private void reportFailed() {
        Delivery selected = selectedDelivery();
        if (selected == null) {
            return;
        }
        if (!selected.status().isActive()) {
            Theme.showError("Cannot report failure", "Only the currently active delivery can be marked as failed.");
            return;
        }
        try {
            AppServices.deliveries().updateStatus(selected.deliveryId(), DeliveryStatus.FAILED,
                    SessionContext.actorName(), "Driver reported that the delivery attempt failed");
            AppServices.events().publish();
        } catch (ValidationException exception) {
            Theme.showError("Status update rejected", exception.getMessage());
        }
    }

    private Delivery selectedDelivery() {
        Delivery selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Theme.showError("Select a delivery", "Choose one of your assigned deliveries first.");
        }
        return selected;
    }

    @FXML
    private void logout() {
        SessionContext.logout();
        SceneManager.getInstance().showLogin();
    }
}
