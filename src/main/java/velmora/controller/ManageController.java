package velmora.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

import velmora.SceneManager;
import velmora.db.AdminDao;
import velmora.db.UserDao;
import velmora.model.Order;
import velmora.model.OrderHistory;
import velmora.model.Session;

public class ManageController implements Initializable {

    @FXML private VBox roleSelectBox;
    @FXML private VBox adminLoginBox;
    @FXML private HBox customerContent;
    @FXML private HBox adminContent;

    @FXML private TextField adminUsernameField;
    @FXML private PasswordField adminPasswordField;
    @FXML private Label adminLoginError;

    @FXML private ListView<Order> customerOrdersListView;
    @FXML private Label customerPlaceholder;
    @FXML private Label customerDetailId, customerDetailSummary, customerDetailDate, customerDetailTotal, customerDetailStatus, customerCancelledNotice;
    @FXML private TextField cancelEmailField;
    @FXML private PasswordField cancelPasswordField;
    @FXML private Label cancelError;
    @FXML private Button cancelButton;

    @FXML private ListView<Order> adminOrdersListView;
    @FXML private Label adminPlaceholder;
    @FXML private Label adminDetailId, adminDetailCustomer, adminDetailSummary, adminDetailDate, adminDetailTotal, adminDetailStatus, adminDetailShipping, adminDetailCard;
    @FXML private Button markShippedButton, markDeliveredButton, adminCancelButton;

    private final AdminDao adminDao = new AdminDao();
    private final UserDao userDao = new UserDao();

