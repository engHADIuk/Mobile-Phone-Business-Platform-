package uk.ac.brunel.delivery.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.DeliveryStatus;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.DeliveryEventBus;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.util.DeliveryTableConfigurer;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

public final class DeliveriesController implements LifecycleController {
    @FXML private Label userLabel;
    @FXML private Label resultLabel;
    @FXML private Label activeFilterLabel;
    @FXML private TextField searchField;
    @FXML private Button assignButton;
    @FXML private Button detailButton;
    @FXML private Button generateButton;
    @FXML private Button homeButton;
    @FXML private Button mapButton;
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

    private DeliveryStatus activeFilter;
    private DeliveryEventBus.Subscription subscription;

    @FXML
    private void initialize() {
        DeliveryTableConfigurer.configure(deliveryTable, idColumn, orderColumn, driverColumn, addressColumn,
                statusColumn, progressColumn, etaColumn, scheduledColumn);
        Theme.styleInput(searchField);
        Theme.styleLight(assignButton);
        Theme.styleSecondary(detailButton);
        Theme.styleSecondary(generateButton);
        Theme.stylePrimary(homeButton);
        Theme.styleSecondary(mapButton);
        Theme.styleSecondary(logoutButton);
        userLabel.setText(SessionContext.requireUser().displayName());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refresh());
        deliveryTable.setRowFactory(table -> {
            TableRow<Delivery> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SceneManager.getInstance().showDeliveryDetail(row.getItem());
                }
            });
            return row;
        });
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
        var deliveries = AppServices.deliveries().findAll(searchField.getText(), activeFilter);
        deliveryTable.setItems(FXCollections.observableArrayList(deliveries));
        resultLabel.setText(deliveries.size() + " deliveries");
        activeFilterLabel.setText(activeFilter == null ? "Showing all statuses" : "Filter: " + activeFilter);
    }

    @FXML private void filterAll() { setFilter(null); }
    @FXML private void filterPending() { setFilter(DeliveryStatus.PENDING); }
    @FXML private void filterTransit() { setFilter(DeliveryStatus.IN_TRANSIT); }
    @FXML private void filterOutForDelivery() { setFilter(DeliveryStatus.OUT_FOR_DELIVERY); }
    @FXML private void filterDelivered() { setFilter(DeliveryStatus.DELIVERED); }
    @FXML private void filterFailed() { setFilter(DeliveryStatus.FAILED); }

    private void setFilter(DeliveryStatus filter) {
        activeFilter = filter;
        refresh();
    }

    @FXML
    private void generateSampleOrder() {
        Delivery delivery = AppServices.deliveries().generateSampleOrder(SessionContext.actorName());
        activeFilter = null;
        searchField.clear();
        refresh();
        deliveryTable.getSelectionModel().select(delivery);
        AppServices.events().publish();
    }

    @FXML
    private void assignSelected() {
        Delivery selected = selectedDelivery();
        if (selected == null) {
            return;
        }
        if (selected.status() != DeliveryStatus.PENDING) {
            Theme.showError("Assignment unavailable", "Only pending deliveries can be assigned or reassigned.");
            return;
        }
        SceneManager.getInstance().showAssignDelivery(selected);
    }

    @FXML
    private void showDetails() {
        Delivery selected = selectedDelivery();
        if (selected != null) {
            SceneManager.getInstance().showDeliveryDetail(selected);
        }
    }

    private Delivery selectedDelivery() {
        Delivery selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Theme.showError("Select a delivery", "Choose a delivery from the table first.");
        }
        return selected;
    }

    @FXML private void openHome() { SceneManager.getInstance().showAdminDashboard(); }
    @FXML private void openLiveMap() { SceneManager.getInstance().showLiveMap(); }

    @FXML
    private void logout() {
        SessionContext.logout();
        SceneManager.getInstance().showLogin();
    }
}
