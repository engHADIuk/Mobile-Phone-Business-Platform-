package velmora.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import velmora.SceneManager;
import velmora.db.OrderItemDao;
import velmora.model.Order;
import velmora.model.OrderHistory;

public class TrackController implements Initializable {

    @FXML private ListView<Order> ordersListView;
    @FXML private ToggleButton filterAll, filterProcessing, filterShipped, filterDelivered;
    @FXML private DatePicker fromDatePicker, toDatePicker;

    @FXML private Label detailPlaceholder;
    @FXML private Label detailId, detailSummary, detailDate, detailTotal, detailStatus, detailCustomer, detailShipping, detailCard;
    @FXML private VBox detailItemsBox;

    private FilteredList<Order> filteredOrders;
    private Predicate<Order> statusPredicate = o -> true;
    private Order currentlySelected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filteredOrders = new FilteredList<>(OrderHistory.getInstance().getOrders(), o -> true);
        ordersListView.setItems(filteredOrders);
        ordersListView.setCellFactory(list -> buildRow());

        ordersListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldOrder, newOrder) -> { currentlySelected = newOrder; viewOrderDetail(newOrder); });

        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyCombinedFilter());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyCombinedFilter());

        Timeline refresher = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            filteredOrders.setPredicate(filteredOrders.getPredicate());
            ordersListView.refresh();
            if (currentlySelected != null) viewOrderDetail(currentlySelected);
        }));
        refresher.setCycleCount(Timeline.INDEFINITE);
        refresher.play();
    }

    private ListCell<Order> buildRow() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setGraphic(null);
                    return;
                }
                Label idLabel = new Label(order.getId());
                idLabel.getStyleClass().add("dash-card-title");

                Label summaryLabel = new Label(order.getSummary());
                summaryLabel.getStyleClass().add("dash-card-desc");
                summaryLabel.setWrapText(true);

                VBox left = new VBox(4, idLabel, summaryLabel);
                left.setMaxWidth(260);

                Label dateLabel = new Label(order.getFormattedDate());
                dateLabel.getStyleClass().add("dash-card-desc");

                Label totalLabel = new Label(String.format("$%.0f", order.getTotal()));
                totalLabel.getStyleClass().add("dash-card-title");

                Label statusLabel = new Label(order.getStatus().getLabel());
                statusLabel.getStyleClass().addAll("status-badge", order.getStatusStyleClass());

                VBox right = new VBox(4, dateLabel, totalLabel, statusLabel);
                right.setAlignment(Pos.TOP_RIGHT);

                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(12, left, spacer, right);
                row.setPadding(new Insets(10, 4, 10, 4));
                setGraphic(row);
            }
        };
    }

    private void viewOrderDetail(Order order) {
        boolean hasSelection = order != null;

        detailPlaceholder.setVisible(!hasSelection);
        detailPlaceholder.setManaged(!hasSelection);

        for (Label label : new Label[]{detailId, detailSummary, detailDate, detailTotal, detailStatus, detailCustomer, detailShipping, detailCard}) {
            label.setVisible(hasSelection);
            label.setManaged(hasSelection);
        }
        detailItemsBox.getChildren().clear();

        if (hasSelection) {
            detailId.setText(order.getId());
            detailSummary.setText(order.getSummary());
            detailDate.setText("Placed " + order.getFormattedDate());
            detailTotal.setText(String.format("$%.0f", order.getTotal()));
            detailStatus.setText("Status: " + order.getStatus().getLabel());
            detailCustomer.setText(order.getCustomerName() + "  •  " + order.getCustomerEmail());
            boolean hasCustomer = !order.getCustomerName().isBlank() || !order.getCustomerEmail().isBlank();
            detailCustomer.setVisible(hasCustomer);
            detailCustomer.setManaged(hasCustomer);

            boolean hasShipping = !order.getShippingAddress().isBlank();
            detailShipping.setText("Ship to: " + order.getShippingAddress());
            detailShipping.setVisible(hasShipping);
            detailShipping.setManaged(hasShipping);

            boolean hasCard = !order.getCardLast4().isBlank();
            detailCard.setText("Card ending in " + order.getCardLast4());
            detailCard.setVisible(hasCard);
            detailCard.setManaged(hasCard);

            List<OrderItemDao.Row> lines = OrderHistory.getInstance().getLineItems(order.getId());
            for (OrderItemDao.Row line : lines) {
                Label lineLabel = new Label(String.format("%s — %s, %s  x%d  ($%.0f)",
                        line.productName, line.color, line.storage, line.qty, line.lineTotal));
                lineLabel.getStyleClass().add("dash-card-desc");
                lineLabel.setWrapText(true);
                detailItemsBox.getChildren().add(lineLabel);
            }
        }
    }

    private void selectOnly(ToggleButton selected) {
        filterAll.setSelected(selected == filterAll);
        filterProcessing.setSelected(selected == filterProcessing);
        filterShipped.setSelected(selected == filterShipped);
        filterDelivered.setSelected(selected == filterDelivered);
    }

    private void applyCombinedFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        filteredOrders.setPredicate(order -> {
            if (!statusPredicate.test(order)) return false;
            if (from != null && order.getDate().isBefore(from)) return false;
            if (to != null && order.getDate().isAfter(to)) return false;
            return true;
        });
    }

    @FXML
    private void onFilterAll() {
        selectOnly(filterAll);
        statusPredicate = o -> true;
        applyCombinedFilter();
    }

    @FXML
    private void onFilterProcessing() {
        selectOnly(filterProcessing);
        statusPredicate = o -> o.getStatus() == Order.Status.PROCESSING;
        applyCombinedFilter();
    }

    @FXML
    private void onFilterShipped() {
        selectOnly(filterShipped);
        statusPredicate = o -> o.getStatus() == Order.Status.SHIPPED;
        applyCombinedFilter();
    }

    @FXML
    private void onFilterDelivered() {
        selectOnly(filterDelivered);
        statusPredicate = o -> o.getStatus() == Order.Status.DELIVERED;
        applyCombinedFilter();
    }

    @FXML
    private void clearDateFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    @FXML
    private void goBack() {
        SceneManager.show("dashboard");
    }
}