    private FilteredList<Order> customerOrders;
    private FilteredList<Order> adminOrders;
    private Order selectedCustomerOrder;
    private Order selectedAdminOrder;
    private boolean awaitingCancelConfirm = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        customerOrders = new FilteredList<>(OrderHistory.getInstance().getOrders(),
                o -> o.getStatus() == Order.Status.PROCESSING || o.getStatus() == Order.Status.SHIPPED);
        customerOrdersListView.setItems(customerOrders);
        customerOrdersListView.setCellFactory(list -> buildCustomerRow());
        customerOrdersListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldOrder, newOrder) -> showCustomerOrder(newOrder));

        adminOrders = new FilteredList<>(OrderHistory.getInstance().getOrders(), o -> true);
        adminOrdersListView.setItems(adminOrders);
        adminOrdersListView.setCellFactory(list -> buildAdminRow());
        adminOrdersListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldOrder, newOrder) -> showAdminOrder(newOrder));

        Timeline refresher = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            customerOrders.setPredicate(customerOrders.getPredicate());
            adminOrders.setPredicate(adminOrders.getPredicate());
            customerOrdersListView.refresh();
            adminOrdersListView.refresh();
            if (selectedCustomerOrder != null) showCustomerOrder(selectedCustomerOrder);
            if (selectedAdminOrder != null) showAdminOrder(selectedAdminOrder);
        }));
        refresher.setCycleCount(Timeline.INDEFINITE);
        refresher.play();
    }

    @FXML
    private void chooseCustomerRole() {
        roleSelectBox.setVisible(false);
        roleSelectBox.setManaged(false);
        customerContent.setVisible(true);
        customerContent.setManaged(true);
    }

    @FXML
    private void chooseAdminRole() {
        roleSelectBox.setVisible(false);
        roleSelectBox.setManaged(false);
        adminLoginBox.setVisible(true);
        adminLoginBox.setManaged(true);
    }

    @FXML
    private void backToRoleSelect() {
        adminLoginBox.setVisible(false);
        adminLoginBox.setManaged(false);
        customerContent.setVisible(false);
        customerContent.setManaged(false);
        adminContent.setVisible(false);
        adminContent.setManaged(false);
        roleSelectBox.setVisible(true);
        roleSelectBox.setManaged(true);
    }

    @FXML
    private void adminLogin() {
        String username = adminUsernameField.getText() == null ? "" : adminUsernameField.getText().trim();
        String password = adminPasswordField.getText() == null ? "" : adminPasswordField.getText();

        if (!adminDao.checkCredentials(username, password)) {
            adminLoginError.setText("Incorrect username or password.");
            adminLoginError.setVisible(true);
            adminLoginError.setManaged(true);
            return;
        }

        adminLoginBox.setVisible(false);
        adminLoginBox.setManaged(false);
        adminContent.setVisible(true);
        adminContent.setManaged(true);
    }

    private ListCell<Order> buildCustomerRow() {
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

                Label statusLabel = new Label(order.getStatus().getLabel());
                statusLabel.getStyleClass().addAll("status-badge", order.getStatusStyleClass());

                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(12, left, spacer, statusLabel);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 4, 10, 4));
                setGraphic(row);
            }
        };
    }

    private ListCell<Order> buildAdminRow() {
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

                Label customerLabel = new Label(order.getCustomerName().isBlank() ? "No name given" : order.getCustomerName());
                customerLabel.getStyleClass().add("dash-card-desc");

                VBox left = new VBox(4, idLabel, customerLabel);
                left.setMaxWidth(220);

                Label statusLabel = new Label(order.getStatus().getLabel());
                statusLabel.getStyleClass().addAll("status-badge", order.getStatusStyleClass());

                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(12, left, spacer, statusLabel);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 4, 10, 4));
                setGraphic(row);
            }
        };
    }

    private void showCustomerOrder(Order order) {
        selectedCustomerOrder = order;
        awaitingCancelConfirm = false;
        boolean hasSelection = order != null;

        customerPlaceholder.setVisible(!hasSelection);
        customerPlaceholder.setManaged(!hasSelection);
        customerCancelledNotice.setVisible(false);
        customerCancelledNotice.setManaged(false);
        hideCancelConfirmFields();

        for (Label label : new Label[]{customerDetailId, customerDetailSummary, customerDetailDate, customerDetailTotal, customerDetailStatus}) {
            label.setVisible(hasSelection);
            label.setManaged(hasSelection);
        }
        cancelButton.setText("Cancel Order");
        cancelButton.setVisible(hasSelection);
        cancelButton.setManaged(hasSelection);

        if (hasSelection) {
            customerDetailId.setText(order.getId());
            customerDetailSummary.setText(order.getSummary());
            customerDetailDate.setText("Placed " + order.getFormattedDate());
            customerDetailTotal.setText(String.format("$%.0f", order.getTotal()));
            customerDetailStatus.setText("Status: " + order.getStatus().getLabel());
        }
    }

    private void hideCancelConfirmFields() {
        cancelEmailField.clear();
        cancelPasswordField.clear();
        cancelEmailField.setVisible(false);
        cancelEmailField.setManaged(false);
        cancelPasswordField.setVisible(false);
        cancelPasswordField.setManaged(false);
        cancelError.setVisible(false);
        cancelError.setManaged(false);
    }

    @FXML
    private void cancelSelectedOrder() {
        if (selectedCustomerOrder == null) return;

        if (!awaitingCancelConfirm) {
            awaitingCancelConfirm = true;
            cancelEmailField.setVisible(true);
            cancelEmailField.setManaged(true);
            cancelPasswordField.setVisible(true);
            cancelPasswordField.setManaged(true);
            cancelButton.setText("Confirm Cancel");
            return;
        }

        String email = cancelEmailField.getText() == null ? "" : cancelEmailField.getText().trim();
        String password = cancelPasswordField.getText() == null ? "" : cancelPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            cancelError.setText("Enter your email and password to confirm.");
            cancelError.setVisible(true);
            cancelError.setManaged(true);
            return;
        }
        if (!email.equalsIgnoreCase(Session.getEmail()) || !userDao.checkCredentials(email, password)) {
            cancelError.setText("That email and password don't match your logged-in account.");
            cancelError.setVisible(true);
            cancelError.setManaged(true);
            return;
        }

        OrderHistory.getInstance().updateStatus(selectedCustomerOrder, Order.Status.CANCELLED);
        customerOrders.setPredicate(customerOrders.getPredicate());

        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
        hideCancelConfirmFields();
        customerCancelledNotice.setVisible(true);
        customerCancelledNotice.setManaged(true);
    }

    private void showAdminOrder(Order order) {
        selectedAdminOrder = order;
        boolean hasSelection = order != null;

        adminPlaceholder.setVisible(!hasSelection);
        adminPlaceholder.setManaged(!hasSelection);

        for (Label label : new Label[]{adminDetailId, adminDetailCustomer, adminDetailSummary, adminDetailDate, adminDetailTotal, adminDetailStatus, adminDetailShipping, adminDetailCard}) {
            label.setVisible(hasSelection);
            label.setManaged(hasSelection);
        }

        if (!hasSelection) {
            markShippedButton.setVisible(false);
            markShippedButton.setManaged(false);
            markDeliveredButton.setVisible(false);
            markDeliveredButton.setManaged(false);
            adminCancelButton.setVisible(false);
            adminCancelButton.setManaged(false);
            return;
        }

        adminDetailId.setText(order.getId());
        String customerText = order.getCustomerName().isBlank() && order.getCustomerEmail().isBlank()
                ? "No customer details on file"
                : order.getCustomerName() + "  •  " + order.getCustomerEmail();
        adminDetailCustomer.setText(customerText);
        adminDetailSummary.setText(order.getSummary());
        adminDetailDate.setText("Placed " + order.getFormattedDate());
        adminDetailTotal.setText(String.format("$%.0f", order.getTotal()));
        adminDetailStatus.setText("Status: " + order.getStatus().getLabel());

        boolean hasShipping = !order.getShippingAddress().isBlank();
        adminDetailShipping.setText("Ship to: " + order.getShippingAddress());
        adminDetailShipping.setVisible(hasShipping);
        adminDetailShipping.setManaged(hasShipping);

        boolean hasCard = !order.getCardLast4().isBlank();
        adminDetailCard.setText("Card ending in " + order.getCardLast4());
        adminDetailCard.setVisible(hasCard);
        adminDetailCard.setManaged(hasCard);

        boolean canShip = order.getStatus() == Order.Status.PROCESSING;
        boolean canDeliver = order.getStatus() == Order.Status.SHIPPED;
        boolean canCancel = order.getStatus() == Order.Status.PROCESSING || order.getStatus() == Order.Status.SHIPPED;

        markShippedButton.setVisible(canShip);
        markShippedButton.setManaged(canShip);
        markDeliveredButton.setVisible(canDeliver);
        markDeliveredButton.setManaged(canDeliver);
        adminCancelButton.setVisible(canCancel);
        adminCancelButton.setManaged(canCancel);
    }

    @FXML
    private void markShipped() {
        if (selectedAdminOrder == null) return;
        OrderHistory.getInstance().updateStatus(selectedAdminOrder, Order.Status.SHIPPED);
        adminOrders.setPredicate(adminOrders.getPredicate());
        showAdminOrder(selectedAdminOrder);
    }

    @FXML
    private void markDelivered() {
        if (selectedAdminOrder == null) return;
        OrderHistory.getInstance().updateStatus(selectedAdminOrder, Order.Status.DELIVERED);
        adminOrders.setPredicate(adminOrders.getPredicate());
        showAdminOrder(selectedAdminOrder);
    }

    @FXML
    private void adminCancelOrder() {
        if (selectedAdminOrder == null) return;
        OrderHistory.getInstance().updateStatus(selectedAdminOrder, Order.Status.CANCELLED);
        adminOrders.setPredicate(adminOrders.getPredicate());
        showAdminOrder(selectedAdminOrder);
    }

    @FXML
    private void goBack() {
        SceneManager.show("dashboard");
    }
}
