package uk.ac.brunel.delivery.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import uk.ac.brunel.delivery.model.DashboardStats;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.DeliveryEventBus;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.util.DeliveryTableConfigurer;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

import java.util.List;

public final class AdminDashboardController implements LifecycleController {
    @FXML private Label userLabel;
    @FXML private Label activeCount;
    @FXML private Label pendingCount;
    @FXML private Label deliveredCount;
    @FXML private Label failedCount;
    @FXML private Label notificationLabel;
    @FXML private Button assignButton;
    @FXML private Button generateButton;
    @FXML private Button deliveriesButton;
    @FXML private Button liveMapButton;
    @FXML private Button logoutButton;
    @FXML private TableView<Delivery> deliveryTable;
    @FXML private TableColumn<Delivery, String> idColumn;
    @FXML private TableColumn<Delivery, String> orderColumn;
    @FXML private TableColumn<Delivery, String> driverColumn;
    @FXML private TableColumn<Delivery, String> addressColumn;
    @FXML private TableColumn<Delivery, DeliveryStatus> statusColumn;
    @FXML private TableColumn<Delivery, Number> progressColumn;
    @FXML private TableColumn<Delivery, String> etaColumn;

    private DeliveryEventBus.Subscription subscription;

    @FXML
    private void initialize() {
        DeliveryTableConfigurer.configure(deliveryTable, idColumn, orderColumn, driverColumn, addressColumn,
                statusColumn, progressColumn, etaColumn, null);
        Theme.styleLight(assignButton);
        Theme.styleSecondary(generateButton);
        Theme.stylePrimary(deliveriesButton);
        Theme.styleSecondary(liveMapButton);
        Theme.styleSecondary(logoutButton);
        userLabel.setText(SessionContext.requireUser().displayName());
        notificationLabel.setText("Live simulation ready");
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
        DashboardStats stats = AppServices.deliveries().getDashboardStats();
        activeCount.setText(Long.toString(stats.active()));
        pendingCount.setText(Long.toString(stats.pending()));
        deliveredCount.setText(Long.toString(stats.deliveredToday()));
        failedCount.setText(Long.toString(stats.failed()));
        List<Delivery> all = AppServices.deliveries().findAll("", null);
        deliveryTable.setItems(FXCollections.observableArrayList(all.stream().limit(6).toList()));
    }

    @FXML
    private void generateSampleOrder() {
        Delivery delivery = AppServices.deliveries().generateSampleOrder(SessionContext.actorName());
        AppServices.events().publish();
        notificationLabel.setText(delivery.deliveryId() + " generated and ready to assign");
        deliveryTable.getSelectionModel().select(delivery);
    }

    @FXML
    private void assignSelected() {
        Delivery selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Theme.showError("Select a delivery", "Choose a pending delivery from the table first.");
            return;
        }
        if (selected.status() != DeliveryStatus.PENDING) {
            Theme.showError("Assignment unavailable", "Only pending deliveries can be assigned or reassigned.");
            return;
        }
        SceneManager.getInstance().showAssignDelivery(selected);
    }

    @FXML private void openDeliveries() { SceneManager.getInstance().showDeliveries(); }
    @FXML private void openLiveMap() { SceneManager.getInstance().showLiveMap(); }

    @FXML
    private void logout() {
        SessionContext.logout();
        SceneManager.getInstance().showLogin();
    }
}
